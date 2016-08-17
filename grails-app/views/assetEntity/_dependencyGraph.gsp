<script type="text/javascript">
var parameterPrecision = {'force':${multiple?10:100}, 'linkSize':10, 'friction':0.1, 'theta':0.1};
var parameterRanges = {'force':[${multiple?-500:-1000}, 0], 'linkSize':[0,1000], 'friction':[0,1], 'theta':[0,1]};

$(document).ready(function() {
	
	// handle detecting whether this graph is being opened in the fullscreen state
	var fullscreen = ${fullscreen};
	if (fullscreen)
		GraphUtil.enableFullscreen();
	
	// figure out which panel should be opened initially
	var showControls = '${showControls ?: ''}';
	GraphUtil.togglePanel('hide');
	if (showControls == 'controls')
		GraphUtil.togglePanel('control');
	else if (showControls == 'legend')
		GraphUtil.togglePanel('legend');
	
	// if the browser doesn't support svg display a message instead of the graph
	if( ! document.implementation.hasFeature("http://www.w3.org/TR/SVG11/feature#BasicStructure", "1.1") )
		$('.tabInner').html('Your browser does not support SVG, see <a href="http://caniuse.com/svg">http://caniuse.com/svg</a> for more details.');
	
	// initially close the control panel twisties
	$('#layoutControlContainerId').slideUp(0);
	$('#labelControlContainerId').slideUp(0);
	GraphUtil.checkForDisabledButtons(parameterRanges);
	
	// enable the bootstrap popover tooltips in the legend
	$('.toolTipButton').popover()
	
	// handle applying the legend twistie preferences
	var legendTwistiePref = '${legendTwistiePref}'
	var twisties = $('#legendDivId #twistieSpanId')
	var twistieDivs = $('#legendDivId .twistieControlledDiv')
	for (var i = 0; i < legendTwistiePref.length; ++i) {
		var pref = legendTwistiePref[i]
		if (pref == '0' && twisties.length > i) {
			$(twisties[i]).addClass('closed').removeClass('open')
			$(twistieDivs[i]).css('display','none')
		}
	}
	
	// add bindings to the filtering selects to set their corresponding radio buttons when clicked on
	$('#filterOptionsMenuId .optionRow select').click(function () {
		$(this).parent().parent().children('input').attr('checked', true)
	})
	
	// define the parameters for the kendo combobox
	var comboBoxParams = {
		animation: {
			close: { duration: 100 },
			open: { duration: 100 }
		},
		dataTextField: "name",
		dataValueField: "personId",
		dataSource: {
			transport: {
				read: {
					url: "/tdstm/assetEntity/getDepGraphFilterPeople",
					dataType: "json",
					data: {
						depGroup: '${depGroup}'
					}
				}
			}
		},
		delay: 0,
		filter: "contains",
		height: 400,
		placeholder: 'Select SME/App Owner',
		change: function () {
			GraphUtil.performSearch()
		}
	};
	
	// create the combobox
	var comboBox = $('#personHighlightSelectId').kendoComboBox(comboBoxParams).data("kendoComboBox");
	
	// bind focussing on the combobox to openning the dropdown
	$('#highlightPersonId input.k-input').focus(function () {
		comboBox.open();
	});
});

// Called when the user clicks the + or - buttons on the control panel
function modifyParameter (action, id) {
	var input = $("#" + id);
	var value = parseFloat(input.val());
	var type = input.attr('name');
	var plusButton = input.parent().children('.plus');
	var minusButton = input.parent().children('.minus');
	
	if (action == 'add')
		value += parameterPrecision[type];
	else if (action == 'sub')
		value -= parameterPrecision[type];
	
	if (parameterPrecision[type] == 0.1)
		value = value.toPrecision(1);
	
	var minValue = parameterRanges[type][0];
	var maxValue = parameterRanges[type][1];
	value = Math.min(Math.max(value, minValue), maxValue);
	
	if (value == minValue)
		minusButton.addClass('disabled');
	else
		minusButton.removeClass('disabled');
	if (value == maxValue)
		plusButton.addClass('disabled');
	else
		plusButton.removeClass('disabled');
	
	if (action != 'none') {
		input.val(value);
		rebuildMap(true, $("#forceId").val(), $("#linkSizeId").val(), $("#frictionId").val(), $("#thetaId").val(), $("#widthId").val(), $("#heightId").val());
	}
}

function listCheck () {
	var labelsList = {}
	$('table.labelTree input[type="checkbox"]').each(function() {
		labelsList[$(this).attr('id')] = $(this).is(':checked');
	});
	return labelsList
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


