package com.andrewgilmartin.incidentresponse;

import java.awt.Color;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;

public class Workspace implements Serializable {

    private static final AtomicLong TASK_ID_GENERATOR = new AtomicLong(1);

    private final String id;
    private final String name;
    private final Set<Task> tasks = new HashSet<>();
    private final Set<Status> statuses = new TreeSet<>();
    private Status defaultIntitialStatus;

    public Workspace(String id, String name, List<Status> statuses) {
        this.id = id;
        this.name = name;
        this.statuses.addAll(statuses);
        this.defaultIntitialStatus = statuses.get(0);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Task createTask(String description, User creator, Collection<User> assignments, Status status) {
        Task task = new Task(
                Long.toString(TASK_ID_GENERATOR.incrementAndGet()),
                description,
                creator,
                assignments,
                status != null ? status : defaultIntitialStatus
        );
        tasks.add(task);
        return task;
    }

    public Set<Task> getTasks() {
        return Collections.unmodifiableSet(tasks);
    }

    public Status createStatus(String name, Color color, int order, boolean finished) {
        Status status = new Status(name, color, order, finished);
        statuses.add(status);
        if (status.compareTo(defaultIntitialStatus) < 0) {
            defaultIntitialStatus = status;
        }
        return status;
    }

    public void removeStatus(Status removed, Status replacement) {
        if (statuses.remove(removed)) {
            for (Task task : tasks) {
                if (task.getStatus() == removed) {
                    task.setStatus(replacement);
                }
            }
            if (removed.equals(defaultIntitialStatus)) {
                this.defaultIntitialStatus = statuses.stream().sorted().findFirst().get();
            }
        }
    }

    public Set<Status> getStatuses() {
        return Collections.unmodifiableSet(statuses);
    }

    public Task findTask(String id) {
        for (Task task : tasks) {
            if (task.getId().equals(id)) {
                return task;
            }
        }
        return null;
    }

    public Status findStatus(String name) {
        for (Status status : statuses) {
            if (status.getName().equalsIgnoreCase(name)) {
                return status;
            }
        }
        return null;
    }

}

// END

