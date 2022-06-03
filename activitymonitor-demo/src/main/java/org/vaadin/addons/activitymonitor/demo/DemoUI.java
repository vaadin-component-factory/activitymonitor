package org.vaadin.addons.activitymonitor.demo;

import org.vaadin.addons.activitymonitor.ActivityMonitor;

import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.annotations.Widgetset;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@Theme("demo")
@Title("ActtivityMonitor Add-on Demo")
@Widgetset("org.vaadin.addons.activitymonitor.WidgetSet")
public class DemoUI extends UI
{

    @WebServlet(value = "/*", asyncSupported = true)
    @VaadinServletConfiguration(productionMode = false, ui = DemoUI.class)
    public static class Servlet extends VaadinServlet {
    }

    @Override
    protected void init(VaadinRequest request) {

        ////////////////////////////////
        // Demo UI boilerplate
        ////////////////////////////////

        // Show it in the middle of the screen
        final VerticalLayout layout = new VerticalLayout();
        layout.setStyleName("demoContentLayout");
        
        final TextField name = new TextField();
        name.setCaption("Type your name here:");

        Button button = new Button("Click Me");
        button.addClickListener(e -> {
            layout.addComponent(new Label("Thanks " + name.getValue() 
                    + ", it works!"));
        });
        
        layout.addComponents(name, button);
        setContent(layout);


        ////////////////////////////////
        // ActivityMonitor setup
        ////////////////////////////////

        ActivityMonitor monitor = new ActivityMonitor();
        monitor.setIdleTimeThreshold(5000);
        monitor.setInactiveTimeThreshold(10000);
        monitor.addClientStatusChangeListener(status -> {
            System.out.println("Client status is now " + status.toString());
        });

        monitor.addCustomTimer("A", 15000);
        monitor.addCustomTimer("B", 20000);
        monitor.addCustomTimerListener(customTimer -> {
            System.out.println("Custom timer " + customTimer + " fired");
        });

        ////////////////////////////////////////////////////////////////////////////

        layout.addComponents(
            new Label("Idle time threshold is " + (monitor.getIdleTimeThreshold() / 1000) + " seconds"),
            new Label("Inactive time threshold is " + (monitor.getInactiveTimeThreshold() / 1000) + " seconds"),
            new Label("Custom timer \"A\" set to a threshold of 15 seconds"),
            new Label("Custom timer \"B\" set to a threshold of 20 seconds")
        );
    }
}
