package com.team109.javara.domain.event.eventListener;


import com.team109.javara.domain.event.event.EdgeDeviceEvent;
import com.team109.javara.domain.event.event.ServerInitiatedEvent;
import com.team109.javara.domain.event.event.TaskEvent;
import com.team109.javara.domain.event.service.AsyncDecisionService;
import com.team109.javara.domain.event.service.AsyncFirstTaskDecision;
import com.team109.javara.domain.event.service.AsyncServerInitiatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class EventListener {
    private final AsyncDecisionService asyncDecisionService;
    private final AsyncFirstTaskDecision asyncFirstTaskDecision;
    private final AsyncServerInitiatedEvent asyncServerInitiatedEvent;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskEvent(TaskEvent event) {
        asyncDecisionService.findAnotherPolice(event.getTaskId(), event.getWantedVehicleId()); // 여기서 비동기 처리
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEdgeDeviceEvent(EdgeDeviceEvent event) {
        asyncFirstTaskDecision.initiateFirstTaskDecision(event.getEdgeDeviceId(), event.getWantedVehicleNumber()); // 여기서 비동기 처리
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleServerInitiatedEvent(ServerInitiatedEvent event) {
        asyncServerInitiatedEvent.serverInitiatedTaskAssignment(event.getMemberId(), event.getWantedVehicleNumber());
    }
}
