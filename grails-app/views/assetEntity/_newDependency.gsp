<div style="display: none;">
	<table id="assetDependencyRow">
		<tr>
			<td><g:select name="dataFlowFreq" from="${com.tds.asset.AssetDependency.constraints.dataFlowFreq.inList}"></g:select></td>
			<td>
					<g:select name="entity" from="['Application','Server','Database','Storage','Other']" 
							onchange='updateAssetsList(this.name, this.value)' 
							value="${forWhom== 'Application' ? 'Application' : 'Server'}"></g:select>
			</td>
			<td class='combo-td'>
				<span id="${forWhom}"><g:select name="asset" id="dependenciesId" 
					from="${entities}" optionKey="${-2}" optionValue="${1}" 
					noSelection="${['null':'Please select']}" class="depSelect" onchange="changeMovebundle(this.value,this.id,jQuery('#moveBundle').val())"></g:select>
				</span>
			</td>
			<td><g:select name="bundles" from="${moveBundleList}" class="depBundle" optionKey="id" optionValue="name" 
				noSelection="${['':' Please Select']}" onchange="changeMoveBundleColor(this.name,this.value, jQuery('#moveBundle').val(),'')"></g:select></td>
			<td nowrap="nowrap"><g:select name="dtype" from="${dependencyType?.value}"  optionValue="value"></g:select>
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
			<td><g:select name="status" from="${dependencyStatus?.value}" optionValue="value"
				onchange="changeMoveBundleColor(this.name,'', jQuery('#moveBundle').val(),this.value)"></g:select></td>
		</tr>
	</table>
</div>
<div style="display: none;">
	<span id="Server"><g:select name="asset" from="${servers}" optionKey="${-2}" optionValue="${1}" 
			noSelection="${['null':'Please select']}" class="depSelect"></g:select></span>
	<span id="Application"><g:select name="asset" from="${applications}" optionKey="${-2}" optionValue="${1}" 
			noSelection="${['null':'Please select']}" class="depSelect"></g:select></span>
	<span id="Database"><g:select name="asset" from="${dbs}" optionKey="${-2}" optionValue="${1}" 
			noSelection="${['null':'Please select']}" class="depSelect"></g:select></span>
	<span id="Storage"><g:select name="asset" from="${files}" optionKey="${-2}" optionValue="${1}" 
			noSelection="${['null':'Please select']}" class="depSelect"></g:select></span>
	<span id="Other"><g:select name="asset" from="${networks}" optionKey="${-2}" optionValue="${1}" 
			noSelection="${['null':'Please select']}" class="depSelect"></g:select></span>
</div>