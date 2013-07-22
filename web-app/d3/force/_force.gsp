
<div id="scriptDivId">
<script type="text/javascript">
//var jsonData = '${jsonData}';
if(force != null) {
	console.log("Force is not null, force="+force)
	force.stop()
}
force = d3.layout.force()
var forceParent = d3.layout.force()
var canvas = d3.select("div#item1")
var vis = canvas.append("svg:svg")
var overlay
var graph
var links = ${links}
var nodes = ${nodes}
var graphstyle = "top:-120;z-index:-1;"
var	r = 5
var	fill = d3.scale.category20()
var gravity = 0
var maxWeight

var defaults = ${defaultsJson}
var widthCurrent
var heightCurrent
var nameList = listCheck();

/*var xMax = 0
var xMin = 10000
var yMax = 0
var yMin = 10000
var xMaxOld = 300
var xMinOld = 300
var yMaxOld = 200
var yMinOld = 200*/
			
// Build the layout model
function buildMap (charge, linkSize, friction, width, height) {

	// Use the new parameters, or the defaults if not specified
	var charge 	 =	( charge	? charge 	: defaults['force'] 	)
	var linkSize =	( linkSize	? linkSize 	: defaults['linkSize'] 	)
	var friction =	( friction	? friction 	: defaults['friction'] 	)
	var width 	 = 	( width 	? width 	: defaults['width'] 	)
	var height 	 = 	( height 	? height 	: defaults['height'] 	)
	
	widthCurrent = width
	heightCurrent = height
	
	var node_drag = d3.behavior.drag()
        .on("dragstart", dragstart)
        .on("drag", dragmove)
        .on("dragend", dragend);
		
	function dragstart(d, i) {
		d.fix = true;
		d.fixed = true;
		force.resume();
	}

	function dragmove(d, i) {
		d.px += d3.event.dx;
		d.py += d3.event.dy;
		d.x += d3.event.dx;
		d.y += d3.event.dy;
		force.resume();
	}

	function dragend(d, i) {
		if( d.weight > 0 ) {
			d.fix = false;
			d.fixed = false;
		}
		force.resume();
	}
	
	// Start each node in the center of the map
	$.each(nodes, function(){
		this.x = width/2+10*Math.random()
		this.px = width/2+10*Math.random()
		this.y = height/2+10*Math.random()
		this.py = height/2+10*Math.random()
	});
	
	// Create the SVG element
	vis
		.attr("width", width)
		.attr("height", height)
		.attr("style", graphstyle)
		.style("fill", "#0000ff");
	
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
		.start();
	
	// Add the links the the SVG
	var link = vis.selectAll("line.link")
		.data(links).enter()
		.append("svg:line")
		.on("mouseover", function(d) { $(this).css('stroke','black') })
		.on("mouseout", function(d) { $(this).css('stroke',d.statusColor) })
		.style("stroke",function(d) {return d.statusColor;})
		.style("stroke-opacity", function (d) { return d.opacity;})
		.attr("class", "link")
		.attr("x1", function(d) { return d.source.x;})
		.attr("y1", function(d) { return d.source.y;})
		.attr("x2", function(d) { return d.target.x;})
		.attr("y2", function(d) { return d.target.y;});
	
	// Add the nodes to the SVG
	var node = vis.selectAll("path")
		.data(nodes).enter()
	
	var maxWeight = 1
	nodes.forEach(function(o, i) {
		maxWeight = Math.max( maxWeight, o.weight )
		o.fix = false;
	})
	
	node = node
		.append("svg:path")
		.attr("class", "node")
		.call(node_drag)
		.on("dblclick", function(d) { return getEntityDetails('planningConsole', d.type, d.id); })
		.attr("d", d3.svg.symbol().size(function(d) { return 200+100*(Math.abs(d.weight-1)/maxWeight); }).type(function(d) { return d.shape; }))
		.attr("fix", false)
		.style("fill", function(d) { return fill(d.group);})
		.style("stroke", function(d) {
			d.r = 15;
			return d.color;
		})
		.style("stroke-width", '2px')
		
		
		
	node.append("title").text(function(d){ return d.title })
	
	graph = vis.selectAll("g.node")
		.data(nodes).enter()
		.append("svg:g")
		.attr("class", "node")
		.call(force.drag);
	
	graph.append("svg:text").attr("style", "font: 11px Tahoma, Arial, san-serif;")
		.attr("dx", 12)
		.attr("dy",".35em")
		.text(function(d) {
			return (nameList[d.type+''])?(d.name):('');
		});
	
	$(document)
		.mouseup( function () {
			//deselectNodes();
		})
	
	force.on("tick", tick)
	
	function tick(e) {
	
		var k =  e.alpha
		nodes.forEach(function(o, i) {
			if(! o.fix) {
				if( maxWeight > 1 && o.weight == 1 )
					k = 0
				o.y += (heightCurrent/2 - o.y) * k
				o.x += (widthCurrent/2 - o.x) * k
			}
		})
		
		node
			.attr("cx", function(d) {
				d.x = Math.max(d.r, Math.min(widthCurrent - d.r, d.x));
				return d.x
			})
			.attr("cy", function(d) {
				d.y = Math.max(d.r, Math.min(heightCurrent - d.r, d.y));
				return d.y
			})
			.attr("transform", function(d) {return "translate(" + d.x + "," + d.y + ")";});
			
		link
			.attr("x1", function(d) {return d.source.x;})
			.attr("y1", function(d) {return d.source.y;})
			.attr("x2", function(d) {return d.target.x;})
			.attr("y2", function(d) {return d.target.y;});
		
		graph
			.attr("cx", function(d) {return d.x = Math.max(d.r, Math.min(widthCurrent - d.r, d.x));})
			.attr("cy", function(d) {return d.y = Math.max(d.r, Math.min(heightCurrent - d.r, d.y));})
			.attr("transform", function(d) {return "translate(" + d.x + "," + d.y + ")";});
			
		
	}
}


// Used to rebuild the layout using the new parameters
function rebuildMap (charge, linkSize, friction, width, height, labels) {

	// Use the new parameters, or the defaults if not specified
	var charge 	 =	( charge	? charge 	: defaults['force'] 	)
	var linkSize =	( linkSize	? linkSize 	: defaults['linkSize'] 	)
	var friction =	( friction	? friction 	: defaults['friction'] 	)
	
	widthCurrent 	= ( widthCurrent  ? widthCurrent  : defaults['width']  )
	heightCurrent 	= ( heightCurrent ? heightCurrent : defaults['height'] )
	
	var width	= ( width	? width  :	( widthCurrent	? widthCurrent	: defaults['width']	 ) )
	var height	= ( height 	? height :	( heightCurrent ? heightCurrent : defaults['height'] ) )
	
	widthCurrent = width
	heightCurrent = height
	
	// Create the SVG element
	vis
		.attr("width", width)
		.attr("height", height)
	
	// Create the force layout
	force
		.linkDistance(linkSize)
		.friction(friction)
		.charge(charge)
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
}

// USed by the defaults button to reset all control values to their default state
function resetToDefaults () {
	$('#labelTree input[type="text"]').each(function() {
		$(this).val( defaults[$(this).attr('name')] )
	});
	widthCurrent = defaults['width']
	heightCurrent = defaults['height']
	rebuildMap();
}


</script>
</div>