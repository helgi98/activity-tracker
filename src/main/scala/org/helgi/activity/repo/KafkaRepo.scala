package org.helgi.activity.repo

import io.grpc.Status
import org.apache.kafka.clients.producer.{ProducerConfig, ProducerRecord}
import org.helgi.activity.config.AppConfig
import org.helgi.activity.model.User
import org.helgi.activity.service.activity_tracker.{AggregatedActivityData, LiveActivityData}
import zio.kafka.producer.{Producer, ProducerSettings}
import zio.kafka.serde.Serde
import zio.{ZIO, ZLayer, stream}


case class KafkaRepo(config: AppConfig) {
  private val producerSettings = ProducerSettings(config.kafka.bootstrapUrls)
    .withProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
    .withProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializer")

  private val producerZIO = ZLayer.fromZIO(ZIO.scoped(Producer.make(producerSettings)))

  private val liveDataSerde: Serde[Any, LiveActivityData] =
    Serde.byteArray.inmap(LiveActivityData.parseFrom)(_.toByteArray)
  private val aggregatedDataSerde: Serde[Any, AggregatedActivityData] =
    Serde.byteArray.inmap(AggregatedActivityData.parseFrom)(_.toByteArray)

  private val liveDataPipeline = Producer.produceAll(Serde.string, liveDataSerde)

  def streamLiveData(user: User, liveData: stream.Stream[Status, LiveActivityData]): ZIO[Any, Status, Unit] =
    liveData.map {
      new ProducerRecord(config.kafka.topics.liveActivityData, user.id, _)
    }
      .via(liveDataPipeline)
      .runDrain
      .provide(producerZIO)
      .mapError {
        _ => Status.INTERNAL
      }

  def sendAggregatedData(user: User, aggregatedData: AggregatedActivityData): ZIO[Any, Status, Unit] =
    Producer.produce(
      new ProducerRecord(config.kafka.topics.aggActivityData, user.id, aggregatedData),
      Serde.string,
      aggregatedDataSerde
    ).provide(producerZIO)
      .mapError {
        _ => Status.INTERNAL
      }.unit
}

object KafkaRepo {
  val live: ZLayer[AppConfig, Nothing, KafkaRepo] = ZLayer.fromFunction(KafkaRepo.apply _)
}