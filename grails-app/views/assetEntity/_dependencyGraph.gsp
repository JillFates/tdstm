<script type="text/javascript">

var parameterPrecision = {'force':${multiple?10:100}, 'linkSize':10, 'friction':0.1, 'theta':0.1, 'width':100, 'height':100}
var parameterRanges = {'force':[${multiple?-100:-1000}, 0], 'linkSize':[0,1000], 'friction':[0,1], 'theta':[0,1], 'width':[600,100000], 'height':[500,100000]}

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
	
	$('#item1').css('height', $(window).height() - 380);
	$('#height').val($('#item1').innerHeight());
	$('#width').val($('#item1').innerWidth());
	
	if( ! document.implementation.hasFeature("http://www.w3.org/TR/SVG11/feature#BasicStructure", "1.1") )
		$('.tabInner').html('Your browser does not support SVG, see <a href="http://caniuse.com/svg">http://caniuse.com/svg</a> for more details.')
})

// Called when the user clicks the + or - buttons on the control panel
function modifyParameter(action, id ){
	listCheck()
	var value = parseFloat($("#"+id).val())
	var type = id.substring(0, id.length-2)
	
	if (action == 'add')
		value += parameterPrecision[type];
	else if (action == 'sub')
		value -= parameterPrecision[type];
	
	if (parameterPrecision[type] == 0.1)
		value = value.toPrecision(1)
	
	val = value
	var minValue = parameterRanges[type][0]
	var maxValue = parameterRanges[type][1]
	value = Math.min(Math.max(value, minValue), maxValue)
	if (value != val && type != 'force') // let's not show the alert for force
		alert((type.charAt(0).toUpperCase() + type.slice(1)) + " must be between " + minValue + " and " + maxValue)
	
	$("#"+id).val( value )
	rebuildMap($("#forceId").val(), $("#linkSizeId").val(), $("#frictionId").val(), $("#thetaId").val(), $("#widthId").val(), $("#heightId").val())
}



function hidePanel(){
	$('#controlPanel').css('display','none')
	$('#legendDivIdGraph').css('display','block')
	$('#legendDivId').css('display','block')
	$('#panelLink').attr('onClick','openPanel()')
}

function openPanel(source){
	if ( $('#'+source).css('display') == 'block' ) {
		$('#'+source).css('display', 'none')
	} else if (source == 'controlPanel') {
		$('#controlPanel').css('display','block')
		$('#legendDivId').css('display','none')
	} else if (source == 'legendDivId') {
		$('#controlPanel').css('display','none')
		$('#legendDivId').css('display','block')
	} else if (source == 'hide') {
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
function depConsoleLabelUserpref($me,forWhom){
	var isChecked = $me.is(":checked")
    jQuery.ajax({
        url:contextPath+'/assetEntity/setImportPerferences',
        data:{'selected':isChecked, 'prefFor':forWhom}
    });
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
		
		<div id="item1" style="float: left;z-index: 10000; width: 1200px; top: 0px; top: 0px;">
			<g:render template="map" model="${pageScope.variables}"/>
		</div>
	</div>
</div>


