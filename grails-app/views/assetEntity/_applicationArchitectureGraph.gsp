<%@page defaultCodec="none" %>
<%@page import="net.transitionmanager.security.Permission"%>
<div id="scriptDivId">
<style>
/* 	these styles must be included here due to a bug in firefox
	where marker styles don't work when used in external stylesheets. */
div.item1 {
	float: left;
}
line.link {
	marker-end: url(#arrowhead);
}
line.link.unresolved {
	marker-end: url(#arrowheadUnresolved);
}
/*line.link.notApplicable {
	marker-end: url(#arrowheadNotApplicable);
}*/
line.link.selected {
	marker-end: url(#arrowheadSelected);
}
line.link.cyclical:not(.selected) {
	marker-end: url(#arrowheadCyclical);
}

#arrowheadCyclical {
	opacity: 1.0 !important;
}
</style>

<script type="text/javascript">

GraphUtil.force = d3.layout.force();
var defaults = {"width":getStandardWidth(), "height":getStandardHeight(), "blackBackground":false};

var canvas = d3.select("div#svgContainerId")
	.append("svg:svg")
	.attr('class', 'chart')
	.attr('id', 'graphSvgId');

// define the arrowhead markers used for marking dependencies
var defs = canvas.append("defs");
defs.html(appSVGShapes.getAll());
defineShapes(defs);
var arrowOffset = 16;

// bind browser resizing to resizing the svg
$(window).resize( function(a, b) {
	GraphUtil.resetGraphSize();
});

// handle the checkbox to toggle hiding of redundant dependencies
var highlightCyclical = $('#highlightCyclicalCheckBoxId').is(':checked');
$('#highlightCyclicalCheckBoxId').on('change', function (a, b) {
	highlightCyclical = a.target.checked;
	GraphUtil.tickOnce();
});
// handle the checkbox to toggle hiding of node labels
$('#labelCheckBoxId').on('change', function (a, b) {
	GraphUtil.tickOnce();
});

var zoomBehavior;
var onInitialGraphExecution = true;
var offsetGroup = canvas;
var vis = canvas;
var background;
var overlay;

var links = null;
var assets = null;
var assetTypes = null;
links = ${links};
assets = ${nodes};
assetTypes = ${assetTypesJson};
environment = '${environment}';

// set the asset select to display the correct asset
if (assets.size() > 0) {
	if ($('#assetSelectId').select2('data')) {
		$('#assetSelectId').select2('data').id = assets[0].id;
		$('#assetSelectId').select2('data').text = assets[0].name;
	}
	var select2List = $('.select2-chosen');
	if(select2List.length) {
		// There are two Select2, one for Asset Classes and one for Filter
		$(select2List[0]).html(assets[0].name);
	}
}

var root = null;
var assetsById;
var r = 5;
var horizontalSpace = 30;
var verticalSpace = 80;
var horizontalSpaceMod = 15;
var verticalSpaceMod = 40;
var widestRow = 0;
var nodeSelected = null;
var maxHorizontalSpace = 500;
var maxVerticalSpace = 800;
var minHorizontalSpace = 15;
var minVerticalSpace = 30;
var nodeOffsetIncrements = 2;
var graphPadding = 30
var preferenceName = '${com.tdsops.tm.enums.domain.UserPreferenceEnum.DEP_GRAPH.name()}';

// color constants
var defaultColor = '#0000ff';
var nodeColor = '#5478BA';
var selectedParentColor = '#00ff00';
var selectedChildColor = '#00cc99';
var selectedLinkColor = '#00dd00';
var backgroundColor = GraphUtil.isBlackBackground() ? '#000000' : '#ffffff';

if (assets.size() > 0)
	$('#pageTitleId').html('Architecture Graph for ' + assets[0].name);

var widthCurrent;
var heightCurrent;
var initialDimensions = GraphUtil.getProperGraphDimensions();
var offsetX = 0;
var offsetY = 0;
var siblingGroups = [];
var siblingGroupsReduced = [];
var nodeMap = null
var rootNode;

var floatMode = false;

// execute layout initialization functions
if (assets.size() > 0) {
	constructPointers();
	findCyclicalDependencies(assets[0]);
	rootNode = createRootNode();
	findRedundantDependencies(rootNode);
	setNodeDirections();
	setY(assets[0], 0, 'down', 0, []);
	assets[0].qy = null;
	setY(assets[0], 0, 'up', 0, []);
	offsetRows();

	// call the function to create the graph
	buildMap (initialDimensions.width, initialDimensions.height);
} else {
	GraphUtil.resetGraphSize();
	canvas.style('background-color', backgroundColor);
}

// changes the references to array indices to object pointers
function constructPointers () {
	
	// construct object references and data for each node
	$.each(assets, function (i, asset) {
	
		if (asset.parents == null)
			asset.parents = [];
		if (asset.children == null)
			asset.children = [];
		$.each(asset.parents, function (j, link) {
			asset.parents[j] = links[link];
		});
		$.each(asset.children, function (j, link) {
			asset.children[j] = links[link];
		});
		
		asset.allChildren = asset.children.clone();
		asset.allParents = asset.parents.clone();
		asset.height = 0;
		asset.childGroups = [];
		asset.siblingGroups = [];
		asset.siblingGroupParents = [];
		asset.exSet = [];
		asset.exChild = null;
		asset.exParent = null;
		asset.endOfExclusive = null;
		asset.root = false;
		asset.repositioned = false;
		asset.fillColor = nodeColor;
		
	});
	// construct object references and data for each link
	$.each(links, function (i, link) {
		link.child = assets[link.child];
		link.parent = assets[link.parent];
		link.root = false;
		link.partOfCycle = false;
	});
}

// sets the direction each node is from the center node
function setNodeDirections () {
	var mainAsset = assets[0];
	
	// initially give all nodes a direction of 'none'
	for (var i = 0; i < assets.size(); ++i)
		assets[i].direction = 'none';
	
	var callCount = 0;
	
	setDirection(mainAsset, 'up', 0);
	setDirection(mainAsset, 'down', 0);
	
	// recursive function that sets the direction for node and its dependencies in a given direction
	function setDirection (node, direction, indent) {
		var indentString = '';
		for (var i = 0; i < indent; ++i) {
			indentString += '   ';
		}
		
		// break infinite loops
		callCount++;
		if (callCount > 100000) {
			console.log('setNodeDirections() in infinite loop');
			return;
		}
		
		if (node.distance == null || node.distance > indent)
			node.distance = indent;
		
		node.direction = direction;
		var nodes = [];
		if (direction == 'up')
			nodes = getChildren(node);
		else
			nodes = getParents(node);
		for (var i = 0; i < nodes.size(); ++i)
			setDirection(nodes[i], direction, indent+1);
	}
	
	// remove nodes that weren't given a direction
	for (var i = 0; i < assets.size(); ++i)
		if (assets[i].direction == 'none') {
			removeNode(assets[i]);
			--i;
		}
}


// recursively sets the y value for each node
function setY (node, y, dir, calls, path) {
	
	// don't go back to nodes we have already been to (cyclical structures haven't been removed yet)
	if (path.indexOf(node.id) != -1) {
		return;
	}
	path.push(node.id);
	var newY = y * verticalSpace;
	
	if (node.qy == null || Math.abs(newY) > Math.abs(node.qy)) {
	
		node.y = newY;
		node.cy = newY;
		node.py = newY;
		node.qy = newY;
		node.row = y;
		node.x = 0;
		node.qx = 0;
		node.dir = dir;
		var indent = '';
		for (var i = 0; i < y; ++i) {
			indent += '   ';
		}
	}
	
	// call this function for each of the chilren or parents, depending on dir
	var nodes = [];
	if (dir == 'up') {
		nodes = getChildren(node);
		for (var i = 0; i < nodes.size(); ++i)
			setY(nodes[i], y + 1, dir, calls+1, path);
	} else {
		nodes = getParents(node);
		for (var i = 0; i < nodes.size(); ++i)
			setY(nodes[i], y - 1, dir, calls+1, path);
	}
	
	path.pop();
}

// creates a temporary root node to use for layout calculation
function createRootNode () {
	var rootNode = {name:"root", id:-10, root:true, children:[], parents:[], siblings:[], size:5, shape:'circle', childGroups:[], siblingGroups:[], siblingGroupParents:[], exSet:[], repositioned:false};
	$.each(assets, function (i, asset) {
		if (asset.parents.size() == 0) {
			var dep = {parentId:-10, childId:asset.id, parent:rootNode, child:asset, root:true};
			rootNode.children.push(dep);
			asset.parents.push(dep);
			links.push(dep);
		}
	});
	assets.push(rootNode);
	return rootNode;
}

// find and mark any cyclical dependencies using a depth first search
function findCyclicalDependencies (root) {
	var foundCycles = false;
	var stack = [];
	removeCycles();
	searchPath(root, stack, 'down');
	searchPath(root, stack, 'up');
	removeCycles();
	links.sort(function (a, b) {
		if (a.partOfCycle && !b.partOfCycle)
			return 1;
		else if (!a.partOfCycle && b.partOfCycle)
			return -1;
		else
			return 0;
	});
	
	// recursively traverse links, marking any that are cyclical
	function searchPath (node, path, direction) {
		
		// check if we looped back to a previous node
		if (path.indexOf(node) != -1) {
			var tempPath = []
			for (var i = 0; i < path.size(); i++)
				tempPath.push(path[i]);
			tempPath.push(node);
			
			var cycle = [];
			for (var i = path.indexOf(node); i < tempPath.size() - 1; i++) {
				var link;
				if (direction == 'up')
					link = getLinkFromNodePair(tempPath[i+1], tempPath[i]);
				else
					link = getLinkFromNodePair(tempPath[i], tempPath[i+1]);
				link.partOfCycle = true;
				cycle.push(link);
			}
			
			return true;
		}
		
		path.push(node);
		
		// call this for each of this node's children, and mark any links that are cyclical
		if (direction == 'down') {
			for (var i = 0; i < node.children.size(); ++i)
				if (node.children[i].cyclical != true)
					node.children[i].cyclical = searchPath(node.children[i].child, path, direction);
		
		} else {
			for (var i = 0; i < node.parents.size(); ++i)
				if (node.parents[i].cyclical != true)
					node.parents[i].cyclical = searchPath(node.parents[i].parent, path, direction);
		}
		
		path.pop();
		return false;
	}
	
	// remove any cyclical link references from nodes
	function removeCycles () {
		var cyclicalLinks = [];
		for (var i = 0; i < links.size(); ++i) {
			var link = links[i];
			if (link.cyclical) {
				dereferenceLink(link);
				cyclicalLinks.push(link);
				links.splice(links.indexOf(link), 1);
				--i;
			}
		}
		for (var i = 0; i < cyclicalLinks.length; ++i) {
			links.push(cyclicalLinks[i]);
		}
	}
	
	// outputs the contents of a node path as a list
	function printNodePath (path) {
		var output = '';
		for (var i in path)
			output = output + ' --> ' + path[i].name
		console.log(output)
	}
}

// find and remove any redundant dependencies using a queue for a breadth first search
function findRedundantDependencies (root) {
	var queue = [];
	
	// create a root node to traverse from
	for (var i = 0; i < assets.size(); ++i) {
		
		assets[i].checked = false;
	}
	
	// search up the tree
	queue.push(root);
	while (queue.size() > 0)
		searchForRedundency(queue.pop(), 'up');
	
	// uncheck all nodes
	for (var i = 0; i < assets.size(); ++i) {
		assets[i].checked = false;
	}
	
	// after all redundant dependencies have been found, remove them
	$.each(links, function (i, link) {
		if (link.redundant && !link.cyclical) {
			var s = link.child.parents;
			var t = link.parent.children;
			s.splice(s.indexOf(link), 1);
			t.splice(t.indexOf(link), 1);
		}
	});
	
	function searchForRedundency (node, direction) {
		
		/* 	Mark this node as checked.
			Because nodes are only added to the queue when all their parents 
			are checked, all future dependencies checked for redundency are 
			guarenteed to not come from a parent to this node. */
		node.checked = true;
		
		// iterate through the node's children
		var children = node.children;
		for (var j = 0; j < children.size(); ++j) {
			
			// if this child has a parent that hasn't been checked, don't add it to the queue
			var canQueue = true;
			var child = children[j].child;
			for (var i = 0; i < child.parents.size(); i++)
				if ( ! child.parents[i].parent.checked )
					canQueue = false;
			
			// if this child can be queued, add it to the queue if it isn't already there
			if (queue.indexOf(child) == -1 && canQueue)
				queue.unshift(child);
			
			// if this child has other parents, check if one of them leads back to the original node
			if (child.parents.size() > 1) {
				
				// check if this dependency is redundant
				var checked = [];
				var link = children[j];
				if (searchUp(node, child, link, checked, direction)) {
					link.redundant = true;
				}
			}
		}
	}
	
	/*	Searches for @param target from @param start by searching up the tree, ignoring dependency @param ignore.
		Returns true if the dependency is redundant. */
	function searchUp (target, start, ignore, checked, direction) {
		
		// if we found a path that reaches the target, the dependency is redundent
		if (target == start)
			return true;
		
		// if this node has already been checked, we can't get to the target from here
		if (start.checked)
			return false;
		
		// if this node is in the checked list, we have already confirmed that it can't lead to the target
		if (checked.indexOf(start) != -1)
			return false;
		
		
		// search from each of this node's children
		for (var i = 0; i < start.parents.size(); ++i)
			if ( start.parents[i] != ignore && searchUp(target, start.parents[i].parent, ignore, checked, direction) )
				return true;
		
		// we can't reach the target from this node, so add it to the checked list and return false
		checked.push(start);
		return false;
	}
}

// removes a node and all the references to it
function removeNode (node) {
	for (var i = 0; i < assets.size(); ++i) {
		if (assets[i].id == node.id) {
			
			// remove child links
			for (var j = 0; j < assets[i].children.size(); ++j) {
				removeLink(assets[i].children[j]);
				--j;
			}
			
			// remove parent links
			for (var j = 0; j < assets[i].parents.size(); ++j) {
				removeLink(assets[i].parents[j]);
				--j;
			}
			
			// remove redundant links
			for (var j = 0; j < links.size(); ++j) {
				if (links[j].parent == node || links[j].child == node) {
					links.splice(j, 1);
					--j;
				}
			}
			
			// remove it from the node list
			assets.splice(assets.indexOf(node), 1);
			return;
		}
	}
}

// removes a link and all the references to it
function removeLink (link) {
	var p = link.child.parents;
	var c = link.parent.children;
	p.splice(p.indexOf(link), 1);
	c.splice(c.indexOf(link), 1);
	links.splice(links.indexOf(link), 1);
}

// breaks all references from the nodes to a link
function dereferenceLink (link) {
	var s = link.child.parents;
	var t = link.parent.children;
	if (s.indexOf(link) != -1)
		s.splice(s.indexOf(link), 1);
	if (t.indexOf(link) != -1)
		t.splice(t.indexOf(link), 1);
}

// puts back all the node to link references that were previously removed
function rereferenceLink (link) {
	var s = link.child.parents;
	var t = link.parent.children;
	if (s.indexOf(link) == -1)
		s.push(link);
	if (t.indexOf(link) == -1)
		t.push(link);
}

function getLinkFromNodePair (parent, child) {
	var childList = parent.children;
	for (var i = 0; i < childList.size(); i++)
		if (childList[i].child == child)
			return childList[i];
	return null;
}

// offsets the rows so that there are none with negative indices
function offsetRows () {
	var offset = 0;
	for (var i = 0; i < assets.size(); ++i)
		if (assets[i].row < 0)
			offset = Math.min(offset, assets[i].row);
	for (var i = 0; i < assets.size(); ++i) {
		assets[i].row -= offset;
		assets[i].qy -= offset * verticalSpace;
		assets[i].cy -= offset * verticalSpace;
		assets[i].y -= offset * verticalSpace;
	}
}

// Build the layout model
function buildMap (width, height) {
	// Use the new parameters, or the defaults if not specified
	var width 	 = 	( width 	? width 	: defaults['width'] 	);
	var height 	 = 	( height 	? height 	: defaults['height'] 	);

	widthCurrent = width;
	heightCurrent = height;
	
	outsideWidth = $(window).width() - width;
	outsideHeight = $(window).height() - height;
	
	var firstTick = true;
	removeNode(rootNode);
	$('#label--10').parent().remove();
	groupNodes();
	
	// construct a map of nodes by their y values
	nodeMap = null;
	nodeMap = getNodeMap();

	// sets the x value for each node
	setXValues();
	offsetY = 0 - (verticalSpace / 2);
	constructSvg();
	offsetX = 0;
	offsetY = 0;
	GraphUtil.force.on("tick", tick);
	setLabelOffsets(nodeMap);
	offsetX = (width / 2) - assets[0].qx;
	offsetY = 0 - (verticalSpace / 2);
	background.remove();
	GraphUtil.force.alpha(1);
	
	// The graph is loaded, so the "regenerate graph" button can be enabled again
	$('#graphSubmitButtonId').removeAttr('disabled');
	
	// sets the property for the asset used as the source
	assets[0].sourceAsset = true;
	GraphUtil.updateAllClasses();
	
	// constructs the svg DOM and defines the event listeners for them
	function constructSvg () {

		zoomBehavior = d3.behavior.zoom().on("zoom", zooming);

		canvas.call(zoomBehavior);
		
		offsetGroup = canvas
			.append('svg:g')
				.on("dblclick.zoom", null)
				.style('width', 'auto')
				.style('height', 'auto')
				.attr('id', 'graphPanel');
		
		vis = offsetGroup.append('svg:g');

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
		
		var dragging = false;
		var clicked = false;
		
		function dragstart (d, i) {
				d3.event.sourceEvent.preventDefault();
				d3.event.sourceEvent.stopPropagation();
			if (d3.event.sourceEvent.button == 2) {
				return;
			}
			closeMenu();
			dragging = true;
			clicked = true;
		}

		function dragmove (d, i) {
			d3.event.sourceEvent.preventDefault();
			d3.event.sourceEvent.stopPropagation();
			if (d3.event.sourceEvent.button == 2) {
				return;
			}
			if (d3.event.dx == 0 && clicked) {
				return;
			}
			
			clicked = false;
			
			handleDrag(d);
			function handleDrag (node) {
				var nodeHere = getNodeByXY(Math.round(node.x), Math.round(Math.round(node.y) / verticalSpace));
				
				if (nodeHere != node)
					swapNodes(node, nodeHere);
				
				node.x += d3.event.dx;
				node.y = node.qy;
				node.px = node.x;
				node.py = node.qy;
				node.cx = node.x;
				node.cy = node.qy;
				
				node.fix = true;
				node.fixed = true;
			}
			setLabelOffsets(nodeMap);
			GraphUtil.force.alpha(Math.max(GraphUtil.force.alpha(), 0.2));
		}
		
		function dragend (d, i) {
			d3.event.sourceEvent.preventDefault();
			d3.event.sourceEvent.stopPropagation();
			if (d3.event.sourceEvent.button == 2) {
				return;
			}
			d.fix = false;
			d.fixed = false;
			dragging = false;
			if (clicked)
				toggleNodeSelection(d);
			GraphUtil.force.alpha(1);
		}
		
		// Rescales the contents of the svg. Called when the user scrolls.
		function zooming () {
			if (!dragging) {
				vis.attr('transform','translate(' + d3.event.translate + ')' + ' scale(' + d3.event.scale + ')');
			}
		}
		
		// Resets the scale and position of the map. Called when the user double clicks on the background
		function resetView () {
			if (d3.event && (!d3.event.srcElement || d3.event.srcElement.nodeName != 'use')) {
				centerGraph()
				zoomBehavior.scale(1)
				if (d3.event.translate && d3.event.scale)
					vis.attr('transform','translate(' + d3.event.translate + ')' + ' scale(' + d3.event.scale + ')')
			} else if (!d3.event) {
				centerGraph()
				zoomBehavior.scale(1)
			}
		}
		
		// Handle the panning when the user starts dragging the canvas
		function mousedown() {
			closeMenu();
			return;
		}
		
		// Create the SVG element
		canvas
			.attr("width", width)
			.attr("height", height)
			.style('background-color', backgroundColor)
			.style('cursor', 'default !important')
			.on("dblclick", resetView)
			.on("dblclick.zoom", null);

		// Create the force layout
		GraphUtil.force
			.gravity(0)
			.linkDistance(0)
			.linkStrength(0)
			.friction(0)
			.charge(false)
			.size([width, height])
			.theta(0)
			.start();

		// Add the links the the SVG
		GraphUtil.linkBindings = vis.selectAll("line.link")
			.data(links).enter()
			.append("svg:line")
				.attr("width", function(d) { return '1px' })
				.attr("class", function(d) { return 'link' })
				.attr("id", function(d) { return 'link-'+d.parent.id+'-'+d.child.id })
				.attr("x1", function(d) { return d.child.x;})
				.attr("y1", function(d) { return d.child.y;})
				.attr("x2", function(d) { return d.parent.x;})
				.attr("y2", function(d) { return d.parent.y;});
		
		// Add the nodes to the SVG
		GraphUtil.nodeBindings = vis.selectAll("use")
			.data(assets).enter();
		
		// Create the nodes
		GraphUtil.nodeBindings = GraphUtil.nodeBindings
			.append("svg:use")
				.attr("xlink:href", function (d) {
					return '#' + appSVGShapes.shape[assetTypes[d.type].internalName].id;
				})
				.attr("class", function (d) { 
					return "node " + d.dir
						+ (d.root?' root':'');
				})
				.call(dragBehavior)
				.on("dblclick", function(d) {
					$('#assetIdId').val(d.id);
					EntityCrud.showAssetDetailView(d.assetClass, d.id);
				})
				.on("mousedown", mousedown)
				.attr("fix", function(d) {
					d.fix = false;
					return false;
				})
				.attr("id", function(d) {
					return 'node-' + d.id;
				})
				.style('cursor', 'default !important');
		
		
		GraphUtil.nodeBindings.append("title").text(function(d){ return d.title });
		
		// Create the node labels
		GraphUtil.labelBindings = vis.selectAll("g.node")
			.data(assets).enter()
			.append("svg:g")
			.attr("class", "label");
		
		if (backgroundColor == '#ffffff')
			GraphUtil.labelBindings.attr("class", "node nodeLabel blackText");
		else
			GraphUtil.labelBindings.attr("class", "node nodeLabel");
		
		GraphUtil.labelTextBindings = GraphUtil.labelBindings.append("svg:text").attr("style", "font: 11px Tahoma, Arial, san-serif;")
			.attr("id", function (d) {
				return "label2-" + d.id;
			})
			.attr("class", "ignoresMouse labelBackground")
			.attr("text-anchor", "middle")
			.attr("dx", 0)
			.attr("dy","-1.45em")
			.text(function(d) {
				return d.name;
			});
		
		GraphUtil.labelTextBindings = GraphUtil.labelBindings.append("svg:text").attr("style", "font: 11px Tahoma, Arial, san-serif;")
			.attr("id", function (d) {
				return "label-" + d.id;
			})
			.attr("class", "ignoresMouse")
			.attr("text-anchor", "middle")
			.attr("dx", 0)
			.attr("dy","-1.45em")
			.text(function(d) {
				return d.name;
			});
		
		GraphUtil.setShowLabels(assets);
		
		// add pointers to the bound elements
		GraphUtil.addBindingPointers();
	
		// Update the classes for all data bound svg objects
		GraphUtil.updateAllClasses();
		
		// set the horizontal and vertical spacing so that the whole map fits on the page
		var nx = nodeMap[1].size();
		var ny = Object.keys(nodeMap).size();
		horizontalSpace = Math.min(Math.max(Math.round(width / nx), minHorizontalSpace), maxHorizontalSpace);
		verticalSpace = Math.min(Math.max(Math.round(height / ny), minVerticalSpace), maxVerticalSpace);
		
		updateXYValues();
		offsetX = (width / 2) - assets[0].qx - horizontalSpace;
		updateXYValues();
		GraphUtil.force.start();

		// Trigger action when the contexmenu is about to be shown
		$(document).bind("contextmenu", function (event) {
			if (event.shiftKey)
				return;
			var validTags = ['use', 'svg', 'g'];
			var target = event.target;
			var tag = target.tagName;
			
			if (validTags.indexOf(tag) != -1) {
				event.preventDefault();
				
				// remove old items
				$(".customMenu").children(".tempItem").remove();
				
				// node specific items
				if (tag == 'use') {
					var id = event.target.id.substr(5);
					var data = target.__data__;
					if(environment == 'development'){
						var consoleOutputItem = '<li class="tempItem console" id="consoleOutputItemId">Output to console</li>';
						$(".customMenu").append(consoleOutputItem);	
					}
					
					$("#consoleOutputItemId").on('click', function (a, b) {
						closeMenu();
						console.log(data);
					});
					
					var showAssetItem = '<li class="tempItem show" id="showAssetItemId">Show Asset</li>';
					$(".customMenu").append(showAssetItem);
					$("#showAssetItemId").on('click', function (a, b) {
						closeMenu();
						EntityCrud.showAssetDetailView(data.assetClass, id);
					});
					
					//<tds:hasPermission permission="${Permission.AssetEdit}">
					var editAssetItem = '<li class="tempItem edit" id="editAssetItemId">Edit Asset</li>';
					$(".customMenu").append(editAssetItem);
					$("#editAssetItemId").on('click', function (a, b) {
						closeMenu();
						EntityCrud.showAssetEditView(data.assetClass, id);
					});
					//</tds:hasPermission>
					
					var showGraphItem = '<li class="tempItem graph" id="showGraphItemId">Show Graph</li>';
					$(".customMenu").append(showGraphItem);
					$("#showGraphItemId").on('click', function (a, b) {
						closeMenu();
						$('#assetIdId').val(id);
						$('#assetSelectId').val(id);
						generateGraph();
					});

				// svg/g specific items
				} else if (tag == 'svg' || tag == 'g') {
					var resetViewItem = '<li class="tempItem refresh" id="resetViewItemId">Reset View</li>';
					$(".customMenu").append(resetViewItem);
					$("#resetViewItemId").on('click', function () {
						closeMenu();
						resetView();
					});
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
			$(".customMenu")
				.css('display', "none")
				.children(".tempItem").remove();
		}
		
		// bind the increase spacing button
		$('#spacingIncreaseId').unbind('click');
		$('#spacingIncreaseId').on('click', function () {
			updateSpacing('add');
		});
		
		// bind the decrease spacing button
		$('#spacingDecreaseId').unbind('click');
		$('#spacingDecreaseId').on('click', function () {
			updateSpacing('sub');
		});
	}
	
	// Toggles selection of a node
	function toggleNodeSelection (node) {
		if (nodeSelected == null && node == null)
			return; // No node is selected, so there is nothing to deselect
		
		// check if we are selecting or deselecting
		if (nodeSelected == node) {
			// deselecting
			nodeSelected = -1;
			
			// The funtion is deselecting a node, so remove the selected class from all elements
			links.each(function (link, i) {
				link.selected = 0;
			});
			assets.each(function (node, i) {
				node.selected = 0;
			});
			
		} else {
			// selecting
			if (nodeSelected != -1)
				toggleNodeSelection(nodeSelected); // Another node is selected, so deselect that one first
			nodeSelected = node;
		
			// Style the selected node
			node.selected = 2;
			
			// Style the dependencies of the selected node
			var useTarget = true;
			function styleDependencies (index, link) {
				var childNode = (useTarget)?(link.child):(link.parent);
				link.selected = 1;
				childNode.selected = 1;
			}
			$.each(node.allChildren, styleDependencies);
			useTarget = false;
			$.each(node.allParents, styleDependencies);
		}
		
		GraphUtil.updateAllClasses();
		
		// Sort all the svg elements to reorder them in the DOM (SVG has no z-index property)
		var selection = d3.selectAll('svg.chart > g g g').filter(':not(.selected)').filter('.selected');
		selection[0] = selection[0].concat(d3.selectAll('svg.chart > g g line').filter(':not(.selected)')[0])
			.concat(d3.selectAll('svg.chart > g g use').filter(':not(.selected)')[0])
			.concat(d3.selectAll('svg.chart > g g g').filter(':not(.selected)')[0])
			.concat(d3.selectAll('svg.chart > g g line').filter('.selected')[0])
			.concat(d3.selectAll('svg.chart > g g use').filter('.selected')[0])
			.concat(d3.selectAll('svg.chart > g g g').filter('.selected')[0]);
		selection.order();
	}
	
	// gets a map representing the position of each node in the graph
	function getNodeMap () {
		
		var nodeMap = {}
		var callCount = 0;
		
		constructNodeMap(assets[0], nodeMap, 'up');
		nodeMap[assets[0].row].splice(nodeMap[assets[0].row].indexOf(assets[0]), 1);
		constructNodeMap(assets[0], nodeMap, 'down');
		
		function constructNodeMap (node, nodeMap, direction) {
			if (direction == 'up') {
				for (var i = 0; i < node.parents.size(); ++i)
					constructNodeMap(node.parents[i].parent, nodeMap, direction);
			} else if (direction == 'down') {
				for (var i = 0; i < node.children.size(); ++i)
					constructNodeMap(node.children[i].child, nodeMap, direction);
			}
			
			if (nodeMap[node.row] == null)
				nodeMap[node.row] = [];
			if (nodeMap[node.row].indexOf(node) == -1) {
				if (node.col == null) {
					node.col = nodeMap[node.row].size();
					nodeMap[node.row].push(node);
				} else {
					if (nodeMap[node.row][node.col] == null) {
						nodeMap[node.row][node.col] = node;
					} else {
						nodeMap[node.row].push(node);
					}
				}
					widestRow = Math.max(widestRow, nodeMap[node.row].size());
			}
		}
		return nodeMap;
	}
	
	// set x values for each node
	function setXValues () {
		
		// creates invisible dummy nodes in spaces that are not occupied by real nodes
		var keys = Object.keys(nodeMap);
		for (var i = keys.size() - 1; i > -1; --i) {
			for (var j = widestRow - 1; j > -1; --j) {
				if (nodeMap[keys[i]][j] == null) {
					nodeMap[keys[i]][j] = {'col':j, 'row':keys[i], 'qy':keys[i] * verticalSpace, 'dummy':true};
				}
			}
		}
		
		var count = 0;
		assets[0].fixed = true;
		updateXYValues();
		
		
		// center either the top or bottom tree, depending on which is less wide
		var rootRow = assets[0].row - 1;
		if (assets[0].topHeavy)
			rootRow = assets[0].row;
			
		if (nodeMap[rootRow] && nodeMap[rootRow].length > 0) {
			var totalHeight = 0;
			for (var i = 0; i < nodeMap[rootRow].length; ++i) {
				if (nodeMap[rootRow][i].height) {
					totalHeight += nodeMap[rootRow][i].height;
				}
			}
			var difference = Math.ceil(assets[0].col - totalHeight / 2);
			if (assets[0].topHeavy)
				difference = Math.ceil(widestRow / 2 - totalHeight / 2);
			if (difference > 0)
				for (var i = 0; i < Object.keys(nodeMap).size(); ++i) {
					var row = parseInt(Object.keys(nodeMap)[i]);
					if ( (assets[0].topHeavy && row >= rootRow) || (!assets[0].topHeavy && row <= rootRow) ) {
						for (var col = nodeMap[row].size() - 1; col >= Math.ceil(difference); --col) {
							var tmp = nodeMap[row][col];
							var otherNode = nodeMap[row][col - Math.ceil(difference)];
							nodeMap[row][col] = nodeMap[row][otherNode.col];
							nodeMap[row][otherNode.col] = tmp;
							otherNode.col += difference;
							tmp.col -= difference;
						}
					}
				}
		}
		
		for (var i = 0; i < assets.length; ++i) {
			assets[i].qx = assets[i].col * horizontalSpace;
			assets[i].cx = assets[i].col * horizontalSpace;
			assets[i].x = assets[i].col * horizontalSpace;
		}
		if (assets.length > 0)
			xOffset = (width / 2) - assets[0].qx;

		// recursively set the X values for all the nodes in a given direction from a given node
		function setX (node, direction) {
			++count;
			if (count > 10000) {
				node.yx = 0;
				return;
			}
			
			// we have already been here so don't try to calculate x again
			if (node.fixed)
				return node.col;
			
			var newX = node.col;
			var sum = 0;
			var high = null;
			var low = null;
			
			var mode = 0;
			if (direction == 'up' && node.parents.size() > 0)
				mode = 1;
			else if (direction == 'down' && node.children.size() > 0)
				mode = 2;
			else if (node.children.size() > 0)
				mode = 2;
				
			
			if (mode == 1) {
				for (var i = 0; i < node.parents.size(); ++i) {
					var xVal = setX(node.parents[i].parent, direction);
					if (high == null) {
						high = xVal;
						low = xVal;
					}
					high = Math.max(high, xVal);
					low = Math.min(low, xVal);
				}
			} else if (mode == 2) {
				for (var i = 0; i < node.children.size(); ++i) {
					var xVal = setX(node.children[i].child, direction);
					if (high == null) {
						high = xVal;
						low = xVal;
					}
					high = Math.max(high, xVal);
					low = Math.min(low, xVal);
				}
			}
						
			if (high != null)
				newX = low + (high - low) / 2;
			var newXInitial = newX;
			var other = getNodeByXY(newX * horizontalSpace, node.row);
			high = newX;
			low = newX;
			var searching = other && other.fixed;
			
			while (searching) {
				other = getNodeByXY(high * horizontalSpace, node.row);
				searching = other.fixed;
				if (searching)
					other = getNodeByXY(low * horizontalSpace, node.row);
				searching = searching && other.fixed;
					
				high = Math.min(high+1, nodeMap[assets[0].row].size()-1);
				low = Math.max(low-1, 0);
			}
			newX = other.col;
			
			swapNodes(node, other);
			
			node.fixed = true;
			
			return newX;
		}
	}
	
	
	// increases or decreases the space between the nodes
	function updateSpacing (action) {
		var newVerticalSpace = verticalSpace + verticalSpaceMod;
		var newHorizontalSpace = horizontalSpace + horizontalSpaceMod;
		if (action == 'sub') {
			newVerticalSpace -= verticalSpaceMod * 2;
			newHorizontalSpace -= horizontalSpaceMod * 2;
		}
		newVerticalSpace = Math.max(Math.min(newVerticalSpace, maxVerticalSpace), minVerticalSpace);
		newHorizontalSpace = Math.max(Math.min(newHorizontalSpace, maxHorizontalSpace), minHorizontalSpace);
		verticalSpace = newVerticalSpace;
		horizontalSpace = newHorizontalSpace;
		updateXYValues();
		setLabelOffsets(nodeMap);
		if (assets.size() < 100) {
			GraphUtil.force.alpha(0.1);
		} else {
			GraphUtil.tickOnce();
		}
	}
	
	// calculate stacking values for @param root
	function calculateStacks (root) {
		
		var waiting = true;
		var stack = [];
		var minIndex = 1000;
		var maxIndex = 1000;
		var rectHeight = 20;
		var direction = root.direction;

		// calculate the widest row in the graph
		var maxWidth = 0;
		var r = [];
		for (var i = 0; i < assets.length; ++i) {
			if (r[assets[i].row] == null)
				r[assets[i].row] = 0;
			++(r[assets[i].row]);
		}		
		for (var i = 0; i < r.length; ++i)
			if (r[i] > maxWidth)
				maxWidth = r[i];
		
		// perform a breadth first traversal of the tree, inserting each node into the stack
		var queue = [root];
		while (queue.length > 0) {
			calculateStack(queue.pop());
		}
		
		// realigns all stack values to start from 0
		for (var i = 0; i < assets.length; ++i) {
			if (assets[i].direction == direction)
				assets[i].col -= minIndex;
		}
		
		return maxIndex - minIndex;
		
		// recursive function calculating the stack starting at a given node
		function calculateStack (node) {
			
			// check if this node has already been placed
			if (node == null || node.col != null)
				return false;
			
			// check if this node has any uncalculated parents
			if (hasUndefinedParents(node)) {
				
				// shift this node down in the queue below a node that has all defined parents
				var next = [queue.pop()];
				while (hasUndefinedParents(next[0]))
					next.unshift(queue.pop());
				queue.push(node);
				queue = queue.concat(next);
				return false;
			}
			
			// the ideal location for any given node is the average of its parents
			var ideal = minIndex + Math.round((maxIndex-minIndex) / 2);
			if (node.parents.length > 0)
				ideal = getIdealStackLocation(node, getParents(node));
			
			// add this node to the stack
			addNode(node, ideal);
			
			// recursively calculate the stack for this node's children
			var childrenToCheck = getChildren(node);
			for (var i = 0; i < childrenToCheck.length; ++i) {
				queue.unshift(childrenToCheck[i]);
			}
			
			return true;
		}
		
		// inserts @param node as close to @param row as possible
		function addNode (node, ideal) {
			
			// first check if the ideal location is availible
			if ( ! stack[ideal] || canFitInCol(node, stack, ideal) ) {
				insertIntoStack(node, stack, ideal);
				
			// if not, move outwards from that location, until an empty location or a location worth switching for
			} else {
				var offsetLow = Math.floor((Math.max(node.height, 1) - 1) / 2);
				var offsetHigh = Math.ceil((Math.max(node.height, 1) - 1) / 2);
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
					if (canFitInCol(node, stack, high)) {
						insertIntoStack(node, stack, high);
					} else if (canFitInCol(node, stack, low)) {
						insertIntoStack(node, stack, low);
					} else {
						inserted = false;
					}
					++high;
					//--low;
				}
				maxIndex = Math.max(maxIndex, node.col + offsetHigh);
				minIndex = Math.min(minIndex, node.col - offsetLow);
			}
		}
		
		// check if a given node has parents that have not yet been added to the stack
		function hasUndefinedParents (node) {
			for (var i = 0; i < node.parents.length; ++i) {
				if (node.parents[i].parent.col == null)
					return true;
			}
			return false;
		}
		
		// inserts node into the stack as close to the given col as possible
		function insertIntoStack (node, stack, col) {
			var low = Math.floor((Math.max(1, node.height)-1)/2);
			var high = Math.ceil((Math.max(1, node.height)-1)/2);
			maxIndex = Math.max(maxIndex, col + high);
			minIndex = Math.min(minIndex, col - low);
			
			for (var r = 0-low; r <= high; ++r) {
				if (stack[col+r] == null)
					stack[col+r] = [];
				stack[col+r][node.row] = node;
			}
			node.col = col;
		}
		
		// gets the best stack location for node (object) based on related nodes.
		function getIdealStackLocation (node, related) {
			
			if (node.exParentX && node.exParentX.col)
				return node.exParentX.col;
			
			// if only a single parent is involved, fill its height
			if (related.size() == 1) {
				var siblings = [];
				for (var i = 0; i < getChildren(related[0]).size(); ++i)
					if (getParents(getChildren(related[0])[i]).size() == 1)
						siblings.push(getChildren(related[0])[i]);
				var offsetFromParent = Math.floor((Math.max(1, related[0].height)-1)/2);
				var offsetFromSiblings = 0;
				var offsetFromHeight = Math.floor((Math.max(1, node.height)-1)/2);
				if (siblings.indexOf(node) != -1) {
					for (var i = 0; i < siblings.indexOf(node); i++)
						offsetFromSiblings += Math.max(1, siblings[i].height);
					return related[0].col - offsetFromParent + offsetFromSiblings + offsetFromHeight;
				}
			}
			
			// if any parents have this node as their only child, only consider those parents
			var newList = [];
			for (var i = 0; i < related.size(); ++i)
				if (related[i].children.size() == 1)
					newList.push(related[i]);
			if (newList.size() > 0)
				related = newList;
			
			var sum = 0;
			var count = 0;
			
			for (var i = 0; i < related.length; ++i) {
				if (related[i].col) {
					for (var n = 0; n < Math.max(1, related[i].height); ++n) {
						++count;
						sum += related[i].col;
					}
				}
			}
			
			// check if the ideal location is directly between 2 values
			if ((sum/count) - Math.floor(sum/count) == 0.5) {
				if ((node.height % 2) == 0)
					return Math.floor(sum/count);
				return Math.ceil(sum/count);
			}
			if (sum == 0)
				return 0;
			if (Math.abs((sum/count) - Math.round(sum / count)) == 0.5) {
			
			}
				
			return Math.round(sum / count);
		}
		
		// checks if a node can fit in a col in the stack
		function canFitInCol (node, stack, col) {
			var low = Math.floor((Math.max(1, node.height)-1)/2);
			var high = Math.ceil((Math.max(1, node.height)-1)/2);
			var intersecting = false;
			var underParents = [];
			var reasons = [];
			
			// check each column
			for (var c = 0-low; c <= high; ++c) {
				if (stack[col+c] != null)
					// check each row
					for (var r = 0; r < stack[col+c].length; ++r) {
						if (stack[col+c][r]) {
							var a = (r <= node.row) && (stack[col+c][r].exEnd >= node.row)
							var b = (r >= node.row) && (r <= node.exEnd)
							
							if ((a || b) && (stack[col+c][r].exSet.indexOf(node) == -1)) {
								intersecting = true;
								if (reasons.indexOf(stack[col+c][r]) == -1)
									reasons.push(stack[col+c][r]);
								if ((getFullChildSet(stack[col+c][r]).indexOf(node) != -1) && (underParents.indexOf(stack[col+c][r]) == -1)) {
									underParents.push(stack[col+c][r]);
								}
							}
						}
					}
				else
					stack[col+c] = [];
			}
			
			// if we are only intersecting our parents it is okay
			var usedByParents = underParents.length == reasons.length;
			if (intersecting && !usedByParents) {
				return false;
			}
			return true;
		}
		
		// outputs the stack to the console for debugging
		function outputStack (stack, useStack) {
			console.log('OUTPUTTING STACK:');
			var grid = [];
			for (var i = 0; i < stack.length; ++i) {
				if (stack[i] != null) {
					for (var j = 0; j < stack[i].length; ++j) {
						if (useStack) {
							if (j > 0) {
								if (grid[j] == null)
									grid[j] = [];
								if (stack[i][j] != null) {
									if (stack[i][j].col == i)
										grid[j][i-minIndex] = '@';
									else
										grid[j][i-minIndex] = '#';
									grid[j][i-minIndex] = stack[i][j].name + ' ';
								} else {
									grid[j][i-minIndex] = ',';
								}
							}
						} else if (stack[i][j] != null) {
							if (grid[stack[i][j].row] == null)
								grid[stack[i][j].row] = [];
							grid[stack[i][j].row][stack[i][j].col - minIndex] = '#';
						}
					}				
				}
			}
			for (var i = 0; i < grid.length; ++i) {
				var str = '';
				if (grid[i])
					for (var j = 0; j < maxWidth + 1; ++j) {
						if (grid[i][j])
							str += grid[i][j];
						else
							str += '.';
					}
				console.log(str + ' ' + i);
			}
		}
	}
	
	// updates the x and y values for every node if their row/col have changed
	function updateXYValues () {
		for (var i = 0; i < assets.size(); ++i) {
			assets[i].qx = assets[i].col * horizontalSpace;
			assets[i].qy = assets[i].row * verticalSpace;
		}
		if (nodeMap && widestRow) {
			var keys = Object.keys(nodeMap);
			for (var i = keys.size() - 1; i > -1; --i) {
				for (var j = widestRow - 1; j > -1; --j) {
					var node = nodeMap[keys[i]][j];
					if (node != null) {
						node.qx = node.col * horizontalSpace + (0.25 * horizontalSpace / nodeOffsetIncrements) * (i % nodeOffsetIncrements);
						node.qy = node.row * verticalSpace;
					}
				}
			}
		}
	}
		
	// offset all nodes to the right so that the graph is centered
	function offsetNodes () {
		assets.each(function (o, i) {
			o.qx += (width / 2) - (widestRow * horizontalSpace / 2);
			o.cx += (width / 2) - (widestRow * horizontalSpace / 2);
			o.x += (width / 2) - (widestRow * horizontalSpace / 2);
		});
	}
	
	// Tick function called at every "tick" of the d3 simulation
	function tick(e) {

		// move the nodes towards their intended positions
		var k = e.alpha
		var movementCutoff = 0.2;
		var movement = false;

		assets.forEach(function(o, i) {
			if (!o.fix) {
				if (Math.abs(o.qx - o.x) > movementCutoff || Math.abs(o.qy - o.y) > movementCutoff)
					movement = true;
				o.x += (o.qx - o.x) * k;
				o.y += (o.qy - o.y) * k;
			}
			if (isNaN(o.x))
				o.x = 0;
			if (isNaN(o.qx))
				o.x = 0;
			if (isNaN(k))
				o.x = 0;
		});
		
		// set the positional attributes for the nodes
		GraphUtil.nodeBindings
			.attr("py", function(d) {
				d.py = d.qy;
				return d.qy;
			})
			.attr("cx", function(d) {
				return d.x;
			})
			.attr("cy", function(d) {
				d.cy = d.qy;
				return d.qy;
			})
			.attr("transform", function(d) {			
				if (isNaN(d.x))
					d.x = 0;
				return "translate(" + (d.x) + "," + (d.y) + ")";
			})
			.style("fill", function(d) {
				return (d.fillColor) ? (d.fillColor) : (d3.select(this).attr('fillColor'));
			});

		// set the dynamic attributes for the links
		$(GraphUtil.linkBindings[0]).each(function (i, o) {
			var d = o.__data__;

			var targetEdge = GraphUtil.targetEdge(d.parent, d.child);
			if(d.notApplicable && d.notApplicable == true) {
				targetEdge.x = d.child.x;
				targetEdge.y = d.child.y;
			}

			o.x1.baseVal.value = d.parent.x;
			o.y1.baseVal.value = d.parent.y;
			o.x2.baseVal.value = targetEdge.x;
			o.y2.baseVal.value = targetEdge.y;

		});
		
		// set the positional attributes for the labels
		
		GraphUtil.labelBindings
			.attr("cx", function(d) {
				return d.x;
			})
			.attr("cy", function(d) {
				return d.y;
			})
			.attr("transform", function(d) {return "translate(" + (d.x) + "," + (d.y) + ")";});
		
		// Update the classes for all data bound svg objects
		GraphUtil.updateAllClasses();
		
		setLabelOffsets(nodeMap);
		
		// if all the nodes have settled then stop ticking
		if (!movement) {
			// Only after tick has finished and is the first time
			if(onInitialGraphExecution)  {
				centerGraph();
				onInitialGraphExecution = false;
			}
			GraphUtil.force.alpha(0);
		}
	}

	/**
	 * It always center the Graph in the center of the page
	 * it takes the zoom behavior that was created in the init
	 */
	function centerGraph() {
		// First let-s calculate the last Graph Size
		var horizontalSpace = $("#graphSvgId").width() / 2;
		var verticalSpace = $("#graphSvgId").height() / 2;

		// Then if everything goes fine, the panel that old the graph itself has the last size
		if ($("#graphPanel")[0]) {
			var horizontalGraphSpace = ($("#graphPanel")[0].getBBox().width / 2) / zoomBehavior.scale()
			var verticalGraphSpace = ($("#graphPanel")[0].getBBox().height / 2) / zoomBehavior.scale()

			var centerX = (horizontalSpace - horizontalGraphSpace) + graphPadding
			var centerY = (verticalSpace - verticalGraphSpace)

			zoomBehavior.translate([centerX, -centerY])
			vis.attr('transform','translate(' + [centerX, -centerY] + ')' + ' scale(' + 1 + ')')
		}
	}
	
	// Gets the node at svg position x in the specified row
	function getNodeByXY (x, row) {
		var index = Math.round(x/horizontalSpace);
		index = Math.min(index, nodeMap[row].size()-1);
		index = Math.max(index, 0);
		var node = nodeMap[row][index];
		return node;
	}
	
	// Swaps the position of nodes a and b
	function swapNodes (a, b) {
		
		// you can't swap a node with itself
		if (a == b)
			return;
		
		// swapped nodes must be in the same row
		if (a.qy != b.qy)
			return;
		
		// if the nodes are more than one column apart, swap every node between them
		if (Math.abs(a.col - b.col) > 1) {
			var highIndex = Math.max(a.col, b.col);
			var lowIndex = Math.min(a.col, b.col);
			var direction = a.col - b.col;
			if (direction > 0)
				for (var i = a.col; i > b.col; --i) {
					swapNodes(nodeMap[a.row][i], nodeMap[a.row][i - 1]);
				}
			else
				for (var i = a.col; i < b.col; ++i) {
					swapNodes(nodeMap[a.row][i], nodeMap[a.row][i + 1]);
				}
		
		// otherwise just swap them
		} else {
			nodeMap[a.row][a.col] = b;
			nodeMap[b.row][b.col] = a;
			var tmp = b.col;
			b.col = a.col;
			a.col = tmp;
		}
		
		// update the x and y positions to reflect the change from the swap
		updateXYValues();
	}
	
	/*
	************************************************************************************************************************
	************************************************************************************************************************
	NODE GROUPING ALGORITHM 
	************************************************************************************************************************
	************************************************************************************************************************
	*/
	
	
	// calculate exEnds and exclusive end nodes
	function calculateExEnds () {
		var set = [];
		for (var i = 0; i < assets.length; ++i) {
			assets[i].exEnd = getExEnd(assets[i]);
			set.push(assets[i]);
			var exSet = [];
			getExclusiveSet(assets[i], exSet);
			assets[i].exSet = exSet;
			
			var nonExclusiveSet = [];
			for (var j = 0; j < exSet.size(); ++j)
				for (var k = 0; k < getChildren(exSet[j]).size(); ++k)
					if ( (exSet.indexOf(getChildren(exSet[j])[k]) == -1) && (nonExclusiveSet.indexOf(getChildren(exSet[j])[k]) == -1) )
						nonExclusiveSet.push(getChildren(exSet[j])[k]);
			if (nonExclusiveSet.size() == 1)
				assets[i].endOfExclusive = nonExclusiveSet[0];
		}
		exclusiveHeight(assets[0], set);
	}
	
	// sorts the order of children for every node and calculates the row/col for each node
	function groupNodes () {

		swapDirections();
		
		calculateExEnds();
		assets[0].direction = 'down';
		assets[0].oldParents = assets[0].parents;
		assets[0].parents = [];
		
		optimizeChildOrder('down');
		calculateStacks(assets[0]);
		assets[0].parents = assets[0].oldParents;
		var heightDown = assets[0].height;
		
		// swap the directions so the same algorithm can be used for both sides
		swapDirections();
		
		assets[0].col = null;
		assets[0].height = 0;
		calculateExEnds();
		assets[0].direction = 'up';
		assets[0].oldParents = assets[0].parents;
		assets[0].parents = [];
		optimizeChildOrder('up');
		
		calculateStacks(assets[0]);
		
		assets[0].parents = assets[0].oldParents;
		var heightUp = assets[0].height;
		assets[0].topHeavy = false;
		if (heightDown > heightUp)
			assets[0].topHeavy = true;
		
		// flip all parent/child relationships
		function swapDirections () {
			
			for (var i = 0; i < assets.size(); ++i) {
				var node = assets[i];
				var tmp = node.parents;
				node.parents = node.children;
				node.children = tmp;
				tmp = node.allParents;
				node.allParents = node.allChildren;
				node.allChildren = tmp;
			}
			
			for (var i = 0; i < links.size(); ++i) {
				var link = links[i];
				if (!link.cyclical) {
					var tmp = link.parent;
					link.parent = link.child;
					link.child = tmp;
					tmp = link.parentId;
					link.parentId = link.childId;
					link.childId = tmp;
				}
			}
		}
		
	}
	
	// Sorts the order of every node (in a given direction)'s successors in a way that will make the layout look nice
	function optimizeChildOrder (direction) {
		
		// set to true to display debugging info in the console
		var optimizeChildOrderLogging = false;
		
		// gets and outputs all the sibling groups
		var groupMatrices = {};
		for (var i = 0; i < assets.length; ++i) {
			if (assets[i].direction == direction && assets[i].parents.size() > 1) {
				if (optimizeChildOrderLogging)
					console.log(' **************************************** ' + assets[i].name + ' **************************************** ');
				if (optimizeChildOrderLogging)
					outputChildMatrix(assets[i]);
				
				var parentTask = null;
				
				// get the base group
				var group = getParents(assets[i]);
				
				// move each sibling up until it reaches a node with multiple children
				for (var j = 0; j < group.length; ++j) {
					while (group[j].parents.size() == 1 && getChildren(getParents(group[j])[0]).size() == 1)
						group.splice(j, 1, getParents(group[j])[0]);
				}
				
				siblingGroups.push(group);
				
				// make a new group that will be reduced further
				var group2 = [];
				for (var j = 0; j < group.length; ++j)
					group2.push(group[j]);
				
				// reduce group 2
				for (var j = 0; j < group2.length; ++j) {
					
					var searching  = true;
					while (group2[j].parents.size() > 0 && searching) {
						
						searching = false;
						
						var parent = getParents(group2[j])[0];
						parentTask = parent;
						var set = parent.exSet;
						for (var s = 0; s < group2.length; ++s) {
							if (set.indexOf(group2[s]) == -1) {
								searching = true;
							}
						}
						
						
						if (searching) {
							var parents = getParents(group2[j]);
							if (group2.indexOf(parents[0]) == -1) {
								group2.splice(j, 1, parents[0]);
							} else {
								group2.splice(j, 1);
								--j;
							}
								
							for (var k = 1; k < parents.length; ++k) {
								var adding = parents[k];
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
					var allSiblings = getChildren(parentTask);
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
					for (var k = 0; k < group2.size(); ++k) {
						if(groupMatrices[parentTask.id - firstIndex][group2[j].id - firstIndex] && groupMatrices[parentTask.id - firstIndex][group2[j].id - firstIndex][group2[k].id - firstIndex])
							++(groupMatrices[parentTask.id - firstIndex][group2[j].id - firstIndex][group2[k].id - firstIndex]);
					}
				
				// give each node a reference to this group
				for (var j = 0; j < group2.length; ++j) {
					group2[j].siblingGroupParents.push(assets[i]);
					group2[j].siblingGroups.push(group2);
				}
				
				siblingGroupsReduced.push(group2);
				assets[i].siblingGroupParents = [assets[i]];
				assets[i].siblingGroups.push(group2);
				if (optimizeChildOrderLogging)
					outputChildMatrix(assets[i]);
			}
		}
		
		// group successors by their children for each node
		for (var i = 0; i < assets.length; ++i) {
			var grouped = false;
			if (assets[i].direction == direction && assets[i].children.size() > 1) {
				if (optimizeChildOrderLogging) {
					console.log(' ---------------------------------------- ' + assets[i].name + ' ---------------------------------------- ');
					outputChildMatrix(assets[i]);
				}
			
				var oldSuccessors = assets[i].children;
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
				assets[i].children = newSuccessors;
				if (optimizeChildOrderLogging)
					outputChildMatrix(assets[i]);
				
			}
		}
		
		// sorts successors using a complex scoring system
		for (var i = 0; i < assets.length; ++i) {
			var grouped = false;
			if (assets[i].direction == direction && assets[i].children.size() > 1) {
				
				if (optimizeChildOrderLogging)
					console.log(' ######################################## ' + assets[i].name + ' ######################################## ');
				
				var initialList = [];
				var groups = assets[i].childGroups;
				for (var j = 0; j < assets[i].children.size(); ++j) {
					
					getChildren(assets[i])[j].groups = {};
					var s = getChildren(assets[i])[j];
					for (var k = 0; k < s.siblingGroups.size(); ++k) {
						s.groups[groups.indexOf(s.siblingGroups[k])] = true;
					}
					initialList.push(getChildren(assets[i])[j]);
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
					
						if (optimizeChildOrderLogging)
							display(newList, groups);
						
						// prevent infinite loops from occuring
						++calls;
						if (calls > 1000)
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
						
						// get the constraints for the next node
						var constraints = [];
						for (var j = 0; j < groups.length; ++j) {
							if (groupStatus[j] == '^') {
								constraints.push(-1 - j);
							}
							if (groupStatus[j] == 'v') {
								constraints.push(1 + j);
							}
						}
				
						// score the nodes
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
								
								
								if (bufferList[j].groups[Math.abs(constraint)] && constraints[k] > 0) {
									++pushConstraintsSatisfied;
									if (usable == 0) {
										fitsConstraints = true;
										usable = 1;
									}
								
								} else if (bufferList[j].groups[Math.abs(constraint)] && constraints[k] < 0) {
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
						
						// add this child to the new list from the buffer in a position based on the constraints it fits
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
								
								if (optimizeChildOrderLogging)
									console.log('push score = ' + score);
								if (score < maxScore) {
									maxScore = score;
									bestPlacement = j;
									pushing = true;
								}
								tempList.pop();
								
								tempList.unshift(row);
								score = evaluateStack(tempList, groups);
								if (optimizeChildOrderLogging)
									console.log('unshift score = ' + score);
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
					
					if (optimizeChildOrderLogging) {
						console.log('BEFORE READDING DUPLICATES FOR ' + assets[i].name)
						display(newList, groups);
					}
					
					// put back all the duplicates
					if (optimizeChildOrderLogging)
						console.log(initialList);
					for (var j = 0; j < initialList.length; ++j) {
						if (optimizeChildOrderLogging) {
							var str = "[";
							for (var k = 0; k < newList.length; ++k) {
								str += newList[k].id + ', ';
							}
							str += ']';
							console.log(str);
						}
						var node = initialList[j];
						var inserted = false;
						
						// check if this node is already in the list
						if (newList.indexOf(node) == -1) {
							
							// this node isn't already in the list so add it next to its duplicate
							var k = 0;
							if (j % 2 == 1)
								k = newList.length-1;
							while ((j % 2 == 0 && k < newList.length) || (j % 2 == 1 && k > -1)) {
								
								var matching = true;
								
								if (matching && newList[k].id != node.id) {
									
									if (Object.keys(node.groups).length == Object.keys(newList[k].groups).length && getMatchingGroups(node.groups, newList[k].groups) == Object.keys(node.groups).length) {
										if (optimizeChildOrderLogging) {
											console.log('matching[' + k + '] ' + node.name + ' to ' + newList[k].name);
											console.log(Object.keys(node.groups) + '');
											console.log(Object.keys(newList[k].groups) + '');
										}
										
										// insert the duplicate in one of the adjacent spots to the found node
										newList.splice(k, 0, node);
										inserted = true;
										break;
									}
								}
								
								if (j % 2 == 1)
									--k;
								else
									++k;
							}
							
							if (!inserted) {
								if (optimizeChildOrderLogging)
									console.log('DIDNT INSERT ' + node.name);
								newList.push(node);
							}
						}
					}
					
					// outputs a visual representation of a list of nodes and their sibling groups
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
					for (var j = 0; j < assets[i].children.size(); ++j) {
						var s = assets[i].children[j];
						var index = newList.indexOf(s.child);
						newSuccessors[index] = s;
					}
					if (optimizeChildOrderLogging) {
						console.log(newList);
						console.log('changing a to b');
						console.log(assets[i].children);
						console.log(newSuccessors);
					}
					var newChildren = [];
					$.each(newSuccessors, function (a, b) {
						if (optimizeChildOrderLogging) {
							console.log(a)
							console.log(b)
						}
						newChildren.push(b);
					});
					assets[i].children = newChildren;
				
				// sort without using groups
				} else {
					var newChildren = [];
					for (var j = 0; j < assets[i].children.size(); ++j) {
						newChildren.push(assets[i].children[j]);
					}
					if (optimizeChildOrderLogging)
						$(newChildren).each(function (a, b) {
							console.log('   > ' + b.child.name);
						});
					
					// just sort by height
					newChildren.sort(function (a, b) {
						if (optimizeChildOrderLogging)
							console.log(' > ' + b.child.height + ' - ' + a.child.height);
						return b.child.height - a.child.height;
					});
					
					// check if this node is in any sibling groups
					var siblings = assets[i].siblingGroups;
					if (siblings.size() > 0) {
						// find a sibling group to pair this node to
						var other = null;
						$(siblings).each(function (a, b) {
							if (b.size() == 2) {
								if (b.indexOf(assets[i]) == 0)
									other = b[1];
								else
									other = b[0];
							}
						});
						
						// group children by putting shared children near the paired node
						if (other != null) {
							var left = getLeftmostNodeInTree(assets[i], other);
							var otherSet = getFullChildSet(other);
							newChildren = [];
							$(assets[i].children).each(function (i, o) {
								var set = getFullChildSet(o.child);
								if (intersection(set, otherSet).size() > 0) {
									if (left == other) {
										newChildren.unshift(o);
									} else {
										newChildren.push(o);
									}
								} else {
									if (left == other) {
										newChildren.push(o);
									} else {
										newChildren.unshift(o);
									}
								}
							});
						}
					} else {
						
						// check if a child is shared with another node
						if (newChildren[0].child.height > assets[i].height && newChildren[0].child.parents.size() == 2) {
							var parents = getParents(newChildren[0].child);
							parents.splice(parents.indexOf(assets[i]), 1);
							var other = parents[0];
							var left = getLeftmostNodeInTree(assets[i], other);
							
							// sort by height
							newChildren.sort(function (a, b) {
								if (left == other)
									return a.child.height - b.child.height;
								return b.child.height - a.child.height;
							});
						}
					}
					
					if (optimizeChildOrderLogging)
						$(newChildren).each(function (a, b) {
							console.log('   > ' + b.child.name);
						});
					assets[i].children = newChildren;
				}
		
				if (optimizeChildOrderLogging) {
					outputChildMatrix(assets[i]);
					console.log(getChildren(assets[i]));
					console.log(assets[i].children);
				}
			}
		}
		
		// gets the number of sibling groups in both list1 and list2
		function getMatchingGroups (list1, list2) {
			var matches = 0;
			for (var i = 0; i < Object.keys(list1).length; ++i) {
				var key = Object.keys(list1)[i];
				if (list2[key] != null)
					++matches;
			}
			return matches;
		}
		
		
		
		// outputs a visual representation of node's children and their sibling groups
		function outputChildMatrix (node) {
			var successors = getChildren(node);
			console.log('child matrix for node [' + node.id + '] ' + node.name);
			console.log('----------------------');
			for (var i = 0; i < successors.size(); ++i) {
				var childNode = successors[i];
				var output = childNode.id + ' : ';
				for (var l = 0; l < siblingGroupsReduced.length; ++l) {
					if (siblingGroupsReduced[l].indexOf(childNode) != -1)
						output += '\t@';
					else
						output += '\t.';
				}
				console.log('\t > ' + output + ' "' + childNode.name + '"');
			}
			console.log('----------------------');
		}
	}
	
	/*	returns a or b depending on which is further to the left */
	function getLeftmostNodeInTree (a, b) {
		
		return searchTree(assets[0]);
		
		function searchTree (node) {
			if (node == a)
				return a;
			if (node == b)
				return b;
			for (var i = 0; i < getChildren(node).size(); ++i) {
				var returnVal = searchTree(getChildren(node)[i]);
				if (returnVal == a)
					return a;
				if (returnVal == b)
					return b;
			}
		}
	}
	
	// gets a list of all the nodes that can be reached by traversing down node's children
	function getFullChildSet (node) {
		if (node.fullChildSet)
			return node.fullChildSet;
		var set = [node];
		$(getChildren(node)).each(function (i, o) {
			set = set.concat(getFullChildSet(o));
		});
		node.fullChildSet = set;
		return set;
	}
	
	/*	fills @param set with all nodes that can only be 
		reached from @param node and its children.
		(This only works if successors are in chronological order)*/
	function getExclusiveSet (node, set) {
		set.push(node);
		getChildren(node).forEach(function (c) {
			
			// true if c doesn't have an exParent
			var isExclusive = true;
			
			if (c.exParent != null) {
				if (c.exParent != node) {
					isExclusive = false;
				} else {
					isExclusive = true;
				}
			} else {
				getParents(c).forEach(function (p) {
					if (set.indexOf(p) == -1)
						isExclusive = false;
				}); 
			}
			if (isExclusive)
				getExclusiveSet(c, set);
		});
	}
	
	// gets the exclusive end for @param node
	function getExEnd (node) {
		var set = [];
		getExclusiveSet(node, set);
		var end = set[0].row;
		for (var i = 0; i < set.size(); ++i) {
			if (end >= 0 && set[i].row >= 0)
				end = Math.max(end, set[i].row);
			else if (end <= 0 && set[i].row <= 0)
				end = Math.min(end, set[i].row);
		}
		return end;
	}
	
	/*	Calculates the exclusive heights of all nodes in @param set
		that are reachable from the root @param node.	*/
	function exclusiveHeight (node, set) {
		
		// if this node is not a leaf, check its successors
		if (intersection(getChildren(node), set).size() > 0) {
			
			// evaluate all nodes below this one
			$.each(intersection(getChildren(node), set), function (i, successor) {
				successor.height = exclusiveHeight(successor, set);
			});
			
			// evaluate this node's height
			$.each(intersection(getChildren(node), set), function (i, successor) {
				
				// if the successor is a leaf, add its height to this node
				if (intersection(getParents(successor), set).size() == 1) {
					node.height += Math.max(successor.height, 1);
				
					// remove this successor from the set
					set.splice(set.indexOf(successor), 1);
				}
			});
			$.each(intersection(getChildren(node), set), function (i, successor) {
				
				// if the successor has multiple parents, share its height between its parents
				// TODO: this method does not produce the best possible layouts, the way height is distributed should be changed
				if (intersection(getParents(successor), set).size() != 1) {
					
					var oldList = getParents(successor);
					var newList = [];
					for (var i = 0; i < oldList.size(); ++i)
						if (oldList[i].parents.size() == 1)
							newList.push(oldList[i]);
					if (newList.size() > 0)
						oldList = newList;
					if (newList.size() == 1) {
						++newList[0].height;
						newList[0].exChild = successor;
						successor.exParent = newList[0];
					} else if (successor.height == 1) {
						++oldList[0].height;
						oldList[0].exChild = successor;
						successor.exParentX = oldList[0];
					} else {
						for (var i = 0; i < successor.height; ++i) {
							var highest = oldList.sort(function (a, b) {return a.height-b.height})[0];
							++(highest.height);
						}
					}
				}
				
				// remove this successor from the set
				set.splice(set.indexOf(successor), 1);
			});
				
		// this successor's is a leaf so its height is automatically 1
		} else {
			node.height = Math.max(1, node.height);
		}
		
		return node.height;
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

// gets a list of nodes that are children to node
function getChildren (node) {
	if (!node) 
		return [];
	var nodes = [];
	for (var i = 0; i < node.children.length; ++i)
		nodes.push(node.children[i].child);
	return nodes;
}

// gets a list of nodes that are parents to node
function getParents (node) {
	if (!node) 
		return [];
	var nodes = [];
	for (var i = 0; i < node.parents.length; ++i)
		nodes.push(node.parents[i].parent);
	return nodes;
}

// retruns true if asset1 and asset2 have the same successors in a given direction
function haveSameSuccessors (asset1, asset2, direction) {
	if (direction == 'up') {
		var same = asset1.parents.size() == asset2.parents.size();
		if (same)
			for (var i = 0; i < asset1.parents.size(); ++i)
				same = same && (asset1.parents[i].parent.id == asset2.parents[i].parent.id)
		return same;
	} else if (direction == 'down') {
		var same = asset1.children.size() == asset2.children.size();
		if (same)
			for (var i = 0; i < asset1.children.size(); ++i)
				same = same && (asset1.children[i].child.id == asset2.children[i].child.id)
		return same;
	}
}

// gets a string representation of a node's parents
function getDepString (asset) {
	var depString = '';
	for (var i = 0; i < asset.parents.size(); ++i)
		depString += '{' + asset.parents[i].parent.name + '} ';
	return depString;
}

// scores the child order given in list using groups
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
					tempScore += list[j].duplicateSize;
//					++tempScore;
			}
		}
	}
	
	return score;
}

// removes children with identical sibling groups from list
function removeDuplicates (list, groups) {
	
	var uniqueRows = [];
	var returnList = [];
	
	for (var j = 0; j < list.length; ++j) {
		var node = list[j];
		var output = '';
		for (var k = 0; k < groups.length; ++k) {
			if (node.groups[k] != null)
				output += 'a';
			else
				output += 'b';
			
		}
		if (uniqueRows.indexOf(output) == -1) {
			node.duplicateSize = 1;
			uniqueRows.push(output);
			returnList.push(node);
		} else {
			returnList[uniqueRows.indexOf(output)].duplicateSize++;
		}
	}
	
	return returnList;
}

// Used to rebuild the layout using the new parameters
function rebuildMap (layoutChanged, width, height, labels) {
	
	widthCurrent 	= ( widthCurrent  ? widthCurrent  : defaults['width']  )
	heightCurrent 	= ( heightCurrent ? heightCurrent : defaults['height'] )
	
	var width	= ( width	? width  :	( widthCurrent	? widthCurrent	: defaults['width']	) )
	var height	= ( height 	? height :	( heightCurrent 	? heightCurrent	: defaults['height']	) )
	
	// Reevaluate which nodes should have their labels shown
	if (assets.size() > 0) {
		GraphUtil.setShowLabels(assets);
		setLabelOffsets(nodeMap);
		
		resizeGraph(width, height);
	}

	// handle the background color
	var blackBackground = GraphUtil.isBlackBackground();
	backgroundColor = blackBackground ? '#000000' : '#ffffff';
	canvas.style('background-color', backgroundColor);
}

function resizeGraph (width, height) {
	widthCurrent = width;
	heightCurrent = height;
	
	canvas
		.attr("width", width)
		.attr("height", height);
	
	var nx = widestRow;
	horizontalSpace = Math.min(Math.max(Math.round(width / nx), minHorizontalSpace), maxHorizontalSpace);
	if (assets.size() > 0)
		offsetX = (width / 2) - assets[0].qx - horizontalSpace / 2;
	
	GraphUtil.force
		.size([width, height])
		.start();
}

// Used by the defaults button to reset all control values to their default state
function resetToDefaults () {
	$('#labelTree input[type="text"]').each(function() {
		$(this).val( defaults[$(this).attr('name')] )
	});
	widthCurrent = defaults['width']
	heightCurrent = defaults['height']
	rebuildMap();
}

// Stops the map by setting the alpha value to 0
function stopMap () {
	GraphUtil.force.stop()
}

function getStandardWidth () {
	return $(window).width() - ($('div.body').offset().left * 2);
}

function getStandardHeight () {
	var bottomMargin = $('.main-footer').outerHeight()
	var graphOffset = $('#svgContainerId').offset().top
	var pageHeight = $(window).height()
	if (GraphUtil.isFullscreen())
		return pageHeight
	return pageHeight - graphOffset - bottomMargin - graphPadding
}

function getDependenciesString (node) {
	var sortedParents = getParents(node);
	sortedParents.sort(function (a, b) {
		if (a.id > b.id)
			return 1;
		if (a.id < b.id)
			return -1;
		return 0;
	});
	var sortedChildren = getChildren(node);
	sortedChildren.sort(function (a, b) {
		if (a.id > b.id)
			return 1;
		if (a.id < b.id)
			return -1;
		return 0;
	});
	var output = 'p:';
	sortedParents.each(function (o) {
		output += o.id + ' ';
	});
	output += 'c:';
	sortedChildren.each(function (o) {
		output += o.id + ' ';
	});
	return output;
}

// returns the node for the asset with the given id
function getNodeById (id) {
	return assets.find(function (a) {
		return a.id == id;
	});
}

// outputs a visual representation of a list of nodes and their sibling groups
function display (list, groups) {
	console.log('--------------------------------------------');
	for (var j = 0; j < list.length; ++j) {
		var output = list[j].id + ': ';
		for (var k = 0; k < groups.length; ++k) {
			if (groups[k].indexOf(list[j]) != -1)
				output += '\t@';
			else
				output += '\t.';
		}
		output += '\t ' + list[j].name;
		console.log('\t > ' + output);
	}
	console.log('--------------------------------------------');
}


// Sets the vertical offsets to make nearby labels more readable
function setLabelOffsets (nodeMap) {
	var maxOffset = 2;
	var labelPadding = 0.1;
	var labelRows = [];
	var keys = Object.keys(nodeMap);
	for (var i = 0; i < keys.size(); ++i) {
		var offset = maxOffset;
		labelRows = [];
		for (var j = 0; j < widestRow; ++j) {
			var node = nodeMap[keys[i]][j];
			
			if (node != null && node.showLabel && ! node.dummy) {
				// get the positional data of this node's label
				var text = _.unescape($('#label-' + node.id)[0].textContent);
				if(text.length > 0 ){

					var rect = validateRectBrowser(node, text.length);


					for (var k = text.length - 1; k >= 0; --k) {
						if (text[k] != ' ') {
							rect = validateRectBrowser(node, k);
							k = 0;
						}
					}

					var endX = rect.x + rect.width;
					var startX = node.qx - endX;
					endX = node.qx + endX;

					var row = 0;

					// maxOffset = the max val is 2, since one go up, one below
					// check if there is a row the label can fit in
					for (var k = 0; k < maxOffset; ++k) {
						if (labelRows[k] == null || labelRows[k] < startX) {
							row = k;
							k = maxOffset;
						}
					}

					var labelOffsetPosition = 4.5;
					if(node && node.allParents ){ // If the node do not have a parent, it means is the top node, set the label offset to up
						if(node.allParents.length <= 0) {
							labelOffsetPosition = 0;
						}
					}

					// insert the label into the row
					offset = (row + 1) * -1;
					if(row > 0){ // The label doesn't fit, add a more offSet to be below
						offset += 2;
					}
					nodeMap[keys[i]][j].labelOffset = (offset + offset * labelPadding) + labelOffsetPosition;
					labelRows[row] = endX;

				}
			}
		}
	}

	/**
	 *  Firefox and IE has some issues to render or access
	 *  a rectangle after this was already created,
	 *  one of the method affected is getExtentOfChar
 	 */

	function validateRectBrowser(node, w) {
		var rect;

		try {
			rect = $('#label-' + node.id)[0].getExtentOfChar(text.length - 1);
		} catch(err) { // if browser doesn't support the operation we fallback to manually create the rect
			rect = GraphUtil.createRect(node.x, node.y, GraphUtil.labelHeightDefault, w);
		}

		return rect;
	}

	// update all the labels y values
	GraphUtil.labelBindings.attr("dy", function(d) {
			if (d.labelOffset) {
				$(this).children().attr('dy', d.labelOffset - 0.8 + 'em');
			} else {
				$(this).children().attr('dy', 0 + 'em');
			}
			return 0;
		});
}

</script>
<ul class="customMenu"></ul>
</div>
