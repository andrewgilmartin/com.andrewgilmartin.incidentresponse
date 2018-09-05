package com.andrewgilmartin.incidentresponse;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Task {

    private final String id;
    private final User creator;
    private String description;
    private Status status;
    private final Set<User> assignments = new HashSet<>();

    public Task(String id, String description, User creator, Collection<User> assignments, Status status) {
        this.id = id;
        this.description = description;
        this.creator = creator;
        this.status = status;
        this.assignments.addAll(assignments);
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public User getCreator() {
        return creator;
    }

    public void setAssignments(Collection<User> users) {
        assignments.clear();
        assignments.addAll(users);
    }

    public Set<User> getAssignments() {
        return Collections.unmodifiableSet(assignments);
    }
}

// END

