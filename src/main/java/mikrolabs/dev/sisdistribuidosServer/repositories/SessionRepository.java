package mikrolabs.dev.sisdistribuidosServer.repositories;

import mikrolabs.dev.sisdistribuidosServer.DTOs.Session;
import mikrolabs.dev.sisdistribuidosServer.DTOs.User;

import java.util.*;

public class SessionRepository {
    private final List<Session> sessions = Collections.synchronizedList(new ArrayList<>());

    public void registerSession(UUID token, Optional<User> user){
        Session session = new Session(token, user);
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

    public boolean deleteSessionByToken(UUID token){
        if (token == null) return false;

        return sessions.removeIf(s -> Objects.equals(s.token(), token));
    }


    public List<Session> getAllSessions(){
        return sessions;
    }


}
