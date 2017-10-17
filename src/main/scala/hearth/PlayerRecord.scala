package hearth

import hearth.PlayerRecord.PlayerRecordState

class PlayerRecord {
  private var winStreak = 0
  private var _stars = 0
  private var gamesPlayed = 0
  private var gamesWon = 0
  private val checkPointStars = List(20,15,10,5,0).map(starsToReachRank)
  private lazy val gamesToReachLegend = gamesPlayed

  def getGamesPlayed: Int = gamesPlayed
  def getGamesWon: Int = gamesWon
  def getGamesLost: Int = gamesPlayed - gamesWon
  def getRank: Int = calculateRankFromStars(_stars)
  def getStars: Int = _stars
  def getGamesToLegendOrZero: Int = if (hasReachedLegend) gamesToReachLegend else 0
  def getState: PlayerRecordState = PlayerRecordState(getGamesPlayed,
                                                      getGamesWon,
                                                      getGamesLost,
                                                      winStreak,
                                                      getStars)

  def lose(): Unit = {
    if (canLoseStars) {
      _stars -= 1
    }

    winStreak = 0
    gamesPlayed += 1
  }

  def win(): Unit = {
    if (winStreak >= 2 && getRank >= 5){
      _stars += 2
    }

    else {
      _stars += 1
    }

    gamesPlayed += 1
    gamesWon += 1
    winStreak += 1

    if (hasReachedLegend) {
      gamesToReachLegend
    }
  }

  private def starsToReachRank(r: Int): Int = r.to(25).toList.tail.map(x => starsInRank(x)).sum

  private def canLoseStars: Boolean = getRank <= 20 && !checkPointStars.contains(_stars - 1)

  private def hasReachedLegend: Boolean = getRank == 0

  private def starsInRank(r: Int): Int = r.toDouble / 5 match {
    case x if x > 4.0 => 2
    case x if x > 3.0 => 3
    case x if x > 2.0 => 4
    case _ => 5
  }

  private def calculateRankFromStars(s: Int): Int = {
    val starsOnRanks = 0.to(25).toList.tail.map(x => starsInRank(x)).reverse

    def findRank(slice: List[Int], _stars: Int): Int = {
      if (_stars == 0) 25
      else {
        if (slice.sum <= _stars) 0.to(25).toList.reverse(slice.length)
        else findRank(slice.dropRight(1), _stars)
      }
    }
    findRank(starsOnRanks, s)
  }
}

object PlayerRecord {
  case class PlayerRecordState(gamesPlayed: Int,
                               gamesWon: Int,
                               gamesLost: Int,
                               winstreak: Int,
                               stars: Int)
}