<div id="scriptDivId">
<style>
/* 	these styles must be included here due to a bug in firefox
	where marker styles don't work when used in external stylesheets. */
line.link {
	marker-end: url(#arrowhead);
}
line.link.selected {
	marker-end: url(#arrowheadSelected);
}
line.link.cut {
	marker-end: url(#arrowheadCut);
}
</style>
<script type="text/javascript">
// If there is already an instance of force running in memory, it should be stopped before creating this one
if (force != null) {
	console.log("Force is not null, force="+force)
	force.stop()
}
force = d3.layout.force();
var canvas = d3.select("div#item1")
	.append("div")
	.attr('id','svgContainerId')
	.append("svg:svg");

// define the shapes used for the svg
canvas.append("defs");
defineShapes(d3.select("defs"));

var outsideWidth = 0;
var outsideHeight = 0;
$(window).resize( function(a, b) {
	GraphUtil.resetGraphSize();
});

var maxCutAttempts = $('#maxCutAttemptsId').val();
if (isNaN(maxCutAttempts))
	maxCutAttempts = 200;
var maxEdgeCount = $('#maxEdgeCountId').val();
if (isNaN(maxEdgeCount))
	maxEdgeCount = 4;
$('#maxCutAttemptsId').on('change', function (e) {
	var newVal = parseInt(e.target.value);
	if (!isNaN(newVal))
		maxCutAttempts = newVal;
});
$('#maxEdgeCountId').on('change', function (e) {
	var newVal = parseInt(e.target.value);
	if (!isNaN(newVal))
		maxEdgeCount = newVal;
});

var zoomBehavior;
var vis = canvas;
var background;
var label;
var defaults = ${defaultsJson};
var selectedBundle = '${dependencyBundle}';
var assetTypes = ${assetTypes};
var links = ${links};
var nodes = ${nodes};
var moveBundles = ${moveBundleMap};

var cutLinks = [];
var cutNodes = [];
var graphstyle = "z-index:-1;";
var fill = d3.scale.category10();
var gravity = ${multiple ? 0.05 : 0};
var distanceIntervals = 500;
var floatMode = false;
var maxWeight;
var groupCount = 1;

var nodeSelected = -1;
var defaultColor = '#0000ff';
var selectedParentColor = '#00ff00';
var selectedChildColor = '#00cc99';
var selectedLinkColor = '#00dd00';
var backgroundColor = defaults.blackBackground ? '#000000' : '#ffffff';
if (defaults.blackBackground);
	$('marker#arrowhead').attr('fill', '#ffffff');

var widthCurrent;
var heightCurrent;
var nameList = getExpanededLabels();

var progressBarCancelDisplayed = false;
var cancelCut = false;

			
// Build the layout model
function buildMap (charge, linkSize, friction, theta, width, height) {
	
	console.log('building map with a height of ' + height + ' pixels');
	
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
	
	var zoom = d3.behavior.zoom()
		.on("zoom", zooming);
	
	canvas.call(zoom);
	
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
		startAlpha = force.alpha();
		dragging = true;		
		clicked = true;
		d3.event.sourceEvent.preventDefault();
		d3.event.sourceEvent.stopPropagation();
	}

	// fires when the user drags a node
	function dragmove (d, i) {
		if ((d3.event.dx != 0 || d3.event.dy != 0) || (!clicked)) {
			startAlpha = Math.min(startAlpha+0.005, 0.1);
			if (force.alpha() < startAlpha) {
				force.alpha(startAlpha);
			}
			
			d.x += d3.event.dx;
			d.y += d3.event.dy;
			d.px = d.x;
			d.py = d.y;
			
			d.fix = true;
			d.fixed = true;
			clicked = false;
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
	function resetView() {
		zoom.scale(1);
		zoom.translate([0,0]);
		scale = 1;
		translateX = 0;
		translateY = 0;
		zoomOffsetX = 0;
		zoomOffsetY = 0;
		vis.attr("transform", "translate(0)" + " scale(1)");
	}
	
	// Handle the panning when the user starts dragging the canvas
	function mousedown() {
		return;
	}
	
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
		.attr("class", 'draggable')
		.attr("style", graphstyle)
		.style('background-color', backgroundColor)
		.style('cursor', 'default')
		.on("dblclick", resetView)
		.on("dblclick.zoom", null);
	
	// Create the force layout
	force
		.nodes(nodes)
		.links(links)
		.gravity(gravity)
		.linkDistance(linkSize)
		.linkStrength(2)
		.friction(friction)
		.charge(charge)
		.size([width, height])
		.theta(theta)
		.start();
	
	// Add the links the the SVG
	var link = vis.selectAll("line.link")
		.data(links).enter()
		.append("svg:line")
			// TM-3722	
			.style("stroke", function(d) {return d.statusColor;})
			.style("stroke-opacity", function (d) { return d.opacity;})
			.attr("fillColor", function(d) { return d.statusColor; })
			.attr("width", function(d) { return '1px' })
			.attr("class", function(d) { return 'link'})
			.attr("id", function(d) { return 'link-'+d.source.index+'-'+d.target.index })
			.attr("x1", function(d) { return d.source.x;})
			.attr("y1", function(d) { return d.source.y;})
			.attr("x2", function(d) { return d.target.x;})
			.attr("y2", function(d) { return d.target.y;});
	
	// Add the nodes to the SVG
	var node = vis.selectAll("use")
		.data(nodes).enter()
	
	// Create the nodes
	node = node
		.append("use")
			.attr("xlink:href", function (d) {
				return '#' + assetTypes[d.type] + 'ShapeId';
			})
			.attr("class", "node")
			.call(dragBehavior)
			.on("dblclick", function(d) {
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
			.attr("fix", false)
			.attr("id", function(d) { return 'node-'+d.index })
			.style('cursor', 'default')
			.attr("fillColor", function(d) {
				return fill(d.group);
			})
			.style("fill", function(d) {
				return fill(d.group);
			})
			.style("stroke", function(d) {
				return d.color;
			})
			.style("stroke-width", '2px')
			.attr("cx", 0)
			.attr("cy", 0)
			.attr("transform", "translate(0, 0)");
			
	node.append("title").text(function(d){ return d.title });
	
	// Create the labels
	label = vis.selectAll("g.node")
		.data(nodes).enter()
		.append("svg:g")
		.attr("cx", 0)
		.attr("cy", 0)
		.attr("transform", "translate(0, 0)");
	
	if (backgroundColor == '#ffffff')
		label.attr("class", "node nodeLabel blackText")
	else
		label.attr("class", "node nodeLabel")
	
	label.append("svg:text").attr("style", "font: 11px Tahoma, Arial, san-serif;")
		.attr("id", function (d) {
			return "label-" + d.id;
		})
		.attr("class", "ignoresMouse")
		.attr("dx", 12)
		.attr("dy",".35em")
		.text(function(d) {
			return (nameList[assetTypes[d.type]])?(d.name):('');
		});
	
	
	// Load the move bundles into the legend
	GraphUtil.addMoveBundlesToLegend(moveBundles, fill);
	
	// Calculate the maximum weight value, which is used during the tick function
	var maxWeight = 1
	_(nodes).forEach(function(o) {
		maxWeight = Math.max( maxWeight, (o.weight?o.weight:0) );
		o.fix = false;
	})
	
	// set up the node families
	var nodeFamilies = GraphUtil.setNodeFamilies(nodes);
	var maxFamilyWeights = [];
	nodeFamilies.each(function (family, i) {
		var maxWeight = 1;
		family.each(function (node, i) {
			maxWeight = Math.max(maxWeight, node.weight);
		});
		maxFamilyWeights[i] = maxWeight;
	});
	if (nodeFamilies.size() < 5)
		gravity = 0;
	force.gravity(gravity);
	
	// run some initial ticks before displaying the graph
	force.on("tick", simpleTick);
	if (nodes.size() < 500)
		calmTick(50);
	else
		calmTick(20);
	force.on("tick", tick);
	force.tick();
	
	// Move the background to the correct place in the DOM
	background.remove();
	
	// Toggles selection of a node
	function toggleNodeSelection (id) {
		if (nodeSelected == -1 && id == -1)
			return; // No node is selected, so there is nothing to deselect
		
		var node = force.nodes()[id];
		var selecting = true;
		
		if (nodeSelected == id) {
			// deselecting
			selecting = false;
			nodeSelected = -1;
		} else {
			// selecting
			if (nodeSelected != -1)
				toggleNodeSelection(nodeSelected); // Another node is selected, so deselect that one first
			nodeSelected = id;
		}
		
		// Style the selected node
		var classes = d3.selectAll('svg g g use').filter('#node-'+node.index)[0][0].classList;
		classes.add('selected');
		classes.add('selectedParent');
		$('svg g g g text').filter('#label-'+node.id).parent()[0].classList.add('selected');
		
		// Style the dependencies of the selected node
		var useTarget = true;
		$.each(node.dependsOn, styleDependencies);
		useTarget = false;
		$.each(node.supports, styleDependencies);
		
		function styleDependencies (index, linkIndex) {
			var link = force.links()[linkIndex];
			var childNode = (useTarget)?(link.target):(link.source);
			var classes = d3.selectAll('svg g g use').filter('#node-'+childNode.index)[0][0].classList;
			classes.add('selected');
			classes.add('selectedChild');
			$('svg g g g text').filter('#label-'+childNode.id).parent()[0].classList.add('selected');
			d3.selectAll('svg g g line').filter('#link-'+link.source.index+'-'+link.target.index)[0][0].classList.add('selected');
		}
		
		// Sort all the svg elements to reorder them in the DOM (SVG has no z-index property)
		if (selecting) {
			var selection = d3.selectAll('svg g g g').filter(':not(.selected)').filter('.selected');
			selection[0] = selection[0].concat(d3.selectAll('svg g g line').filter(':not(.selected)')[0])
				.concat(d3.selectAll('svg g g use').filter(':not(.selected)')[0])
				.concat(d3.selectAll('svg g g line').filter('.selected')[0])
				.concat(d3.selectAll('svg g g use').filter('.selected')[0])
				.concat(d3.selectAll('svg g g g').filter(':not(.selected)')[0])
				.concat(d3.selectAll('svg g g g').filter('.selected')[0]);
			selection.order();
		} else { // The funtion is deselected a node, so remove the selected class from all elements
			d3.selectAll('svg g g line').filter('.selected')[0].each(function(o){
				o.classList.remove('selected');
			})
			d3.selectAll('svg g g use').filter('.selected')[0].each(function(o){
				o.classList.remove('selected');
				o.classList.remove('selectedParent');
				o.classList.remove('selectedChild');
			})
			d3.selectAll('svg g g g').filter('.selected')[0].each(function(o){
				o.classList.remove('selected');
			})
		}
		
		// run a tick to update the graph
		force.tick();
	}
	
	// Tick function without svg manipulation
	function simpleTick (e) {
		if ( gravity == 0 && ! floatMode ) {
			var k = e.alpha;
			$(nodes).each(function(i, o) {
				k = e.alpha;
				if (! o.fix) {
					if ( maxFamilyWeights[o.family] > 1 && o.weight < maxFamilyWeights[o.family] )
						k = 0;
					o.y += (heightCurrent / 2 - o.y) * k;
					o.x += (widthCurrent / 2 - o.x) * k;
				}
			});
		}
	}
	
	// Updates the dynamic attributes of the svg elements every tick of the simulation
	function tick (e) {
		// move each node towards the center
		if ( gravity == 0 && ! floatMode ) {
			var k = e.alpha;
			$(nodes).each(function(i, o) {
				k = e.alpha;
				if (! o.fix) {
					if ( maxFamilyWeights[o.family] > 1 && o.weight < maxFamilyWeights[o.family] )
						k = 0;
					o.y += (heightCurrent / 2 - o.y) * k;
					o.x += (widthCurrent / 2 - o.x) * k;
				}
			});
		}
		var d = null;
		// set the dynamic attributes for the nodes
		$(node[0]).each(function (i, o) {
			d = o.__data__;
			o.transform.baseVal.getItem(0).setTranslate(d.x, d.y);
			o.cx = o.x;
			o.cy = o.y;
			if (d.cut == 2) {
				d.cut = 3;
				o.fillColor = fill(d.group);
				o.style.fill = fill(d.group);
			}
		});
		
		// set the dynamic attributes for the links
		$(link[0]).each(function (i, o) {
			d = o.__data__;
			o.x1.baseVal.value = d.source.x;
			o.y1.baseVal.value = d.source.y;
			o.x2.baseVal.value = d.target.x;
			o.y2.baseVal.value = d.target.y;
			if (d.cut == 2) {
				d.cut = 3;
				o.classList.add('cut');
			} else if (d.cut != 3) {
				o.style.stroke = d.statusColor;
			}
		});
		
		// set the dynamic attributes for the labels
		$(label[0]).each(function (i, o) {
			d = o.__data__;
			o.transform.baseVal.getItem(0).setTranslate(d.x, d.y)
		});
	}
}


// Used to rebuild the layout using the new parameters
function rebuildMap (layoutChanged, charge, linkSize, friction, theta, width, height, labels) {
	// Use the new parameters, or the defaults if not specified
	var charge 	 =	( charge	? charge 	: defaults['force'] 	)
	var linkSize =	( linkSize	? linkSize 	: defaults['linkSize'] 	)
	var friction =	( friction	? friction 	: defaults['friction'] 	)
	var theta 	 =	( theta	? theta 	: defaults['theta'] 	)
	
	// handle resizing when not in fullscreen mode
	if (!GraphUtil.isFullscreen() && (width && height)) {
		resizeGraph(width, height);
	}
	
	// handle the background color
	backgroundColor = $('#blackBackgroundId').is(':checked') ? '#000000' : '#ffffff'
	canvas.style('background-color', backgroundColor)
	background.attr('fill', backgroundColor)
	
	// Create the force layout
	force
		.linkDistance(linkSize)
		.friction(friction)
		.charge(charge)
		.theta(theta);
	
	// Delete all labels currently drawn
	vis
		.selectAll("text")
		.remove();
	
	// Reset the list of types to show names for
	nameList = getExpanededLabels();
	
	// Add the new labels
	label
		.append("svg:text").attr("style", "font: 11px Tahoma, Arial, san-serif;")
		.attr("id", function (d) {
			return "label-" + d.id;
		})
		.attr("class", "ignoresMouse")
		.attr("dx", 8)
		.attr("dy",".35em")
		.text(function(d) {
			return (nameList[assetTypes[d.type]])?(d.name):('');
		});
		
	if (backgroundColor == '#ffffff') {
		label.attr("class", "node nodeLabel blackText")
	} else {
		label.attr("class", "node nodeLabel")
	}
	
	// if we only changed the labels or background color, only one tick is needed to reflect this change
	if (layoutChanged)
		force.start();
	else
		force.tick();
}

function resizeGraph (width, height) {
	widthCurrent = width;
	heightCurrent = height;
	
	canvas
		.attr("width", width)
		.attr("height", height);
	
	force
		.size([width, height])
		.start();
}
	
// calls the tick function n times without letting the browser paint in between
function calmTick (n) {
	for (var i = 0; i < n; ++i) {
		force.tick();
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
					if (linkList.indexOf(links[node.dependsOn[i]]) == -1) {
						linkList.push(links[node.dependsOn[i]]);
						queue.unshift(links[node.dependsOn[i]].target);
					}
				}
				for (var i = 0; i < node.supports.size(); ++i) {
					if (linkList.indexOf(links[node.supports[i]]) == -1) {
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
		alert('could not find any unpartitioned applications');
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
					c.group = groupCount;
				}
			});
			edges[0].parent.cut = 2;
			edges[0].parent.group = groupCount;
			
			// set which links were be cut
			$(edges).each(function (i, e) {
				links[e.id].cut = 1;
			});
			
			// set cut to 0 for any links that won't be cut
			for (var i = 0; i < links.size(); ++i) {
				if (links[i].cut != 1)
					links[i].cut = 0;
			}
			
			// remove the cut links from the graph
			for (var i = 0; i < links.size(); ++i) {
				for (var j = 0; j < links.size(); ++j) {
					if (links[j].cut == 1) {
						links[j].source.dependsOn.splice(links[j].source.dependsOn.indexOf(j), 1);
						links[j].target.supports.splice(links[j].target.supports.indexOf(j), 1);
						links[j].cut = 2;
						cutLinks.push(links[j]);
						links.splice(j, 1);
						
						for (var k = j; k < links.size(); ++k) {
							links[k].id = k;
							links[k].source.dependsOn[links[k].source.dependsOn.indexOf(k+1)] = k;
							links[k].target.supports[links[k].target.supports.indexOf(k+1)] = k;
						}
						
						j = links.size();
					}
				}
			}
			
			// update the graph for the modified link list
			force.links(links)
				.gravity(0.05)
				.alpha(0.1);
		
		// no cuts could be found for the constraints, so update the user
		} else {
			alert('No dependency cuts could be found');
		}
		
		$('#minCutButtonId').removeAttr('disabled');
		removeProgressBar();
	}
}

// the logic required to undo the cuts was too complicated, so just reload the graph
function undoCuts () {
	resetMap();
}

// Used by the defaults button to reset all control values to their default state
function resetToDefaults () {
	$('#labelTree input[type="text"]').each(function() {
		if (defaults[$(this).attr('name')])
			$(this).val( defaults[$(this).attr('name')] )
	});
	rebuildMap(true);
}

// Stops the map by setting the alpha value to 0
function stopMap () {
	force.stop()
}

// gets the normal width of this graph
function getStandardWidth () {
	var graphOffset = $('#svgContainerId').offset().left;
	var pageWidth = $(window).width();
	return pageWidth - graphOffset * 2;
}

// gets the normal height of this graph
function getStandardHeight () {
	var bottomMargin = $('#svgContainerId').offset().left;
	var graphOffset = $('#svgContainerId').offset().top;
	var pageHeight = $(window).height();
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

// initializes the progress bar for min cuts
function displayProgressBar () {
	var progressBar = tds.ui.progressBar(-1, 999999, function() {}, function() {}, "<h1>Calculating Group Cut Suggestion</h1>");
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