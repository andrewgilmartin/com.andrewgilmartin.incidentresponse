package com.andrewgilmartin.incidentresponse;

import com.andrewgilmartin.slack.SlackRequest;
import com.andrewgilmartin.slack.SlackResponse;
import com.andrewgilmartin.slack.SlackResponseContent;
import java.util.function.BiConsumer;
import com.andrewgilmartin.slack.SlackApp;
import java.util.List;

public class IncidentResponseSlackApp implements SlackApp {

    private final String verificationToken;
    private final Controller controller;
    
    private final BiConsumer<SlackResponseContent, Task> formatTask = this::formatTask; // helper for inner class instances

    public IncidentResponseSlackApp(Controller controller, String verificationToken) {
        this.controller = controller;
        this.verificationToken = verificationToken;
    }

    public Controller getController() {
        return controller;
    }

    @Override
    public String getVerificationToken() {
        return verificationToken;
    }

    @Override
    public void request(SlackRequest request, SlackResponse response) {
        Workspace workspace = controller.findWorkspace(request.getChannel().getId());
        Message message = new Message(workspace, request.getCommandText());
        Command command;
        if (message.hasError()) {
            command = new UnparsableMessageErrorCommand(request, response, workspace, message);
        } else if (message.hasId()) {
            command = new UpdateCommand(request, response, workspace, message);
        } else if (message.hasText()) {
            if ("help".equalsIgnoreCase(message.getText())) {
                command = new HelpCommand(request, response, workspace, message);
            } else if ("all".equalsIgnoreCase(message.getText())) {
                command = new ListCommand(request, response, workspace, message);
            } else if ("finished".equalsIgnoreCase(message.getText())) {
                command = new ListCommand(request, response, workspace, message);
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
                    .textf("`%s [ all | finished ] [user...] [status...]`", slackRequest.getCommandName()).line()
                    .text("The available statuses are ").acceptAll((c, s) -> c.space().text(s), workspace.getStatusSet().getStatuses()).line()
                    .text("For further information visit ").link("http://nowhere.com");

        }
    }

    private class ListCommand extends CommandBase {

        public ListCommand(SlackRequest slackRequest, SlackResponse slackResponse, Workspace workspace, Message message) {
            super(slackRequest, slackResponse, workspace, message);
        }

        @Override
        public void perform() {
            TaskFilter listMatching = new TaskFilter();
            if ("all".equalsIgnoreCase(message.getText())) {
                listMatching.hasStatus(workspace.getStatusSet().getStatuses());
            } else if ("finished".equalsIgnoreCase(message.getText())) {
                listMatching.hasStatus(workspace.getStatusSet().getFinishedStatuses());
            } else if (message.hasStatuses()) {
                listMatching.hasStatus(message.getStatuses());
            } else {
                listMatching.hasStatus(workspace.getStatusSet().getUnfinishedStatuses());
            }
            if (message.hasUsers()) {
                listMatching.hasAssigment(message.getUsers());
            }
            List<Task> tasks = controller.findTasks(workspace, listMatching);
            if (tasks.isEmpty()) {
                slackResponse.getResponseContent().text("No matching tasks");
            } else {
                slackResponse.getResponseContent().text("Matched tasks");
                for (Task task : tasks) {
                    slackResponse.getResponseContent().accept(formatTask, task);
                }
            }
        }
    }

    private class AddCommand extends CommandBase {

        public AddCommand(SlackRequest slackRequest, SlackResponse slackResponse, Workspace workspace, Message message) {
            super(slackRequest, slackResponse, workspace, message);
        }

        @Override
        public void perform() {
            Task addedTask = controller.addTask(
                    workspace,
                    message.getText(),
                    new User(slackRequest.getUser()),
                    message.getUsers(),
                    message.hasStatuses() ? message.firstStatus() : null
            );
            slackResponse.getResponseContent().text("Added task").accept(formatTask, addedTask);
        }
    }

    private class UpdateCommand extends CommandBase {

        public UpdateCommand(SlackRequest slackRequest, SlackResponse slackResponse, Workspace workspace, Message message) {
            super(slackRequest, slackResponse, workspace, message);
        }

        @Override
        public void perform() {
            Task task = controller.findTask(workspace, message.getId());
            if (task != null) {
                String description = null;
                // NOTE multiple threads could be in this code updating the same task
                if (message.hasText()) {
                    description = message.getText();
                }
                Status status = null;
                boolean becameFinished = false;
                boolean becameUnfinished = false;
                if (message.hasStatuses()) {
                    status = message.firstStatus();
                    boolean wasFinished = task.getStatus().isFinished(); // need stable value for the next two assignments
                    becameFinished = !wasFinished && status.isFinished();
                    becameUnfinished = wasFinished && !status.isFinished();
                }
                List<User> assignments = null;
                if (message.hasUsers()) {
                    assignments = message.getUsers();
                }
                User creator = controller.findcreateUser(slackRequest.getUser());
                
                Task updatedTask = controller.updateTask(workspace, task.getId(), description, creator, assignments, status);

                if (becameFinished) {
                    slackResponse.setBroadcastResponse(true);
                    slackResponse.getResponseContent().text("Finished task").accept(formatTask, updatedTask);
                } else if (becameUnfinished) {
                    slackResponse.setBroadcastResponse(true);
                    slackResponse.getResponseContent().text("Reopened task").accept(formatTask, updatedTask);
                } else {
                    slackResponse.getResponseContent().text("Updated task").accept(formatTask, updatedTask);
                }
            } else {
                slackResponse.setErrorText("I'm sorry, but I can't find the task. List the tasks to confirm the task id.");
            }
        }
    }

}

// END

