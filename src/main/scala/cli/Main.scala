package cli

import akka.actor.ActorSystem
import akka.actor.Props
import com.typesafe.config.ConfigFactory
import config.SimulationConfig
import hactors.Papa
import hactors.Papa.StartTheParty
import pureconfig.loadConfigOrThrow

object Main {
  def main(args: Array[String]): Unit = {

    val simConfig = loadConfigOrThrow[SimulationConfig]
    val akkaConfig = ConfigFactory.load()
    val system = ActorSystem("hs_simulation", akkaConfig)
    val papa = system.actorOf(Props(new Papa(simConfig)))
    papa ! StartTheParty
  }
}