#!/bin/bash
set -e

# The bank-client and the rabbitmq
# need to be installed locally as
# it is used also by the end-to-end tests.

mvn clean install -f bank-client/pom.xml
mvn clean install -f rabbitmq/pom.xml

mvn package -f services/pom.xml

