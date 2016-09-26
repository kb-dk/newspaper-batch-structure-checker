FROM openjdk:7-jre

#develro user
RUN useradd -ms /bin/bash develro

#Create non-root user
RUN useradd -ms /bin/bash newspapr
USER newspapr

#Copy component to docker container
COPY target/newspaper-batch-structure-checker-component-1.10-SNAPSHOT-package.tar.gz /home/newspapr/newspaper-batch-structure-checker-component-package.tar.gz

#Extract component

#Bake in config

#Run


#Show how to make derived docker with other config

