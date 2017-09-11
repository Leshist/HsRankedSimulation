package hactors

import akka.actor.{Actor, ActorRef}
import akka.event.Logging
import hactors.Deck.ReportBackResults
import hactors.Papa.PartyIsOver
import hactors.ResultCollector.{AllParticipants, DeckResult, ReportResults}
import hactors.SeasonParticipants.ReportBackParticipants
import config.SimulationConfig
import results.ResultPrinter
import scala.collection.mutable

class ResultCollector(seasonParticipants: ActorRef,
                      config: SimulationConfig,
                      papa: ActorRef) extends Actor {
  private val log = Logging(context.system, this)
  private val results = mutable.Set[(ActorRef, DeckResult)]()
  private val shouldHaveReportsOf = mutable.Set[ActorRef]()
  private val haveReportsOf = mutable.Set[ActorRef]()


  def receive = {
    case ReportResults => askForAllParticipants()
    case AllParticipants(ptcp) =>
      ptcp foreach (p => shouldHaveReportsOf += p)
      askForResults(ptcp)
    case result @ DeckResult(gp, gw, gl, str, gtl, sta) =>
      val originalSender = sender()
      recordResultAndCheckProgress(originalSender, result)
    case _ => log.info("huh?")
  }

  private def askForAllParticipants(): Unit = seasonParticipants ! ReportBackParticipants

  private def askForResults(participants: Traversable[ActorRef]): Unit = participants foreach(p => p ! ReportBackResults)

  private def recordResultAndCheckProgress(deck: ActorRef, result: DeckResult): Unit = {
    results += Tuple2(deck, result)
    haveReportsOf += deck
    if (results.size == config.decksAmount)
      logResultsAndEndSimulation()
    else
      askForResults(shouldHaveReportsOf -- haveReportsOf)
  }

  private def logResultsAndEndSimulation(): Unit = {
    val deckResults = results.toList map (x => x._2)
    val resultsPrinter = new ResultPrinter(deckResults, log)
    resultsPrinter.printResults()
    papa ! PartyIsOver
  }
}

object ResultCollector {
  case object ReportResults
  case class AllParticipants(participants: List[ActorRef])
  case class DeckResult(gamesPlayed: Int,
                        gamesWon: Int,
                        gamesLost: Int,
                        strength: Int,
                        gamesToLegend: Int,
                        stars: Int)
}