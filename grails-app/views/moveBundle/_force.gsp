<div id="scriptDivId">
<style>
/* 	these styles must be included here due to a bug in firefox
	where marker styles don't work when used in external stylesheets. */
	

line.link {
	marker-end: url(#arrowhead);
}
line.link.redundant {
	marker-end: url(#arrowheadRedundant);
}
line.link.cyclical {
	marker-end: url(#arrowheadCyclical);
}
line.link.blackBackground {
	marker-end: url(#arrowheadBlackBackground);
}
line.link.unresolved {
	marker-end: url(#arrowheadUnresolved);
}
line.link.notApplicable {
	marker-end: url(#arrowheadNA);
}
line.link.notApplicable.blackBackground {
	marker-end: url(#arrowheadNABlackBackground);
}
line.link.cut {
	marker-end: url(#arrowheadCut);
}
line.link.bundleConflict {
	marker-end: url(#arrowheadBundleConflict);
}
line.link.selected {
	marker-end: url(#arrowheadSelected) !important;
}

</style>
<script type="text/javascript">
// If there is already an instance of force running in memory, it should be stopped before creating this one
if (GraphUtil.force != null) {
	GraphUtil.force.stop()
}
GraphUtil.force = d3.layout.force();
var canvas = d3.select("div#item1")
	.append("div")
	.attr('id','svgContainerId')
	.append("svg:svg")
	.attr('class','chart');

// define the shapes used for the svg
var defs = canvas.append("defs");
defs.html(appSVGShapes.getAll());
defineShapes(d3.select("defs"));

var outsideWidth = 0;
var outsideHeight = 0;
$(window).resize( function(a, b) {
	GraphUtil.resetGraphSize();
});

var maxCutAttempts = ${defaults.maxCutAttempts};
var maxEdgeCount = $('#maxEdgeCountId').val();
if (isNaN(maxEdgeCount))
	maxEdgeCount = 4;
$('#maxEdgeCountId').unbind('change').on('change', function (e) {
	var newVal = parseInt(e.target.value);
	if (!isNaN(newVal))
		maxEdgeCount = newVal;
});

var zoomBehavior;
var vis = canvas;
var background;
var defaults = ${defaultsJson};
var defaultPrefs = ${defaultPrefs};
var selectedBundle = '${dependencyBundle}';
var assetTypes = ${assetTypesJson};
var links = ${links};
var nodes = ${nodes};
var depBundles = ${depBundleMap};
var moveBundles = ${moveBundleMap};
var moveEvents = ${moveEventMap};

var cutLinks = [];
var cutNodes = [];
var graphstyle = "z-index:-1;";
var fill = d3.scale.category10();
var fillMode = 'bundle';
var gravity = ${multiple ? 0.05 : 0};
var distanceIntervals = 500;
var graphPadding = 15;
var cutLinkSize = 500;
var floatMode = false;
var maxWeight;
var maxFamilyWeights = [];
var groupCount = 0;
var preferenceName = 'depGraph';

var nodeSelected = -1;
var defaultColor = '#0000ff';
var selectedParentColor = '#00ff00';
var selectedChildColor = '#00cc99';
var selectedLinkColor = '#00dd00';
var backgroundColor = GraphUtil.isBlackBackground() ? '#000000' : '#ffffff';
if (defaults.blackBackground);
	$('marker#arrowhead').attr('fill', '#ffffff');

var widthCurrent;
var heightCurrent;

var progressBarCancelDisplayed = false;
var cancelCut = false;


// Build the layout model
function buildMap (charge, linkSize, friction, theta, width, height) {
	
	// Use the new parameters, or the defaults if not specified
	var charge 	 =	( charge	? charge 	: defaults['force'] 	);
	var linkSize =	( linkSize	? linkSize 	: defaults['linkSize'] 	);
	var friction =	( friction	? friction 	: defaults['friction'] 	);
	var theta 	 =	( theta	? theta 	: defaults['theta'] 	);
	var width 	 = 	( width 	? width 	: defaults['width'] 	);
	var height 	 = 	( height 	? height 	: defaults['height'] 	);
	
	widthCurrent = width;
	heightCurrent = height;
	
	if (width == -1) {
		widthCurrent = getStandardWidth();
		width = widthCurrent;
	}
	if (height == -1) {
		heightCurrent = getStandardHeight();
		height = heightCurrent;
	}
	
	zoomBehavior = d3.behavior.zoom()
		.on("zoom", zooming);
	
	canvas.call(zoomBehavior);
	
	vis = canvas
		.append('svg:g')
			.on("dblclick.zoom", null)
			.style('width', 'auto')
			.style('height', 'auto')
		.append('svg:g');
	
	
	background = vis
		.append('svg:rect')
			.attr('width', width)
			.attr('height', height)
			.attr('fill', backgroundColor);
	
	
	// Sets the custom node dragging behavior
	var dragBehavior = d3.behavior.drag()
		.on("dragstart", dragstart)
		.on("drag", dragmove)
		.on("dragend", dragend);
	
	var startAlpha = 0;
	var dragging = false;
	var clicked = false;
	
	// fires on mousedown on a node
	function dragstart (d, i) {
		startAlpha = GraphUtil.force.alpha();
		dragging = true;		
		clicked = true;
		d3.event.sourceEvent.preventDefault();
		d3.event.sourceEvent.stopPropagation();
	}

	// fires when the user drags a node
	function dragmove (d, i) {
		if ((d3.event.dx != 0 || d3.event.dy != 0) || (!clicked)) {
			d.x += d3.event.dx;
			d.y += d3.event.dy;
			d.px = d.x;
			d.py = d.y;
			
			if (d.cutShadow)
				d.cutShadow.transform.baseVal.getItem(0).setTranslate(d.x, d.y);

			d.fix = true;
			d.fixed = true;
			clicked = false;
			
			startAlpha = Math.min(startAlpha+0.005, 0.1);
			if (GraphUtil.force.alpha() < startAlpha) {
				if (!GraphUtil.setAlpha(0.1)) {
					GraphUtil.updateNodePosition(d);
					updateElementPositions();
				}
			}
			
			d3.event.sourceEvent.preventDefault();
			d3.event.sourceEvent.stopPropagation();
		}
	}

	// fires on mouseup on a node. if the user never dragged moved their mouse, treat this like a click
	function dragend (d, i) {
		d.fix = false;
		d.fixed = false;
		dragging = false;
		if (clicked)
			toggleNodeSelection(d.index);
		d3.event.sourceEvent.preventDefault();
		d3.event.sourceEvent.stopPropagation();
	}
	
	// Rescales the contents of the svg. Called when the user scrolls.
	function zooming (e) {
		if (!dragging) {
			vis.attr('transform','translate(' + d3.event.translate + ')' + ' scale(' + d3.event.scale + ')');
		}
	}
	
	// Resets the scale and position of the map. Called when the user double clicks on the background
	function resetView () {
		if (d3.select(d3.event.target)[0][0].nodeName == 'use')
			return;
		centerGraph();
	}
	
	// Handle the panning when the user starts dragging the canvas
	function mousedown() {
		return;
	}
	
	// updates the current fillMode
	fillMode = GraphUtil.getFillMode();
	
	// Start each node in the center of the map
	$.each(nodes, function() {
		this.x = width/2+10*Math.random();
		this.px = width/2+10*Math.random();
		this.y = height/2+10*Math.random();
		this.py = height/2+10*Math.random();
	});
	
	// Create the SVG element
	canvas
		.attr("width", width)
		.attr("height", height)
		.attr("class", 'draggable chart')
		.attr("style", graphstyle)
		.style('background-color', backgroundColor)
		.style('cursor', 'default')
		.on("dblclick", resetView)
		.on("dblclick.zoom", null);
	
	
	// Create the force layout
	GraphUtil.force
		.nodes(nodes)
		.links(links)
		.gravity(gravity)
		.linkDistance(function (d) {
			if (d.cut)
				return 1000;
			return linkSize;
		})
		.linkStrength(function (d) {
			if (d.duplicate)
				return 0;
			return 3;
		})
		.friction(friction)
		.charge(charge)
		.size([width, height])
		.theta(theta);
	
	GraphUtil.startForce();
	
	// Add the links the the SVG
	GraphUtil.linkBindings = vis.selectAll("line.link")
		.data(links).enter()
		.append("svg:line")
			.attr("width", function(d) { return '1px' })
			.attr("class", function(d) { return 'link' })
			.attr("id", function(d) { return 'link-'+d.source.index+'-'+d.target.index })
			.attr("x1", function(d) { return d.source.x;})
			.attr("y1", function(d) { return d.source.y;})
			.attr("x2", function(d) { return d.target.x;})
			.attr("y2", function(d) { return d.target.y;});

	if ($.browser.mozilla)
		GraphUtil.linkBindings.style('opacity', 1);
	
	// Add the nodes to the SVG
	GraphUtil.nodeBindings = vis.selectAll("use")
		.data(nodes).enter()
	
	// Create the nodes
	GraphUtil.nodeBindings = GraphUtil.nodeBindings
		.append("use")
			.attr("xlink:href", function (d) {
				return '#' + appSVGShapes.shape[assetTypes[d.type].internalName].id;
			})
			.attr("class", "node")
			.call(dragBehavior)
			.on("dblclick", function(d) {
				if (nodeSelected == -1)
					toggleNodeSelection(d.index);
				return EntityCrud.showAssetDetailView(d.assetClass, d.id);
				if (d.assetClass == 'APPLICATION')
					return getEntityDetails('planningConsole', 'Application', d.id);
				else if (d.assetClass == 'STORAGE')
					return getEntityDetails('planningConsole', 'Files', d.id);
				else if (d.assetClass == 'DATABASE')
					return getEntityDetails('planningConsole', 'Database', d.id);
				return getEntityDetails('planningConsole', 'Server', d.id);
			})
			.on("mousedown", mousedown)
			.attr("id", function(d) { return 'node-'+d.index })
			.style('cursor', 'default')
			.style("fill", function(d) {
				return GraphUtil.getFillColor(d, fill, fillMode);
			})
			.attr("cx", 0)
			.attr("cy", 0)
			.attr("transform", "translate(0, 0)");
			
	GraphUtil.nodeBindings.append("title").text(function(d){ return d.title });
	
	// Create the labels
	GraphUtil.labelBindings = vis.selectAll("g.node")
		.data(nodes).enter()
		.append("svg:g")
		.attr("cx", 0)
		.attr("cy", 0)
		.attr("transform", "translate(0, 0)");
	
	GraphUtil.labelBindings.attr("class", "label");
	
	GraphUtil.labelTextBindings = GraphUtil.labelBindings.append("svg:text").attr("style", "font: 11px Tahoma, Arial, san-serif;")
		.attr("id", function (d) {
			return "label2-" + d.id;
		})
		.attr("class", "ignoresMouse labelBackground")
		.attr("dx", 16)
		.attr("dy",".35em")
		.text(function(d) {
			return d.name;
		});
	
	GraphUtil.labelTextBindings = GraphUtil.labelBindings.append("svg:text").attr("style", "font: 11px Tahoma, Arial, san-serif;")
		.attr("id", function (d) {
			return "label-" + d.id;
		})
		.attr("class", "ignoresMouse")
		.attr("dx", 16)
		.attr("dy",".35em")
		.text(function(d) {
			return d.name;
		});
	
	GraphUtil.setShowLabels(GraphUtil.force.nodes());
	
	// add pointers to the bound elements
	GraphUtil.addBindingPointers();
	
	// Update the classes for all data bound svg objects
	GraphUtil.updateAllClasses();
	
	// bind the "color by" radio buttons
	$('#colorByFormId').children().unbind('change').on('change', function (e) {
		fillMode = GraphUtil.getFillMode();
		GraphUtil.nodeBindings.style("fill", function(d) {
			return GraphUtil.getFillColor(d, fill, fillMode);
		});
		if (fillMode == 'group')
			GraphUtil.updateLegendColorKey(depBundles, fill, fillMode);
		else if (fillMode == 'bundle')
			GraphUtil.updateLegendColorKey(moveBundles, fill, fillMode);
		else
			GraphUtil.updateLegendColorKey(moveEvents, fill, fillMode);
	});
	
	// set up dimensional data for the svg bindings
	GraphUtil.setNodeDimensions();
	getLabelWidths();
	
	// bind the show budle conflicts checkbox
	$('#bundleConflictsId').unbind('change').on('change', function (e) {
		GraphUtil.updateLinkClasses();
		GraphUtil.updateNodeClasses();
	});
	
	// Load the move bundles into the legend
	$('#colorByFormId').children(':checked').trigger('change');
	
	
	// Calculate the maximum weight value, which is used during the tick function
	var maxWeight = 1
	_(nodes).forEach(function(o) {
		maxWeight = Math.max( maxWeight, (o.weight?o.weight:0) );
		o.fix = false;
	})
	
	// set up the node families
	var nodeFamilies = GraphUtil.setNodeFamilies(nodes);
	maxFamilyWeights = [];
	nodeFamilies.each(function (family, i) {
		var maxWeight = 1;
		family.each(function (node, i) {
			maxWeight = Math.max(maxWeight, node.weight);
		});
		maxFamilyWeights[i] = maxWeight;
	});
	
	GraphUtil.force.gravity(0.05);
	
	var gravityNodesFound = 0;
	if (nodeFamilies.size() < 5)
		GraphUtil.force.nodes().each(function (o, i) {
			if ( maxFamilyWeights[o.family] > 1 && o.weight < maxFamilyWeights[o.family] )
				o.noGravity = true;
			else
				++gravityNodesFound;
		});
	
	if (gravityNodesFound == 1 || nodeFamilies.size() == nodes.size())
		GraphUtil.force.gravity(1);
	
	// run some initial ticks before displaying the graph
	if (nodes.size() < 500)
		calmTick(200);
	else
		calmTick(20);
	GraphUtil.force.on("tick", tick);
	GraphUtil.force.tick();
	
	if (nodes.size() > 12)
		setIdealGraphPosition();
	
	// Move the background to the correct place in the DOM
	background.remove();
}

// Updates the dynamic attributes of the svg elements every tick of the simulation
function tick (e) {
	updateElementPositions();
}

// updates the attributes of all the svg elements
function updateElementPositions () {
	
	var d = null;
	
	// set the dynamic attributes for the nodes
	$(GraphUtil.nodeBindings[0]).each(function (i, o) {
		d = o.__data__;
		o.transform.baseVal.getItem(0).setTranslate(d.x, d.y);
		if (d.cutShadow){
			d.cutShadow.transform.baseVal.getItem(0).setTranslate(d.x, d.y);
		}
	});
	
	// set the dynamic attributes for the links
	$(GraphUtil.linkBindings[0]).each(function (i, o) {
		d = o.__data__;

		var targetEdge = GraphUtil.targetEdge(d.source, d.target);

		o.x1.baseVal.value = d.source.x;
		o.y1.baseVal.value = d.source.y;
		o.x2.baseVal.value = targetEdge.x;
		o.y2.baseVal.value = targetEdge.y;
		if (d.cut == 2) {
			d.cut = 3;
			o.classList.add('cut');
		}
	});
	
	// set the dynamic attributes for the labels
	$(GraphUtil.labelBindings[0]).each(function (i, o) {
		d = o.__data__;
		o.transform.baseVal.getItem(0).setTranslate(d.x, d.y)
	});
}

// Toggles selection of a node
function toggleNodeSelection (id) {
	if (nodeSelected == -1 && id == -1)
		return; // No node is selected, so there is nothing to deselect
	
	var node = GraphUtil.force.nodes()[id];
	
	console.log(node);
	
	// check if we are selecting or deselecting
	if (nodeSelected == id) {
		// deselecting
		nodeSelected = -1;
		
		// The funtion is deselecting a node, so remove the selected class from all elements
		GraphUtil.force.links().each(function (link, i) {
			link.selected = 0;
		});
		GraphUtil.force.nodes().each(function (node, i) {
			node.selected = 0;
		});
		
	} else {
		// selecting
		if (nodeSelected != -1)
			toggleNodeSelection(nodeSelected); // Another node is selected, so deselect that one first
		nodeSelected = id;
	
		// Style the selected node
		node.selected = 2;
		
		// Style the dependencies of the selected node
		function styleDependencies (index, linkIndex, useTarget) {
			var link = GraphUtil.force.links()[linkIndex];
			var childNode = (useTarget)?(link.target):(link.source);
			link.selected = 1;
			childNode.selected = 1;
		}
		$.each(node.dependsOn, function (i, o) {styleDependencies(i, o, true)});
		$.each(node.supports, function (i, o) {styleDependencies(i, o, false)});
	}
	
	GraphUtil.updateAllClasses();
	
	// Sort all the svg elements to reorder them in the DOM (SVG has no z-index property)
	GraphUtil.reorderDOM();
}

// Used to rebuild the layout using the new parameters
function rebuildMap (layoutChanged, charge, linkSize, friction, theta, width, height) {
	
	// handle resizing when not in fullscreen mode
	if (!GraphUtil.isFullscreen() && (width && height)) {
		resizeGraph(width, height);
	}
	
	// handle the background color
	var blackBackground = GraphUtil.isBlackBackground();
	backgroundColor = blackBackground ? '#000000' : '#ffffff'
	canvas.style('background-color', backgroundColor)
	background.attr('fill', backgroundColor)
	
	// Create the force layout
	if (linkSize)
		GraphUtil.force.linkDistance(function (d) {
			if (d.cut)
				return cutLinkSize;
			return linkSize;
		});
	if (friction)
		GraphUtil.force.friction(friction);
	if (charge)
		GraphUtil.force.charge(charge);
	if (theta)
		GraphUtil.force.theta(theta);
	
	// Reset the list of types to show names for
	var labelsChanged = GraphUtil.setShowLabels(GraphUtil.force.nodes())
	
	// Update the classes for all data bound svg objects
	GraphUtil.updateAllClasses();
	
	// Set the new label widths
	if (labelsChanged)
		getLabelWidths();
	
	// updates the current fillMode
	fillMode = GraphUtil.getFillMode();
	
	// if we only changed the labels or background color, only one tick is needed to reflect this change
	if (layoutChanged)
		GraphUtil.startForce();
	else
		GraphUtil.force.tick();
}

function resizeGraph (width, height) {
	widthCurrent = width;
	heightCurrent = height;
	
	canvas
		.attr("width", width)
		.attr("height", height);
	
	var oldSize = GraphUtil.force.size();
	var dx = width - oldSize[0];
	var dy = height - oldSize[1];
	GraphUtil.force.nodes().each(function (o, i) {
		o.x += dx / 2;
		o.y += dy / 2;
	});
	
	GraphUtil.force
		.size([width, height]);
	
	GraphUtil.startForce();
	
	reoptimizeGraph();
}

// centers the view onto the graph
function centerGraph () {
	var ranges = getGraphRanges();
	var visWidth = ranges.maxX - ranges.minX;
	var visHeight = ranges.maxY - ranges.minY;
	
	var dimensions = getDimensionsForOptimizing();
	var graphWidth = dimensions.graphWidth;
	var graphHeight = dimensions.graphHeight;
	
	var ratioX = graphWidth / visWidth;
	var ratioY = graphHeight / visHeight;
	var scaleAfter = Math.min(ratioX, ratioY);
	
	var translateXAfter = (graphWidth / 2) - ((visWidth / 2) + ranges.minX) * scaleAfter + graphPadding;
	var translateYAfter = (graphHeight / 2) - ((visHeight / 2) + ranges.minY) * scaleAfter + graphPadding;
	if (GraphUtil.isFullscreen())
		translateYAfter += $('#dependencyDivId').height();
	var translateAfter = [translateXAfter, translateYAfter];
	
	scaleAfter = Math.min(scaleAfter, 2.0);
	
	if (scaleAfter < 2) {
		zoomBehavior.scale(scaleAfter);
		zoomBehavior.translate(translateAfter);
	} else {
		zoomBehavior.scale(1);
		zoomBehavior.translate([0, 0]);
	}
		zoomBehavior.event(canvas);
}

// finds the rotation of the graph that requires the least zooming
function findBestRotation () {
	var dimensions = getDimensionsForOptimizing();
	var graphWidth = dimensions.graphWidth;
	var graphHeight = dimensions.graphHeight;
	var bestAngle = 0;
	var bestScale = 0;
	var iterations = 180;
	var angleChange = 360 / iterations;
	for (var i = 1; i <= iterations; i++) {
		if (i == (iterations / 4) + 1) {
			GraphUtil.rotateGraph(180 - angleChange);
			i = 3 * (iterations / 4) - 1;
		} else {
			GraphUtil.rotateGraph(angleChange);
			
			var ranges = getGraphRanges();
			
			var visWidth = ranges.maxX - ranges.minX;
			var visHeight = ranges.maxY - ranges.minY;
			var ratioX = graphWidth / visWidth;
			var ratioY = graphHeight / visHeight;
			var scaleAfter = Math.min(ratioX, ratioY);
			if (scaleAfter >= bestScale) {
				bestScale = scaleAfter;
				bestAngle = i * angleChange;
			}
		}
	}
	return bestAngle;
}

function getGraphRanges () {
	var returnVal = {};
	returnVal.minX = 9999999999;
	returnVal.maxX = -9999999999;
	returnVal.minY = 9999999999;
	returnVal.maxY = -9999999999;
	
	GraphUtil.force.nodes().each(function(o, i) {
		var offsetX = o.dimensions.width / 2;
		var offsetY = o.dimensions.height / 2;
		returnVal.minX = Math.min(returnVal.minX, o.x - offsetX);
		returnVal.maxX = Math.max(returnVal.maxX, o.x + Math.max(o.labelWidth, offsetX));
		returnVal.minY = Math.min(returnVal.minY, o.y - offsetY);
		returnVal.maxY = Math.max(returnVal.maxY, o.y + offsetY);
	});
	
	return returnVal;
}

function setIdealGraphPosition () {
	var angle = findBestRotation();
	GraphUtil.rotateGraph(angle);
	updateElementPositions();
	centerGraph();
	updateElementPositions();
}

function reoptimizeGraph () {
	zoomBehavior
		.translate([0, 0])
		.scale(1)
		.event(canvas);
	setIdealGraphPosition();
}

function getDimensionsForOptimizing () {
	var dimensions = GraphUtil.getProperGraphDimensions();
	var returnVal = {};
	returnVal.graphWidth = dimensions.width - graphPadding * 2;
	returnVal.graphHeight = dimensions.height - graphPadding * 2;
	if (GraphUtil.isFullscreen())
		returnVal.graphHeight -= $('#dependencyDivId').height();
	return returnVal;
}

function getLabelWidths () {
	GraphUtil.force.nodes().each(function (o, i) {
		if (o.showLabel) {
			var labelElement = o.labelElement.select('text:not(.labelBackground)')[0][0];
			var extent = labelElement.getExtentOfChar(labelElement.getNumberOfChars() - 1);
			var offset = labelElement.dx.baseVal.getItem(0).value;
			var labelWidth = extent.x + extent.width + offset;
			o.labelWidth = labelWidth;
		} else {
			o.labelWidth = 0;
		}
	});
}

function resetTranslate () {
	zoomBehavior.translate([0, 0]);
	zoomBehavior.event(canvas);
}

// calls the tick function n times without letting the browser paint in between
function calmTick (n) {
	for (var i = 0; i < n; ++i) {
		GraphUtil.force.tick();
	}
}

// gets the min cut of the graph defined by the nodes and links
function getMinCut (nodeList, linkList) {

	// get a list of edges
	var edges = [];
	$(linkList).each(function (i, o) {
		var edge = {parent:o.source, child:o.target, id:o.id};
		edges.push(edge);
	});
	
	var vertices = [];
	$(nodeList).each(function (i, o) {
		o.hasApp = false;
		if (o.type == 'Application')
			o.hasApp = true;
		o.captured = [];
		vertices.push(o);
	});
	
	// use the contraction algorithm to reduce the edges to point to 2 nodes
	var n = 0;
	while (vertices.size() > 2) {
		var randomIndex = Math.floor(Math.random() * edges.size());
		var edge = edges[randomIndex];
		
		// don't contract the last 2 applications
		var apps = 0;
		for (var j = 0; j < vertices.size(); ++j) {
			if (vertices[j].hasApp)
				++apps;
		}
		
		if (apps != 2 || !(edge.child.hasApp && edge.parent.hasApp)) {
			if (edge.parent.id != edge.child.id) {
				contractEdge(edges, edge, vertices);
				if (edge.parent.hasApp)
					vertices[vertices.indexOf(edge.parent)].hasApp = true;
			}
		}
			
		// prevent infinite loops
		++n;
		if (n > 100000) {
			console.log('MAX CALL COUNT REACHED');
			return [];
		}
	}
	
	// combines the nodes in an edge into one
	function contractEdge (edges, edge, vertices) {
		var removedVertex = edge.child;
		var newVertex = edge.parent;
		if (removedVertex.hasApp)
			newVertex.hasApp = true;
		for (var i = 0; i < edges.size(); ++i) {
			if (edges[i].child == removedVertex) {
				edges[i].child = newVertex;
			} 
			if (edges[i].parent == removedVertex) {
				edges[i].parent = newVertex;
			}
			
			// remove self edges
			if (edges[i].child == edges[i].parent) {
				edges.splice(i, 1);
				--i;
			}
		}
		$(removedVertex.captured).each(function (i, o) {
			newVertex.captured.push(o);
		});
		newVertex.captured.push(removedVertex);
		vertices.splice(vertices.indexOf(removedVertex), 1);
	}
	
	return edges;
}

// finds a min cut and removes it from the graph
function cutAndRemove () {
	
	var nodeList = [];
	var linkList = [];
	var appCount = 0;
	var callCount = 0;
	$('#minCutButtonId').attr('disabled', 'disable');
	stopMap();
	
	// find a neighborhood of nodes to use with a breadth first search
	while (appCount < 2 && callCount < 1000) {
		nodeList = [];
		linkList = [];
		appCount = 0;
		var randomIndex = Math.floor(Math.random() * nodes.size());
		var startNode = nodes[randomIndex];
		var queue = [startNode];
		
		while (queue.size() > 0) {
			var node = queue.pop();
			if (nodeList.indexOf(node) == -1) {
				nodeList.push(node);
				if (node.type == 'Application')
					++appCount;
				for (var i = 0; i < node.dependsOn.size(); ++i) {
					if ((linkList.indexOf(links[node.dependsOn[i]]) == -1) && (! links[node.dependsOn[i]].cut)) {
						linkList.push(links[node.dependsOn[i]]);
						queue.unshift(links[node.dependsOn[i]].target);
					}
				}
				for (var i = 0; i < node.supports.size(); ++i) {
					if ((linkList.indexOf(links[node.supports[i]]) == -1) && (! links[node.supports[i]].cut)) {
						linkList.push(links[node.supports[i]]);
						queue.unshift(links[node.supports[i]].source);
					}
				}
			}
		}
		++callCount;
	}
	
	// if it looped this many times, no suitable neighborhood could be found
	if (callCount >= 1000) {
		$('#minCutButtonId').removeAttr('disabled');
		alert('Unable to determine any applicable splits');
		return;
	}
	
	
	var progressBar = displayProgressBar();
	var edges = [];
	var bestDifference = 9999;
	var intervals = Math.ceil(maxCutAttempts / 100);
	findBestCut(0);
	
	// called iteratively using setTimeout to prevent locking up the thread with long executions
	function findBestCut (i) {
		
		// check if the job should be canceled
		if (cancelCut) {
			cancelCut = false;
			removeProgressBar();
			$('#minCutButtonId').removeAttr('disabled');
			return;
		}
		
		// update the progress bar
		if (i % intervals == 0) {
			setProgress(Math.round(100 * i / maxCutAttempts));
		}
		
		// find the best cut
		if (i < maxCutAttempts) {
			var newEdges = getMinCut(nodeList, linkList);
			if (newEdges.size() > 0) {
				var parentSize = newEdges[0].parent.captured.size();
				var childSize = newEdges[0].child.captured.size();
				var difference = Math.abs(childSize - parentSize);
				
				if (difference < bestDifference && newEdges.size() <= maxEdgeCount) {
					edges = newEdges;
					bestDifference = difference;
					newEdges[0].parent.capturedFinal = newEdges[0].parent.captured;
					newEdges[0].child.capturedFinal = newEdges[0].child.captured;
				}
			}
			setTimeout(function () {findBestCut(i+1)}, 0);
		} else {
			executeCut();
		}
	}
	
	// executed by the last iteration of findBestCut
	function executeCut () {
		// if we found a good set of edges, execute the cut
		if (edges.size() > 0) {
			
			// another node group will be added, so this counter must be incremented
			++groupCount;
			
			// reset any nodes that were cut previously
			$(nodes).each(function (i, n) {
				n.cut = 0;
			});
			
			// find which nodes were cut and put them in a new group
			$(edges[0].parent.capturedFinal).each(function (i, c) {
				if (edges[0].child.capturedFinal.indexOf(c) == -1) {
					c.cut = 2;
					c.cutGroup = groupCount;
				}
			});
			edges[0].parent.cut = 2;
			edges[0].parent.cutGroup = groupCount;
			
			// add shadows for the cut node
			GraphUtil.createCutShadows(fill);
			
			// set which links were cut
			$(edges).each(function (i, e) {
				links[e.id].cut = true;
			});
			
			// update the graph for the modified link list
			GraphUtil.updateLinkClasses();
			GraphUtil.disableFreeze();
			rebuildMap(true, false, parseInt($('#linkSizeId').val()), false, false);
		
		// no cuts could be found for the constraints, so update the user
		} else {
			alert('No dependency splits could be found');
		}
		
		$('#minCutButtonId').removeAttr('disabled');
		removeProgressBar();
	}
}

// the logic required to undo the cuts was too complicated, so just reload the graph
function undoCuts () {
	resetMap();
}


// Stops the map by setting the alpha value to 0
function stopMap () {
	GraphUtil.force.stop()
}

// gets the normal width of this graph
function getStandardWidth () {
	var graphOffset = $('#svgContainerId').offset().left;
	var pageWidth = $(window).width();
	if (GraphUtil.isFullscreen())
		return pageWidth;
	return pageWidth - graphOffset * 2;
}

// gets the normal height of this graph
function getStandardHeight () {
	var bottomMargin = $('#svgContainerId').offset().left;
	var graphOffset = $('#svgContainerId').offset().top;
	var pageHeight = $(window).height();
	if (GraphUtil.isFullscreen())
		return pageHeight;
	return pageHeight - graphOffset - bottomMargin;
}

function getInitialDimensions () {
	var fullWidth = $('.main_bottom').width() - ($('#svgContainerId').offset().left * 2);
	var width = Math.max(fullWidth, $('#width').val());
	var height = $('#height').val();
	return {width:width, height:height};
}

// reloads the graph as if the user clicked on the map tab again
function resetMap () {
	getList('graph', selectedBundle);
}

// gets the list of children for the specified node
function getChildren (node) {
	var nodes = [];
	if (node) 
		for (var i = 0; i < node.dependsOn.length; ++i)
			nodes.push(links[node.dependsOn[i]].target);
	return nodes;
}

// gets the list of parents for the specified node
function getParents (node) {
	var nodes = [];
	if (node) 
		for (var i = 0; i < node.supports.length; ++i)
			nodes.push(links[node.supports[i]].source);
	return nodes;
}

function getLinkDistance (link) {
	var width = Math.abs(link.target.x - link.source.x);
	var height = Math.abs(link.target.y - link.source.y);
	return Math.sqrt(width*width + height*height);
}

// initializes the progress bar for min cuts
function displayProgressBar () {
	var progressBar = tds.ui.progressBar(-1, 999999, function() {}, function() {}, "<h1>Calculating Group Split Suggestion</h1>");
	progressBarCancelDisplayed = false;
	
	return progressBar;
}

// sets the value of the progress bar to n %
function setProgress (n) {
	
	/* the cancel button sometimes takes a few iterations before being accessable in the DOM,
	so check it each time until it can be made visible */
	if (!progressBarCancelDisplayed) {
		var cancel = $('#progressCancel');
		if (cancel.size() > 0) {
			progressBarCancelDisplayed = true;
			cancel.css('display', '');
			cancel.on('click', function () {
				cancelCut = true;
			});
			var status = $('#progressStatus');
			status.html('');
		}
	}
	
	// update properties of the progress bar to reflect the new values
	$('#globalProgressBar').css('display', 'block');
	var bar = $('#innerGlobalProgressBar');
	bar.css('transition-duration', '0.00s');
	bar.show();
	bar.attr('aria-valuenow', n);
	bar.css('width', n + '%');
	bar.html(n + '%');
}

// removes the progress bar from the DOM
function removeProgressBar () {
	$('#globalProgressBar').remove();
}
</script>
</div>