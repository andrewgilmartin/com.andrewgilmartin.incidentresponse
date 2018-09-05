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
import java.util.ServiceLoader;
import java.util.concurrent.Executors;
import com.andrewgilmartin.slack.SlackApp;
import com.andrewgilmartin.slack.SlackResponseBase;

/**
 *
 * {@code
 *
 * ssh -NR 9090:localhost:9090 remote-user@remote-host &
 * tcp-tunnel 9080 localhost 9090 &
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

    private final SlackApp slackBot;
    private final HttpServer httpServer;

    public static void main(String... args) throws Exception {
        logger.info("starting server on port 9090");
        try {
            HttpServerSlackServer s = new HttpServerSlackServer(9090);
            s.run();
        } catch (Exception e) {
            logger.error(e, "unable to ...");
            throw e;
        }
    }

    public HttpServerSlackServer(int port) throws IOException {
        slackBot = findFirst(ServiceLoader.load(SlackApp.class));
        if (slackBot == null) {
            throw new IllegalStateException("no com.andrewgilmartin.slack.SlackApp services defined in /META-INF/services/");
        }
        httpServer = HttpServer.create(new InetSocketAddress(port), 10);
        httpServer.setExecutor(Executors.newFixedThreadPool(10));
        httpServer.createContext("/", this);
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
        } else if (verificationToken == null || !verificationToken.equals(slackBot.getVerificationToken())) {
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
            slackBot.request(slackRequest, slackResponse);
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
