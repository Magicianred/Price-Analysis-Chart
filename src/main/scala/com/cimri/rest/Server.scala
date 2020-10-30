package com.cimri.rest

import java.sql.{DriverManager, ResultSet}
import akka.stream.alpakka.cassandra.scaladsl.CassandraSource

import scala.concurrent.{Future}
import akka.Done

import scala.util.{Failure, Success}
import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.{LazyLogging, StrictLogging}
import spray.json._
import akka.http.scaladsl.Http
import akka.stream.alpakka.cassandra.{CassandraSessionSettings}
import akka.stream.alpakka.cassandra.scaladsl.{CassandraSession, CassandraSessionRegistry}


case class Postgres(url: String, title: String)

case class Products(products: List[Postgres])

case class DatePrice(date: String, price: Double)
case class Merchant(merchantUrl: String, prices: List[DatePrice])

case class Product(url: String, merchants: List[Merchant])


object Rest extends SprayJsonSupport with DefaultJsonProtocol with LazyLogging {


  def getProductList = {
    implicit val postgresFormat = jsonFormat2(Postgres.apply)
    implicit val productFormat = jsonFormat1(Products.apply)

    classOf[org.postgresql.Driver]
    val conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/database", "cimri", "err")
    try {
      val stm = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)

      val rs = stm.executeQuery("select * from productUrl ;")

      var list: List[Postgres] = Nil
      while (rs.next) {
        val postgres: Postgres = Postgres(rs.getString("url"), rs.getString("title"))
        list = postgres :: list
      }
      print(Products(list).toJson.toString)
      Products(list).toJson
    } catch {
      case x => {
        println(x)
        Nil
      }
    }


  }

  var mymap: scala.collection.mutable.Map[String, List[DatePrice]] = scala.collection.mutable.Map()

  def getPrices(url: String): Future[Done] = {
    mymap = scala.collection.mutable.Map()

    val ids: Future[Done] =
      CassandraSource(s"select * from cimri.datePrice where url = '" + url + "' ")(Server.cassandraSession).map(row => {
        val dp = DatePrice(row.getInstant("date").toString, row.getDouble("price"))
        val sa = row.getString("merchanturl")
        mymap.put(sa, dp :: mymap.getOrElse(sa, Nil))
        dp
      }).run()(Server.materializer)
    ids

  }

  def func(url: String): String = {

    implicit val productFormat = jsonFormat2(DatePrice.apply)
    implicit val postgresFormat = jsonFormat2(Merchant.apply)
    implicit val postgres2Format = jsonFormat2(Product.apply)
    var lst: List[Merchant] = Nil
    for (key <- mymap.keys) {
      val merch = Merchant(key, mymap.getOrElse(key, Nil))
      lst = merch :: lst
    }

    val product = Product(url, lst)
    product.toJson.toString
  }


  val route: Route = get {
    path("productList") {
      complete(getProductList.toString)
    }
  } ~ post {
    path("productUrl") {
      entity(as[String]) { url =>
        val saved = Rest.getPrices(url)
        onComplete(saved) {
          case Success(a) => complete(
            func(url)
          )
          case Failure(e) => {
            logger.error(s"Failed to insert a person", e)
            complete(StatusCodes.InternalServerError)
          }
        }
      }
    }
  }


}


object Server extends App with StrictLogging {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()(system)
  implicit val ec = system.dispatcher


  val sessionSettings = CassandraSessionSettings()
  implicit val cassandraSession: CassandraSession =
    CassandraSessionRegistry.get(system).sessionFor(sessionSettings)

  Http()
    .bindAndHandle(Rest.route, "localhost", 8080)
    .map(_ => logger.info("Server started at port 8080"))(ec)

}
