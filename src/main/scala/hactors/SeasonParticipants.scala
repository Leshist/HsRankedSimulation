package hactors

import Math.abs

import akka.actor.{Actor, ActorRef, Props}
import akka.event.Logging
import hactors.MatchMaking.RequestRound
import hactors.ResultCollector.AllParticipants
import hactors.SeasonParticipants.ReportBackParticipants

import scala.util.Random

class SeasonParticipants(playersAmount: Int,
                         matchMaking: ActorRef) extends Actor {
  private val log = Logging(context.system, this)
  private val system = context.system
  private val random = new Random(1)
  private lazy val participants = spawnDeckActors()

  def receive = {
    case SeasonParticipants.CreateParticipants =>
      createParticipants()
      turnOnMatchMaking()
    case ReportBackParticipants                =>
      val originalSender = sender()
      originalSender ! AllParticipants(participants)
    case _                                     => log.info(s"unhandled msg for $self")
  }

  private def turnOnMatchMaking(): Unit = matchMaking ! RequestRound

  private def createParticipants(): Unit = participants

  private def generateStrength(): Int = genStrHelper() + genStrHelper() + genStrHelper()

  private def genStrHelper(): Int = abs((random.nextGaussian() * 2).toInt) + 1

  private def spawnDeckActor(): ActorRef = system.actorOf(Props(new Deck(strength = generateStrength(), matchMaking)))

  private def spawnDeckActors(): List[ActorRef] = List.fill(playersAmount)(spawnDeckActor())
}

object SeasonParticipants {
  case object CreateParticipants
  case object ReportBackParticipants
}