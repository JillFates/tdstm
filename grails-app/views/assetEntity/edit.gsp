<g:form method="post">
	<table style="border:0;width:1000px;">
		<tr>
			<td colspan="2">
				<div class="dialog">
					<table>
						<tbody>
							<tr>
								<td class="label" nowrap="nowrap"><label for="assetName">Name</label></td>
								<td ><input type="text" id="assetName" name="assetName" value="${assetEntityInstance.assetName}" /></td>
								
								<td class="label" nowrap="nowrap"><label for="application">Application</label></td>
								<td ><input type="text" id="application" name="application" value="${assetEntityInstance.application}" /></td>
								
								<td ><td class="label">Source</td>
								     <td class="label">Target</td>
								</td>
								<td class="label" nowrap="nowrap"><label for="custom1">Custom1</label>
								</td>
								<td ><input type="text" id="custom1" name="custom1" value="${assetEntityInstance.custom1}" />
								</td>
								
								
							</tr>
							<tr>
								<td class="label" nowrap="nowrap"><label for="assetType">Type</label></td>
								<td ><g:select from="${assetTypeOptions}" id="assetTypeId" name="assetType" value="${assetEntityInstance.assetType}" onChange="selectManufacturer(this.value)"/></td>
								<td class="label" nowrap="nowrap"><label for="priority">Priority</label>
								</td>
								<td ><g:select id="priority" name ="priority" from="${priorityOption}" value= "${assetEntityInstance.priority}" noSelection="${['':' Please Select']}"/>
								</td>
								<td class="label" nowrap="nowrap"><label for="sourceLocation">Location</label>
								<td><input type="text" id="sourceLocation"
									name="sourceLocation" value="${assetEntityInstance.sourceLocation}" /></td>
									<td><input type="text" id="targetLocation"
									name="targetLocation" value="${assetEntityInstance.targetLocation}" /></td>
								</td>
								<td class="label" nowrap="nowrap"><label for="custom2">Custom2</label>
								</td>
								<td ><input type="text" id="custom2" name="custom2" value="${assetEntityInstance.custom2}" />
								</td>
								
								
							</tr>
							<tr>
								<td class="label" nowrap="nowrap"><label for="manufacturer">Manufacturer</label>
								</td>
								 <td >
								 <div id="manufacturerId">
								   <g:select id="manufacturer" name="manufacturer.id" from="${manufacturers}" value="${assetEntityInstance.manufacturer}" onChange="selectModel(this.value)" optionKey="id" optionValue="name" noSelection="${['':' Unassigned']}"/>
								 </div>
								</td>
								<td class="label" nowrap="nowrap"><label for="ipAddress">IP1</label></td>
								<td ><input type="text" id="ipAddress" name="ipAddress"
									value="${assetEntityInstance.ipAddress}" />
								</td>
								<td class="label" nowrap="nowrap"><label for="sourceRoom">Room</label>
								<td><input type="text" id="sourceRoom"
									name="sourceRoom" value="${assetEntityInstance.sourceRoom}" /></td>
									<td><input type="text" id="targetRoom"
									name="targetRoom" value="${assetEntityInstance.targetRoom}" /></td>
								</td>
								<td class="label" nowrap="nowrap"><label for="custom3">Custom3</label>
								</td>
								<td ><input type="text" id="custom3" name="custom3" value="${assetEntityInstance.custom3}" />
								</td>
								
								
							</tr>
							<tr>
								<td class="label" nowrap="nowrap"><label for="model">Model</label>
								</td>
								<td>
								<div id="modelId">
								   <g:select id="model" name ="model.id" from="${models}" value= "${assetEntityInstance.model}" noSelection="${['':' Unassigned']}" />
								 </div>
								</td>
								
								<td class="label" nowrap="nowrap"><label for="assetTag">Tag</label></td>
								<td ><input type="text" id="assetTag" name="assetTag" value="${assetEntityInstance.assetTag}" /></td>
								
								<td class="label" nowrap="nowrap"><label for="sourceRack">Rack/Cab</label>
								<td><input type="text" id="sourceRack"
									name="sourceRack" value="${assetEntityInstance.sourceRack}" /></td>
									<td><input type="text" id="targetRack"
									name="targetRack" value="${assetEntityInstance.targetRack}" /></td>
								</td>
								<td class="label" nowrap="nowrap"><label for="custom4">Custom4</label>
								</td>
								<td ><input type="text" id="custom4" name="custom4" value="${assetEntityInstance.custom4}" />
								</td>
								
								
							</tr>
							<tr>
								<td class="label" nowrap="nowrap"><label for="shortName">Alt Name</label></td>
								<td ><input type="text" id="shortName"
									name="shortName" value="${assetEntityInstance.shortName}" />
								</td>
								<td class="label" nowrap="nowrap"><label for="os">OS</label></td>
								<td ><input type="text" id="os" name="os"
									value="${assetEntityInstance.os}" />
								</td>
								<td class="label" nowrap="nowrap"><label for="sourceRack">Position</label>
								<td><input type="text" id="sourceRackPosition"
									name="sourceRackPosition" value="${assetEntityInstance.sourceRackPosition}" /></td>
									<td><input type="text" id="targetRackPosition"
									name="targetRackPosition" value="${assetEntityInstance.targetRackPosition}" /></td>
								</td>
								<td class="label" nowrap="nowrap"><label for="custom5">Custom5</label>
								</td>
								<td ><input type="text" id="custom5" name="custom5" value="${assetEntityInstance.custom5}" />
								</td>
								
								
							</tr>
							<tr>
								<td class="label" nowrap="nowrap"><label for="serialNumber">S/N</label></td>
								<td ><input type="text" id="serialNumber"
									name="serialNumber" value="${assetEntityInstance.serialNumber}" />
								</td>
								<td class="label" nowrap="nowrap"><label for="maintContract">Maint</label></td>
								<td ><input type="text" id="maintContract" name="maintContract"
									value="${assetEntityInstance.maintContract}" />
								</td>
								<td class="label" nowrap="nowrap"><label for="sourceBladeChassis">Blade</label>
								<td><input type="text" id="sourceBladeChassis"
									name="sourceBladeChassis" value="${assetEntityInstance.sourceBladeChassis}" /></td>
									<td><input type="text" id="targetBladeChassis"
									name="targetBladeChassis" value="${assetEntityInstance.targetBladeChassis}" /></td>
								</td>
								<td class="label" nowrap="nowrap"><label for="custom6">Custom6</label>
								</td>
								 <td ><input type="text" id="custom6" name="custom6" value="${assetEntityInstance.custom6}" />
								</td>
								
								
								
								
							</tr>
							<tr>
								<td class="label" nowrap="nowrap"><label for="appOwner">owner</label></td>
								<td ><input type="text" id="appOwner" name="appOwner" value="${assetEntityInstance.appOwner}"/></td>
								<td class="label"><label for="retireDate">Retire
									Date:</label>
								</td>
								<td valign="top"
									class="value ${hasErrors(bean:assetEntityInstance,field:'retireDate','errors')}">
								    <script type="text/javascript" charset="utf-8">
				                    jQuery(function($){$('.dateRange').datepicker({showOn: 'both', buttonImage: '${createLinkTo(dir:'images',file:'calendar.gif')}', buttonImageOnly: true,beforeShow: customRange});function customRange(input) {return null;}});
				                    </script> <input type="text" class="dateRange" size="15" style="width: 112px; height: 14px;" name="retireDate" id="retireDate"
									value="<tds:convertDate date="${assetEntityInstance?.retireDate}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/>" > 
							     </td>
								<td class="label" nowrap="nowrap"><label for="sourceBladePosition">Blade Position</label>
								<td><input type="text" id="sourceBladePosition"
									name="sourceBladePosition" value="${assetEntityInstance.sourceBladePosition}" /></td>
									<td><input type="text" id="targetBladePosition"
									name="targetBladePosition" value="${assetEntityInstance.targetBladePosition}" /></td>
								</td>
								<td class="label" nowrap="nowrap"><label for="custom7">Custom7</label>
								</td>
								 <td ><input type="text" id="custom7" name="custom7" value="${assetEntityInstance.custom7}" />
								</td>
								
								
								
							</tr>
							<tr>
								<td  class="label"><label for="maintExpDate">Maint Exp.
									</label></td>
									<td valign="top"
										class="value ${hasErrors(bean:assetEntityInstance,field:'maintExpDate','errors')}">
									    <script type="text/javascript" charset="utf-8">
					                    jQuery(function($){$('.dateRange').datepicker({showOn: 'both', buttonImage: '${createLinkTo(dir:'images',file:'calendar.gif')}', buttonImageOnly: true,beforeShow: customRange});function customRange(input) {return null;}});
					                    </script> <input type="text" class="dateRange" size="15" style="width: 112px; height: 14px;" name="maintExpDate" id="maintExpDate"
										value="<tds:convertDate date="${assetEntityInstance?.maintExpDate}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/>" > 
									</td>
								<td class="label" nowrap="nowrap"><label for="railType">Rail Type</label>
								</td>
								 <td ><g:select id="railType" name ="railType" from="${railTypeOption}" value= "${assetEntityInstance.railType}" noSelection="${['':' Please Select']}"/>
								</td>
								
								
								
								<td>&nbsp</td>
								<td>&nbsp</td>
								<td>&nbsp</td>
								<td class="label" nowrap="nowrap"><label for="custom8">Custom8</label>
								</td>
								 <td ><input type="text" id="custom8" name="custom8" value="${assetEntityInstance.custom8}" />
								</td>
								
							</tr>
							<tr>
							       <td class="label" nowrap="nowrap"><label for="moveBundle">Bundle</label></td>
								<td ><g:select from="${moveBundleList}" id="moveBundle" name="moveBundle.id" value="${assetEntityInstance.moveBundle}" optionKey="id" optionValue="name" tabindex="34" noSelection="${['':' Please Select']}"/>
								</td>
							   <td class="label" nowrap="nowrap"><label for="planStatus">Plan Status</label></td>
								<td ><g:select id="planStatus" name ="planStatus" from="${planStatusOptions}" value= "${assetEntityInstance.planStatus}" noSelection="${['':' Please Select']}"/>
								</td>
								<td>&nbsp</td>
								<td>&nbsp</td>
								<td>&nbsp</td>
							    
								<td class="label" nowrap="nowrap"><label for="truck">Truck</label>
								</td>
								<td ><input type="text" id="truck" name="truck" value="${assetEntityInstance.truck}" />
								</td>
							
							</tr>
							
							<tr>
							    <td class="label" nowrap="nowrap"><label for="cart">Cart</label>
								</td>
								 <td ><input type="text" id="cart" name="cart" value="${assetEntityInstance.cart}" />
								</td>
								<td class="label" nowrap="nowrap"><label for="shelf">Shelf</label>
								</td>
								 <td ><input type="text" id="shelf" name="shelf" value="${assetEntityInstance.shelf}" />
								</td>
								<td>&nbsp</td>
								<td>&nbsp</td>
								<td>&nbsp</td>
							    <td class="label" nowrap="nowrap"><label for="appSme">SME</label>
								</td>
								 <td ><input type="text" id="appSme" name="appSme" value="${assetEntityInstance.appSme}" />
								</td>
							</tr>
						</tbody>
					</table>
				</div></td>
		</tr>
<tr>
			<td valign="top">
				<div style="width: auto;">
					<span style="float: left;"><h1>Supports:</h1></span>
					<span style="float: right;"><input type='button' value='Add' onclick="addAssetDependency('support')"></span>
					<br/>
					<table style="width: 100%;">
						<thead>
							<tr>
								<th>Frequency</th>
								<th>Entity Type</th>
								<th>Name</th>
								<th>Type</th>
								<th>Status</th>
								<th>&nbsp;</th>
							</tr>
						</thead>
						<tbody id="createSupportsList">
							<g:each in="${supportAssets}" var="support" status="i">
								<tr id='row_s_${i}'>
									<td><g:select name="dataFlowFreq_support_${i}" value="${support.dataFlowFreq}" from="${support.constraints.dataFlowFreq.inList}" /></td>
									<td><g:select name="entity_support_${i}" from="['Server','Application','Database','Files']" onchange='updateAssetsList(this.name, this.value)' value="${support?.asset?.assetType}"></g:select></td>
									<td><g:select name="asset_support_${i}" from="${com.tds.asset.AssetEntity.findAllByAssetTypeAndProject(support?.asset?.assetType, project)}" value="${support?.asset?.id}" optionKey="id" optionValue="assetName"  style="width:90px;"></g:select></td>
									<td><g:select name="dtype_support_${i}" value="${support.type}" from="${support.constraints.type.inList}" />
									</td>
									<td><g:select name="status_support_${i}" value="${support.status}"	from="${support.constraints.status.inList}" />
									</td>
									<td><a href="javascript:deleteRow('row_s_${i}')"><span class='clear_filter'><u>X</u></span></a></td>
								</tr>
							</g:each>
						</tbody>
					</table>
				</div></td>
			<td valign="top">
				<div style="width: auto;">
					<span style="float: left;"><h1>Is dependent on:</h1></span>
					<span style="float: right;"><input type='button' value='Add' onclick="addAssetDependency('dependent')"></span>
					<br/>
					<table style="width: 100%;">
						<thead>
							<tr>
								<th>Frequency</th>
								<th>Entity Type</th>
								<th>Name</th>
								<th>Type</th>
								<th>Status</th>
								<th>&nbsp;</th>
							</tr>
						</thead>
						<tbody id="createDependentsList">
						<g:each in="${dependentAssets}" var="dependent" status="i">
							<tr id='row_d_${i}'>
								<td><g:select name="dataFlowFreq_dependent_${i}" value="${dependent.dataFlowFreq}" from="${dependent.constraints.dataFlowFreq.inList}" /></td>
								<td><g:select name="entity_dependent_${i}" from="['Server','Application','Database','Files']" onchange='updateAssetsList(this.name, this.value)' value="${dependent?.dependent?.assetType}"></g:select></td>
								<td><g:select name="asset_dependent_${i}" from="${com.tds.asset.AssetEntity.findAllByAssetTypeAndProject(dependent?.dependent?.assetType, project)}" value="${dependent?.dependent?.id}" optionKey="id" optionValue="assetName"  style="width:90px;"></g:select></td>
								<td><g:select name="dtype_dependent_${i}" value="${dependent.type}" from="${dependent.constraints.type.inList}" />
								</td>
								<td><g:select name="status_dependent_${i}" value="${dependent.status}" from="${dependent.constraints.status.inList}" />
								</td>
								<td><a href="javascript:deleteRow('row_d_${i}')"><span class='clear_filter'><u>X</u></span></a></td>
							</tr>
						</g:each>
						</tbody>
					</table>
				</div></td>
		</tr>
		<tr>
			<td colspan="2">
				<div class="buttons">
				    <input name="dependentCount" id="dependentCount" type="hidden" value="${dependentAssets.size()}">
					<input  name="supportCount"  id="supportCount" type="hidden" value="${supportAssets.size()}">
					<input name="attributeSet.id" type="hidden" value="1">
					<input name="project.id" type="hidden" value="${projectId}">
					<input name="id" type="hidden" value="${assetEntityInstance.id}">
					<span class="button"><g:actionSubmit class="save" value="Update" /> </span>
				</div></td>
		</tr>
	</table>
</g:form>