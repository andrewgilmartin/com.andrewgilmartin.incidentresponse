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

    public static User parseUser(String s) {
        SlackUser u = parseSlackUserBase(s);
        if (u != null) {
            return new User(u.getId(), u.getName());
        }
        return null;
    }
}
