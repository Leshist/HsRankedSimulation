package hactors

import akka.actor.{Actor, ActorRef, Props}
import akka.event.Logging
import hactors.Season.StartSeason
import config.SimulationConfig


class Papa(config: SimulationConfig) extends Actor {
  private val log = Logging(context.system, this)
  private val system = context.system

  def receive = {
    case Papa.StartTheParty => startSeason()
    case Papa.PartyIsOver => endSeason()
    case _ => log.info(s"unhandled msg for $self")
  }

  private def createSeasonActor(): ActorRef = system.actorOf(Props(new Season(config, self)))

  private def startSeason(): Unit = createSeasonActor() ! StartSeason

  private def endSeason(): Unit = {
    context.system.terminate()
    System.exit(0)
  }
}

object Papa {
  case object StartTheParty
  case object PartyIsOver
}