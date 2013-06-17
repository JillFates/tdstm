

<!-- Assets show template -->
<div ng-hide="editMode(type.name)">
	<table>
		<tr>
			<td colspan="10" class="buttons">
				<div>
					<span class="button">
						<tds:hasPermission permission='ProjectEditView'>
							<input type="button" value="Edit" class="edit" ng-click="toggleEditMode(type.name)" />
						</tds:hasPermission>
					</span>
				</div>
			</td>
		</tr>
		<tr>
			<th>Field</th>
			<th ng-repeat="phase in phases">{{phase.label}}</th>
		</tr>
		<tr ng-repeat="field in fields[type.name]">
			<td>{{field.id}}</td>
			<td ng-repeat="phase in phases" class="{{importance[type.name][field.label]['phase'][phase.id]}}">{{importance[type.name][field.label]['phase'][phase.id]}}</td>
		</tr>
	</table>
</div>