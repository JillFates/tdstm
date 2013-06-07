

<!-- Assets show template -->
<div>
	<table>
		<tr>
			<th>Field</th>
			<th ng-repeat="phase in phases">{{phase.label}}</th>
		</tr>
		<tr ng-repeat="field in fields">
			<td>{{field.id}}</td>
			<td ng-repeat="phase in phases">{{importance[field.id]['phase'][phase.id]}}</td>
		</tr>
	</table>
</div>