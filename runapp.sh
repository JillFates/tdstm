#!/bin/sh

export GRAILS_OPTS="-XX:MaxPermSize=1024m -Xmx1024M -server"

OPT="-reloading"
OPT="$OPT -Dtdstm.config.location=/etc/tdstm-config.groovy"

grails $OPT run-app

