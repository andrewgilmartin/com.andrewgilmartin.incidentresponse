package com.andrewgilmartin.slack;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SlackUserBase implements SlackUser {

    protected static Pattern SLACK_USER_PATTERN = Pattern.compile("<(.*?)\\|(.*?)>");

    private final String id;
    private final String name;

    public SlackUserBase(String id, String name) {
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
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        final SlackUserBase other = (SlackUserBase) that;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "<" + id + "|" + name + ">";
    }

    public static SlackUserBase parseSlackUserBase(String s) {
        Matcher m = SLACK_USER_PATTERN.matcher(s);
        if (m.matches()) {
            return new SlackUserBase(m.group(1), m.group(2));
        }
        return null;
    }
}

// END

