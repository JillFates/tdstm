

<!-- Assets show template -->
<div ng-hide="editMode(type)">
	<table class="list">
		<tr>
			<th>Field</th>
			<th ng-repeat="phase in phases">{{phase.label}}</th>
		</tr>
		<tr ng-repeat="field in fields">
			<td>{{field.id}}</td>
			<td ng-repeat="phase in phases">{{importance[field.id]['phase'][phase.id]}}</td>
		</tr>
	</table>
	<button ng-click="toggleEditMode(type)">Edit</button></td>
</div>