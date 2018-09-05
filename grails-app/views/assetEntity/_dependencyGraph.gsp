<script type="text/javascript">
var parameterPrecision = {'force':${multiple?10:100}, 'linkSize':10, 'friction':0.1, 'theta':0.1};
var parameterRanges = {'force':[${multiple?-500:-1000}, 0], 'linkSize':[0,1000], 'friction':[0,1], 'theta':[0,1]};

$(document).ready(function() {

	// handle detecting whether this graph is being opened in the fullscreen state
	var fullscreen = ${fullscreen};
	if (fullscreen) {
		GraphUtil.enableFullscreen();
	}

	// List of the dependency types and statuses used for the dependency group generation
	GraphUtil.dependencyPanelConfig.dependencyStatus.groupingControl = ${raw(statusTypes)};
	
	GraphUtil.dependencyPanelConfig.dependencyType.groupingControl = ${raw(connectionTypes)};
	
	// figure out which panel should be opened initially
	var showControls = '${showControls ?: ''}';
	GraphUtil.togglePanel('hide');
	GraphUtil.togglePanel(showControls);

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
	var twistieDivs = $('#legendDivId .twistieControlledDiv')
	if(legendTwistiePref && legendTwistiePref.length > 0) {
		legendTwistiePref = legendTwistiePref.split(',');
		for (var i = 0; i < legendTwistiePref.length; ++i) {
			//debugger;
			var pref = legendTwistiePref[i];
			var selectedTwisty = $("#legendDivId #twistieSpanId[groupType="+pref+"]");
			if (selectedTwisty && selectedTwisty.length > 0) {
				$(selectedTwisty).addClass('open').removeClass('closed');
				$('#'+selectedTwisty.attr('for')).css('display','block');
			}
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
					url: "${createLink(mapping: 'wsDepAnalyzer', action: 'peopleAssociatedToDepGroup')}",
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
		placeholder: 'Select App Owner or SME',
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

// --------  DEPENDENCY PANEL
(function($) {
	$.fn.has_scrollbar = function() {
		var divnode = this.get(0);
		if(divnode.scrollHeight > divnode.clientHeight)
			return true;
	}
})(jQuery);
// --------  DEPENDENCY PANEL

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


