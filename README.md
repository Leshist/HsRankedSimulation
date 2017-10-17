# HsRankedSimulation
This project has no significant value, it's a Scala training playground 
It does simulate Hearthstone ranked system, but it does not simulate real game, 
Instead, every deck is assigned relevant strength as non-zero Int. 
Game between 2 decks(with strength k and n) outcome is calculated by rolling n + k sided dice
Result of the roll is m if (m <= n) n-strength deck wins, else - second one wins

# how to configure
you need http://cassandra.apache.org/
### configuration file lies here:
src/main/resources/application.conf

## cassandra cqlsh prerequisites
creates keyspace and names it HS2, or whatever, just make sure it matches keyspace name in application.conf
- CREATE KEYSPACE “HS2”
WITH replication = {'class': ‘SimpleStrategy’, 'replication_factor' : 2};

creates table for simulations in HS2 keyspace
- CREATE TABLE Simulations(
   id UUID PRIMARY KEY,
   date timestamp,
   simdecks int,
   simrounds int);

creates table for Deck's state
- CREATE TABLE DeckState(
 id UUID PRIMARY KEY,
 seasonId UUID, 
 deckid UUID,
 gamesplayed int,
 gameswon int,
 gameslost int, 
 stars int);

# how to run:
sbt run

# todo:
- add Play Web + Some UI with ability to start app 
- add Spark analytics over results Cassandra