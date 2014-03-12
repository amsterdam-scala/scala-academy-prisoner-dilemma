

SErver

clients = Client(state = [WAITING, PLAYING],
       actorRef


GameActor(p1, p2: ActorRef)
- schedule rounds
- client ! StartGame
- wait for rdy
- send client ! StartRound(nr)
- wait RoundAnswer
- send result
- repeat random # 
- FinishGame => kill himself, back to lobby

LobbyActor
- Register(name) => WAITING
- Unregister(name)
- start and stop game actors




client
state = [WAITING, PLAYING]

- StartGame(id, name) => PLAYING, ! PlayerReady
- RoundAnswer(roundNr, answer = [TALK, SILENT]]
- RoundResult(otherPlaxerAnswer, score)
- FinishGame(score) => state = waiting
- 
