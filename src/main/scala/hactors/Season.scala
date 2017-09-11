package hactors

import akka.actor.{Actor, ActorRef, Props}
import akka.event.Logging
import hactors.ResultCollector.ReportResults
import hactors.Season.SeasonEnded
import hactors.SeasonParticipants.CreateParticipants
import config.SimulationConfig

class Season(config: SimulationConfig, papa: ActorRef) extends Actor {
  private val log = Logging(context.system, this)
  private val system = context.system
  private lazy val resultsCollector = createResultCollector()
  private lazy val matchMaking = createMatchMaking()
  private lazy val seasonParticipants = createSeasonParticipants()

  def receive = {
    case Season.StartSeason              => startMatchMaking()
    case SeasonEnded                     => seasonEnd()
    case _                               => log.info(s"unhandled msg for $self")
  }

  private def startMatchMaking(): Unit = seasonParticipants ! CreateParticipants

  private def createResultCollector(): ActorRef = system.actorOf(Props(new ResultCollector(seasonParticipants, config, papa)))

  private def createMatchMaking(): ActorRef = system.actorOf(Props(new MatchMaking(config.matchMakingRoundsAmount, self, config.decksAmount)))

  private def createSeasonParticipants(): ActorRef =
    system.actorOf(Props(new SeasonParticipants(playersAmount = config.decksAmount, matchMaking = matchMaking)))

  private def seasonEnd(): Unit = resultsCollector ! ReportResults
}

object Season {
  case object StartSeason
  case object SeasonEnded
}