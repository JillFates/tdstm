/*
 * Javascript functions used by the graph view utilizing d3
 */

var GraphUtil = (function ($) {
	
	// public functions
	var public = {};
	
	// returns true if the graph is loaded
	public.graphExists = function () {
		return ($('#svgContainerId').children('svg').size() > 0);
	}
	
	// returns true if the graph is in fullscreen mode
	public.isFullscreen = function () {
		return $('#item1').hasClass('fullscreen');
	}
	
	// resets the graph to the proper size
	public.getProperGraphDimensions = function () {
		var width = getStandardWidth();
		var height = getStandardHeight();
		if (public.isFullscreen()) {
			width = $(window).innerWidth();
			height = $(window).innerHeight();
		}
		return {width:width, height:height};
	}
	
	// resets the graph to the proper size
	public.resetGraphSize = function () {
		if (public.graphExists()) {
			var dimensions = public.getProperGraphDimensions();
			resizeGraph(dimensions.width, dimensions.height);
			public.correctLegendSize();
		}
	}
	
	// toggles full screen mode for any graph
	public.toggleFullscreen = function () {
		if (public.graphExists()) {
			if (public.isFullscreen())
				public.disableFullscreen();
			else
				public.enableFullscreen();
			public.resetGraphSize();
		}
	}
	
	// changes the graph to fullscreen mode
	public.enableFullscreen = function () {
		$('#item1').addClass('fullscreen');
		$('#fullscreenButtonId').children('h4').html('Normal Mode');
		public.resetGraphSize();
	}
	
	// changes the graph to normal mode
	public.disableFullscreen = function () {
		$('#item1').removeClass('fullscreen');
		$('#fullscreenButtonId').children('h4').html('Full Screen');
		public.resetGraphSize();
	}
	
	// adds the move bundle color indicator to the legend
	public.addMoveBundlesToLegend = function (moveBundleMap, colors) {
		var template = $('#templateBundleId');
		$(Object.keys(moveBundleMap)).each(function (i, o) {
			var newRow = template.clone();
			newRow.removeClass('hidden');
			var node = newRow.children('.bundleColorExample').children().children();
			node.css('fill', colors(i));
			var label = newRow.children('.bundleNameLabel');
			label.html(moveBundleMap[o]);
			$('#legendId').append(newRow);
		});
		$('#moveBundleKeyId').removeClass('hidden');
	}
	
	// sets the size of the legend so that it can scroll when longer than the user's window
	public.correctLegendSize = function () {
		var legend = $('#legendDivId');
		legend.css('height', '');
		legend.css('overflow-y', '');
		var svgContainer = $('#svgContainerId svg');
		var bottom = legend.offset().top + legend.height();
		var newBottom = svgContainer.offset().top + svgContainer.innerHeight();
		
		if (bottom >= newBottom) {
			var newHeight = newBottom - legend.offset().top;
			legend.css('height', newHeight);
			legend.css('overflow-y', 'scroll');
		} else {
			legend.css('height', '');
			legend.css('overflow-y', '');
		}
	}
	
	// calculates node families
	public.setNodeFamilies = function (nodes) {
		var uncheckedNodes = nodes.clone();
		var family = 0
		var nodeFamilies = [];
		
		// build the families
		nodes.each(function (node, i) {
			var index = uncheckedNodes.indexOf(node);
			if (index != -1) {
				nodeFamilies[family] = [];
				traverseNodesForFamily(node, uncheckedNodes, family);
				nodeFamilies[family].push(node);
				++family;
			} else {
				nodeFamilies[node.family].push(node);
			}
		});
		
		// sort the families
		nodeFamilies.sort(function (a, b) {
			if (a.size() > b.size())
				return 1;
			if (a.size() < b.size())
				return -1;
			return 0;
		});
		
		// update the family references after sorting
		nodeFamilies.each(function (f, i) {
			f.each(function (node) {
				node.family = i;
			});
		});
		
		return nodeFamilies;
	}
	
	function traverseNodesForFamily (node, uncheckedNodes, family) {
		var index = uncheckedNodes.indexOf(node);
		if (index != -1) {
			node.family = family;
			uncheckedNodes.splice(index, 1);
			getChildren(node).each(function (child, i) {
				traverseNodesForFamily(child, uncheckedNodes, family);
			});
			getParents(node).each(function (parent, i) {
				traverseNodesForFamily(parent, uncheckedNodes, family);
			});
			
		}
	}
	
	// performs one tick of the 
	public.tickOnce = function (force) {
		var oldAlpha = force.alpha();
		force.alpha(1);
		force.tick();
		force.alpha(oldAlpha);
	}
	
	// return the public object to make the publiclic functions accessable
	return public;
	
})(jQuery); //passed 'jQuery' global variable into local parameter '$'