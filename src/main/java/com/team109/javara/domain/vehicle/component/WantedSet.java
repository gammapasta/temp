package com.team109.javara.domain.vehicle.component;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WantedSet{
    private final Set<String> wantedSet = ConcurrentHashMap.newKeySet();//  동시성 제어위해 ConcurrentHashMap.newKeySet() 사용

    public void add(String wantedVehicleNumber) {
        wantedSet.add(wantedVehicleNumber);
    }
    public void remove(String wantedVehicleNumber) {
        wantedSet.remove(wantedVehicleNumber);
    }
    public int size(){
        return wantedSet.size();
    }

    public boolean contains(String wantedVehicleNumber) {
        return wantedSet.contains(wantedVehicleNumber);
    }

    public List<String> getWantedListFromSet(){
        return new ArrayList<>(wantedSet);
    }

}