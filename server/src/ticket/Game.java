package ticket;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class Game extends BaseModel {
            
    public Game(Map data, Object[] path) {
        super(data, path);
    }

    public Game(String gameId, String sessionId, boolean started, Object[] path) {
        super(new Object[] {"gameId", gameId, 
                            "sessionIds", C.vector.invoke(sessionId),
                            "started", started,
                            "trainDeck", C.shuffle.invoke(TrainType.DECK),
                            "faceUpDeck", C.vector.invoke(),
                            "trainDiscard", C.vector.invoke(),
                            "destDeck", C.shuffle.invoke(DestinationCard.DECK),
                            "destDiscard", C.vector.invoke(),
                            "openRoutes", Route.ROUTES,
                            "messages", C.vector.invoke(),
                            "gameHistory", C.vector.invoke(),
                            "longestRouteHolder", null},
                path);
    }

    public Map getCurrentModel(String curSession, State state) {
        List<Map> players = getSessionIds().stream()
            .map((sessionId) -> state.getSession(sessionId)
                        .getClientModel(sessionId.equals(curSession)))
            .collect(Collectors.toList());

        String holder = getLongestRouteHolder();
        String username = (holder == null) ? null
                    : state.getSession(holder).getUsername();

        Map ret = (Map)C.selectKeys.invoke(data, new String[] {
                    "faceUpDeck", "openRoutes", "messages", "gameHistory"});
        return (Map)C.assoc.invoke(ret,
                "players", players,
                "trainDeck", getTrainDeck().size(),
                "destDeck", getDestDeck().size(),
                "longestRouteHolder", username);
    }

    public Map getAvailableModel(State state) {
        List<String> players = getSessionIds().stream()
            .map((sessionId) -> state.getSession(sessionId).getUsername())
            .collect(Collectors.toList());
        return (Map)C.hashMap.invoke(
            "gameId", getGameId(),
            "players", players);
    }

    public String getLongestRouteHolder() {
        return (String)data.get("longestRouteHolder");
    }

    public List<TrainType> getTrainDeck() {
        return (List)data.get("trainDeck");
    }

    public List<DestinationCard> getDestDeck() {
        return (List)data.get("destDeck");
    }

    public String getGameId() {
        return (String)data.get("gameId");
    }

    public boolean started() {
        return (boolean)data.get("started");
    }

    public State start(State state) {
        Game game = new Game(set("started", true), path);
        for (Session ses : getSessions(state)) {
            for (int i = 0; i < 4; i++) {
                ses = ses.giveTrain(game.topTrain());
                game = game.drawCard("trainDeck");
            }
            for (int i = 0; i < 3; i++) {
                ses = ses.giveDest(game.topDest());
                game = game.drawCard("destDeck");
            }
            state = state.commit(ses);
        }
        return state.commit(game);
    }

    private List<Session> getSessions(State state) {
        return getSessionIds().stream()
            .map((sessionId) -> state.getSession(sessionId))
            .collect(Collectors.toList());
    }

    private TrainType topTrain() {
        return getTrainDeck().get(0);
    }

    private DestinationCard topDest() {
        return getDestDeck().get(0);
    }

    private Game drawCard(String deck) {
        return new Game(update(deck, C.subvec, 1), path);
    }

    public List<String> getSessionIds() {
        return (List<String>)data.get("sessionIds");
    }

    public Game addSessionId(String sessionId) {
        return new Game(update("sessionIds", C.conj, sessionId), path);
    }

    public Game removeSessionId(String sessionId) {
        return new Game(remove("sessionIds", sessionId), path);
    }

    public boolean isAvailable(User u) {
        List<String> ids = getSessionIds();
        return (!started() && ids.size() < 5 &&
                ids.stream().filter(u.getSessionIds()::contains)
                .collect(toList()).size() == 0);
    }
}
