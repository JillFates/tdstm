
<!-- Assets edit template -->

<div ng-show="editMode(type.name)" class="fieldsPane">
	<table class="needHeaderBorder">
		<tr id="buttonRowId">
			<td colspan="10" class="buttons">
				<div>
					<span class="button">
						<input type="button" value="Update" class="save" ng-click="toggleEditMode(type.name);updateAsset(type.name);updateHelp(type.name)" />
						<input type="button" value="Cancel" class="delete" ng-click="toggleSection(type.name);cancelAsset(type.name);">
						<input type="button" value="Reset Highlighting to Defaults" class="edit" ng-click="retriveDefaultImp(type.name)">
					</span>
				</div>
			</td>
		</tr>
		<tr id="headerRowId">
			<th>Field</th>
			<th ng-repeat="phase in phases">{{phase.label}}</th>
			<th>Help Text</th>
		</tr>
		<tr ng-repeat="field in fields[type.name]">
			<td><span>{{field.id}}</span></td>
			<td ng-repeat="phase in phases" class="{{importance[type.name][field.label]['phase'][phase.id]}}">
			<div class="pickbox" ng-repeat='datum in standardOptions' ng-click="assignData(type.name,datum,field.label,phase.id);">{{datum}}</div>
			</td>
			<td><input type="text" ng-model="help[type.name][field.label]" id="help_{{type.name}}_{{field.label}}"></td>
		</tr>
		<tr ng-repeat="field in fields['customs']">
			<td><input type="text" ng-model="field.id" id="{{type.name}}_{{field.label}}"><span class="small_text"> {{ customFieldNumber(field.label) }} </span></td>
			<td ng-repeat="phase in phases" class="{{importance[type.name][field.label]['phase'][phase.id]}}">
			<div class="pickbox" ng-repeat='datum in customOptions' ng-click="assignData(type.name,datum,field.label,phase.id);">{{datum}}</div>
			</td>
			<td><input type="text" ng-model="help[type.name][field.label]" id="help_{{type.name}}_{{field.label}}"></td>
		</tr>
	</table>
</div>