package com.andrewgilmartin.incidentresponse;

import com.andrewgilmartin.slack.SlackRequest;
import com.andrewgilmartin.slack.SlackResponse;
import com.andrewgilmartin.slack.SlackResponseContent;
import java.util.Iterator;
import java.util.function.BiConsumer;
import com.andrewgilmartin.slack.SlackApp;

public class IncidentResponseSlackApp implements SlackApp {

    private String verificationToken;
    private final Model model = new Model();
    private final BiConsumer<SlackResponseContent, Task> formatTask = this::formatTask; // helper for inner class instances

    public IncidentResponseSlackApp() {
        String verificationToken = System.getProperty("com.andrewgilmartin.incidentresponse.IncidentResponseSlackApp.verificationToken");
        if (verificationToken == null) {
            throw new IllegalStateException("Missing the Slack verification token property com.andrewgilmartin.incidentresponse.IncidentResponseSlackApp.verificationToken");
        }
        this.verificationToken = verificationToken;
    }

    public void setVerificationToken(String verificationToken) {
        this.verificationToken = verificationToken;
    }

    @Override
    public String getVerificationToken() {
        return verificationToken;
    }

    @Override
    public void request(SlackRequest request, SlackResponse response) {
        Workspace workspace = model.findcreateWorkspace(request.getChannel());
        Message message = new Message(workspace, request.getCommandText());
        Command command;
        if (message.hasError()) {
            command = new UnparsableMessageErrorCommand(request, response, workspace, message);
        } else if (message.hasId()) {
            command = new UpdateCommand(request, response, workspace, message);
        } else if (message.hasText()) {
            if ("help".equalsIgnoreCase(message.getText())) {
                command = new HelpCommand(request, response, workspace, message);
            } else {
                command = new AddCommand(request, response, workspace, message);
            }
        } else {
            command = new ListCommand(request, response, workspace, message);
        }
        command.perform();
    }

    private void formatTask(SlackResponseContent content, Task task) {
        content
                .attachment()
                .color(task.getStatus().getColor())
                .text(task.getId())
                .space()
                .text(task.getStatus())
                .space()
                .text(task.getDescription());
        for (User user : task.getAssignments()) {
            content.space().user(user);
        }
    }

    private interface Command {

        void perform();
    }

    private abstract class CommandBase implements Command {

        protected final SlackRequest slackRequest;
        protected final SlackResponse slackResponse;
        protected final Workspace workspace;
        protected final Message message;

        public CommandBase(SlackRequest slackRequest, SlackResponse slackResponse, Workspace workspace, Message message) {
            this.slackRequest = slackRequest;
            this.slackResponse = slackResponse;
            this.workspace = workspace;
            this.message = message;
        }

    }

    private class ErrorCommand extends CommandBase {

        public ErrorCommand(SlackRequest slackRequest, SlackResponse slackResponse, Workspace workspace, Message message) {
            super(slackRequest, slackResponse, workspace, message);
        }

        @Override
        public void perform() {
            slackResponse.setErrorText(message.getError());
        }

    }

    private class UnparsableMessageErrorCommand extends ErrorCommand {

        public UnparsableMessageErrorCommand(SlackRequest slackRequest, SlackResponse slackResponse, Workspace workspace, Message message) {
            super(slackRequest, slackResponse, workspace, message);
        }

        @Override
        public void perform() {
            slackResponse.setErrorText("I'm sorry, but I do not understand the message. Please, check for typos.");
        }

    }

    private class HelpCommand extends CommandBase {

        public HelpCommand(SlackRequest slackRequest, SlackResponse slackResponse, Workspace workspace, Message message) {
            super(slackRequest, slackResponse, workspace, message);
        }

        @Override
        public void perform() {
            slackResponse.getResponseContent()
                    .text("Briefly, the 3 incident response actions are add task, update task, and list tasks. Ie, ").line()
                    .textf("`%s description [user...] [status]`", slackRequest.getCommandName()).line()
                    .textf("`%s 2 [description] [user...] [status]`", slackRequest.getCommandName()).line()
                    .textf("`%s [user...] [status...]`", slackRequest.getCommandName()).line()
                    .text("The available statuses are ").acceptAll((c, s) -> c.space().text(s), workspace.getStatuses()).line()
                    .text("For further information visit ").link("http://nowhere.com");

        }
    }

    private class ListCommand extends CommandBase {

        public ListCommand(SlackRequest slackRequest, SlackResponse slackResponse, Workspace workspace, Message message) {
            super(slackRequest, slackResponse, workspace, message);
        }

        @Override
        public void perform() {
            Iterator<Task> tasks = workspace.getTasks()
                    .stream()
                    .filter(
                            TaskFilter.create()
                                    .hasDescription(message.getText())
                                    .hasStatus(message.getStatuses())
                                    .hasAssigment(message.getUsers()))
                    .sorted((a, b) -> a.getId().compareTo(b.getId()))
                    .iterator();
            if (tasks.hasNext()) {
                slackResponse.getResponseContent().text("Matched tasks");
                do {
                    slackResponse.getResponseContent().accept(formatTask, tasks.next());
                } while (tasks.hasNext());
            } else {
                slackResponse.getResponseContent().text("No matching tasks");
            }
        }
    }

    private class AddCommand extends CommandBase {

        public AddCommand(SlackRequest slackRequest, SlackResponse slackResponse, Workspace workspace, Message message) {
            super(slackRequest, slackResponse, workspace, message);
        }

        @Override
        public void perform() {
            Task task = workspace.createTask(
                    message.getText(),
                    model.findcreateUser(slackRequest.getUser()),
                    message.getUsers(),
                    message.hasStatuses() ? message.firstStatus() : null
            );
            slackResponse.getResponseContent().text("Added task").accept(formatTask, task);
        }
    }

    private class UpdateCommand extends CommandBase {

        public UpdateCommand(SlackRequest slackRequest, SlackResponse slackResponse, Workspace workspace, Message message) {
            super(slackRequest, slackResponse, workspace, message);
        }

        @Override
        public void perform() {
            Task task = workspace.findTask(message.getId());
            if (task != null) {
                if (message.hasText()) {
                    task.setDescription(message.getText());
                }
                boolean becameFinished = false;
                boolean becomeUnfinished = false;
                if (message.hasStatuses()) {
                    Status status = message.firstStatus();
                    becameFinished = status.isFinished() && !task.getStatus().isFinished();
                    becomeUnfinished = !status.isFinished() && task.getStatus().isFinished();
                    task.setStatus(status);
                }
                if (message.hasUsers()) {
                    task.setAssignments(message.getUsers());
                }
                if (becameFinished) {
                    slackResponse.setBroadcastResponse(true);
                    slackResponse.getResponseContent().text("Finished task").accept(formatTask, task);
                } else if (becomeUnfinished) {
                    slackResponse.setBroadcastResponse(true);
                    slackResponse.getResponseContent().text("Reopened task").accept(formatTask, task);
                } else {
                    slackResponse.getResponseContent().text("Updated task").accept(formatTask, task);
                }
            } else {
                slackResponse.setErrorText("I'm sorry, but I can't find the task. Perhaps, list the tasks to confirm the task id.");
            }
        }
    }

}

// END

