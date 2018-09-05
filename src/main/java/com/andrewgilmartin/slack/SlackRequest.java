package com.andrewgilmartin.slack;

/**
 * A SlackRequest instance holds the details of a Slack user's slash command
 * use.
 */
public interface SlackRequest {

    /**
     * The Slack channel from which the request was made.
     */
    SlackChannel getChannel();

    /**
     * The Slack user who made the request.
     */
    SlackUser getUser();

    /**
     * The Slack slash command name, eg /doit
     */
    String getCommandName();

    /**
     * The slash command's text.
     */
    String getCommandText();

}
