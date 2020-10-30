package com.cimri.kafka

import akka.NotUsed
import akka.actor.ActorSystem
import akka.kafka.ConsumerMessage.{CommittableMessage, CommittableOffset}
import akka.kafka.scaladsl.{Committer, Consumer}
import akka.kafka.{CommitterSettings, ConsumerMessage, ConsumerSettings, Subscriptions}
import akka.stream.alpakka.cassandra.CassandraWriteSettings
import akka.stream.alpakka.cassandra.scaladsl.CassandraFlow
import akka.stream.scaladsl.{FileIO, Flow, RunnableGraph, Source}
import akka.stream.{ActorMaterializer, Materializer}
import com.typesafe.config.ConfigFactory
import akka.stream.scaladsl.{FileIO, Flow, Sink}
import net.liftweb.json._
import com.datastax.oss.driver.api.core.cql.{BoundStatement, PreparedStatement}
import akka.stream.alpakka.cassandra.CassandraSessionSettings
import akka.stream.alpakka.cassandra.scaladsl.CassandraSession
import akka.stream.alpakka.cassandra.scaladsl.CassandraSessionRegistry
import org.apache.kafka.common.serialization.StringDeserializer

import scala.concurrent.{ExecutionContextExecutor, Future}
import java.util.Calendar

import com.cimri.Product
import com.datastax.oss.driver.api.core.session.SessionBuilder
import spray.json._
import DefaultJsonProtocol._

import scala.collection.immutable
import com.cimri._
import java.sql.{Connection, DriverManager, ResultSet}

import akka.discovery.Discovery
import com.github.nscala_time.time.Imports.DateTime
import com.github.nscala_time.time.StaticDateTimeFormat


case class toCassandra(url: String, date: String, merchantUrl: String, price: Double)

object KafkaConsumer {

  implicit val actorSystem: ActorSystem = ActorSystem("alpakka-kafka-actor")

  final val bootstrapServer: String = ConfigFactory.load().getString("kafka.bootstrap-server")

  final val groupId: String = ConfigFactory.load().getString("kafka.group-id")

  final val topic: String = ConfigFactory.load().getString("kafka.topic")

  implicit val ec: ExecutionContextExecutor = actorSystem.dispatcher

  implicit val mat: Materializer = ActorMaterializer()(actorSystem)


  val sessionSettings = CassandraSessionSettings("example-with-akka-discovery")
  implicit val cassandraSession: CassandraSession =
    CassandraSessionRegistry.get(actorSystem).sessionFor(sessionSettings)
  val serviceDiscovery = Discovery(actorSystem).discovery

  val kafkaConsumerSettings: ConsumerSettings[String, String] =
    ConsumerSettings(actorSystem, new StringDeserializer, new StringDeserializer)
      .withBootstrapServers(bootstrapServer)
      .withGroupId(groupId)
  val committerSettings = CommitterSettings(actorSystem)

  def runConsumer = {

    println("Consumer is running...")

    val commiter = Committer.flowWithOffsetContext(committerSettings)

    val kafkaSource: Source[ConsumerMessage.CommittableMessage[String, String], Consumer.Control] = Consumer
      .committableSource(kafkaConsumerSettings, Subscriptions.topics(topic))

    val decodeMessageFlow: Source[(CommittableOffset, Product), Consumer.Control] = kafkaSource.via(decodeValueFlow)

    val commitFlow: Source[Product, Consumer.Control] = decodeMessageFlow.map { case (offset, data) =>
      offset.commitScaladsl()
      data
    }


    classOf[org.postgresql.Driver]
    val conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/database", "cimri", "err")


    def errFlow(p: Product): Unit = {
      p match {
        case Product(a, b, c) => {
          val lst = for (i <- c) yield toCassandra(a, Calendar.getInstance().getTime().toString, i.getOrElse("merchantUrl", " ").toString, i.getOrElse("price", " ").toString.toDouble)


          try {
            val stm = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)

            val rs = stm.executeQuery("INSERT INTO newTable VALUES ('" + a + "','" + b + "');");
            println("Inserting into Postgre...")

          } catch {
            case x => println(x)
          }

          val statementBinder: (toCassandra, PreparedStatement) => BoundStatement = {
            print("statement binder")
            (cassandraObj, preparedStatement) => preparedStatement.bind(cassandraObj.url, cassandraObj.merchantUrl, Double.box(cassandraObj.price))
          }

          //println((DateTime.now).toString(StaticDateTimeFormat.forPattern("yyyy-MM-dd HH:MM:SS")))

          val written: Future[immutable.Seq[toCassandra]] = Source(lst)
            .via(
              CassandraFlow.create(CassandraWriteSettings.defaults,
                s"INSERT INTO cimri.datePrice(url,merchanturl,date,price) VALUES (?,?,toTimestamp(now()),?) USING ttl 604800",
                statementBinder)
            )
            .runWith(Sink.seq)
        }
      }
    }

    val smt = commitFlow.runForeach(errFlow)

    smt
  }

  implicit val formats = DefaultFormats

  private def decodeValueFlow: Flow[CommittableMessage[String, String], (CommittableOffset, Product), NotUsed] =
    Flow[CommittableMessage[String, String]]
      .map { msg: CommittableMessage[String, String] => {
        println("decoding...")
        val messageValue = msg.record.value
        println(messageValue)
        val parsed = messageValue.parseJson
        val k: Product = JsonParserr.read(parsed)
        val c = (msg.committableOffset, k)

        c
      }
      }
}
