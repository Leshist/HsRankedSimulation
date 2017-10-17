package hactors

import akka.actor.{Actor, ActorRef, Props}
import akka.event.Logging
import hactors.DeckBattle.Battle
import hactors.MatchMaking.{FinishedRound, QueueForAGame}

class MatchMakingRound(matchmaking: ActorRef) extends Actor {
  val log = Logging(context.system, this)
  private val system = context.system
  private val deckBattleActor = system.actorOf(Props(new DeckBattle))

  def receive = {
    case MatchMakingRound.PlayRound(decks) => playRound(decks)
    case _                                 => log.info("huh???")
  }

  private def playRound(decks: List[(ActorRef, Int, Int)]) = {
    val sorted = decks sortWith(_._2 < _._2)
    val paired = sorted grouped 2
    paired foreach {
      case List(x)    => matchmaking ! QueueForAGame(x._1, x._2, x._3)
      case List(x, y) => deckBattleActor ! Battle((x._1, x._3), (y._1, y._3))
      case Nil        => log.info("wat")
    }
    matchmaking ! FinishedRound
  }
}

object MatchMakingRound {
  case class PlayRound(decks: List[(ActorRef, Int, Int)])
}
