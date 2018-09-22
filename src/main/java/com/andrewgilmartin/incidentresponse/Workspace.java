package com.andrewgilmartin.incidentresponse;

import java.io.Serializable;
import java.util.Objects;

public class Workspace implements Serializable {

    private final String id;
    private final StatusSet statusSet;

    public Workspace(String id, StatusSet statusSet) {
        this.id = id;
        this.statusSet = statusSet;
    }

    public String getId() {
        return id;
    }

    public StatusSet getStatusSet() {
        return statusSet;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 13 * hash + Objects.hashCode(this.id);
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
        final Workspace other = (Workspace) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }
    
    
}

// END

