package com.andrewgilmartin.incidentresponse.memory;

import com.andrewgilmartin.incidentresponse.IncidentResponseSlackApp;
import com.andrewgilmartin.slack.httpserver.HttpServerSlackServer;
import java.util.Map;

public class Main {

    public static void main(String... args) throws Exception {
        String verificationToken = p("ir_token");
        String port = p("ir_port");
        String path = p("ir_path");
        if (verificationToken == null || (port == null || !port.matches("^\\d+$")) || (path == null || !path.matches("^/"))) {
            dumpenv();
            throw new IllegalArgumentException("ir_token, ir_port, ir_path are not defined or invalid");
        }

        HttpServerSlackServer server = new HttpServerSlackServer(
                Integer.parseInt(port),
                path,
                new IncidentResponseSlackApp(new MemoryController(), verificationToken)
        );
        server.run();
    }

    private static String p(String name) {
        String value = System.getProperty(name, null);
        if (value == null) {
            value = System.getenv(name);
        }
        return value;
    }

    private static void dumpenv() {
        System.out.println("# BEGIN ENV");
        for (Map.Entry<String, String> e : System.getenv().entrySet()) {
            System.out.println(e.getKey() + "=" + e.getValue());
        }
        System.out.println("# END ENV");

        System.out.println("# BEGIN PROPERTIES");
        for (Map.Entry<Object, Object> p : System.getProperties().entrySet()) {
            System.out.println(p.getKey() + "=" + p.getValue());
        }
        System.out.println("# END PROPERTIES");
    }
}
