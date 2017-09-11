package hactors

import akka.actor.{Actor, ActorRef}
import akka.event.Logging
import scala.util.Random
import hactors.Deck.{Win, Loss}



class DeckBattle extends Actor {

  private val log = Logging(context.system, this)
  private val random = new Random(1)

  def receive = {
    case DeckBattle.Battle(first, second) => deckBattle(first, second)
    case _                                => log.info(s"unhandled msg for $self")
  }

  def deckBattle(first: (ActorRef, Int),
                 second: (ActorRef, Int)): Unit = {

    if (random.nextInt(first._2 + second._2) > first._2) {
      second._1 ! Win
      first._1 ! Loss
    }

    else {
      first._1 ! Win
      second._1 ! Loss
    }
  }
}

object DeckBattle {
  case class Battle(first: (ActorRef, Int), second: (ActorRef, Int))
}
