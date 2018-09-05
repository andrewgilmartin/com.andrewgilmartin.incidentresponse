package com.andrewgilmartin.incidentresponse;

import com.andrewgilmartin.slack.SlackChannel;
import com.andrewgilmartin.slack.SlackUser;
import com.andrewgilmartin.util.Logger;
import java.awt.Color;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

public class Model {

    private static final Logger logger = Logger.getLogger(Model.class);

    private static final List<Status> DEFAULT_STATUSES = Arrays.asList(
            new Status("ASSIGNED", Color.BLUE, 30, false), // also default/initial status
            new Status("RED", Color.RED, 60, false),
            new Status("YELLOW", Color.YELLOW, 50, false),
            new Status("GREEN", Color.GREEN, 40, false),
            // finished
            new Status("DONE", Color.BLACK, 20, true),
            new Status("CANCELED", Color.GRAY, 10, true)
    );

    private final Set<Workspace> workspaces = new HashSet<>();
    private final Set<User> users = new HashSet<>();

    public Workspace findWorkspace(String id) {
        for (Workspace ws : workspaces) {
            if (ws.getId().equals(id)) {
                return ws;
            }
        }
        return null;
    }

    public Workspace findcreateWorkspace(SlackChannel channel) {
        Workspace ws = findWorkspace(channel.getId());
        if (ws == null) {
            ws = new Workspace(channel.getId(), channel.getName(), DEFAULT_STATUSES);
            workspaces.add(ws);
        }
        return ws;
    }

    public User findUser(String id) {
        for (User u : users) {
            if (u.getId().equals(id)) {
                return u;
            }
        }
        return null;
    }

    public User findcreateUser(SlackUser slackUser) {
        User u = findUser(slackUser.getId());
        if (u == null) {
            u = new User(slackUser.getId(), slackUser.getName());
            users.add(u);
        }
        return u;
    }
}

// END

