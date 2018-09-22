package com.andrewgilmartin.incidentresponse;

import com.andrewgilmartin.slack.SlackUser;
import com.andrewgilmartin.slack.SlackUserBase;

public class User extends SlackUserBase {

    public User(SlackUser user) {
        super(user.getId(), user.getName());
    }

    public User(String id, String name) {
        super(id, name);
    }

}
