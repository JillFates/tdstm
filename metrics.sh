j#!/bin/sh
# This script is used to get the line count metrics for the project
#

java=`( find src/main/ grails-app/ -type f -name '*.java' -print0 | xargs -0 cat ) | wc -l`
echo "Java $java"
groovy=`( find src/ grails-app/ -type f -name '*.groovy' -print0 | xargs -0 cat ) | wc -l`
echo "Groovy $groovy"
gsp=`( find grails-app/ -type f -name '*.gsp' -print0 | xargs -0 cat ) | wc -l`
echo "GPS $gsp"
inttest=`( find src/integration-test/ -type f -name '*.groovy' -print0 | xargs -0 cat ) | wc -l`
echo "Integration Tests $inttest"
unittest=`( find src/test/ -type f -name '*.groovy' -print0 | xargs -0 cat ) | wc -l`
echo "Unit Tests $unittest"
angular=`( find src/main/webapp/tds/web-app/app-js/ -type f -print0 | xargs -0 cat ) | wc -l`
echo "New Angular $angular"
oldAngular=`( find src/main/webapp/components/ -type f -print0 | xargs -0 cat ) | wc -l`
echo "Old Angular $oldAngular"
# js=`( find web-app/ -type f -name '*.js' -print0 | xargs -0 cat ) | wc -l`
# echo "Javascript $js"
css=`( find src/main/webapp/ -type f -name '*.sass' -print0 | xargs -0 cat ) | wc -l`
echo "CSS/SASS $css"

let total="$java + $groovy + $gsp + $angular + $oldAngular + $css + $inttest + $unittest"
echo "Total $total"
