package com.andrewgilmartin.slack;

public class SlackRequestBase implements SlackRequest {

    final SlackChannel channel;
    final SlackUser user;
    final String commandName;
    final String commandText;

    public SlackRequestBase(SlackChannel channel, SlackUser user, String commandName, String commandText) {
        this.channel = channel;
        this.user = user;
        this.commandName = commandName;
        this.commandText = commandText;
    }

    @Override
    public SlackChannel getChannel() {
        return channel;
    }

    @Override
    public SlackUser getUser() {
        return user;
    }

    @Override
    public String getCommandName() {
        return commandName;
    }

    @Override
    public String getCommandText() {
        return commandText;
    }

}

