
<!-- Assets edit template -->

<div ng-show="editMode(type)">
	<table class="list">
		<tr>
			<th>Field</th>
			<th ng-repeat="phase in phases">{{phase.label}}</th>
		</tr>
		<tr ng-repeat="field in fields">
			<td>{{field.id}}</td>
			<td ng-repeat="phase in phases" width="180" >
			<div class='pickbox' ng-repeat='datum in data' ng-style="myStyle" ng-click="myStyle={'background-color':'red'}; assignData(datum,field.label,phase.id);">{{datum}}</div>
		</td>
		</tr>
	</table>
	<button ng-click="toggleEditMode(type);updateAsset(type);">Save</button>
</div>