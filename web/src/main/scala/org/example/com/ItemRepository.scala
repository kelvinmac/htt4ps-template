package org.example.com

import cats.effect.{IO, Resource}
import cats._
import cats.data._
import cats.implicits._
import com.codahale.metrics.MetricRegistry
import org.example.com.repositories.{Repository, RepositoryItem}

import java.util.UUID

class ItemRepository(metricRegistry: MetricRegistry) extends Repository[IO] {
  override def get(id: String): IO[Option[RepositoryItem]] = {
    id match {
      case "failure" => incrementFailureMetrics() >> IO(None)
      case _         => incrementSuccessMetrics() >> retrieveItem()
    }
  }

  private def retrieveItem(): IO[Option[RepositoryItem]] =
    IO(Some(RepositoryItem(name = "repository-item", id = UUID.randomUUID())))

  private def incrementFailureMetrics(): IO[Unit] =
    IO(metricRegistry.meter("item-repository.failure").mark())

  private def incrementSuccessMetrics(): IO[Unit] =
    IO(metricRegistry.meter("item-repository.success").mark())
}

object ItemRepository {
  def apply()(implicit metricRegistry: MetricRegistry): Resource[IO, ItemRepository] = {
    Resource.eval(IO(new ItemRepository(metricRegistry)))
  }
}
