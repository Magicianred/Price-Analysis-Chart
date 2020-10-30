package com.cimri

import java.nio.file.Paths

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{FileIO, Flow, Sink}
import akka.stream.alpakka.csv.scaladsl.{CsvParsing, CsvToMap}

import scala.concurrent.duration.DurationInt




object CsvMain extends App {

  def process ={

    implicit val system: ActorSystem = ActorSystem()
    implicit val materializer: ActorMaterializer = ActorMaterializer()

    //
    val filePath = "src/main/resources/urls.csv"

    // create csv file as source
    val fileSrc = FileIO.fromPath(Paths.get(filePath))

    // define a process;
    val filterFlow = Flow[Map[String, String]].map(row => HtmlParse.processURL(row.getOrElse("url", "")))


    val future =
      fileSrc
        .via(CsvParsing.lineScanner(' ')) // Source[ByteString, NotUsed]#Repr[List[ByteString]]
        .via(CsvToMap.toMapAsStrings()) // Source[ByteString, NotUsed]#Repr[List[ByteString]]#Repr[Map[String, String]
        .throttle(1, 2.second)
        .via(filterFlow)


    future
  }

}
