<script type="text/javascript">

$(document).ready(function() {
	$('#appLabel').attr('checked',true)
	var isAppChecked = $('#appChecked').val()
	var isServerChecked = $('#serverChecked').val()
	var isFilesChecked = $('#filesChecked').val()
	if(isAppChecked=='true'){
		  $('#appLabel').attr('checked',true)
	}else{
	  	  $('#appLabel').attr('checked',false)
	}
	if(isServerChecked=='true'){
	  $('#serverLabel').attr('checked',true)
	}else{
	  $('#serverLabel').attr('checked',false)
	}
	if(isFilesChecked=='true'){
      $('#filesLabel').attr('checked',true)
	}else{
	  $('#filesLabel').attr('checked',false)
	}
	if( ! document.implementation.hasFeature("http://www.w3.org/TR/SVG11/feature#BasicStructure", "1.1"))
		$('.tabInner').html('Your browser does not support SVG, see <a href="http://caniuse.com/svg">http://caniuse.com/svg</a> for more details.')
})

function modifyParameter(action, id ){
	listCheck()
	var value = parseFloat($("#"+id).val())
	if(id=='frictionId' && action =='add'){
		if(value==1){
    	    alert("Maximum value for friction should be 1") 
		 }else{
			value += 0.1
			value = value.toPrecision(1)
		 }
	}else if(id=='frictionId' && action =='sub'){
		if(value==0){
	        alert("Minimum value for friction should be 0") 
		 }else{
			value -= 0.1
			value = value.toPrecision(1)
		 }
	}else{
	 var value = parseInt($("#"+id).val())
		if(action=='add'){
			if(id=='linkSizeId'){
				value += 10
			}else{
				value += 100
			}
		} else {
			if(id=='linkSizeId'){
				value -= 10
			}else{
				value -= 100
			}
		}
	}
	$("#"+id).val( value )
	rebuildMap($("#forceId").val(), $("#linkSizeId").val(), $("#frictionId").val(), $("#widthId").val(), $("#heightId").val())
}



function hidePanel(){
	$('#controlPanel').css('display','none')
	$('#legendDivIdGraph').css('display','block')
	$('#legendDivId').css('display','block')
	$('#panelLink').attr('onClick','openPanel()')
}
function openPanel(source){
	if(source == 'control') {
		$('#controlPanel').css('display','block')
		$('#legendDivId').css('display','none')
	}
	if(source == 'legend') {
		$('#controlPanel').css('display','none')
		$('#legendDivId').css('display','block')
	}
	if(source == 'hide') {
		$('#controlPanel').css('display','none')
		$('#legendDivId').css('display','none')
	}
}
function listCheck(){
	var labelsList = {}
	$('#labelTree input[type="checkbox"]').each(function() {
		labelsList[$(this).attr('id')] = $(this).is(':checked');
	});
	return labelsList
}

  $('#forceId').val($('#force').val())
  $('#linksSizeId').val($('#distance').val())
  $('#frictionId').val($('#friction').val())
  $('#heightId').val($('#height').val())
  $('#widthId').val($('#width').val())
  $('#listCheckId').val(listCheck())
  

  $('#tabTypeId').val('graph')
  
</script>
<div class="tabs">
	<g:render template="depConsoleTabs" model="${[entity:entity, stats:stats, dependencyBundle:dependencyBundle]}"/>
	<div class="tabInner">
	<input type="hidden" id="assetTypesId" name="assetType" value="${asset}" />
	<input type="hidden" id="force"  value="${defaults.force}" />
	<input type="hidden" id="distance" value="${defaults.linkSize}" />
	<input type="hidden" id="friction"  value="${defaults.friction}" />
	<input type="hidden" id="height" value="${defaults.height}" />
	<input type="hidden" id="width" value="${defaults.width}" />
	<input type="hidden" id="appChecked" value="${appChecked}" />
	<input type="hidden" id="serverChecked" value="${serverChecked}" />
	<input type="hidden" id="filesChecked" value="${filesChecked}" />
	<input type="hidden" id="listCheckId" value="?" />
	
	<div id="item1" style="float: left;z-index: 10000;">
			 <g:render template="map" model="${pageScope.variables}"/>
			<!--<div id="legendDivIdGraph" style="float: left; border: 1px solid #ccc; margin-left: 3px; margin-top: 3px; width: 178px; background-color: white; position: absolute; display: none;">
				<table id="legendId" cellpadding="0" cellspacing="0" style="margin-left: 5px; border: 0; width: 140px;">
					<tr><td style="padding: 3px 3px;"><h3>Legend</h3></td></tr>
					<tr><td colspan="2"><span style="color: blue;"><h4>Nodes:</h4></span></td></tr>
					<tr>
						<td nowrap="nowrap" ><img src="${resource(dir:'images',file:'iconApp.png')}" height="14" /></td>
						<td><span style="vertical-align: text-top;">Apps</span></td>
					</tr>
					<tr>
						<td nowrap="nowrap" ><img src="${resource(dir:'images',file:'iconServer.png')}" height="14" /></td>
						<td><span style="vertical-align: text-top;">Servers</span></td>
					</tr>
					<tr>
						<td nowrap="nowrap" ><img src="${resource(dir:'images',file:'iconDB.png')}" height="14" /></td>
						<td><span style="vertical-align: text-top;">DB</span></td>
					</tr>
					<tr>
						<td nowrap="nowrap" ><img src="${resource(dir:'images',file:'iconStorage.png')}" height="21" /></td>
						<td><span style="vertical-align: text-top;">Storage</span></td>
					</tr>
					<tr>
						<td nowrap="nowrap" ><img src="${resource(dir:'images',file:'iconNetwork.png')}" height="16" /></td>
						<td><span style="vertical-align: text-top;">Network</span></td>
					</tr>
					<tr><td width="5px" ><hr style="width: 30px;color:rgb(56,56,56);"></hr></td><td>Valid Links</td></tr>
					<tr><td ><hr style="width: 30px;color:red;"></hr></td><td>Questioned</td></tr>
					<tr><td ><hr style="width: 30px;color:rgb(224,224,224);"></hr></td><td>N/A</td></tr>
					<tr><td nowrap="nowrap" colspan="2"><span style="color: Gray;"><h4>Events:</h4></span></td></tr>
					<g:each in="${eventColorCode}" var="color">
						<tr>
						   <td><input type="text" size="1" style="background-color: ${color.value};height:5px;width:5px;" />
							</td>
							<td nowrap="nowrap">
								${color.key}
							</td>
						</tr>
					</g:each>
					<tr><td><input type="text" size="1" style="border: 2px solid red;height:5px;width:5px; " />
						</td>
						<td nowrap="nowrap">No Event</td>
					</tr>
				</table>
			</div>
			<div id="mapResizerId" class="ui-resizable-handle ui-resizable-se ui-icon ui-icon-gripsmall-diagonal-se ui-icon-grip-diagonal-se" style="z-index: 1001;"></div>-->
		</div>
	</div>
</div>


