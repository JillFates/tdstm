<g:form method="post">
<input type="hidden" name="id" value="${databaseInstance?.id}" />
<table style="border: 0">
	<tr>
		<td colspan="2">
			<div class="dialog">
				<table>
					<tbody>
						<tr>
							<td class="label" nowrap="nowrap"><label for="assetName">Name<span style="color: red;">*</span></label></td>
							<td style="font-weight:bold;"><input type="text" id="assetName" name="assetName" value="${databaseInstance.assetName}" tabindex="11" /></td>
							<td class="label" nowrap="nowrap"><label for="description">Description</label></td>
							<td colspan="3"><input type="text" id="description" name="description" value="${databaseInstance.description}" size="50" tabindex="21" /></td>
						</tr>
						<tr>
							<td class="label" nowrap="nowrap"><label for="assetType">Type</label></td>
							<td><input type="text" id="assetType" name="assetType" readonly="readonly" value="${databaseInstance.assetType}" tabindex="12" /></td>
							<td class="label" nowrap="nowrap"><label for="supportType">Support</label></td>
							<td><input type="text" id="supportType" name="supportType" value="${databaseInstance.supportType}" tabindex="26" /></td>
							<td class="label" nowrap="nowrap"><label for="environment">Environment</label></td>
							<td><g:select id="environment" name="environment" from="${com.tds.asset.AssetEntity.constraints.environment.inList}" value="${databaseInstance.environment}" tabindex="32" /></td>
						</tr>
						<tr>
							<td class="label" nowrap="nowrap"><label for="dbFormat">Format<span style="color: red;">*</span></label></td>
							<td><input type="text" id="dbFormat" name="dbFormat" value="${databaseInstance.dbFormat}" tabindex="13" /></td>
							<td class="label"><label for="retireDate">Retire Date:</label></td>
							<td valign="top" class="value ${hasErrors(bean:databaseInstance,field:'retireDate','errors')}">
							    <script type="text/javascript" charset="utf-8">
							    jQuery(function($){$('.dateRange').datepicker({showOn: 'both', buttonImage: '${createLinkTo(dir:'images',file:'calendar.gif')}', buttonImageOnly: true,beforeShow: customRange});function customRange(input) {return null;}});
					                    </script>
					                    <input type="text" class="dateRange" size="15" style="width: 112px; height: 14px;" name="retireDate" id="retireDate"
					                    value="<tds:convertDate date="${databaseInstance?.retireDate}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/>" onchange="isValidDate(this.value);" tabindex="27" >
							</td>
							<td class="label" nowrap="nowrap"><label for="moveBundle">Bundle</label></td>
							<td><g:select from="${moveBundleList}" id="moveBundle" name="moveBundle.id" value="${databaseInstance?.moveBundle?.id}" optionKey="id" optionValue="name" tabindex="34" /></td>
						</tr>
						<tr>
							<td class="label" nowrap="nowrap"><label for="dbSize">Size<span style="color: red;">*</span></label></td>
							<td><input type="text" id="dbSize" name="dbSize" value="${databaseInstance.dbSize}" tabindex="14" /></td>
							<td  class="label"><label for="maintExpDate">Maint Exp.</label></td>
							<td valign="top" class="value ${hasErrors(bean:databaseInstance,field:'maintExpDate','errors')}">
							    <script type="text/javascript" charset="utf-8">
					                    jQuery(function($){$('.dateRange').datepicker({showOn: 'both', buttonImage: '${createLinkTo(dir:'images',file:'calendar.gif')}', buttonImageOnly: true,beforeShow: customRange});function customRange(input) {return null;}});
					                    </script>
					                    <input type="text" class="dateRange" size="15" style="width: 112px; height: 14px;" name="maintExpDate" id="maintExpDate"
					                    value="<tds:convertDate date="${databaseInstance?.maintExpDate}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/>" onchange="isValidDate(this.value);" tabindex="28" >
							</td>
							<td class="label" nowrap="nowrap"><label for="planStatus">Plan Status</label></td>
							<td><g:select from="${planStatusOptions}" id="planStatus" name="planStatus" value="${databaseInstance.planStatus}" tabindex="35" /></td>
						</tr>
					</tbody>
				</table>
			</div>
		</td>
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
								<td class="dep-${support.status}"><g:select name="dataFlowFreq_support_${i}" value="${support.dataFlowFreq}" from="${support.constraints.dataFlowFreq.inList}" /></td>
								<td class="dep-${support.status}"><g:select name="entity_support_${i}" from="['Server','Application','Database','Files']" onchange='updateAssetsList(this.name, this.value)' value="${support?.asset?.assetType}"></g:select></td>
								<td class="dep-${support.status}"><g:select name="asset_support_${i}" from="${com.tds.asset.AssetEntity.findAllByAssetTypeAndProject(support?.asset?.assetType, project)}" value="${support?.asset?.id}" optionKey="id" optionValue="assetName"></g:select></td>
								<td class="dep-${support.status}"><g:select name="dtype_support_${i}" value="${support.type}" from="${support.constraints.type.inList}" /></td>
								<td class="dep-${support.status}"><g:select name="status_support_${i}" value="${support.status}" from="${support.constraints.status.inList}" /></td>
								<td><a href="javascript:deleteRow('row_s_${i}')"><span class='clear_filter'><u>X</u></span></a></td>
							</tr>
						</g:each>
					</tbody>
				</table>
			</div>
		</td>
		<td valign="top">
			<div style="width: 400px;">
				<span style="float: left;"><h1>Depends on:</h1></span>
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
								<td class="dep-${dependent.status}"><g:select name="dataFlowFreq_dependent_${i}" value="${dependent.dataFlowFreq}" from="${dependent.constraints.dataFlowFreq.inList}" /></td>
								<td class="dep-${dependent.status}"><g:select name="entity_dependent_${i}" from="['Server','Application','Database','Files']" onchange='updateAssetsList(this.name, this.value)' value="${dependent?.dependent?.assetType}"></g:select></td>
								<td class="dep-${dependent.status}"><g:select name="asset_dependent_${i}" from="${com.tds.asset.AssetEntity.findAllByAssetTypeAndProject(dependent?.dependent?.assetType, project)}" value="${dependent?.dependent?.id}" optionKey="id" optionValue="assetName"></g:select></td>
								<td class="dep-${dependent.status}"><g:select name="dtype_dependent_${i}" value="${dependent.type}" from="${dependent.constraints.type.inList}" /></td>
								<td class="dep-${dependent.status}"><g:select name="status_dependent_${i}" value="${dependent.status}" from="${dependent.constraints.status.inList}" /></td>
								<td><a href="javascript:deleteRow('row_d_${i}')"><span class='clear_filter'><u>X</u></span></a></td>
							</tr>
						</g:each>
					</tbody>
				</table>
			</div>
		</td>
	</tr>
	<tr>
		<td colspan="2">
			<div class="buttons">
				<input name="dependentCount" id="dependentCount" type="hidden" value="${dependentAssets.size()}" />
				<input name="supportCount" id="supportCount" type="hidden" value="${supportAssets.size()}" />
				<input name="redirectTo" type="hidden" value="$('#redirectTo').val()">
				<span class="button"><g:actionSubmit class="save" value="Update" /></span>
				<span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /> </span>
			</div>
		</td>
	</tr>
</table>
</g:form>
