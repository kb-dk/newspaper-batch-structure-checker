#!/usr/bin/env bash

#Clean all and rebuild
docker ps -a | grep checker | cut -d' ' -f1 | xargs docker rm -v; docker rmi -f structure-checker; docker build -t structure-checker .


#bypass entrypoint
docker run -e USER_ID=$(id -u) --name 'structure-checker-test' -v $(pwd)/target:/home/newspapr/logs -it --entrypoint='bash' structure-checker

#Remove dangling containers
docker rm $(docker ps -a -q)

#Remove dangling images
docker rmi $(docker images --filter "dangling=true" -q --no-trunc)


#Run the autonomous component in a docker

docker run \
    -e USER_ID=$(id -u) \
    -v $(pwd)/target/logs:/home/newspapr/logs \
    -v $(pwd)/src/main/config:/home/newspapr/conf \
    statsbiblioteket/newspaper-batch-structure-checker-component:1.10-SNAPSHOT


#TODO the host folders MUST exist before invocation or they will be unreadable inside....
docker run \
    -v $(pwd)/target/logs:/home/newspapr/logs \
    statsbiblioteket/newspaper-batch-structure-checker-component:1.10-SNAPSHOT


#Set the docker users UID to the invoking user. Nessesary for having write permissions to the log dir (if used)
#   -e USER_ID=$(id -u)

#Specify an external folder to hold the logfiles
#   -v $(pwd)/target/logs:/home/newspapr/logs

#Specify an external folder with the config files
#    -v $(pwd)/src/main/config:/home/newspapr/conf \


docker run statsbiblioteket/newspaper-batch-structure-checker-component:1.10-SNAPSHOT
