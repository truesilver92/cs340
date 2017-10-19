package ticket;

import java.util.HashMap;
import java.util.Map;

public class Facade {
    private static Map run(State.Swapper swapper, String key, boolean getByUsername) {
        State m;
        try {
            m = State.swap(swapper);
        } catch (E.BaseException e) {
            return error(e);
        }
        String sessionId;
        if (getByUsername) {
            sessionId = m.getNewestSession(key);
        } else {
            sessionId = key;
        }
        return success(m, sessionId);
    }

    private static Map run(State.Swapper swapper, String sessionId) {
        return run(swapper, sessionId, false);
    }

    private static Map success(State m, String sessionId) {
        return m.getClientModel(sessionId);
    }

    private static Map error(E.BaseException e) {
        return Server.error(e.getCode(), e.getMessage());
    }

    public static Map register(String username, String password) {
        return run((state) -> {
            if (state.userExists(username)) {
                throw new E.UserExistsException();
            }
            return state.createUser(username, password)
                .createSession(username);
        }, username, true);
    }

    public static Map login(String username, String password) {
        return run((state) -> {
                state.authenticate(username, password);
                return state.createSession(username);
        }, username, true);
    }

    public static Map create(String sessionId) {
        return run((state) -> {
            state.authenticate(sessionId);
            return state.createGame(sessionId);
        }, sessionId);
    }

    public static Map join(String sessionId, String gameId) {
        return run((state) -> {
            state.authenticate(sessionId);
            return state.joinGame(sessionId, gameId);
        }, sessionId);
    }

    public static Map leave(String sessionId){
        return run((state) -> {
                state.authenticate(sessionId);
                return state.leaveGame(sessionId);
            }, sessionId);
    }

    public static Map start(String sessionId){
        return run((state) -> {
            state.authenticate(sessionId);
            return state.startGame(sessionId);
        }, sessionId);
    }

    public static Map state(String sessionId) {
        return success(State.getState(), sessionId);
    }

    public static Map clear() {
        // For testing purposes. Hopefully none of our users find out
        // about this endpoint.
        State.swap((state) -> new State());
        return new HashMap();
    }
}
