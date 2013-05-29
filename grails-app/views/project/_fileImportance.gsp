<%@page import="com.tdsops.tm.enums.domain.ValidationType"%>
<div class="assetDivId_${assetType.value} field_list">
<div>
	<table class="list">
		<tr>
			<th>Fields</th>
			<th>Discovery</th>
			<th>Validated</th>
			<th>DependencyReview</th>
			<th>DependencyScan</th>
			<th>BundleReady</th>
		</tr>
		<tr>
		<td id="imageId_${assetType.value}" style="display: none;"><img src="${resource(dir:'images',file:'processing.gif')}" /></td>
		</tr>
		<tr ng-repeat="field in ${assetType.value.value}">
			<td>{{field.name}}</td>
			<g:each in="${ValidationType.list}" var="validation">
				<td id="td_${assetType.value.value}_${validation}_{{field.property}}" class="tdClass_{{field.property}}_${validation}_${assetType.value}">
					<span id="${assetType.value}_${validation}_{{field.property}}" class="radioEdit_${assetType.value}">&nbsp;</span>
					<span ng-repeat="imp in field.importance" class="radioShow_${assetType.value}" style="display: none;">
					<input type="radio" ng-model="F_${validation}[field.property]" ng-change="radioChange(imp.name,field.property,'${validation}','${assetType.value}')"
						name="${assetType.value}_${validation}_{{field.name}}" value="{{imp.name}} " id="${assetType.value}_${validation}_{{field.property}}-{{imp.name}}" />{{imp.sy}}
					</span>
				</td>
			</g:each>		
		</tr>
	</table>
</div>
<div class="buttons">
	<span class="button">
    	<input class="update_${assetType.value} save" type="button" value="Update" ng-click="updateAssetForm('${assetType.value}')" style="display: none" />
    	<input class="update_${assetType.value} delete" type="button" value="Cancel" ng-click="cancelAssetForm('${assetType.value}')" style="display: none" />
		<tds:hasPermission permission='ProjectEditView'>
			<input id="edit_${assetType.value}" class="edit" type="button" value="Edit" ng-click="editAssets('${assetType.value}')" />
		</tds:hasPermission>
	</span>
</div>
	<input type="hidden" id="${assetType.value}_disJsonId" value="{{F_Discovery}}" />
	<input type="hidden" id="${assetType.value}_vlJsonId" value="{{F_Validated}}" />
	<input type="hidden" id="${assetType.value}_drJsonId" value="{{F_DependencyReview}}" />
	<input type="hidden" id="${assetType.value}_dsJsonId" value="{{F_DependencyScan}}" />
	<input type="hidden" id="${assetType.value}_brJsonId" value="{{F_BundleReady}}" />
</div>
