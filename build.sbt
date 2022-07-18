ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.8"

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
resolvers += "confluent" at "https://packages.confluent.io/maven/"

Compile / PB.targets := Seq(
  scalapb.gen(grpc = true) -> (Compile / sourceManaged).value,
  scalapb.zio_grpc.ZioCodeGenerator -> (Compile / sourceManaged).value
)

lazy val root = (project in file("."))
  .settings(
    Common.settings: _*
  )
