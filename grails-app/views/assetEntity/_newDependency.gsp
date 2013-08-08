<div style="display: none;">
	<table id="assetDependencyRow">
		<tr>
			<td><g:select name="dataFlowFreq" from="${com.tds.asset.AssetDependency.constraints.dataFlowFreq.inList}"></g:select></td>
			<td>
					<g:select name="entity" from="['Application','Server','Database','Storage','Network']" 
							onchange='updateAssetsList(this.name, this.value)' 
							value="${forWhom== 'Application' ? 'Application' : 'Server'}"></g:select>
			</td>
			<td class='combo-td'>
				<span id="${forWhom}"><g:select name="asset" id="dependenciesId" 
					from="${entities}" optionKey="${-2}" optionValue="${1}" 
					noSelection="${['null':'Please select']}" class="depSelect"></g:select>
				</span>
			</td>
			<td><g:select name="dtype" from="${dependencyType?.value}"  optionValue="value"></g:select></td>
			<td><g:select name="status" from="${dependencyStatus?.value}" optionValue="value"></g:select></td>
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
	<span id="Network"><g:select name="asset" from="${networks}" optionKey="${-2}" optionValue="${1}" 
			noSelection="${['null':'Please select']}" class="depSelect"></g:select></span>
</div>