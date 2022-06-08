package org.vaadin.addons.activitymonitor.client;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.vaadin.addons.activitymonitor.client.shared.ActivityMonitorRPC;
import org.vaadin.addons.activitymonitor.client.shared.ActivityMonitorState;
import org.vaadin.addons.activitymonitor.client.shared.ClientStatus;

import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.TouchEndEvent;
import com.google.gwt.event.dom.client.TouchMoveEvent;
import com.google.gwt.event.dom.client.TouchStartEvent;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;
import com.vaadin.client.ServerConnector;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.extensions.AbstractExtensionConnector;
import com.vaadin.shared.ui.Connect;

@Connect(org.vaadin.addons.activitymonitor.ActivityMonitor.class)
public class ActivityMonitorConnector extends AbstractExtensionConnector {

    private static final Logger logger = Logger
            .getLogger(ActivityMonitorConnector.class.getName());

    private Timer idleTimer;
    private Timer inactiveTimer;
    private Map<String, CustomTimer> customTimers;
    private ClientStatus status = ClientStatus.ACTIVE;

    private class CustomTimer extends Timer {
        public String name;
        public int timeout;

        @Override
        public void run() {
            rpc().customTimerTriggered(name);
        }
    }

    ActivityMonitorConnector() {
        idleTimer = new Timer() {
            @Override
            public void run() {
                if (status != ClientStatus.IDLE) {
                    status = ClientStatus.IDLE;
                    rpc().clientStatusChanged(ClientStatus.IDLE);
                }
            }
        };
        inactiveTimer = new Timer() {
            @Override
            public void run() {
                if (status != ClientStatus.INACTIVE) {
                    status = ClientStatus.INACTIVE;
                    rpc().clientStatusChanged(ClientStatus.INACTIVE);
                }
            }
        };
        customTimers = new HashMap<>();
    }

    private ActivityMonitorRPC rpc() {
        return getRpcProxy(ActivityMonitorRPC.class);
    }

    @Override
    public ActivityMonitorState getState() {
        return (ActivityMonitorState) super.getState();
    }

    private void stopTimers() {
        idleTimer.cancel();
        inactiveTimer.cancel();
        for (CustomTimer ct : customTimers.values()) {
            ct.cancel();
        }
    }

    private void resetTimers() {
        ActivityMonitorState state = getState();

        idleTimer.cancel();
        if (state.idleThreshold != 0) {
            idleTimer.schedule(state.idleThreshold);
        }

        inactiveTimer.cancel();
        if (state.inactiveTimeThreshold != 0) {
            inactiveTimer.schedule(state.inactiveTimeThreshold);
        }

        for (CustomTimer ct : customTimers.values()) {
            ct.cancel();
            ct.schedule(ct.timeout);
        }

        if (status != ClientStatus.ACTIVE) {
            status = ClientStatus.ACTIVE;
            rpc().clientStatusChanged(ClientStatus.ACTIVE);
        }
    }

    @Override
    protected void extend(ServerConnector target) {

        Window.addResizeHandler(e -> resetTimers());

        RootPanel rp = RootPanel.get();

        rp.addDomHandler(e -> resetTimers(), KeyDownEvent.getType());
        rp.addDomHandler(e -> resetTimers(), MouseMoveEvent.getType());
        rp.addDomHandler(e -> resetTimers(), MouseDownEvent.getType());
        rp.addDomHandler(e -> resetTimers(), TouchMoveEvent.getType());
        rp.addDomHandler(e -> resetTimers(), TouchStartEvent.getType());
        rp.addDomHandler(e -> resetTimers(), TouchEndEvent.getType());
        rp.addDomHandler(e -> resetTimers(), ScrollEvent.getType());
    }

    @Override
    public void onStateChanged(StateChangeEvent event) {
        super.onStateChanged(event);

        if (this.getConnection() == null) {
            logger.severe("No connection to server!");
            return;
        }

        ActivityMonitorState state = getState();

        boolean reset = true;

        if (state.timersEnabled == false) {
            stopTimers();
            reset = false;
        }

        if (event.hasPropertyChanged("customTimers")) {

            // Stop and destroy custom timers that have been removed
            for (String name : customTimers.keySet()) {
                if (!state.customTimers.containsKey(name)) {
                    customTimers.get(name).cancel();
                    customTimers.remove(name);
                }
            }

            // Recreate custom timers
            for (String name : state.customTimers.keySet()) {

                // Reset interval
                if (customTimers.containsKey(name)) {
                    customTimers.get(name).cancel();
                } else {
                    // Create new timer
                    CustomTimer ct = new CustomTimer();
                    ct.name = name;
                    ct.timeout = state.customTimers.get(name);
                    customTimers.put(name, ct);
                }

            }

        }

        if (reset) {
            resetTimers();
        }

    }

}
