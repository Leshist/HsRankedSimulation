package hactors

import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

import akka.actor.{Actor, ActorRef}
import akka.event.Logging
import config.SimulationConfig
import database.CassandraSeasonedWriter
import hactors.DbWriter._
import hearth.PlayerRecord.PlayerRecordState
import akka.actor.Status.Failure
import hactors.Papa.PartyIsOver

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._


class DbWriter(config: SimulationConfig,
               seasonId: UUID,
               papa: ActorRef) extends Actor {
  private val log = Logging(context.system, this)
  private val system = context.system
  private val db = new CassandraSeasonedWriter(self, seasonId, config)
  private val dbRequestsCounter = new AtomicInteger(0)
  private val errors = new ListBuffer[Throwable]

  def receive = {
    case StoreSimulation =>
      incrementDbRequestCounter()
      storeSimulation()

    case StoreDeckState(state, deckId) =>
      incrementDbRequestCounter()
      storeDeckState(state, deckId)

    case CloseDbRequest => system.scheduler.scheduleOnce(5.seconds, self, CloseDbInternalRequest)

    case CloseDbInternalRequest => handleCloseRequest()

    case FinishedDbOperation =>
      decrementDbRequestCounter()

    case Failure(e) =>
      decrementDbRequestCounter()
      errors += e
      log.info(s"DbWriter Errors: ${errors.length}, last one: ${errors.toList.lastOption}")

    case msg => log.info(s"unhandled msg for db writer $msg")
  }

  private def storeSimulation(): Unit = db.storeSimulation()
  private def storeDeckState(state: PlayerRecordState, deckId: UUID): Unit = db.storeDeck(state, deckId)
  private def incrementDbRequestCounter(): Unit = dbRequestsCounter.incrementAndGet()
  private def decrementDbRequestCounter(): Unit = dbRequestsCounter.decrementAndGet()
  private def getDbRequestCounter: Int = dbRequestsCounter.get()
  private def handleCloseRequest(): Unit =
    if (getDbRequestCounter == 0) {
      db.close()
      papa ! PartyIsOver
    }

    else system.scheduler.scheduleOnce(1.seconds, self, CloseDbRequest)

}

object DbWriter {
  case class StoreDeckState(state: PlayerRecordState, deckId: UUID)
  case object FinishedDbOperation
  case object StoreSimulation
  case object CloseDbRequest
  case object CloseDbInternalRequest
}