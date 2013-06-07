
<!-- Assets edit template -->

<div ng-controller="editMain">
	<table style="padding: 4px;">
		<tr>
			<th>Field</th>
			<th ng-repeat="phase in phases">{{phase.label}}</th>
		</tr>
		<tr ng-repeat="field in fields">
			<td>{{field.id}}</td>
			<td ng-repeat="phase in phases" width="180">
				<importance-div data="data"></importance-div></td>
		</tr>
	</table>
</div>