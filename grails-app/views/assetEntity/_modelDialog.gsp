<div id="manufacturerShowDialog" title="Show Manufacturer" style="display: none;">
	<div class="dialog">
		<table>
	    	<tbody>
		<tr class="prop">
			<td valign="top" class="name">Name:</td>
			<td valign="top" class="value" id="showManuName"></td>
		</tr>
		<tr>
			<td valign="top" class="name">AKA:</td>
			<td valign="top" class="value"  id="showManuAka"></td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name">Description:</td>
			<td valign="top" class="value" id="showManuDescription"></td>
		</tr>
		</tbody>
		</table>
	</div>
	<tds:hasPermission permission='ModelDialogView'>
	<div class="buttons">
	        <input type="hidden" name="id" id="show_manufacturerId" />
	        <span class="button"><input type="button" class="edit" value="Edit" onclick="showOrEditModelManuDetails('manufacturer',$('#show_manufacturerId').val(),'Manufacturer','edit','Edit');$('#manufacturerShowDialog').dialog('close');"/></span>
	</div>
	</tds:hasPermission>
</div>
<div id="modelShowDialog"  title="Show Model" style="display: none;">
<div class="dialog">
	<table>
		<tbody>
		<tr>
			<td valign="top" class="name">Manufacturer:</td>
			<td valign="top" class="value" id="showManufacturer"></td>
		</tr>
		<tr>
			<td valign="top" class="name">Model Name:</td>
			<td valign="top" class="value" id="showModelName"></td>
		</tr>
		<tr>
			<td valign="top" class="name">AKA:</td>
			<td valign="top" class="value" id="showModelAka"></td>
		</tr>
		<tr>
			<td valign="top" class="name">Asset Type:</td>
			<td valign="top" class="value" id="showModelAssetType"></td>
		</tr>
		<tr>
			<td valign="top" class="name">Usize:</td>
			<td valign="top" class="value" id="showModelUsize"></td>
		</tr>
		<tr>
		    <td valign="top" class="name">Power (max/design/avg) :</td>
		    <td>
			  <span id="namePlatePowerSpanId"></span>/

			  <span id="PowerDesignSpanId"></span>/

			  <span id="powerSpanId"></span>
			</td>
		</tr>

		<tr>
			<td valign="top" class="name">Front image:</label></td>
			<td valign="top" class="value" id="showModelFrontImage"></td>
		</tr>
		<tr>
			<td valign="top" class="name">Rear image:</td>
			<td valign="top" class="value" id="showModelRearImage"></td>
		</tr>
		<tr>
			<td valign="top" class="name">Use Image:</td>
			<td valign="top" class="value" id="showModelUseImage"></td>
	        </tr>
		<tr id="showModelBladeRowsTr">
			<td valign="top" class="name">Blade Rows:</td>
			<td valign="top" class="value" id="showModelBladeRows"></td>
		</tr>
		<tr id="showModelBladeCountTr">
			<td valign="top" class="name">Blade Count:</td>
			<td valign="top" class="value" id="showModelBladeCount"></td>
		</tr>
		<tr id="showModelBladLabelCountTr">
			<td valign="top" class="name">Blade Label Count:</td>
			<td valign="top" class="value" id="showModelBladLabelCount"></td>
		</tr>
		<tr id="showModelBladeHeightTr">
			<td valign="top" class="name">Blade Height:</td>
			<td valign="top" class="value" id="showModelBladeHeight"></td>
		</tr>
		<tr>
			<td valign="top" class="name">Source TDS:</td>
			<td valign="top" class="value" id="showModelSourceTds"></td>
	        </tr>
		<tr>
			<td valign="top" class="name">Notes:</td>
			<td valign="top" class="value" id="showModelNotes"></td>
		</tr>
		</tbody>
	</table>
</div>
<tds:hasPermission permission='ModelDialogView'>
<div class="buttons"> 
	<g:form action="edit" controller="model" target="new">
		<input name="id" type="hidden" id="show_modelId"/>
		<span class="button">
			<input type="submit" class="edit" value="Edit"></input>
		</span>
	</g:form>
</div>
</tds:hasPermission>
</div>