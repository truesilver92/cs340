package ticket;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Holds the global state. Includes methods that provide interaction between individually
 * models and the entire model.
 */
public class Model {
    private static final Object globalState = C.atom.invoke(new Model());
    private final Object state;

    public interface Swapper {
        Model swap(Model oldState);
    }

    public Model() {
        this(C.readString.invoke("{\"users\" {}, \"games\" {}, \"sessions\" {}}"));
    }

    public Model(Object state) {
        this.state = state;
    }

    public Model commit(BaseModel model) {
        return new Model(C.assocIn.invoke(this.state, model.path, model.data));
    }

    public String toString() {
        return state.toString();
    }

    public void pprint() {
        C.pprint.invoke(state);
    }

    public static Model swap(Swapper swapper) {
        return (Model)C.swap.invoke(globalState, C.swapperToFn, swapper);
    }

    public static Model getState() {
        return (Model)C.deref.invoke(globalState);
    }

    private boolean exists(Object... path) {
        return C.getIn.invoke(state, path) != null;
    }

    private Model delete(BaseModel bm) {
        return new Model(C.dissocIn.invoke(this.state, bm.path));
    }

    // USER
    public Model createUser(String name, String password) {
        return commit(new User(name, password, userPath(name)));
    }

    public User getUser(String name) {
        Object[] path = userPath(name);
        return new User((Map)C.getIn.invoke(state, path), path);
    }

    public boolean userExists(String name) {
        return exists("users", name);
    }

    private Object[] userPath(String name) {
        return new Object[] {"users", name};
    }

    public User getUserBySessionId(String sessionId) {
        return getUser(getSession(sessionId).getUsername());
    }


    // SESSION
    public Model createSession(String username) {
        String id = UUID.randomUUID().toString();
        Object[] path = {"sessions", id};
        return commit(new Session(id, username, path))
              .commit(getUser(username).addSessionId(id));
    }

    public String getNewestSession(String username) {
        return (String)C.last.invoke(getUser(username).getSessionIds());
    }

    public Session getSession(String sessionId) {
        Object[] path = {"sessions", sessionId};
        return new Session((Map)C.getIn.invoke(state, path), path);
    }

    // GAME
    public Model createGame(String sessionId) {
        if (exists("sessions", sessionId, "gameId")) {
            throw new E.HasGameException();
        }
        String gameId = UUID.randomUUID().toString();
        Object[] path = {"games", gameId};
        return commit(new Game(gameId, sessionId, false, path))
              .commit(getSession(sessionId).setGameId(gameId));
    }

    public Model joinGame(String sessionId, String gameId) {
        if (exists("sessions", sessionId, "gameId")){
            throw new E.HasGameException();
        } else if (!exists("games", gameId)) {
            throw new E.GameUnavailableException();
        }
        Game game = getGame(gameId);
        if (!game.isAvailable(getUserBySessionId(sessionId))) {
            throw new E.GameUnavailableException();
        }
        return commit(game.addSessionId(sessionId))
              .commit(getSession(sessionId).setGameId(gameId));
    }

    public Model leaveGame(String sessionId) {
        Session session = getSession(sessionId);
        String gameId = session.getGameId();
        if (gameId == null) {
            throw new E.NoCurrentGameException();
        }
        Game game = getGame(gameId);
        if (game.started()) {
            throw new E.GameAlreadyStartedException();
        }
        Model m = commit(session.setGameId(null));
        if (game.getSessionIds().size() == 1) {
            return m.delete(game);
        } else {
            return m.commit(game.removeSessionId(sessionId));
        }
    }

    public Model startGame(String sessionId){
        if (!exists("sessions", sessionId, "gameId")) {
            throw new E.NoCurrentGameException();
        }
        Game game = getGameBySession(sessionId);
        if (game.getSessionIds().size() < 2) {
            throw new E.NotEnoughUsersException();
        }
        return commit(game.setStarted(true));
    }

    public Game getGameBySession(String sessionId) {
        return getGame(getSession(sessionId).getGameId());
    }

    public Game getGame(String gameId) {
        Object[] path = {"games", gameId};
        return new Game((Map)C.getIn.invoke(state, path), path);
    }

    public List<Game> getGames() {
        return ((Set<String>)((Map)C.get.invoke(state, "games")).keySet()).stream()
                .map((gameId) -> getGame(gameId)).collect(Collectors.toList());
    }

    public List<Map> getAvailableGames(String sessionId) {
        return getGames().stream()
            .filter((game) -> game.isAvailable(getUserBySessionId(sessionId)))
            .map((game) -> game.getAvailableModel(this))
            .collect(Collectors.toList());
    }

    // OTHER
    public Map getClientModel(String sessionId) {
        String gameId = getSession(sessionId).getGameId();
        List<Map> availableGames = null;
        Map currentGame = null;
        if (gameId == null) {
            availableGames = getAvailableGames(sessionId);
        } else {
            currentGame = getGame(gameId).getCurrentModel(sessionId, this);
        }

        return (Map)C.hashMap.invoke(
                "sessionId", sessionId,
                "availableGames", availableGames,
                "currentGame", currentGame);
    }

    public void authenticate(String username, String password) {
        if (!userExists(username) || !getUser(username).getPassword().equals(password)) {
            throw new E.LoginException();
        }
    }

    public void authenticate(String sessionId) {
        if (!exists("sessions", sessionId)) {
            throw new E.SessionException();
        }
    }
}
