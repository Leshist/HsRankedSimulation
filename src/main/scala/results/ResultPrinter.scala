package results

import akka.event.LoggingAdapter
import hactors.ResultCollector.DeckResult

class ResultPrinter(results: List[DeckResult],
                   log: LoggingAdapter) {
  private val onlyLegendDecks = results filter(_.stars >= 95)
  private val decksStrength = results.map(_.strength).sorted
  private val groupedByStrDecks = decksStrength groupBy identity
  private val strengthToAmountOfDecks = groupedByStrDecks.transform((key, value) => value.length)
  private val decksAmountWithStrengthSorted = strengthToAmountOfDecks.toList.sortWith((l, r) => l._1 < r._1)

  def printResults(): Unit = {
    if (onlyLegendDecks.nonEmpty)
      logReachedLegendFeatures()

    else
      logNoLegendsFeatures()
  }

  private def logReachedLegendFeatures(): Unit = {
    val lowestGtl = _findSmallestGtl(onlyLegendDecks).get
    val topWr = (lowestGtl.gamesWon.toFloat/lowestGtl.gamesPlayed) * 100
    val topStrPercentile = percentRepresentation(lowestGtl.strength, decksStrength).get
    println("for this run, generation produced: ")
    decksAmountWithStrengthSorted.foreach(k => println(s"${k._2} decks with ${k._1} relative strength"))
    println(s"out of ${results.length} decks, ${onlyLegendDecks.length} have reached legend rank")
    println(s"deck with lowest amount of games to legend ${lowestGtl.gamesToLegend}, " +
      s"with winrate: ${topWr - topWr % 0.01} %")
    println(s"this deck strength - ${lowestGtl.strength}")
    println(s"this deck was in top ${topStrPercentile - topStrPercentile % 0.01} % decks rated by generated strength")
  }

  private def logNoLegendsFeatures(): Unit = {
    println("ops, 0 decks have reached legend rank, try increasing amount of decks or rounds in application.conf")
  }

  private def percentRepresentation(x: Int, xs: List[Int]): Option[Float] = xs match {
    case _ if xs.contains(x) => Some((1 - (xs.indexOf(x).toFloat / xs.length))*100)
    case _ => Some(0)
  }

  private def _findSmallestGtl(xs: List[DeckResult]): Option[DeckResult] = xs match {
    case x :: y :: rest => _findSmallestGtl( (if (x.gamesToLegend < y.gamesToLegend) x else y) :: rest )
    case List(DeckResult(gp, gw, gl, str, gtl, sta)) => Some(DeckResult(gp, gw, gl, str, gtl, sta))
    case _ => Some(DeckResult(0, 0, 0, 0, 0, 0))
  }

}
