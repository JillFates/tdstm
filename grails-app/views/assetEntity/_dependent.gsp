<td valign="top" >
	<div style="width: auto;">
		<span style="float: left;"><h1>Supports:</h1></span>
		<span style="float: right;"><input type='button' value='Add' onclick="addAssetDependency('support','edit')"></span>
		<br/>
		<table style="width: 100%;">
			<thead>
				<tr>
					<th>Frequency</th>
					<th>Class</th>
					<th>Name</th>
					<th>Type</th>
					<th>Status</th>
					<th>&nbsp;</th>
				</tr>
			</thead>
			<tbody id="editSupportsList">
				<g:each in="${supportAssets}" var="support" status="i">
					<g:set var="type" value="${support?.asset?.assetType}"></g:set>
					<tr id='row_s_${i}'>
						<td><g:select name="dataFlowFreq_support_${i}" value="${support.dataFlowFreq}" from="${support.constraints.dataFlowFreq.inList}" /></td>
						<td>
							<g:select name="entity_support_${i}" id="entity_support_${i}" from="['Server','Application','Database','Storage','Network']" 
								onChange="updateAssetsList(this.name)" 
								value="${type=='Files' ? 'Storage' : (nonNetworkTypes.contains(type) ? type : 'Network')}">
							</g:select>
						</td>
						<td id="assetListSupportTdId_${i}">
							<select name="asset_support_${i}" class="assetSelect" onmousedown="updateAssetsList(this.name, '${type}', '${support?.asset?.id}')">
								<option value="${support?.asset?.id}" selected>${support?.asset.assetName}</option>
							</select>
						</td>
						<td><g:select name="dtype_support_${i}" value="${support.type}" from="${dependencyType.value}" optionValue="value" /></td>
						<td><g:select name="status_support_${i}" value="${support.status}" from="${dependencyStatus.value}" optionValue="value" /></td>
						<td><a href="javascript:deleteRow('row_s_${i}')"><span class='clear_filter'>X</span></a></td>
					</tr>
				</g:each>
			</tbody>
		</table>
	</div>
</td>
<td valign="top">
	<div style="width: auto;">
		<span style="float: left;"><h1>Is dependent on:</h1></span>
		<span style="float: right;"><input type='button' value='Add' onclick="addAssetDependency('dependent', 'edit')"></span>
		<br/>
		<table style="width: 100%;">
			<thead>
				<tr>
					<th>Frequency</th>
					<th>Class</th>
					<th>Name</th>
					<th>Type</th>
					<th>Status</th>
					<th>&nbsp;</th>
				</tr>
			</thead>
			<tbody id="editDependentsList">
			<g:each in="${dependentAssets}" var="dependent" status="i">
			   <g:set var="type" value="${dependent?.dependent?.assetType}"></g:set>
				<tr id='row_d_${i}'>
					<td><g:select name="dataFlowFreq_dependent_${i}" value="${dependent.dataFlowFreq}" from="${dependent.constraints.dataFlowFreq.inList}" /></td>
					<td>
						<g:select name="entity_dependent_${i}" id="entity_dependent_${i}" from="['Server','Application','Database','Storage','Network']" 
							onchange="updateAssetsList(this.name)" 
							value="${type== 'Files' ? 'Storage' : (nonNetworkTypes.contains(type) ? type : 'Network')}">
						</g:select>
					</td>
					<td id="assetListDependentTdId_${i}">
						<select name="asset_dependent_${i}" class="assetSelect" onmousedown="updateAssetsList(this.name, '${type}', '${dependent?.dependent?.id}')">
							<option value="${dependent?.dependent?.id}" selected>${dependent?.dependent.assetName}</option>
						</select>
					</td>
					<td><g:select name="dtype_dependent_${i}" value="${dependent.type}" from="${dependencyType.value}" optionValue="value"/></td>
					<td><g:select name="status_dependent_${i}" value="${dependent.status}" from="${dependencyStatus.value}" optionValue="value"/></td>
					<td><a href="javascript:deleteRow('row_d_${i}')"><span class='clear_filter'>X</span></a></td>
				</tr>
			</g:each>
			</tbody>
		</table>
	</div>
</td>