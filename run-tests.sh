#!/bin/bash
set -e

mvn test surefire-report:report
