# Run goals on selected projects and their dependencies
- ``mvn --projects A,B,F --also-make clean install``
- ``mvn -pl A,B,F -am clean install``
- https://stackoverflow.com/questions/1114026/maven-modules-building-a-single-specific-module
