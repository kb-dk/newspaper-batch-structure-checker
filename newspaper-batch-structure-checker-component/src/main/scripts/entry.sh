#!/usr/bin/env bash

#echo $USER_ID # 1000 From docker run command line
#echo $user #From dockerfile ENV

usermod -u $USER_ID $user #Change uid to the specified user, so mount points work correctly

sudo -u $user java -classpath conf:lib/* dk.statsbiblioteket.newspaper.BatchStructureCheckerExecutable -c conf/config.properties
