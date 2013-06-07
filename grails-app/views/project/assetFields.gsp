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
	<table class="fieldTable" data-ng-init="types=['AssetEntity','Application','Database','Files']">
		<tr ng-repeat="type in types">
			<td><h1>{{type}}</h1></td>
			<td>
			<span ng-click="toggleSection(type)">
				<img ng-hide="showSection(type)" class="assetImage" src="${resource(dir:'images',file:'triangle_right.png')}" /> 
				<img ng-show="showSection(type)" class="assetImage" src="${resource(dir:'images',file:'triangle_down.png')}" /> 
			</span>
			</td>
		</tr>
	</table>
<!-- need to iterate show and edit divs for all assetTypes-->
	<!-- show Importance div -->
	<div ng-show="showSection('AssetEntity')" ng-hide="editMode('AssetEntity')">
		<span assetentityshow></span>
		<button ng-click="toggleEditMode('AssetEntity')">Edit</button>
	</div>	
	
	<!-- edit Importance div -->
	<div ng-show="editMode('AssetEntity')">
	<span assetentityedit></span>
		<button ng-click="toggleEditMode('AssetEntity')">Save</button>
	</div>
</div>
</body>
</html>