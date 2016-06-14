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
	public.shapeOffset = -20;
	public.labelShapeOffset = -8
	public.GPUNodeThreshold = 500
	public.labelHeightDefault = 15; // Modify if the size of the text from the rect is different.
	public.nodeRadius = {'Default': 28, 'Server': 29, 'Database': 27, 'Files': 28, 'Other': 29, 'Application': 26, 'VM': 25};
	public.defaultDimensions = {'width': 28, 'height': 28};

	// returns true if the graph is loaded
	public.graphExists = function () {
		return ($('#svgContainerId #svgTranslatorId').children('svg').size() > 0)
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

	public.isHighlightCyclesEnabled = function () {
		return $('#highlightCyclicalCheckBoxId').is(':checked');
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
		}
	}

	// changes the graph to fullscreen mode
	public.enableFullscreen = function () {
		$('#item1').addClass('fullscreen');
		if ($('#fullscreenButtonId').hasClass('showMenu'))
			$('#fullscreenButtonId').children('h4').html('Show Menu');
		else
			$('#fullscreenButtonId').children('h4').html('Normal View');
		public.moveDependencyGroups();
		public.resetGraphSize();
	}

	// changes the graph to normal mode
	public.disableFullscreen = function () {
		$('#item1').removeClass('fullscreen');
		$('#fullscreenButtonId').children('h4').html('Fullscreen');
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
		var svgContainer = $('#svgContainerId');
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
	public.tickOnce = function () {
		var oldAlpha = public.force.alpha();
		public.force.alpha(1);
		public.force.tick();
		public.force.alpha(oldAlpha);
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
	public.toggleGraphTwistie = function (twistieSpan) {
		var container = $('#' + twistieSpan.attr('for'));
		if (twistieSpan.hasClass('closed')) {
			twistieSpan.removeClass('closed').addClass('open');
			container.slideDown(300, function () {
				public.correctControlPanelSize();
			});
		} else {
			twistieSpan.removeClass('open').addClass('closed');
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
				+ ((d.root) ? ' root' : '')
				+ ((public.isConflictsEnabled() && ! d.hasMoveEvent) ? ' noEvent' : '')
				+ ((public.isBundleFilterEnabled() && ! public.isInFilteredBundle(d, bundle)) ? ' filtered' : '')
				+ ((d.sourceAsset) ? ' sourceAsset' : '')
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
				+ ((d.cut) ? ' cut' : '')
				+ ((d.root) ? ' root' : '')
				+ ((public.isConflictsEnabled() && d.bundleConflict) ? ' bundleConflict' : '')
				+ ((public.isHighlightCyclesEnabled() && d.partOfCycle) ? ' cyclical' : '')
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

	// returns true if this page has a dependency group table
	public.hasDependencyGroups = function () {
		return ($('#dependencyDivId').size() > 0);
	}

	// Restyles the first row of the dependency group table to handle fullscreen mode
	public.moveDependencyGroups = function () {
		if (public.hasDependencyGroups()) {
			if (public.isFullscreen())
				$('#dependencyDivId').addClass('floating');
			else
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
		var element;
		var nodes = public.nodeBindings.first();
		for (var i = 0; i < nodes.length; i++) {
			element = nodes[i];
			element.__data__.nodeElement = element;
		}
		var labels = public.labelBindings.first();
		for (var i = 0; i < labels.length; i++) {
			element = labels[i];
			element.__data__.labelElement = d3.select(element);
		}
		var links = public.linkBindings.first();
		for (var i = 0; i < links.length; i++) {
			element = links[i];
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

	// Basic function to create a Rectangle on pure js
	public.createRect = function(x, y, h, w){
		var svgNS = "http://www.w3.org/2000/svg",
			rect = document.createElementNS(svgNS, 'rect');
		rect.setAttributeNS(null, 'x', x);
		rect.setAttributeNS(null, 'y', y);
		rect.setAttributeNS(null, 'height', h);
		rect.setAttributeNS(null, 'width', w);

		return rect;
	}

	public.updateNodePosition = function (node) {
		node.nodeElement.setAttribute('transform', 'translate(' + node.x + ' ' + node.y + ')');
		node.nodeElement.setAttribute('cx', node.x);
		node.nodeElement.setAttribute('cy', node.y);
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


	// Sets the user's graph preferences to the current values in the control panel
	public.updateUserPrefs = function (preferenceName) {
		var form = $('#preferencesformId');
		var disabled = $('#preferencesformId').find(':input:disabled').removeAttr('disabled');
		var prefsArray = form.serializeArray();
		disabled.attr('disabled', 'disabled');
		var prefsObject = {};
		prefsArray.each(function (pref, i) {
			prefsObject[pref.name] = pref.value;
		});
		setUserPreference(preferenceName, JSON.stringify(prefsObject));
	}

	// Used by the defaults button to reset all control values to their default state
	public.resetToDefaults = function (preferenceName) {
		// resets the force layout parameters
		$('table.labelTree input[type="text"]').each(function() {
			if (defaults[$(this).attr('name')])
				$(this).val( defaults[$(this).attr('name')] )
		});

		// resets the user's graph preferences to the defaults
		setUserPreference(preferenceName, JSON.stringify(defaultPrefs));


		// resets the graph preferences
		var inputs = $('#preferencesformId input:not([type="button"]),#preferencesformId select');
		inputs.each(function (i, o) {
			var name = $(o).attr('name');
			var type = $(o).attr('type');
			var defaultValue = defaultPrefs[name];
			if (defaultValue != undefined) {
				if (type == 'checkbox') {
					$(o).prop('checked', defaultValue)
				} else if (type == 'text' || type == undefined) {
					$(o).val(defaultValue)
				} else if (type == 'radio') {
					$(o).prop('checked', ($(o).val() == defaultValue))
				}
			} else {
				$(o).prop('checked', false)
			}
		});

		// rebuild the map with the parameters
		if (public.graphExists())
			rebuildMap(true);
		public.checkForDisabledButtons(parameterRanges);
	}

	public.checkForDisabledButtons = function (parameterRanges) {
		Object.keys(parameterRanges).each(function (o, i) {
			modifyParameter('none', o + 'Id');
		});
	}

	// rotates the graph by a given number of degrees
	public.rotateGraph = function (degrees) {
		var nodes = public.force.nodes();
		var originX = (widthCurrent / 2);
		var originY = (heightCurrent / 2);
		public.force.nodes().each(function (o, i) {
			var dx = o.x - originX;
			var dy = o.y - originY;
			var distance = Math.sqrt(dx * dx + dy * dy);
			var oldAngle = Math.degrees(Math.atan2(dy, dx));
			var newAngle = Math.radians((oldAngle + degrees) % 360);
			var newX = originX + distance * Math.cos(newAngle);
			var newY = originY + distance * Math.sin(newAngle);
			o.x = newX;
			o.y = newY;
			o.px = newX;
			o.py = newY;
		});
	}

	// Converts from degrees to radians.
	Math.radians = function (degrees) {
		return degrees * Math.PI / 180;
	};

	// Converts from radians to degrees.
	Math.degrees = function (radians) {
		return radians * 180 / Math.PI;
	};


	// Gets the list of types to show labels for
	public.getExpanededLabels = function () {
		var labelsList = {};
		$('table.labelTree input[type="checkbox"]').each(function(i, o) {
			$(o.classList).each(function(i, c) {
				labelsList[c] = $(o).is(':checked');
			});
		});
		return labelsList;
	}

	// Sets the showLabel property for every node
	public.setShowLabels = function (nodes) {
		var nameList = public.getExpanededLabels();
		var changed = false;

		nodes.each(function (o, i) {
			if (o.showLabel != nameList[assetTypes[o.type].internalName])
				changed = true;
			o.showLabel = nameList[assetTypes[o.type].internalName];
		});

		return changed;
	}

	public.setNodeDimensions = function (fast) {
		public.force.nodes().each(function (o, i) {
			try {				
				if (fast)
					o.dimensions = public.defaultDimensions;
				else
					o.dimensions = o.nodeElement.getBBox();
			} catch (e) {
				o.dimensions = public.defaultDimensions;
			}
		});
	}

	// creates the round shadows for nodes after suggesting splits
	public.createCutShadows = function (color) {
		public.force.nodes().each(function (o, i) {
			if (o.cutShadow)
				$(o.cutShadow).remove();

			var cutShadow = vis.append('circle')
				.attr('id', 'cutShadow-' + o.id)
				.attr('class', 'cutShadow')
				.attr('transform', 'translate(' + o.x + ',' + o.y + ')')
				.style('fill', color(o.cutGroup))
				.attr('r', 22);
			o.cutShadow = cutShadow[0][0];
		});
		public.reorderDOM();
	}

	// Sort all the svg elements to reorder them in the DOM (SVG has no z-index property)
	public.reorderDOM = function () {
		var selection = d3.selectAll('svg.chart > g g g').filter(':not(.selected)').filter('.selected');
		selection[0] = selection[0]
			.concat(d3.selectAll('svg.chart > g g circle.cutShadow')[0])
			.concat(d3.selectAll('svg.chart > g g line').filter(':not(.selected)')[0])
			.concat(d3.selectAll('svg.chart > g g use').filter(':not(.selected)')[0])
			.concat(d3.selectAll('svg.chart > g g g').filter(':not(.selected)')[0])
			.concat(d3.selectAll('svg.chart > g g line').filter('.selected')[0])
			.concat(d3.selectAll('svg.chart > g g use').filter('.selected')[0])
			.concat(d3.selectAll('svg.chart > g g g').filter('.selected')[0]);
		selection.order();
	};

	// Adjust the line based on a radius of the object to match the edge instead of the center
	public.targetEdge = function(source, target){
		var x1 = source.x,
			y1 = source.y,
			x2 = target.x,
			y2 = target.y,
			angle = Math.atan2(y2 - y1, x2 - x1),
			nodeRadius = public.nodeRadius["Default"];
		
			if(target.type && public.nodeRadius[target.type]) {
				nodeRadius = public.nodeRadius[target.type];
			}

		return { x: x2 - Math.cos(angle) * (nodeRadius), y: y2 - Math.sin(angle) * (nodeRadius) };
	};

	public.transformElement = function (element, x, y, scale) {
		if (public.force && public.force.nodes().size() > public.GPUNodeThreshold)
			element[0][0].style.transform = 'translate3d(' + x + 'px, ' + y + 'px, 0px)' + ' scale3d(' + scale + ', ' + scale + ', 1)'
		else
			element[0][0].style.transform = 'translate(' + x + 'px, ' + y + 'px)' + ' scale(' + scale + ')'
		
		if (public.isIE())
			vis.style('line-height', Math.random())
		
	}
	
	// returns true if the user is on ie
	public.isIE = function () {
		if (navigator.appName == 'Microsoft Internet Explorer')
			return true
		if (navigator.appName == 'Netscape' && navigator.userAgent.indexOf('Trident') != -1)
			return true
		return false
	}
	
	// zooms in or out depending on direction
	performZoom = function (direction) {
		if (zoomBehavior && svgContainer) {
			var screenModifier = -0.5
			var modifier = 0.5
			if (direction == 'in') {
				screenModifier = 1
				modifier = 2
			}
			
			var newX = zoomBehavior.translate()[0] * modifier - (widthCurrent / 2) * screenModifier
			var newY = zoomBehavior.translate()[1] * modifier - (heightCurrent / 2) * screenModifier
			var newTranslate = [newX, newY]
			var newScale = zoomBehavior.scale() * modifier
			
			zoomBehavior
				.scale(newScale)
				.translate(newTranslate)
				.event(svgContainer)
		}
	}
	public.zoomIn = function () {
		performZoom('in')
	}
	public.zoomOut = function () {
		performZoom('out')
	}
	
	// return the public object to make the public functions accessable
	return public;

})(jQuery); //passed 'jQuery' global variable into local parameter '$'