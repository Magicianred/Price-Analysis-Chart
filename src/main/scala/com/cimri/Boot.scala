package com.cimri

import akka.actor.ActorSystem
import com.cimri.kafka.{KafkaConsumer, KafkaProducer}

import scala.concurrent.ExecutionContextExecutor
import scala.util.control.Breaks.break
import scala.util.{Failure, Success}


object Boot extends App {

  implicit val actorSystem: ActorSystem = ActorSystem("alpakka-kafka-actor")

  implicit val ec: ExecutionContextExecutor = actorSystem.dispatcher

  while(true){
    KafkaProducer.runProducer.onComplete {
      case Failure(exception) => throw exception
      case _: Success[_] => {
        print("now Consumer")
        KafkaConsumer.runConsumer
      }

    }

    Thread.sleep(7200000)
    println("Working...")
  }
}
