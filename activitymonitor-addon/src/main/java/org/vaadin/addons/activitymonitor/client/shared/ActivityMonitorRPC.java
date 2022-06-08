package org.vaadin.addons.activitymonitor.client.shared;

import com.vaadin.shared.communication.ServerRpc;

public interface ActivityMonitorRPC extends ServerRpc {

    void clientStatusChanged(ClientStatus status);

    void customTimerTriggered(String timerName);

}
