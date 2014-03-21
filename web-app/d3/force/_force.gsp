
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
</style>
<script type="text/javascript">
// If there is already an instance of force running in memory, it should be stopped before creating this one
if(force != null) {
	console.log("Force is not null, force="+force)
	force.stop()
}
force = d3.layout.force()
var forceParent = d3.layout.force()
var canvas = d3.select("div#item1")
	.append("div")
	.attr('id','svgContainerId')
	.append("svg:svg");

// define the arrowhead markers used for marking dependencies
canvas.append("defs")
	.append("marker")
	.attr("id", "arrowhead")
	.attr("viewBox", "0 -5 10 10")
	.attr("refX", 20)
	.attr("refY", 0)
	.attr("markerUnits", "userSpaceOnUse")
	.attr("markerWidth", 10)
	.attr("markerHeight", 10)
	.attr("orient", "auto")
	.append("path")
	.attr("d", "M0,-4L10,0L0,4")
	.attr('fill', '#808080');
	
d3.select("defs")
	.append("marker")
	.attr("id", "arrowheadSelected")
	.attr("viewBox", "0 -5 10 10")
	.attr("refX", 20)
	.attr("refY", 0)
	.attr("markerUnits", "userSpaceOnUse")
	.attr("markerWidth", 10)
	.attr("markerHeight", 10)
	.attr("orient", "auto")
	.append("path")
	.attr("d", "M0,-5L10,0L0,5")
	.attr("fill", "#00dd00");

$('#svgContainerId')
	.resizable({
		minHeight: 400,
		minWidth: 1000,
		helper: "ui-resizable-helper",
		stop: function(e, ui) {	
			$('#heightId').val($(this).height());
			$('#widthId').val($(this).width());
			rebuildMap($("#forceId").val(), $("#linkSizeId").val(), $("#frictionId").val(), $("#thetaId").val(), $(this).width(), $(this).height());
		}
	});
var zoomBehavior;
var vis = canvas
var background
var overlay
var graph
var defaults = ${defaultsJson}
var links = ${links}
var nodes = ${nodes}
var graphstyle = "top:-120;z-index:-1;"
var	r = 5
var	fill = d3.scale.category20()
var gravity = ${multiple?0.05:0}
var maxWeight

var nodeSelected = -1
var defaultColor = '#0000ff'
var selectedParentColor = '#00ff00'
var selectedChildColor = '#00cc99'
var selectedLinkColor = '#00dd00'
var backgroundColor = defaults.blackBackground ? '#000000' : '#ffffff'
if (defaults.blackBackground)
	$('marker#arrowhead').attr('fill', '#ffffff');

var widthCurrent
var heightCurrent
var nameList = listCheck();

var floatMode = false;

			
// Build the layout model
function buildMap (charge, linkSize, friction, theta, width, height) {
	$('#item1').css('height', '');

	// Use the new parameters, or the defaults if not specified
	var charge 	 =	( charge	? charge 	: defaults['force'] 	);
	var linkSize =	( linkSize	? linkSize 	: defaults['linkSize'] 	);
	var friction =	( friction	? friction 	: defaults['friction'] 	);
	var theta 	 =	( theta	? theta 	: defaults['theta'] 	);
	var width 	 = 	( width 	? width 	: defaults['width'] 	);
	var height 	 = 	( height 	? height 	: defaults['height'] 	);
	
	widthCurrent = width;
	heightCurrent = height;
	
	var zoom = d3.behavior.zoom()
		.on("zoom", zooming)
		.on("zoomend", zoomEnd);
	
	// Sets the panning behavior
	var panBehavior = d3.behavior.drag()
		.on("dragstart", panStart)
		.on("drag", panMove)
		.on("dragend", panEnd);
	
	canvas.call(zoom)
		.on("mousedown.zoom", null)
		.on("touchstart.zoom", null)
		.on("touchmove.zoom", null)
		.on("touchend.zoom", null)
		.call(panBehavior);
	
	vis = canvas
		.append('svg:g')
			.on("dblclick.zoom", null)
			.style('width', '100%')
			.style('height', '100%')
		.append('svg:g')
	
	
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
	
	var startAlpha = 0
	var dragging = false;
	var clicked = false;
	
	function dragstart (d, i) {
		startAlpha = force.alpha();
		dragging = true;		
		clicked = true;
		d3.event.sourceEvent.stopPropagation();
	}

	function dragmove (d, i) {
		startAlpha = Math.min(startAlpha+0.005, 0.1);
		d.x += d3.event.dx;
		d.y += d3.event.dy;
		d.px = d.x;
		d.py = d.y;
		d.cx = d.x;
		d.cy = d.y;
		
		d.fix = true;
		d.fixed = true;
		clicked = false;
		force.alpha(startAlpha);
		d3.event.sourceEvent.stopPropagation();
	}

	function dragend (d, i) {
		d.fix = false;
		d.fixed = false;
		dragging = false;
		if (clicked)
			toggleNodeSelection(d.index);
		d3.event.sourceEvent.stopPropagation();
	}
	
	
	var translateX = 0;
	var translateY = 0;
	var scale = 1;
	var panning = true;
	function panStart (d, i) {
		var target = d3.event.sourceEvent.target.nodeName;
		if (target == 'g' || target == 'rect' || target == 'svg')
			panning = true;		
		clicked = true;
	}

	function panMove (d, i) {
		if (panning) {
			translateX += d3.event.dx;
			translateY += d3.event.dy;
			vis.attr('transform',
				'translate(' + (translateX + zoomOffsetX) + ',' + (translateY + zoomOffsetY) + ')'
				+ ' scale(' + scale + ')');
		}
		clicked = false;
	}

	function panEnd (d, i) {
		panning = false;
		if (clicked)
			toggleNodeSelection(nodeSelected);
	}
	
	// Rescales the contents of the svg. Called when the user scrolls.
	var panXInitial = 0;
	var panYInitial = 0;
	var zoomOffsetX = 0;
	var zoomOffsetY = 0;
	
	// Rescales the contents of the svg. Called when the user scrolls.
	function zooming () {
		if (!dragging) {
			var target = d3.event.sourceEvent.target.nodeName;
			if (target == 'g' || target == 'rect' || target == 'svg') {
				zoomOffsetX = d3.event.translate[0];
				zoomOffsetY = d3.event.translate[1];
				scale = d3.event.scale;
				vis.attr('transform',
					'translate(' + (translateX + zoomOffsetX) + ',' + (translateY + zoomOffsetY) + ')'
					+ ' scale(' + scale + ')');
			}
		}
	}
	function zoomEnd () {
		if (!dragging)
			if ( ! d3.event.sourceEvent )
				vis.attr('transform',
					'translate(' + (translateX + zoomOffsetX) + ',' + (translateY + zoomOffsetY) + ')'
					+ ' scale(' + scale + ')');
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
		vis.attr("transform",
		  "translate(0)"
		  + " scale(1)");
	}
	
	// Handle the panning when the user starts dragging the canvas
	function mousedown() {
		return;
	}
	
	// Start each node in the center of the map
	$.each(nodes, function() {
		this.x = width/2+10*Math.random()
		this.px = width/2+10*Math.random()
		this.y = height/2+10*Math.random()
		this.py = height/2+10*Math.random()
	});
	
	// Create the SVG element
	canvas
		.attr("width", width)
		.attr("height", height)
		.attr("style", graphstyle)
		.style('background-color', backgroundColor)
		.style('cursor', 'default !important')
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
		.charge(function(){
			if(true)
			return charge
		})
		.size([width, height])
		.theta(theta)
		.start();
	
	
	// Add the links the the SVG
	var link = vis.selectAll("line.link")
		.data(links).enter()
		.append("svg:line")
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
	
	link.style("stroke", function(d) {
		return d3.select(this).attr('fillColor')
	});
	
	// Add the nodes to the SVG
	var node = vis.selectAll("path")
		.data(nodes).enter()
	
	// Calculate the maximum weight value, which is used during the tick function
	var maxWeight = 1
	nodes.forEach(function(o, i) {
		maxWeight = Math.max( maxWeight, (o.weight?o.weight:0) )
		o.fix = false;
	})
	
	// Create the nodes
	node = node
		.append("svg:path")
			.attr("class", "node")
			.call(dragBehavior)
			.on("dblclick", function(d) { return getEntityDetails('planningConsole', d.type, d.id); })
			.on("mousedown", mousedown)
			.attr("d", d3.svg.symbol().size(function(d) { return d.size; }).type(function(d) { return d.shape; }))
			.attr("fix", false)
			.attr("id", function(d) { return 'node-'+d.index })
			.style("fill", function(d) { return fill(d.group); })
			.style('cursor', 'default !important')
			.attr("fillColor", function(d) {
				return fill(d.group) 
			})
			.style("stroke", function(d) {
				d.r = 15;
				return d.color;
			})
			.style("stroke-width", '2px')
	
	
	// Toggles selection of a node
	function toggleNodeSelection(id) {
		
		if (nodeSelected == -1 && id == -1)
			return // No node is selected, so there is nothing to deselect
		
		var node = force.nodes()[id]
		var parentColor = fill(node.group)
		var childColor = ''
		var linkColor = ''
		var linkWidth = '1px'
		var selectedLinks = ['z','z']
		var selectedNodes = ['z','z']
			selectedNodes.push('node-'+node.index)
		var selecting = true
		
		if (nodeSelected == id) {
			// deselecting
			selecting = false
			nodeSelected = -1
		} else {
			// selecting
			if (nodeSelected != -1)
				toggleNodeSelection(nodeSelected) // Another node is selected, so deselect that one first
			parentColor = selectedParentColor
			childColor = selectedChildColor
			linkColor = selectedLinkColor
			linkWidth = '3px'
			nodeSelected = id
		}
		
		// Style the selected node
		node.fillColor = parentColor
		d3.selectAll('svg g g path').filter('#node-'+node.index)[0][0].classList.add('selected')
		
		// Style the dependencies of the selected node
		var useTarget = true;
		$.each(node.dependsOn, styleDependencies);
		useTarget = false;
		$.each(node.supports, styleDependencies);
		
		function styleDependencies (index, linkIndex) {
			var link = force.links()[linkIndex]
			var childNode = (useTarget)?(link.target):(link.source)
			link.fillColor = (linkColor ? linkColor : link.statusColor)
			link.width = linkWidth
			childNode.fillColor = childColor ? childColor : fill(childNode.group)
			selectedLinks.push('link-'+link.source.index+'-'+link.target.index)
			selectedNodes.push('node-'+childNode.index)
			d3.selectAll('svg g g path').filter('#node-'+childNode.index)[0][0].classList.add('selected')
			d3.selectAll('svg g g line').filter('#link-'+link.source.index+'-'+link.target.index)[0][0].classList.add('selected')
		}
		
		// Sort all the svg elements to reorder them in the DOM (SVG has no z-index property)
		if(selecting) {
			var selection = d3.selectAll('svg g g g')
			selection[0] = selection[0].concat(d3.selectAll('svg g g line').filter(':not(.selected)')[0])
				.concat(d3.selectAll('svg g g path').filter(':not(.selected)')[0])
				.concat(d3.selectAll('svg g g line').filter('.selected')[0])
				.concat(d3.selectAll('svg g g path').filter('.selected')[0])
			var alpha = Math.max(force.alpha(), 0.007);
			$.each(force.nodes(), function(i, n) {
				n.fixed = true;
			})
			selection.order();
			force.stop();
			$.each(force.nodes(), function(i, n) {
				n.fixed = false;
			})
			force.alpha(alpha);
		} else { // The funtion is deselected a node, so remove the selected class from all elements
			d3.selectAll('svg g g line').filter('.selected')[0].each(function(o){
				o.classList.remove('selected')
			})
			d3.selectAll('svg g g path').filter('.selected')[0].each(function(o){
				o.classList.remove('selected')
			})
			force.alpha(Math.max(force.alpha(), 0.007));
		}
	}
	
	
	node.append("title").text(function(d){ return d.title })
	
	graph = vis.selectAll("g.node")
		.data(nodes).enter()
		.append("svg:g")
	
	if (backgroundColor == '#ffffff')
		graph.attr("class", "node nodeLabel blackText")
	else
		graph.attr("class", "node nodeLabel")
	
	graph.append("svg:text").attr("style", "font: 11px Tahoma, Arial, san-serif;")
		.attr("dx", 12)
		.attr("dy",".35em")
		.text(function(d) {
			return (nameList[d.type+''])?(d.name):('');
		});
	
	force.on("tick", tick)
	
	var firstTick = true;
	function tick(e) {
		if ( gravity == 0 && ! floatMode ) {
			var k =  e.alpha
			nodes.forEach(function(o, i) {
				if(! o.fix) {
					if( maxWeight > 1 && (o.weight?o.weight:0) == 1 )
						k = 0
					o.y += (heightCurrent/2 - o.y) * k * ((o.weight+1) / maxWeight)
					o.x += (widthCurrent/2 - o.x) * k * ((o.weight+1) / maxWeight)
				}
			})
		}
		
		node
			.attr("cx", function(d) {
				return d.x
			})
			.attr("cy", function(d) {
				return d.y
			})
			.attr("transform", function(d) {return "translate(" + d.x + "," + d.y + ")";})
			.style("fill", function(d) {
				return (d.fillColor) ? (d.fillColor) : (d3.select(this).attr('fillColor'))
			})
			
		link
			.attr("x1", function(d) {return d.source.x;})
			.attr("y1", function(d) {return d.source.y;})
			.attr("x2", function(d) {return d.target.x;})
			.attr("y2", function(d) {return d.target.y;})
			.style("stroke", function(d) {
				return (d.fillColor) ? (d.fillColor) : (d3.select(this).attr('fillColor'))
			})
			.style("stroke-width", function(d) {return d.width})
		
		graph
			.attr("cx", function(d) {return d.x })
			.attr("cy", function(d) {return d.y })
			.attr("transform", function(d) {return "translate(" + d.x + "," + d.y + ")";});
	}
	
	vis.append(background.remove());
}


// Used to rebuild the layout using the new parameters
function rebuildMap (charge, linkSize, friction, theta, width, height, labels) {

	// Use the new parameters, or the defaults if not specified
	var charge 	 =	( charge	? charge 	: defaults['force'] 	)
	var linkSize =	( linkSize	? linkSize 	: defaults['linkSize'] 	)
	var friction =	( friction	? friction 	: defaults['friction'] 	)
	var theta 	 =	( theta	? theta 	: defaults['theta'] 	)
	
	widthCurrent 	= ( widthCurrent  ? widthCurrent  : defaults['width']  )
	heightCurrent 	= ( heightCurrent ? heightCurrent : defaults['height'] )
	
	var width	= ( width	? width  :	( widthCurrent	? widthCurrent	: defaults['width']	) )
	var height	= ( height 	? height :	( heightCurrent 	? heightCurrent	: defaults['height']	) )
	
	widthCurrent = width
	heightCurrent = height
	
	backgroundColor = $('#blackBackgroundId').is(':checked') ? '#000000' : '#ffffff'
	
	// Create the SVG element
	canvas
		.attr("width", width)
		.attr("height", height)
		.style('background-color', backgroundColor)
	
	background.attr('fill', backgroundColor)
	
	// Create the force layout
	force
		.linkDistance(linkSize)
		.friction(friction)
		.charge(charge)
		.theta(theta)
		.size([width, height])
		.start();
	
	// Delete all labels currently drawn
	vis
		.selectAll("text")
		.remove();
	
	// Reset the list of types to show names for
	nameList = listCheck();
	
	// Add the new labels
	graph
		.append("svg:text").attr("style", "font: 11px Tahoma, Arial, san-serif;")
		.attr("dx", 8)
		.attr("dy",".35em")
		.text(function(d) {
			return (nameList[d.type+''])?(d.name):('');
		});
		
		if (backgroundColor == '#ffffff') {
			graph.attr("class", "node nodeLabel blackText")
		} else {
			graph.attr("class", "node nodeLabel")
		}
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
	//$('#playPauseButtonId').val('Resume')
	force.stop()
}


</script>
</div>