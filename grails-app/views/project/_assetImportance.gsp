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
		<tr ng-repeat="field in fields">
			<td>{{field.name}}</td>
			<td ng-repeat="item in field.validations"
				id="td_AE_{{field.property}}_{{item.name}}"
				class="tdClass_{{field.property}}_{{item.name}}"><span
				id="AE_{{field.property}}_{{item.name}}" class="radioEdit">&nbsp;</span>
				<span ng-repeat="imp in item.imp" class="radioShow"
				style="display: none;"> <input type="radio"
					ng-model="newObject['AE_'+field.property+'_'+item.name]"
					ng-change="radioChange(imp.name,field.property,item.name)"
					name="{{field.name}}_{{item.name}}" value="{{imp.name}} "
					id="AssetEntity_{{field.property}}_{{item.name}}-{{imp.name}}" />{{imp.sy}}
			</span></td>
		</tr>
	</table>
	<input id="update" class="assetFieldbutton" type="button"
		value="Update" ng-click="updateAssetForm('AssetEntity')"
		style="display: none" />
	<tds:hasPermission permission='ProjectEditView'>
		<input id="edit" class="assetFieldbutton" type="button" value="Edit"
			ng-click="editAssets()" />
	</tds:hasPermission>
	<br> <input type="hidden" id="jsonId" value="{{newObject}}" />
</div>