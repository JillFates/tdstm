
<div class="tabs">
	<ul>
		<li id="appli">
			<a href="javascript:getList('Apps',${dependencyBundle})">Apps(${appDependentListSize})</a>
		</li>
		<li id="serverli">
			<a href="javascript:getList('server',${dependencyBundle})"><span>Servers(${assetEntityListSize})</span> </a>
		</li>
		<li id="dbli"><a href="#item3">
			<a href="javascript:getList('database',${dependencyBundle})">DB(${dbDependentListSize})</a>
		</li>
		<li id="fileli">
			<a href="javascript:getList('files',${dependencyBundle})">Files(${filesDependentListSize})</a>
		</li>
		<li id="graphli" class="active">
			<a href="javascript:getList('graph',${dependencyBundle})">Map</a>
		</li>
	</ul>
	<div class="tabInner">
	<input type="hidden" id="assetTypesId" name="assetType" value="${asset}" />
	<input type="hidden" id="force"  value="${force}" />
	<input type="hidden" id="distance" value="${distance}" />
	<input type="hidden" id="friction"  value="${friction}" />
	<input type="hidden" id="height" value="${height}" />
	<input type="hidden" id="width" value="${width}" />
	<input type="hidden" id="appChecked" value="${appChecked}" />
	<input type="hidden" id="serverChecked" value="${serverChecked}" />
	<input type="hidden" id="filesChecked" value="${filesChecked}" />
	
	
	
		<div id="item1" style="float: left;z-index: 10000;">
			 <g:render template="map"/>
		</div>
	</div>
</div>
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
})
function increaseValue(action, id ){
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
			if(id=='heightId' || id=='widthId'){
				value += 100
			}else{
				value += 10
			}
		} else {
			if(id=='heightId' || id=='widthId'){
				value -= 100
			}else{
				value -= 10
			}
		}
	}
	$("#"+id).val( value )
}



function hidePanel(){
	$('#controlPanel').css('display','none')
	$('#panelLink').attr('onClick','openPanel()')
}
function openPanel(){
	$('#controlPanel').css('display','block')
	$('#panelLink').attr('onClick','hidePanel()')
}
function listCheck(){
	var labelsList = " "
	$('#labelTree input:checked').each(function() {
		labelsList += $(this).val()+',';
	});
	$('#labelsList').val(labelsList)
}

  $('#forceId').val($('#force').val())
  $('#linksId').val($('#distance').val())
  $('#frictionId').val($('#friction').val())
  $('#heightId').val($('#height').val())
  $('#widthId').val($('#width').val())
  

  $('#tabTypeId').val('graph')
</script>

