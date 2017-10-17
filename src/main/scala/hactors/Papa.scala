package hactors

import akka.actor.{Actor, ActorRef, Props}
import akka.event.Logging
import hactors.Season.StartSeason
import config.SimulationConfig


class Papa(config: SimulationConfig) extends Actor {
  private val log = Logging(context.system, this)
  private val system = context.system
  private val season = createSeasonActor()

  def receive = {
    case Papa.StartTheParty => startThePartyImpl()
    case Papa.PartyIsOver   => partyIsOverImpl()
    case _                  => log.info(s"unhandled msg for papa")
  }

  private def createSeasonActor(): ActorRef = system.actorOf(Props(new Season(config, self)))

  private def startThePartyImpl(): Unit = season ! StartSeason

  private def partyIsOverImpl(): Unit = {
    Thread.sleep(1000)
    log.info("papa ending actor system")
    context.system.terminate()
    System.exit(0)
  }
}

object Papa {
  case object StartTheParty
  case object PartyIsOver
}