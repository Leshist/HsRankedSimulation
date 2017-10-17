package app

import java.util.UUID

import akka.actor.ActorSystem
import akka.actor.Props
import com.typesafe.config.ConfigFactory
import config.SimulationConfig
import database.CassandraSeasonedWriter
import hactors.DbWriter.{CloseDbRequest, StoreSimulation}
import hactors.{DbWriter, Papa}
import hactors.Papa.StartTheParty
import hactors.ResultCollector.DeckResult
import pureconfig.loadConfigOrThrow
import results.ResultHdfsWriter
import io.getquill._

import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global


object Main {
  def main(args: Array[String]): Unit = {
    val simConfig = loadConfigOrThrow[SimulationConfig]
    val akkaConfig = ConfigFactory.load()
    val system = ActorSystem("hs_simulation", akkaConfig)
    val papa = system.actorOf(Props(new Papa(simConfig)))
    papa ! StartTheParty
  }
}