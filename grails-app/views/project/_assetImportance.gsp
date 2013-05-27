<%@page import="com.tdsops.tm.enums.domain.ValidationType"%>
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
			<g:each in="${ValidationType.list}" var="validation">
				<td id="td_${validation}_{{field.property}}" class="tdClass_{{field.property}}_${validation}">
					<span id="${validation}_{{field.property}}" class="radioEdit">&nbsp;</span>
					<span ng-repeat="imp in field.importance" class="radioShow" style="display: none;">
					<input type="radio" ng-model="${validation}[field.property]" ng-change="radioChange(imp.name,field.property,'${validation}')"
						name="${validation}_{{field.name}}" value="{{imp.name}} " id="AssetEntity_${validation}_{{field.property}}-{{imp.name}}" />{{imp.sy}}
					</span>
				</td>
			</g:each>		
		</tr>
	</table>
	<input id="update" class="assetFieldbutton" type="button" value="Update" ng-click="updateAssetForm('AssetEntity')" style="display: none" />
	<tds:hasPermission permission='ProjectEditView'>
		<input id="edit" class="assetFieldbutton" type="button" value="Edit" ng-click="editAssets()" />
	</tds:hasPermission>
	<br> 
	<input type="hidden" id="disJsonId" value="{{Discovery}}" />
	<input type="hidden" id="vlJsonId" value="{{Validated}}" />
	<input type="hidden" id="drJsonId" value="{{DependencyReview}}" />
	<input type="hidden" id="dsJsonId" value="{{DependencyScan}}" />
	<input type="hidden" id="brJsonId" value="{{BundleReady}}" />
</div>