
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
	
	
		<div id="item1" style="float: left;z-index: 10000;">
			 <g:render template="map"/>
		</div>
	</div>
</div>
<script type="text/javascript">
$('#appLabel').attr('checked',true)
function increaseValue(action, id ){
	var value = parseFloat($("#"+id).val())
	if(id=='frictionId' && action =='add'){
		value += 0.1
		value = value.toPrecision(2)
	}else if(id=='frictionId' && action =='sub'){
		value -= 0.1
		value = value.toPrecision(2)
	}else{
	 var value = parseInt($("#"+id).val())
		if(action=='add'){
			value += 10
			
		} else {
			value -= 10
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



  $('#forceId').val($('#force').val())
  $('#linksId').val($('#distance').val())
  $('#frictionId').val($('#friction').val())
  $('#heightId').val($('#height').val())
  $('#widthId').val($('#width').val())
  

  $('#tabTypeId').val('graph')
</script>

