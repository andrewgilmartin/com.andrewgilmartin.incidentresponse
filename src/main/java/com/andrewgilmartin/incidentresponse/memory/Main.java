package com.andrewgilmartin.incidentresponse.memory;

import com.andrewgilmartin.incidentresponse.IncidentResponseSlackApp;
import com.andrewgilmartin.slack.httpserver.HttpServerSlackServer;

public class Main {

    public static void main(String... args) throws Exception {
        String slackVerificationToken = null;
        int port = 5000;
        String path = "/ir";
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--port":
                    port = Integer.parseInt(args[i + 1]);
                    i += 1;
                    break;
                case "--path":
                    path = args[i + 1];
                    i += 1;
                    break;
                case "--token":
                    slackVerificationToken = args[i + 1];
                    i += 1;
                    break;
                default:
                    System.err.printf(
                            "usage: %s "
                            + "--port http-port-number "
                            + "--path url-path "
                            + "--token slack-verification-token",
                            Main.class.getName()
                    );
                    System.exit(1);
            }
        }
        HttpServerSlackServer server = new HttpServerSlackServer(
                port,
                path,
                new IncidentResponseSlackApp(new MemoryController(), slackVerificationToken)
        );
        server.run();
    }
}
