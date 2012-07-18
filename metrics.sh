#!/bin/sh
# This script is used to get the line count metrics for the project
#

java=`( find src/ grails-app/ -type f -name '*.java' -print0 | xargs -0 cat ) | wc -l`
echo "Java $java"
groovy=`( find src/ grails-app/ -type f -name '*.groovy' -print0 | xargs -0 cat ) | wc -l`
echo "Groovy $groovy"
gsp=`( find grails-app/ -type f -name '*.gsp' -print0 | xargs -0 cat ) | wc -l`
echo "GPS $gsp"
js=`( find grails-app/ web-app/ -type f -name '*.js' -print0 | xargs -0 cat ) | wc -l`
echo "Javascript $js"
css=`( find grails-app/ web-app/ -type f -name '*.css' -print0 | xargs -0 cat ) | wc -l`
echo "CSS $css"

let total="$java + $groovy + $gsp + $js + $css"
echo "Total $total"
