<script type="text/javascript">
var parameterPrecision = {'force':${multiple?10:100}, 'linkSize':10, 'friction':0.1, 'theta':0.1, 'width':100, 'height':100}
var parameterRanges = {'force':[${multiple?-500:-1000}, 0], 'linkSize':[0,1000], 'friction':[0,1], 'theta':[0,1], 'width':[600,100000], 'height':[500,100000]}

$(document).ready(function() {
	var fullscreen = ${fullscreen};
	if (fullscreen)
		GraphUtil.enableFullscreen();
	
	var showControls = '${showControls ?: ''}';
	GraphUtil.togglePanel('hide');
	if (showControls == 'controls')
		GraphUtil.togglePanel('control');
	else if (showControls == 'legend')
		GraphUtil.togglePanel('legend');
	
	if( ! document.implementation.hasFeature("http://www.w3.org/TR/SVG11/feature#BasicStructure", "1.1") )
		$('.tabInner').html('Your browser does not support SVG, see <a href="http://caniuse.com/svg">http://caniuse.com/svg</a> for more details.');
	
	$('#layoutControlContainerId').slideUp(0);
})

// Called when the user clicks the + or - buttons on the control panel
function modifyParameter (action, id) {
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
	rebuildMap(true, $("#forceId").val(), $("#linkSizeId").val(), $("#frictionId").val(), $("#thetaId").val(), $("#widthId").val(), $("#heightId").val());
}
function listCheck () {
	var labelsList = {}
	$('table.labelTree input[type="checkbox"]').each(function() {
		labelsList[$(this).attr('id')] = $(this).is(':checked');
	});
	return labelsList
}
function getExpanededLabels () {
	var labelsList = [];
	$('table.labelTree input[type="checkbox"]').each(function(i, o) {
		$(o.classList).each(function(i, c) {
			labelsList[c] = $(o).is(':checked');
		});
	});
	return labelsList;
}
function depConsoleLabelUserpref ($me,forWhom) {
	var isChecked = $me.is(":checked")
	jQuery.ajax({
		url:contextPath+'/assetEntity/setImportPerferences',
		data:{'value':isChecked, 'preference':forWhom}
	});
}
$('#tabTypeId').val('graph')
  
</script>
<div class="tabs">
	<g:render template="depConsoleTabs" model="${[entity:entity, stats:stats, dependencyBundle:dependencyBundle]}"/>
	<div class="tabInner">
		<input type="hidden" id="assetTypesId" name="assetType" value="${asset}" />
		<div id="item1" style="float: left;" class="graphContainer">
			<g:render template="map" model="${pageScope.variables}"/>
		</div>
	</div>
</div>


