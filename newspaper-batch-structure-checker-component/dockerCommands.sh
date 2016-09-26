#!/usr/bin/env bash
docker ps -a | grep checker | cut -d' ' -f1 | xargs docker rm -v; docker rmi -f structure-checker; docker build -t structure-checker .



 docker run -e USER_ID=$(id -u) --name 'structure-checker-test' -v $(pwd)/target:/home/newspapr/logs -it --entrypoint='bash' structure-checker