import sbt.Keys._
import sbt._

object Common {
  private val sttpClient = "3.7.0"
  private val pureConfigVersion = "0.17.1"
  private val circeVersion = "0.14.2"
  private val zioKafkaVersion = "2.0.0"
  private val grpcVersion = "1.47.0"
  private val commonGoogleProtosVersion = "2.5.0-2"
  private val kafkaProtoSerializerVersion = "7.2.0"
  private val flywayVersion = "8.5.12"
  private val postgresVersion = "42.3.6"
  private val logbackVersion = "1.2.11"

  final val settings: Seq[Setting[_]] =
    projectSettings ++ dependencySettings ++ compilerPlugins

  private[this] def projectSettings = Seq(
    name := "activity-tracker"
  )

  private[this] def dependencySettings = Seq(
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.client3" %% "zio" % sttpClient, // for ZIO 2.x
      "com.softwaremill.sttp.client3" %% "circe" % sttpClient,
      "com.github.pureconfig" %% "pureconfig" % pureConfigVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "dev.zio" %% "zio-kafka" % zioKafkaVersion,
      "io.grpc" % "grpc-netty" % grpcVersion,
      "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion,
      "com.thesamet.scalapb.common-protos" %% "proto-google-common-protos-scalapb_0.11" % commonGoogleProtosVersion % "protobuf",
      "com.thesamet.scalapb.common-protos" %% "proto-google-common-protos-scalapb_0.11" % commonGoogleProtosVersion,
      "io.confluent" % "kafka-protobuf-serializer" % kafkaProtoSerializerVersion,
      "org.flywaydb" % "flyway-core" % flywayVersion,
      "org.postgresql" % "postgresql" % postgresVersion,
      "ch.qos.logback" % "logback-classic" % logbackVersion,
    )
  )

  private[this] def compilerPlugins = Seq(
  )
}