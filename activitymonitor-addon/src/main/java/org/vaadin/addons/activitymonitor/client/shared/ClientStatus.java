package org.vaadin.addons.activitymonitor.client.shared;

public enum ClientStatus {

    /**
     * Status indicating that the user is interacting with
     * or has very recently been interacting with the application.
     * 
     * This status is set for as long as neither the IDLE nor the
     * INACTIVE state has been reached.
     */
    ACTIVE,

    /**
     * Status indicating that the user has recently
     * stopped interacting with the application.
     * 
     * By default, this status gets set when the user
     * hasn't shown any interaction events for 30 seconds.
     */
    IDLE,

    /**
     * Status indicating that the client has not been
     * interacting with the application for an extended
     * amount of time.
     * 
     * By default, this status gets set when the user
     * hasn't shown any interaction events for 60 seconds.
     */
    INACTIVE
}
