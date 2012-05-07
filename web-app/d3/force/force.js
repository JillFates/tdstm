var w = 1600,
	h = 1000,
	graphstyle = "top:-120;z-index:-1;",
	r = 5,
	fill = d3.scale.category20();
var vis = d3.select("div#item1")
			.append("svg:svg")
			.attr("width", w)
			.attr("height", h)
			.attr("style",graphstyle);
			
d3.json("../d3/force/miserables.json", function(json) {
	var force = self.force = d3.layout.force()
				.nodes(json.nodes).links(json.links)
				.gravity(.05)
				.distance(json.linkdistance)
				.charge(json.force)
				.size([ w, h ])
				.start();
	var link = vis.selectAll("line.link")
				.data(json.links).enter()
				.append("svg:line")
				.attr("class", "link")
				.attr("x1", function(d) { return d.source.x;})
				.attr("y1", function(d) { return d.source.y;})
				.attr("x2", function(d) { return d.target.x;})
				.attr("y2", function(d) {
		return d.target.y;
	});
	var node = vis.selectAll("path")
				.data(json.nodes).enter()
				.append("svg:path")
				.attr("class", "node")
				.call(force.drag)
				.on("dblclick", function(d) { return getEntityDetails('planningConsole', d.type, d.id); })
				.attr("d", d3.svg.symbol().size(function(d) { return d.size; }).type(function(d) { return d.shape; }))
			    .style("fill", function(d) {return fill(d.group);});
	node.append("title").text(function(d){ return d.title })
	
         
    var graph = vis.selectAll("g.node")
				.data(json.nodes).enter()
				.append("svg:g")
				.attr("class", "node")
				.call(force.drag);
	graph.append("svg:text").attr("style", "font: 11px Tahoma, Arial, san-serif;")
							.attr("dx", 8)
							.attr("dy",".35em")
							.text(function(d) {return d.name});

	force.on("tick", function() {
			link.attr("x1", function(d) {return d.source.x;})
				.attr("y1", function(d) {return d.source.y;})
				.attr("x2", function(d) {return d.target.x;})
				.attr("y2", function(d) {return d.target.y;});

	node.attr("cx", function(d) {return d.x = Math.max(r, Math.min(w - r, d.x));})
		.attr("cy", function(d) {return d.y = Math.max(r, Math.min(h - r, d.y));})
		.attr("transform", function(d) {return "translate(" + d.x + "," + d.y + ")";});
	graph.attr("cx", function(d) {return d.x = Math.max(r, Math.min(w - r, d.x));})
		.attr("cy", function(d) {return d.y = Math.max(r, Math.min(h - r, d.y));})
		.attr("transform", function(d) {return "translate(" + d.x + "," + d.y + ")";});
	});
});
