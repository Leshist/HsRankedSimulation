package database

import java.util.UUID
import java.util.Date

import akka.actor.ActorRef
import akka.pattern.pipe
import config.SimulationConfig
import hactors.DbWriter.FinishedDbOperation
import Function.const

import hearth.PlayerRecord.PlayerRecordState
import io.getquill.{CassandraAsyncContext, LowerCase}

import scala.concurrent.ExecutionContext.Implicits.global

class CassandraSeasonedWriter(tracker: ActorRef,
                              seasonId: UUID,
                              config: SimulationConfig) {
  private val db = new CassandraAsyncContext[LowerCase]("db")
  import db._

  private def genUUID(): UUID = UUID.randomUUID()
  private def getDate: Date = new Date()

  case class Simulations(id: UUID,
                         simdecks: Int,
                         simrounds: Int,
                         time: Date)

  case class Deckstate(id: UUID,
                       seasonId: UUID,
                       deckId: UUID,
                       gamesPlayed: Int,
                       gamesWon: Int,
                       gamesLost: Int,
                       stars: Int)

  def storeSimulation(): Unit = {
    val simulationRecord = Simulations(seasonId,
                                       config.decksAmount,
                                       config.matchMakingRoundsAmount,
                                       getDate)
    val result = db.run(query[Simulations].insert(lift(simulationRecord)))
    result.map(const(FinishedDbOperation)).pipeTo(tracker)
  }

  def storeDeck(state: PlayerRecordState, deckId: UUID): Unit = {
    val deckStateRecord = Deckstate(genUUID(),
                                    seasonId,
                                    deckId,
                                    state.gamesPlayed,
                                    state.gamesWon,
                                    state.gamesLost,
                                    state.stars)
    val result =  db.run(query[Deckstate].insert(lift(deckStateRecord)))
    result.map(const(FinishedDbOperation)).pipeTo(tracker)
  }

  def close(): Unit = db.close()
}
