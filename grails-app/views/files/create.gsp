<script type="text/javascript">
	$("#file_assetName").val($('#gs_assetName').val())
	$("#file_fileFormat").val($('#gs_fileFormat').val())
	$("#file_fileSize").val($('#gs_fileSize').val())
	$("#file_planStatus").val($('#gs_planStatus').val())
	$("#file_moveBundle").val($('#gs_moveBundle').val())
</script>
<g:form method="post">
	<input type="hidden" id="file_assetName" name="assetNameFilter" value="" />
	<input type="hidden" id="file_fileFormat" name="fileFormatFilter" value="" />
	<input type="hidden" id="file_fileSize" name="fileSizeFilter" value="" />
	<input type="hidden" id="file_planStatus" name="planStatusFilter" value="" />
	<input type="hidden" id="file_moveBundle" name="moveBundleFilter" value="" />
	<table style="border: 0;">
		<tr>
			<td colspan="2">
				<div class="dialog">
					<table>
						<tbody>
							<tr>
								<td class="label" nowrap="nowrap"><label for="assetName">Name<span style="color: red;">*</span></label>
								</td>
								<td><input type="text" id="assetName" name="assetName"
									value="${fileInstance.assetName}" />
								</td>
								<td class="label" nowrap="nowrap">Description</td>
								<td colspan="2"><input type="text" id="description"
									name="description"
									value="${fileInstance.description}" size="50" />
								</td>
								<td>&nbsp;</td>
								<td class="label" nowrap="nowrap">Bundle</td>
								<td><g:select from="${moveBundleList}" id="moveBundle" name="moveBundle.id" value="${fileInstance?.moveBundle}" optionKey="id" optionValue="name" tabindex="34" />
								</td>
							</tr>

							<tr>
								<td class="label" nowrap="nowrap"><label for="assetType">
										Type</label>
								</td>
								<td><input type="text" id="assetType" name="assetType"
									value="Storage" readonly="readonly" />
								</td>
								<td class="label" nowrap="nowrap"><label for="lun">LUN</label>
								</td>
								<td><input type="text" id="lun" name="LUN"
									value="${fileInstance.LUN}"/> 
								</td>
								<td class="label" nowrap="nowrap"><label for="supportType">Support</label>
								</td>
								<td><input type="text" id="supportType" name="supportType"
									value="${fileInstance.supportType}" /></td>
								<td class="label" nowrap="nowrap"><label for="planStatus">PlanStatus</label></td>
								<td>
								<g:select from="${planStatusOptions}" id="planStatus" name="planStatus" value="${fileInstance.planStatus}" /></td>
							</tr>

							<tr>
								<td class="label" nowrap="nowrap"><label for="fileFormat">Format<span style="color: red;">*</span></label>
								</td>
								<td><input type="text" id="fileFormat" name="fileFormat"
									value="${fileInstance.fileFormat}" />
								</td>
								<td class="label" nowrap="nowrap"><label for="environment">Environment
								</label>
								</td>
								<td><g:select id="environment" name="environment" from="${com.tds.asset.AssetEntity.constraints.environment.inList}"/>
								</td>
								<td class="label" nowrap="nowrap"><label for="fileSize">Size<span style="color: red;">*</span>
								</label>
								</td>
								<td><input type="text" id="fileSize" name="fileSize" size="10"
									value="${fileInstance.fileSize}" /> &nbsp;
									<g:select from="${com.tds.asset.Files.constraints.sizeUnit.inList}" name="sizeUnit" id="sizeUnit" value="GB"/>
								</td>
								<td class="label">Validation</td>
								<td>
									<g:select from="${fileInstance.constraints.validation.inList}" id="validation" name="validation" value="Discovery"/>
								</td>
							</tr>
							<g:render template="../assetEntity/customEdit" model="[assetEntityInstance:fileInstance]"></g:render>
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
								<th>Type</th>
								<th>Name</th>
								<th>Type</th>
								<th>Status</th>
								<th>&nbsp;</th>
							</tr>
						</thead>
						<tbody id="createSupportsList">
						</tbody>
					</table>
				</div></td>
			<td valign="top">
				<div  style="width: auto;">
					<span style="float: left;"><h1>Is dependent on:</h1></span>
					<span style="float: right;"><input type='button' value='Add' onclick="addAssetDependency('dependent','create')"></span>
					<br/>
					<table style="width: 100%;">
						<thead>
							<tr>
								<th>Frequency</th>
								<th>Type</th>
								<th>Name</th>
								<th>Type</th>
								<th>Status</th>
								<th>&nbsp;</th>
							</tr>
						</thead>
						<tbody id="createDependentsList">
						</tbody>
					</table>
				</div></td>
		</tr>
		<tr>
			<td colspan="2">
				<div class="buttons">
				<input name="dependentCount" id="create_dependentCount" type="hidden" value="0">
					<input  name="supportCount"  id="create_supportCount" type="hidden" value="0">
					<input name="attributeSet.id" type="hidden" value="1"> <input
						name="project.id" type="hidden" value="${projectId}"> <span
						class="button"><g:actionSubmit class="save" value="Save" onclick="return validateFileFormat()"/>
					</span>
				</div>
			</td>
		</tr>
	</table>
</g:form>
<script>
	currentMenuId = "#assetMenu";
	$("#assetMenuId a").css('background-color','#003366')
	
</script>
