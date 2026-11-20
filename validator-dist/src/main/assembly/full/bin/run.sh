#!/bin/sh

cd $(dirname $(readlink -f $0))/..

java $JAVA_OPTS -classpath .:conf:extension/*:lib/* no.difi.vefa.validator.dist.Cli $@
