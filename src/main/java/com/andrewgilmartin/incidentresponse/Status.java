package com.andrewgilmartin.incidentresponse;

import java.awt.Color;
import java.util.Objects;

public class Status implements Comparable<Status> {

    private final String name;
    private final Color color;
    private final int order;
    private final boolean finished;

    public Status(String name, Color color, int order, boolean finished) {
        this.name = name;
        this.color = color;
        this.order = order;
        this.finished = finished;
    }

    public String getName() {
        return name;
    }

    public Color getColor() {
        return color;
    }

    public int getOrder() {
        return order;
    }

    public boolean isFinished() {
        return finished;
    }

    @Override
    public String toString() {
        return "!"+name;
    }
    
    public static Status valueOf(String name) {
        return name != null ? new Status(name, Color.BLUE, 1, false) : null;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.name);
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
        final Status other = (Status) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(Status that) {
        int c = this.order - that.order;
        if ( c == 0 ) {
            c = this.name.compareTo(that.name);
        }
        return c;
    }
        
}

// END

