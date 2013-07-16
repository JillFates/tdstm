<script type="text/javascript">
	$("#db_assetName").val($('#gs_assetName').val())
	$("#db_dbFormat").val($('#gs_dbFormat').val())
	$("#db_planStatus").val($('#gs_planStatus').val())
	$("#db_moveBundle").val($('#gs_moveBundle').val())
</script>
<g:form method="post"  name="createAssetsFormId">
<input type="hidden" id="db_assetName" name="assetNameFilter" value="" />
<input type="hidden" id="db_dbFormat" name="dbFormatFilter" value="" />
<input type="hidden" id="db_planStatus" name="planStatusFilter" value="" />
<input type="hidden" id="db_moveBundle" name="moveBundleFilter" value="" />
<table style="border: 0">
	<tr>
		<td colspan="2">
			<div class="dialog">
				<table>
					<tbody>
						<tr>
							<td class="label ${config.assetName}" nowrap="nowrap"><label for="assetName">Name<span style="color: red;">*</span></label></td>
							<td style="font-weight:bold;"><input type="text" id="assetName" class="${config.assetName}" name="assetName" value="${databaseInstance.assetName}" tabindex="11" /></td>
							<td class="label ${config.description}" nowrap="nowrap"><label for="description">Description</label></td>
							<td colspan="5"><input type="text" id="description" class="${config.description}" name="description" value="${databaseInstance.description}" size="50" tabindex="21" /></td>
						</tr>
						<tr>
							<td class="label" nowrap="nowrap"><label for="assetType">Type</label></td>
							<td><input type="text" id="assetType" name="assetType" readonly="readonly" value="Database" tabindex="12" /></td>
							<td class="label ${config.supportType}" nowrap="nowrap"><label for="supportType">Support</label></td>
							<td><input type="text" id="supportType" class="${config.supportType}" name="supportType" value="${databaseInstance.supportType}" tabindex="26" /></td>
							<td class="label ${config.environment}" nowrap="nowrap"><label for="environment">Environment</label></td>
							<td colspan="3"><g:select id="environment" class="${config.environment}" name="environment" from="${com.tds.asset.AssetEntity.constraints.environment.inList}" tabindex="32" /></td>
						</tr>
						<tr>
							<td class="label ${config.dbFormat}" nowrap="nowrap"><label for="dbFormat">Format<span style="color: red;">*</span></label></td>
							<td><input type="text" id="dbFormat" class="${config.dbFormat}" name="dbFormat" value="${databaseInstance.dbFormat}" tabindex="13" /></td>
							<td class="label ${config.retireDate}"><label for="retireDate">Retire Date:</label></td>
							<td valign="top" class="value ${hasErrors(bean:databaseInstance,field:'retireDate','errors')}">
							    <script type="text/javascript" charset="utf-8">
							    jQuery(function($){$('.dateRange').datepicker({showOn: 'both', buttonImage: '${resource(dir:'images',file:'calendar.gif')}', buttonImageOnly: true,beforeShow: customRange});function customRange(input) {return null;}});
							    </script>
							    <input type="text" class="dateRange ${config.retireDate}" size="15" style="width: 112px; height: 14px;" name="retireDate" id="retireDate"
								value="<tds:convertDate date="${databaseInstance?.retireDate}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/>" onchange="isValidDate(this.value);" tabindex="27" > 
							</td>
							<td class="label ${config.moveBundle}" nowrap="nowrap"><label for="moveBundle">Bundle</label></td>
							<td colspan="3"><g:select from="${moveBundleList}" id="moveBundle" class="${config.moveBundle}" name="moveBundle.id" value="${databaseInstance?.moveBundle}" optionKey="id" optionValue="name" tabindex="34" /></td>
						</tr>
						<tr>
							<td class="label ${config.dbSize}" nowrap="nowrap"><label for="dbSize">Size<span style="color: red;">*</span></label></td>
							<td><input type="text" id="dbSize" class="${config.dbSize}" name="dbSize" value="${databaseInstance.dbSize}" tabindex="14" /></td>
							<td class="label ${config.maintExpDate}"><label for="maintExpDate">Maint Exp.</label></td>
							<td valign="top" class="value ${hasErrors(bean:databaseInstance,field:'maintExpDate','errors')}">
							    <script type="text/javascript" charset="utf-8">
							    jQuery(function($){$('.dateRange').datepicker({showOn: 'both', buttonImage: '${resource(dir:'images',file:'calendar.gif')}', buttonImageOnly: true,beforeShow: customRange});function customRange(input) {return null;}});
							    </script>
							    <input type="text" class="dateRange ${config.maintExpDate}" size="15" style="width: 112px; height: 14px;" name="maintExpDate" id="maintExpDate"
								value="<tds:convertDate date="${databaseInstance?.maintExpDate}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/>" onchange="isValidDate(this.value);" tabindex="28" > 
							</td>
							<td class="label ${config.planStatus}" nowrap="nowrap"><label for="planStatus">Plan Status</label></td>
							<td colspan="3"><g:select from="${planStatusOptions}" id="planStatus" class="${config.planStatus}" name="planStatus" value="${databaseInstance.planStatus}" tabindex="35" /></td>
						</tr>
						<tr>
							<td class="label" nowrap="nowrap"><label>Version</label></td><td></td>
							<td class="label" nowrap="nowrap"><label>SME1</label></td><td></td>
							<td class="label ${config.validation}"><label for="validation">Validation</label></td>
							<td colspan="3"><g:select from="${databaseInstance.constraints.validation.inList}" id="validation" class="${config.validation}" onChange="assetFieldImportance(this.value,'Database');" name="validation" value="Discovery"/></td>
						</tr>
						<tr>
							<td class="label ${config.externalRefId}" nowrap="nowrap"><label for="externalRefId">External Ref Id</label></td>
							<td><input type="text" id="externalRefId" class="${config.externalRefId}" name="externalRefId" value="${databaseInstance.externalRefId}" tabindex="11" /></td>
						</tr>
						<tbody class="customTemplate">
							<g:render template="../assetEntity/customEdit" model="[assetEntityInstance:databaseInstance]"></g:render>
						</tbody>
					</tbody>
				</table>
			</div>
		</td>
	</tr>
	<tr>
		<td valign="top">
			<div style="width: auto;">
				<span style="float: left;"><h1>Supports:</h1></span>
				<span style="float: right;"><input type='button' value='Add' onclick="addAssetDependency('support','create')"></span>
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
					<tbody id="createSupportsList">
					</tbody>
				</table>
			</div>
		</td>
		<td valign="top">
			<div style="width: auto;">
				<span style="float: left;"><h1>Depends on:</h1></span>
				<span style="float: right;"><input type='button' value='Add' onclick="addAssetDependency('dependent','create')"></span>
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
					<tbody id="createDependentsList">
					</tbody>
				</table>
			</div>
		</td>
	</tr>
	<tr>
		<td colspan="2">
			<div class="buttons">
				<input name="dependentCount" id="create_dependentCount" type="hidden" value="0" />
				<input name="supportCount" id="create_supportCount" type="hidden" value="0" />
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
