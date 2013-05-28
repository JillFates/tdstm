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
<br><br>
<h1 class="assetFieldHeader1">Project Field Important</h1>
<div ng-app id="ng-app" ng-controller="assetFieldImportanceCtrl">
<div id="stylingNoteId">
<b>Styling Notes:</b>
<br>
<table>
	<tr>
		<td class="C">Name</td>
		<td>ServerX05</td>
		<td>Critical</td>
	</tr>
	<tr>
		<td>Type</td>
		<td>Server</td>
		<td>Valuable</td>
	</tr>
	<tr>
		<td>Manufacturer</td>
		<td>HP</td>
		<td>Ignore</td>
	</tr>
	<tr>
		<td colspan="3">......</td>
	</tr>
	<g:each in="['Critical','Valuable','Ignore']" var="imp">
		<tr>
			<td ${imp=='Critical'? 'class="C"' : ''}>
				<input type="radio" ${imp=='Critical'? 'checked="checked"' : 'onclick="this.checked = false;"'}/>
				<input type="radio" ${imp=='Valuable'? 'checked="checked"' : 'onclick="this.checked = false;"'}/>
				<input type="radio" ${imp=='Ignore'? 'checked="checked"' : 'onclick="this.checked = false;"'}/>
			</td>
			<td>&nbsp;</td>
			<td>${imp}</td>
		</tr>
	</g:each>
</table>
</div>
<table class="fieldTable">
<g:each in="${EntityType.listAsMap}" var="entityType">
	<tr>
		<td><h1>${entityType.value}</h1></td>
		<td><h1>
			<span id="showId_${entityType.value}" class="Arrowcursor" ng-click="showAssets('show', '${entityType.value}')">|></span>
			<span id="hideId_${entityType.value}" class="Arrowcursor" ng-click="showAssets('hide','${entityType.value}')" style="display: none;">V</span></h1>
		</td>
	</tr>
	<tr>
		<td></td>
		<td><g:render template="${entityType.key}Importance" model="['assetType':entityType]"></g:render></td>
	<tr>

</g:each>
</table>
</div>
</body>
</html>