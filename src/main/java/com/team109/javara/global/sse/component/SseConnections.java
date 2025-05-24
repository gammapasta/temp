package com.team109.javara.global.sse.component;

import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


@Component
public class SseConnections {
    private final Set<Long> sseSet = ConcurrentHashMap.newKeySet();//동시성 제어위해 ConcurrentHashMap.newKeySet()

    public void add(Long memberId) {
        sseSet.add(memberId);
    }
    public void remove(Long memberId) {
        sseSet.remove(memberId);
    }
    public int size(){
        return sseSet.size();
    }

}