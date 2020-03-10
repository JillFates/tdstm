#!/bin/bash

# 
# Usage
# ./runapp.sh
# ./runapp.sh test-app -unit PersonService
# ./runapp.sh test-app -integration GormUtil
#

# Configuration Settings
SDK_INIT=~/.sdkman/bin/sdkman-init.sh
GRAILS_VERSION=2.3.11
JVM_MEMORY=1526m
JVM_PERMGEN=512m

if [ ! -f $SDK_INIT ]; then
   echo "Please install the SDKMan! before trying to use this script (see http://sdkman.io/)"
   exit 1
fi 
 
source $SDK_INIT
if [ $? -ne 0 ]; then
   echo Failed to initialize GVM
   exit 1
fi

sdk use grails $GRAILS_VERSION
if [ $? -ne 0 ]; then
   echo "Failed to select Grails $GRAILS_VERSION, please use the 'sdk install grails $GRAILS_VERSION' command before continuing"
   exit 1
fi

# -------------------
# The follow are the JVM switch settings for the application
# -------------------

# A number of default runtime settings
GRAILS_OPTS="-Xdebug -server -XX:+UseParallelGC -Djava.net.preferIPv4Stack=true"

# Memory Settings
GRAILS_OPTS="$GRAILS_OPTS -Xmx$JVM_MEMORY -Xms$JVM_MEMORY -XX:PermSize=$JVM_PERMGEN -XX:MaxPermSize=$JVM_PERMGEN"

# Set the JNI inflation to high value to limit only methods call excessively
#    see https://blogs.oracle.com/buck/entry/inflation_system_properties
GRAILS_OPTS="$GRAILS_OPTS -Dsun.reflect.inflationThreshold=100000"

# Configuration to allow to attach to JVM for debugger/profiler  
#GRAILS_OPTS="$GRAILS_OPTS -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"

export GRAILS_OPTS

# -------------------
# The following are commandline options to pass into Grails directly
# -------------------

# Configuration to point to our own tdstm-config.groovy file
OPT="-reloading -Dtdstm.config.location=/etc/tdstm-config.trunk.groovy"

# Set Grails to disable recompile and reloading to improve performance (QA Only)
#OPTS="$OPTS -Ddisable.auto.recompile=true -Dgrails.gsp.enable.reload=false -noreloading"


if [ "$1" == "" ]; then
   CMD=" run-app"
else
   CMD="$1 $2 $3 $4 $5 $6 $7 $8"
fi

grails $OPT $CMD

