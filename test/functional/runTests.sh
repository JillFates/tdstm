#!/bin/bash
result=0
#To Install uncomment the next 2 lines
# npm install
# node_modules/protractor/bin/webdriver-manager update

export BASE_URL=http://localhost:8080
# export BASE_URL=https://dev01.tm.tdsops.net
# export BASE_URL=http://tmdev.tdsops.com
# export BASE_URL=https://stage01.tm.tdsops.net
#Add tds credentials 
export USER_NAME=
export PASSWORD=

export BROWSER_NAME=chrome
BROWSER_NAME=${1:-$BROWSER_NAME}

echo "Running on  $BROWSER_NAME"
#Configure DOWNLOAD_PATH for importExport suite
#export DOWNLOAD_PATH=

#Uncomment the suite you want to run
declare -a suites=(
  # "all  "
  # "test"  
  # "test1"
   # "test2" 
  # "projects"
  # "menu"
  # "regression" 
  # "tasks" 
  "dashboards" 
  # "planning"
  # "reports" 
  # "assets" 
  # "importExport"
  # "admin"
  )

for i in "${suites[@]}"

do
    echo "running suite $i..."

    if [[ "$BROWSER_NAME" == phantomjs ]];
      then
        node_modules/protractor/bin/protractor support/phantom.config.js --suite $i
      else
        node_modules/protractor/bin/protractor support/protractor.config.js --suite $i
        # node_modules/protractor/bin/protractor support/protractor.config.js
    fi
    tempResult=$?
    [ $tempResult -ne 0 ] && result=$tempResult && fail="$fail \n $i"
  echo -e "suite $i run status: $tempResult \n"
done

[ $result -ne 0 ] && echo -e "\n global result failures: $fail \n exit status $result"  || echo "global result success"

exit $result