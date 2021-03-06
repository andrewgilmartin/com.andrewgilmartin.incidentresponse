package com.andrewgilmartin.incidentresponse.aws;

import com.amazonaws.SdkBaseException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.andrewgilmartin.incidentresponse.Controller;
import com.andrewgilmartin.incidentresponse.*;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBClientBuilder;
import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.CreateDomainRequest;
import com.amazonaws.services.simpledb.model.CreateDomainResult;
import com.amazonaws.services.simpledb.model.GetAttributesRequest;
import com.amazonaws.services.simpledb.model.GetAttributesResult;
import com.amazonaws.services.simpledb.model.Item;
import com.amazonaws.services.simpledb.model.PutAttributesRequest;
import com.amazonaws.services.simpledb.model.PutAttributesResult;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.simpledb.model.SelectRequest;
import static com.amazonaws.services.simpledb.util.SimpleDBUtils.quoteName;
import static com.amazonaws.services.simpledb.util.SimpleDBUtils.quoteValue;
import com.andrewgilmartin.slack.SlackUser;
import com.andrewgilmartin.util.Logger;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class AwsController implements Controller {

    private static final Logger logger = Logger.getLogger(AwsController.class);

    private static final String WORKSPACE_ATTRIBUTE = "workspace";
    private static final String DESCRIPTION_ATTRIBUTE = "description";
    private static final String STATUS_ATTRIBUTE = "status";
    private static final String CREATOR_ATTRIBUTE = "creator";
    private static final String ASSIGNMENT_ATTRIBUTE = "assignment";

    private final AmazonSimpleDB db;
    private final String domain;
    private final Map<String, AtomicInteger> workspaceIdToTaskCount = new HashMap<>();

    public AwsController(String domain, AWSCredentialsProvider credentialsProvider) {
        this.domain = domain;
        this.db = AmazonSimpleDBClientBuilder
                .standard()
                .withCredentials(credentialsProvider) // eg, new AWSStaticCredentialsProvider(new BasicAWSCredentials(awsKey,awsSecret))
                .build();

        CreateDomainResult result = db.createDomain(new CreateDomainRequest().withDomainName(domain));
    }

    @Override
    public Workspace findWorkspace(String workspaceId) {
        Workspace workspace = new Workspace(
                workspaceId,
                StatusSet.COMMON_STATUS_SET
        );
        return workspace;
    }

    @Override
    public List<Task> findTasks(Workspace workspace, TaskFilter taskFilter) {
        List<Task> tasks = new LinkedList<>();
        try {
            SelectRequest selectRequest = new SelectRequest()
                    .withSelectExpression(
                            "select "
                            + DESCRIPTION_ATTRIBUTE + ", "
                            + STATUS_ATTRIBUTE + ", "
                            + CREATOR_ATTRIBUTE + ", "
                            + ASSIGNMENT_ATTRIBUTE
                            + " from "
                            + quoteName(domain)
                            + " where "
                            + WORKSPACE_ATTRIBUTE + " = " + quoteValue(workspace.getId())
                    )
                    .withConsistentRead(false); // TODO investiage whether or not this can be set to true
            for (Item item : db.select(selectRequest).getItems()) {
                Task task = constructTask(workspace, item.getName(), item.getAttributes());
                if (taskFilter.test(task)) {
                    tasks.add(task);
                }
            }
        } catch (SdkBaseException e) {
            logger.error(e, "unable to list tasks: workspaceId={0}", workspace.getId());
        }
        return tasks;
    }

    @Override
    public Task findTask(Workspace workspace, String taskId) {
        try {
            GetAttributesResult result = db.getAttributes(new GetAttributesRequest(domain, taskId));
            if (result.getSdkHttpMetadata().getHttpStatusCode() == 200) {
                return constructTask(workspace, taskId, result.getAttributes());
            }
        } catch (SdkBaseException e) {
            logger.error(e, "unable to find task: workspaceId={0}; taskId={1}", workspace.getId(), taskId);
        }
        return null;
    }

    private Task constructTask(Workspace workspace, String taskId, Collection<Attribute> attributes) {
        String description = null;
        Status status = null;
        User creator = null;
        List<User> assignments = new LinkedList<>();
        for (Attribute attribute : attributes) {
            switch (attribute.getName()) {
                case DESCRIPTION_ATTRIBUTE:
                    description = attribute.getValue();
                    break;
                case STATUS_ATTRIBUTE:
                    status = workspace.getStatusSet().findStatus(attribute.getValue());
                    break;
                case CREATOR_ATTRIBUTE:
                    creator = User.parseUser(attribute.getValue());
                    break;
                case ASSIGNMENT_ATTRIBUTE:
                    assignments.add(User.parseUser(attribute.getValue()));
                    break;
                default:
                    break;
            }
        }
        Task task = new Task(
                taskId,
                description,
                creator,
                assignments,
                status
        );
        return task;
    }

    @Override
    public Task addTask(Workspace workspace, String description, User creator, Collection<User> assignments, Status status) {
        try {
            String taskId = nextTaskId(workspace);
            List<ReplaceableAttribute> attributes = new LinkedList<>();
            attributes.add(new ReplaceableAttribute(WORKSPACE_ATTRIBUTE, workspace.getId(), Boolean.TRUE));
            attributes.add(new ReplaceableAttribute(DESCRIPTION_ATTRIBUTE, description, Boolean.TRUE));
            attributes.add(new ReplaceableAttribute(CREATOR_ATTRIBUTE, creator.toString(), Boolean.TRUE));
            attributes.add(new ReplaceableAttribute(STATUS_ATTRIBUTE, status.toString(), Boolean.TRUE));
            attributes.addAll(replaceableAttributeValues(ASSIGNMENT_ATTRIBUTE, assignments.iterator()));
            PutAttributesResult result = db.putAttributes(
                    new PutAttributesRequest()
                            .withDomainName(domain)
                            .withItemName(taskId)
                            .withAttributes(attributes)
            );
            if (result.getSdkHttpMetadata().getHttpStatusCode() == 200) {
                Task task = new Task(
                        taskId,
                        description,
                        creator,
                        assignments,
                        status
                );
                return task;
            }
        } catch (SdkBaseException e) {
            logger.error(e, "unable to add task: workspaceId={0}", workspace.getId());
        }
        return null;
    }

    @Override
    public Task updateTask(Workspace workspace, String taskId, String description, User creator, Collection<User> assignments, Status status) {
        try {
            List<ReplaceableAttribute> attributes = new LinkedList<>();
            attributes.add(new ReplaceableAttribute(WORKSPACE_ATTRIBUTE, workspace.getId(), Boolean.TRUE));
            attributes.add(new ReplaceableAttribute(DESCRIPTION_ATTRIBUTE, description, Boolean.TRUE));
            attributes.add(new ReplaceableAttribute(CREATOR_ATTRIBUTE, creator.toString(), Boolean.TRUE));
            attributes.add(new ReplaceableAttribute(STATUS_ATTRIBUTE, status.toString(), Boolean.TRUE));
            attributes.addAll(replaceableAttributeValues(ASSIGNMENT_ATTRIBUTE, assignments.iterator()));
            PutAttributesResult result = db.putAttributes(
                    new PutAttributesRequest()
                            .withDomainName(domain)
                            .withItemName(taskId)
                            .withAttributes(attributes)
            );
            if (result.getSdkHttpMetadata().getHttpStatusCode() == 200) {
                Task task = new Task(
                        taskId,
                        description,
                        creator,
                        assignments,
                        status
                );
                return task;
            }
        } catch (SdkBaseException e) {
            logger.error(e, "unable to update task: workspaceId={0}; taskId={1}", workspace.getId(), taskId);
        }
        return null;
    }

    private List<ReplaceableAttribute> replaceableAttributeValues(String name, Iterator values) {
        List<ReplaceableAttribute> attributes = new LinkedList<>();
        if (values.hasNext()) {
            attributes.add(new ReplaceableAttribute(name, values.next().toString(), Boolean.TRUE));
            while (values.hasNext()) {
                attributes.add(new ReplaceableAttribute(name, values.next().toString(), Boolean.FALSE));
            }
        }
        return attributes;
    }

    @Override
    public User findcreateUser(SlackUser slackUser) {
        return new User(slackUser.getId(), slackUser.getName());
    }

    private String nextTaskId(Workspace workspace) {
        synchronized (workspaceIdToTaskCount) {
            AtomicInteger count = workspaceIdToTaskCount.get(workspace.getId());
            if (count == null) {
                try {
                    /**
                     * TODO It is not good practice to put an HTTP request
                     * within a synchronized block. Given that this is a Slack
                     * application, we should use a delayed Slack response.
                     */
                    SelectRequest selectRequest = new SelectRequest()
                            .withSelectExpression(
                                    "select "
                                    + " count(*) "
                                    + " from "
                                    + quoteName(domain)
                                    + " where "
                                    + WORKSPACE_ATTRIBUTE + " = " + quoteValue(workspace.getId())
                            )
                            .withConsistentRead(false); // TODO investiage whether or not this can be set to true
                    count = new AtomicInteger(Integer.parseInt(db.select(selectRequest).getItems().get(0).getAttributes().get(0).getValue()));
                    workspaceIdToTaskCount.put(workspace.getId(), count);
                } catch (SdkBaseException e) {
                    logger.error(e, "unable to list tasks: workspaceId={0}", workspace.getId());
                    return null;
                }
            }
            return Integer.toString(count.incrementAndGet());
        }
    }
}

// END

