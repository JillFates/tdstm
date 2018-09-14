
// global functions for accessing the graph outside the scope of the main function
var getData = function () { return null }
var forceDisplay = function () { return null }
var debug = {};

$(document).ready(function () {
	// check keyup on the search field for the enter key
	$('#searchBoxId').keyup(function (e) {
		if (e.keyCode == 13)
			$('#SubmitButtonId').submit();
	});

	$('#searchBoxId').on('keyup', handleClearFilterStatus);

	// disable the default option for the event select
	$('#moveEventId').on('change', function () {
		$(this).children().first().attr('disabled', 'disabled');
	});

	generateGraph();
});


function displayWarningOrErrorMsg(isCyclical) {
	var message = d3.select('div.body')
		.append('div')
		.attr('class', 'chart');
	if (isCyclical)
		message.html('The task data for this event contains cyclical dependency mappings that thereby preventing graph generation');
	else
		message.html('<br />There is insufficient task data to create a graph for this event');
	$('#spinnerId').css('display', 'none');
	// disable export button
	$('#exportCriticalPathButton').addClass('disabledLabel');
}

/**
 * Generate the TimeLine based on the response data
 * @param response
 * @param status
 */
function buildGraph(response, status) {

	// show the loading spinner
	$('#spinnerId').css('display', 'block');

	// restore export button
	$('#exportCriticalPathButton').removeClass('disabledLabel');

	// check for errors in the ajax call
	if (status == 'error') {
		displayWarningOrErrorMsg(response.responseText == 'cyclical')
		return;
	}

	// parse the data received from the server
	var data = $.parseJSON(response.responseText);
	data = data.data;
	var ready = false;

	// if the event has no tasks show an error message and exit
	if (data.items.size() == 0) {
		displayWarningOrErrorMsg(false)
		return;
	}

	// graph default config
	var miniRectStroke = 1.0;
	var miniRectHeight = 7 - miniRectStroke;
	var mainRectHeight = 30;
	var backgroundExtraHeight = 200;
	var initialExtent = 1000000;
	var anchorOffset = 10; // the length of the "point" at the end of task polygons
	var margin = { top: 20, right: 0, bottom: 15, left: 0 };
	var minExtentRange = 10;
	var labelFloatOffset = 1;
	var labelPadding = 10;
	var scrollingLabelsPerformanceCutoff = 50;

	// data received from the server
	var items = data.items;
	var starts = data.starts;
	var dependencies = [];
	var siblingGroups = [];
	var siblingGroupsReduced = [];
	var parsedHeight = parseInt($('#mainHeightFieldId').val());
	if (!isNaN(parsedHeight))
		mainRectHeight = parsedHeight;

	// data from current user config
	var hideRedundant = $('#hideRedundantCheckBoxId').is(':checked');
	var highlightCritical = $('#highlightCriticalPathId').is(':checked');
	var useHeights = $('#useHeightCheckBoxId').is(':checked');
	var taskSelected = null;

	// perform all necesary precalculations on the data
	sanitizeData(items, dependencies);

	// sort the tasks chronologically for the stacking algorithm
	items.sort(function (a, b) {
		var t1 = a.start ? (new Date(a.start)).getTime() : 0;
		var t2 = b.start ? (new Date(b.start)).getTime() : 0;
		if (t1 > t2)
			return 1;
		else if (t1 < t2)
			return -1;
		else if (a.milestone && !b.milestone)
			return 1;
		else if (!a.milestone && b.milestone)
			return -1;
		else if ((a.predecessors.length + a.successors.length) < (b.predecessors.length + b.successors.length))
			return 1;
		else if ((a.predecessors.length + a.successors.length) > (b.predecessors.length + b.successors.length))
			return -1;
		else if (a.predecessors.length < b.predecessors.length)
			return 1;
		else if (a.predecessors.length > b.predecessors.length)
			return -1;
		else
			return 0;
	});

	// set up the ranges for the mini and main graphs
	var windowWidth = $(window).width(); // the width of the browser window
	var graphPageOffset = $('div.body').offset().left; // the left offset of the graph on the page
	var graphExtraPadding = 10; // extra padding width for the graph on the page
	var zoomScale = 2;
	var d3Linear = getTimeFormatToDraw(parseStartDate(data.startDate), items[items.length - 1].end);
	var x = d3.time.scale()
		.domain([parseStartDate(data.startDate), items[items.length - 1].end])
		.range([0, windowWidth - graphPageOffset * 2 - graphExtraPadding]);
	var x1 = d3.time.scale()
		.domain(x.domain())
		.range([0, x.range()[1] * zoomScale]);

	// perform the stacking layout algorithm then determine the width and height of the graphs
	var maxStack = calculateStacks();
	var mainHeight = ((maxStack + 1) * mainRectHeight * 1.5);
	var miniHeight = ((maxStack + 1) * miniRectHeight) + miniRectStroke;
	if (miniHeight <= 10) {
		miniHeight = 100;
		miniRectHeight = 100;
	}
	var height = mainHeight + miniHeight + margin.top + margin.bottom;
	var width = x.range()[1];

	// predeclarations of graph elements
	var svgContainer = null;
	var chart = null;
	var main = null;
	var mini = null;
	var brushContainer = null;

	// populate the Team select
	var teamSelect = null;
	populateTeamSelect();


	// stores data and functions used for dragging on the mini graph
	var miniDrag = {
		tempBrush: null,
		tempBrushXInitial: 0,
		drawing: 0,

		// handle panning and time selection behavior on the mini graph
		drawStart: function () {
			miniDrag.tempBrushXInitial = d3.mouse(chart.node())[0] - margin.left;

			// if we are outside the brush, we are drawing a new region or setting a new brush position
			if ((d3.event.sourceEvent.which == 1) && (x.invert(miniDrag.tempBrushXInitial + 4) < brush.extent()[0] || x.invert(miniDrag.tempBrushXInitial - 4) > brush.extent()[1])) {
				miniDrag.drawing = 1;
				miniDrag.tempBrush = brushContainer.append('svg:rect')
					.attr('class', 'tempBrush hidden')
					.attr('width', 0)
					.attr('height', miniHeight)
					.attr('x', d3.mouse(chart.node())[0] - margin.left)
					.attr('y', 0);

				// otherwise we are panning the brush
			} else {
				mini.classed('brushMoving', true)
				miniDrag.tempBrushXInitial = 0;
				miniDrag.drawing = 0;
			}
		},

		// handle panning and time selection behavior on the mini graph
		drawMove: function () {
			if (miniDrag.drawing == 1) {
				miniDrag.tempBrush.classed('hidden', false);
				miniDrag.tempBrush
					.attr('x', Math.min(miniDrag.tempBrushXInitial, d3.mouse(chart.node())[0] - margin.left))
					.attr('width', Math.abs(miniDrag.tempBrushXInitial - (d3.mouse(chart.node())[0] - margin.left)));
				display(false, false);
				chart.classed('resizing', true);
			} else {
				if (miniDrag.tempBrush != null)
					miniDrag.tempBrush.remove();
				miniDrag.tempBrush = null;
				miniDrag.tempBrushXInitial = 0;
				chart.classed('resizing', false);
			}
		},

		// handle panning and time selection behavior on the mini graph
		drawEnd: function () {
			if (miniDrag.drawing == 1) {
				var xStart = miniDrag.tempBrushXInitial
				var xEnd = d3.mouse(chart.node())[0] - margin.left
				var xa = Math.min(xStart, xEnd);
				var xb = Math.max(xStart, xEnd);

				// if the user clicked, move the current brush to that position
				if (xb - xa < 6) {
					var width = x(brush.extent()[1]) - x(brush.extent()[0])
					xa = xa - width / 2
					xb = xa + width
				}

				brush.extent([x.invert(xa), x.invert(xb)]);
				miniDrag.tempBrush.remove();
				miniDrag.tempBrush = null;
				miniDrag.tempBrushXInitial = 0;
				display(true, true);
			}
			mini.classed('brushMoving', false)
			miniDrag.drawing = 0;
			chart.classed('resizing', false);
			display(false, false);
			chart[0][0].style.webkitTransform = 'scale(' + (1 + Math.random() / 1000000) + ')';
		}
	}

	// Sets the custom dragging behavior
	miniDrag.behavior = d3.behavior.drag()
		.on("dragstart", miniDrag.drawStart)
		.on("drag", miniDrag.drawMove)
		.on("dragend", miniDrag.drawEnd);

	// stores data and functions used for dragging on the main graph
	var mainDrag = {
		mainSelection: null,
		mainSelectionXInitial: 0,
		selectingMain: false,
		dragging: false,

		// handle panning and time selection behavior on the main graph
		dragStart: function () {
			mainDrag.dragging = false;
			if (d3.event.sourceEvent.shiftKey) {
				mainDrag.selectingMain = true;
				mainDrag.mainSelectionXInitial = d3.mouse(chart.node())[0] - margin.left;
				mainDrag.mainSelection = main.append('svg:rect')
					.attr('class', 'tempBrush')
					.attr('width', 0)
					.attr('height', mainHeight)
					.attr('x', mainDrag.mainSelectionXInitial)
					.attr('y', 0);
			}
		},

		// handle panning and time selection behavior on the main graph
		dragMove: function () {
			if (Math.abs(d3.event.dx) > 1)
				mainDrag.dragging = true;
			if (mainDrag.selectingMain) {
				mainDrag.mainSelection
					.attr('x', Math.min(mainDrag.mainSelectionXInitial, d3.mouse(chart.node())[0] - margin.left))
					.attr('width', Math.abs(mainDrag.mainSelectionXInitial - (d3.mouse(chart.node())[0] - margin.left)));
				if (mainDrag.dragging)
					chart.classed('resizing', true);
			} else {
				var minExtent = brush.extent()[0];
				var maxExtent = brush.extent()[1];
				var timeShift = (maxExtent - minExtent) * (d3.event.dx / width);
				brush.extent([new Date(minExtent - timeShift), new Date(maxExtent - timeShift)]);
				display(false, false);
				if (mainDrag.dragging)
					chart.classed('dragging', true);
			}
		},

		// handle panning and time selection behavior on the main graph
		dragEnd: function () {
			if (mainDrag.selectingMain) {
				var a = Math.min(mainDrag.mainSelectionXInitial, d3.mouse(chart.node())[0] - margin.left);
				var b = Math.abs(mainDrag.mainSelectionXInitial - (d3.mouse(chart.node())[0] - margin.left)) + Math.min(mainDrag.mainSelectionXInitial, d3.mouse(chart.node())[0] - margin.left);
				var extentWidth = x(brush.extent()[1]) - x(brush.extent()[0]);
				var width = x.range()[1];
				var xa = x(brush.extent()[0]) + (a / width) * extentWidth;
				var xb = x(brush.extent()[0]) + (b / width) * extentWidth;
				brush.extent([x.invert(xa), x.invert(xb)]);

				mainDrag.selectingMain = false;

				mainDrag.mainSelection.remove();
				mainDrag.mainSelection = null;
				mainDrag.mainSelectionXInitial = 0;
				display(true, true);
				display(true, true);
			} else {
				display(false, false);
				if (GraphUtil.isIE()) {
					calculateLabelVisWidths();
				}
				correctLabelPositions();
				chart[0][0].style.webkitTransform = 'scale(' + (1 + Math.random() / 100000) + ')';
			}
			chart.classed('dragging', false);
			chart.classed('resizing', false);
		}
	}

	// Sets the custom dragging behavior
	mainDrag.behavior = d3.behavior.drag()
		.on("dragstart", mainDrag.dragStart)
		.on("drag", mainDrag.dragMove)
		.on("dragend", mainDrag.dragEnd);


	// gets the container that will hold the svg
	svgContainer = d3.select('div#svgContainerId')
		.style('display', null)

	// construct the SVG
	chart = svgContainer
		.append('svg:svg')
		.attr('id', 'timelineSVGId')
		.attr('width', width + margin.right + margin.left)
		.attr('height', height + margin.top + margin.bottom + 20)
		.attr('class', 'chart unselectable');

	// create the defs section
	chart.append('svg:defs')
		.append('svg:clipPath')
		.attr('id', 'clip')
		.append('svg:rect')
		.attr('width', width)
		.attr('height', mainHeight);

	// construct the darkening filter for critical path highlighting
	chart.select('defs')
		.append('svg:filter')
		.attr('id', 'criticalFilterId')
		.append('svg:feColorMatrix')
		.attr('type', 'matrix')
		.attr('values', '\
			0.7 0   0   0   0\
			0   0.7 0   0   0\
			0   0   0.7 0   0\
			0   0   0   1   0');

	// construct the light filter for hovering over tasks
	chart.select('defs')
		.append('svg:filter')
		.attr('id', 'hoverFilterId')
		.append('svg:feColorMatrix')
		.attr('type', 'matrix')
		.attr('values', '\
			1.2 0   0   0   0\
			0   1.2 0   0   0\
			0   0   1.2 0   0\
			0   0   0   1   0');




	// construct the mini graph
	mini = chart.append('svg:g')
		.attr('transform', 'translate(' + margin.left + ',' + margin.top + ')')
		.attr('width', width)
		.attr('class', 'mini')
		.attr('height', miniHeight)
		.call(miniDrag.behavior)

	// construct the main graph
	main = chart.append('svg:g')
		.attr('transform', 'translate(' + margin.left + ',' + (miniHeight + 60) + ')')
		.attr('width', width)
		.attr('height', mainHeight)
		.attr('class', 'main')
		.append('svg:g')
		.attr('class', 'mainContent')
		.call(mainDrag.behavior)

	// construct the area behind the objects in the main graph
	var background = main.append('svg:rect')
		.attr('width', width)
		.attr('height', mainHeight + backgroundExtraHeight)
		.attr('class', 'background')

	// construct the main graph translator
	mainTranslator = main.append('svg:g')
		.attr('transform', 'translate(' + 0 + ',' + 0 + ')')
		.attr('id', 'mainTranslatorId')



	// define the arrowhead marker used for marking dependencies
	var arrowheadNames = ["arrowhead", "arrowheadSelected", "arrowheadCritical", "arrowheadRedundant", "arrowheadCyclical"];
	for (var i = 0; i < arrowheadNames.size(); ++i)
		chart.select("defs")
			.append("svg:marker")
			.attr("id", arrowheadNames[i])
			.attr("viewBox", "0 -5 10 10")
			.attr("refX", 10)
			.attr("refY", 0)
			.attr("markerWidth", 6)
			.attr("markerHeight", 6)
			.attr("orient", "auto")
			.append("svg:path")
			.attr("d", "M0,-5L10,0L0,5");




	// stores the internal d3 definitions for the axis
	var axisDefs = {
		// construct the minutes axis for the mini graph
		xMinuteAxis: d3.svg.axis()
			.scale(x)
			.orient('bottom')
			.ticks(d3Linear.time, d3Linear.tick)
			.tickFormat(d3Linear.format)
			.tickSize(6, 0, 0),

		// construct the hours axis for the mini graph
		xHourAxis: d3.svg.axis()
			.scale(x)
			.orient('top')
			.ticks(d3Linear.time, d3Linear.tick)
			.tickFormat(d3Linear.format)
			.tickSize(6, 0, 0),

		// construct the hours axis for the main graph
		x1MainGraphAxis: d3.svg.axis()
			.scale(x1)
			.orient('top')
			.ticks(d3Linear.time, d3Linear.tick)
			.tickFormat(d3Linear.format)
			.tickSize(6, 0, 0)
	}

	// stores the svg elements for the graph axis
	var axisElements = {
		mainMinuteAxis: mainTranslator.append('svg:g')
			.attr('transform', 'translate(0,0.5)')
			.attr('class', 'main axis minute')
			.call(axisDefs.x1MainGraphAxis),

		miniMinuteAxis: mini.append('svg:g')
			.attr('transform', 'translate(0,' + miniHeight + ')')
			.attr('class', 'axis minute')
			.call(axisDefs.xMinuteAxis),

		miniHourAxis: mini.append('svg:g')
			.attr('transform', 'translate(0,0.5)')
			.attr('class', 'axis hour')
			.call(axisDefs.xHourAxis)
	}


	// construct the line representing the current time
	var mainNowLine = mainTranslator.append('svg:line')
		.attr('y1', 0)
		.attr('y2', mainHeight)
		.attr('class', 'main todayLine')

	var miniNowLine = mini.append('svg:line')
		.attr('x1', x(now()) + 0.5)
		.attr('y1', 0)
		.attr('x2', x(now()) + 0.5)
		.attr('y2', miniHeight)
		.attr('class', 'todayLine');

	// shift the today line every 3000 miliseconds
	d3.timer(function () {
		mainNowLine
			.attr('x1', x1(now()) + 0.5)
			.attr('x2', x1(now()) + 0.5);
		miniNowLine
			.attr('x1', x(now()) + 0.5)
			.attr('x2', x(now()) + 0.5);
	}, 3000);

	// construct the container for the task polygons
	var itemPolys = mainTranslator.append('svg:g')

	// construct the container for the dependency lines
	var itemArrows = mainTranslator.append('svg:g')

	// construct the container for the task labels
	var itemLabels = mainTranslator.append('svg:g')

	// construct the polys in the mini graph
	var miniPolys = mini.append('svg:g')
		.attr('class', 'miniItems')
		.selectAll('polygon')
		.data(items, function (d) { return d.id; })
		.enter()
		.append('svg:polygon')
		.attr('id', function (d) { return 'mini-' + d.id; })
		.attr('class', 'miniItem')
		.attr('points', function (d) { return getPointsMini(d); });


	// invisible hit area to move around the selection window
	mini.append('svg:rect')
		.attr('id', 'miniBackgroundId')
		.attr('pointer-events', 'painted')
		.attr('width', width)
		.attr('height', miniHeight)
		.attr('visibility', 'hidden')
		.on('mouseup', moveBrush);


	// draw the selection area
	var brush = d3.svg.brush()
		.x(x)
		.on("brush", brushed)
		.on("brushend", brushedEnd)
		.extent([parseStartDate(data.startDate), new Date(Math.min(parseStartDate(data.startDate).getTime() + d3Linear.zoomTime, x.domain()[1].getTime()))])

	var extentWidth = x(brush.extent()[1]) - x(brush.extent()[0]);
	zoomScale = extentWidth / width;
	x1.range([0, width / zoomScale]);

	// construct the container for the brush
	brushContainer = mini.append('svg:g')
		.attr('class', 'brushContainer');

	// construct the mini graph brush
	brushContainer.append('svg:g')
		.attr('class', 'x brush')
		.call(brush)
		.on("mousedown", function () { return })
		.call(d3.behavior.drag())
		.selectAll('rect')
		.attr('y', 1)
		.attr('height', miniHeight - 1);

	mini.selectAll('rect.background').remove()



	// setup all the heights to fit to the rect height
	setupHeights(mainRectHeight);

	$('#spinnerId').css('display', 'none');
	var offsetInitial = -1 * x(brush.extent()[0]);
	ready = true;
	display(true, true);
	display(true, true);

	// add the key listeners to the graph
	addKeyBindings();
	GraphUtil.addTimelineKeyListeners(brush, x1, mainTranslator, function (resize) {
		display(false, resize, true);
		if (GraphUtil.isIE())
			calculateLabelVisWidths();
		correctLabelPositions();
		display(false, resize, true);
	});
	window.scrollTo(0, 0);

	// add references to internal values to the debug object
	debug.x = x
	debug.x1 = x1
	debug.brush = brush


	/* define the global accessor functions */
	getData = function () {
		return data;
	}

	forceDisplay = function () {
		display(false, true);
	}

	function addKeyBindings() {
		// handle events from the task height text box
		$('#mainHeightFieldId').on('change', function () {
			var parsedHeight = parseInt($(this).val());
			if (parsedHeight != mainRectHeight)
				setupHeights(parsedHeight);
		})
		$('#mainHeightFieldId').on('keyup', function (e) {
			var parsedHeight = parseInt($(this).val());
			if ((e.keyCode == 13) && (parsedHeight != mainRectHeight))
				setupHeights(parsedHeight);
		})

		// handle the checkbox to toggle hiding of redundant dependencies
		$('#hideRedundantCheckBoxId').on('change', function (a, b) {
			hideRedundant = $(this).is(':checked');
			fullRedraw();
		});

		// handle the checkbox to toggle highlighting of critical path tasks
		$('#highlightCriticalPathId').on('change', function (a, b) {
			highlightCritical = $(this).is(':checked');
			display(false, true);
		});

		// handle the checkbox to toggle using task heights
		$('#useHeightCheckBoxId').on('change', function (a, b) {
			useHeights = $(this).is(':checked');
			fullRedraw();
		});

		// handle when the user changes the team filtering select
		teamSelect.on('change', function () {
			display(true, true);
		});

		// bind the zoom button listeners
		$('#zoomInButtonId').on('click', function () {
			GraphUtil.timelineZoom(brush, 'in', zoomCallback)
		})
		$('#zoomOutButtonId').on('click', function () {
			GraphUtil.timelineZoom(brush, 'out', zoomCallback)
		})

		function zoomCallback() {
			if (GraphUtil.isIE())
				calculateLabelVisWidths();
			correctLabelPositions();
			display(false, true, true);
			display(false, true, true);
		}
	}

	// populate the team select
	function populateTeamSelect() {
		teamSelect = $("#teamSelectId")
		teamSelect.children().remove();
		teamSelect.append('<option value="ALL">All Teams</option>');
		teamSelect.append('<option value="NONE">No Team Assignment</option>');
		teamSelect.append('<option disabled>──────────</option>');

		$.each(data.roles, function (index, team) {
			teamSelect.append('<option value="' + team + '">' + team + '</option>');
		});

		teamSelect.val('ALL');
	}

	// called when the brush is dragged
	function brushed() {
		var brushRange = Math.abs(brush.extent()[1] - brush.extent()[0]);
		var minRange = Math.abs(x.invert(minExtentRange) - x.invert(0));
		if (brushRange < minRange)
			brush.extent([brush.extent()[0], new Date(brush.extent()[0].getTime() + minRange)]);

		miniDrag.drawing = -1;
		if (d3.event.mode == "move") {
			display(false, false);
		} else {
			display(false, true);
		}
	}

	// called when the brush is released
	function brushedEnd() {
		correctLabelPositions();
	}

	// updates the svg
	function display(resized, scaled, repainting) {
		if (!ready)
			return;

		var startTime = 0;
		if (window.performance && window.performance.now)
			startTime = performance.now();

		var polys, labels, lines;
		var minExtent = brush.extent()[0];
		var maxExtent = brush.extent()[1];
		var visItems = items;
		var visDeps = dependencies;


		mini.select('.brush').call(brush.extent([minExtent, maxExtent]));
		var offset = -1 * x1(brush.extent()[0]) - offsetInitial;

		mainTranslator.attr('transform', 'translate(' + offset + ', 0)');

		if ((!scaled) && (!repainting))
			return;

		if (scaled) {
			var extentWidth = x(brush.extent()[1]) - x(brush.extent()[0]);
			zoomScale = extentWidth / width;
			x1.range([0, width / zoomScale]);

			// update the scales for the axis

			var innerTimeLine = getTimeFormatToDraw(parseStartDate(brush.extent()[0]), parseStartDate(brush.extent()[1]), true);
			axisDefs.x1MainGraphAxis.ticks(innerTimeLine.time, innerTimeLine.tick);
			axisDefs.x1MainGraphAxis.tickFormat(innerTimeLine.format);

			axisDefs.x1MainGraphAxis.scale(d3.time.scale().domain(x1.domain()).range(x1.range()));
			axisElements.mainMinuteAxis.call(axisDefs.x1MainGraphAxis);

			$.each(items, function (i, d) {
				d.points = getPoints(d);
			});
		}

		// update any existing item polys
		polys = itemPolys.selectAll('polygon')
			.data(visItems, function (d) { return d.id; });

		if (scaled)
			polys.attr('points', function (d) {
				var p = d.points;
				return p.x + ',' + p.y + ' '
					+ p.x2 + ',' + p.y + ' '
					+ (p.x + p.w) + ',' + p.y2 + ' '
					+ p.x2 + ',' + (p.y + p.h) + ' '
					+ p.x + ',' + (p.y + p.h) + ' ';
			})
		polys.attr('class', function (d) { return 'mainItem ' + getClasses(d); });

		// add any task polys in the new domain extents
		polys.enter()
			.append('svg:polygon')
			.attr('points', function (d) {
				var p = d.points;
				return p.x + ',' + p.y + ' '
					+ p.x2 + ',' + p.y + ' '
					+ (p.x + p.w) + ',' + p.y2 + ' '
					+ p.x2 + ',' + (p.y + p.h) + ' '
					+ p.x + ',' + (p.y + p.h) + ' ';
			})
			.attr('class', function (d) { return 'mainItem ' + getClasses(d); })
			.attr('id', function (d) { return 'task-' + d.id; })
			.on("click", function (d) {
				toggleTaskSelection(d);
			})
			.on("dblclick", function (d) {
				showAssetComment(d.id, 'show');
			})
			.append('svg:title')
			.html(function (d) {
				return d.number
					+ ': ' + d.name
					+ ' - ' + ((d.assignedTo != 'null') ? (d.assignedTo) : ('Unassigned'))
					+ ' - ' + d.status;
			});

		polys.exit().remove();

		// update any existing dependency lines
		lines = itemArrows.selectAll('line')
			.data(visDeps, function (d) { return d.predecessor.id + '-' + d.successor.id; });

		if (scaled)
			lines
				.attr('x1', function (d) {
					var start = x1(d.predecessor.start);
					var end = x1(d.predecessor.end);
					var lowest = start + (end - start) * 0.75;
					return Math.round(Math.max(lowest, end - anchorOffset));
				})
				.attr('x2', function (d) {
					var start = x1(d.successor.start);
					var end = x1(d.successor.end);
					var highest = start + (end - start) * 0.25;
					return Math.round(Math.min(highest, start + anchorOffset));
				})

		if (resized)
			lines
				.attr('y1', function (d) { return d.predecessor.points.y2; })
				.attr('y2', function (d) { return d.successor.points.y2; })
		lines.attr('class', function (d) { return 'dependency mainItem ' + getClasses(d); });

		// add any dependency lines in the new domain extents
		lines.enter().insert('svg:line')
			.attr('x1', function (d) {
				var start = x1(d.predecessor.start);
				var end = x1(d.predecessor.end);
				var lowest = start + (end - start) * 0.75;
				return Math.round(Math.max(lowest, end - anchorOffset));
			})
			.attr('x2', function (d) {
				var start = x1(d.successor.start);
				var end = x1(d.successor.end);
				var highest = start + (end - start) * 0.25;
				return Math.round(Math.min(highest, start + anchorOffset));
			})
			.attr('y1', function (d) { return d.predecessor.points.y2; })
			.attr('y2', function (d) { return d.successor.points.y2; })
			.attr('id', function (d) { return 'dep-' + d.predecessor.id + '-' + d.successor.id; })
			.attr('class', function (d) { return 'dependency mainItem ' + getClasses(d); });


		// move any selected lines to the top of the DOM
		if (taskSelected != null)
			lines.sort(function (a, b) { return a.selected - b.selected; });
		lines.exit().remove();

		if (scaled || resized)
			GraphUtil.forceReflow(itemArrows)


		if (GraphUtil.isIE()) {

			// update the item labels
			labels = itemLabels.selectAll('text')
				.data(visItems, function (d) { return d.id; })

			if (scaled)
				labels
					.attr('x', function (d) { return d.points.x; })
					.attr('width', function (d) { return Math.max(0, d.points.w - anchorOffset); })

			if (resized)
				labels
					.attr('y', function (d) { return d.points.y2 + 3; })
					.attr('height', function (d) { return d.points.h; })
			labels.attr('class', function (d) { return 'itemLabel unselectable mainItem ' + getClasses(d); });

			// update the item labels' children
			itemLabels.selectAll('text')

			// add any labels in the new domain extents
			labels.enter()
				.append('svg:g')
				.attr('class', 'itemLabel')
				.append('text')
				.attr('class', function (d) { return 'itemLabel unselectable mainItem ' + getClasses(d); })
				.attr('id', function (d) { return 'label-' + d.id; })
				.attr('x', function (d) { return d.points.x; })
				.attr('y', function (d) { return d.points.y2 + 3; })
				.attr('width', function (d) { return Math.max(0, d.points.w - anchorOffset); })
				.attr('height', function (d) { return d.points.h; })
				.text(function (d) { return d.number + ': ' + d.name; });


			if (scaled || resized)
				GraphUtil.forceReflow(itemLabels)

			labels.exit().remove();
		} else {

			// update the item labels
			labels = itemLabels.selectAll(function () { return this.getElementsByTagName("foreignObject"); })
				.data(visItems, function (d) { return d.id; });

			if (scaled)
				labels
					.attr('x', function (d) { return d.points.x; })
					.attr('width', function (d) { return Math.max(0, d.points.w - anchorOffset); });

			if (resized)
				labels
					.attr('y', function (d) { return d.points.y; })
					.attr('height', function (d) { return d.points.h; })
			labels.attr('class', function (d) { return 'itemLabel unselectable mainItem ' + getClasses(d); });

			// update the item labels' children
			itemLabels.selectAll(function () { return this.getElementsByTagName("foreignObject"); })
				.select(':first-child')
				.attr('style', function (d) { return 'height: ' + d.points.h + 'px !important;max-width: ' + Math.max(0, d.points.w - anchorOffset) + 'px !important;'; })
				.select(':first-child')
				.select(':first-child')
				.attr('style', function (d) { return 'width: ' + (d.points.w - anchorOffset) + 'px !important;max-width: ' + Math.max(0, d.points.w - anchorOffset) + 'px !important;'; });

			// add any labels in the new domain extents
			labels.enter()
				.append('svg:g')
				.attr('class', 'itemLabel')
				.append('svg:foreignObject')
				.attr('class', function (d) { return 'itemLabel unselectable mainItem ' + getClasses(d); })
				.attr('id', function (d) { return 'label-' + d.id; })
				.attr('x', function (d) { return d.points.x; })
				.attr('y', function (d) { return d.points.y; })
				.attr('width', function (d) { return Math.max(0, d.points.w - anchorOffset); })
				.attr('height', function (d) { return d.points.h; })
				.append('xhtml:body')
				.attr('style', function (d) { return 'height: ' + d.points.h + 'px !important;max-width: ' + Math.max(0, d.points.w - anchorOffset) + 'px !important;'; })
				.attr('class', 'itemLabel')
				.append('div')
				.attr('class', 'itemLabel')
				.attr('align', 'center')
				.append('p')
				.attr('class', 'itemLabel')
				.attr('style', function (d) { return 'width: ' + (d.points.w - anchorOffset) + 'px !important;max-width: ' + Math.max(0, d.points.w - anchorOffset) + 'px !important;'; })
				.append('span')
				.attr('class', 'itemLabel')
				.html(function (d) { return d.number + ': ' + d.name; });

			labels.exit().remove();
		}

		if (scaled) {
			if (GraphUtil.isIE()) {
				calculateLabelMaxWidths();
				calculateLabelBoundedWidths();
				calculateLabelVisWidths();
			}
			calculateLabelOffsets();
			correctLabelPositions();
		}

		// updates the mini graph
		miniPolys
			.attr('class', function (d) { return 'miniItem ' + getClasses(d); });


		if (window.performance && window.performance.now)
			console.log('display(' + resized + ') took ' + (performance.now() - startTime) + ' ms');
	}

	// clears all items from the main group then redraws them
	function fullRedraw() {
		itemPolys.selectAll('polygon').remove();
		itemArrows.selectAll('line').remove();
		if (GraphUtil.isIE())
			itemLabels.selectAll('g').remove();
		else
			itemLabels.selectAll(function () { return this.getElementsByTagName("foreignObject"); }).remove();
		miniPolys.attr('points', function (d) { return getPointsMini(d); });
		display(true, true);
	}

	// calculate the full widths of the labels
	function calculateLabelMaxWidths() {
		$('text.itemLabel').each(function (i, o) {
			var d = o.__data__
			if (d.labelWidth == null)
				d.labelWidth = o.getBoundingClientRect().width
		});
	}

	// calculate the widths of the labels bounded by the box of their task
	function calculateLabelBoundedWidths() {
		$('text.itemLabel').each(function (i, o) {
			var d = o.__data__
			var boundWidth = Math.max(0, d.points.x2 - d.points.x)
			d.boundedWidth = Math.min(d.labelWidth, boundWidth)
		});
	}

	// calculate the widths of the labels bounded by the current view
	function calculateLabelVisWidths() {
		$('text.itemLabel').each(function (i, o) {
			var d = o.__data__
			var leftX = Math.max(x1(brush.extent()[0]), d.points.x)
			var rightX = Math.max(x1(brush.extent()[0]), Math.min(x1(brush.extent()[1]), d.points.x2))
			var boundWidth = Math.max(0, rightX - leftX)
			d.visWidth = Math.min(d.labelWidth, boundWidth)
		});
	}

	function calculateLabelOffsets() {
		var pageOffset = x1(brush.extent()[0]);
		var svgOffset = $(chart[0][0]).offset().left;
		var labelElements = $('span.itemLabel')
		var isIE = GraphUtil.isIE()
		if (isIE)
			labelElements = $('text.itemLabel')

		labelElements.each(function (i, o) {
			var d = o.__data__
			var labelWidth = o.getBoundingClientRect().width
			if (isIE)
				labelWidth = d.boundedWidth
			d.labelStart = d.points.x + (d.points.w / 2) - (labelWidth / 2)
			d.labelEnd = d.points.x + (d.points.w / 2) + (labelWidth / 2)
		});
	}
	debug.calculateLabelOffsets = calculateLabelOffsets;

	// Offsets the labels horizontally so that they aren't cut off by the sides of the screen
	function correctLabelPositions() {

		var visStart = x1(brush.extent()[0]);
		var visEnd = x1(brush.extent()[1]);

		labels = itemLabels.selectAll(function () { return this.getElementsByTagName("foreignObject"); })
		if (GraphUtil.isIE())
			labels = itemLabels.selectAll('g')

		labels.data(items, function (d) { return d.id; });

		labels[0].each(function (o, i) {

			var d = o.__data__;
			var pageX = visStart - d.labelStart;
			var pageX2 = visEnd - d.labelEnd;

			var newX = d.points.x;
			var newW = Math.max(0, d.points.w - anchorOffset);
			d.useOffset = true;

			// check if the task is visible
			if (x1(d.end) > visStart && x1(d.start) < visEnd) {
				// check if it should be floating left
				if (d.labelStart < visStart && pageX > 0) {
					newX = d.labelStart + pageX + labelFloatOffset;
					newW = Math.max(0, Math.min(d.labelEnd - d.labelStart + labelPadding, x1(d.end) - newX - labelPadding - labelFloatOffset - anchorOffset));
					// check if it should be floating right
				} else if (d.labelEnd > visEnd && pageX2 < 0) {
					newX = Math.max(d.labelStart + pageX2 - labelFloatOffset - labelPadding, x1(d.start));
					newW = Math.max(0, Math.min((d.labelEnd - d.labelStart) + labelPadding, visEnd - x1(d.start)));
					// else it is positioned normally
				} else {
					o.__data__.useOffset = false;
				}
			} else {
				o.__data__.useOffset = false;
			}

			o.__data__.labelX = newX;
			o.__data__.labelW = newW;

			if (GraphUtil.isIE())
				d3.select(o)
					.select(':first-child')
					.attr('x', function () {
						if (d.useOffset)
							return newX
						return d.labelStart
					})
					.attr('textLength', function (d) {
						if (d.visWidth < d.labelWidth)
							return Math.max(d.visWidth, 0.01)
						return ''
					})
			else
				d3.select(o)
					.attr('x', newX)
					.attr('width', newW)
					.select(':first-child')
					.attr('style', function (d) { return 'height: ' + o.__data__.points.h + 'px !important;max-width: ' + newW + 'px !important;'; })
					.select(':first-child')
					.select(':first-child')
					.attr('style', function (d) { return 'width: ' + newW + 'px !important;max-width: ' + newW + 'px !important;'; });

		});
	}
	debug.correctLabelPositions = correctLabelPositions;

	// setup all the heights to fit to the rect height
	function setupHeights(newRectHeight) {
		mainRectHeight = isNaN(newRectHeight) ? mainRectHeight : newRectHeight;
		mainHeight = ((maxStack + 1) * mainRectHeight) + margin.bottom;
		height = mainHeight + miniHeight + margin.top + margin.bottom;
		$('.main').height(mainHeight);
		$('clippath rect').height(mainHeight);
		chart.attr('height', height + margin.top + margin.bottom + 20);
		background.attr('height', mainHeight + backgroundExtraHeight);
		mainNowLine.attr('y2', mainHeight);
		fullRedraw();
	}

	// gets the css classes that apply to task @param d
	function getClasses(d) {
		var classString = 'unselectable '
			+ (d.selected ? 'selected ' : '')
			+ (d.milestone ? 'milestone ' : '')
			+ (d.criticalPath && highlightCritical ? 'critical ' : '')
			+ (d.root ? 'root ' : '')
			+ (d.redundant ? 'redundant ' : '')
			+ (d.cyclical ? 'cyclical ' : '')
			+ (d.end < now() ? 'past ' : 'future ')
			+ (d.highlight ? 'highlighted ' : '')
			+ (d.redundant && hideRedundant ? 'hidden ' : '')
			+ (d.status);
		if (d.status != 'Completed' && d.end < now())
			classString += ' overdue ';
		else if (d.status == 'Completed' && d.end > now())
			classString += ' ahead ';
		else
			classString += ' ontime ';
		var teamSelect = $('#teamSelectId');
		if (teamSelect.val() != 'ALL' && teamSelect.val() != d.role)
			classString += ' unfocussed ';

		return classString;
	}

	// gets the points string for task polygons
	function getPoints(d) {
		var points = {};
		var taskHeight = useHeights ? Math.max(1, d.height) : 1;
		var x = x1(d.start);
		var offset = Math.floor((taskHeight - 1) / 2);
		var y = (d.stack - offset) * mainRectHeight + 0.4 * mainRectHeight + 0.5;
		var w = x1(d.end) - x1(d.start) - 2;
		var h = (mainRectHeight) * taskHeight - (mainRectHeight * 0.2);
		var x2 = Math.max(x + anchorOffset - 2, x + w - anchorOffset);
		if (w < anchorOffset)
			x2 = x + w;
		var y2 = y + (h / 2);
		return { x: x, y: y, x2: x2, y2: y2, w: w, h: h };
	}


	// gets the points string for mini polygons
	function getPointsMini(d) {
		var taskHeight = useHeights ? Math.max(1, d.height) : 1;
		var offset = Math.floor((taskHeight - 1) / 2);
		var xa = Math.floor(x(d.start));
		var ya = (d.stack - offset) * (miniRectHeight) + 1;
		var w = Math.max(Math.floor(x(d.end)) - Math.floor(x(d.start)) - miniRectStroke, 1);
		var h = (miniRectHeight) * taskHeight - miniRectStroke;
		return xa + ',' + ya + ' '
			+ (xa + w) + ',' + ya + ' '
			+ (xa + w) + ',' + (ya + h) + ' '
			+ xa + ',' + (ya + h) + ' ';

	}

	// used to get the offset used for dependency arrows' links to the task rects
	function getAnchorLocation(d) {
		var p = d.predecessor;
		var s = d.successor;
		var ps = p.start.getTime();
		var pe = p.end.getTime();
		var ss = s.start.getTime();
		var se = s.end.getTime();
		return [x1(new Date(ps + 0.9 * (pe - ps))), x1(new Date(se - 0.9 * (se - ss)))];
	}


	// Toggles selection of a task
	function toggleTaskSelection(taskObject, skipRepaint) {

		if (mainDrag.dragging)
			return;

		var selecting = true;
		if (taskSelected == null && taskObject == null)
			return; // No node is selected, so there is nothing to deselect

		// deselecting
		if (taskSelected == taskObject) {
			selecting = false;

			// selecting
		} else {
			toggleTaskSelection(taskSelected, true) // if another task is selected, deselect that one first
			taskSelected = taskObject;
		}

		// recursively style all tasks and dependencies connected to this task
		function styleDependencies(task, direction) {
			if (selecting != task.selected) {
				task.selected = selecting;

				if (direction == 'left' || direction == 'both')
					$.each(task.predecessors.concat(task.redundantPredecessors), function (i, d) {
						d.selected = selecting;
						styleDependencies(d.predecessor, 'left');
					});
				if (direction == 'right' || direction == 'both')
					$.each(task.successors.concat(task.redundantSuccessors), function (i, d) {
						d.selected = selecting;
						styleDependencies(d.successor, 'right');
					});
			}
		}

		styleDependencies(taskObject, 'both');

		if (!selecting)
			taskSelected = null;

		if (!skipRepaint) {
			display(false, false, true);
			correctLabelPositions()
		}
	}

	// moves the brush to the selected location
	function moveBrush() {
		var origin = d3.mouse(this);
		var point = x.invert(origin[0]);

		if (!brush.empty()) {
			var halfExtent = (brush.extent()[1].getTime() - brush.extent()[0].getTime()) / 2;
			var start = new Date(point.getTime() - halfExtent);
			var end = new Date(point.getTime() + halfExtent);
		} else {
			var halfExtent = initialExtent / 2;
			var start = new Date(point.getTime() - halfExtent);
			var end = new Date(point.getTime() + halfExtent);
		}

		brush.extent([start, end]);
		display(true, false);
	}

	// cuts off any labels that extend outside of their task's polygon
	function trimLabels() {
		$('.itemLabel').each(function (i, label) {
			var poly = $('#task-');
		});
	}

	// calculate stacking values for @param task
	function calculateStacks() {

		var waiting = true;
		var stack = [];
		var minIndex = 1000;
		var maxIndex = 1000;
		var rectHeight = 20;

		function calculateStack(task) {

			// check if this task has already been placed
			if (task == null || task.stack != null)
				return;

			// check if this task has any uncalculated predecessors
			if (hasUndefinedPredecessors(task))
				return;

			// the ideal location for any given task is the average of its predecessors
			var ideal = minIndex + Math.round((maxIndex - minIndex) / 2);
			if (task.predecessors.length > 0)
				ideal = getIdealStackLocation(task, getPredecessors(task), 'p');
			addTask(task, ideal, false);

			// recursively calculate the stack for this tasks's successors
			var successorsToCheck = getSuccessors(task);
			for (var i = 0; i < successorsToCheck.length; ++i)
				calculateStack(successorsToCheck[i]);

			return;
		}

		// inserts @param task as close to @param row as possible
		function addTask(task, ideal, correcting) {

			// first check if the ideal location is availible
			if (!stack[ideal] || canFitInRow(task, stack, ideal)) {
				insertIntoStack(task, stack, ideal);

				// if not, move outwards from that location, until an empty location or a location worth switching for
			} else {
				var offsetLow = Math.floor((Math.max(task.height, 1) - 1) / 2);
				var offsetHigh = Math.ceil((Math.max(task.height, 1) - 1) / 2);
				var high = ideal;
				var low = ideal;
				var highLocked = false;
				var lowLocked = false;
				var inserted = false;
				while (!inserted) {
					inserted = true;
					if (high > maxIndex) {
						++maxIndex;
					} else if (low < minIndex) {
						--minIndex;
					}
					if (canFitInRow(task, stack, high)) {
						insertIntoStack(task, stack, high);
					} else if (canFitInRow(task, stack, low)) {
						insertIntoStack(task, stack, low);
					} else {
						inserted = false;
					}
					++high;
					--low;
				}
				maxIndex = Math.max(maxIndex, task.stack + offsetHigh);
				minIndex = Math.min(minIndex, task.stack - offsetLow);
			}
		}

		// Sorts the successors of @param task
		function sortSuccessors(task) {
			var toSort = getSuccessors(task);
			var sorted = [];
			while (toSort.size() > 0) {
				for (var i = 1; i < toSort.size(); ++i) {
					if ((toSort[0].predecessorIds.valueOf() == toSort[i].predecessorIds.valueOf()) && (getSuccessors(toSort[0]).valueOf() == getSuccessors(toSort[i]).valueOf())) {
						sorted.push(toSort[i]);
						toSort.splice(i, 1);
					}
				}
				sorted.push(toSort[0]);
				toSort.splice(0, 1);
			}

			return sorted;
		}

		function hasUndefinedPredecessors(task) {
			for (var i = 0; i < task.predecessors.length; ++i)
				if (task.predecessors[i].predecessor.stack == null)
					return true;
			return false;
		}

		function hasUndefindedSuccessors(task) {
			for (var i = 0; i < task.successors.length; ++i)
				if (task.successors[i].successor.stack == null)
					return true;
			return false;
		}

		function insertIntoStack(task, stack, row) {
			insertIntoStack2(task, stack, row);
		}
		function insertIntoStack2(task, stack, row) {
			var low = Math.floor((Math.max(1, task.height) - 1) / 2);
			var high = Math.ceil((Math.max(1, task.height) - 1) / 2);
			maxIndex = Math.max(maxIndex, row + high);
			minIndex = Math.min(minIndex, row - low);

			for (var r = 0 - low; r <= high; ++r) {
				if (stack[row + r] == null)
					stack[row + r] = [];
				stack[row + r].push([x(task.start), x(task.end)]);
			}
			task.stack = row;
		}

		// gets the best stack location for task (object) based on related tasks.
		function getIdealStackLocation(task, related, direction) {

			// if only a single parent is involved, fill its height
			if (related.size() == 1) {
				var siblings = [];
				for (var i = 0; i < getSuccessors(related[0]).size(); ++i)
					if (getPredecessors(getSuccessors(related[0])[i]).size() == 1)
						siblings.push(getSuccessors(related[0])[i]);
				var offsetFromParent = Math.floor((Math.max(1, related[0].height) - 1) / 2);
				var offsetFromSiblings = 0;
				var offsetFromHeight = Math.floor((Math.max(1, task.height) - 1) / 2);
				if (siblings.indexOf(task) != -1) {
					for (var i = 0; i < siblings.indexOf(task); i++)
						offsetFromSiblings += Math.max(1, siblings[i].height);
					return related[0].stack - offsetFromParent + offsetFromSiblings + offsetFromHeight;
				}
			}

			// if any predecessors have this task as their only successor, only consider those predecessors
			if (direction == 'p') {
				var newList = [];
				for (var i = 0; i < related.size(); ++i)
					if (related[i].successors.size() == 1)
						newList.push(related[i]);
				if (newList.size() > 0)
					related = newList;
			}

			var sum = 0;
			var count = 0;
			var closestIsSuccessor = related[0].start > task.start;

			for (var i = 0; i < related.length; ++i) {
				if (related[i].stack) {
					for (var n = 0; n < Math.max(1, related[i].height); ++n) {
						++count;
						sum += related[i].stack;
					}
				}
			}

			// check if the ideal location is directly between 2 values
			if ((sum / count) - Math.floor(sum / count) == 0.5) {


				if ((task.height % 2) == 0)
					return Math.floor(sum / count);
				return Math.ceil(sum / count);
			}
			if (sum == 0)
				return 0;
			if (Math.abs((sum / count) - Math.round(sum / count)) == 0.5) {

			}

			return Math.round(sum / count);
		}

		function getTimeBetween(taskA, taskB) {
			return Math.min(Math.abs(x(taskA.start) - x(taskB.end)),
				Math.abs(x(taskB.start) - x(taskA.end)));
		}

		function getDependentTasks(task) {
			return getPredecessors(task).concat(getSuccessors(task));
		}

		// checks if a task can fit in a row in the stack
		function canFitInRow(task, stack, row) {
			var low = Math.floor((Math.max(1, task.height) - 1) / 2);
			var high = Math.ceil((Math.max(1, task.height) - 1) / 2);
			for (var r = 0 - low; r <= high; ++r) {
				if (stack[row + r] != null)
					for (var i = 0; i < stack[row + r].length; ++i) {
						var startsBefore = stack[row + r][i][0] < x(task.exEnd);
						var endsAfter = stack[row + r][i][1] > x(task.start);
						if (startsBefore && endsAfter)
							return false;
					}
				else
					stack[row + r] = [];
			}
			return true;
		}

		calculateStack(data.root);

		// realigns all stack values to start from 0
		for (var i = 0; i < items.length; ++i)
			items[i].stack -= minIndex;

		return maxIndex - minIndex;
	}

	function getSuccessors(task) {
		if (!task)
			return;
		var tasks = [];
		for (var i = 0; i < task.successors.length; ++i)
			tasks.push(task.successors[i].successor);
		return tasks;
	}

	function getPredecessors(task) {
		if (!task)
			return;
		var tasks = [];
		for (var i = 0; i < task.predecessors.length; ++i)
			tasks.push(task.predecessors[i].predecessor);
		return tasks;
	}

	// increases/decreases the current brush extent. called when the user scrolls on the mini graph.
	function zoom() {
		var delta = 1;
		var halfExtent = (brush.extent()[1].getTime() - brush.extent()[0].getTime()) / 2;
		if (d3.event.sourceEvent.deltaY < 0)
			delta = 1.2;
		if (d3.event.sourceEvent.deltaY > 0 && halfExtent > 50000)
			delta = 0.8;
		var midpoint = (brush.extent()[1].getTime() + brush.extent()[0].getTime()) / 2;
		var newStart = midpoint - (halfExtent * delta);
		var newEnd = midpoint + (halfExtent * delta);
		brush.extent([new Date(newStart), new Date(newEnd)]);
		display(true, true);
	}

	/*	Reconstructs the data in @param tasks for d3 by:
	 - adding a "root" task when there are multiple start tasks
	 - identifying tasks where start = end as milestones
	 - removing redundant dependency links
	 - converting the start and completion times for tasks from date Strings to javascript Dates
	 - populates the dependencies list from data in the items list
	 - corrects any impossible start times for tasks */
	function sanitizeData(tasks, dependencies) {

		data.searchFilter = '';

		for (var i = 0; i < items.length; ++i) {
			items[i].successors = [];
			items[i].predecessors = [];
			items[i].redundantSuccessors = [];
			items[i].redundantPredecessors = [];
		}

		// if there are any cyclical structures, remove one of the dependencies and mark it as cyclical
		for (var i = 0; i < Object.keys(data.cyclicals).size(); i++) {
			var key = parseInt(Object.keys(data.cyclicals)[i]);
			var predecessor = items[binarySearch(items, key, 0, items.length - 1)];
			var stack = data.cyclicals[Object.keys(data.cyclicals)[i]];
			for (var j = 0; j < stack.size(); ++j) {
				var node = items[binarySearch(items, stack[j], 0, items.length - 1)];
				if (node.predecessorIds.indexOf(key) != -1) {
					// construct a dependency object and move the predecessorId to the redundant list
					var depObject = { "predecessor": predecessor, "successor": node, "modifier": "hidden", "selected": false, "redundant": true, "cyclical": true };
					depObject.root = predecessor.root;
					predecessor.redundantSuccessors.push(depObject);
					node.redundantPredecessors.push(depObject);
					dependencies.push(depObject);
					node.predecessorIds.splice(node.predecessorIds.indexOf(key), 1);
				}
			}
		}

		// if there are cyclical structures, tell the user that the data might be inaccurate
		if (Object.keys(data.cyclicals).size() > 0)
			alert("This task data contains cyclical dependency structures, so the resulting timeline may not be entirely accurate. Dependencies that create cyclical structures will be displayed as green lines.");

		// if there is more than 1 start task, create a fake root task
		data.root = null;
		if (starts.size() > 1) {
			var earliest = starts[0].startInitial;
			for (var i = 0; i < starts.size(); i++) {
				var task = items[binarySearch(items, starts[i], 0, items.length - 1)];
				task.predecessorIds.push(-10);
				earliest = Math.min(earliest, task.startInitial);
			}
			var root = {};
			root.id = -10;
			root.name = 'root';
			root.root = true;
			root.startInitial = 0;
			root.endInitial = 0;
			root.predecessorIds = [];
			items = [root].concat(items);
			data.root = root;
		} else if (items.size() > 0) {
			data.root = items[binarySearch(items, starts[0], 0, items.length - 1)];
		}

		// convert all data to its proper format
		var startTime = parseStartDate(data.startDate);
		for (var i = 0; i < items.length; ++i) {
			items[i].milestone = (items[i].startInitial == items[i].endInitial);
			items[i].startInitial = new Date(startTime.getTime() + (items[i].startInitial) * 60000);
			items[i].endInitial = new Date(startTime.getTime() + (items[i].endInitial + items[i].milestone) * 60000);
			items[i].start = null;
			items[i].end = null;
			items[i].exChild = null;
			items[i].exParent = null;
			items[i].endOfExclusive = null;
			items[i].selected = false;
			items[i].checked = false;
			items[i].height = 0;
			items[i].root = items[i].root != null;
			items[i].successors = [];
			items[i].predecessors = [];
			items[i].redundantSuccessors = [];
			items[i].redundantPredecessors = [];
			items[i].siblingGroups = [];
			items[i].siblingGroupParents = [];
			items[i].childGroups = [];
		}

		// generate dependencies in a separate loop to ensure no dependencies pointing to removed tasks are created
		for (var i = 0; i < items.length; ++i)
			if (items[i] && items[i].predecessorIds)
				for (var j = 0; j < items[i].predecessorIds.length; ++j) {
					var predecessorIndex = binarySearch(items, items[i].predecessorIds[j], 0, items.length - 1)
					if (items[predecessorIndex]) {
						var depObject = { "predecessor": items[predecessorIndex], "successor": items[i], "modifier": "hidden", "selected": false, "redundant": false, "cyclical": false, "criticalPath": false };
						if (items[predecessorIndex].criticalPath && items[i].criticalPath)
							depObject.criticalPath = true;
						if (predecessorIndex != -1) {
							depObject.root = items[predecessorIndex].root;
							items[predecessorIndex].successors.push(depObject);
							items[i].predecessors.push(depObject);
							dependencies.push(depObject);
						}
					}
				}

		// find and remove any redundant dependencies using a queue for a breadth first search
		var queue = [];
		if (data.root != null) {
			queue.push(data.root);
			while (queue.size() > 0)
				searchForRedundency(queue.pop());
		}

		function searchForRedundency(node) {

			/* 	Mark this node as checked.
			 Because nodes are only added to the queue when all their predecessors
			 are checked, all future dependencies checked for redundency are
			 guarenteed to not come from a predecessor to this node. */
			node.checked = true;

			// iterate through the node's successors
			for (var j = 0; j < node.successors.size(); ++j) {


				// if this successor has a predecessor that hasn't been checked, don't add it to the queue
				var child = node.successors[j].successor;
				var canQueue = true;
				for (var i = 0; i < getPredecessors(child).size(); i++)
					if (!getPredecessors(child)[i].checked)
						canQueue = false;

				// if this successor can be queued, add it to the queue if it isn't already there
				if (queue.indexOf(child) == -1 && canQueue)
					queue.unshift(child);

				// if this successor has other predecessors, check if one of them leads back to the original node
				if (child.predecessors.size() > 1) {

					// check if this dependency is redundant
					checked = [];
					if (searchUp(node, child, node.successors[j], checked)) {

						// the dependency is redundant, so remove it from both nodes' dependency lists and mark it as redundant
						node.successors[j].redundant = true;
						var index = node.successors[j].successor.predecessors.indexOf(node.successors[j]);
						node.successors[j].successor.redundantPredecessors.push(node.successors[j]);
						node.redundantSuccessors.push(node.successors[j]);
						node.successors[j].successor.predecessors.splice(index, 1);
						node.successors.splice(j, 1);
						--j;
					}
				}
			}
		}

		/*	Searches for @param target from @param start by searching up the tree, ignoring dependency @param ignore.
		 Returns true if the dependency is redundant. */
		function searchUp(target, start, ignore, checked) {

			// if we found a path that reaches the target, the dependency is redundent
			if (target == start)
				return true;

			// if this node has already been checked, we can't get to the target from here
			if (start.checked)
				return false;

			// if this node is in the checked list, we have already confirmed that it can't lead to the target
			if (checked.indexOf(start) != -1)
				return false;

			// search from each of this node's predecessors
			for (var i = 0; i < start.predecessors.size(); ++i)
				if (start.predecessors[i] != ignore && searchUp(target, start.predecessors[i].predecessor, ignore, checked))
					return true;

			// we can't reach the target from this node, so add it to the checked list and return false
			checked.push(start);
			return false;
		}

		// correct any errors in the start times for tasks
		var checking = [];
		for (var i = 0; i < items.length; ++i)
			calculateTimes(items[i], checking);

		// calculate exEnds and exclusive end tasks
		for (var i = 0; i < items.length; ++i) {
			items[i].exEnd = getExEnd(items[i]);
			var exSet = [];
			getExclusiveSet(items[i], exSet);
			items[i].exSet = exSet;
			var nonExclusiveSet = [];
			for (var j = 0; j < exSet.size(); ++j)
				for (var k = 0; k < getSuccessors(exSet[j]).size(); ++k)
					if ((exSet.indexOf(getSuccessors(exSet[j])[k]) == -1) && (nonExclusiveSet.indexOf(getSuccessors(exSet[j])[k]) == -1))
						nonExclusiveSet.push(getSuccessors(exSet[j])[k]);
			if (nonExclusiveSet.size() == 1)
				items[i].endOfExclusive = nonExclusiveSet[0];
		}


		// gets and outputs all the sibling groups
		var groupMatrices = {};
		for (var i = 0; i < items.length; ++i) {
			if (items[i].predecessors.size() > 1) {

				var parentTask = null;

				// get the base group
				var group = getPredecessors(items[i]);

				// move each sibling up until it reaches a task with multiple successors
				for (var j = 0; j < group.length; ++j) {
					while (group[j].predecessors.size() == 1 && getSuccessors(getPredecessors(group[j])[0]).size() == 1)
						group.splice(j, 1, getPredecessors(group[j])[0]);
				}

				siblingGroups.push(group);

				// make a new group that will be reduced further
				var group2 = [];
				for (var j = 0; j < group.length; ++j)
					group2.push(group[j]);

				// reduce group 2
				for (var j = 0; j < group2.length; ++j) {

					var searching = true;
					while (group2[j].predecessors.size() > 0 && searching) {

						searching = false;

						var parent = getPredecessors(group2[j])[0];
						parentTask = parent;
						var set = parent.exSet;
						for (var s = 0; s < group2.length; ++s) {
							if (set.indexOf(group2[s]) == -1) {
								searching = true;
							}
						}


						if (searching) {
							var predecessors = getPredecessors(group2[j]);
							if (group2.indexOf(predecessors[0]) == -1) {
								group2.splice(j, 1, predecessors[0]);
							} else {
								group2.splice(j, 1);
								--j;
							}

							for (var k = 1; k < predecessors.length; ++k) {
								var adding = predecessors[k];
								if (group2.indexOf(adding) == -1)
									group2.push(adding);
							}
						}
					}
					group2[j].siblings = true;
					group2[j].siblingGroupParents = (group2[j].siblingGroupParents != null ? group2[j].siblingGroupParents : []);
					group2[j].siblingGroups = (group2[j].siblingGroups ? group2[j].siblingGroups : []);

				}

				// build the association matrix if it doesn't exist
				var firstIndex = 0;
				var groupMatrix = groupMatrices[parentTask.id - firstIndex];
				if (groupMatrix == null) {
					groupMatrix = {};
					var allSiblings = getSuccessors(parentTask);
					for (var j = 0; j < allSiblings.size(); ++j) {
						groupMatrix[allSiblings[j].id - firstIndex] = {};
						for (var k = 0; k < allSiblings.size(); ++k) {
							groupMatrix[allSiblings[j].id - firstIndex][allSiblings[k].id - firstIndex] = 0;
						}
						allSiblings[j].allSiblings = allSiblings;
					}

					groupMatrices[parentTask.id - firstIndex] = groupMatrix;
				}

				parentTask.childGroups.push(group2);


				// add this group to the matrix
				for (var j = 0; j < group2.size(); ++j)
					for (var k = 0; k < group2.size(); ++k)
						++(groupMatrices[parentTask.id - firstIndex][group2[j].id - firstIndex][group2[k].id - firstIndex]);

				// give each task a reference to this group
				for (var j = 0; j < group2.length; ++j) {
					group2[j].siblingGroupParents.push(items[i]);
					group2[j].siblingGroups.push(group2);
				}

				siblingGroupsReduced.push(group2);
				items[i].siblingGroupParents = [items[i]];
				items[i].siblingGroups.push(group2);
			}
		}

		// group successors by their predecessors for each task
		for (var i = 0; i < items.length; ++i) {
			var grouped = false;
			if (items[i].successors.size() > 1) {

				var oldSuccessors = items[i].successors;
				var newSuccessors = [];
				for (var j = 0; j < oldSuccessors.size(); ++j) {
					var inserted = false;
					var location = 0;
					if (newSuccessors.size() == 0) {
						newSuccessors.push(oldSuccessors[j]);
						inserted = true;
					}

					for (var k = 0; k < newSuccessors.size(); ++k)
						if ((!inserted) && (haveSameSuccessors(newSuccessors[k].successor, oldSuccessors[j].successor, true) == 1)) {
							newSuccessors.splice(k, 0, oldSuccessors[j]);
							location = k;
							inserted = true;
							grouped = true;
						}

					// keep trying, ignoring an increasing number of non-matches
					var tolerance = 0;
					while (!inserted) {
						++tolerance;
						for (var k = 0; k < newSuccessors.size(); ++k) {
							if ((!inserted) && (haveSameSuccessors(newSuccessors[k].successor, oldSuccessors[j].successor, false) <= tolerance)) {
								newSuccessors.splice(k, 0, oldSuccessors[j]);
								inserted = true;
								grouped = true;
								location = k;
							}
						}
						if (tolerance > 100) {
							newSuccessors.push(oldSuccessors[j]);
							inserted = true;
							location = newSuccessors.size();
						}
					}
					if (!inserted) {
						newSuccessors.push(oldSuccessors[j]);
						if (!grouped)
							newSuccessors = newSuccessors.sort(function (a, b) { return b.height - a.height });
					}
				}

				items[i].successors = newSuccessors;
			}
		}


		for (var i = 0; i < items.length; ++i) {
			var grouped = false;
			if (items[i].successors.size() > 1) {

				var initialList = [];
				var groups = items[i].childGroups;
				for (var j = 0; j < items[i].successors.size(); ++j) {

					getSuccessors(items[i])[j].groups = {};
					var s = getSuccessors(items[i])[j];
					for (var k = 0; k < s.siblingGroups.size(); ++k) {
						s.groups[groups.indexOf(s.siblingGroups[k])] = true;
					}
					initialList.push(getSuccessors(items[i])[j]);
				}

				var groupStatus = [];
				var newList = [];

				// first remove all duplicates
				var bufferList = initialList.slice(0);
				bufferList = removeDuplicates(initialList.slice(0), groups);

				// sort the list by number of groups
				bufferList.sort(function (a, b) {
					return Object.keys(a.groups).size() - Object.keys(b.groups).size();
				});


				if (newList.length == 0)
					newList.push(bufferList.pop());

				for (var j = 0; j < groups.length; ++j) {
					groupStatus.push('|');
				}

				if (groupStatus.size() != 0) {
					var calls = 0;
					while (bufferList.length > 0) {

						// prevent infinite loops from occuring
						++calls;
						if (calls > 100)
							break;

						// calculate the status
						for (var j = 0; j < groups.length; ++j) {

							var g = groups[j];

							groupStatus[j] = '|';

							// use newList
							var found = false;
							(function () {
								var emptyTop = newList[0].groups[j] == null;
								for (var k = 0; k < newList.length; ++k) {
									var t = newList[k];
									if (t.groups[j] != null) {
										found = true;
										if (emptyTop) {
											groupStatus[j] = 'v';
											return;
										}
									} else {
										if (!emptyTop) {
											groupStatus[j] = '^';
											return;
										}
									}
								}

								if (!found)
									groupStatus[j] = '_';

								return;
							}())


							// find complete groups
							found = false;
							for (var k = 0; k < bufferList.length; ++k)
								if (bufferList[k].groups[j])
									found = true;
							if (!found)
								groupStatus[j] = '%';
						}

						// get the constraints for the next task
						var constraints = [];
						for (var j = 0; j < groups.length; ++j) {
							if (groupStatus[j] == '^') {
								constraints.push(-1 - j);
							}
							if (groupStatus[j] == 'v') {
								constraints.push(1 + j);
							}
						}

						// score the tasks
						var bestScore = 0;
						var bestTask = 0;

						var pushConstraintsSatisfied = 0;
						var unshiftConstraintsSatisfied = 0;
						var maxConstraintsSatisfied = 0;
						var constraintTask = 0;
						var pushing = true;

						var pushMatches = 0;
						var unshiftMatches = 0;
						var maxMatches = 0;
						var pushingMatches = true;
						var matchTask = 0;

						for (var j = 0; j < bufferList.length; ++j) {

							var usable = (constraints.length > 0) ? (0) : (-1);
							var fitsConstraints = (constraints.length == 0);
							pushConstraintsSatisfied = 0;
							unshiftConstraintsSatisfied = 0;

							pushMatches = getMatchingGroups(bufferList[j].groups, newList[newList.length - 1].groups);
							unshiftMatches = getMatchingGroups(bufferList[j].groups, newList[newList.length - 1].groups);

							for (var k = 0; k < constraints.length; ++k) {

								var constraint = constraints[k];
								if (constraint > 0)
									--constraint;
								else
									++constraint;


								if (bufferList[j].groups[constraint] && constraints[k] > 0) {
									++pushConstraintsSatisfied;
									if (usable == 0) {
										fitsConstraints = true;
										usable = 1;
									}

								} else if (bufferList[j].groups[constraint] && constraints[k] < 0) {
									++unshiftConstraintsSatisfied;
									if (usable == 0) {
										fitsConstraints = true;
										usable = -1;
									}
								} else {
									if (constraint > 0 && usable != 2) {
										usable = -2;
									} else if (constraint < 0 && usable != -2) {
										usable = 2;
									} else {
										fitsConstraints = false;
										usable = 3;
									}
								}
							}


							var score = 0;
							if (usable == -1 || usable == -2)
								for (var k = 0; k < groups.length; ++k)
									if ((groups[k].indexOf(newList[0]) != -1) && (groups[k].indexOf(bufferList[j]) != -1))
										score += usable;
							if (usable == 1 || usable == 2)
								for (var k = 0; k < groups.length; ++k)
									if ((groups[k].indexOf(newList[0]) != -1) && (groups[k].indexOf(bufferList[j]) != -1))
										score += usable;


							if (score < 0)
								score = (fitsConstraints && unshiftConstraintsSatisfied > 0) ? --score : 0;
							else
								score = (fitsConstraints && pushConstraintsSatisfied > 0) ? ++score : 0;

							if (Math.abs(score) > Math.abs(bestScore)) {
								bestScore = score;
								bestTask = j;
							}
							if (Math.abs(score) > Math.abs(bestScore)) {
								bestScore = score;
								constraint = j;
							}

							if (Math.max(pushConstraintsSatisfied, unshiftConstraintsSatisfied) > maxConstraintsSatisfied) {
								maxConstraintsSatisfied = Math.max(pushConstraintsSatisfied, unshiftConstraintsSatisfied);
								constraintTask = j;
								pushing = pushConstraintsSatisfied > unshiftConstraintsSatisfied;
								unshiftConstraintsSatisfied = 0;
								pushConstraintsSatisfied = 0;
							}

							if (Math.max(pushMatches, unshiftMatches) > maxMatches) {
								maxMatches = Math.max(pushMatches, unshiftMatches);
								matchTask = j;
								pushingMatches = pushMatches > unshiftMatches;
								unshiftMatches = 0;
								pushMatches = 0;
							}
						}

						if (bestScore < 0) {
							newList.unshift(bufferList[bestTask]);
							bufferList.splice(bestTask, 1);
						} else if (bestScore > 0) {
							newList.push(bufferList[bestTask]);
							bufferList.splice(bestTask, 1);
						} else if (maxConstraintsSatisfied > 0) {
							if (pushing)
								newList.push(bufferList[constraintTask]);
							else
								newList.unshift(bufferList[constraintTask]);
							bufferList.splice(constraintTask, 1);

						} else if (maxMatches > 0) {
							if (pushingMatches)
								newList.push(bufferList[matchTask]);
							else
								newList.unshift(bufferList[matchTask]);
							bufferList.splice(matchTask, 1);

						} else {

							var maxScore = 100000;
							var score = 0;
							var bestPlacement = 0;
							var pushing = true;
							for (var j = 0; j < bufferList.length; ++j) {

								var row = bufferList[j];
								var tempList = newList.slice(0);

								tempList.push(row);
								score = evaluateStack(tempList, groups);
								if (score < maxScore) {
									maxScore = score;
									bestPlacement = j;
									pushing = true;
								}
								tempList.pop();

								tempList.unshift(row);
								score = evaluateStack(tempList, groups);
								if (score < maxScore) {
									maxScore = score;
									bestPlacement = j;
									pushing = false;
								}
								tempList.shift();
							}

							if (pushing)
								newList.push(bufferList[bestPlacement]);
							else
								newList.unshift(bufferList[bestPlacement]);
							bufferList.splice(bestPlacement, 1);
						}
					}

					/*
					 console.log('BEFORE READDING DUPLICATES FOR ' + items[i].name)
					 display(newList, groups);
					 */

					// put back all the duplicates
					for (var j = 0; j < initialList.length; ++j) {

						var task = initialList[j];

						// check if this task is already in the list
						if (newList.indexOf(task) == -1) {

							// this task is already in the list so add it next to its duplicate
							for (var k = 0; k < newList.length; ++k) {

								var matching = true;
								for (var l = 0; l < Object.keys(task.groups).length; ++l) {
									if (newList[k].groups[Object.keys(task.groups)[l]] == null)
										matching = false;
								}

								if (matching && newList[k].id != task.id) {
									newList.splice(k, 0, task);
									k = newList.length;
								}
							}
						}
					}

					/*
					 console.log('AFTER READDING DUPLICATES FOR ' + items[i].name)
					 display(newList, groups);
					 */

					function display(list, groups) {
						console.log('--------------------------------------------');
						for (var j = 0; j < list.length; ++j) {
							var output = list[j].id + ' : ';
							for (var k = 0; k < groups.length; ++k) {
								if (groups[k].indexOf(list[j]) != -1)
									output += '\t@';
								else
									output += '\t.';
							}
							output += '\t:' + list[j].name;
							console.log('\t > ' + output);
						}
						console.log('--------------------------------------------');
					}

					// match the actual successor order to the calculated order
					var newSuccessors = [];
					for (var j = 0; j < items[i].successors.size(); ++j) {
						var s = items[i].successors[j];
						var index = newList.indexOf(s.successor);
						newSuccessors[index] = s;
					}

					items[i].successors = newSuccessors;
				}
			}
		}

		function getMatchingGroups(list1, list2) {
			var matches = 0;
			for (var i = 0; i < Object.keys(list1).length; ++i) {
				var key = Object.keys(list1)[i];
				if (list2[key] != null)
					++matches;
			}
			return matches;
		}

		function evaluateStack(list, groups) {
			var score = 0;
			var tempScore = 0;
			var foundFirst = false;

			for (var k = 0; k < groups.length; ++k) {
				tempScore = 0;
				foundFirst = false;
				for (var j = 0; j < list.length; ++j) {
					if (list[j].groups[k]) {
						foundFirst = true;
						score += tempScore;
						tempScore = 0;
					} else {
						if (foundFirst)
							++tempScore;
					}
				}
			}

			return score;
		}


		function removeDuplicates(list, groups) {

			var uniqueRows = [];
			var returnList = [];

			for (var j = 0; j < list.length; ++j) {
				tempScore = 0;
				foundFirst = false;
				var task = list[j];
				var output = '';
				for (var k = 0; k < groups.length; ++k) {
					if (task.groups[k] != null)
						output += 'a';
					else
						output += 'b';

				}
				if (uniqueRows.indexOf(output) == -1) {
					uniqueRows.push(output);
					returnList.push(task);
				}
			}

			return returnList;
		}

		function outputChildMatrix(node) {
			var successors = getSuccessors(node);
			console.log('child matrix for node [' + node.id + '] ' + node.name);
			console.log('----------------------');
			for (var i = 0; i < successors.size(); ++i) {
				var node = successors[i];
				var output = node.id + ' : ';
				for (var l = 0; l < siblingGroupsReduced.length; ++l) {
					if (siblingGroupsReduced[l].indexOf(node) != -1)
						output += '\t@';
					else
						output += '\t.';
				}
				console.log('\t > ' + output + ' "' + node.name + '"');
			}
			console.log('----------------------');
		}

		/*	compares two tasks, returning true if they have the same siblingGroups.
		 assumes siblingGroupParents is sorted. */
		function haveSameSuccessors(task1, task2, fullMatch) {

			var returnVal = true;
			var parents1 = task1.siblingGroupParents;
			var parents2 = task2.siblingGroupParents;
			var matches = 0;
			var possibleMatches = Math.max(parents1.size(), parents2.size());

			if ((fullMatch) && (parents1.size() != parents2.size())) {
				return 0;
			}

			for (var i = 0; i < parents2.size(); ++i)
				if (parents1.indexOf(parents2[i]) != -1)
					++matches;

			if (fullMatch)
				return (matches == possibleMatches) ? 1 : 0;
			return possibleMatches - matches;
		}

		/*	fills @param set with all nodes that can only be
		 reached from @param node and its children.
		 (This only works if successors are in chronological order)*/
		function getExclusiveSet(node, set) {
			set.push(node);
			getSuccessors(node).forEach(function (c) {

				// true if c doesn't have an exParent
				var isExclusive = true;

				if (c.exParent != null) {
					if (c.exParent != node) {
						isExclusive = false;
					} else {
						isExclusive = true;
					}
				} else {
					getPredecessors(c).forEach(function (p) {
						if (set.indexOf(p) == -1)
							isExclusive = false;
					});
				}
				if (isExclusive)
					getExclusiveSet(c, set);
			});
		}

		// gets the exclusive height for @param task recursively
		function getExHeight(task) {
			var set = [];
			getExclusiveSet(task, set);
			var height = 0;
			for (var i = 0; i < set.size(); ++i)
				if (getSuccessors(task).indexOf(set[i]) != -1)
					height += getExHeight(set[i]);
			return Math.max(1, height);
		}

		// gets the exclusive end for @param task
		function getExEnd(task) {
			var set = [];
			getExclusiveSet(task, set);
			var end = set[0].end;
			for (var i = 0; i < set.size(); ++i)
				end = Math.max(end, set[i].end);
			return new Date(end);
		}

		var set = [];
		//		if ($('#useHeightCheckBoxId').is(':checked') && (items.size() > 0)) {
		if (items.size() > 0) {
			getExclusiveSet(items[0], set);
			exclusiveHeight(items[0], set);
		}

		/*	Calculates the exclusive heights of all tasks in @param set
		 that are reachable from the root @param task.	*/
		function exclusiveHeight(task, set) {

			// if this task is not a leaf, check its successors
			if (intersection(getSuccessors(task), set).size() > 0) {

				// evaluate all tasks below this one
				$.each(intersection(getSuccessors(task), set), function (i, successor) {
					successor.height = exclusiveHeight(successor, set);
				});

				// evaluate this task's height
				$.each(intersection(getSuccessors(task), set), function (i, successor) {

					// if the successor is a leaf, add its height to this task
					if (intersection(getPredecessors(successor), set).size() == 1) {
						task.height += Math.max(successor.height, 1);

						// remove this successor from the set
						set.splice(set.indexOf(successor), 1);
					}
				});
				$.each(intersection(getSuccessors(task), set), function (i, successor) {

					// if the successor has multiple parents, share its height between its parents
					if (intersection(getPredecessors(successor), set).size() != 1) {

						var oldList = getPredecessors(successor);
						var newList = [];
						for (var i = 0; i < oldList.size(); ++i)
							if (oldList[i].successors.size() == 1)
								newList.push(oldList[i]);
						if (newList.size() > 0)
							oldList = newList;
						if (newList.size() == 1) {
							newList[0].exChild = successor;
							successor.exParent = newList[0];
						}
						for (var i = 0; i < successor.height; ++i)
							++(oldList.sort(function (a, b) { return a.height - b.height })[0].height)
					}

					// remove this successor from the set
					set.splice(set.indexOf(successor), 1);
				});

				// this successor's is a leaf so its height is automatically 1
			} else {
				task.height = Math.max(1, task.height);
			}

			return task.height;
		}

		// gets the intersection of two sets
		function intersection(set1, set2) {
			var set3 = [];
			for (var i = 0; i < set1.size(); ++i)
				if (set2.indexOf(set1[i]) != -1)
					set3.push(set1[i]);
			return set3;
		}
	}

	// ensures times are correct
	function calculateTimes(task, checking) {
		if (!task.start) {
			if (task.predecessors.length == 0) {
				task.start = task.startInitial;
			} else {
				checking.push(task);
				var latest = 0;
				for (var i = 0; i < task.predecessors.length; ++i) {
					var tmp = calculateTimes(task.predecessors[i].predecessor, checking);
					if (tmp > latest)
						latest = tmp;
				}
				checking.pop(task);
				task.start = latest;
			}
		}
		if (!task.end) {
			if (task.root) {
				task.start = new Date(task.start.getTime() - 2);
				task.end = new Date(task.start.getTime() - 1);
			} else {
				task.end = new Date(task.start.getTime() + (task.endInitial.getTime() - task.startInitial.getTime()));
			}
		}
		return task.end;
	}

	// helper function for building the dependency list
	function binarySearch(list, key, imin, imax) {
		if (imax < imin) return -1;
		var imid = Math.round((imin + imax) / 2);
		if (list[imid].id > key) return binarySearch(list, key, imin, imid - 1);
		else if (list[imid].id < key) return binarySearch(list, key, imid + 1, imax);
		else return imid;
	}

	// gets the current time as a Date object
	function now() {
		var now = new Date();
		return new Date(now.getTime());
	}

	// returns true if all of task d's predecessors are completed
	function isReady(d) {
		for (var i = 0; i < d.predecessors.length; ++i)
			if (d.predecessors[i].predecessor.status != 'Completed')
				return false;
		return true
	}

	// returns true if the event was a left click, return false if it was a different kind of click
	function isLeftClick() {
		if (d3.event.isLeftClick == undefined)
			return d3.event.button == 0
		return d3.event.isLeftClick()
	}
}

function submitForm() {
	$('.chart').remove();
	d3.select('#svgContainerId').style('display', 'none')
	generateGraph($('#moveEventId').val());
}


/**
 * Call the REST API to grab the data to generate the Timeline
 */
function generateGraph(event) {
	var params = {};

	if (event != 0) {
		params = { 'moveEventId': event };
	}

	params.viewUnpublished = $('#viewUnpublishedId').is(':checked') ? '1' : '0';

	$('#spinnerId').css('display', 'block');

	jQuery.ajax({
		dataType: 'json',
		url: 'taskTimelineData',
		data: params,
		type: 'GET',
		complete: buildGraph
	});
}

// highlight tasks matching the user's regex
function performSearch() {
	if ($('svg#timelineSVGId') != null) {
		var searchString = $('#searchBoxId').val();
		var data = getData();
		var hasSlashes = (searchString.length > 0) && (searchString.charAt(0) == '/' && searchString.charAt(searchString.length - 1) == '/');
		var isRegex = false;
		var regex = /.*/;


		// check if the user entered an invalid regex
		if (hasSlashes) {
			try {
				regex = new RegExp(searchString.substring(1, searchString.length - 1));
				isRegex = _.isRegExp(regex);
			} catch (e) {
				alert(e);
				//$('#searchBoxId').val('');
				searchString = '';
			}
		}

		if (data) {
			data.searchFilter = searchString;
			handleClearFilterStatus();

			_(data.items).forEach(function (task, i) {

				var name = task.name;

				if (searchString == '') {
					task.highlight = false;
				} else {
					if (isRegex && name.match(regex) != null)
						task.highlight = true;
					else if (!isRegex && name.toLowerCase().indexOf(searchString.toLowerCase()) != -1)
						task.highlight = true;
					else
						task.highlight = false;
				}
			});
			forceDisplay();
		}
	}
	return false;
}

function clearFilter() {
	$('#searchBoxId').val('');
	handleClearFilterStatus();
	performSearch();
}

function handleClearFilterStatus() {
	if ($('#searchBoxId').val() != '')
		$('#filterClearId').removeClass('disabled');
	else
		$('#filterClearId').addClass('disabled');
}

/**
 * Parse start date that comes in zulu format and apply the time zone offset to
 * have a Date object in the user's time zone.
 */
function parseStartDate(startDate) {
	var momentTZ = moment().tz(tdsCommon.timeZone());
	var localTZOffset = new Date().getTimezoneOffset();
	var momentStartDate = tdsCommon.parseDateTimeFromZulu(startDate);
	momentStartDate = momentStartDate.tz("GMT");
	momentStartDate = momentStartDate.add(momentTZ.utcOffset() + localTZOffset, 'minutes');
	return new Date(momentStartDate.valueOf());
}

function exportCriticalPath() {
	var eventId = $('#moveEventId').val();
	if (eventId && (eventId != 0) && (!$('#exportCriticalPathButton').hasClass('disabledLabel'))) {
		window.open(tdsCommon.createAppURL("/task/eventTimelineResults?showAll=true&eventId=" + eventId), '_blank');
	}
}

var _MS_PER_MIN = 60000,
	_MS_PER_HOUR = 3.6e+6,
	_MS_PER_DAY = 8.64e+7,
	_MS_PER_WEEK = 6.048e+8,
	_MS_PER_MONTH = 2.628e+9,
	_MS_PER_YEAR = 31535965440.0381851,
	_MAX_TICK_PERFORMANCE = 40;

/**
 * Utility method to calculate the difference between  two dates based on a definition
 * This doesn't include the last day as a part of the calc. doesn-t really matter.
 * @param date1 the init Start Date
 * @param date2 the last End Date
 * @param _DEF
 * @returns {number} that represent the conversion defined by the _DEF (definition)
 */
function dateDiffByDef(date1, date2, _DEF) {
	// Let-'s remove all timezone, since we only need the time lapse
	var utc1 = Date.UTC(date1.getFullYear(), date1.getMonth(), date1.getDate());
	var utc2 = Date.UTC(date2.getFullYear(), date2.getMonth(), date2.getDate());

	return Math.floor((utc2 - utc1) / _DEF);
}

/**
 * Calculate the specific period between two dates
 * @param startDate
 * @param endDate
 * @param period 'years', 'months', 'weeks', 'days', 'hours', 'minutes'
 * @returns {number}
 */
function specifTimeDiff(startDate, endDate, period) {
	var start = moment(startDate);
	var end = moment(endDate);
	return end.diff(start, period);
}

function applyScaleDefinition(msConversion, scale, differScaleTime) {
	if (msConversion === _MS_PER_MIN) {
		scale.time = d3.time.minute;
		scale.tick = 10;
		scale.format = d3.time.format('%I:%M%p');
	}
	if (msConversion === _MS_PER_HOUR) {
		scale.time = d3.time.hour;
		scale.tick = 5;
		scale.format = !tdsCommon.isFormatMMDDYYYY() ? d3.time.format('%d %b, %I %p') : d3.time.format('%b %d, %I %p');
	}
	if (msConversion === _MS_PER_DAY) {
		scale.time = d3.time.day;
		scale.tick = 1;
		scale.format = !tdsCommon.isFormatMMDDYYYY() ? d3.time.format('%d %b') : d3.time.format('%b %d');
	}
	if (msConversion === _MS_PER_WEEK) {
		scale.time = d3.time.week;
		scale.tick = 2;
		scale.format = !tdsCommon.isFormatMMDDYYYY() ? d3.time.format('%d %b') : d3.time.format('%b %d');
	}
	if (msConversion === _MS_PER_MONTH) {
		scale.time = d3.time.month;
		scale.tick = 1;
		scale.format = d3.time.format('%b %Y');
	}
	if (msConversion === _MS_PER_YEAR) {
		scale.time = d3.time.year;
		scale.tick = 1;
		scale.format = d3.time.format('%Y');
	}
}

/**
 *  Applies the percent of two specif dates
 * @param startDate
 * @param endDate
 * @param percent
 * @returns {number}
 */
function getTimeLinePercent(startDate, endDate, percent) {
	// Calculate the 10% between two dates
	var time10Perc = endDate.getTime() - startDate.getTime();
	return time10Perc * percent / 100;
}

/**
 * @param startDate the init Start Date
 * @param endDate the last End Date
 * @param increasePer string that represent how we should handle the expo
 * It calculate the tick number based on two dates
 * it returns an object that represent the best d3.time.xxx, d3.time.format(xxx) and the tick jump
 */
function getTimeFormatToDraw(startDate, endDate, increasePer) {

	var msConversion = [_MS_PER_MIN, _MS_PER_HOUR, _MS_PER_DAY, _MS_PER_WEEK, _MS_PER_MONTH, _MS_PER_YEAR],
		difference = 0,
		maxTick = _MAX_TICK_PERFORMANCE,
		scale = { tick: 10, format: d3.time.format('%H:%M'), time: d3.time.minute, zoomTime: 30 * _MS_PER_MIN };


	for (var i = 0; i < msConversion.length; i++) {

		if (increasePer && msConversion[i] === _MS_PER_MIN) {
			maxTick = 25;
		}

		if (increasePer && msConversion[i] === _MS_PER_WEEK) {
			maxTick = 25;
		}

		difference = dateDiffByDef(startDate, endDate, msConversion[i]);
		if (difference <= maxTick) {
			applyScaleDefinition(msConversion[i], scale, true);

			if (increasePer && msConversion[i] === _MS_PER_HOUR) {
				scale.tick = 1;
				scale.format = d3.time.format('%I:%M%p');

				var hours = specifTimeDiff(startDate, endDate, 'hours');
				if (!isNaN(hours) && hours >= 10) {
					scale.tick = 2;
				}

				if (!isNaN(hours) && hours >= 20) {
					scale.tick = 5;
					scale.format = !tdsCommon.isFormatMMDDYYYY() ? d3.time.format('%d %b, %I %p') : d3.time.format('%b %d, %I %p');
				}

			}

			if (increasePer && msConversion[i] === _MS_PER_WEEK) {
				scale.format = !tdsCommon.isFormatMMDDYYYY() ? d3.time.format('%d %b (week %W)') : d3.time.format('%b %d (week %W)');
			}

			if (increasePer && msConversion[i] === _MS_PER_DAY) {
				var weeks = specifTimeDiff(startDate, endDate, 'weeks');
				if (!isNaN(weeks) && weeks >= 2) {
					scale.tick = scale.tick + 2;
				}
			}


			if (increasePer && msConversion[i] === _MS_PER_MIN) {
				var hours = specifTimeDiff(startDate, endDate, 'hours');
				if (!isNaN(hours) && hours >= 2) {
					scale.tick = scale.tick + 20;
				}
			}

			if (increasePer && msConversion[i] === _MS_PER_WEEK) {
				var months = specifTimeDiff(startDate, endDate, 'months');
				if (!isNaN(months) && months >= 1) {
					scale.tick = scale.tick + months;
				}
			}

			scale.zoomTime = getTimeLinePercent(startDate, endDate, 10);

			break;
		}
	}

	// Return the config
	return scale;
}
