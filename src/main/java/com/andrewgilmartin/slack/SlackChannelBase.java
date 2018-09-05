package com.andrewgilmartin.slack;

import java.util.Objects;

public class SlackChannelBase implements SlackChannel {

    private final String id;
    private final String name;

    public SlackChannelBase(String id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        int hash = 5;
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
        // allow subclass to not have to implement the equals method
        if (!getClass().isAssignableFrom(obj.getClass())) {
            return false;
        }
        final SlackChannelBase other = (SlackChannelBase) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "[" + id + "|" + name + "]";
    }
}

// END

