

<!-- Assets show template -->
<div ng-hide="editMode(type.name)" class="fieldsPane">
	<table>
		<tr id="buttonRowId">
			<td colspan="10" class="buttons">
				<div>
					<tds:hasPermission permission='EditProjectFieldSettings'>
						<span class="button">
							<input type="button" value="Edit" class="edit" ng-click="toggleEditMode(type.name)" />
						</span>
					</tds:hasPermission>
				</div>
			</td>
		</tr>
		<div id="fieldsRowContainerId">
			<tr id="headerRowId">
				<th>Field</th>
				<th ng-repeat="phase in phases">{{phase.label}}</th>
				<th>Help Text</th>
			</tr>
			<tr ng-repeat="field in fields[type.name]">
				<td>{{field.id}}</td>
				<td ng-repeat="phase in phases" class="{{importance[type.name][field.label]['phase'][phase.id]}}">{{importance[type.name][field.label]['phase'][phase.id]}}</td>
				<td>{{help[type.name][field.label]}}</td>
			</tr>
			<tr ng-repeat="field in fields['customs']">
				<td>{{field.id}}<span class="small_text">{{ customLabelNumber(field.id, field.label)}}</span></td>
				<td ng-repeat="phase in phases" class="{{importance[type.name][field.label]['phase'][phase.id]}}">{{importance[type.name][field.label]['phase'][phase.id]}}</td>
				<td>{{help[type.name][field.label]}}</td>
			</tr>
		</div>
	</table>
</div>