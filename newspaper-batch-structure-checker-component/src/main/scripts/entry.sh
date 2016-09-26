#!/usr/bin/env bash

#echo $USER_ID # From docker run command line
#echo $user #From dockerfile ENV

if [[ -v "${USER_ID// }" ]]; then
    usermod -u $USER_ID $user #Change uid to the specified user, so mount points work correctly
fi

sudo -u $user java -classpath conf:lib/* dk.statsbiblioteket.newspaper.BatchStructureCheckerExecutable -c conf/config.properties
