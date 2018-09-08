package com.andrewgilmartin.incidentresponse;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicLong;

public class Workspace implements Serializable {

    private static final AtomicLong TASK_ID_GENERATOR = new AtomicLong(1);

    private final String id;
    private final String name;
    private final Set<Task> tasks = new ConcurrentSkipListSet<>((Task a, Task b) -> a.getId().compareTo(b.getId()));
    private final StatusSet statusSet;

    public Workspace(String id, String name, StatusSet statusSet) {
        this.id = id;
        this.name = name;
        this.statusSet = statusSet;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Set<Task> getTasks() {
        return Collections.unmodifiableSet(tasks);
    }

    public StatusSet getStatusSet() {
        return statusSet;
    }

    public Task createTask(String description, User creator, Collection<User> assignments, Status status) {
        Task task = new Task(
                Long.toString(TASK_ID_GENERATOR.incrementAndGet()),
                description,
                creator,
                assignments,
                status != null ? status : statusSet.getDefaultIntitialStatus() // TODO ensure that status is in the available statuses
        );
        tasks.add(task);
        return task;
    }

    public Task findTask(String id) {
        for (Task task : tasks) {
            if (task.getId().equals(id)) {
                return task;
            }
        }
        return null;
    }
}

// END

