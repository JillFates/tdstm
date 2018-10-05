<%--
 Html that copied into the _dependent.gsp tables when user clicks Add button
 @param moveBundleList - list of move bundles for the project
 @param dependencyType - list of the various dependency type values
 @param dependencyStatus - list of the various dependency status values
--%>
<%@page import="com.tdsops.tm.enums.domain.AssetClass"%>
<div style="display: none;">
	<table id="assetDependencyRow">
		<tr>
			<%-- Frequency SELECT --%>
			<td><g:select name="dataFlowFreq_FIELD_SUFFIX" from="${com.tds.asset.AssetDependency.constraints.dataFlowFreq.inList}"></g:select></td>

			<%-- Asset Class Options SELECT --%>
			<td class="class-wrap-depend">
				<%-- Set the default to Application if on Application edit otherwise default to Servers --%>
				<g:select name="entity_FIELD_SUFFIX"
					onChange="EntityCrud.updateDependentAssetNameSelect(this.name)"
					from="${AssetClass.getClassOptions().entrySet()}" optionKey="key" optionValue="value"
					>
				</g:select>
			</td>

			<%-- Asset Name SELECT --%>
			<td>
				<input type="hidden" name="asset_FIELD_SUFFIX" id="asset_FIELD_SUFFIX"
					style="width:150px"
					onchange="EntityCrud.updateDependentBundle(this.value, this.id, 'bundles_FIELD_SUFFIX');" />
			</td>

			<%-- Bundle SELECT --%>
			<td>
				<g:select name="moveBundle_FIELD_SUFFIX" from="${moveBundleList}" class="depBundle" optionKey="id" optionValue="name"
					noSelection="${['':' Please Select']}" onchange="EntityCrud.changeDependentBundleColor(this.name,this.value, jQuery('#moveBundle').val(),'')"
				></g:select>
			</td>

			<%-- Dependency Type SELECT --%>
			<td nowrap="nowrap">
				<g:select name="type_FIELD_SUFFIX" from="${dependencyType}" optionValue="value"></g:select>

				<g:render template="../assetEntity/dependentComment" model="[dependency:[id:'FIELD_SUFFIX', comment:''], type:'', forWhom:'edit']"></g:render>

				<%--
				<input type="hidden" name="comment_FIELD_SUFFIX" id="comment_FIELD_SUFFIX" value="">
				<div id="depComment_FIELD_SUFFIX" style="display:none" >
					<textarea rows="5" cols="100" name="dep_comment_FIELD_SUFFIX" id="dep_comment"></textarea>
					<div class="buttons">
						<span class="button">
							<input type="button" class="save" value="Close"
								onclick="EntityCrud.onDepCommentDialogClose('dep_comment_FIELD_SUFFIX', 'comment_FIELD_SUFFIX', 'depComment_FIELD_SUFFIX', 'commLink_FIELD_SUFFIX');"
							/>
						</span>
					</div>
				</div>
			    <a id="commLink_FIELD_SUFFIX" href="javascript:EntityCrud.openDepCommentDialog('depComment_FIELD_SUFFIX')">
				   <asset:image src="i/db_table_light.png" border="0px" alt="Comment" />
				</a>
				--%>

			</td>

			<%-- Status SELECT --%>
			<td>
				<g:select name="status_FIELD_SUFFIX" from="${dependencyStatus}" optionValue="value"
					onchange="EntityCrud.changeDependentBundleColor(this.name,'', jQuery('#moveBundle').val(),this.value)">
				</g:select>
			</td>

			<%-- Delete Row Icon --%>
			<td>
				<a href="javascript:EntityCrud.deleteAssetDependencyRow('ROW_ID');"><span class="clear_filter">X</span></a>
			</td>
		</tr>
	</table>
</div>