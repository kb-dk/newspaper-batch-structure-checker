FROM openjdk:7-jre

RUN apt-get update; \
    apt-get install -y sudo

ENV component="newspaper-batch-structure-checker-component-1.10-SNAPSHOT" \
    user="newspapr"

# #Create non-root user
# 2002 just because it does not overlap with anything
RUN useradd -ms /bin/bash -u 2002 ${user}

#Copy component to docker container and extract
ADD target/${component}-package.tar.gz /home/${user}/

#Skip the annoying middle layer, create the log folder and chown all to the created user
RUN mv /home/${user}/${component}/* /home/${user}/; \
    rm -r /home/${user}/${component}/; \
    mkdir "/home/${user}/logs"; \
    chown ${user}:${user} /home/${user} -R

VOLUME ["conf", "/home/${user}/logs"]

WORKDIR /home/${user}
ENTRYPOINT [ "bin/entry.sh" ]


