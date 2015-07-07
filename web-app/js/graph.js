/*
 * Javascript functions used by the graph view utilizing d3
 */

var GraphUtil = (function ($) {
	
	// public functions
	var public = {};
	
	// public variables
	public.force = null;
	public.nodeBindings = null;
	public.linkBindings = null;
	public.labelBindings = null;
	public.labelTextBindings = null;
	
	// returns true if the graph is loaded
	public.graphExists = function () {
		return ($('#svgContainerId').children('svg').size() > 0);
	}
	
	// returns true if the graph is in fullscreen mode
	public.isFullscreen = function () {
		return $('#item1').hasClass('fullscreen');
	}
	
	public.isBlackBackground = function () {
		return $('#blackBackgroundId').is(':checked');
	}
	
	public.isConflictsEnabled = function () {
		return $('#bundleConflictsId').is(':checked');
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
			public.correctBothPanelSizes();
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
		$('#fullscreenButtonId').children('h4').html('Normal View');
		public.moveDependencyGroups();
		public.resetGraphSize();
	}
	
	// changes the graph to normal mode
	public.disableFullscreen = function () {
		$('#item1').removeClass('fullscreen');
		$('#fullscreenButtonId').children('h4').html('Full Screen');
		public.moveDependencyGroups();
		public.resetGraphSize();
	}
	
	// sets the size of the legend so that it can scroll when longer than the user's window
	public.correctLegendSize = function () {
		public.correctPanelSize('legendDivId');
	}
	
	// sets the size of the control panel so that it can scroll when longer than the user's window
	public.correctControlPanelSize = function () {
		public.correctPanelSize('controlPanel');
	}
	
	// sets the size of the legend and control panel so that they can scroll when longer than the user's window
	public.correctBothPanelSizes = function () {
		public.correctLegendSize();
		public.correctControlPanelSize();
	}
	
	// sets the size of a panel so that it can scroll when longer than the user's window
	public.correctPanelSize = function (panelId) {
		var panel = $('#' + panelId);
		panel.css('height', '');
		panel.css('overflow-y', '');
		var svgContainer = $('#svgContainerId svg');
		if (panel.size() == 0 || svgContainer.size() == 0)
			return false;
		
		var bottom = panel.offset().top + panel.height();
		var newBottom = svgContainer.offset().top + svgContainer.innerHeight();
		
		if (bottom >= newBottom) {
			var newHeight = newBottom - panel.offset().top;
			panel.css('height', newHeight);
			panel.css('overflow-y', 'scroll');
		} else {
			panel.css('height', '');
			panel.css('overflow-y', '');
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
	
	// adds the move bundle color indicator to the legend
	public.updateLegendColorKey = function (dataMap, colors, fillMode) {
		var template = $('#colorKeyTemplateId');
		$('.colorKey').remove();
		$(Object.keys(dataMap)).each(function (i, o) {
			var newRow = template.clone();
			newRow
				.addClass('colorKey')
				.removeClass('hidden');
			var node = newRow.children('.bundleColorExample').children().children();
			node.css('fill', colors(i));
			var label = newRow.children('.bundleNameLabel');
			label.html(dataMap[o]);
			$('#legendId').append(newRow);
		});
		$('#colorKeyLabelId').removeClass('hidden')
		if (fillMode == 'group')
			$('#colorKeyLabelId h4').html('Dependency Groups');
		else if (fillMode == 'bundle')
			$('#colorKeyLabelId h4').html('Move Bundles');
		else
			$('#colorKeyLabelId h4').html('Move Events');
	}
	
	public.getFillMode = function () {
		var checkedRadio = $('#colorByFormId input:checked');
		if (checkedRadio.attr('id') == 'colorByDepGroupId')
			return 'group';
		if (checkedRadio.attr('id') == 'colorByMoveBundleId')
			return 'bundle';
		return 'event';
	}
	
	public.getFillColor = function (node, colors, fillMode) {
		if (fillMode == 'group') {
			return colors(node.depBundleIndex);
		} else if (fillMode == 'bundle') {
			return colors(node.moveBundleIndex);
		} else {
			return colors(node.moveEventIndex);
		}
	}
	
	public.settleGraph = function (force, simpleTick, normalTick) {
		force.on("tick", simpleTick);
		for (var i = 0; i < 100; ++i) {
			force.alpha(0.8);
			force.tick();
		}
		force.stop();
		force.on("tick", tick);
		force.start();
	}
	
	// called when the user clicks the show/hide layout adjustments twistie
	public.toggleGraphTwistie = function () {
		var container = $('#layoutControlContainerId');
		var twistieRow = $('#twistieRowId');
		if (twistieRow.hasClass('closed')) {
			twistieRow.removeClass('closed').addClass('open');
			container.slideDown(300, function () {
				public.correctControlPanelSize();
			});
		} else {
			twistieRow.removeClass('open').addClass('closed');
			container.slideUp(300, function () {
				public.correctControlPanelSize();
			});
		}
	}
	
	public.updateNodeClasses = function () {
		var bundle = public.getFilteredBundle();
		public.nodeBindings.attr("class", function(d) {
			return 'node'
				+ ((d.selected == 1) ? ' selected selectedChild' : '')
				+ ((d.selected == 2) ? ' selected selectedParent' : '')
				+ ((public.isConflictsEnabled() && ! d.hasMoveEvent) ? ' noEvent' : '')
				+ ((public.isBundleFilterEnabled() && ! public.isInFilteredBundle(d, bundle)) ? ' filtered' : '')
				+ ((public.isBlackBackground()) ? ' blackBackground' : '');
		});
	}
	public.updateLinkClasses = function () {
		public.linkBindings.attr("class", function(d) {
			return 'link'
				+ ((d.selected == 1) ? ' selected' : '')
				+ ((d.unresolved) ? ' unresolved' : '')
				+ ((d.notApplicable) ? ' notApplicable' : '')
				+ ((d.future) ? ' future' : '')
				+ ((d.cut == 3) ? ' cut' : '')
				+ ((public.isConflictsEnabled() && d.bundleConflict) ? ' bundleConflict' : '')
				+ ((public.isBlackBackground()) ? ' blackBackground' : '');
		});
	}
	public.updateLabelClasses = function () {
		var bundle = public.getFilteredBundle();
		public.labelBindings.attr("class", function(d) {
			return 'label'
				+ ((d.selected > 0) ? ' selected' : '')
				+ ((public.isBlackBackground()) ? ' blackBackground' : '')
				+ ((public.isBundleFilterEnabled() && ! public.isInFilteredBundle(d, bundle)) ? ' filtered' : '')
				+ ((! d.showLabel) ? ' hidden' : '');
		});
	}
	
	public.updateAllClasses = function () {
		public.updateNodeClasses();
		public.updateLinkClasses();
		public.updateLabelClasses();
	}
	
	public.checkSvgCompatibility = function () {
		if ( ! document.implementation.hasFeature("http://www.w3.org/TR/SVG11/feature#BasicStructure", "1.1") )
			$('.tabInner').html('Your browser does not support SVG, see <a href="http://caniuse.com/svg">http://caniuse.com/svg</a> for more details.');
	}
	
	// Restyles the first row of the dependency group table to handle fullscreen mode
	public.moveDependencyGroups = function () {
		if (public.isFullscreen()) {
			$('#dependencyDivId').addClass('floating');
			setGroupTablePosition();
		} else {
			$('#dependencyDivId').removeClass('floating');
			setGroupTablePosition();
		}
	}
	
	// gets the move bundle id currently selected for filtering
	public.getFilteredBundle = function () {
		var value = $('#planningBundleSelectId').val();
		value = parseInt(value);
		return value;
	}
	
	// returns true if there is a move bundle selected for filtering
	public.isBundleFilterEnabled = function () {
		return ! isNaN(public.getFilteredBundle());
	}
	
	// returns true if the node is in the bundle
	public.isInFilteredBundle = function (node, bundle) {
		return (node.moveBundleId && node.moveBundleId == bundle);
	}
	
	
	public.hidePanel = function (panel) {
		if (panel == 'control') {
			$('#controlPanel').removeClass('openPanel');
			$('#controlPanelTabId').removeClass('activeTab');
		} else if (panel == 'legend') {
			$('#legendDivId').removeClass('openPanel');
			$('#legendTabId').removeClass('activeTab');
		}
	}
	
	public.openPanel = function (panel) {
		if (panel == 'control') {
			$('#controlPanel').addClass('openPanel');
			$('#controlPanelTabId').addClass('activeTab');
		} else if (panel == 'legend') {
			$('#legendDivId').addClass('openPanel');
			$('#legendTabId').addClass('activeTab');
		}
	}

	// handles switching between the control panel and the legend
	public.togglePanel = function (panel) {
		if (panel == 'control') {
			if ($('#controlPanelTabId.activeTab').size() > 0)
				public.hidePanel('control');
			else
				public.openPanel('control');
			public.hidePanel('legend');
		} else if (panel == 'legend') {
			if ($('#legendTabId.activeTab').size() > 0)
				public.hidePanel('legend');
			else
				public.openPanel('legend');
			public.hidePanel('control');
		} else if (panel == 'hide') {
			public.hidePanel('control');
			public.hidePanel('legend');
		}
		public.correctBothPanelSizes();
	}
	
	// returns true if the graph is frozen
	public.isFrozen = function () {
		return $('#playPauseButtonId').hasClass('enabled');
	}
	
	public.enableFreeze = function () {
		$('#playPauseButtonId')
			.addClass('enabled')
			.attr('value', 'Resume Graph');
		public.force.stop();
	}
	
	public.disableFreeze = function () {
		$('#playPauseButtonId')
			.removeClass('enabled')
			.attr('value', 'Freeze Graph');
		public.force.resume();
	}
	
	public.toggleFreeze = function () {
		if (public.isFrozen()) {
			public.disableFreeze();
		} else {
			public.enableFreeze();
		}
	}
	
	// sets the alpha if the graph is not frozen
	public.setAlpha = function (alpha) {
		if ( ! public.isFrozen() )
			public.force.alpha(alpha);
		else
			return false;
		return true;
	}
	
	// adds references back from the data objects to their bound elements
	public.addBindingPointers = function () {
		for (var i = 0; i < public.nodeBindings.size(); i++) {
			var element = public.nodeBindings.first()[i];
			element.__data__.nodeElement = d3.select(element);
		}
		for (var i = 0; i < public.labelBindings.size(); i++) {
			var element = public.labelBindings.first()[i];
			element.__data__.labelElement = d3.select(element);
		}
		for (var i = 0; i < public.linkBindings.size(); i++) {
			var element = public.linkBindings.first()[i];
			element.__data__.linkElement = d3.select(element);
		}
	}
	
	// gets a list of all links adjacent to this node
	public.getAdjacentLinks = function (node) {
		var list = [];
		for (var i = 0; i < public.linkBindings.first().size(); i++) {
			var link = public.linkBindings.first()[i].__data__;
			if (link.source == node || link.target == node)
				list.push(link);
		}
		
		return list;
	}
	
	public.updateNodePosition = function (node) {
		node.nodeElement.attr('transform', 'translate(' + node.x + ' ' + node.y + ')');
		node.nodeElement.attr('cx', node.x);
		node.nodeElement.attr('cy', node.y);
		var links = public.getAdjacentLinks(node);
		for (var i = 0; i < links.size(); i++) {
			var l = links[i];
			var element = l.linkElement;
			element.attr('x1', l.source.x);
			element.attr('y1', l.source.y);
			element.attr('x2', l.target.x);
			element.attr('y2', l.target.y);
		}
		node.labelElement.attr('transform', 'translate(' + node.x + ' ' + node.y + ')');
	}
	
	// unfreezes the graph and starts the force layout
	public.startForce = function () {
		public.disableFreeze();
		public.force.start();
	}
	
	// return the public object to make the public functions accessable
	return public;
	
})(jQuery); //passed 'jQuery' global variable into local parameter '$'