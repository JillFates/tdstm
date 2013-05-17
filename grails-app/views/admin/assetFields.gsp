<!doctype html>
<html xmlns:ng="http://angularjs.org">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="projectHeader" />
<title>Asset Fields</title>
<g:javascript src="angular.js" />
<g:javascript src="admin.js" />

</head>
<body>
<br><br>
<h1 class="assetFieldHeader1">Project Field Important</h1>
<div ng-app id="ng-app" ng-controller="MyCtrl">
<h2 class="assetFieldHeader2">
	Assets <span id="showId" class="assetFieldSpan" ng-click="showAssets('show', 'AssetEntity')">|></span>
	<span id="hideId" class="assetFieldSpan" ng-click="showAssets('hide','AssetEntity')" style="display: none;">V</span>
</h2>
	<div class="assetDivId" style="display: none;">
			<table class="assetFieldTable">
			<tr>
			<th>Fields</th>
			<th>Discovery</th>
			<th>Validated</th>
			<th>DependencyReview</th>
			<th>DependencyScan</th>
			<th>BundleReady</th>
			</tr>
            <tr  ng-repeat="field in fields" >
             <td>{{field.name}}</td>
                <td ng-repeat="item in field.validations" id="td_AE_{{field.property}}_{{item.name}}" class="tdClass_{{field.property}}_{{item.name}}">
                	<span id="AE_{{field.property}}_{{item.name}}" class="radioEdit">&nbsp;</span>
	                <span  ng-repeat="imp in item.importance" class="radioShow" style="display: none;">
	                    <input type="radio" ng-model="newObject['AE_'+field.property+'_'+item.name]"  ng-change="radioChange(imp.name,field.property,item.name)" name="{{field.name}}_{{item.name}}" value="{{imp.name}} " id="AssetEntity_{{field.property}}_{{item.name}}-{{imp.name}}" />{{imp.symbol}}
	                </span>
                </td>
            </tr>
        </table>
			<input id="update" class="assetFieldbutton" type="button" value="Update" ng-click="updateAssetForm('AssetEntity')" style="display: none" />
		<tds:hasPermission permission='ProjectEditView'>
			<input id="edit" class="assetFieldbutton" type="button" value="Edit" ng-click="editAssets()" />
		</tds:hasPermission>
		<br>
         <input type="hidden" id="jsonId" value="{{newObject}}"/>
	</div>
</div>
</body>
</html>