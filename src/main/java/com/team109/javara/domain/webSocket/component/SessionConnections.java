package com.team109.javara.domain.webSocket.component;

import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
@Component
public class SessionConnections {
    private final Set<String> sessionSet = ConcurrentHashMap.newKeySet();//  동시성 제어위해 ConcurrentHashMap.newKeySet() 사용

    public void add(String sessionId) {
        sessionSet.add(sessionId);
    }
    public void remove(String sessionId) {
        sessionSet.remove(sessionId);
    }
    public int size(){
        return sessionSet.size();
    }

}
