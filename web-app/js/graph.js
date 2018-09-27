/*
 * Javascript functions used by the graph view utilizing d3
 */

var GraphUtil = (function ($) {

	// private constants
	const KEY_CODES = {
		LEFT: KeyEvent.DOM_VK_LEFT,
		UP: KeyEvent.DOM_VK_UP,
		RIGHT: KeyEvent.DOM_VK_RIGHT,
		DOWN: KeyEvent.DOM_VK_DOWN,
		MINUS: KeyEvent.DOM_VK_DASH,
		PLUS: KeyEvent.DOM_VK_EQUALS,
		RETURN: KeyEvent.DOM_VK_RETURN,
		ENTER: KeyEvent.DOM_VK_ENTER
	}
	const ARROW_KEYS = [KEY_CODES.LEFT, KEY_CODES.UP, KEY_CODES.RIGHT, KEY_CODES.DOWN]
	const ZOOM_KEYS = [KEY_CODES.MINUS, KEY_CODES.PLUS]
	const SUBMIT_TEXT_KEYS = [KEY_CODES.RETURN, KEY_CODES.ENTER]
	const IGNORE_KEY_EVENT_TAGS = ['INPUT', 'TEXTAREA']

	// public functions
	var public = {};

	// public constants
	public.NO_TRANSFORM = 'translate(0 0)scale(1)';
	public.NO_TRANSFORM_CSS = 'translate(0px 0px) scale(1)';

	// public member variables
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
	public.zoomPercentage = 0.25;
	public.nodeRadius = {'Default': 28, 'Server': 29, 'Database': 27, 'Files': 28, 'Other': 29, 'Application': 26, 'VM': 25};
	public.defaultDimensions = {'width': 28, 'height': 28};
	public.lastHighlightSearch = null;

	// ############################################################## graph UI functions ##############################################################

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

	// gets the height of the page footer
	public.getFooterHeight = function () {
		var footerHeight = $('footer.main-footer').outerHeight();
		return footerHeight ? footerHeight : 0;
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
		public.correctPanelSize('controlPanelId');
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

	// adds the move bundle color indicator to the legend
	public.updateLegendColorKey = function (dataMap, colors, fillMode) {
		var template = $('#colorKeyTemplateId');
		$('.colorKey').remove();
		var dataList = Object.keys(dataMap)
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


	// gets the current value of the colorBy control
	public.getFillMode = function () {
		return $('#colorBySelectId').val();
	}

	// sets the current value of the colorBy control
	public.setFillMode = function (fillMode) {
		$('#colorBySelectId').val(fillMode);
	}


	// if the user's browser doesn't support SVG, replace the graph with an error message
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


	// hides the specified panel ('control' or 'legend')
	public.hidePanel = function (panel) {
		if (panel == 'control') {
			$('#controlPanelId').removeClass('openPanel');
			$('#controlPanelTabId').removeClass('activeTab');
		} else if (panel == 'legend') {
			$('#legendDivId').removeClass('openPanel');
			$('#legendTabId').removeClass('activeTab');
		}
	}

	// opens the specified panel ('control' or 'legend')
	public.openPanel = function (panel) {
		if (panel == 'control') {
			$('#controlPanelId').addClass('openPanel');
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

	// populates the team select
	public.populateTeamSelect = function (data) {
		// get the select element and clear whatever options were in it before
		teamSelect = $("#teamSelectId");
		teamSelect.children('.teamOption').remove();

		// add the default values
		teamSelect.append('<option value="ALL">All Teams</option>');
		teamSelect.append('<option value="NONE">No Team Assignment</option>');
		teamSelect.append('<option disabled>──────────</option>');
		teamSelect.val('ALL');

		// add all the roles from the data
		$.each(data.roles, function (index, team) {
			teamSelect.append('<option value="' + team + '">' + team + '</option>');
		});

		return teamSelect;
	}


	// returns true if the graph is frozen
	public.isFrozen = function () {
		return $('#playPauseButtonId').hasClass('enabled');
	}

	// freezes the graph
	public.enableFreeze = function () {
		$('#playPauseButtonId')
			.addClass('enabled')
			.attr('value', 'Resume Graph');
		public.force.stop();
	}

	// resumes the graph
	public.disableFreeze = function () {
		$('#playPauseButtonId')
			.removeClass('enabled')
			.attr('value', 'Freeze Graph');
		public.force.resume();
	}

	// toggles whether the graph is frozen
	public.toggleFreeze = function () {
		if (public.isFrozen()) {
			public.disableFreeze();
		} else {
			public.enableFreeze();
		}
	}

	// ############################################################## graph data and control functions ##############################################################

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

	// performs one tick of the force layout
	public.tickOnce = function () {
		var oldAlpha = public.force.alpha();
		public.force.alpha(1);
		public.force.tick();
		public.force.alpha(oldAlpha);
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

	// generates the preference value for the current legend twistie state
	public.serializeLegendTwistiePrefs = function () {
		var twisties = $('#legendDivId #twistieSpanId'); // '#' means ONE! ONLY ONE!
		var pref = [];
		for (var i = 0; i < twisties.length; ++i)
			if ($(twisties[i]).hasClass('open'))
				pref.push($(twisties[i]).attr('groupType'));
		return pref.join(',');
	}


	// sets the class list for every node in the graph
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
	// sets the class list for every link in the graph
	public.updateLinkClasses = function () {
		public.linkBindings.attr("class", function(d) {
			return 'link'
				+ ((d.selected == 1) ? ' selected' : '')
				+ ((d.unresolved && !d.partOfCycle) ? ' unresolved' : '')
				+ ((d.notApplicable && !d.partOfCycle) ? ' notApplicable' : '')
				+ ((d.future && !d.partOfCycle) ? ' future' : '')
				+ ((d.validated && !d.partOfCycle) ? ' validated' : '')
				+ ((d.questioned && !d.partOfCycle) ? ' questioned' : '')
				+ ((d.cut) ? ' cut' : '')
				+ ((d.root) ? ' root' : '')
				+ ((public.isConflictsEnabled() && d.bundleConflict) ? ' bundleConflict' : '')
				+ ((public.isHighlightCyclesEnabled() && d.partOfCycle) ? ' cyclical' : '')
				+ ((public.isBlackBackground()) ? ' blackBackground' : '')
				+ public.getFilteredClass(d)
		});
	}
	// sets the class list for every label in the graph
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

	// updates the class list for ever graph element
	public.updateAllClasses = function () {
		public.updateNodeClasses();
		public.updateLinkClasses();
		public.updateLabelClasses();
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


	// sets the alpha if the graph is not frozen
	public.setAlpha = function (alpha) {
		if ( ! public.isFrozen() )
			public.force.alpha(alpha);
		else
			return false;
		return true;
	}

	// unfreezes the graph and starts the force layout
	public.startForce = function () {
		public.disableFreeze();
		public.force.start();
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

	// creates an SVG rect DOM element using pure js
	public.createRect = function (x, y, h, w) {
		var svgNS = "http://www.w3.org/2000/svg",
			rect = document.createElementNS(svgNS, 'rect');
		rect.setAttributeNS(null, 'x', x);
		rect.setAttributeNS(null, 'y', y);
		rect.setAttributeNS(null, 'height', h);
		rect.setAttributeNS(null, 'width', w);

		return rect;
	}

	// sets the given node to its proper position
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

	// sets the dimensions for this node based on its icon
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
	public.targetEdge = function (source, target) {
		var x1 = source.x,
		    y1 = source.y,
		    x2 = target.x,
		    y2 = target.y,
		    angle = Math.atan2(y2 - y1, x2 - x1),
		    nodeRadius = public.nodeRadius["Default"];

		if (target.type && public.nodeRadius[target.type])
			nodeRadius = public.nodeRadius[target.type];

		return { x: x2 - Math.cos(angle) * (nodeRadius), y: y2 - Math.sin(angle) * (nodeRadius) };
	};


	// ############################################################## graph transform functions ##############################################################

	// sets the transform for a given element
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

	// zooms in or out depending on direction
	performZoom = function (direction, modifier) {
		if (zoomBehavior && transformContainer) {
			// determine the multipliers that will be used for the transformation
			modifier = (modifier != null) ? modifier : 1
			var zoomMultiplier = (1 - public.zoomPercentage) / modifier
			var screenModifier = zoomMultiplier - 1
			if (direction == 'in') {
				zoomMultiplier = 1 / zoomMultiplier
				screenModifier = zoomMultiplier - 1
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

	// adjusts the scale and translate of the specified element using an animated transition
	public.animateTransform = function (translate, scale, transformElement) {
		// determine the transformation parameters
		translate = (translate != null) ? translate : zoomBehavior.translate()
		scale = (scale != null) ? scale : zoomBehavior.scale()
		transformElement = (transformElement != null) ? transformElement : transformContainer

		// perform the transform, only animating the transition if an SVG element is used
		if (transformElement[0][0].tagName == 'DIV')
			transformElement.style('transform', transformString(translate[0], translate[1], scale, 'px'))
		else
			transformElement
				.transition()
				.duration(100)
				.ease(function (t) {
					return Math.min(t, 1)
				})
				.attr('transform', transformString(translate[0], translate[1], scale))
	}

	// zooms in or out of the timeline, calling displayCallback when finished
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

	// ############################################################## key binding functions ##############################################################

	// add key listeners for zooming and panning
	public.addKeyListeners = function (modifier) {
		$(window).on('keydown', function (e) {
			// ignore keystrokes while the user is typing in an text field
			if ( ! IGNORE_KEY_EVENT_TAGS.contains(e.target.tagName) ) {
				// handle modifier keys
				var modifier = 1
				if (e.shiftKey)
					modifier = 2
				if (e.ctrlKey)
					modifier = 0.5

				// perform action based on key code
				switch (e.keyCode) {
					case KEY_CODES.LEFT:  public.translateLeft(modifier); break;
					case KEY_CODES.RIGHT: public.translateRight(modifier); break;
					case KEY_CODES.UP:    public.translateUp(modifier); break;
					case KEY_CODES.DOWN:  public.translateDown(modifier); break;
					case KEY_CODES.PLUS:  performZoom('in', modifier); break;
					case KEY_CODES.MINUS: performZoom('out', modifier); break;
				}
			}
		})
	}

	// add key listeners for zooming and panning
	public.addTimelineKeyListeners = function (brush, x1, mainTranslator, displayCallback) {
		$(window).on('keydown', function (e) {
			// ignore keystrokes while the user is typing in an text field
			if ( ! IGNORE_KEY_EVENT_TAGS.contains(e.target.tagName) ) {
				var key = e.keyCode

				// handle modifier keys
				var modifier = 1
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
				switch (key) {
					case KEY_CODES.LEFT:  newRange = [t1 - offset, t2 - offset]; break;
					case KEY_CODES.RIGHT: newRange = [t1 + offset, t2 + offset]; break;
					case KEY_CODES.PLUS:  newRange = [t1 + offset, t2 - offset]; break;
					case KEY_CODES.MINUS: newRange = [t1 - (offset * 2), t2 + (offset * 2)]; break;
				}

				var delay = 0
				brush.extent([new Date(newRange[0]), new Date(newRange[1])])

				// if we are translating perform an animated transition
				if (ARROW_KEYS.contains(key)) {
					var xTranslate = -1 * x1(new Date(newRange[0]))
					public.animateTransform([xTranslate, 0], 1, mainTranslator)
					delay = 150
				}

				// call the display callback function (after a delay if translating to allow the animation time to play)
				window.setTimeout(displayCallback, delay, true)
			}
		})
	}

	// ############################################################## filter highlighting functions ##############################################################

	// searches only when the user presses enter in the search box
	public.handleSearchKeyEvent = function (e, searchFunction) {
		if (SUBMIT_TEXT_KEYS.contains(e.keyCode)) {
			if (searchFunction)
				window.setTimeout(searchFunction, 1)
			else
				window.setTimeout(public.performSearch, 1)
		}
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
				url: tdsCommon.createAppURL('/ws/depAnalyzer/filteredAssetList'),
				// asynchronous: true,
				data: {'nameFilter':nameFilter, 'isRegex':isRegex, 'personId':personFilter},
				cache: false,
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

	// ############################################################## misc functions ##############################################################

	// returns true if the user is on ie
	public.isIE = function () {
		if (navigator.appName == 'Microsoft Internet Explorer')
			return true
		if (navigator.appName == 'Netscape' && navigator.userAgent.indexOf('Trident') != -1)
			return true
		return false
	}

	// forces a browser reflow on the specified element
	public.forceReflow = function (element) {
		element.style('line-height', Math.random())
	}

	// Converts from degrees to radians.
	Math.radians = function (degrees) {
		return degrees * Math.PI / 180;
	};

	// Converts from radians to degrees.
	Math.degrees = function (radians) {
		return radians * 180 / Math.PI;
	};

	// constructs a string for
	function transformString (x, y, scale, unit) {
		unit = unit ? unit : ''
		return 'translate(' + x + unit + ',' + y + unit  + ')scale(' + scale + ')'
	}

	// return the public object to make the public functions accessable
	return public;

})(jQuery); //passed 'jQuery' global variable into local parameter '$'