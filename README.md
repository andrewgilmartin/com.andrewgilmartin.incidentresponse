# Incident Response Slack App

As our organization learns how to better use Slack we have experimented with different ways of managing the response to incidents, aka emergencies. When an incident is discovered the existing #incident-response channel is used to send an alert. An experiment was to then immediately create a new channel for only the new incident's pertinent communications. While we rarely have overlapping incidents having a dedicated channel prevents the interleaving of messages about other incidents (and tangents) and only those working the incident need join. When the incident is resolved its messages can be copied into the beginnings of the post-mortem document, and then the channel can be archived.

During the response, tasks emerge that need to be assigned and tracked. Slack itself is not good at this alone. There are many applications for task management that can be made accessible via Slack slash-commands. For incident response tasks, however, the general purpose applications were too focused on the user and not enough on the channel. When listing tasks you only want to see those for this incident. While applications have means of limiting the results with selection criteria, overall, the fit was bad.

What was needed was a task manager with a scope limited to one channel. The application would be installed in the workspace, accessible to everyone, everywhere without configuration, but channel focused. The task manager needed to support the use cases

* Adding tasks with a description, assignments, and a status. Only notify the assigned users.
* Updating a task’s description, assignments, or status. Only notify the channel when the changes are pertinent to all.
* Listing the tasks with optional criteria.

These use cases turned into the `/ir` slash-command

```
/ir description [ user … ] [ status ]

/ir task-id [ description [ user … ] [ status ]

/ir [ user … ] [ status … ]
```

The description is text, the user (or users) is indicated with an at-name, eg “@andrew”, and the status is indicated with a bang-name, eg “!red”. The task-id is a positive integer., eg “345”.

The Incident Response repository holds the source code for the Slack app. It is experimental on a number of fronts. The Incident Response Slack App uses Slack’s webhook outgoing message protocol using a minimal Slack SDK with one implementation. The app’s data model is not persistent; it did not need to be for the experiment. Lastly, the implementation does not depend on external jars; it uses only what Oracle provides in the JRE. This implementation limitation was undertaken to remind myself of how suitable Java is for students wanting to build internet tools without much background about the Java ecosystem. The short answer is, not very suitable.

The Incident Response data model is composed of Workspace, Task, User, and Status objects. 

A workspace has an id, a name, one or more statuses, and zero or more tasks. (The workspace is a Slack channel.) 

A task has an id, a description, one status, and zero or more assigned users. The task is used to reference the task when updating it using the slash command. 

A user has an id, and a name. (A user is a Slack user.)

A status has a name, color, order, and a completion indicator. The completion indicator aids determining when the channel needs to know about a task’s status change. The color is used when presenting the task as a Slack “attachment.”

To run the implementation create the jar 

```
mvn package
```

and then run

```
java \
  -Dcom.andrewgilmartin.incidentresponse.IncidentResponseSlackApp.verificationToken=XXX \
  -jar target/incidentresponse1-1.0-SNAPSHOT.jar \
  9090
```
Replace XXX with your verification token Slack provides when installing a slash command, and 9090 with the port number it should listen on.

END