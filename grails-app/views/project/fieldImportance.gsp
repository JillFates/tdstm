<!doctype html>
<%@page import="com.tdsops.tm.enums.domain.EntityType"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="projectHeader" />
<title>Project Field Settings</title>
<g:javascript src="angular/angular.min.js" />
<%--TODO:the following bootstrap contains few extra methods which is usefull for angular bootstrap,once testing entire application with this js I will replace it with bootstrap.js--%>
<g:javascript src="ui-bootstrap-tpls-0.10.0.js" />
<g:javascript src="controllers/fieldImportance.js" />
<link rel="stylesheet" href="${resource(dir:'css',file:'bootstrap-tabs.css')}" type="text/css"/>
</head>
<body>
	<h1 class="assetFieldHeader1">Project Field Settings</h1><br>
<div ng-app="MyApp" id="ng-app" ng-controller="assetFieldImportanceCtrl">
	<div style="margin-left:20px;">
		<h2>Custom Fields Shown:
		<g:select ng-model="customShown" name="customFieldSelect" from="${Project.constraints.customFieldsShown.inList}" 
                         value="customShown" ng-change="updateCustomFieldsShown();" /></h2>
    </div>
    <div>
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
		
		<tabset>
   			 <tab ng-repeat="type in types" ng-click="toggleSection(type.name)" heading="{{type.id}}">
				<table class="fieldTable">
						<tr ng-show="showSection(type.name)" class="crudTable">
							<td>
								<div>
								 	<div ng-include src="'showImportance.gsp'"></div>
									<div ng-include src="'editImportance.gsp'"></div>
								</div>
							</td>
						</tr>
				</table>
			</tab>
  		</tabset>
		
</div>
<input type="hidden" id="customfieldShown" name="customfieldShown" value="${Project.CUSTOM_FIELD_COUNT}" />
<input type="hidden" id="projectCustomShown" name="projectCustomShown" value="${project.customFieldsShown}" />
</div>
</div>
<script type="text/javascript">
currentMenuId = "#projectMenu";
$("#projectMenuId a").css('background-color','#003366')
$(".legend").css('margin-left',$(window).width()-375+"px")
</script>
</body>
</html>