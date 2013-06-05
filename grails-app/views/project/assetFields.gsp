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
	<h1 class="assetFieldHeader1">Project Field Importantance</h1>
<div ng-app="MyApp" id="ng-app" ng-controller="assetFieldImportanceCtrl">
	<table class="fieldTable">
		<tr>
			<td><h1>AssetEntity</h1></td>
			<td>
			<span ng-click="toggleSection('AssetEntity')">
				<img ng-hide="showSection('AssetEntity')" class="assetImage" src="${resource(dir:'images',file:'triangle_right.png')}" /> 
				<img ng-show="showSection('AssetEntity')" class="assetImage" src="${resource(dir:'images',file:'triangle_down.png')}" /> 
			</span>
			</td>
		</tr>
	</table>
	
<div ng-show="showSection('AssetEntity')" class="assetImage crudTable">

<!-- show field Importance div which will be repeated for all assetTypes and moved to a template-->
		<div ng-hide="editMode('AssetEntity')" >
			<table>
				<tr>
					<th>Field</th>
					<th ng-repeat="phase in phases">{{phase.label}}</th>
				</tr>
				<tr ng-repeat="field in fields">
					<td>{{field.id}}</td>
					<td ng-repeat="phase in phases">{{importance[field['id']]['phase'][phase['id']]}}</td>
				</tr>
				</table>
			<button ng-click="toggleEditMode('AssetEntity')">Edit</button>
	</div>
	
<!-- edit field Importance div which will be repeated for all assetTypes and moved to a template-->	
		<div ng-show="editMode('AssetEntity')" >
				<table style="padding:4px;">
					<tr>
						<th>Field</th>
						<th ng-repeat="phase in phases">{{phase.label}}</th>
					</tr>
					<tr ng-repeat="field in fields">
						<td>{{field.id}}</td>
						<td ng-repeat="phase in phases" width="180">
                            <span edit-importance>REPLACED</span>
                       </td>
					</tr>
				</table>
				<button ng-click="toggleEditMode('AssetEntity')">Save</button>
		</div>

</div>
</div>
</body>
</html>