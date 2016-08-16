/*
 * Javascript functions used by the graph view utilizing d3
 */

var GraphUtil = (function ($) {
	
	// private constants
	const KEY_CODES = {
		LEFT: 37,
		UP: 38,
		RIGHT: 39,
		DOWN: 40,
		MINUS: 189,
		PLUS: 187
	}
	const DIRECTIONS = [KEY_CODES.LEFT, KEY_CODES.UP, KEY_CODES.RIGHT, KEY_CODES.DOWN]
	const ZOOM_KEYS = [KEY_CODES.MINUS, KEY_CODES.PLUS]
	
	// public functions
	var public = {};

	// public variables
	public.force = null;
	public.nodeBindings = null;
	public.linkBindings = null;
	public.labelBindings = null;
	public.labelTextBindings = null;
	public.labelTextBackgroundBindings = null;
	public.shapeOffset = -20;
	public.labelShapeOffset = -8;
	public.GPUNodeThreshold = 500;
	public.labelHeightDefault = 15; // Modify if the size of the text from the rect is different.
	public.translateDist = 200;
	public.nodeRadius = {'Default': 28, 'Server': 29, 'Database': 27, 'Files': 28, 'Other': 29, 'Application': 26, 'VM': 25};
	public.defaultDimensions = {'width': 28, 'height': 28};
	public.lastHighlightSearch = null;
	
	// returns true if the graph is loaded
	public.graphExists = function () {
		return ($('#svgContainerId svg').size() > 0)
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
		if (panel.size() == 0 || svgContainer.size() == 0 || svgContainer.children().size() == 0)
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
		var dataList = Object.keys(dataMap)
		//if (dataList.length == undefined)
		//	dataList = Object.keys(dataMap)
		$(dataList).each(function (i, o) {
			var newRow = template.clone();
			newRow
				.addClass('colorKey')
				.removeClass('hidden');
			var node = newRow.children('.bundleColorExample').children().children();
			node.css('fill', colors(i));
			var label = newRow.children('.bundleNameLabel');
			label.html(dataMap[o]);
			$('#colorGroupingTableId').append(newRow);
		});
		$('#colorKeyLabelId').removeClass('hidden')
		$('#colorKeyLabelId p').html(colorByGroupLabels[fillMode] + 's');
	}

	public.getFillMode = function () {
		return $('#colorBySelectId').val();
	}

	public.setFillMode = function (fillMode) {
		$('#colorBySelectId').val(fillMode);
	}

	public.getFillColor = function (node, colors, fillMode) {
		colors(0) // I have no idea why but for some reason including this line prevents the colors from being assigned incorrectly
		var group = Object.keys(colorByGroups[fillMode])
		var nodeVal = node.colorByProperties[fillMode].toString()
		var groupIndex = group.indexOf(nodeVal)
		return colors(groupIndex)
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
		
		// update the legend twistie preference if applicable
		if (twistieSpan.parents('#legendDivId').length > 0 && twistieSpan.parents('.tabInner').length > 0) {
			var prefValue = public.serializeLegendTwistiePrefs();
			setUserPreference('legendTwistieState', prefValue);
		}
	}
	
	// generates the prefernce value for the current legend twistie state
	public.serializeLegendTwistiePrefs = function () {
		var twisties = $('#legendDivId #twistieSpanId')
		var pref = ''
		for (var i = 0; i < twisties.length; ++i)
			if ($(twisties[i]).hasClass('open'))
				pref = pref + '1'
			else
				pref = pref + '0'
		return pref
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
				+ ((public.isBlackBackground()) ? ' blackBackground' : '')
				+ public.getFilteredClass(d)
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
				+ ((public.isBlackBackground()) ? ' blackBackground' : '')
				+ public.getFilteredClass(d)
		});
	}
	public.updateLabelClasses = function () {
		var bundle = public.getFilteredBundle();
		public.labelBindings.attr("class", function(d) {
			
			if (d.highlighted == 'y')
				$(this).children().attr('dy', '0.36em')
			else
				$(this).children().attr('dy', '0.35em')
			return 'label'
				+ ((d.selected > 0) ? ' selected' : '')
				+ ((public.isBlackBackground()) ? ' blackBackground' : '')
				+ ((public.isBundleFilterEnabled() && ! public.isInFilteredBundle(d, bundle)) ? ' filtered' : '')
				+ ((! d.showLabel) ? ' hidden' : '')
				+ public.getFilteredClass(d)
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
		$('table.labelTree tr.labelToggleRow input[type="checkbox"]').each(function(i, o) {
			var classList = o.classList
			if (classList == null)
				classList = o.className.split(' ')
			$(classList).each(function(i, c) {
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
			// get the cut shadow object (create it if it doesn't exist)
			var cutShadow = o.cutShadow
			if (cutShadow == null) {
				cutShadow = vis.append('circle')
					.attr('id', 'cutShadow-' + o.id)
					.attr('class', 'cutShadow')
					.attr('r', 22)
				o.cutShadow = cutShadow[0][0]
			} else {
				cutShadow = d3.select(o.cutShadow)
			}
			
			// syle it based on highlighting and cut group
			cutShadow.style('opacity', null)
			if (o.highlighted == 'y')
				cutShadow.style('fill', '#ff0000')
			else if (o.highlighted == 'n' || o.cutGroup == -1)
				cutShadow.style('opacity', 0)
			else
				cutShadow.style('fill', color(o.cutGroup))
			
			// apply the basic attributes
			cutShadow.attr('transform', 'translate(' + o.x + ',' + o.y + ')')
		});
		public.reorderDOM();
	}

	// Sort all the svg elements to reorder them in the DOM (SVG has no z-index property)
	public.reorderDOM = function () {
		var selection = d3.selectAll('svg.chart > g g g').filter(':not(.selected)').filter('.selected');

		var cutShadows = d3.selectAll('svg.chart > g g circle.cutShadow')
		var lines = d3.selectAll('svg.chart > g g line')
		var nodes = d3.selectAll('svg.chart > g g use')
		var labels = d3.selectAll('svg.chart > g g g')
		var groups = [cutShadows, lines, nodes, labels]
		var filters = [':not(.hl):not(.selected)', ':not(.hl).selected', '.hl:not(.selected)', '.hl.selected']
		
		for (var g = 0; g < groups.length; ++g)
			for (var f = 0; f < filters.length; ++f)
				selection[0] = selection[0].concat(groups[g].filter(filters[f])[0])
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
		var transform = 'translate(' + x + 'px, ' + y + 'px)' + ' scale(' + scale + ')'
		if (public.force && public.force.nodes().size() > public.GPUNodeThreshold)
			transform = 'translate3d(' + x + 'px, ' + y + 'px, 0px)' + ' scale3d(' + scale + ', ' + scale + ', 1)'
		
		element[0][0].style.transform = transform
		if (public.isIE()) {
			element[0][0].style['-ms-transform'] = transform
			public.forceReflow(vis)
		}
		
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
	performZoom = function (direction, modifier) {
		if (zoomBehavior && transformContainer) {
			modifier = (modifier != null) ? modifier : 1
			var screenModifier = -0.5
			var zoomMultiplier = 0.5 / modifier
			if (direction == 'in') {
				screenModifier = 1 * modifier
				zoomMultiplier = 1 / zoomMultiplier
			}
			
			var newX = zoomBehavior.translate()[0] * zoomMultiplier - (widthCurrent / 2) * screenModifier
			var newY = zoomBehavior.translate()[1] * zoomMultiplier - (heightCurrent / 2) * screenModifier
			var newTranslate = [newX, newY]
			var newScale = zoomBehavior.scale() * zoomMultiplier
			
			var zoomEvent = zoomBehavior
				.scale(newScale)
				.translate(newTranslate)
			public.animateTransform(newTranslate, newScale)
		}
	}
	public.zoomIn = function () {
		performZoom('in')
	}
	public.zoomOut = function () {
		performZoom('out')
	}
	
	// translates the view by the specified x and y
	performTranslate = function (x, y) {
		var newTranslate = zoomBehavior.translate()
		newTranslate[0] += x
		newTranslate[1] += y
		
		zoomBehavior.translate(newTranslate)
		public.animateTransform(newTranslate)
	}
	public.translateLeft = function (modifier) {
		modifier = (modifier != null) ? modifier : 1
		performTranslate(public.translateDist * modifier, 0)
	}
	public.translateRight = function (modifier) {
		modifier = (modifier != null) ? modifier : 1
		performTranslate(-1 * public.translateDist * modifier, 0)
	}
	public.translateUp = function (modifier) {
		modifier = (modifier != null) ? modifier : 1
		performTranslate(0, public.translateDist * modifier)
	}
	public.translateDown = function (modifier) {
		modifier = (modifier != null) ? modifier : 1
		performTranslate(0, -1 * public.translateDist * modifier)
	}
	public.animateTransform = function (transform, scale, transformElement) {
		transform = (transform != null) ? transform : zoomBehavior.transform()
		scale = (scale != null) ? scale : zoomBehavior.scale()
		transformElement = (transformElement != null) ? transformElement : transformContainer
		if (transformElement[0][0].tagName == 'DIV')
			transformElement.style('transform', 'translate(' + transform[0] + 'px,' + transform[1] + 'px) scale(' + scale + ')')
		else
			transformElement
				.transition()
				.duration(100)
				.ease(function (t) {
					return Math.min(t, 1)
				})
				.attr('transform', 'translate(' + transform[0] + ',' + transform[1] + ')scale(' + scale + ')')
	}
	
	// add key listeners for zooming and panning
	public.addKeyListeners = function (graph, modifier) {
		modifier = (modifier != null) ? modifier : 1
		graph.on('keydown', function (e) {
			var key = e.keyCode
			console.log('KEYDOWN - ' + key)
			
			// handle modifier keys
			if (e.shiftKey)
				modifier = 2
			if (e.ctrlKey)
				modifier = 0.5
			
			// perform action based on key code
			if (key == KEY_CODES.LEFT) {
				public.translateLeft(modifier)
			} else if (key == KEY_CODES.RIGHT) {
				public.translateRight(modifier)
			} else if (key == KEY_CODES.UP) {
				public.translateUp(modifier)
			} else if (key == KEY_CODES.DOWN) {
				public.translateDown(modifier)
			} else if (key == KEY_CODES.PLUS) {
				performZoom('in', modifier)
			} else if (key == KEY_CODES.MINUS) {
				performZoom('out', modifier)
			}
		}).focus()
	}
	
	// add key listeners for zooming and panning
	public.timelineZoom = function (brush, direction, displayCallback) {
		var t1 = brush.extent()[0].getTime()
		var t2 = brush.extent()[1].getTime()
		var rangeSize = t2 - t1
		var offset = (rangeSize / 4)
		var newRange = [t1 - (offset * 2), t2 + (offset * 2)]
		if (direction == 'in')
			newRange = [t1 + offset, t2 - offset]
		
		brush.extent([new Date(newRange[0]), new Date(newRange[1])]);
		displayCallback(true)
	}
	
	// add key listeners for zooming and panning
	public.addTimelineKeyListeners = function (graph, brush, x1, mainTranslator, displayCallback) {
		var modifier = 1
		graph.on('keydown', function (e) {
			var key = e.keyCode
			
			// handle modifier keys
			if (e.shiftKey)
				modifier = 2
			if (e.ctrlKey)
				modifier = 0.5
			
			var t1 = brush.extent()[0].getTime()
			var t2 = brush.extent()[1].getTime()
			var rangeSize = t2 - t1
			var offset = (rangeSize / 4) * modifier
			var newRange = [t1, t2]
			
			// perform action based on key code
			if (DIRECTIONS.contains(key)) {
				if (key == KEY_CODES.LEFT)
					newRange = [t1 - offset, t2 - offset]
				if (key == KEY_CODES.RIGHT)
					newRange = [t1 + offset, t2 + offset]
				
				brush.extent([new Date(newRange[0]), new Date(newRange[1])]);
				var xTranslate = -1 * x1(new Date(newRange[0]))
				public.animateTransform([xTranslate, 0], 1, mainTranslator)
				window.setTimeout(displayCallback, 150, true)
			} else {
				if (key == KEY_CODES.PLUS)
					newRange = [t1 + offset, t2 - offset]
				if (key == KEY_CODES.MINUS)
					newRange = [t1 - (offset * 2), t2 + (offset * 2)]
				
				brush.extent([new Date(newRange[0]), new Date(newRange[1])]);
				displayCallback(true)
			}
			
			
		}).focus()
	}
	
	// searches only when the user presses enter in the search box
	public.handleSearchKeyEvent = function (e) {
		if (e.keyCode == 13)
			window.setTimeout(public.performSearch, 1)
	}
	
	// highlight tasks matching the user's regex
	public.performSearch = function () {
		
		// read the filter settings from the DOM
		var personFilter = $('#personHighlightSelectId').data('kendoComboBox').value();
		var nameFilter = $('#searchBoxId').val();
		var hasSlashes = (nameFilter.length > 0) && (nameFilter.charAt(0) == '/' && nameFilter.charAt(nameFilter.length-1) == '/');
		var isRegex = false;
		
		// if the current filter set is identical to the previous one, don't perform a search
		var highlightObject = personFilter + '_' + nameFilter;
		if (highlightObject == public.lastHighlightSearch)
			return;
		else
			public.lastHighlightSearch = highlightObject;
		
		// determine whether the "clear filter" icons should be usable
		if (nameFilter != '')
			$('#filterClearId').attr('class', 'ui-icon ui-icon-closethick');
		else
			$('#filterClearId').attr('class', 'disabled ui-icon ui-icon-closethick');
		if (personFilter != '')
			$('#clearPersonFilterId').removeClass('disabled');
		else
			$('#clearPersonFilterId').addClass('disabled');
		
		// if there is no filter, unhighlight everything
		if (personFilter == '' && nameFilter == '') {
			public.applyHighlights(null);
			return;
		}
		
		// check if the user entered an invalid regex
		var regex = /.*/
		if (hasSlashes) {
			try {
				nameFilter = nameFilter.substring(1, nameFilter.length-1);
				regex = new RegExp(nameFilter);
				isRegex = _.isRegExp(regex);
			} catch (e) {
				alert(e);
				nameFilter = '';
			}
		}
		
		if (personFilter != '') {
			// get the list of assets to highlight from the server
			$.ajax({
				url: '/tdstm/assetEntity/getFilteredDepGraph',
				asynchronous: true,
				data: {'nameFilter':nameFilter, 'isRegex':isRegex, 'personFilter':personFilter},
				complete: function (response) {
					var highlightList = JSON.parse(response.responseText);
					public.applyHighlights(highlightList);
				}
			});
		} else {
			// highlight using locally stored data
			var highlightList = [];
			for (var i = 0; i < nodes.length; ++i) {
				var node = nodes[i];
				var name = node.name;
				
				var nameMatches = false;
				if (isRegex && name.match(regex) != null)
					nameMatches = true;
				else if (!isRegex && name.toLowerCase().indexOf(nameFilter.toLowerCase()) != -1)
					nameMatches = true;
				
				if (nameMatches)
					highlightList.push(node.id);
			}
			
			public.applyHighlights(highlightList);
		}
		
		return false;
	}
	
	// update the DOM according to a given a list of assets to highlight
	public.applyHighlights = function (highlightAssets) {
		var nodes = public.force.nodes();
		for (var i = 0; i < nodes.length; ++i) {
			var node = nodes[i];
			if (highlightAssets == null)
				node.highlighted = 'x'
			else if (highlightAssets.indexOf(node.id) != -1)
				node.highlighted = 'y'
			else
				node.highlighted = 'n'
			
		}
		public.updateAllClasses();
		public.createCutShadows(fill);
	}
	
	// removes the value in the source filter field and performs a new search
	public.clearFilter = function (source) {
		if (source == 'text' || source == 'all')
			$('#searchBoxId').val('');
		if (source == 'person' || source == 'all')
			$('#personHighlightSelectId').data("kendoComboBox").select(-1);
		public.performSearch();
	}
	
	// gets the highlight class for this object based on its properties
	public.getFilteredClass = function (obj) {
		if (obj.linkElement) {
			var parent = obj.source ? obj.source : obj.parent
			var child = obj.target ? obj.target : obj.child
			if (parent.highlighted == 'y' || child.highlighted == 'y')
				return ' hl'
			else if (parent.highlighted == 'n' || child.highlighted == 'n')
				return ' nohl'
			else
				return ''
		} else {
			if (obj.highlighted == 'y')
				return ' hl'
			else if (obj.highlighted == 'n')
				return ' nohl'
			else
				return ''
		}
	}
	
	// opens or closes the submenu for the highlighting feature
	public.toggleHighlightDropdown = function () {
		$('#filterOptionsMenuId').toggleClass('open')
		$('#filterOptionsButtonId').toggleClass('open')
	}
	
	// forces a browser reflow on the specified element
	public.forceReflow = function (element) {
		element.style('line-height', Math.random())
	}

	// return the public object to make the public functions accessable
	return public;

})(jQuery); //passed 'jQuery' global variable into local parameter '$'
