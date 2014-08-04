
// global functions for accessing the graph outside the scope of the main function
var getData = function () {return null}
var forceDisplay = function () {return null}

$(document).ready(function () {
	// check keyup on the search field for the enter key
	$('#searchBoxId').keyup( function (e) {
		if (e.keyCode == 13)
			$('#SubmitButtonId').submit();
	});
	generateGraph();
});

function buildGraph (response, status) {
	
	// show the loading spinner
	$('#spinnerId').css('display', 'block');
	
	// calculate the position for the filter clear button
	$('#filterClearId').css('left', function () {
		return $('#searchBoxId').offset().left + $('#searchBoxId').outerWidth() - 20;
	});
	
	// check for errors in the ajax call
	if (status == 'error') {
		var message = d3.select('div.body')
			.append('div')
			.attr('class','chart');
		if (response.responseText == 'cyclical')
			message.html('This event\'s task data contains a cyclical dependency sturcture, so no graph can be generated');
		else
			message.html('<br />not enough task data to create a graph for this event');
		$('#spinnerId').css('display', 'none');
		return;
	}

	var data = $.parseJSON(response.responseText);
	var ready = false;
	
	// populate the roles select
	$("#rolesSelectId").children().remove();
	$("#rolesSelectId").append('<option value="ALL">Show all</option>');
	$.each(data.roles, function (index, role) {
		$("#rolesSelectId").append('<option value="' + role + '">' + role + '</option>');
	});
	$("#rolesSelectId").val('ALL');
	$('#rolesSelectId').on('change', function () {
		display(true, true);
	});
	
	// handle the checkbox to toggle hiding of redundant dependencies
	var hideRedundant = $('#hideRedundantCheckBoxId').is(':checked');
	$('#hideRedundantCheckBoxId').on('change', function (a, b) {
		hideRedundant = a.target.checked;
		if (hideRedundant)
			$('.redundant').addClass('hidden');
		else
			$('.redundant').removeClass('hidden');
	});
	
	// graph defaults
	var miniRectHeight = 5;
	var mainRectHeight = 30;
	var initialExtent = 1000000;
	var anchorOffset = 10; // the length of the "point" at the end of task polygons
	var margin = {top: 20, right: 0, bottom: 15, left: 0};
	var items = data.items;
	var starts = data.starts;
	var dependencies = [];
	var siblingGroups = [];
	var siblingGroupsReduced = [];
	
	sanitizeData(items, dependencies);
	
	// sort the tasks chronologically for the stacking algorithm
	items.sort( function (a,b) {
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

	var x = d3.time.scale()
		.domain([new Date(data.startDate), items[items.length-1].end])
		.range([0, $('div.body').innerWidth() - 20 - $('div.body').offset().left]);
	
	var maxStack = calculateStacks();
	var miniHeight = ((maxStack+1)*miniRectHeight);
	var mainHeight = ((maxStack+1)*mainRectHeight*1.5);

	var width = x.range()[1];
	var height = mainHeight + miniHeight + margin.top + margin.bottom;
	var zoomScale = 2;
	var x1 = d3.time.scale().domain(x.domain()).range([0, width*zoomScale]);

	var taskSelected = null;
	var xPrev = 0;
	var dragging = false;


	// construct the SVG
	var chart = d3.select('div.body')
		.append('svg:svg')
		.attr('id', 'timelineSVGId')
		.attr('width', width + margin.right + margin.left)
		.attr('height', height + margin.top + margin.bottom + 20)
		.attr('class', 'chart unselectable');

	chart.append('defs').append('clipPath')
		.attr('id', 'clip')
		.append('rect')
			.attr('width', width)
			.attr('height', mainHeight);

	// construct the mini graph
	var mini = chart.append('g')
		.attr('transform', 'translate(' + margin.left + ',' + margin.top + ')')
		.attr('width', width)
		.attr('class', 'mini')
		.attr('height', miniHeight)
		.on('mousedown', drawStart)
		.on('mousemove', drawMove)
		.on('mouseup', drawEnd)
		.on('mouseleave', drawEnd);
	
	var tempBrush = null;
	var tempBrushXInitial = 0;
	var drawing = false;
		
	function drawStart () {
		tempBrushXInitial = d3.mouse(chart.node())[0] - margin.left;
		
		if (x.invert(tempBrushXInitial) > brush.extent()[1] || x.invert(tempBrushXInitial) < brush.extent()[0]) {
			drawing = true;
			tempBrush = mini.append('rect')
				.attr('class', 'tempBrush')
				.attr('width', 0)
				.attr('height', miniHeight)
				.attr('x', d3.mouse(chart.node())[0] - margin.left)
				.attr('y', 0);						
		} else {
			tempBrushXInitial = 0;
			drawing = false;
		}
	}
	
	function drawMove () {
		if (drawing) {
			tempBrush
				.attr( 'x', Math.min(tempBrushXInitial, d3.mouse(chart.node())[0] - margin.left) )
				.attr( 'width', Math.abs(tempBrushXInitial - (d3.mouse(chart.node())[0] - margin.left)) );
			display(false, false);
		} else {
			if (tempBrush != null)
				tempBrush.remove();
			tempBrush = null;
			tempBrushXInitial = 0;
		}
	}
	
	function drawEnd () {
		if (drawing) {
			var xa = Math.min(tempBrushXInitial, d3.mouse(chart.node())[0] - margin.left);
			var xb = Math.abs(tempBrushXInitial - (d3.mouse(chart.node())[0] - margin.left));
			brush.extent([x.invert(xa), x.invert(xa+xb)]);
			
			tempBrush.remove();
			tempBrush = null;
			tempBrushXInitial = 0;
			drawing = false;
			display(true, true);
			display(false, false);
		}
		display(false, false);
	}
	
	// Sets the custom dragging behavior
	var panBehavior = d3.behavior.drag()
		.on("dragstart", dragStart)
		.on("drag", dragMove)
		.on("dragend", dragEnd);
		
	// construct the main graph
	var main = chart.append('g')
		.attr('transform', 'translate(' + margin.left + ',' + (miniHeight + 60) + ')')
		.attr('width', width)
		.attr('height', mainHeight)
		.attr('class', 'main')
		.append('g')
		.attr('class', 'mainContent')
		.call(panBehavior)
	
	var mainSelection = null;
	var mainSelectionXInitial = 0;
	var selectingMain = false;
	var mainTranslate = 0;
	
	// handle panning and time selection behavior on the main graph
	function dragStart () {
		dragging = false;
		if (d3.event.sourceEvent.shiftKey) {
			selectingMain = true;
			mainSelectionXInitial = d3.mouse(chart.node())[0] - margin.left;
			mainSelection = main.append('rect')
				.attr('class', 'tempBrush')
				.attr('width', 0)
				.attr('height', mainHeight)
				.attr('x', mainSelectionXInitial)
				.attr('y', 0);
		}
	}
	
	// handle panning and time selection behavior on the main graph
	function dragMove () {
		if (Math.abs(d3.event.dx) > 1)
			dragging = true;
		if (selectingMain) {
			mainSelection
				.attr( 'x', Math.min(mainSelectionXInitial, d3.mouse(chart.node())[0] - margin.left) )
				.attr( 'width', Math.abs(mainSelectionXInitial - (d3.mouse(chart.node())[0] - margin.left)) );
		} else {
			var offset = -1 * x(brush.extent()[0]) - offsetInitial;
			var minExtent = brush.extent()[0];
			var maxExtent = brush.extent()[1];
			var timeShift = (maxExtent - minExtent) * (d3.event.dx / width);
//					if (minExtent-timeShift > x.domain()[0] && maxExtent-timeShift < x.domain()[1]) {
				brush.extent([new Date(minExtent-timeShift), new Date(maxExtent-timeShift)]);
				mainTranslate += d3.event.dx;
				display(false, false);
//					}
		}
	}
	
	// handle panning and time selection behavior on the main graph
	function dragEnd () {
		if (selectingMain) {
			var a = Math.min(mainSelectionXInitial, d3.mouse(chart.node())[0] - margin.left);
			var b = Math.abs(mainSelectionXInitial - (d3.mouse(chart.node())[0] - margin.left)) + Math.min(mainSelectionXInitial, d3.mouse(chart.node())[0] - margin.left);
			var extentWidth = x(brush.extent()[1]) - x(brush.extent()[0]);
			var width = x.range()[1];
			var xa = x(brush.extent()[0]) + (a/width)*extentWidth;
			var xb = x(brush.extent()[0]) + (b/width)*extentWidth;
			brush.extent([x.invert(xa), x.invert(xb)]);
		
			selectingMain = false;
			
			mainSelection.remove();
			mainSelection = null;
			mainSelectionXInitial = 0;
			display(true, true);
			display(false, false);
		} else {
			display(false, false);
			mainTranslate = 0;
		}
	}
	
	function getViewWidth () {
		
	}
	
	// construct the area behind the objects
	var background = main.append('rect')
		.attr('width', width)
		.attr('height', mainHeight)
		.attr('class', 'background')
		
	// define the arrowhead marker used for marking dependencies
	var arrowheadNames = ["arrowhead", "arrowheadSelected", "arrowheadRedundant", "arrowheadCyclical"];
	for (var i = 0; i < arrowheadNames.size(); ++i)
		chart.select("defs")
			.append("marker")
			.attr("id", arrowheadNames[i])
			.attr("viewBox", "0 -5 10 10")
			.attr("refX", 10)
			.attr("refY", 0)
			.attr("markerWidth", 6)
			.attr("markerHeight", 6)
			.attr("orient", "auto")
			.append("path")
			.attr("d", "M0,-5L10,0L0,5");
		
	// construct the minutes axis for the main graph
	var xMinuteAxis = d3.svg.axis()
		.scale(x)
		.orient('bottom')
		.ticks(d3.time.minutes, 10)
		.tickFormat(d3.time.format('%H:%M'))
		.tickSize(6, 0, 0);

	// construct the minutes axis for the mini graph
	var x1MinuteAxis = d3.svg.axis()
		.scale(x1)
		.orient('bottom')
		.ticks(d3.time.minutes, 15)
		.tickFormat(d3.time.format('%H:%M'))
		.tickSize(6, 0, 0);

	// construct the hours axis for the main graph
	var xHourAxis = d3.svg.axis()
		.scale(x)
		.orient('top')
		.ticks(d3.time.hours, 1)
		.tickFormat(d3.time.format('%H:%M'))
		.tickSize(6, 0, 0);

	// construct the hours axis for the mini graph
	var x1HourAxis = d3.svg.axis()
		.scale(x1)
		.orient('top')
		.ticks(d3.time.hours, 0.5)
		.tickFormat(d3.time.format('%H:%M'))
		.tickSize(6, 0, 0);

	main.append('g')
		.attr('transform', 'translate(0,' + mainHeight + ')')
		.attr('class', 'main axis minute')
		.call(x1MinuteAxis);

	main.append('g')
		.attr('transform', 'translate(0,0.5)')
		.attr('class', 'main axis hour')
		.call(x1HourAxis)

	mini.append('g')
		.attr('transform', 'translate(0,' + miniHeight + ')')
		.attr('class', 'axis minute')
		.call(xMinuteAxis);

	mini.append('g')
		.attr('transform', 'translate(0,0.5)')
		.attr('class', 'axis hour')
		.call(xHourAxis)

	// construct the line representing the current time
	main.append('line')
		.attr('y1', 0)
		.attr('y2', mainHeight)
		.attr('class', 'main todayLine')
		
	mini.append('line')
		.attr('x1', x(now()) + 0.5)
		.attr('y1', 0)
		.attr('x2', x(now()) + 0.5)
		.attr('y2', miniHeight)
		.attr('class', 'todayLine');

	// construct the container for the task polygons
	var itemPolys = main.append('g')
		
	// construct the container for the dependency lines
	var itemArrows = main.append('g')

	// construct the container for the task labels
	var itemLabels = main.append('g')
		
	// construct the polys in the mini graph
	var miniPolys = mini.append('g')
		.attr('class', 'miniItems')
		.selectAll('polygon')
		.data(items, function (d) { return d.id; })
		.enter()
		.append('polygon')
		.attr('id', function(d) { return 'mini-' + d.id; })
		.attr('class', 'miniItem')
		.attr('points', function(d) { return getPointsMini(d); });
	
	// invisible hit area to move around the selection window
	mini.append('rect')
		.attr('pointer-events', 'painted')
		.attr('width', width)
		.attr('height', miniHeight)
		.attr('visibility', 'hidden')
		.on('mouseup', moveBrush);

	// draw the selection area
	var brush = d3.svg.brush()
		.x(x)
		.on("brush", brushed)
		.extent([new Date(data.startDate), new Date( Math.min( new Date(data.startDate).getTime() + 30 * 60000, x.domain()[1].getTime() ) )])
	
	var extentWidth = x(brush.extent()[1]) - x(brush.extent()[0]);
	zoomScale = extentWidth / width;
	x1.range([0, width / zoomScale]);

	mini.append('g')
		.attr('class', 'x brush')
		.call(brush)
		.on("mousedown", function () {return})
		.call(d3.behavior.drag())
		.selectAll('rect')
			.attr('y', 1)
			.attr('height', miniHeight - 1);

	mini.selectAll('rect.background').remove();

	// shift the today line every 100 miliseconds
	setInterval(function () {
		main.select('.main.todayLine')
			.attr('x1', x1(now()) + 0.5)
			.attr('x2', x1(now()) + 0.5);
		mini.select('.todayLine')
			.attr('x1', x(now()) + 0.5)
			.attr('x2', x(now()) + 0.5);
	}, 100);
	
	$('#spinnerId').css('display', 'none');
	$('#mainHeightFieldId').keyup(fullRedraw);
	var offsetInitial =  -1 * x(brush.extent()[0]);
	ready = true;
	display(true, true);
			
	// calculate the position for the filter clear button
	$('#filterClearId').css('left', function () {
		return $('#searchBoxId').offset().left + $('#searchBoxId').outerWidth() - 20;
	});
	
	/* define the global accessor functions */
	
	getData = function () {
		return data;
	}
	
	forceDisplay = function () {
		display(false, true);
	}
	
	// called when the brush is dragged
	function brushed () {
		drawing = false;
		if (d3.event.mode == "move")
			display(false, false);
		else
			display(false, true);
	}
	
	// updates the svg
	function display (resized, scaled) {
		if (!ready)
			return;
		
	
		var startTime = performance.now();
		
		var polys, labels, lines;
		var minExtent = brush.extent()[0];
		var maxExtent = brush.extent()[1];
		var visItems = items;
		var visDeps = dependencies;
		

		mini.select('.brush').call(brush.extent([minExtent, maxExtent]));
		var offset = -1 * x1(brush.extent()[0]) - offsetInitial;
		itemPolys.attr('transform', 'translate(' + offset + ', 0)');
		itemArrows.attr('transform', 'translate(' + offset + ', 0)');
		itemLabels.attr('transform', 'translate(' + offset + ', 0)');
			
		if ( ! scaled )
			return;
		
		var extentWidth = x(brush.extent()[1]) - x(brush.extent()[0]);
		zoomScale = extentWidth / width;
		x1.range([0, width / zoomScale]);
		
		$.each(items, function (i, d) {
			d.points = getPoints(d);
		});

		// update any existing item polys
		polys = itemPolys.selectAll('polygon')
			.data(visItems, function (d) { return d.id; })
			.attr('points', function(d) {
				var p = d.points;
				return p.x + ',' + p.y + ' '
					 + p.x2 + ',' + p.y + ' '
					 + (p.x+p.w) + ',' + p.y2 + ' '
					 + p.x2 + ',' + (p.y+p.h) + ' '
					 + p.x + ',' + (p.y+p.h) + ' ';
			})
			.attr('class', function(d) { return 'mainItem ' + getClasses(d); });
		
		// add any task polys in the new domain extents
		polys.enter()
			.append('polygon')
			.attr('points', function(d) {
				var p = d.points;
				return p.x + ',' + p.y + ' '
					 + p.x2 + ',' + p.y + ' '
					 + (p.x+p.w) + ',' + p.y2 + ' '
					 + p.x2 + ',' + (p.y+p.h) + ' '
					 + p.x + ',' + (p.y+p.h) + ' ';
			})
			.attr('class', function(d) { return 'mainItem ' + getClasses(d); })
			.attr('id', function(d) { return 'task-' + d.id; })
			.on("click", function(d) {
				toggleTaskSelection(d);
				display(false, false);
			})
			.on("dblclick", function(d) {
				showAssetComment(d.id, 'show');
			})
			.append('title')
			.html(function(d) { return d.number + ': ' + d.name + ' - ' + d.assignedTo + ' - ' + d.status; });
			
		polys.exit().remove();
		
		// update any existing dependency lines
		lines = itemArrows.selectAll('line')
			.data(visDeps, function (d) { return d.predecessor.id + '-' + d.successor.id; })
			.attr('x1', function(d) {
				var start = x1(d.predecessor.start);
				var end = x1(d.predecessor.end);
				var lowest = start + (end - start) * 0.75;
				return Math.round(Math.max( lowest, end-anchorOffset ));
			})
			.attr('x2', function(d) {
				var start = x1(d.successor.start);
				var end = x1(d.successor.end);
				var highest = start + (end - start) * 0.25;
				return Math.round(Math.min( highest, start+anchorOffset ));
			})
			.attr('class', function(d) { return 'dependency mainItem ' + getClasses(d); });
		
		if (resized)
			lines
				.attr('y1', function(d) { return d.predecessor.points.y2; })
				.attr('y2', function(d) { return d.successor.points.y2; })
		
		// add any dependency lines in the new domain extents
		lines.enter().insert('line')
			.attr('x1', function(d) {
				var start = x1(d.predecessor.start);
				var end = x1(d.predecessor.end);
				var lowest = start + (end - start) * 0.75;
				return Math.round(Math.max( lowest, end-anchorOffset ));
			})
			.attr('x2', function(d) {
				var start = x1(d.successor.start);
				var end = x1(d.successor.end);
				var highest = start + (end - start) * 0.25;
				return Math.round(Math.min( highest, start+anchorOffset ));
			})
			.attr('y1', function(d) { return d.predecessor.points.y2; })
			.attr('y2', function(d) { return d.successor.points.y2; })
			.attr('id', function(d) { return 'dep-' + d.predecessor.id + '-' + d.successor.id; })
			.attr('class', function(d) { return 'dependency mainItem ' + getClasses(d); });
		
	
		// move any selected lines to the top of the DOM
		lines.sort(function (a, b) { return a.selected - b.selected; });
		lines.exit().remove();

		
		// update the item labels
		labels = itemLabels.selectAll(function() { return this.getElementsByTagName("foreignObject"); })
			.data(visItems, function (d) { return d.id; })
			.attr('x', function(d) { return d.points.x; })
			.attr('width', function(d) { return Math.max(0, d.points.w - anchorOffset); })
			.attr('class', function(d) { return 'itemLabel unselectable mainItem ' + getClasses(d);} );
			
		if (resized)
			labels
				.attr('y', function(d) { return d.points.y; })
				.attr('height', function(d) { return d.points.h; })
		
		// update the item labels' children
		itemLabels.selectAll(function() { return this.getElementsByTagName("foreignObject"); })
			.selectAll(':first-child')
			.attr('style', function(d) { return 'height: ' + d.points.h + 'px !important;max-width: ' + Math.max(0, d.points.w - anchorOffset) + 'px !important;'; })
			.selectAll(':first-child')
			.selectAll(':first-child')
			.attr('style', function(d) { return 'width: ' + (d.points.w - anchorOffset) + 'px !important;max-width: ' + Math.max(0, d.points.w - anchorOffset) + 'px !important;'; });
		
		// add any labels in the new domain extents
		labels.enter().append('foreignObject')
			.attr('class', function(d) { return 'itemLabel unselectable mainItem ' + getClasses(d);} )
			.attr('id', function(d) { return 'label-' + d.id; })
			.attr('x', function(d) { return d.points.x; })
			.attr('y', function(d) { return d.points.y; })
			.attr('width', function(d) { return Math.max(0, d.points.w - anchorOffset); })
			.attr('height', function(d) { return d.points.h; })
			.append('xhtml:body')
			.attr('style', function(d) { return 'height: ' + d.points.h + 'px !important;max-width: ' + Math.max(0, d.points.w - anchorOffset) + 'px !important;'; })
			.attr('class', 'itemLabel')
			.append('div')
			.attr('class', 'itemLabel')
			.attr('align', 'center')
			.append('p')
			.attr('class', 'itemLabel')
			.attr('style', function(d) { return 'width: ' + (d.points.w - anchorOffset) + 'px !important;max-width: ' + Math.max(0, d.points.w - anchorOffset) + 'px !important;'; })
			.html(function (d) { return d.number + ': ' + d.name; });

		labels.exit().remove();
		
		// updates the mini graph
		miniPolys
			.attr('class', function(d) { return 'miniItem ' + getClasses(d); });
		
		console.log('display(' + resized + ') took ' + (performance.now() - startTime) + ' ms');
	}
	
	// clears all items from the main group then redraws them
	function fullRedraw () {
		itemPolys.selectAll('polygon').remove();
		itemArrows.selectAll('line').remove();
		itemLabels.selectAll(function() { return this.getElementsByTagName("foreignObject"); }).remove();
		display(true, true);
	}
	
	// gets the css classes that apply to task @param d
	function getClasses (d) {
		var classString = ''
			+ (d.selected ? 'selected ' : '')
			+ (d.milestone ? 'milestone ' : '')
			+ (d.criticalPath ? 'critical ' : '')
			+ (d.root ? 'root ' : '')
			+ (d.redundant ? 'redundant ' : '')
			+ (d.cyclical ? 'cyclical ' : '')
			+ (d.end < now() ? 'past ' : 'future ')
			+ (d.highlight ? 'highlighted ' : '')
			+ (hideRedundant ? 'hidden ' : '')
			+ (d.status);
		if (d.status != 'Completed' && d.end < now())
			classString += ' overdue ';
		else if (d.status == 'Completed' && d.end > now())
			classString += ' ahead ';
		else
			classString += ' ontime ';
		if ($('#rolesSelectId').val() != 'ALL' && $('#rolesSelectId').val() != d.role)
			classString += ' unfocussed ';
		return classString;
	}
	
	// gets the points string for task polygons
	function getPoints (d) {
		var points = {};
		var x = x1(d.start);
		var offset = Math.floor((Math.max(1, d.height)-1)/2);
		var y = (d.stack-offset) * mainRectHeight + 0.4 * mainRectHeight + 0.5;
		var w = x1(d.end) - x1(d.start);
		var h = (mainRectHeight) * Math.max(1, d.height) - (mainRectHeight * 0.2);
		var x2 = x + w - anchorOffset;
		var y2 = y + (h/2);
		return {x:x, y:y, x2:x2, y2:y2, w:w, h:h};
	}
	
	// gets the points string for mini polygons
	function getPointsMini (d) {
		var offset = Math.floor((Math.max(1, d.height)-1)/2);
		var xa = x(d.start);
		var ya = (d.stack - offset) * (miniRectHeight);
		var w = x(d.end) - x(d.start);
		var h = (miniRectHeight) * Math.max(1, d.height);
		return xa + ',' + ya + ' '
			 + (xa+w) + ',' + ya + ' '
			 + (xa+w) + ',' + (ya+h) + ' '
			 + xa + ',' + (ya+h) + ' ';
		
	}
	
	// used to get the offset used for dependency arrows' links to the task rects
	function getAnchorLocation (d) {
		var p = d.predecessor;
		var s = d.successor;
		var ps = p.start.getTime();
		var pe = p.end.getTime();
		var ss = s.start.getTime();
		var se = s.end.getTime();
		return [ x1(new Date(ps + 0.9*(pe-ps))), x1(new Date(se - 0.9*(se-ss))) ];
	}


	// Toggles selection of a task
	function toggleTaskSelection(taskObject) {
		
		if (dragging)
			return;
		
		var selecting = true;
		if (taskSelected == null && taskObject == null)
			return; // No node is selected, so there is nothing to deselect
		
		// deselecting
		if (taskSelected == taskObject) {
			selecting = false;
			
		// selecting
		} else {
			toggleTaskSelection(taskSelected) // if another task is selected, deselect that one first
			taskSelected = taskObject;
		}

		
		// recursively style all tasks and dependencies connected to this task
		function styleDependencies (task, direction) {
			if (selecting != task.selected) {
				task.selected = selecting;
				
				if (direction == 'left' || direction == 'both')
					$.each(task.predecessors.concat(task.redundantPredecessors), function(i, d) {
						d.selected = selecting;
						styleDependencies(d.predecessor ,'left');
					});
				if (direction == 'right' || direction == 'both')
					$.each(task.successors.concat(task.redundantSuccessors), function(i, d) {
						d.selected = selecting;
						styleDependencies(d.successor ,'right');
					});
			}
		}
		
		styleDependencies(taskObject, 'both');
		
		if ( ! selecting )
			taskSelected = null;
			
		display(true, true);
	}
	
	// moves the brush to the selected location
	function moveBrush () {
		var origin = d3.mouse(this);	
		var point = x.invert(origin[0]);
		
		if ( ! brush.empty() ) {	
			var halfExtent = (brush.extent()[1].getTime() - brush.extent()[0].getTime()) / 2;
			var start = new Date(point.getTime() - halfExtent);
			var end = new Date(point.getTime() + halfExtent);
		} else {
			var halfExtent = initialExtent / 2;
			var start = new Date(point.getTime() - halfExtent);
			var end = new Date(point.getTime() + halfExtent);
		}

		brush.extent([start,end]);
		display(true, false);
	}
	
	// cuts off any labels that extend outside of their task's polygon
	function trimLabels () {
		$('.itemLabel').each(function (i, label) {
			var poly = $('#task-');
		});
	}
	
	// calculate stacking values for @param task
	function calculateStacks () {
		
		var waiting = true;
		var stack = [];
		var minIndex = 1000;
		var maxIndex = 1000;
		var rectHeight = 20;
		
		function calculateStack (task) {
			
			// check if this task has already been placed
			if (task == null || task.stack != null)
				return;
			
			// check if this task has any uncalculated predecessors
			if (hasUndefinedPredecessors(task))
				return;
			
			// the ideal location for any given task is the average of its predecessors
			var ideal = minIndex + Math.round((maxIndex-minIndex) / 2);
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
		function addTask (task, ideal, correcting) {

			// first check if the ideal location is availible
			if ( ! stack[ideal] || canFitInRow(task, stack, ideal) ) {
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
		function sortSuccessors (task) {
			var toSort = getSuccessors(task);
			var sorted = [];
			while (toSort.size() > 0) {
				for (var i = 1; i < toSort.size(); ++i) {
					if ( (toSort[0].predecessorIds.valueOf() == toSort[i].predecessorIds.valueOf()) && (getSuccessors(toSort[0]).valueOf() == getSuccessors(toSort[i]).valueOf()) ) {
						sorted.push(toSort[i]);
						toSort.splice(i, 1);
					}
				}
				sorted.push(toSort[0]);
				toSort.splice(0, 1);
			}
			
			return sorted;
		}
		
		function hasUndefinedPredecessors (task) {
			for (var i = 0; i < task.predecessors.length; ++i)
				if (task.predecessors[i].predecessor.stack == null)
					return true;
			return false;
		}
		
		function hasUndefindedSuccessors (task) {
			for (var i = 0; i < task.successors.length; ++i)
				if (task.successors[i].successor.stack == null)
					return true;
			return false;
		}
		
		function insertIntoStack (task, stack, row) {
			insertIntoStack2(task, stack, row);
		}
		function insertIntoStack2 (task, stack, row) {
			var low = Math.floor((Math.max(1, task.height)-1)/2);
			var high = Math.ceil((Math.max(1, task.height)-1)/2);
			maxIndex = Math.max(maxIndex, row + high);
			minIndex = Math.min(minIndex, row - low);
			
			for (var r = 0-low; r <= high; ++r) {
				if (stack[row+r] == null)
					stack[row+r] = [];
				stack[row+r].push([x(task.start), x(task.end)]);
			}
			task.stack = row;
		}
		
		// gets the best stack location for task (object) based on related tasks.
		function getIdealStackLocation (task, related, direction) {
			
			// if only a single parent is involved, fill its height
			if (related.size() == 1) {
				var siblings = [];
				for (var i = 0; i < getSuccessors(related[0]).size(); ++i)
					if (getPredecessors(getSuccessors(related[0])[i]).size() == 1)
						siblings.push(getSuccessors(related[0])[i]);
				var offsetFromParent = Math.floor((Math.max(1, related[0].height)-1)/2);
				var offsetFromSiblings = 0;
				var offsetFromHeight = Math.floor((Math.max(1, task.height)-1)/2);
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
			if ((sum/count) - Math.floor(sum/count) == 0.5) {
			
			
				if ((task.height % 2) == 0)
					return Math.floor(sum/count);
				return Math.ceil(sum/count);
			}
			if (sum == 0)
				return 0;
			if (Math.abs((sum/count) - Math.round(sum / count)) == 0.5) {
			
			}
				
			return Math.round(sum / count);
		}
		
		function getTimeBetween (taskA, taskB) {
			return Math.min( Math.abs(x(taskA.start)-x(taskB.end)),
				Math.abs(x(taskB.start)-x(taskA.end)) );
		}
		
		function getDependentTasks (task) {
			return getPredecessors(task).concat(getSuccessors(task));
		}
		
		// checks if a task can fit in a row in the stack
		function canFitInRow (task, stack, row) {
			var low = Math.floor((Math.max(1, task.height)-1)/2);
			var high = Math.ceil((Math.max(1, task.height)-1)/2);
			for (var r = 0-low; r <= high; ++r) {
				if (stack[row+r] != null)
					for (var i = 0; i < stack[row+r].length; ++i) {
						var startsBefore = stack[row+r][i][0] < x(task.exEnd);
						var endsAfter = stack[row+r][i][1] > x(task.start);
						if (startsBefore && endsAfter)
							return false;
					}
				else
					stack[row+r] = [];
			}
			return true;
		}
		
		calculateStack(data.root);
		
		// realigns all stack values to start from 0
		for (var i = 0; i < items.length; ++i)
			items[i].stack -= minIndex;
		
		return maxIndex - minIndex;
	}
		
	function getSuccessors (task) {
		if (!task) 
			return;
		var tasks = [];
		for (var i = 0; i < task.successors.length; ++i)
			tasks.push(task.successors[i].successor);
		return tasks;
	}
	
	function getPredecessors (task) {
		if (!task) 
			return;
		var tasks = [];
		for (var i = 0; i < task.predecessors.length; ++i)
			tasks.push(task.predecessors[i].predecessor);
		return tasks;
	}

	// increases/decreases the current brush extent. called when the user scrolls on the mini graph.
	function zoom () {
		var delta = 1;
		var halfExtent = (brush.extent()[1].getTime() - brush.extent()[0].getTime()) / 2;
		if (d3.event.sourceEvent.deltaY < 0)
			delta = 1.2;
		if (d3.event.sourceEvent.deltaY > 0 && halfExtent > 50000)
			delta = 0.8;
		var midpoint = (brush.extent()[1].getTime() + brush.extent()[0].getTime()) / 2;
		var newStart = midpoint - (halfExtent*delta);
		var newEnd = midpoint + (halfExtent*delta);
		brush.extent([new Date(newStart),new Date(newEnd)]);
		display(true, true);
	}
	
	/*	Reconstructs the data in @param tasks for d3 by:
		 - adding a "root" task when there are multiple start tasks
		 - identifying tasks where start = end as milestones
		 - removing redundant dependency links
		 - converting the start and completion times for tasks from date Strings to javascript Dates
		 - populates the dependencies list from data in the items list 
		 - corrects any impossible start times for tasks */
	function sanitizeData (tasks, dependencies) {
		
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
			var predecessor = items[binarySearch(items, key, 0, items.length-1)];
			var stack = data.cyclicals[Object.keys(data.cyclicals)[i]];
			for (var j = 0; j < stack.size(); ++j) {
				var node = items[binarySearch(items, stack[j], 0, items.length-1)];
				if (node.predecessorIds.indexOf(key) != -1) {
					// construct a dependency object and move the predecessorId to the redundant list
					var depObject = { "predecessor":predecessor, "successor":node, "modifier":"hidden", "selected":false, "redundant":true, "cyclical":true };
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
		if (starts.size() > 1) {
			var earliest = starts[0].startInitial;
			for (var i = 0; i < starts.size(); i++) {
				var task = items[binarySearch(items, starts[i], 0, items.length-1)];
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
		} else {
			data.root = items[binarySearch(items, starts[0], 0, items.length-1)];
		}
		
		// convert all data to its proper format
		var startTime = new Date(data.startDate);
		for (var i = 0; i < items.length; ++i) {
			items[i].milestone = (items[i].startInitial == items[i].endInitial);
			items[i].startInitial = new Date(startTime.getTime() + (items[i].startInitial)*60000);
			items[i].endInitial = new Date(startTime.getTime() + (items[i].endInitial + items[i].milestone)*60000);
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
			if (items[i].predecessorIds)
				for (var j = 0; j < items[i].predecessorIds.length; ++j) {
					var predecessorIndex = binarySearch(items, items[i].predecessorIds[j], 0, items.length-1)
					var depObject = { "predecessor":items[predecessorIndex], "successor":items[i], "modifier":"hidden", "selected":false, "redundant":false, "cyclical":false };
					if (predecessorIndex != -1) {
						depObject.root = items[predecessorIndex].root;
						items[predecessorIndex].successors.push(depObject);
						items[i].predecessors.push(depObject);
						dependencies.push(depObject);
					}
				}
		
		// find and remove any redundant dependencies using a queue for a breadth first search
		var queue = [];
		queue.push(data.root);
		while (queue.size() > 0)
			searchForRedundency(queue.pop());
		
		function searchForRedundency (node) {
			
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
		function searchUp (target, start, ignore, checked) {
			
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
				if ( start.predecessors[i] != ignore && searchUp(target, start.predecessors[i].predecessor, ignore, checked) )
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
					if ( (exSet.indexOf(getSuccessors(exSet[j])[k]) == -1) && (nonExclusiveSet.indexOf(getSuccessors(exSet[j])[k]) == -1) )
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
					
					var searching  = true;
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
						if ( (!inserted) && (haveSameSuccessors(newSuccessors[k].successor, oldSuccessors[j].successor, true) == 1) ) {
							newSuccessors.splice(k, 0, oldSuccessors[j]);
							location = k;
							inserted = true;
							grouped = true;
						}
					
					// keep trying, ignoring an increasing number of non-matches
					var tolerance = 0;
					while ( ! inserted ) {
						++tolerance;
						for (var k = 0; k < newSuccessors.size(); ++k) {
							if ( (!inserted) && (haveSameSuccessors(newSuccessors[k].successor, oldSuccessors[j].successor, false) <= tolerance) ) {
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
							newSuccessors = newSuccessors.sort(function (a, b) {return b.height-a.height});
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
							
							pushMatches = getMatchingGroups(bufferList[j].groups, newList[newList.length-1].groups);
							unshiftMatches = getMatchingGroups(bufferList[j].groups, newList[newList.length-1].groups);
							
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
									if ( (groups[k].indexOf(newList[0]) != -1) && (groups[k].indexOf(bufferList[j]) != -1) )
										score += usable;
							if (usable == 1 || usable == 2)
								for (var k = 0; k < groups.length; ++k)
									if ( (groups[k].indexOf(newList[0]) != -1) && (groups[k].indexOf(bufferList[j]) != -1) )
										score += usable;
							
							
							if (score < 0)
								score = (fitsConstraints && unshiftConstraintsSatisfied > 0) ? --score : 0;
							else
								score = (fitsConstraints && pushConstraintsSatisfied > 0) ? ++score : 0;
							
							if ( Math.abs(score) > Math.abs(bestScore) ) {
								bestScore = score;
								bestTask = j;
							}
							if ( Math.abs(score) > Math.abs(bestScore) ) {
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
					
					function display (list, groups) {
						console.log('--------------------------------------------');
						for (var j = 0; j < list.length; ++j) {
							var output = list[j].id + ' : ';
							for (var k = 0; k < groups.length; ++k) {
								if (groups[k].indexOf(list[j]) != -1)
									output += '\t@';
								else
									output += '\t.';
							}
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
		
		function getMatchingGroups (list1, list2) {
			var matches = 0;
			for (var i = 0; i < Object.keys(list1).length; ++i) {
				var key = Object.keys(list1)[i];
				if (list2[key] != null)
					++matches;
			}
			return matches;
		}
		
		function evaluateStack (list, groups) {
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
		
		
		function removeDuplicates (list, groups) {
			
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
		
		function outputChildMatrix (node) {
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
		function haveSameSuccessors (task1, task2, fullMatch) {
		
			var returnVal = true;
			var parents1 = task1.siblingGroupParents;
			var parents2 = task2.siblingGroupParents;
			var matches = 0;
			var possibleMatches = Math.max(parents1.size(), parents2.size());
			
			if ( (fullMatch) && (parents1.size() != parents2.size()) ) {
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
		function getExclusiveSet (node, set) {
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
		function getExHeight (task) {
			var set = [];
			getExclusiveSet(task, set);
			var height = 0;
			for (var i = 0; i < set.size(); ++i)
				if (getSuccessors(task).indexOf(set[i]) != -1)
					height += getExHeight(set[i]);
			return Math.max(1, height);
		}
		
		// gets the exclusive end for @param task
		function getExEnd (task) {
			var set = [];
			getExclusiveSet(task, set);
			var end = set[0].end;
			for (var i = 0; i < set.size(); ++i)
				end = Math.max(end, set[i].end);
			return new Date(end);
		}
		
		var set = [];
		if ($('#useHeightCheckBoxId').is(':checked')) {
			getExclusiveSet(items[0], set);
			exclusiveHeight(items[0], set);
		}
		
		/*	Calculates the exclusive heights of all tasks in @param set
			that are reachable from the root @param task.	*/
		function exclusiveHeight (task, set) {
			
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
							++(oldList.sort(function (a, b) {return a.height-b.height})[0].height)
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
		function intersection (set1, set2) {
			var set3 = [];
			for (var i = 0; i < set1.size(); ++i)
				if (set2.indexOf(set1[i]) != -1)
					set3.push(set1[i]);
			return set3;
		}
	}
	
	// ensures times are correct
	function calculateTimes (task, checking) {	
		if ( ! task.start ) {
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
		if ( ! task.end ) {
			if (task.root) {
				task.start = new Date(task.start.getTime()-2);
				task.end = new Date(task.start.getTime()-1);
			} else {
				task.end = new Date(task.start.getTime() + (task.endInitial.getTime()-task.startInitial.getTime()));
			}
		}
		return task.end;
	}
	
	// helper function for building the dependency list
	function binarySearch(list, key, imin, imax) {
		if (imax < imin) return -1;
		var imid = Math.round((imin + imax) / 2);
		if (list[imid].id > key) return binarySearch(list, key, imin, imid-1);
		else if (list[imid].id < key) return binarySearch(list, key, imid+1, imax);
		else return imid;
	}
	
	// gets the current time as a Date object
	function now () {
		var now = new Date();
		return new Date(now.getTime());
	}
	
	// returns true if all of task d's predecessors are completed
	function isReady (d) {
		for (var i = 0; i < d.predecessors.length; ++i)
			if (d.predecessors[i].predecessor.status != 'Completed')
				return false;
		return true
	}
}

function submitForm () {
	$('.chart').remove();
	generateGraph($('#moveEventId').val());
}

function generateGraph (event) {
	var params = {};
	if (event != 0)
		params = {'moveEventId':event};
	$('#spinnerId').css('display', 'block');
	
	jQuery.ajax({
		dataType: 'json',
		url: 'taskTimelineData',
		data: params,
		type:'GET',
		complete: buildGraph
	});
}

// highlight tasks matching the user's regex
function performSearch () {
	console.log('search');
	if ($('svg#timelineSVGId') != null) {
		var searchString = $('#searchBoxId').val();
		var data = getData();
		var hasSlashes = (searchString.length > 0) && (searchString.charAt(0) == '/' && searchString.charAt(searchString.length-1) == '/');
		var isRegex = false;
		var regex = /.*/;
		
			
		// check if the user entered an invalid regex
		if (hasSlashes) {
			try {
				regex = new RegExp(searchString.substring(1, searchString.length-1));
				isRegex = _.isRegExp(regex);
			} catch (e) {
				alert(e);
				//$('#searchBoxId').val('');
				searchString = '';
			}
		}
		
		data.searchFilter = searchString;
		if (searchString != '') {
			$('#filterClearId').attr('class', 'ui-icon ui-icon-closethick');
		} else {
			$('#filterClearId').attr('class', 'disabled ui-icon ui-icon-closethick');
		}
		
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
	return false;
}

function clearFilter () {
	$('#searchBoxId').val('');
	performSearch();
}