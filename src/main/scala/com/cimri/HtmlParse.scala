package com.cimri

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.model.Element
import org.jsoup.HttpStatusException


/*
NOTE: Document >> some selection -> error when selection does not exist
      Document >?> some selection -> Returns Option; if exists Some(), not exists None
 */

case class Product(url: String, title: String, prices: List[Map[String, Any]])

object HtmlParse extends App{

  def processURL(url: String) = {

    val browser = JsoupBrowser()

    // Document of URL

      val doc = browser.get(url)

    // get product title by class
    val productTitle = doc >> text("div [class = s1wytv2f-2 jTAVuj]")
    //println(productTitle)

    // tr list
    val offers = doc >> elementList("[class = s17f9cy4-3 bkOZxz]")

    def offersToMap(trList: List[Element]) = {

      var shopPrices: scala.collection.mutable.Map[String, Double] = scala.collection.mutable.Map()

      // for an offer, a shop
      for (tr <- trList) {
        val tdList = tr >> elementList("td")

        // get shop url
        val shopUrl = tdList(0) >> element("img") >> attr("alt")

        // get shop price
        val tdPriceArr = (tdList(3) >> text("[class=s17f9cy4-14 ifXJMM]")).split(" +")
        val price = tdPriceArr(tdPriceArr.length - 2).replace(".", "").replace(",", ".").toDouble

        val mapPrice = shopPrices.get(shopUrl)

        mapPrice match {
          case None => shopPrices += (shopUrl -> price)
          case Some(p) => if (p > price) shopPrices += (shopUrl -> price)
        }

      }
      shopPrices.toMap
    }


    import net.liftweb.json.Serialization.write
    import net.liftweb.json._

    val offerMap = offersToMap(offers)
    val listOfShops = {
      for((url, price)  <- offerMap) yield Map("merchantUrl" -> url, "price" -> price)
    }

    val curProduct = Product(url, productTitle, listOfShops.toList)
    implicit val formats = DefaultFormats
    val jsonStr = write(curProduct)

    jsonStr

  }
}
