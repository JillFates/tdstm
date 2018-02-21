#!/bin/bash
#
# This script is responsible to start the API Automation test
# with newman/postman. 
#
# 1. Run Script
#    This script should be invoked by given an environment. An environment
#    is a file located on the resources/<server_hostname>.postman_environment.json
#    to run the API automation test against the TM QA 04 server, execute:
#        $ ./run_test.sh tmqa04.tm.tdsops.net
#    the run_test.sh arguments will be translated to newman run tmqa04.tm.tdsops.net.postman_environment.json
#
# 2. Adding some newman_opts
#    To make the newman execution flexible an environment variable could be used to define extras newman options.
#    You need to export the NEWMAN_OPTS variable prior to running the this script. 
#        $ export NEWMAN_OPTS='timeout 30 --disable-unicode --disable-color'
#        $ ./run_test.sh tmqa04.tm.tdsops.net
#    the NEWMAN_OPTS options will be added by the end of the newmna execution, the script will execute something like:
#        $ newman run ..... timeout 30 --disable-unicod --disable-color 
#
# 3. You could override the colleciton executed by newman, by settings the NEWMAN_COLLECTION environment variable.

function show_help {
    echo "Usage: ${0} [OPTIONS] SERVER1 SERVER2 ..."
    echo
    echo "Options: "
    echo "    -i, --interactive         - Enable the interactive mode"
    echo "    -f, --folder              - Define a specific Newman folder to be executed"
    echo "    -l, --list                - List the available targets"
    echo "    -h, --help                - Display this message"
    echo
    echo "Environment Variable: "
    echo "    NEWMAN_COLLECTION         - Defines a collection to be run: By default is: src/ProviderEndpointsAutomation.postman_collection.json"
    echo "    NEWMAN_OPTS               - Add newman supported options. (Run 'newman --help' to see available options)"
}

function LIST_OPTIONS {
    echo "Target systems: (Run: ${0} <target1> <target2>)"
    for file in `ls resources/*.json | egrep -oh '([0-9A-Za-z_\.\-])+(.com|.net|.local)'`; do
        echo "    ${file}"
    done
    echo

    echo "List available newman directories (use on the ${0} --folder <folder_name>"
    IFS=$'\n'; for folder in `egrep -oh '\"name\": \"(.*)\"' src/*.json | cut -d':' -f2`; do
        echo "  ${folder}"
    done

    echo
    echo 'Choose one of the targets system, and optionally a specific directory'
}


function RUN_NEWMAN_ON_FILE {
    if ! which newman &> /dev/null; then
        echo 'ERROR: Could not locate the newman executable file on the system'
        echo 'ERROR: Please ensure newman is installed and included on the your PATH env variable.'
        exit 1
    fi

    POSTMAN_COLLECTION_FILE=$1
    if [[ ! $POSTMAN_COLLECTION_FILE ]]; then  
        echo 'ERROR: The RUN_NEWMAN_ON_FILE function requires a postman collection to be executed.'
        echo 'ERROR: No file was specified. '
        exit 2
    fi

    if [[ ! -f $POSTMAN_COLLECTION_FILE ]]; then
        echo "ERROR: Coould not locate the file: ${POSTMAN_COLLECTION_FILE}"
        echo 'ERROR: Please, check the NEWMAN_COLLECTION variable settings.'
        exit 3
    fi

    echo "+=== Running Postman Collection: ${1}"
    if [[ $2 ]]; then
        echo "+=== Select Folder: '$2'"
        newman run "${POSTMAN_COLLECTION_FILE}" -g "${NEWMAN_ENVIRONMENT_FILE}" \
            --insecure --folder "$2" $NEWMAN_OPTS
    else
        newman run "${POSTMAN_COLLECTION_FILE}" -g "${NEWMAN_ENVIRONMENT_FILE}" \
            --insecure $NEWMAN_OPTS
    fi

}

##
# Given array of values create an option menu with the value indexes
function show_menu {
    counter=1
    for targets in $*; do
        echo "  ${counter}. ${targets}"
        counter=$(($counter + 1))
    done

    echo "  ${counter}. ALL"
    echo "  0. Cancel"
}

##
# When running in the interactive mode, choose the targets servers
# by menu
function SELECT_SERVERS {
    echo "Select one of the following options: "

    SERVERS=()
    for server in `ls resources/*.json | egrep -oh '([0-9A-Za-z_\.\-])+(.com|.net|.local)'`; do
        SERVERS+=($server)
    done

    show_menu ${SERVERS[*]}

    read -p "Choose a target server: " server_index
    server_index=$(($server_index - 1))

    if [[ $server_index -eq ${#SERVERS[*]} ]]; then
        TARGETS=$SERVERS

    elif [[ $server_index -ge 0 ]] && [[ $server_index -lt ${#SERVERS[*]} ]]; then
        TARGETS=${SERVERS[$server_index]}

    elif [[ $server_index -eq -1 ]]; then
        echo "The process was canceled it... bye"
        exit 1
    else
        echo "ERROR: Was not able to found a server that matching the given option. "
        echo "ERROR: the $server_index is not a valid option"
        exit 2
    fi
}

##
# When running in the interactive mode, choose the targets servers
# by menu
function SELECT_FOLDERS {
    echo "Select one of the following options: "

    TARGET_FOLDERS=()
    IFS=$'\n'; for folder in `egrep -oh '\"name\": \"(.*)\"' src/*.json | cut -d':' -f2`; do
        TARGET_FOLDERS+=($folder)
    done

    show_menu ${TARGET_FOLDERS[*]}

    read -p "Choose a target folder: " folder_index
    folder_index=$(($folder_index - 1))

    if [[ $folder_index -eq ${#TARGET_FOLDERS[*]} ]]; then
        FOLDER=""

    elif [[ $folder_index -ge 0 ]] && [[ $folder_index -lt ${#TARGET_FOLDERS[*]} ]]; then
        FOLDER=`echo ${TARGET_FOLDERS[$folder_index]} | sed 's/^ //g' | sed 's/\"//g'`

    elif [[ $folder_index -eq -1 ]]; then
        echo "The process was canceled it... bye"
        exit 1
    else
        echo "ERROR: Was not able to found a server that matching the given option. "
        echo "ERROR: the $server_index is not a valid option"
        exit 2
    fi
}

# ----------------------------------------
# MAIN SCRIPT START HERE
# ---------------------------------------- 
## Init variables
FOLDER=''
TARGETS=()
LIST=false
INTERACTIVE_MODE=false

[[ ! $NEWMAN_COLLECTION ]] && {
    export NEWMAN_COLLECTION="src/ProviderEndpointsAutomation.postman_collection.json"
}

# Validate the command line arguments
if [[ ! $1 ]]; then
    echo "ERROR: You must specify a server or command line argument. See the list below"
    echo

    LIST_OPTIONS
    exit 2
fi

# Parser the supported commands
while true; do
    case $1 in
        -i|--interactive)
            INTERACTIVE_MODE=true
            shift 1 
        ;;

        -l|--list)
            LIST=true
            LIST_OPTIONS
            exit 0
        ;;

        -f|--folder)
            FOLDER="$2"
            shift 2
        ;;

        -h|--help)
            show_help
            exit 0
        ;;

        --) shift; break ;;        

        *)
            [[ $1 = "" ]] && break

            TARGETS+=($1)
            shift 1
        ;;
    esac
done

##
# Running the system in interactive mode!
if [[ $INTERACTIVE_MODE = true ]]; then
    echo "Running $0 in interactive mode (--interactive)"

    echo
    SELECT_SERVERS

    echo
    SELECT_FOLDERS
fi

## Run the test for each defined servers
if [[ ${#TARGETS[*]} -ne 0 ]]; then 
    for target in ${TARGETS[*]}; do
        echo "+=====================================================+"
        echo "+=== Running target: ${target}"

        NEWMAN_ENVIRONMENT_FILE="resources/${target}.postman_environment.json"
        if [[ ! -f $NEWMAN_ENVIRONMENT_FILE ]]; then
            echo "WARN: Could not locate the file: ${NEWMAN_ENVIRONMENT_FILE}"
            echo "WARN: Skipping API Integration. Due to the environment ${target} is not supported!"
            echo "WARN: Execute ${0} -l to list the available"
            echo
            exit 0
        fi

        if [[ $FOLDER != "" ]]; then
            RUN_NEWMAN_ON_FILE "${NEWMAN_COLLECTION}" "$FOLDER"
        else
            RUN_NEWMAN_ON_FILE "${NEWMAN_COLLECTION}"
        fi
    done
else
    echo "ERROR: You must specify a server to run against. Please run '${0} -l' to list the servers."
    exit 1
fi
