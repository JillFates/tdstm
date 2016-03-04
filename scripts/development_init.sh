#!/bin/bash
# This script is used to run various initialization on environment

# Absolute path to this script. /home/user/bin/foo.sh
SCRIPT=$(readlink -f $0)
# Absolute path this script is in. /home/user/bin
SCRIPT_PATH=`dirname $SCRIPT`

# Create User Accounts
sudo mysql tdstm < $SCRIPT_PATH/development_users.sql 
