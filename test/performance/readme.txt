What to do to run Performance Test
Tool: gatling 
Getting started: https://github.com/excilys/gatling/wiki/Getting-Started

In order to run the test:
1. Configure user credentials on this file
  trunk/test/performance/gatling-charts-highcharts-1.5.5/user-files/data/user_tsd.csv
2. Configure the number of users, ramp time for each scenario on this file
  trunk/test/performance/gatling-charts-highcharts-1.5.5/user-files/simulations/assetLists/GetAssetsLists.scala
3. To run the simulation run this trunk/test/performance/gatling-charts-highcharts-1.5.5/bin/gatling.sh
  if more than one simulation is displayed, choose assetLists.GetAssetList simulation and start it.
4. Once the simulation is complete, it save a html report and log in the console the link to the report.