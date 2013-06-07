<!doctype html>
<%@page import="com.tdsops.tm.enums.domain.EntityType"%>
<html xmlns:ng="http://angularjs.org">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="projectHeader" />
<title>Asset Fields</title>
<g:javascript src="angular.js" />
<g:javascript src="controllers/fieldImportance.js" />
</head>
<body>
	<br>
	<br>
	<h1 class="assetFieldHeader1">Project Field Importance</h1>
<div ng-app="MyApp" id="ng-app" ng-controller="assetFieldImportanceCtrl">
<div data-ng-init="types=['AssetEntity','Application','Database','Files']">
	<table class="fieldTable">
		<tr ng-repeat="type in types">
			<td class="assetTd"><h1>{{type}}</h1></td>
			<td class="assetTd">
				<span ng-click="toggleSection(type)">
					<img ng-hide="showSection(type)" class="assetImage" src="${resource(dir:'images',file:'triangle_right.png')}" /> 
					<img ng-show="showSection(type)" class="assetImage" src="${resource(dir:'images',file:'triangle_down.png')}" /> 
				</span>
			</td>
			<td>
				<div ng-show="showSection(type)" class="crudTable">
				 	<div ng-include src="'showImportance.gsp'"></div>
					<div ng-include src="'editImportance.gsp'"></div>
				</div>
			</td>
		</tr>
	</table>
</div>
</div>
</body>
</html>