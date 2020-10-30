package com.cimri.kafka

import akka.Done
import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.scaladsl.{FileIO, Source}
import akka.stream.{ActorAttributes, ActorMaterializer, IOResult, Materializer, Supervision}
import com.cimri.CsvMain
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.jsoup.HttpStatusException

import scala.concurrent.{ExecutionContextExecutor, Future}

object KafkaProducer {

  implicit val actorSystem: ActorSystem = ActorSystem("alpakka-kafka-actor")

  final val bootstrapServer: String = ConfigFactory.load().getString("kafka.bootstrap-server")

  final val topic: String = ConfigFactory.load().getString("kafka.topic")

  implicit val ec: ExecutionContextExecutor = actorSystem.dispatcher

  implicit val mat: Materializer = ActorMaterializer()

  private val kafkaProducerSettings = ProducerSettings(actorSystem, new StringSerializer, new StringSerializer)
    .withBootstrapServers(bootstrapServer)

  def runProducer: Future[Done] = {

    val decider: Supervision.Decider = {
      case _: HttpStatusException => Supervision.Stop
      case _                      => Supervision.Resume
    }

    val jsonFlow = {
      println("jsonFlow Producer")
      val c = CsvMain.process
      println("csvMain done:" + c)
      c
    }


    val producerFlow: Source[ProducerRecord[String, String], Future[IOResult]] = jsonFlow.map { item: String =>
      new ProducerRecord[String, String](topic, item, item)
    }

    producerFlow
      .withAttributes(ActorAttributes.supervisionStrategy(decider))
      .runWith(Producer.plainSink(kafkaProducerSettings))

  }

}
