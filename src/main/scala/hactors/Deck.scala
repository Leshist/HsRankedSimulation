package hactors

import java.util.UUID

import akka.actor.{Actor, ActorRef}
import akka.event.Logging
import hactors.DbWriter.StoreDeckState
import hactors.Deck.{ReminderToQueue, ReportBackResults}
import hactors.MatchMaking.QueueForAGame
import hactors.ResultCollector.DeckResult
import hearth.PlayerRecord

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class Deck(deckId: UUID,
           strength: Int,
           matchMaking: ActorRef,
           dbWriter: ActorRef) extends Actor {
  private val log = Logging(context.system, this)
  private val system = context.system
  private val playerRecord = new PlayerRecord
  private val scheduler = system.scheduler.scheduleOnce(1.seconds, self, ReminderToQueue)

  def receive = {
    case Deck.Win => handleWin()

    case Deck.Loss => handleLose()

    case ReminderToQueue => matchMaking ! formQueueRequest()

    case ReportBackResults =>
      val copiedSender = sender()
      copiedSender ! DeckResult(playerRecord.getGamesPlayed,
                          playerRecord.getGamesWon,
                          playerRecord.getGamesLost,
                          strength,
                          playerRecord.getGamesToLegendOrZero,
                          playerRecord.getStars)

      context.become(finishedState)

    case _       => log.info(s"unhandled msg for $self")
  }

  def finishedState: Receive = {
    case _ => ()
  }

  private def formQueueRequest(): QueueForAGame = QueueForAGame(self, playerRecord.getStars, strength)

  private def storeStateAndQueueForAGame(): Unit = {
    val myState = playerRecord.getState
    dbWriter ! StoreDeckState(myState, deckId)
    matchMaking ! formQueueRequest()
  }

  private def handleWin() = {
    playerRecord.win()
    storeStateAndQueueForAGame()
  }

  private def handleLose() = {
    playerRecord.lose()
    storeStateAndQueueForAGame()
  }

}

object Deck {
  case object Win
  case object Loss
  case object ReportBackScore
  case object ReminderToQueue
  case object ReportBackResults
}