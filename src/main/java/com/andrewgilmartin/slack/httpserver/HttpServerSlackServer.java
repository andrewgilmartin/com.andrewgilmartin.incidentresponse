package com.andrewgilmartin.slack.httpserver;

import com.andrewgilmartin.slack.SlackRequestBase;
import com.andrewgilmartin.slack.SlackChannelBase;
import com.andrewgilmartin.slack.SlackUserBase;
import com.andrewgilmartin.util.IO;
import com.andrewgilmartin.util.Logger;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import com.andrewgilmartin.slack.SlackApp;
import com.andrewgilmartin.slack.SlackResponseBase;
import java.util.Objects;

/**
 *
 * {@code
 *
 * # tunnel connections to REMOTE-HOST:9090 to localhost:9080
 * ssh -NR 9090:localhost:9080 REMOTE-USER@REMOTE-HOST &
 *
 * # show incoming and outgoing data between localhost:9080 and localhost:9090
 * java \
 *     -classpath $HOME/.m2/repository/soap/soap/2.3/soap-2.3.jar
 *     org.apache.soap.util.net.TcpTunnelGui
 *     9080 localhost 9090 &
 *
 * # run the Slack app as a standalone server listening on port 9090
 * java \
 *     -Dcom.andrewgilmartin.incidentresponse.IncidentResponseSlackApp.verificationToken=XXX \
 *     -jar target/incidentresponse1-1.0-SNAPSHOT.jar \
 *     9090
 *
 * }
 */
public class HttpServerSlackServer implements Runnable, HttpHandler, AutoCloseable {

    private static final Logger logger = Logger.getLogger(HttpServerSlackServer.class);

    private static final int HTTP_OK = 200;
    private static final int HTTP_BAD_REQUEST = 400;
    private static final int HTTP_UNAUTHORIZED = 401;
    private static final int HTTP_NOT_FOUND = 404;
    private static final int HTTP_INTERNAL_SERVER_ERROR = 500;

    private final SlackApp slackApp;
    private final HttpServer httpServer;

    public HttpServerSlackServer(int port, String path, SlackApp slackApp) throws IOException {
        this.slackApp = slackApp;
        this.httpServer = HttpServer.create(new InetSocketAddress(port), 10);
        this.httpServer.setExecutor(Executors.newFixedThreadPool(10));
        this.httpServer.createContext(path, this);
    }

    @Override
    public void close() {
        httpServer.stop(0);
    }

    @Override
    public void run() {
        httpServer.start();
    }

    @Override
    public void handle(HttpExchange he) throws IOException {
        Map<String, List<String>> parameters = parseParameters(IO.slurp(he.getRequestBody()));
        String verificationToken = findFirst(parameters, "token");
        String sslCheck = findFirst(parameters, "ssl_check");
        if (sslCheck != null && "1".equals(sslCheck)) {
            he.sendResponseHeaders(HTTP_OK, 0);
            he.getResponseBody().close();
        } else if (verificationToken == null || !verificationToken.equals(slackApp.getVerificationToken())) {
            he.sendResponseHeaders(HTTP_UNAUTHORIZED, 0);
            he.getResponseBody().close();
        } else {
            SlackRequestBase slackRequest = new SlackRequestBase(
                    new SlackChannelBase(findFirst(parameters, "channel_id"), findFirst(parameters, "channel_name")),
                    new SlackUserBase(findFirst(parameters, "user_id"), findFirst(parameters, "user_name")),
                    findFirst(parameters, "command"),
                    findFirst(parameters, "text")
            );
            SlackResponseBase slackResponse = new SlackResponseBase();
            slackApp.request(slackRequest, slackResponse);
            he.getResponseHeaders().add("content-type", "application/json; charset=utf-8");
            he.sendResponseHeaders(HTTP_OK, 0);
            slackResponse.render(he.getResponseBody());
        }
    }

    private Map<String, List<String>> parseParameters(String query) throws UnsupportedEncodingException {
        Map<String, List<String>> q = new HashMap<>();
        if (query != null) {
            String[] pp = query.split("&");
            for (String p : pp) {
                String[] nv = p.split("=", 2);
                String name = URLDecoder.decode(nv[0], StandardCharsets.UTF_8.name());
                String value = nv.length == 2 ? URLDecoder.decode(nv[1], StandardCharsets.UTF_8.name()) : "";
                if (!q.containsKey(name)) {
                    q.put(name, new LinkedList<>());
                }
                q.get(name).add(value);
            }
        }
        return q;
    }

    private <T> T findFirst(Map<String, List<T>> m, String k) {
        return m == null ? null : findFirst(m.get(k));
    }

    private <T> T findFirst(List<T> l) {
        return l == null || l.isEmpty() ? null : l.get(0);
    }

    private <T> T findFirst(Iterator<T> i) {
        return i == null || !i.hasNext() ? null : i.next();
    }

    private <T> T findFirst(Iterable<T> i) {
        return i == null ? null : findFirst(i.iterator());
    }

}
