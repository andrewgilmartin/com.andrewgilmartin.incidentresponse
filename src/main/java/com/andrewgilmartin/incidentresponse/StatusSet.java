package com.andrewgilmartin.incidentresponse;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

/**
 * Convenience class for organizing statuses.
 */
public class StatusSet {

    private final Set<Status> allStatuses;
    private final Set<Status> unfinishedStatuses;
    private final Set<Status> finishedStatuses;
    private final Status defaultIntitialStatus;

    public StatusSet(Collection<Status> statuses, Status defaultStatus) {
        this.allStatuses = Collections.unmodifiableSet(new ConcurrentSkipListSet<>(
                statuses
        ));
        this.finishedStatuses = Collections.unmodifiableSet(new ConcurrentSkipListSet<>(
                statuses.stream().filter((s) -> s.isFinished()).collect(Collectors.toList())
        ));
        this.unfinishedStatuses = Collections.unmodifiableSet(new ConcurrentSkipListSet<>(
                statuses.stream().filter((s) -> !s.isFinished()).collect(Collectors.toList())
        ));
        this.defaultIntitialStatus = defaultStatus;
    }

    /**
     * Returns all the statuses.
     */
    public Set<Status> getStatuses() {
        return allStatuses;
    }

    /**
     * Returns only those statuses that are flagged as not finished.
     */
    public Set<Status> getUnfinishedStatuses() {
        return unfinishedStatuses;
    }

    /**
     * Returns only those statuses that are flagged as finished.
     */
    public Set<Status> getFinishedStatuses() {
        return finishedStatuses;
    }

    /**
     * Returns the default status, ie for use when status is unspecified.
     */
    public Status getDefaultIntitialStatus() {
        return defaultIntitialStatus;
    }

    /**
     * Find the status with the given, case-insensitive name
     */
    public Status findStatus(String name) {
        for (Status status : allStatuses) {
            if (status.getName().equalsIgnoreCase(name)) {
                return status;
            }
        }
        return null;
    }
}
