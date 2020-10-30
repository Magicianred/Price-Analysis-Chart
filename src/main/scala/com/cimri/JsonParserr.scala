package com.cimri

import spray.json.{DefaultJsonProtocol, DeserializationException, JsObject, JsString, JsValue, JsonWriter, RootJsonFormat,JsNumber,JsArray}
import com.cimri.Product
import spray.json._
import DefaultJsonProtocol._


object JsonParserr extends DefaultJsonProtocol {


    def arrToList(value:Vector[JsValue]):List[Map[String,Any]]=

      ( for(va:JsValue <- value)yield {
         va.asJsObject.getFields("merchantUrl", "price") match {
           case Seq(JsString(url), JsNumber(price)) =>
             Map("merchantUrl"->url,"price"->price)
           case _ => throw DeserializationException("Book expected")
         }


    }).toList

    def read(value: JsValue): Product = {
      value.asJsObject.getFields("url", "title", "prices") match {
        case Seq(JsString(url), JsString(title), JsArray(prices)) =>
          Product(url,title,arrToList(prices))
        case _ => throw DeserializationException("Book expected")
      }
    }



}



