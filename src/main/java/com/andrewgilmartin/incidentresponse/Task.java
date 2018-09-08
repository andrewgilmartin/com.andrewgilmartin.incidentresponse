package com.andrewgilmartin.incidentresponse;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Task {

    private final String id;
    private final User creator;
    private String description;
    private Status status;
    private Set<User> assignments;

    public Task(String id, String description, User creator, Collection<User> assignments, Status status) {
        this.id = id;
        this.description = description;
        this.creator = creator;
        this.status = status;
        this.assignments = Collections.unmodifiableSet(new HashSet<>(assignments));
    }

    public synchronized String getId() {
        return id;
    }

    public synchronized String getDescription() {
        return description;
    }

    public synchronized void setDescription(String description) {
        this.description = description;
    }

    public synchronized Status getStatus() {
        return status;
    }

    public synchronized void setStatus(Status status) {
        this.status = status;
    }

    public synchronized User getCreator() {
        return creator;
    }

    public synchronized void setAssignments(Collection<User> assignments) {
        this.assignments = Collections.unmodifiableSet(new HashSet<>(assignments));
    }

    public synchronized Set<User> getAssignments() {
        return assignments;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Task other = (Task) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }
}

// END

