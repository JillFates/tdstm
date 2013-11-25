<script type="text/javascript">
$(document).ready(function() {
	$(".depComDiv").dialog({ autoOpen: false})
})
</script>
<td valign="top" >
	<div style="width: auto;" >
		<span style="float: left;"><h1>Supports:&nbsp;&nbsp;</h1></span>
		<span><input type='button'  class="addDepButton" value='Add' onclick="addAssetDependency('support','${whom}')"></span>
		<br/>
		<table style="width: 100%;">
			<thead>
				<tr>
					<th>Frequency</th>
					<th>Class</th>
					<th>Name</th>
					<th>Bundle</th>
					<th>Type</th>
					<th>Status</th>
					<th>&nbsp;</th>
				</tr>
			</thead>
			<tbody id="${whom}SupportsList">
				<g:each in="${supportAssets}" var="support" status="i">
					<g:set var="type" value="${support?.asset?.assetType}"></g:set>
					<tr id='row_s_${i}_${support.id}'>
						<td><g:select name="dataFlowFreq_support_${support.id}" value="${support.dataFlowFreq}" from="${support.constraints.dataFlowFreq.inList}" /></td>
						<td>
							<g:select name="entity_support_${support.id}" id="entity_support_${support.id}" from="['Server','Application','Database','Storage','Other']" 
								onChange="updateAssetsList(this.name)" 
								value="${type== 'Files' ? 'Storage' : (nonNetworkTypes.contains(type) ? type : 'Other')}" />
						</td>
						<td id="assetListSupportTdId_${i}"  class='combo-td'>
							<select name="asset_support_${support.id}" class="assetSelect" onmousedown="updateAssetsList(this.name, '${type}', '${support?.asset?.id}')" 
								 onchange="changeMovebundle(this.value,this.name,'${assetEntity?.moveBundle?.id}')">
								<option value="${support?.asset?.id}" selected>${support?.asset.assetName}</option>
							</select>
						</td>
						<td>
							<g:set var="supportBundle" value="${support?.asset?.moveBundle}"></g:set>
						<%--Used to show bundle colors based on bundleConflicts--%>
							<g:if test="${supportBundle!=assetEntity.moveBundle && support.status == 'Validated' }" >
								<g:select from="${moveBundleList}" class="depBundle" name="moveBundle_support_${support.id}" value="${supportBundle?.id}" 
										optionKey="id" optionValue="name" style="background-color: red" onchange="changeMoveBundleColor(this.name,this.value,'${assetEntity?.moveBundle?.id}','')"/>
							</g:if>
							<g:elseif test="${supportBundle!=assetEntity.moveBundle }" >
								<g:select from="${moveBundleList}" name="moveBundle_support_${support.id}" value="${supportBundle?.id}" 
									optionKey="id" optionValue="name" onchange="changeMoveBundleColor(this.name,this.value,'${assetEntity?.moveBundle?.id}','')" 
									class="dep-${(support.status != 'Questioned' && support.status != 'Validated') ? 'Unknown' : support.status } depBundle"/>
							</g:elseif>
							<g:else>
								<g:select from="${moveBundleList}" class="depBundle" name="moveBundle_support_${support.id}" value="${supportBundle?.id}" 
									optionKey="id" optionValue="name" onchange="changeMoveBundleColor(this.name,this.value,'${assetEntity?.moveBundle?.id}','')"
									class="dep-${(support.status != 'Questioned' && support.status != 'Validated') ? 'Unknown' : support.status } depBundle"/>
							</g:else>
						</td>
						<td nowrap="nowrap"><g:select name="dtype_support_${support.id}" value="${support.type}" from="${dependencyType.value}" optionValue="value" />
						  <g:render template="../assetEntity/dependencyComment" model= "[dependency:support,type:'support',forWhom:'edit']"></g:render>
						</td>
						<td><g:select name="status_support_${support.id}" value="${support.status}" from="${dependencyStatus.value}" 
							optionValue="value" onchange="changeMoveBundleColor(this.name,'','${assetEntity.moveBundle?.id}',this.value)"/></td>
						<td><a href="javascript:deleteRow('row_s_${i}_${support.id}', '${whom}_supportAddedId')"><span class='clear_filter'>X</span></a></td>
					</tr>
				</g:each>
			</tbody>
		</table>
	</div>
</td>
<td valign="top">
	<div style="width: auto;">
		<span style="float: left;"><h1>Is dependent on:&nbsp;&nbsp;</h1></span>
		<span><input type='button' class="addDepButton" value='Add' onclick="addAssetDependency('dependent', '${whom}')"></span>
		<br/>
		<table style="width: 100%;">
			<thead>
				<tr>
					<th>Frequency</th>
					<th>Class</th>
					<th>Name</th>
					<th>Bundle</th>
					<th>Type</th>
					<th>Status</th>
					<th>&nbsp;</th>
				</tr>
			</thead>
			<tbody id="${whom}DependentsList">
			<g:each in="${dependentAssets}" var="dependent" status="i">
			   <g:set var="type" value="${dependent?.dependent?.assetType}"></g:set>
				<tr id='row_d_${i}_${dependent.id}'>
					<td><g:select name="dataFlowFreq_dependent_${dependent.id}" value="${dependent.dataFlowFreq}" from="${dependent.constraints.dataFlowFreq.inList}" /></td>
					<td>
						<g:select name="entity_dependent_${dependent.id}" id="entity_dependent_${i}" from="['Server','Application','Database','Storage','Other']"
							value="${type== 'Files' ? 'Storage' : (nonNetworkTypes.contains(type) ? type : 'Other')}"  
							onchange="updateAssetsList(this.name)" />
					</td>
					<td id="assetListDependentTdId_${i}"  class='combo-td'>
						<select name="asset_dependent_${dependent.id}" class="assetSelect" onmousedown="updateAssetsList(this.name, '${type}', '${dependent?.dependent?.id}')" 
							onchange="changeMovebundle(this.value,this.name,'${assetEntity?.moveBundle?.id}')">
							<option value="${dependent?.dependent?.id}" selected>${dependent?.dependent.assetName}</option>
						</select>
					</td>
					<td>
						<g:set var="depBundle" value="${dependent?.dependent?.moveBundle}"></g:set>
						<%--Used to show bundle colors based on bundleConflicts--%>
						<g:if test="${depBundle!=assetEntity.moveBundle && dependent.status == 'Validated' }" >
							<g:select from="${moveBundleList}" class="depBundle" name="moveBundle_dependent_${dependent.id}" value="${depBundle?.id}" optionKey="id" 
							optionValue="name" style="background-color: red" onchange="changeMoveBundleColor(this.name,this.value,'${assetEntity.moveBundle?.id}','')"/>
						</g:if>
						<g:elseif test="${depBundle!=assetEntity.moveBundle }" >
							<g:select from="${moveBundleList}" name="moveBundle_dependent_${dependent.id}" value="${depBundle?.id}" 
							optionKey="id" optionValue="name" class="dep-${(dependent.status != 'Questioned' && dependent.status != 'Validated') ? 'Unknown' : dependent.status } depBundle"
							onchange="changeMoveBundleColor(this.name,this.value,'${assetEntity.moveBundle?.id}','')"/>
						</g:elseif>
						<g:else>
							<g:select from="${moveBundleList}" class="depBundle" name="moveBundle_dependent_${dependent.id}" value="${depBundle?.id}" 
							optionKey="id" optionValue="name" class="dep-${(dependent.status != 'Questioned' && dependent.status != 'Validated') ? 'Unknown' : dependent.status } depBundle"
							onchange="changeMoveBundleColor(this.name,this.value,'${assetEntity.moveBundle?.id}','')"/>
						</g:else>
					</td>
					<td>
						<g:select name="dtype_dependent_${dependent.id}" value="${dependent.type}" from="${dependencyType.value}" optionValue="value"/>
						<g:render template="../assetEntity/dependencyComment" model= "[dependency:dependent,type:'dependent',forWhom:'edit']"></g:render>
					</td>
					<td><g:select name="status_dependent_${dependent.id}" value="${dependent.status}" from="${dependencyStatus.value}" 
						optionValue="value" onchange="changeMoveBundleColor(this.name,'','${assetEntity.moveBundle?.id}',this.value)"/>
						</td>
					<td><a href="javascript:deleteRow('row_d_${i}_${dependent.id}', '${whom}_dependentAddedId')"><span class='clear_filter'>X</span></a></td>
				</tr>
			</g:each>
			</tbody>
		</table>
	</div>
</td>