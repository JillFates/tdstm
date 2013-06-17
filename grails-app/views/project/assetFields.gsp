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
		<div class="stylingNote" >
			<h2 class="depSpin">Styling notes:</h2>
			<br>
			<table>
				<tr ng-repeat="note in notes">
					<td class="{{note.id}}">{{note.field}}</td>
					<td>{{note.type}}</td>
					<td>{{note.imp}}</td>
				</tr>
				<tr>
					<td>.....</td>
				</tr>
			</table>
		</div>
		<div data-ng-init="types=[{'id':'AssetEntity','name':'AssetEntity'},{'id':'Application','name':'Application'},{'id':'Database','name':'Database'},{'id':'Storage','name':'Files'}]">
	<table class="fieldTable">
		<tr ng-repeat="type in types">
			<td class="assetTd"><h1 ng-click="toggleSection(type.name)" class="assetImage">{{type.id}}
					<img ng-hide="showSection(type.name)"  class="dgImages" src="${resource(dir:'images',file:'triangle_right.png')}" /> 
					<img ng-show="showSection(type.name)"  src="${resource(dir:'images',file:'triangle_down.png')}" /> </h1>
			</td>
			<td>
				<div ng-show="showSection(type.name)" class="crudTable">
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