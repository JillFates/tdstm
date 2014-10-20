<%--
 --- Html that inserted into the Asset Dependency Edit when user clicks Add button
 --%>
<%@page import="com.tdsops.tm.enums.domain.AssetClass"%>
<div style="display: none;">
	<table id="assetDependencyRow">
		<tr>
			<td><g:select name="dataFlowFreq" from="${com.tds.asset.AssetDependency.constraints.dataFlowFreq.inList}"></g:select></td>

			<%-- Asset Class Options SELECT --%>
			<td>
				<g:select name="entity" 
					onChange="EntityCrud.updateDependentAssetNameSelect(this.name)" 
					from="${AssetClass.getClassOptions().entrySet()}" optionKey="key" optionValue="value"
					<%-- Set the default to Application if on Application edit otherwise default to Servers --%>
					>
				</g:select>
			</td>

			<%-- Asset Name SELECT --%>
			<td>
				<input type="hidden" name="asset" id="dependenciesId"  
					style="width:150px"
					onchange="changeMovebundle(this.value,this.id,jQuery('#moveBundle').val())" />
			</td>

			<%-- Bundle SELECT --%>
			<td>
				<g:select name="bundles" from="${moveBundleList}" class="depBundle" optionKey="id" optionValue="name" 
					noSelection="${['':' Please Select']}" onchange="changeMoveBundleColor(this.name,this.value, jQuery('#moveBundle').val(),'')"
				></g:select>
			</td>

			<%-- Dependency Type SELECT --%>
			<td nowrap="nowrap">
				<g:select name="dtype" from="${dependencyType}"  optionValue="value"></g:select>
				<input type="hidden" name="aDepComment" id="aDepComment" value="">
				<div id="depComment"  style="display:none" >
					<textarea rows="5" cols="50" name="dep_comment" id="dep_comment"></textarea>
					<div class="buttons">
						<span class="button"><input type="button" class="save" value="Save" onclick="saveDepComment('dep_comment', 'aDepComment', 'depComment', 'commLink')"/> </span>
					</div>
				</div>
			    <a id="commLink" href="javascript:openCommentDialog('depComment')">
				   <img src="${resource(dir:'i',file:'db_table_light.png')}" border="0px" />
				</a>
			</td>

			<%-- Status SELECT --%>
			<td>
				<g:select name="status" from="${dependencyStatus}" optionValue="value"
					onchange="changeMoveBundleColor(this.name,'', jQuery('#moveBundle').val(),this.value)">
				</g:select>
			</td>
		</tr>
	</table>
</div>