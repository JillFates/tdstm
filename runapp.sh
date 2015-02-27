#!/bin/sh

# 
# Usage
# ./runapp.sh
# ./runapp.sh test-app -unit RunbookService
# ./runapp.sh test-app -integration GormUtil
#

if [ "$1" == "" ]; then
   CMD="run-app"
else
   CMD="$1 $2 $3 $4 $5 $6 $7 $8"
fi

export GRAILS_OPTS="-XX:MaxPermSize=1024m -Xmx1024M -server"

OPT="-reloading"
OPT="$OPT -Dtdstm.config.location=/etc/tdstm-config.groovy"

grails $OPT $CMD

