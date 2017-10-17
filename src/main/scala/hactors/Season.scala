package hactors

import java.util.UUID

import akka.actor.{Actor, ActorRef, Props}
import akka.event.Logging
import hactors.Season.SeasonEnded
import hactors.SeasonParticipants.CreateParticipants
import config.SimulationConfig
import hactors.DbWriter.{CloseDbRequest, StoreSimulation}

class Season(config: SimulationConfig, papa: ActorRef) extends Actor {
  private val log = Logging(context.system, this)
  private val system = context.system
  private val matchMaking = createMatchMaking()
  private val dbWriter = createDbWriter()
  // needs dbWriter and matchMaking to be created first
  private val seasonParticipants = createSeasonParticipants()
  private val seasonId = UUID.randomUUID()

  def receive = {
    case Season.StartSeason              => startSeasonImpl()
    case SeasonEnded                     => seasonEndImpl()
    case _                               => log.info(s"unhandled msg for $self")
  }

  private def startSeasonImpl(): Unit =
    dbWriter ! StoreSimulation
    seasonParticipants ! CreateParticipants

  private def createResultCollector(): ActorRef =
    system.actorOf(Props(new ResultCollector(seasonParticipants, config, papa)))

  private def createMatchMaking(): ActorRef =
    system.actorOf(Props(new MatchMaking(config.matchMakingRoundsAmount, self, config.decksAmount)))

  private def createSeasonParticipants(): ActorRef =
    system.actorOf(Props(new SeasonParticipants(
      playersAmount = config.decksAmount,
      matchMaking = matchMaking,
      dbWriter = dbWriter)))

  private def createDbWriter(): ActorRef = system.actorOf(Props(new DbWriter(config, seasonId, papa)))

  private def seasonEndImpl(): Unit = dbWriter ! CloseDbRequest

}

object Season {
  case object StartSeason
  case object SeasonEnded
}