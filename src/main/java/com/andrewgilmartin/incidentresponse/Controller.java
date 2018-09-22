package com.andrewgilmartin.incidentresponse;

import com.andrewgilmartin.slack.SlackUser;
import java.util.Collection;
import java.util.List;

public interface Controller {

    Workspace findWorkspace(String workspaceId);

    Task addTask(Workspace workspace, String description, User creator, Collection<User> assignments, Status status);

    Task updateTask(Workspace workspace, String taskId, String description, User creator, Collection<User> assignments, Status status);

    Task findTask(Workspace workspace, String taskId);

    List<Task> findTasks(Workspace workspace, TaskFilter taskFilter);

    User findcreateUser(SlackUser slackUser);

}

// END
