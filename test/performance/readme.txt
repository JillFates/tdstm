What to do to run Performance Test
Tool: gatling
Version: 2.1.3
Getting started: https://github.com/excilys/gatling/wiki/Getting-Started

In order to run the test:
1. Download Gatling 
2. Configure gatling to look for the user-files on this folder 
3. Add this environment variable
   $ export BASE_URL=http://localhost:8080
3. Add user credentials to use on the test to this file
   trunk/test/performance/user-files/data/user_tsd.csv
4. Configure the number of users, ramp time for each scenario on this file
   trunk/test/performance/gatling-charts-highcharts-1.5.5/user-files/simulations/assetLists/GetAssetsLists.scala
5. To run the simulation run this file under gatling folder ./bin/gatling.sh
    if more than one simulation is displayed, choose:
     * assetLists.GetAssetList -> Go through Application, database, server and storage list page. (User configuration need to be set up on user-files/simulations/AssetList/GetAssetsLists.scala file)
     * GenerateTasks -> Generate task for the existing task created on Marketing Demo project. 
6. Once the simulation is complete, it save a html report and log in the console the link to the report.