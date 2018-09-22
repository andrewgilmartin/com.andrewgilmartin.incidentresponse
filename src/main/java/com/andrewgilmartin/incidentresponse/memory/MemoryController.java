package com.andrewgilmartin.incidentresponse.memory;

import com.andrewgilmartin.incidentresponse.Controller;
import com.andrewgilmartin.incidentresponse.Status;
import com.andrewgilmartin.incidentresponse.StatusSet;
import com.andrewgilmartin.incidentresponse.Task;
import com.andrewgilmartin.incidentresponse.TaskFilter;
import com.andrewgilmartin.incidentresponse.User;
import com.andrewgilmartin.incidentresponse.Workspace;
import com.andrewgilmartin.slack.SlackUser;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class MemoryController implements Controller {

    private static final AtomicInteger ID_GENERATOR = new AtomicInteger(0);

    private final Map<String, Workspace> idToWorkspace = new HashMap<>();

    private static class MemoryWorkspace extends Workspace {

        private final List<Task> tasks = new LinkedList<>();

        public MemoryWorkspace(String id, StatusSet statusSet) {
            super(id, statusSet);
        }

        public List<Task> getTasks() {
            return tasks;
        }

    }

    @Override
    public Workspace findWorkspace(String workspaceId) {
        synchronized (idToWorkspace) {
            Workspace workspace = idToWorkspace.get(workspaceId);
            if (workspace == null) {
                workspace = new Workspace(workspaceId, StatusSet.COMMON_STATUS_SET);
                idToWorkspace.put(workspace.getId(), workspace);
            }
            return workspace;
        }
    }

    @Override
    public Task addTask(Workspace workspace, String description, User creator, Collection<User> assignments, Status status) {
        synchronized (workspace) {
            Task task = new Task(Integer.toString(ID_GENERATOR.incrementAndGet()), description, creator, assignments, status);
            ((MemoryWorkspace) workspace).getTasks().add(task);
            return task;
        }
    }

    @Override
    public Task updateTask(Workspace workspace, String taskId, String description, User creator, Collection<User> assignments, Status status) {
        synchronized (workspace) {
            ListIterator<Task> t = ((MemoryWorkspace) workspace).getTasks().listIterator();
            while (t.hasNext()) {
                Task task = t.next();
                if (task.getId().equals(taskId)) {
                    Task replacement = new Task(
                            task.getId(),
                            description,
                            creator,
                            assignments,
                            status
                    );
                    t.set(task);
                    return replacement;
                }
            }
            return null;
        }
    }

    @Override
    public Task findTask(Workspace workspace, String taskId) {
        synchronized (workspace) {
            for (Task task : ((MemoryWorkspace) workspace).getTasks()) {
                if (task.getId().equals(taskId)) {
                    return task;
                }
            }
            return null;
        }
    }

    @Override

    public List<Task> findTasks(Workspace workspace, TaskFilter taskFilter) {
        synchronized (workspace) {
            List<Task> tasks = new LinkedList<>();
            for (Task task : ((MemoryWorkspace) workspace).getTasks()) {
                if (taskFilter.test(task)) {
                    tasks.add(task);
                }
            }
            return tasks;
        }
    }

    @Override
    public User findcreateUser(SlackUser slackUser) {
        return new User(slackUser.getId(), slackUser.getName());
    }

}
