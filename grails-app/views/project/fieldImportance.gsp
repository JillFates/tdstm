<!doctype html>
<%@page import="com.tdsops.tm.enums.domain.EntityType"%>
<html xmlns:ng="http://angularjs.org">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="projectHeader" />
<title>Project Field Settings</title>
<g:javascript src="angular.js" />
<g:javascript src="controllers/fieldImportance.js" />
</head>
<body>
	<h1 class="assetFieldHeader1">Project Field Settings</h1>
	<div ng-app="MyApp" id="ng-app" ng-controller="assetFieldImportanceCtrl">
		<div class="legend" >
				<h1 class="assetImage" ng-click="toggleLegend()">Legend
					<img ng-hide="showLegend()" src="${resource(dir:'images',file:'triangle_right.png')}" /> 
					<img ng-show="showLegend()" src="${resource(dir:'images',file:'triangle_down.png')}" /> 
				</h1>
			<table class="legendTable" ng-show="showLegend()">
				<tr ng-repeat="note in notes">
					<td>{{note.imp}}</td>
					<td class="{{note.id}}">{{note.field}}</td>
					<td class="{{note.id}}">{{note.type}}</td>
				</tr>
			</table>
		</div>
		<div data-ng-init="types=[{'id':'Application','name':'Application'},{'id':'AssetEntity','name':'AssetEntity'},{'id':'Database','name':'Database'},{'id':'Storage','name':'Files'}]">
	<table class="fieldTable">
		<tr ng-repeat="type in types">
			<td class="assetTd" nowrap="nowrap"><h1 ng-click="toggleSection(type.name)" class="assetImage"><span ng-bind="type.id"></span>
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
<input type="hidden" id="customfieldShown" name="customfieldShown" value="${Project.CUSTOM_FIELD_COUNT}" />
</div>
<script type="text/javascript">
currentMenuId = "#projectMenu";
$("#projectMenuId a").css('background-color','#003366')
$(".legend").css('margin-left',$(window).width()-375+"px")
</script>
</body>
</html>