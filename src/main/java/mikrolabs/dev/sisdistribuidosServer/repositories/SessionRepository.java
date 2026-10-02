package mikrolabs.dev.sisdistribuidosServer.repositories;

import mikrolabs.dev.sisdistribuidosServer.DTOs.Session;

import java.util.*;

public class SessionRepository {
    private final List<Session> sessions = Collections.synchronizedList(new ArrayList<>());

    public void registerSession(UUID token, String username){
        Session session = new Session(token, username);
        sessions.add(session);
    }

    public Optional<Session> getSessionByToken(UUID token){
        if (token == null) return Optional.empty();
        synchronized (sessions) {
            return sessions.stream()
                    .filter(s -> Objects.equals(s.token(), token))
                    .findFirst();
        }
    }

    public String getUsernameByToken(UUID token){
        return getSessionByToken(token)
                .map(Session::username)
                .orElse(null);

    }

    public boolean deleteSessionByToken(UUID token){
        if (token == null) return false;

        return sessions.removeIf(s -> Objects.equals(s.token(), token));
    }

    public List<Session> getAllSessions(){
        synchronized (sessions) {
            return List.copyOf(sessions);
        }
    }


}
