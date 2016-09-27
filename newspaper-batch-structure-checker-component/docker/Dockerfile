FROM openjdk:7-jre

ENV component="newspaper-batch-structure-checker-component-1.10-SNAPSHOT"

WORKDIR /opt/

#Copy component to docker container and extract
ADD ${component}-package.tar.gz .

WORKDIR /opt/${component}

COPY config.properties logback.xml conf/

ENTRYPOINT [ "java", "-classpath", "conf:lib/*", "dk.statsbiblioteket.newspaper.BatchStructureCheckerExecutable", "-c", "conf/config.properties"]


