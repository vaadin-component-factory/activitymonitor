# ActivityMonitor Add-on for Vaadin 8

ActivityMonitor is a UI extension add-on for Vaadin 8 that tracks whether or not a user is active on the app or not. Provides two timers by default - "idle" and "inactive". When the user touches the keyboard, mouse or touchscreen, they are considered active. After they haven't interacted with the application for some time, the server is notified of this. By default, the server gets notified that the client is "idle" after 30 seconds. After 60 seconds, the client is considered "inactive". As soon as the user interacts with the application again, the server is notified that they are "active".
Additional timers with custom timeouts can be added.

This state information can be used to throttle or kick inactive user sessions in order to save on resources.

## Download release

Official releases of this add-on are available at Vaadin Directory. For Maven instructions, download and reviews, go to https://vaadin.com/addon/activitymonitor

## Building and running demo

git clone 
mvn clean install
cd demo
mvn jetty:run

To see the demo, navigate to http://localhost:8080/. Watch your console for the changes to the client's status.

## Release notes

### Version 1.0-SNAPSHOT
- Initial release

Supports the ACTIVE, IDLE and INACTIVE states as well as user-defined custom timers.

## Issue tracking

The issues for this add-on are tracked on its github.com page. All bug reports and feature requests are appreciated. 

## Contributions

Contributions are welcome, but there are no guarantees that they are accepted as such. Process for contributing is the following:
- Fork this project
- Create an issue to this project about the contribution (bug or feature) if there is no such issue about it already. Try to keep the scope minimal.
- Develop and test the fix or functionality carefully. Only include minimum amount of code needed to fix the issue.
- Refer to the fixed issue in commit
- Send a pull request for the original project
- Comment on the original issue that you have implemented a fix for it

## License & Author

Add-on is distributed under Apache License 2.0. For license terms, see LICENSE.txt.

ActivityMonitor is written by Patrik Lindström for Vaadin Ltd.

# Developer Guide

## Getting started

For a usage example, see src/test/java/org/vaadin/template/demo/DemoUI.java
