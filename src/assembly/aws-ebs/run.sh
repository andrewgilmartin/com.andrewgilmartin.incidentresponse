#!/bin/bash

pwd
env | sort
java -version 2>&1

java -classpath lib com.andrewgilmartin.incidentresponse.aws.Main --port 5000 --path /ir

# END