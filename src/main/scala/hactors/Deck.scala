package hactors

import akka.actor.{Actor, ActorRef}
import akka.event.Logging
import hactors.Deck.{ReminderToQueue, ReportBackResults}
import hactors.MatchMaking.QueueForAGame
import hactors.ResultCollector.DeckResult
import hearth.PlayerRecord
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class Deck(strength: Int,
           matchMaking: ActorRef) extends Actor {
  private val log = Logging(context.system, this)
  private val system = context.system
  private val playerRecord = new PlayerRecord
  private val scheduler = system.scheduler.schedule(1.milliseconds, 5.milliseconds, self, ReminderToQueue)
  def gamesPlayed: Int = playerRecord.games
  def stars: Int = playerRecord.stars

  def receive = {
    case Deck.Win => handleWin()
    case Deck.Loss => handleLose()
    case ReminderToQueue => queueForAGame()
    case ReportBackResults =>
      val copiedSender = sender()
      copiedSender ! DeckResult(playerRecord.games,
                          playerRecord.wins,
                          playerRecord.loses,
                          strength,
                          playerRecord.gamesToLegendOrZero,
                          playerRecord.stars)
      scheduler.cancel()
      context.become(finishedState)
    case _       => log.info(s"unhandled msg for $self")
  }

  def finishedState: Receive = {
    case _ => ()
  }

  private def formQueueRequest(): QueueForAGame = QueueForAGame(self, stars, strength)

  private def queueForAGame() = matchMaking ! formQueueRequest()

  private def handleWin() =
    playerRecord.win()
    queueForAGame()

  private def handleLose() =
    playerRecord.lose()
    queueForAGame()

}

object Deck {
  case object Win
  case object Loss
  case object ReportBackScore
  case object ReminderToQueue
  case object ReportBackResults
}