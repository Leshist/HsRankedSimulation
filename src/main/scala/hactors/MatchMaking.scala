package hactors

import akka.actor.{Actor, ActorRef, Props}
import akka.event.Logging
import hactors.MatchMaking.{FinishedRound, RequestRound}
import hactors.MatchMakingRound.PlayRound
import hactors.Season.SeasonEnded

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class MatchMaking(roundsToPlay: Int,
                  season: ActorRef,
                  decksAmount: Int) extends Actor {
  private val name = "MM"
  private val log = Logging(context.system, this)
  private val system = context.system
  private var roundsPlayed = 0
  private val queuedForRound = new ListBuffer[(ActorRef, Int, Int)]
  private val roundPlayer = makeRoundMaker()
  private val queueFulnessCoef = 0.90
  private val logProgressFrequency = if (roundsToPlay > 20) roundsToPlay / 20 else 1

  def receive = {
    case MatchMaking.QueueForAGame(ref, score, strength) => addToQueue(ref, score, strength)
    case MatchMaking.RequestRound                        => requestRound()
    case FinishedRound                                   => handleRoundsEnd()
    case _                                               => log.info(s"unhandled msg for $name")
  }

  def addToQueue(deck: ActorRef, score: Int, strength: Int): Unit = queuedForRound += Tuple3(deck, score, strength)

  def requestRound(): Unit = {
    val queued: ListBuffer[(ActorRef, Int, Int)] = queuedForRound.clone()
    val percentageOfAllQueued = queued.length.toFloat / decksAmount

    if (percentageOfAllQueued >= queueFulnessCoef) {
      roundPlayer ! PlayRound(queued.toList.distinct)
      val queuedAfterRoundPhase = queuedForRound -- queued
      queuedForRound.clear()
      queuedForRound ++ queuedAfterRoundPhase
    }

    else {
      context.system.scheduler.scheduleOnce(500.milliseconds, self, RequestRound)
    }

  }
  def makeRoundMaker(): ActorRef = system.actorOf(Props(new MatchMakingRound(self)))

  def handleRoundsEnd(): Unit = {
    roundsPlayed += 1

    if (roundsPlayed == roundsToPlay) {
      handleSeasonEnd()
    }
    else {
      requestRound()
    }

    if (roundsPlayed % logProgressFrequency == 0)
      logProgress()

  }

  def handleSeasonEnd(): Unit = {
    system.scheduler.scheduleOnce(5.seconds, season, SeasonEnded)
  }

  def logProgress(): Unit = log.info(s"playing matchmaking... ${((roundsPlayed.toFloat/roundsToPlay)*100).toInt}% done")
}

object MatchMaking {
  case object RequestRound
  case object FinishedRound
  case class QueueForAGame(me: ActorRef, score: Int, strength: Int)
}
