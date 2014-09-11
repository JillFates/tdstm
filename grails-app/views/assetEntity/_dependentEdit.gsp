<%--
 --- Not sure what this template is used for...
 --%>
<div style="display: none;">
	<table id="assetDependencyRow">
		<tr>
			<td><g:select name="dataFlowFreq" from="${com.tds.asset.AssetDependency.constraints.dataFlowFreq.inList}"></g:select></td>
			<td>
				<g:select name="assetClassOption" 
					onchange="AssetCrudModule.updateDependentAssetNameSelect(this.name, this.value)" 
					from="${com.tdsops.tm.enums.domain.AssetClass.getClassOptions().entrySet()}" optionKey="key" optionValue="value"
					<%-- Set the default to Application if on Application edit otherwise default to Servers --%>
					value="${ forWhom == 'Application' ? 'APPLICATION' : 'SERVER-DEVICE'}" 
				></g:select>
			</td>
			<td>
				<input type="hidden" name="asset" id="dependenciesId"  
					style="width:300px"
					onchange="changeMovebundle(this.value,this.id,jQuery('#moveBundle').val())" 
				/>
			</td>
			<td>
				<g:select name="bundles" from="${moveBundleList}" class="depBundle" optionKey="id" optionValue="name" 
					noSelection="${['':'Please select']}" onchange="changeMoveBundleColor(this.name,this.value, jQuery('#moveBundle').val(),'')"
				></g:select>
			</td>
			<td nowrap="nowrap">
				<g:select name="dtype" from="${dependencyType?.value}"  optionValue="value"></g:select>
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
			<td>
				<g:select name="status" from="${dependencyStatus?.value}" optionValue="value"
					onchange="changeMoveBundleColor(this.name,'', jQuery('#moveBundle').val(),this.value)"
				></g:select>
			</td>
		</tr>
	</table>
</div>