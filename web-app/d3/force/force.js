var w = 1500,
	h = 1000,
	r = 5,
	fill = d3.scale.category20();
var vis = d3.select("body")
			.append("svg:svg")
			.attr("width", w)
			.attr("height", h);
d3.json("miserables.json", function(json) {
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
	var node = vis.selectAll("g.node")
				.data(json.nodes).enter()
				.append("svg:g")
				.attr("class", "node")
				.call(force.drag);
	
	/*node.each(function(d) {
		switch(d.type){
			case 'Application':
				vis.select(this).append("svg:circle")
					.attr("r", r).style("fill", function(d) {return fill(d.group);
				});
			break;
			case 'VM' :
			case 'Server':
				vis.select(this).append("svg:rect")
					.attr("rx", 8).attr("ry", 4)
					.style("fill", function(d) {return fill(d.group);
				});
			break;
			case 'Files':
		    case 'Database':
		    	vis.select(this).append("svg:ellipse")
					.attr("rx", 8).attr("ry", 4)
					.style("fill", function(d) {return fill(d.group);
				});
			break;
		}
	});*/

	node.append("svg:circle")
		.attr("r", r).style("fill", function(d) {return fill(d.group);
	});
	
	
	/*node.append("svg:image").attr("class", "circle")
		.attr("xlink:href","https://d3nwyuy0nl342s.cloudfront.net/images/icons/public.png")
		.attr("x", "-8px")
		.attr("y", "-8px")
		.attr("width", "16px")
		.attr("height", "16px");*/

	node.append("svg:text").attr("class", "nodetext")
		.attr("dx", 12)
		.attr("dy",".35em")
		.text(function(d) {return d.name});

	force.on("tick", function() {
		link.attr("x1", function(d) {return d.source.x;})
		.attr("y1", function(d) {return d.source.y;})
		.attr("x2", function(d) {return d.target.x;})
		.attr("y2", function(d) {return d.target.y;});

	node.attr("cx", function(d) {return d.x = Math.max(r, Math.min(w - r, d.x));})
		.attr("cy", function(d) {return d.y = Math.max(r, Math.min(h - r, d.y));})
		.attr("transform", function(d) {
			return "translate(" + d.x + "," + d.y + ")";
		});
	});
});