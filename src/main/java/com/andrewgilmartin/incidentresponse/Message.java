package com.andrewgilmartin.incidentresponse;

import com.andrewgilmartin.util.Logger;
import java.util.LinkedList;
import java.util.List;

public class Message {

    private static final Logger logger = Logger.getLogger(Message.class);

    private static final char EOL = 0;
    private static final int NAN = Integer.MAX_VALUE;

    private String errorMessage;
    private String id = null;
    private List<User> users = new LinkedList<>();
    private List<Status> statuses = new LinkedList<>();
    private StringBuilder text = new StringBuilder();

    public boolean hasError() {
        return errorMessage != null;
    }

    public String getError() {
        return errorMessage;
    }

    public boolean hasId() {
        return id != null;
    }

    public String getId() {
        return id;
    }

    public boolean hasUsers() {
        return !users.isEmpty();
    }

    public List<User> getUsers() {
        return users;
    }

    public boolean hasStatuses() {
        return !statuses.isEmpty();
    }

    public Status firstStatus() {
        return statuses.isEmpty() ? null : statuses.get(0);
    }

    public List<Status> getStatuses() {
        return statuses;
    }

    public boolean hasText() {
        return text.length() > 0;
    }

    public String getText() {
        return hasText() ? text.toString() : null;
    }

    public Message(Workspace workspace, String message) {

        char[] m = message.toCharArray();
        int l = m.length;
        int i = 0;
        int state = 0;
        int begin = NAN;
        String t1 = null;

        // looking for [ id ] ( text | "!" status | "<" user-id "|" user-name ">" )*
        
        PARSE:
        for (;;) {
            char c = i < l ? m[i++] : EOL;
            switch (state) {
                case 0: {
                    // start of line
                    switch (c) {
                        case EOL:
                            break PARSE;
                        case ' ':
                        case '\t':
                        case '\r':
                        case '\n':
                            continue;
                        case '<':
                            begin = i;
                            state = 4;
                            continue;
                        case '!':
                            begin = i;
                            state = 6;
                            continue;
                        case '0':
                        case '1':
                        case '2':
                        case '3':
                        case '4':
                        case '5':
                        case '6':
                        case '7':
                        case '8':
                        case '9':
                            begin = i - 1;
                            state = 2;
                            continue;
                        default:
                            begin = i - 1;
                            state = 3;
                            continue;
                    }
                }

                case 1: {
                    // inside line (and so no id)
                    switch (c) {
                        case EOL:
                            break PARSE;
                        case ' ':
                        case '\t':
                        case '\r':
                        case '\n':
                            continue;
                        case '<':
                            begin = i;
                            state = 4;
                            continue;
                        case '!':
                            begin = i;
                            state = 6;
                            continue;
                        default:
                            begin = i - 1;
                            state = 3;
                            continue;
                    }
                }

                case 2: {
                    // inside id                    
                    if (!Character.isDigit(c)) {
                        id = string(m, begin, i - 1);
                        begin = NAN;
                        i -= 1;
                        state = 1;
                    }
                    continue;
                }

                case 3: {
                    // inside text
                    switch (c) {
                        case EOL:
                            append(text, m, begin, i);
                            break PARSE;
                        case '<':
                        case '!':
                        case ' ':
                        case '\t':
                        case '\r':
                        case '\n':
                            append(text, m, begin, i - 1);
                            begin = NAN;
                            state = 1;
                            i -= 1;
                            continue;
                        default:
                            continue;
                    }
                }

                case 4: {
                    // after user <
                    switch (c) {
                        case EOL:
                            errorMessage = "missing the user's id";
                            break PARSE;
                        case '|':
                            t1 = string(m, begin, i - 1);
                            begin = i;
                            state = 5;
                        default:
                            continue;
                    }
                }

                case 5: {
                    // after user |
                    switch (c) {
                        case EOL:
                            errorMessage = "missing the user's name";
                            break PARSE;
                        case '>':
                            User user = new User(t1, string(m, begin, i - 1));
                            users.add(user);
                            begin = NAN;
                            state = 1;
                            continue;
                        default:
                            continue;
                    }
                }

                case 6: {
                    // after status !
                    if (Character.isLetterOrDigit(c)) {
                        // it has atleast one char
                        state = 7;
                    } else {
                        text.append("!");
                        state = 1;
                    }
                    continue;
                }

                case 7: {
                    // in status
                    if (c == EOL) {
                        String name = string(m, begin, i);
                        Status status = workspace.findStatus(name);
                        if (status == null) {
                            errorMessage = "unknown status !" + name;
                        } else {
                            statuses.add(status);
                        }
                        break PARSE;
                    } else if (!Character.isLetterOrDigit(c)) {
                        String name = string(m, begin, i - 1);
                        Status status = workspace.findStatus(name);
                        if (status == null) {
                            errorMessage = "unknown status !" + name;
                            break PARSE;
                        }
                        statuses.add(status);
                        state = 1;
                        i -= 1;
                    }
                    continue;
                }
            }
        }
    }
    
    private String string(char[] message, int begin, int end) {
        return begin != NAN && begin < end ? new String(message, begin, end - begin) : null;
    }

    private void append(StringBuilder sb, char[] message, int begin, int end) {
        if (sb.length() > 0) {
            sb.append(' ');
        }
        sb.append(message, begin, end - begin);
    }
}

// END

