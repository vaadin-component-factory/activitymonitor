package org.vaadin.addons.activitymonitor.client.shared;

import java.util.HashMap;
import java.util.Map;

import com.vaadin.shared.communication.SharedState;

public class ActivityMonitorState extends SharedState {

    public int idleThreshold = 30000;
    public int inactiveTimeThreshold = 60000;
    public boolean timersEnabled = true;

    public Map<String, Integer> customTimers = new HashMap<>(1);

}
