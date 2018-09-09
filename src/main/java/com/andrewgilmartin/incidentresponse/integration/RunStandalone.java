package com.andrewgilmartin.incidentresponse.integration;

import com.andrewgilmartin.incidentresponse.IncidentResponseSlackApp;
import com.andrewgilmartin.slack.httpserver.HttpServerSlackServer;

public class RunStandalone {

    public static void main(String... args) throws Exception {
        String verificationToken = p("ir.token");
        int port = Integer.parseInt(p("ir.port"));
        String path = p("ir.path");
        HttpServerSlackServer server = new HttpServerSlackServer(
                port,
                path,
                new IncidentResponseSlackApp(verificationToken)
        );
        server.run();
    }

    private static String p(String name) {
        String value = System.getProperty(name, null);
        if (value == null) {
            value = System.getenv(name);
        }
        if (value == null) {
            throw new IllegalStateException(name + " is not defined");
        }
        return value;
    }
}
