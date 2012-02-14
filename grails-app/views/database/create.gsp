<g:form method="post">
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
							<td><input type="text" id="assetType" name="assetType" readonly="readonly" value="Database" tabindex="12" /></td>
							<td class="label" nowrap="nowrap"><label for="supportType">Support</label></td>
							<td><input type="text" id="supportType" name="supportType" value="${databaseInstance.supportType}" tabindex="26" /></td>
							<td class="label" nowrap="nowrap"><label for="environment">Environment</label></td>
							<td><g:select id="environment" name="environment" from="${com.tds.asset.AssetEntity.constraints.environment.inList}" tabindex="32" /></td>
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
							<td><g:select from="${moveBundleList}" id="moveBundle" name="moveBundle.id" value="${databaseInstance?.moveBundle}" optionKey="id" optionValue="name" tabindex="34" /></td>
						</tr>
						<tr>
							<td class="label" nowrap="nowrap"><label for="dbSize">Size<span style="color: red;">*</span></label></td>
							<td><input type="text" id="dbSize" name="dbSize" value="${databaseInstance.dbSize}" tabindex="14" /></td>
							<td class="label"><label for="maintExpDate">Maint Exp.</label></td>
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
						<tr>
							<td class="label" nowrap="nowrap"><label for="custom1">${project.custom1 ?: 'Custom1'}</label></td>
							<td><input type="text" id="custom1" name="custom1"  value="${databaseInstance.custom1}"  /></td>
							<td class="label" nowrap="nowrap"><label for="custom1">${project.custom2 ?: 'Custom2'}</label></td>
							<td><input type="text" id="custom2" name="custom2"  value="${databaseInstance.custom2}"  /></td>
							<td class="label" nowrap="nowrap"><label for="custom3">${project.custom3 ?: 'Custom3'}</label></td>
							<td><input type="text" id="custom3" name="custom3"  value="${databaseInstance.custom3}"  /></td>
						</tr>
						<tr>
							<td class="label" nowrap="nowrap"><label for="custom4">${project.custom4 ?: 'Custom4'}</label></td>
							<td><input type="text" id="custom4" name="custom4"  value="${databaseInstance.custom4}"  /></td>
							<td class="label" nowrap="nowrap"><label for="custom5">${project.custom5 ?: 'Custom5'}</label></td>
							<td><input type="text" id="custom5" name="custom5"  value="${databaseInstance.custom5}"  /></td>
							<td class="label" nowrap="nowrap"><label for="custom6">${project.custom6 ?: 'Custom6'}</label></td>
							<td><input type="text" id="custom6" name="custom6"  value="${databaseInstance.custom6}"  /></td>
						</tr>
						<tr>
							<td class="label" nowrap="nowrap"><label for="custom7">${project.custom7 ?: 'Custom7'}</label></td>
							<td><input type="text" id="custom7" name="custom7"  value="${databaseInstance.custom7}"  /></td>
							<td class="label" nowrap="nowrap"><label for="custom8">${project.custom8 ?: 'Custom8'}</label></td>
							<td><input type="text" id="custom8" name="custom8"  value="${databaseInstance.custom8}"  /></td>
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
					</tbody>
				</table>
			</div>
		</td>
		<td valign="top">
			<div style="width: auto;">
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
					</tbody>
				</table>
			</div>
		</td>
	</tr>
	<tr>
		<td colspan="2">
			<div class="buttons">
				<input name="dependentCount" id="dependentCount" type="hidden" value="0" />
				<input name="supportCount" id="supportCount" type="hidden" value="0" />
				<input name="attributeSet.id" type="hidden" value="1" />
				<input name="project.id" type="hidden" value="${projectId}" />
				<span class="button"><g:actionSubmit class="save" value="Save" /></span>
			</div>
		</td>
	</tr>
</table>
</g:form>
<script>
	currentMenuId = "#assetMenu";
	$("#assetMenuId a").css('background-color','#003366')
</script>
