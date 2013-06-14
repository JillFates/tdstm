
<!-- Assets edit template -->

<div ng-show="editMode(type)">
	<table>
		<tr>
			<td colspan="10" class="buttons">
				<div>
					<span class="button">
						<input type="button" value="Update" class="save" ng-click="toggleEditMode(type);updateAsset(type);" />
						<input type="button" value="Cancel" class="delete" ng-click="toggleSection(type)" />
					</span>
				</div>
			</td>
		</tr>
		<tr>
			<th>Field</th>
			<th ng-repeat="phase in phases">{{phase.label}}</th>
		</tr>
		<tr ng-repeat="field in fields[type]">
			<td>{{field.id}}</td>
			<td ng-repeat="phase in phases" class="{{importance[type][field.label]['phase'][phase.id]}}">
			<div class="pickbox" ng-repeat='datum in data' ng-click="assignData(type,datum,field.label,phase.id);">{{datum}}</div>
		</td>
		</tr>
	</table>
</div>