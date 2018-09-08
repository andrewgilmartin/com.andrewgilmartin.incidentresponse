package com.andrewgilmartin.incidentresponse;

import com.andrewgilmartin.slack.SlackChannel;
import com.andrewgilmartin.slack.SlackUser;
import com.andrewgilmartin.util.Logger;
import java.awt.Color;
import java.util.Arrays;
import java.util.Set;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
    
    private final Set<Workspace> workspaces = new ConcurrentSkipListSet<>();
    private final ReadWriteLock workspacesLock = new ReentrantReadWriteLock();
    
    private final Set<User> users = new ConcurrentSkipListSet<>();
    private final ReadWriteLock usersLock = new ReentrantReadWriteLock();
    
    public Workspace findWorkspace(String id) {
        workspacesLock.readLock().lock();
        try {
            for (Workspace ws : workspaces) {
                if (ws.getId().equals(id)) {
                    return ws;
                }
            }
            return null;
        } finally {
            workspacesLock.readLock().unlock();
        }
    }
    
    public Workspace findcreateWorkspace(SlackChannel channel) {
        workspacesLock.writeLock().lock();
        try {
            Workspace ws = findWorkspace(channel.getId());
            if (ws == null) {
                ws = new Workspace(
                        channel.getId(), 
                        channel.getName(), 
                        new StatusSet(DEFAULT_STATUSES, DEFAULT_STATUSES.get(0)));
                workspaces.add(ws);
            }
            return ws;
        } finally {
            workspacesLock.writeLock().unlock();
        }
    }
    
    public User findUser(String id) {
        usersLock.readLock().lock();
        try {
            for (User u : users) {
                if (u.getId().equals(id)) {
                    return u;
                }
            }
            return null;
        } finally {
            usersLock.readLock().unlock();
        }
    }
    
    public User findcreateUser(SlackUser slackUser) {
        usersLock.writeLock().lock();
        try {
            User u = findUser(slackUser.getId());
            if (u == null) {
                u = new User(slackUser.getId(), slackUser.getName());
                users.add(u);
            }
            return u;
        } finally {
            usersLock.writeLock().unlock();
        }
    }
}

// END

