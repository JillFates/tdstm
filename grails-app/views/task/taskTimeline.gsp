<!--
The MIT License (MIT)

Copyright (c) 2013 bill@bunkat.com

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
-->

<!--
-->
<html>
	<head>
		<title>Task Timeline</title>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta name="layout" content="projectHeader" />
		<g:javascript src="asset.tranman.js"/>
		<g:javascript src="asset.comment.js" />
		<g:javascript src="entity.crud.js" />
		<g:javascript src="model.manufacturer.js"/>
		<link type="text/css" rel="stylesheet" href="${g.resource(dir:'css',file:'ui.datepicker.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.datetimepicker.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css/jqgrid',file:'ui.jqgrid.css')}" />
		<script type="text/javascript" src="${resource(dir:'d3',file:'d3.js')} "></script>
		<style>
		.unselectable {
			-webkit-touch-callout: none;
			-webkit-user-select: none;
			-khtml-user-select: none;
			-moz-user-select: moz-none;
			-ms-user-select: none;
			user-select: none;
		}

		.mini text {
			font: 9px sans-serif;
		}

		.main text {
			font: 12px sans-serif;
		}

		.month text {
			text-anchor: start;
		}

		.todayLine {
			stroke: blue;
			stroke-width: 1.5;
		}

		.axis line, .axis path {
			stroke: black;
		}
		
		polygon.miniItem {
			stroke-width: 1;
			stroke: black;
			fill: green;
		}
		polygon.miniItem.selected {
			fill: #00B0C0 !important;
		}
		
		
		polygon.mainItem {
			stroke-width: 2;
			fill: #FFFF00;
			stroke: #FFFF00;
		}
		polygon.mainItem.selected {
			stroke: blue !important;
			fill: lightblue !important;
		}
		
		polygon.mainItem.Completed {
			fill: #5478BA;
		}
		polygon.mainItem.Started {
			fill: darkturquoise;
		}
		polygon.mainItem.Ready {
			fill: lightgreen;
		}
		polygon.mainItem.Pending {
			fill: lightgrey;
		}
		
		polygon.mainItem.overdue {
			stroke: red;
		}
		polygon.mainItem.ontime {
			stroke: black;
		}
		polygon.mainItem.ahead {
			stroke: green;
		}
		
		polygon.mainItem:hover {
			fill-opacity: 0.7 !important;
		}
		
		polygon.mainItem.critical {
			stroke-width: 4;
		}
		polygon.mainItem.milestone {
			fill-opacity: 0.75;
		}

		.brush .extent {
			stroke: gray;
			fill: blue;
			fill-opacity: .165;
		}
		.tempBrush {
			fill: red;
			fill-opacity: .2;
			stroke: orange;
			stroke-opacity: .6;
		}

		.dependency {
			pointer-events: none !important;
			stroke: darkgrey;
			stroke-width: 1.5;
			marker-end: url(#arrowhead);
			stroke-opacity: 0.8;
		}

		path {
			pointer-events: none !important;
		}

		#arrowhead path {
			fill: darkgrey;
			fill-opacity: 0.8;
			stroke-opacity: 0.8;
		}

		#arrowheadSelected path {
			fill: red;
			fill-opacity: 1;
			stroke-opacity: 1;
		}

		#arrowheadRedundant path {
			fill: orange;
			fill-opacity: 0.6;
			stroke-opacity: 0.6;
		}

		#arrowheadCyclical path {
			fill: #44AA00;
			fill-opacity: 1;
			stroke-opacity: 1;
		}

		.dependency.selected {
			stroke: red;
			stroke-width: 2;
			marker-end: url(#arrowheadSelected);
			stroke-opacity: 1;
		}
		.dependency.redundant {
			stroke: orange;
			stroke-opacity: 0.5;
			marker-end: url(#arrowheadRedundant);
		}
		.dependency.cyclical {
			stroke: #44AA00;
			stroke-opacity: 1;
			stroke-width: 3;
			marker-end: url(#arrowheadCyclical);
		}
		
		.unfocussed {
			opacity: 0.4 !important;
			fill-opacity: 0.4 !important;
			stroke-opacity: 0.4 !important;
		}
		
		.background {
			fill-opacity: 0;
		}
		.itemLabel {
			background-color: transparent !important;
			cursor: default !important;
			pointer-events: none !important;
			font: 10px sans-serif !important;
		}
		.itemLabel.selected {
			fill: #AA33AA;
			font-weight: bold !important;	
		}
		body.itemLabel {
			display: table !important;
			margin-left: auto !important;
			margin-right: auto !important;
			table-layout: fixed !important;
		}
		div.itemLabel {
			display: table-cell;
			text-align: center;
			vertical-align: middle;	
		}
		.root {
			display: none !important;
		}
		
		div.body {
			width: 100%;
		}
		</style>
		<script type="text/javascript">
		
		$(document).ready(function () {
			$("#commentsListDialog").dialog({ autoOpen: false })
			$("#createCommentDialog").dialog({ autoOpen: false })
			$("#showCommentDialog").dialog({ autoOpen: false })
			$("#editCommentDialog").dialog({ autoOpen: false })
			generateGraph()
		});
		
		function buildGraph (response, status) {
		
			// check for errors in the ajax call
			if (status == 'error') {
				var message = d3.select('div.body')
					.append('div')
					.attr('class','chart');
				if (response.responseText == 'cyclical')
					message.html('This event\'s task data contains a cyclical dependency sturcture, so no graph can be generated');
				else
					message.html('not enough task data to create a graph for this event');
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
			
			
			// graph defaults
			var miniRectHeight = 5;
			var mainRectHeight = 30;
			var initialExtent = 1000000;
			var anchorOffset = 10; // the length of the "point" at the end of task polygons
			var margin = {top: 20, right: 0, bottom: 15, left: 0};
			var items = data.items;
			var starts = data.starts;
			var dependencies = [];
			
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
					.attr('width', function(d) { return (d.points.w - anchorOffset); })
					.attr('class', function(d) { return 'itemLabel unselectable mainItem ' + getClasses(d);} );
					
				if (resized)
					labels
						.attr('y', function(d) { return d.points.y; })
						.attr('height', function(d) { return d.points.h; })
				
				// update the item labels' children
				itemLabels.selectAll(function() { return this.getElementsByTagName("foreignObject"); })
					.selectAll(':first-child')
					.attr('style', function(d) { return 'height: ' + d.points.h + 'px !important;max-width: ' + (d.points.w - anchorOffset) + 'px !important;'; })
					.selectAll(':first-child')
					.selectAll(':first-child')
					.attr('style', function(d) { return 'width: ' + (d.points.w - anchorOffset) + 'px !important;max-width: ' + (d.points.w - anchorOffset) + 'px !important;'; });
				
				// add any labels in the new domain extents
				labels.enter().append('foreignObject')
					.attr('class', function(d) { return 'itemLabel unselectable mainItem ' + getClasses(d);} )
					.attr('id', function(d) { return 'label-' + d.id; })
					.attr('x', function(d) { return d.points.x; })
					.attr('y', function(d) { return d.points.y; })
					.attr('width', function(d) { return (d.points.w - anchorOffset); })
					.attr('height', function(d) { return d.points.h; })
					.append('xhtml:body')
					.attr('style', function(d) { return 'height: ' + d.points.h + 'px !important;max-width: ' + (d.points.w - anchorOffset) + 'px !important;'; })
					.attr('class', 'itemLabel')
					.append('div')
					.attr('class', 'itemLabel')
					.attr('align', 'center')
					.append('p')
					.attr('class', 'itemLabel')
					.attr('style', function(d) { return 'width: ' + (d.points.w - anchorOffset) + 'px !important;max-width: ' + (d.points.w - anchorOffset) + 'px !important;'; })
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
					+ (d.status);
				if (d.status != 'Completed' && d.end < now())
					classString += ' overdue '
				else if (d.status == 'Completed' && d.end > now())
					classString += ' ahead '
				else
					classString += ' ontime '
				if ($('#rolesSelectId').val() != 'ALL' && $('#rolesSelectId').val() != d.role)
					classString += ' unfocussed '
				
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
				var ya = (d.stack-offset) * (miniRectHeight);
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
						var offsetLow = Math.floor((Math.max(task.height, 1)-1)/2);
						var offsetHigh = Math.ceil((Math.max(task.height, 1)-1)/2);
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
						task.predecessorIds.push(10);
						earliest = Math.min(earliest, task.startInitial);
					}
					var root = {};
					root.id = 10;
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
					var nonExclusiveSet = [];
					for (var j = 0; j < exSet.size(); ++j)
						for (var k = 0; k < getSuccessors(exSet[j]).size(); ++k)
							if ( (exSet.indexOf(getSuccessors(exSet[j])[k]) == -1) && (nonExclusiveSet.indexOf(getSuccessors(exSet[j])[k]) == -1) )
								nonExclusiveSet.push(getSuccessors(exSet[j])[k]);
					if (nonExclusiveSet.size() == 1)
						items[i].endOfExclusive = nonExclusiveSet[0];
				}
						
				// group successors by their predecessors for each task
				for (var i = 0; i < items.length; ++i) {
					var grouped = false;
					if (items[i].successors.size() > 0) {
						var oldSuccessors = items[i].successors;
						var newSuccessors = [];
						for (var j = 0; j < oldSuccessors.size(); ++j) {
							var inserted = false;
							for (var k = 0; k < newSuccessors.size(); ++k)
								if (!inserted && haveSameSuccessors(newSuccessors[k].successor, oldSuccessors[j].successor)) {
									newSuccessors.splice(k, 0, oldSuccessors[j]);
									inserted = true;
									grouped = true;
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
				
				function haveSameSuccessors (task1, task2) {
					if ( (task1.endOfExclusive != null && task2.endOfExclusive != null) && (task1.endOfExclusive == task2.endOfExclusive) )
						return true;
					return false;
					var same = task1.successors.size() == task2.successors.size();
					if (same)
						for (var i = 0; i < getSuccessors(task1).size(); ++i)
							same = same && (getSuccessors(task1)[i].id == getSuccessors(task2)[i].id)
					return same;
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
		</script>
	</head>
	<body>
		<div class="body">
			<h1>Task Timeline</h1>
			<g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
			</g:if>
			Event: <g:select from="${moveEvents}" name="moveEventId" id="moveEventId" optionKey="id" optionValue="name" noSelection="${['0':' Select a move event']}" value="${selectedEventId}" onchange="submitForm()" />
			&nbsp; Role: <select name="roleSelect" id="rolesSelectId"></select>
	            &nbsp; Task Size (pixels): <input type="text" id="mainHeightFieldId" value="30"/>
			&nbsp; Use Heights: <input type="checkbox" id="useHeightCheckBoxId" checked="checked"/>
			<span id="spinnerId" style="display: none"><img alt="" src="${resource(dir:'images',file:'spinner.gif')}"/></span>
			<g:render template="../assetEntity/commentCrud"/>
		</div>
	</body>
</html>