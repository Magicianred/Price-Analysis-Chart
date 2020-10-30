name := "cimri-staj-example"

organization := "com.cimri"

version := "1.0.0-SNAPSHOT"

scalaVersion := "2.13.2"

organization := "com.cimri"

libraryDependencies ++= {
  val akkaV = "2.6.8"
  val alpakkaCsv = "2.0.2"
  val kafka = "2.0.5"
  val scraper =  "2.2.0"
  val lift = "3.4.2"



  Seq(
    "com.typesafe.akka" %% "akka-stream" % akkaV,
    "com.typesafe.akka" %% "akka-slf4j" % akkaV,
    "ch.qos.logback" % "logback-classic" % "1.1.3" % Runtime,
    "org.postgresql" % "postgresql" % "9.3-1102-jdbc41",
    "com.lightbend.akka" %% "akka-stream-alpakka-csv" % alpakkaCsv,
    "com.typesafe.akka" %% "akka-stream-kafka" % kafka ,
    "net.ruippeixotog" %% "scala-scraper" % scraper,
    "net.liftweb" %% "lift-json" % lift,
    "com.lightbend.akka" %% "akka-stream-alpakka-cassandra" % "2.0.2",
    "com.lightbend.akka"      %% "akka-stream-alpakka-elasticsearch" % "2.0.2",
    "com.typesafe.akka" %% "akka-discovery" % akkaV,
    "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.10",
    "com.typesafe.akka"          %% "akka-http"            % "10.1.10",
    "com.typesafe.scala-logging" %% "scala-logging"        % "3.9.2",

  )
}


