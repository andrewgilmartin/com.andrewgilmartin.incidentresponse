package com.andrewgilmartin.incidentresponse;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class TaskFilter implements Predicate<Task> {

    private Pattern includedDescription;
    private final Set<User> includedUsers = new HashSet<>();
    private final Set<Status> includedStatuses = new HashSet<>();

    public static TaskFilter create() {
        return new TaskFilter();
    }

    public TaskFilter hasDescription(String regex) {
        if (regex != null) {
            this.includedDescription = Pattern.compile(regex);
        }
        return this;
    }

    public TaskFilter hasAssigment(Collection<User> users) {
        if (users != null) {
            this.includedUsers.addAll(users);
        }
        return this;
    }

    public TaskFilter hasAssigment(User user) {
        if (user != null) {
            this.includedUsers.add(user);
        }
        return this;
    }

    public TaskFilter hasStatus(Collection<Status> statuses) {
        if (statuses != null) {
            this.includedStatuses.addAll(statuses);
        }
        return this;
    }

    public TaskFilter hasStatus(Status status) {
        if (status != null) {
            this.includedStatuses.add(status);
        }
        return this;
    }

    @Override
    public boolean test(Task t) {
        return (includedDescription == null || includedDescription.matcher(t.getDescription()).find())
                && (includedUsers.isEmpty() || !Collections.disjoint(t.getAssignments(), includedUsers))
                && (includedStatuses.isEmpty() || includedStatuses.contains(t.getStatus()));
    }

}

// END
