<%@page defaultCodec="none" %>
<%@page import="net.transitionmanager.security.Permission"%>
<div id="scriptDivId">
<style type="text/css">
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

// store all the JSON server parameters into an object
var serverParams = {
	'maxCutAttempts': ${defaults.maxCutAttempts},
	'defaults': ${defaultsJson},
	'depGroup': '${depGroup}',
	'defaultPrefs': ${defaultPrefs},
	'selectedBundle': '${dependencyBundle}',
	'assetTypes': ${assetTypesJson},
	'links': ${links},
	'nodes': ${nodes},
	'colorByGroups': ${colorByGroups},
	'colorByGroupLabels': ${colorByGroupLabelsJson},
	'gravity': ${multiple ? 0.05 : 0}
}

// If there is already an instance of force running in memory, it should be stopped before creating this one
if (GraphUtil.force != null)
	GraphUtil.force.stop()

GraphUtil.force = d3.layout.force()

var svgContainer = d3.select("div#item1")
	.append("div")
	.attr('id','svgContainerId')
	.style('background-color','#ffffff')
var transformContainer = svgContainer
	.attr('class','containsSpinner')
	.append("div")
	.attr('id','svgTranslatorId')
var canvas = transformContainer
	.append("svg:svg")
	.attr('class','chart')

// define the shapes used for the svg
var defs = canvas.append("defs")
defs.html(appSVGShapes.getAll())
defineShapes(d3.select("defs"))

var outsideWidth = 0
var outsideHeight = 0
$(window).resize( function(a, b) {
	GraphUtil.resetGraphSize()
})

// initialize edge cutting variables and listeners
var maxCutAttempts = serverParams.maxCutAttempts
var maxEdgeCountInput = $('#maxEdgeCountId')
var maxEdgeCount = maxEdgeCountInput.val()
if (isNaN(maxEdgeCount))
	maxEdgeCount = 4
maxEdgeCountInput.unbind('change').on('change', function (e) {
	var newVal = parseInt(e.target.value)
	if (!isNaN(newVal))
		maxEdgeCount = newVal
})

var zoomBehavior
var dragBehavior
var selectionBehavior
var vis = canvas
var background
var defaults = serverParams.defaults
var depGroup = serverParams.depGroup // specify the Group
var defaultPrefs = serverParams.defaultPrefs
var selectedBundle = serverParams.selectedBundle
var assetTypes = serverParams.assetTypes
var links = serverParams.links
// populate the nodes
var nodeData = serverParams.nodes
var nodes = []
for (let nodeParams of nodeData)
	nodes.push(new Node(nodeParams))
var colorByGroups = serverParams.colorByGroups
var colorByGroupLabels = serverParams.colorByGroupLabels

var cutLinks = []
var cutNodes = []
var graphstyle = 'z-index:90;'
var fill = d3.scale.category10()
var fillMode = 'bundle'
var gravity = serverParams.gravity
var distanceIntervals = 500
var graphPadding = 15
var canvasSize = 0
var cutLinkSize = 500
var graphSizeLimit = 40000
var tooBigZoom = {'scale': 0.37892914162759944, 'translate':[527.9102296165405, 185.33142583118914]}
var initialTickConfig = {
	'large': {'min': 500, 'ticks': 200, 'theta': 8},
	'small': {'max': 500, 'ticks': 500, 'theta': 1}
}
var floatMode = false
var maxWeight
var maxFamilyWeights = []
var groupCount = 0
var preferenceName = 'depGraph'
var isIE = GraphUtil.isIE()

var selectedNodes = []
var defaultColor = '#0000ff'
var selectedParentColor = '#00ff00'
var selectedChildColor = '#00cc99'
var selectedLinkColor = '#00dd00'
var backgroundColor = GraphUtil.isBlackBackground() ? '#000000' : '#ffffff'
if (defaults.blackBackground)
	$('marker#arrowhead').attr('fill', '#ffffff')

var widthCurrent
var heightCurrent
var tx = -canvasSize / 2
var ty = -canvasSize / 2

var progressBarCancelDisplayed = false
var cancelCut = false

// Build the layout model
function buildMap () {
	// get the default graph configuration
	var config = getForceConfig()
	widthCurrent = config.width
	heightCurrent = config.height
	
	if (currentColorBy)
		GraphUtil.setFillMode(currentColorBy)
	
	// set the base SVG size
	setGraphDimensions(config.width, config.height)
	
	// initialize the d3 force layout
	createForceLayout(config)
	
	
	// perform the initial ticks then display the graph
	var initialTickIterator = initialTickGenerator(config, initialTickConfig, GraphUtil, calmTick)
	performInitialTicks(initialTickIterator, function(){
		GraphUtil.force.stop()
		createGraph(config)
		
		if (nodes.size() > 12)
			setIdealGraphPosition()
		
		$('#svgContainerId').removeClass('containsSpinner')
		GraphUtil.force.on("tick", tick)
		updateElementPositions(function(){
			GraphUtil.restoreDependencyPanel('Type');
			GraphUtil.restoreDependencyPanel('Status');
			GraphUtil.applyShowHideDependencies();
		});
	})
}

// creates the d3 force layout and fully initializes it
function createForceLayout (config) {
	
	// Start each node near the center of the map
	$.each(nodes, function() {
		this.x = (config.width / 2) + (10 * Math.random())
		this.px = this.x
		this.y = (config.height / 2) + (10 * Math.random())
		this.py = this.py
		this.selected = GraphUtil.SELECTION_STATES.NOT_SELECTED
	});
	
	// Create the force layout
	GraphUtil.force
		.nodes(nodes)
		.links(links)
		.gravity(gravity)
		.linkDistance(function (d) {
			if (d.cut)
				return 1000;
			return config.linkSize;
		})
		.linkStrength(function (d) {
			if (d.duplicate)
				return 0;
			return 3;
		})
		.friction(config.friction)
		.charge(config.charge)
		.size([config.width, config.height])
		.theta(config.theta);
	
	GraphUtil.startForce();
	GraphUtil.force.stop();
	
	// Calculate the maximum weight value, which is used during the tick function
	var maxWeight = 1
	_(nodes).forEach(function(o) {
		maxWeight = Math.max( maxWeight, (o.weight?o.weight:0) )
		o.fix = false
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
	
	// calculate which nodes should use the gravity
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
}

// creates the graph SVG and sets up the bindings and behaviors for it
function createGraph (config) {

	// updates the current fillMode
	fillMode = GraphUtil.getFillMode()
	
	// create the zoom, drag, and selection behaviors and add their handlers
	createBehaviorHandler()
	
	setGraphDimensions(config.width, config.height)
	
	svgContainer.style('background-color', backgroundColor)
	
	// Create the SVG element
	canvas
		.attr("class", 'draggable chart')
		.attr("style", graphstyle)
		.on("dblclick.zoom", null)
	
	// add key listeners to the graph
	GraphUtil.addKeyListeners()
	
	// transform the canvas
	GraphUtil.transformElement(canvas, canvasSize/2, canvasSize/2, 1)
	
	// create the main group in the SVG
	vis = canvas
		.append('svg:g')
			.on("dblclick.zoom", null)
			.style('width', 'auto')
			.style('height', 'auto')
		.append('svg:g')
	
	
	// create the group element to contain the selection region
	GraphUtil.selectionElements.layer = canvas
		.append('svg:g')
			.attr('class', 'selectionLayer')
			.on("dblclick.zoom", null)
			.style('width', 'auto')
			.style('height', 'auto')
			.style('display', 'none')
		
	GraphUtil.selectionElements.regionPath = GraphUtil.selectionElements.layer.append('path')
		.attr('class', 'selectionPath')
	GraphUtil.selectionElements.regionProjection = GraphUtil.selectionElements.layer.append('path')
		.attr('class', 'selectionProjection')

	
	transformContainer = vis
	GraphUtil.transformElement(transformContainer, -canvasSize/2, -canvasSize/2, 1)
	
	// create the SVG bindings for the nodes, labels, and edges
	createSVGBindings(nodes, links)
	
	// add pointers to the bound elements
	GraphUtil.addBindingPointers()
	
	// Update the classes for all data bound svg objects
	GraphUtil.updateAllClasses()
	
	// set up dimensional data for the svg bindings
	if (nodes.length > 1000)
		GraphUtil.setNodeDimensions(true)
	else
		GraphUtil.setNodeDimensions(false)
	
	// get the widths of the rasterized labels
	getLabelWidths()
	
	// bind the "color by" radio buttons
	$('#colorBySelectId').unbind('change').on('change', setColorBy)
	setColorBy()
	GraphUtil.correctPanelSizes()
	
	// bind the show budle conflicts checkbox
	$('#bundleConflictsId').unbind('change').on('change', function (e) {
		GraphUtil.updateLinkClasses()
		GraphUtil.updateNodeClasses()
	});
}

// creates the SVG elements for the nodes, labels, and edges
function createSVGBindings (nodes, links) {
	
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
			.attr("y2", function(d) { return d.target.y;})
	
	// line opacity is really slow in firefox so don't use it initially
	if ($.browser.mozilla)
		GraphUtil.linkBindings.style('opacity', 1)
	
	// Add the nodes to the SVG
	GraphUtil.nodeBindings = vis.selectAll("use")
		.data(nodes).enter()
	
	// Create the nodes
	GraphUtil.nodeBindings = GraphUtil.nodeBindings
		.append("svg:use")
			.attr("xlink:href", function (d) {
				return '#' + appSVGShapes.shape[assetTypes[d.type].internalName].id;
			})
			.attr("class", "node")
			.attr("assetId", function (d) {
				return d.id;
			})
			.attr("assetClass", function (d) {
				return d.assetClass;
			})
			.call(dragBehavior)
			.attr("id", function(d) { return 'node-'+d.index })
			.attr("style", function(d) {
				return "cursor:default; fill:" + GraphUtil.getFillColor(d, fill, fillMode) + ";";
			})
			.attr("cx", 0)
			.attr("cy", 0)
			.attr("transform", "translate(0, 0)")
	
	// Add titles to the nodes
	GraphUtil.nodeBindings.append("svg:title").text(function(d){ return d.title })
	
	// Create the label containers
	GraphUtil.labelBindings = vis.selectAll("g.node")
		.data(nodes).enter()
		.append("svg:g")
		.attr("cx", 0)
		.attr("cy", 0)
		.attr("transform", "translate(0, 0)")
	
	GraphUtil.labelBindings.attr("class", "label")
	
	// Create the label backgrounds
	GraphUtil.labelTextBackgroundBindings = GraphUtil.labelBindings.append("svg:text")
		.attr("id", function (d) {
			return "label2-" + d.id;
		})
		.attr("class", "ignoresMouse labelBackground")
		.attr("dx", 18)
		.attr("dy",".35em")
		.text(function(d) {
			return d.name;
		})
	
	// Create the label foregrounds
	GraphUtil.labelTextBindings = GraphUtil.labelBindings.append("svg:text")
		.attr("id", function (d) {
			return "label-" + d.id;
		})
		.attr("class", "ignoresMouse")
		.attr("dx", 18)
		.attr("dy",".35em")
		.text(function(d) {
			return d.name;
		})
	
	// Filter the visibility of the labels
	GraphUtil.setShowLabels(GraphUtil.force.nodes())
}

// sets up the handlers to use with the zoom and drag behaviors
function createBehaviorHandler () {
	
	
	// set the region selection behavior
	selectionBehavior = d3.behavior.drag()
		.on("dragstart", selectStart)
		.on("drag", selectMove)
		.on("dragend", selectEnd)
	
	svgContainer.call(selectionBehavior)
	svgContainer[0][0].__chart__ = {x:0,y:0,k:1}
	
	// set the zoom behavior
	zoomBehavior = d3.behavior.zoom()
		.on("zoom", zooming)
	svgContainer
		.call(zoomBehavior)
		.on("dblclick", resetView)
		.on("dblclick.zoom", null)
	
	// set the custom node dragging behavior
	dragBehavior = d3.behavior.drag()
		.on("dragstart", dragstart)
		.on("drag", dragmove)
		.on("dragend", dragend)
	
	// local variables used to manage state between handler functions
	var startAlpha = 0
	var dragging = false
	var clicked = false
	var selectingRegion = false
	var dragNodes = []
	var cursorPosition = new GraphPoint(0, 0)
	var tempFreeze = false
	
	// fires on mousedown on a node
	function dragstart (d, i) {
		startAlpha = GraphUtil.force.alpha()
		dragging = true
		clicked = true
		GraphUtil.captureEvent()
	}

	// fires when the user drags a node
	function dragmove (d, i) {
		if ((d3.event.dx != 0 || d3.event.dy != 0) || (!clicked)) {
			let dx = d3.event.dx * zoomBehavior.scale()
			let dy = d3.event.dy * zoomBehavior.scale()
			// calculate alpha stuff
			var shouldUpdatePositions = false
			startAlpha = Math.min(startAlpha+0.005, 0.1)
			shouldUpdatePositions = (GraphUtil.force.alpha() < startAlpha) && (!GraphUtil.setAlpha(0.1))
			
			// the nodes that should be dragged
			dragNodes = [d]
			// if the user is dragging a selected node, drag all selected nodes
			if (d.selected == GraphUtil.SELECTION_STATES.SELECTED_PRIMARY)
				dragNodes = selectedNodes
			
			// get the scale multiplier
			var multiplier = zoomBehavior.scale()
			if (isIE)
				multiplier = 1 // ie doesn't get mouse position correctly
			
			// translate each node to the new position
			for (node of dragNodes) {
				// fix node in place
				node.fix = true
				node.fixed = true
				// calculate new position for this node
				node.x += dx / multiplier
				node.y += dy / multiplier
				node.px = node.x
				node.py = node.y
				if (node.cutShadow)
					node.cutShadow.transform.baseVal.getItem(0).setTranslate(node.x, node.y)
				// update the position if needed
				if (shouldUpdatePositions)
					GraphUtil.updateNodePosition(node)
			}
			
			// update the element positions if needed
			if (shouldUpdatePositions)
				updateElementPositions()
			
			// mark that this is a drag, not a click
			clicked = false
			// stop event from bubbling
			GraphUtil.captureEvent()
		}
	}

	// fires on mouseup on a node. if the user never dragged moved their mouse, treat this like a click
	function dragend (d, i) {
		for (node of dragNodes) {
			node.fix = false
			node.fixed = false
		}
		dragNodes = []
		var event = d3.event.sourceEvent
		dragging = false
		if (clicked) {
			var mode = GraphUtil.SELECT_MODES.REPLACE
			if (event.ctrlKey) 
				mode = GraphUtil.SELECT_MODES.TOGGLE
			modifyNodeSelection([d], mode)
		}
		d3.event.sourceEvent.preventDefault()
		GraphUtil.captureEvent()
	}
	
	// Rescales the contents of the svg. Called when the user scrolls.
	function zooming (e) {
		if (!dragging && !selectingRegion) {
			GraphUtil.captureEvent()
			
			var offset = canvasSize / 2
			var x = d3.event.translate[0] - offset
			var y = d3.event.translate[1] - offset
			GraphUtil.transformElement(transformContainer, x, y, d3.event.scale)
		}
	}
	
	// fires when the user begins dragging a selection region
	function selectStart (d, i) {
		var event = d3.event.sourceEvent
		if (event.shiftKey) {
			// pause the force graph while the user is selecting
			if (GraphUtil.force.alpha() > 0) {
				tempFreeze = true
				GraphUtil.force.stop()
			}
			// get the cursor position on the graph
			var mPos = d3.mouse(svgContainer[0][0])
			cursorPosition.setScreenCoordinates(mPos[0], mPos[1])
			// show the region selection layer
			GraphUtil.selectionElements.layer.style('display','')
			// initialize the region selection at the current point
			GraphUtil.initializeSelectionPath(cursorPosition, event.ctrlKey)
			selectingRegion = true
			// capture the mouse event to avoid further propagation/bubbling
			GraphUtil.captureEvent()
		}
	}
	// fires whenever the user moves the mouse while selecting a region
	function selectMove (d, i) {
		var event = d3.event.sourceEvent
		if (selectingRegion) {
			// get the cursor position on the graph
			var mPos = d3.mouse(svgContainer[0][0])
			cursorPosition.setScreenCoordinates(mPos[0], mPos[1])
			// set the mouse cursor to the crosshair while selecting
			canvas[0][0].classList.add('shift_key')
			// update the selection path based on this new position
			GraphUtil.setTempSelectionPath(cursorPosition)
			GraphUtil.updateSelectionPath(cursorPosition, false)
			// capture the mouse event to avoid further propagation/bubbling
			GraphUtil.captureEvent()
		}
	}
	// fires when the user releases the mouse while selecting a region
	function selectEnd (d, i) {
		var event = d3.event.sourceEvent
		if (selectingRegion) {
			// if we paused the graph at the start of this selection, unpause it now
			if (tempFreeze) {
				tempFreeze = false
				GraphUtil.force.resume()
			}
			// get the cursor position on the graph
			var mPos = d3.mouse(svgContainer[0][0])
			cursorPosition.setScreenCoordinates(mPos[0], mPos[1])
			// set the mouse cursor back to the default
			canvas[0][0].classList.remove('shift_key')
			// hide the region select layer
			GraphUtil.selectionElements.layer.style('display','none')
			// finalize the selection region and filter the nodes
			GraphUtil.updateSelectionPath(cursorPosition, true)
			GraphUtil.filterRegionSelection()
			selectingRegion = false
			// capture the mouse event to avoid further propagation/bubbling
			GraphUtil.captureEvent()
		}
	}
}

// asynchronously performs the initial ticks for the graph then executes the callback
function performInitialTicks (tickItr, callback) {
	if (tickItr.next().done)
		callback()
	else
		window.setTimeout(performInitialTicks, 1, tickItr, callback);
}

// Updates the dynamic attributes of the svg elements every tick of the simulation
function tick (e) {
	updateElementPositions()
}

// calls the tick function once
function calmTick (n) {
	for (var i = 0; i < n; ++i) {
		GraphUtil.force.resume()
		GraphUtil.force.tick()
		GraphUtil.force.stop()
	}
}

// updates the attributes of all the svg elements (in as low-level a way as possible for efficiency)
function updateElementPositions (callback) {
	var d = null;
	
	// set the dynamic attributes for the nodes
	$(GraphUtil.nodeBindings[0]).each(function (i, o) {
		d = o.__data__;
		o.transform.baseVal.getItem(0).setTranslate(d.x, d.y);
		if (d.cutShadow) {

			var yOffset = d.y;
			if (d && d.type === 'Other') {
				yOffset = d.y - 5;
			}

			d.cutShadow.transform.baseVal.getItem(0).setTranslate(d.x, yOffset);
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
		o.transform.baseVal.getItem(0).setTranslate(d.x, d.y);
	});
	
	if (isIE)
		vis.style('line-height', Math.random())

	drawContextMenu();

	if (callback) {
		return callback();
	}
}

// Handles the modification of node selection based on a given list of nodes (the "delta") and a selection mode (from GraphUtil enum)
function modifyNodeSelection (delta, mode, skipRender) {
	// keep track of which nodes will be added to or removed from the selection in this function call
	var toSelect = []
	var toDeselect = []
	
	// if we are using subtraction mode, just remove these nodes from the selection
	if (mode == GraphUtil.SELECT_MODES.SUB) {
		toDeselect = delta
		selectedNodes = _.difference(selectedNodes, delta)
		let linksToCheck = []
		toDeselect.each(function (node, i){
			node.selected = GraphUtil.SELECTION_STATES.NOT_SELECTED
			linksToCheck = _.union(linksToCheck, GraphUtil.getAdjacentLinks(node))
		})
		linksToCheck.each(function (link) {
			link.selected = GraphUtil.SELECTION_STATES.NOT_SELECTED
		})
		
	// if we are in replace mode, change the selection to the current set of nodes
	} else if (mode == GraphUtil.SELECT_MODES.REPLACE) {
		toSelect = _.difference(delta, selectedNodes)
		toDeselect = _.difference(selectedNodes, delta)
		if (delta.size() == 1 && _.isEqual(delta, selectedNodes)) {
			toSelect = []
			toDeselect = delta
		}
		modifyNodeSelection(toDeselect, GraphUtil.SELECT_MODES.SUB, true)
		modifyNodeSelection(toSelect, GraphUtil.SELECT_MODES.ADD, true)
		
	// same as normal replace mode, but don't toggle single nodes
	} else if (mode == GraphUtil.SELECT_MODES.REPLACE_NO_TOGGLE) {
		toSelect = _.difference(delta, selectedNodes)
		toDeselect = _.difference(selectedNodes, delta)
		modifyNodeSelection(toDeselect, GraphUtil.SELECT_MODES.SUB, true)
		modifyNodeSelection(toSelect, GraphUtil.SELECT_MODES.ADD, true)
	
	// if every given node is already selected, deselect them with subtraction mode
	} else if (_.difference(delta, selectedNodes).size() == 0) {
		toDeselect = selectedNodes
		modifyNodeSelection(delta, GraphUtil.SELECT_MODES.SUB, true)

	// if we are in additive mode, add the new nodes to the selection
	} else if (mode == GraphUtil.SELECT_MODES.ADD) {
		toSelect = _.difference(delta, selectedNodes)
		selectedNodes = _.union(selectedNodes, delta)
		toSelect.each(function (node, i){
			node.selected = GraphUtil.SELECTION_STATES.SELECTED_PRIMARY
			GraphUtil.getAdjacentLinks(node).each(function (link) {
				link.selected = GraphUtil.SELECTION_STATES.SELECTED_PRIMARY
			})
		})
		
	// if we are in toggle mode, toggle the state of the given nodes
	} else if (mode == GraphUtil.SELECT_MODES.TOGGLE) {
		toDeselect = _.intersection(delta, selectedNodes)
		toSelect = _.difference(delta, selectedNodes)
		modifyNodeSelection(toDeselect, GraphUtil.SELECT_MODES.SUB, true)
		modifyNodeSelection(toSelect, GraphUtil.SELECT_MODES.ADD, true)

	// if an unknown mode is given, output a warning to the console
	} else {
		console.warn('[WARNING] - Invalid mode: "%o"', mode)
	}

	
	// if it wasn't specified that the render should be skipped, update the styles and reoreder the DOM
	if (!skipRender) {
		// if the selection was changed, update the elements
		if (toSelect.size() > 0 || toDeselect.size() > 0) {
			GraphUtil.updateAllClasses()
			// Sort all the svg elements to reorder them in the DOM (SVG has no z-index property)
			GraphUtil.reorderDOM()
		}
	}
}

// Used to rebuild the layout using the new parameters
function rebuildMap (layoutChanged, charge, linkSize, friction, theta, width, height) {

	// handle resizing when not in fullscreen mode
	if (!GraphUtil.isFullscreen() && (width && height))
		resizeGraph(width, height)
	
	// handle the background color
	var blackBackground = GraphUtil.isBlackBackground()
	backgroundColor = blackBackground ? '#000000' : '#ffffff'
	svgContainer.style('background-color', backgroundColor)
	GraphUtil.forceReflow(svgContainer)
	
	// Create the force layout
	if (linkSize)
		GraphUtil.force.linkDistance(function (d) {
			if (d.cut)
				return cutLinkSize
			return linkSize
		})
	if (friction)
		GraphUtil.force.friction(friction)
	if (charge)
		GraphUtil.force.charge(charge)
	if (theta)
		GraphUtil.force.theta(theta)
	
	// Reset the list of types to show names for
	var labelsChanged = GraphUtil.setShowLabels(GraphUtil.force.nodes())
	
	// Update the classes for all data bound svg objects
	GraphUtil.updateAllClasses()
	
	// Set the new label widths
	if (labelsChanged)
		getLabelWidths()
	
	// updates the current fillMode
	fillMode = GraphUtil.getFillMode()
	
	// if we only changed the labels or background color, only one tick is needed to reflect this change
	if (layoutChanged)
		GraphUtil.startForce()
	else
		GraphUtil.force.tick()
}

function resizeGraph (width, height) {
	widthCurrent = width
	heightCurrent = height
	
	setGraphDimensions(width, height)
	
	var oldSize = GraphUtil.force.size()
	var dx = width - oldSize[0]
	var dy = height - oldSize[1]
	GraphUtil.force.nodes().each(function (o, i) {
		o.x += dx / 2
		o.y += dy / 2
	});
	
	GraphUtil.force.size([width, height])
	GraphUtil.startForce()
	
	reoptimizeGraph()
}

// centers the view onto the graph
function centerGraph () {
	// calculate the ranges of positions for nodes
	var ranges = getGraphRanges()
	var visWidth = ranges.maxX - ranges.minX
	var visHeight = ranges.maxY - ranges.minY
	
	// calculate the graph dimensions
	var dimensions = getDimensionsForOptimizing()
	var graphWidth = dimensions.graphWidth
	var graphHeight = dimensions.graphHeight
	
	// calculate the new scale
	var ratioX = graphWidth / visWidth
	var ratioY = graphHeight / visHeight
	var scaleAfter = Math.min(ratioX, ratioY)
	
	// calculate the new translate
	var translateXAfter = (graphWidth / 2) - ((visWidth / 2) + ranges.minX) * scaleAfter + graphPadding
	var translateYAfter = (graphHeight / 2) - ((visHeight / 2) + ranges.minY) * scaleAfter + graphPadding
	if (GraphUtil.isFullscreen())
		translateYAfter += $('#dependencyDivId').height()
	var translateAfter = [translateXAfter, translateYAfter]
	
	// set the new scale and translate values
	if (scaleAfter < 0.1 )
		// the graph exceeded the min scale, so use the max size translate
		zoomBehavior.scale(tooBigZoom.scale).translate(tooBigZoom.translate)
	else if (scaleAfter > 2)
		// the graph exceeded the max scale, so use the default scale
		zoomBehavior.scale(1).translate([0, 0])
	else
		// use the calculated transform
		zoomBehavior.scale(scaleAfter).translate(translateAfter)
	
	zoomBehavior.event(svgContainer)
}

// finds the rotation of the graph that requires the least zooming
function findBestRotation () {
	var dimensions = getDimensionsForOptimizing()
	var graphWidth = dimensions.graphWidth
	var graphHeight = dimensions.graphHeight
	var bestAngle = 0
	var bestScale = 0
	var iterations = 180
	var angleChange = 360 / iterations
	for (var i = 1; i <= iterations; i++) {
		if (i == (iterations / 4) + 1) {
			GraphUtil.rotateGraph(180 - angleChange)
			i = 3 * (iterations / 4) - 1
		} else {
			GraphUtil.rotateGraph(angleChange)
			
			var ranges = getGraphRanges()
			
			var visWidth = ranges.maxX - ranges.minX
			var visHeight = ranges.maxY - ranges.minY
			var ratioX = graphWidth / visWidth
			var ratioY = graphHeight / visHeight
			var scaleAfter = Math.min(ratioX, ratioY)
			if (scaleAfter >= bestScale) {
				bestScale = scaleAfter
				bestAngle = i * angleChange
			}
		}
	}
	return bestAngle
}

function getGraphRanges () {
	var returnVal = {}
	returnVal.minX = 9999999999
	returnVal.maxX = -9999999999
	returnVal.minY = 9999999999
	returnVal.maxY = -9999999999
	var radius = GraphUtil.nodeRadius.Default
	
	GraphUtil.force.nodes().each(function(o, i) {
		var offsetX = o.dimensions.width / 2
		var offsetY = o.dimensions.height / 2
		returnVal.minX = Math.min(returnVal.minX, o.x - offsetX - radius)
		returnVal.maxX = Math.max(returnVal.maxX, o.x + Math.max(o.labelWidth, offsetX) + radius)
		returnVal.minY = Math.min(returnVal.minY, o.y - offsetY - radius)
		returnVal.maxY = Math.max(returnVal.maxY, o.y + offsetY + radius)
	})
	
	return returnVal
}

function setIdealGraphPosition () {
	if (nodes.length < 3000) {
		var angle = findBestRotation()
		GraphUtil.rotateGraph(angle)
		updateElementPositions()
		centerGraph()
	}
}

function reoptimizeGraph () {
	zoomBehavior
		.translate([0, 0])
		.scale(1)
		.event(svgContainer)
	setIdealGraphPosition();
	drawContextMenu();
}

function drawContextMenu() {
	// Trigger action when the contexmenu is about to be shown
	$(document).bind("contextmenu", function (event) {

		if (event.shiftKey)
			return;
		var validTags = ['use', 'svg', 'g', 'line'];
		var target = event.target.correspondingUseElement || event.target;
		var tag = target.tagName;

		if (validTags.indexOf(tag) != -1) {
			event.preventDefault();

			// remove old items
			$(".customMenu").children(".tempItem").remove();

			// node specific items
			if (tag == 'use') {

				var data = target.__data__;

				$("#consoleOutputItemId").on('click', function (a, b) {
					closeMenu();
				});

				var showAssetItem = '<li class="tempItem show" id="showAssetItemId">Show Asset</li>';
				$(".customMenu").append(showAssetItem);
				$("#showAssetItemId").on('click', function (a, b) {
					closeMenu();
					EntityCrud.showAssetDetailView(data.assetClass, data.id);
				});

				//<tds:hasPermission permission="${Permission.AssetEdit}">
				var editAssetItem = '<li class="tempItem edit" id="editAssetItemId">Edit Asset</li>';
				$(".customMenu").append(editAssetItem);
				$("#editAssetItemId").on('click', function (a, b) {
					closeMenu();
					EntityCrud.showAssetEditView(data.assetClass, data.id);
				});
				//</tds:hasPermission>
			}

			// node specific items
			if (tag == 'line') {

				var data = target.__data__;

				$("#consoleOutputItemId").on('click', function (a, b) {
					closeMenu();
				});

				var showDependencyItem = '<li class="tempItem show" id="showDependencyItemId">Show Dependency</li>';
				$(".customMenu").append(showDependencyItem);
				$("#showDependencyItemId").on('click', function (a, b) {
					closeMenu();
					EntityCrud.showAssetDependencyEditView(data.source, data.target, 'view');
				});

				//<tds:hasPermission permission="${Permission.AssetEdit}">
				var editDependencyItemId = '<li class="tempItem edit" id="editDependencyItemId">Edit Dependency</li>';
				$(".customMenu").append(editDependencyItemId);
				$("#editDependencyItemId").on('click', function (a, b) {
					closeMenu();
					EntityCrud.showAssetDependencyEditView(data.source, data.target, 'edit');
				});
				//</tds:hasPermission>
			}

			$(".customMenu").css({
				top: event.pageY + "px",
				left: event.pageX + "px",
				display: "block"
			})
		}

	});

	// If the document is clicked somewhere
	$(document).bind("mousedown", function (e) {
		// If the clicked element is not the menu
		if (!$(e.target).parents(".customMenu").length > 0) {
			closeMenu();
		}
	});

	// close the context menu
	function closeMenu () {
		$(".customMenu").css('display', "none").children(".tempItem").remove();
	}
}

function getDimensionsForOptimizing () {
	var dimensions = GraphUtil.getProperGraphDimensions()
	var returnVal = {}
	returnVal.graphWidth = dimensions.width - graphPadding * 2
	returnVal.graphHeight = dimensions.height - graphPadding * 2
	if (GraphUtil.isFullscreen())
		returnVal.graphHeight -= $('#dependencyDivId').height()
	return returnVal
}

// calculates the label widths for every node
function getLabelWidths () {
	GraphUtil.force.nodes().each(function (o, i) {
		if (o.showLabel) {
			var labelElement = o.labelElement.select('text:not(.labelBackground)')[0][0]
			var extent = labelElement.getExtentOfChar(labelElement.getNumberOfChars() - 1)
			var offset = labelElement.dx.baseVal.getItem(0).value
			var labelWidth = extent.x + extent.width + offset
			o.labelWidth = labelWidth
		} else {
			o.labelWidth = 0
		}
	});
}

// resets the translate to (0,0)
function resetTranslate () {
	zoomBehavior
		.translate([0, 0])
		.event(svgContainer)
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
				if (n.cutGroup == -1)
					n.cutGroup = 0;
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
	GraphUtil.force.nodes().each(function (node) {
		console.log(node)
		node.cutGroup = -1
		node.cut = 0
	})
	GraphUtil.force.links().each(function (link) {
		link.cut = null
	})
	
	GraphUtil.createCutShadows(fill)
	
	rebuildMap(true, false, parseInt($('#linkSizeId').val()), false, false);
}


// Stops the map by setting the alpha value to 0
function stopMap () {
	GraphUtil.force.stop()
}

// gets the normal width of this graph
function getStandardWidth () {
	var graphOffset = $('#svgContainerId').offset().left
	var pageWidth = $(window).width()
	if (GraphUtil.isFullscreen())
		return pageWidth
	return pageWidth - graphOffset * 2
}

// gets the normal height of this graph
function getStandardHeight () {
	var bottomMargin = $('.main-footer').outerHeight()
	var graphOffset = $('#svgContainerId').offset().top
	var pageHeight = $(window).height()
	if (GraphUtil.isFullscreen())
		return pageHeight
	return pageHeight - graphOffset - bottomMargin - graphPadding
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
	var nodes = []
	if (node) 
		for (var i = 0; i < node.dependsOn.length; ++i)
			nodes.push(links[node.dependsOn[i]].target)
	return nodes
}

// gets the list of parents for the specified node
function getParents (node) {
	var nodes = []
	if (node)
		for (var i = 0; i < node.supports.length; ++i)
			nodes.push(links[node.supports[i]].source)
	return nodes
}

// gets the list of nodes with dependencies involving for the specified node
function getDependencies (node) {
	var depList = []
	if (node) {
		for (var i in node.dependsOn)
			depList.push(links[node.dependsOn[i]])
		for (var i in node.supports)
			depList.push(links[node.supports[i]])
	}
	return depList
}

function getLinkDistance (link) {
	var width = Math.abs(link.target.x - link.source.x)
	var height = Math.abs(link.target.y - link.source.y)
	return Math.sqrt(width*width + height*height)
}

// initializes the progress bar for min cuts
function displayProgressBar () {
	var progressBar = tds.ui.progressBar(-1, 999999, function() {}, function() {}, "Calculating Group Split Suggestion")
	progressBarCancelDisplayed = false
	
	return progressBar
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

// Resets the scale and position of the map. Called when the user double clicks on the background
function resetView () {
	if (d3.select(d3.event.target)[0][0].nodeName == 'use')
		return
	centerGraph()
}

// gets the config object for the force layout
function getForceConfig () {
	// get the default graph configuration
	var charge = defaults['force']
	var linkSize = defaults['linkSize']
	var friction = defaults['friction']
	var theta = defaults['theta']
	var width = getStandardWidth()
	var height = getStandardHeight()
	
	return {
		'charge': charge,
		'linkSize': linkSize,
		'friction': friction,
		'theta': theta,
		'width': width,
		'height': height
	}
}

// sets the Color By value for the map using the checked option on the control panel
function setColorBy () {
	fillMode = GraphUtil.getFillMode()
	currentColorBy = fillMode
	GraphUtil.nodeBindings.style("fill", function(d) {
		return GraphUtil.getFillColor(d, fill, fillMode)
	});
	GraphUtil.updateLegendColorKey(colorByGroups[fillMode], fill, fillMode)
}

// function for debugging performance
function debugTiming (message) {
	console.log((performance.now() - debugTime) + ' ms');
	debugTime = performance.now();
	var fullLine = '##############################################################';
	var output = ' ' + message + ' done ';
	output = fullLine.substr(0, Math.floor((fullLine.length - output.length) / 2)) + output + fullLine.substr(0, Math.ceil((fullLine.length - output.length) / 2));
	console.log(output);
}

function setGraphDimensions (width, height) {
	svgContainer
		.style("width", width + 'px')
		.style("height", height + 'px')
	canvas
		.attr("width", width)
		.attr("height", height)
}
</script>
<ul class="customMenu"></ul>
</div>
