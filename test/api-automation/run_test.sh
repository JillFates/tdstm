#!/bin/bash
#
# This script is responsible to start the API Automation test
# with newman/postman. 
#
# 1. Run Script
#    This script should be invoked by given an environment. An environment
#    is a file located on the resources/<server_hostname>.postman_environment.json
#    to run the API automation test again the TM QA 04 server, execute:
#        $ ./run_test.sh tmqa04.tm.tdsops.net
#    the run_test.sh arguments will be translated to newman run tmqa04.tm.tdsops.net.postman_environment.json
#
# 2. Adding some newman_opts
#    To make the newman execution flexible an environment variable could be used to define extra newmna options.
#    You need to export the NEWMAN_OPTS variable prior to running the this script. 
#        $ export NEWMAN_OPTS='timeout 30 --disable-unicode --disable-color'
#        $ ./run_test.sh tmqa04.tm.tdsops.net
#    the NEWMAN_OPTS options will be added by the end of the newmna execution, the script will execute something like:
#        $ newman run ..... timeout 30 --disable-unicod --disable-color 
#


function RUN_NEWMAN_ON_FILE {
    if ! which newman &> /dev/null; then
        echo 'ERROR: Could not locate the newman executable'
        exit 1
    fi

    POSTMAN_COLLECTION_FILE=$1
    if [[ ! $POSTMAN_COLLECTION_FILE ]]; then  
        echo 'ERROR: The RUN_NEWMAN_ON_FILE function requires a postman collection to be executed.'
        echo 'ERROR: No file was specified. '
        exit 2
    fi

    if [[ ! -f $POSTMAN_COLLECTION_FILE ]]; then
        echo 'ERROR: Coould not locate the file: ${POSTMAN_COLLECTION_FILE}'
        exit 3
    fi

    echo "Running postman collection: ${1}"
    newman run $POSTMAN_COLLECTION_FILE \
        -g "${NEWMAN_ENVIRONMENT_FILE}" --insecure $NEWMAN_OPTS
}

# ----------------------------------------
# MAIN SCRIPT START HERE
# ---------------------------------------- 
[[ ! $NEWMAN_ENVIRONMENT_FILE ]] && {
    export NEWMAN_ENVIRONMENT_FILE="resources/${1}.postman_environment.json"
}

if [[ ! -f $NEWMAN_ENVIRONMENT_FILE ]]; then
    echo "WARN: Could not locate the file: ${NEWMAN_ENVIRONMENT_FILE}"
    echo "WARN: Skipping API Integration. Due to the environment ${1} is not supported!"
    exit 0
fi

echo "Loading environment from: ${NEWMAN_ENVIRONMENT_FILE}"

# Run newman files 
RUN_NEWMAN_ON_FILE 'src/ProviderEndpointsAutomation.postman_collection.json'
