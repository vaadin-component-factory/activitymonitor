package org.vaadin.addons.activitymonitor;

import java.util.HashSet;
import java.util.Set;

import com.vaadin.server.AbstractExtension;
import com.vaadin.ui.UI;

import org.vaadin.addons.activitymonitor.client.shared.ActivityMonitorRPC;
import org.vaadin.addons.activitymonitor.client.shared.ActivityMonitorState;
import org.vaadin.addons.activitymonitor.client.shared.ClientStatus;

/**
 * A UI extension that monitors client activity state. When the client moves
 * their mouse, types anything on the keyboard or touches their touchscreen
 * (with the application focused), the client is considered "active". When the
 * client stops interacting with the UI, two timers check for activity
 * thresholds - "idle" and "inactive". The "idle" timer (default: 30 seconds)
 * indicates that the client has stopped interacting with the application. The
 * "inactive" timer ( default: 60 seconds) indicates that the client has not
 * interacted with the application for a long time and might be busy doing
 * something else.
 *
 * Custom timers can also be added in order to increase granularity. It is worth
 * noting that when a timer fires, a round-trip is initiated by the client. This
 * has the effect of extending the lifetime of the session.
 * 
 * This information can be used to provide present/away indication for real-time
 * chat functionality, or it can be used to throttle data feed speed to inactive
 * clients in order to save on resources, among other things.
 */
public class ActivityMonitor extends AbstractExtension {

    /**
     * Invoked whenever the client's status changes
     */
    public static interface ClientStatusChangeListener {
        void statusChanged(ClientStatus newStatus);
    }

    /**
     * Invoked whenever a custom timer fires
     */
    public static interface CustomTimerListener {
        void timerTriggered(String name);
    }

    private final Set<ClientStatusChangeListener> changeListeners = new HashSet<>(
            1);
    private final Set<CustomTimerListener> customTimerListeners = new HashSet<>(
            1);
    private ClientStatus status = ClientStatus.ACTIVE;

    private final ActivityMonitorRPC rpc = new ActivityMonitorRPC() {
        @Override
        public void clientStatusChanged(ClientStatus status) {
            ActivityMonitor.this.status = status;
            for (ClientStatusChangeListener l : changeListeners) {
                l.statusChanged(status);
            }
        }

        @Override
        public void customTimerTriggered(String timerName) {
            for (CustomTimerListener l : customTimerListeners) {
                l.timerTriggered(timerName);
            }
        }
    };

    @Override
    protected ActivityMonitorState getState() {
        return (ActivityMonitorState) super.getState();
    }

    @Override
    protected ActivityMonitorState getState(boolean markDirty) {
        return (ActivityMonitorState) super.getState(markDirty);
    }

    /**
     * Create an ActivityMonitor instance and attach it to the current UI
     */
    public ActivityMonitor() {
        this(UI.getCurrent());
    }

    /**
     * Create an ActivityMonitor instance and attach it to an arbitrary UI
     * 
     * @param ui
     *            a UI instance
     */
    public ActivityMonitor(UI ui) {
        extend(ui);
        registerRpc(rpc);
    }

    /**
     * Enable/start client activity status monitoring. (Enabled/started by
     * default when ActivityMonitor instance is created).
     */
    public void enable() {
        getState(true).timersEnabled = true;
    }

    /**
     * Disable/stop client activity status monitoring. (Enabled/started by
     * default when ActivityMonitor instance is created).
     */
    public void disable() {
        getState(true).timersEnabled = false;
    }

    /**
     * Return true if client activity is being monitored.
     * 
     * @return a boolean value.
     */
    public boolean isEnabled() {
        return getState().timersEnabled;
    }

    /**
     * Add a listener that gets triggered whenever the client's status changes.
     * 
     * @param listener
     *            a ClientStatusChangeListener instance (usually a lambda)
     */
    public void addClientStatusChangeListener(
            ClientStatusChangeListener listener) {
        changeListeners.add(listener);
    }

    /**
     * Remove a previously added client status change listener.
     * 
     * @param listener
     *            a previously added ClientStatusChangeListener instance
     */
    public void removeClientStatusChangeListener(
            ClientStatusChangeListener listener) {
        changeListeners.remove(listener);
    }

    /**
     * Remove all client status change listeners.
     */
    public void clearClientStatusChangeListeners() {
        changeListeners.clear();
    }

    /**
     * Add a named timer with a custom threshold. This function is provided to
     * allow for additional granularity in the activity levels. No custom timers
     * will run unless they're added.
     * 
     * @param name
     *            name of the timer. Used for identifying the timer in the
     *            listener.
     * @param threshold
     *            time of inactivity until timer fires, in milliseconds
     */
    public void addCustomTimer(String name, int threshold) {
        getState(true).customTimers.put(name, threshold);
    }

    /**
     * Update the threshold for a custom timer.
     * 
     * @param name
     *            name of timer. Used for identifying the timer in the listener.
     * @param threshold
     *            time of inactivity until timer fires, in milliseconds
     */
    public void setCustomTimerThreshold(String name, int threshold) {
        if (getState().customTimers.remove(name) != null) {
            getState(true).customTimers.put(name, threshold);
        }
    }

    /**
     * Remove a custom timer. This also removes and stops the associated
     * client-side timer object.
     * 
     * @param name
     *            name of a previously added custom timer.
     */
    public void removeCustomTimer(String name) {
        getState(true).customTimers.remove(name);
    }

    /**
     * Remove all custom timers.
     */
    public void clearCustomTimers() {
        getState(true).customTimers.clear();
    }

    /**
     * Add a custom timer listener. This function gets called whenever a custom
     * timer fires (i.e. when the user has been idle for longer than the
     * specified amount of time). The custom timer can be identified by the name
     * string provided as the listener function parameter.
     * 
     * Note, that adding a custom timer or modifying its interval will cause all
     * existing custom timers to be reset for that client.
     * 
     * @param listener
     *            a listener instance or lambda
     */
    public void addCustomTimerListener(CustomTimerListener listener) {
        customTimerListeners.add(listener);
    }

    /**
     * Remove a previously added custom timer listener.
     * 
     * @param listener
     *            reference to the listener function object.
     */
    public void removeCustomTimerListener(CustomTimerListener listener) {
        customTimerListeners.remove(listener);
    }

    /**
     * Remove all previously added custom timer listeners.
     */
    public void clearCustomTimerListeners() {
        customTimerListeners.clear();
    }

    /**
     * Set the idle time threshold - the client is considered to be 'idle' after
     * this many milliseconds. Set this to 0 to disable the idle threshold
     * timer.
     * 
     * Default: 30 seconds (30000 msec).
     * 
     * @param msec
     *            time in milliseconds
     */
    public void setIdleTimeThreshold(int msec) {
        getState(true).idleThreshold = Math.max(msec, 0);
    }

    /**
     * Get the current value for the idle time threshold. A client is considered
     * adle after this many milliseconds.
     * 
     * @return time in milliseconds (default: 30000).
     */
    public int getIdleTimeThreshold() {
        return getState().idleThreshold;
    }

    /**
     * Set the inactivity time threshold - the client is considered to be
     * inactive (i.e. left the desk) after this many milliseconds.
     * 
     * Set this to 0 to disable the inactive threshold timer.
     * 
     * Default: 60 seconds (60000 msec).
     * 
     * @param msec
     *            time in milliseconds
     */
    public void setInactiveTimeThreshold(int msec) {
        getState(true).inactiveTimeThreshold = Math.max(msec, 0);
    }

    /**
     * Get the current value for the inactivity time threshold. The client is
     * considered inactive after this many milliseconds.
     * 
     * @return time in milliseconds (default: 60000).
     */
    public int getInactiveTimeThreshold() {
        return getState().inactiveTimeThreshold;
    }

    /**
     * Check if client is currently considered "active", i.e. has touched an
     * input device around the application before the idle (and inactivity)
     * timers have triggered.
     * 
     * @return true neither 'idle' nor 'inactive' timers have been exceeded.
     */
    public boolean isActive() {
        return status == ClientStatus.ACTIVE;
    }

    /**
     * Get the current status value.
     * 
     * @return a {@link ClientStatus} value
     */
    public ClientStatus getCurrentStatus() {
        return status;
    }

}
