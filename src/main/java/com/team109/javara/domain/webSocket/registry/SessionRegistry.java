package com.team109.javara.domain.webSocket.registry;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionRegistry {

    private final Map<String, String> sessionToDevice = new ConcurrentHashMap<>();
    private final Map<String, String> deviceToSession = new ConcurrentHashMap<>();

    public synchronized void add(String sessionId, String deviceId) {
        sessionToDevice.put(sessionId, deviceId);
        deviceToSession.put(deviceId, sessionId);
    }

    public synchronized void removeBySessionId(String sessionId) {
        String deviceId = sessionToDevice.remove(sessionId);
        if (deviceId != null) {
            deviceToSession.remove(deviceId);
        }
    }

    public synchronized void removeByDeviceId(String deviceId) {
        String sessionId = deviceToSession.remove(deviceId);
        if (sessionId != null) {
            sessionToDevice.remove(sessionId);
        }
    }

    public String getDeviceId(String sessionId) {
        return sessionToDevice.get(sessionId);
    }

    public String getSessionId(String deviceId) {
        return deviceToSession.get(deviceId);
    }

    public boolean hasSession(String sessionId) {
        return sessionToDevice.containsKey(sessionId);
    }

    public boolean hasDevice(String deviceId) {
        return deviceToSession.containsKey(deviceId);
    }
}