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
		<title>Task Graph</title>
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
		.miniItem {
			stroke-width: 4;
			stroke: green;
			fill: #88FF88;
		}
		
		
		
		polygon.mainItem {
			stroke-width: 2;
			fill: #FFFF00;
			stroke: #FFFF00;
			fill-opacity: 1;
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
			stroke: darkgrey;
			stroke-width: 1.5;
			marker-end: url(#arrowhead);
			stroke-opacity: 0.8;
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

		.dependency.selected {
			stroke: red;
			stroke-width: 2;
			marker-end: url(#arrowheadSelected);
			stroke-opacity: 1;
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
			cursor: default !important;
			pointer-events: none !important;
		}
		.itemLabel.selected {
			fill: #AA33AA;
			font-weight: bold !important;	
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
				d3.select('div.body')
					.append('div')
					.attr('class','chart')
					.html('not enough task data to create a graph for this event');
				return;
			}
			var data = $.parseJSON(response.responseText);
			
			// populate the roles select
			$("#rolesSelectId").children().remove();
			$("#rolesSelectId").append('<option value="ALL">Show all</option>');
			$.each(data.roles, function (index, role) {
				$("#rolesSelectId").append('<option value="' + role + '">' + role + '</option>');
			});
			$("#rolesSelectId").val('ALL');
			$('#rolesSelectId').on('change', display);
			
			// graph defaults
			var miniRectHeight = 5;
			var mainRectHeight = 30;
			var initialExtent = 1000000;
			var anchorOffset = 10;
			var margin = {top: 20, right: 0, bottom: 15, left: 0};
			var items = data.items;
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
				.domain([items[0].start, items[items.length-1].end])
				.range([0, $('div.body').innerWidth() - 20 - $('div.body').offset().left]);
			var domainOffset = (x.domain()[1] - x.domain()[0]) * 0.1;
			x.domain([new Date(x.domain()[0].getTime() - domainOffset),  new Date(x.domain()[1].getTime() + domainOffset)]);
			
			var maxStack = calculateStack(items);
			var miniHeight = ((maxStack+1)*miniRectHeight);
			var mainHeight = ((maxStack+1)*mainRectHeight*1.5);

			var width = x.range()[1];
			var height = mainHeight + miniHeight + margin.top + margin.bottom;
			var x1 = d3.time.scale().range([0, width]);

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
				.call(d3.behavior.zoom().on("zoom", zoom))
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
					display();
				}
			}
			
			function drawEnd () {
				if (drawing) {
					var x1 = Math.min(tempBrushXInitial, d3.mouse(chart.node())[0] - margin.left);
					var x2 = Math.abs(tempBrushXInitial - (d3.mouse(chart.node())[0] - margin.left));
					brush.extent([x.invert(x1), x.invert(x1+x2)]);
					
					tempBrush.remove();
					tempBrush = null;
					tempBrushXInitial = 0;
					drawing = false;
					display();
				}
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
			
			// handle panning and time selection behavior on the main graph
			function dragStart () {
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
				if (selectingMain) {
					mainSelection
						.attr( 'x', Math.min(mainSelectionXInitial, d3.mouse(chart.node())[0] - margin.left) )
						.attr( 'width', Math.abs(mainSelectionXInitial - (d3.mouse(chart.node())[0] - margin.left)) );
					display();
				} else {
					var minExtent = brush.extent()[0];
					var maxExtent = brush.extent()[1];
					var timeShift = (maxExtent - minExtent) * (d3.event.dx / width);
					brush.extent([new Date(minExtent-timeShift), new Date(maxExtent-timeShift)]);
					display();
				}
			}
			
			// handle panning and time selection behavior on the main graph
			function dragEnd () {
				if (selectingMain) {
					selectingMain = false;
					var a = Math.min(mainSelectionXInitial, d3.mouse(chart.node())[0] - margin.left);
					var b = Math.abs(mainSelectionXInitial - (d3.mouse(chart.node())[0] - margin.left));
					brush.extent([x1.invert(a), x1.invert(a+b)]);
					
					mainSelection.remove();
					mainSelection = null;
					mainSelectionXInitial = 0;
					display();
				}
			}

			// construct the area behind the objects
			var background = main.append('rect')
				.attr('width', width)
				.attr('height', mainHeight)
				.attr('class', 'background')
				
			// define the arrowhead marker used for marking dependencies
			chart.select("defs")
				.append("marker")
				.attr("id", "arrowhead")
				.attr("viewBox", "0 -5 10 10")
				.attr("refX", 10)
				.attr("refY", 0)
				.attr("markerWidth", 6)
				.attr("markerHeight", 6)
				.attr("orient", "auto")
				.append("path")
				.attr("d", "M0,-5L10,0L0,5");
			chart.select("defs")
				.append("marker")
				.attr("id", "arrowheadSelected")
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
				.attr('clip-path', 'url(#clip)');
				
			mini.append('line')
				.attr('x1', x(now()) + 0.5)
				.attr('y1', 0)
				.attr('x2', x(now()) + 0.5)
				.attr('y2', miniHeight)
				.attr('class', 'todayLine');

			// construct the container for the task polygons
			var itemPolys = main.append('g')
				.attr('clip-path', 'url(#clip)');
				
			// construct the container for the task labels
			var itemLabels = main.append('g')
				.attr('clip-path', 'url(#clip)');
				
			// construct the container for the dependency lines
			var itemArrows = main.append('g')
				.attr('clip-path', 'url(#clip)');

			// construct the container for the path in the mini graph
			mini.append('g').selectAll('miniItems')
				.data(getPaths(items))
				.enter().append('path')
				.attr('class', function(d) { return 'miniItem past'; })
				.attr('d', function(d) { return d.path; });

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
				.on("brush", display)
				.extent([new Date(data.startDate), new Date(new Date(data.startDate).getTime() + 30 * 60000)])

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

			display();

			// updates the svg
			function display () {
				
				var polys, labels, lines;
				var minExtent = brush.extent()[0];
				var maxExtent = brush.extent()[1];
				var visItems = items.filter(function (d) { return d.start < maxExtent && d.end > minExtent});
				var visDeps = dependencies;

				mini.select('.brush').call(brush.extent([minExtent, maxExtent]));		

				x1.domain([minExtent, maxExtent]);
				
				// update the axis
				main.select('.main.axis.minute').call(x1MinuteAxis);
				main.select('.main.axis.hour').call(x1HourAxis)

				// update any existing item polys
				polys = itemPolys.selectAll('polygon')
					.data(visItems, function (d) { return d.id; })
					.attr('points', function(d) { return getPoints(d); })
					.attr('class', function(d) { return getClasses(d); });
/*					.attr('x', function(d) { return x1(d.start); })
					.attr('y', function(d) { return d.stack * mainRectHeight + 0.4 * mainRectHeight + 0.5; })
					.attr('width', function(d) { return x1(d.end) - x1(d.start); })*/
				
				// add any task polys in the new domain extents
				polys.enter()
					.append('polygon')
					.attr('points', function(d) { return getPoints(d); })
					.attr('class', function(d) { return getClasses(d); })
					.attr('id', function(d) { return 'task-' + d.id; })
					.on("click", function(d) {
						toggleTaskSelection(d);
						display();
					})
					.on("dblclick", function(d) {
						showAssetComment(d.id, 'show');
					})
					.append('title')
					.html(function(d) { return d.name + ' - ' + d.assignedTo + ' - ' + d.status; });
					
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
					.attr('y1', function(d) {
						return (d.predecessor.stack + 0.9) * mainRectHeight;
					})
					.attr('y2', function(d) {
						return d.successor.stack * mainRectHeight + 0.4 * mainRectHeight + 0.5 + 0.5 * mainRectHeight;
					})
					.attr('class', function(d) { return 'dependency ' + getClasses(d); });
				
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
					.attr('y1', function(d) {
						return (d.predecessor.stack + 0.9) * mainRectHeight;
					})
					.attr('y2', function(d) {
						return d.successor.stack * mainRectHeight + 0.4 * mainRectHeight + 0.5 + 0.5 * mainRectHeight;
					})
					.attr('id', function(d) { return 'dep-' + d.predecessor.id + '-' + d.successor.id; })
					.attr('class', function(d) { return 'dependency ' + getClasses(d); });
				
				
				// move any selected lines to the top of the DOM
				lines.sort(function (a, b) { return a.selected - b.selected; });
				lines.exit().remove();
				
				// update the item labels
				labels = itemLabels.selectAll('text')
					.data(visItems, function (d) { return d.id; })
					.attr('x', function(d) {
						var anchor = new Date(d.start.getTime() + (d.end.getTime()-d.start.getTime())/2);
						if (anchor < minExtent) {
							$(this).css('text-anchor', 'start')
							return x1(minExtent) + 2
						} if (anchor > maxExtent) {
							$(this).css('text-anchor', 'end')
							return x1(maxExtent) + 2
						}
						$(this).css('text-anchor', 'middle')
						return x1(anchor) + 2;
					})
					.attr('y', function(d) { return d.stack * mainRectHeight + 0.8 * mainRectHeight + 0.5; })
					.attr('class', function(d) { return 'itemLabel unselectable ' + getClasses(d);} );
				
				// add any labels in the new domain extents
				labels.enter().append('text')
					.text(function (d) { return d.name; })
					.attr('class', function(d) { return 'itemLabel unselectable ' + getClasses(d);} )
					.attr('x', function(d) {
						var anchor = new Date(d.start.getTime() + (d.end.getTime()-d.start.getTime())/2);
						if (anchor < minExtent) {
							$(this).css('text-anchor', 'start')
							return x1(minExtent) + 2
						} if (anchor > maxExtent) {
							$(this).css('text-anchor', 'end')
							return x1(maxExtent) + 2
						}
						$(this).css('text-anchor', 'middle')
						return x1(anchor) + 2;
					})
					.attr('y', function(d) { return d.stack * mainRectHeight + 0.8 * mainRectHeight + 0.5; })
					.append('title')
					.html(function(d) { return d.name; });
					
				// move any selected labels to the top of the DOM
				labels.sort(function (a, b) { return a.selected - b.selected; });
				labels.exit().remove();
			}
			
			// gets the css classes that apply to task @param d
			function getClasses (d) {
				var classString = 'mainItem '
					+ (d.selected ? 'selected ' : '')
					+ (d.milestone ? 'milestone ' : '')
					+ (d.criticalPath ? 'critical ' : '')
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
				var x = x1(d.start);
				var y = d.stack * mainRectHeight + 0.4 * mainRectHeight + 0.5;
				var w = x1(d.end) - x1(d.start);
				var h = 0.8 * mainRectHeight;
				var x2 = x + w - 10;
				var y2 = y + (h/2);
				return x + ',' + y + ' '
					 + x2 + ',' + y + ' '
					 + (x+w) + ',' + y2 + ' '
					 + x2 + ',' + (y+h) + ' '
					 + x + ',' + (y+h) + ' ';
				
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
				
				var selecting = true;
				if (taskSelected == null && taskObject == null)
					return // No node is selected, so there is nothing to deselect
				
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
							$.each(task.predecessors, function(i, d) {
								d.selected = selecting;
								styleDependencies(d.predecessor ,'left');
							});
						if (direction == 'right' || direction == 'both')
							$.each(task.successors, function(i, d) {
								d.selected = selecting;
								styleDependencies(d.successor ,'right');
							});
					}
				}
				styleDependencies(taskObject, 'both');
				
				if ( ! selecting )
					taskSelected = null;
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
				display();
			}

			// generates a single path for each item class in the mini display
			// ugly - but draws mini 2x faster than append lines or line generator
			// is there a better way to do a bunch of lines as a single path with d3?
			function getPaths(items) {
				var paths = {}, d, offset = 0.5 * miniRectHeight + 0.5, result = [];
				for (var i = 0; i < items.length; i++) {
					d = items[i];
					if (!paths[d.class]) paths[d.class] = '';	
					paths[d.class] += ['M',x(d.start),((d.stack*miniRectHeight) + offset),'H',x(d.end)].join(' ');
				}

				for (var className in paths) {
					result.push({class: className, path: paths[className]});
				}

				return result;
			}

			// recalculate stacking values for the items
			function calculateStack (items) {
				var stack = [];
				var minIndex = 0;
				var maxIndex = 0;
				for (var i = 0; i < items.length; ++i) {
					var d = items[i];
					
					// the ideal location for any given task is the average of its predecessors
					var ideal = minIndex + Math.round((maxIndex-minIndex) / 2);
					if (maxIndex-minIndex > 0)
						ideal = getIdealStackLocation(d);
					// first check if the ideal location is availible
					if (!stack[ideal] || x(d.start) >= stack[ideal]) {
						stack[ideal] = x(d.end);
						d.stack = ideal;
					// if not, move outwards from that location, until an empty location or a location worth switching for
					} else {
						var high = ideal;
						var low = ideal;
						var inserted = false;
						while (!inserted) {
							if (high > maxIndex) {
								stack[high] = x(d.end);
								d.stack = high;
								++maxIndex;
								inserted = true;
							} else if (low < minIndex) {
								stack[low] = x(d.end);
								d.stack = low;
								--minIndex;
								inserted = true;
							} else if (stack[high] <= x(d.start)) {
								stack[high] = x(d.end);
								d.stack = high;
								inserted = true;
							} else if (stack[low] <= x(d.start)) {
								stack[low] = x(d.end);
								d.stack = low;
								inserted = true;
							}
							++high;
							--low;
						}
					}
				}
				for (var i = 0; i < items.length; ++i)
					items[i].stack -= minIndex;
				return maxIndex - minIndex;
			}
			
			// gets the best stack location for task @param d. (average location of its predecessors)
			function getIdealStackLocation (d) {
				var sum = 0;
				for (var i = 0; i < d.predecessors.length; ++i) {
					if (!isNaN(parseInt(d.predecessors[i].predecessor.stack)))
						sum += d.predecessors[i].predecessor.stack;
				}
				if (sum == 0) 
					return 0;
				return Math.round(sum / d.predecessors.length);
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
				display();
			}
			
			/*	Reconstructs the data in @param items for d3 by:
				 - identifying tasks where start = end as milestones
				 - converting the start and completion times for tasks from date Strings to javascript Dates
				 - populates the dependencies list from data in the items list 
				 - corrects any impossible start times for tasks */
			function sanitizeData (items, dependencies) {
				
				// convert all data to its proper format
				var startTime = new Date(data.startDate);
				for (var i = 0; i < items.length; ++i) {
					items[i].milestone = (items[i].startInitial == items[i].endInitial);
					items[i].startInitial = new Date(startTime.getTime() + (items[i].startInitial)*60000);
					items[i].endInitial = new Date(startTime.getTime() + (items[i].endInitial + items[i].milestone)*60000);
					items[i].start = null;
					items[i].end = null;
					items[i].successors = [];
					items[i].predecessors = [];
					items[i].selected = false;
				}
				
				// generate dependencies in a separate loop to ensure no dependencies pointing to removed tasks are created 
				for (var i = 0; i < items.length; ++i)
					if (items[i].predecessorIds)
						for (var j = 0; j < items[i].predecessorIds.length; ++j) {
							var predecessorIndex = binarySearch(items, items[i].predecessorIds[j], 0, items.length-1)
							var depObject = { "predecessor":items[predecessorIndex], "successor":items[i], "modifier":"hidden", "selected":false };
							if (predecessorIndex != -1) {
								items[predecessorIndex].successors.push(depObject);
								items[i].predecessors.push(depObject);
								dependencies.push(depObject);
							}
						}
				
				// correct any errors in the start times for tasks
				for (var i = 0; i < items.length; ++i)
					calculateTimes(items[i]);
			}
			
			// ensures times are correct
			function calculateTimes (task) {
				if ( ! task.start ) {
					if (task.predecessors.length == 0) {
						task.start = task.startInitial;
					} else {
						var latest = 0;
						for (var i = 0; i < task.predecessors.length; ++i) {
							var tmp = calculateTimes(task.predecessors[i].predecessor);
							if (tmp > latest)
								latest = tmp;
						}
						task.start = latest;
					}
				}
				if ( ! task.end )
					task.end = new Date(task.start.getTime() + (task.endInitial.getTime()-task.startInitial.getTime()))
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
				
			jQuery.ajax({
				dataType: 'json',
				url: 'taskGraph',
				data: params,
				type:'GET',
				complete: buildGraph
			});
		}
		</script>
	</head>
	<body>
		<div class="body">
			<h1>Task Graph</h1>
			<g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
			</g:if>
			Event: <g:select from="${moveEvents}" name="moveEventId" id="moveEventId" optionKey="id" optionValue="name" noSelection="${['0':' Select a move event']}" value="${selectedEventId}" onchange="submitForm()" />
			&nbsp; Role: <select name="roleSelect" id="rolesSelectId"></select>
			<g:render template="../assetEntity/commentCrud"/>
		</div>
	</body>
</html>