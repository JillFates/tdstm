/* adds a static constant with the given identifier to a "class" (target) with a given value.
 * this is kind of a hack but currently I don't know of any better way to define static constants in javascript. */
function addStaticConstant (target, identifier, value) {
	Object.defineProperty(target, identifier, {
		configurable: false,
		enumerable: false,
		writable: false,
		value: value
	})
}

// represents a point in the graph space
class Point {
	constructor (x, y) {
		this.x = x
		this.y = y
	}
	update (newX, newY) {
		this.x = newX
		this.y = newY
	}
	copy () {
		return new Point(this.x, this.y)
	}
	inverted () {
		return new Point(this.x,-1 * this.y)
	}
	salt () {
		this.x = GraphUtil.saltValue(this.x)
		this.y = GraphUtil.saltValue(this.y)
	}
	salted () {
		var newPoint = new Point(this.x, this.y)
		newPoint.salt()
		return newPoint
	}
	distanceTo (other) {
		if (other instanceof GraphPoint)
			return this.distanceTo(other.graphPos)
		else
			return GraphUtil.getDistance(this.x, this.y, other.x, other.y)
	}
	getRasterization () {
		let tl = zoomBehavior.translate()
		let zoom = zoomBehavior.scale()
		let newX = this.x + (tl[0] * zoom)
		let newY = this.y + (tl[1] * zoom)
	}
	toString () {
		return '(' + this.x + ',' + this.y + ')'
	}
}

// same as a point but it keeps track of both the rasterized (gloabal) position and the internal graph (local) position
class GraphPoint {
	constructor (screenX, screenY) {
		this.screenPos = new Point()
		this.graphPos = new Point()
		this.setScreenCoordinates(screenX, screenY)
	}
	copy () {
		return new GraphPoint(this.screenPos.x, this.screenPos.y)
	}
	// sets the x and y graph coordinates of the point then calculates the screen coordinates based on that
	setGraphCoordinates (coordinates) {
		if (coordinates instanceof Point)
			this.setGraphCoordinates(coordinates.x, coordinates.y)
		else if (coordinates.length > 1)
			this.setGraphCoordinates(coordinates[0], coordinates[1])
	}
	setGraphCoordinates (x, y) {
		this.graphPos.update(x, y)
		this.screenPos = this.rasterize(new Point(x,y))
	}
	// sets the x and y screen coordinates of the point then calculates the graph coordinates based on that
	setScreenCoordinates (coordinates) {
		if (coordinates instanceof Point)
			this.setScreenCoordinates(coordinates.x, coordinates.y)
		else if (coordinates.length > 1)
			this.setScreenCoordinates(coordinates[0], coordinates[1])
	}
	setScreenCoordinates (x, y) {
		this.screenPos.update(x, y)
		this.graphPos = this.derasterize(new Point(x,y))
	}
	rasterize (p) {
		let tf = this.getTransformData()
		var raster = p.copy()
		raster.x = (p.x * tf.zoom) + tf.tl[0]
		raster.y = (p.y * tf.zoom) + tf.tl[1]
		return raster

	}
	derasterize (p) {
		let tf = this.getTransformData()
		var deraster = p.copy()
		deraster.x = (p.x - tf.tl[0]) / tf.zoom
		deraster.y = (p.y - tf.tl[1]) / tf.zoom
		return deraster

	}
	update () {
		setScreenCoordinates(this.screenPos)
	}
	getTransformData () {
		return {
			tl: zoomBehavior.translate(),
			zoom: zoomBehavior.scale()
		}
	}
	toString () {
		return '{' + this.screenPos.toString() + '-->' + this.graphPos.toString() + '}'
	}
}

// represents a node in the force graph
class GraphNode extends Point {
	constructor (params) {
		super(0,0)
		this.insideRegion = false
		// set all the members sent from the server using the parameters list
		if (params)
			for (let prop of GraphNode.INITIAL_PARAMS)
				this[prop] = params[prop]
	}
}
addStaticConstant(GraphNode, 'INITIAL_PARAMS', ['id','name','type','depBundleId','moveBundleId','moveEventId','shape','size','title','color','dependsOn','supports','assetClass','cutGroup','colorByProperties'])

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
		ENTER: KeyEvent.DOM_VK_ENTER,
		SPACE: KeyEvent.DOM_VK_SPACE,
		SHIFT: KeyEvent.DOM_VK_SHIFT,
		CTRL: KeyEvent.DOM_VK_CONTROL
	}
	const ARROW_KEYS = [KEY_CODES.LEFT, KEY_CODES.UP, KEY_CODES.RIGHT, KEY_CODES.DOWN]
	const ZOOM_KEYS = [KEY_CODES.MINUS, KEY_CODES.PLUS]
	const SUBMIT_TEXT_KEYS = [KEY_CODES.RETURN, KEY_CODES.ENTER]
	const IGNORE_KEY_EVENT_TAGS = ['INPUT', 'TEXTAREA']
	const KEY_STATE_DOWN = 1
	const KEY_STATE_UP = 2
	const SALT_MULTIPLIER = 0.0000001

	// public functions
	var public = {};

	// public constants and enums
	public.NO_TRANSFORM = 'translate(0 0)scale(1)';
	public.NO_TRANSFORM_CSS = 'translate(0px 0px) scale(1)';
	const PANELS = {
		CONTROL: 1,
		DEPENDENCY: 2,
		LEGEND: 3,
		NONE: 4
	}
	public.PANELS = PANELS; // represents the options for which panel is currently openned
	const SELECT_MODES = {
		ADD: 1,
		SUB: 2,
		REPLACE: 3,
		REPLACE_NO_TOGGLE: 4 ,
		TOGGLE: 5
	}
	public.SELECT_MODES = SELECT_MODES; // represents the diferent modes that are used internally as arguments for the selection function
	const SELECTION_STATES = {
		NOT_SELECTED: 0,
		SELECTED_SECONDARY: 1,
		SELECTED_PRIMARY: 2
	}
	public.SELECTION_STATES = SELECTION_STATES // represents the selection state of any given node or link

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
	public.selectionElements = {
		layer: null,
		regionPath: null,
		regionProjection: null
	} // stores the svg elements associated with the region selection feature
	public.selectionPathString = ''; // the svg path 'd' attribute string for the current selection path
	public.tempPathString = ''; // the svg path 'd' attribute string for the current selection path including the current temporary point
	var lastSelectionPoint; // the last point selected, this is a helper variable
	public.selectionPath = []; // the list of points in the current selection path
	public.preselectionList = []; // stores the nodes that were already selected before a region select began
	public.SELECTION_MIN_EDGE_LENGTH = 20; // the minimum distance between stored points in a selection path, lower values increase path resolution but decrease speed
	public.realtimeSelectionHighlighting = false; // set to true if the region select should highlight selected nodes in real time as it is being drawn
	// Stored only on the current page session
	public.dependencyPanelConfig = {
		dependencyStatus: {
			dirty: false,
			status: 'true',
			show: [],
			highlight: [],
			groupingControl: []
		},
		dependencyType: {
			dirty: false,
			status: 'true',
			show: [],
			highlight: [],
			groupingControl: []
		}
	};
	public.LASSO_TOOL = 1; // constant to represent the lasso tool
	public.SELECTION_ADD_TOOL = 2; // constant to represent the selection add tool
	// the current state of each tool
	public.toolStates = {
		lasso: false,
		selectionAdd: false
	}


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
			public.correctPanelSizes();
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

		public.correctActionButtons();
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

	// sets the size of the Dependency Panel so that it can scroll when longer than the user's window
	public.correctDependenciesPanelSize = function () {
		public.correctPanelSize('dependenciesPanelId');
	}

	// sets the size of the legend and control panel so that they can scroll when longer than the user's window
	public.correctPanelSizes = function () {
		public.correctLegendSize();
		public.correctControlPanelSize();
		public.correctDependenciesPanelSize();
	}

	// sets the size of a panel so that it can scroll when longer than the user's window
	public.correctPanelSize = function (panelId) {
		var panel = $('#' + panelId);
		panel.css('height', '');
		panel.css('overflow-y', 'scroll');
		var svgContainer = $('#svgContainerId');
		if (panel.size() == 0 || svgContainer.size() == 0 || svgContainer.children().size() == 0)
			return false;

		var bottom = panel.offset().top + panel.height();
		var newBottom = svgContainer.offset().top + svgContainer.innerHeight();

		if (bottom >= newBottom) {
			var newHeight = newBottom - panel.offset().top;
			panel.css('height', newHeight);
		} else {
			panel.css('height', '');
			panel.css('overflow-y', '');
		}

		public.correctActionButtons();
	}

	public.correctActionButtons = function() {
		if($('#dependenciesPanelId').size() == 0)
			return
		if($('#dependenciesPanelId').has_scrollbar()) {
			$('.dependency_panel_action_buttons').css('position', 'fixed');
			$('.dependency_panel_action_buttons').css('bottom', '45px');
		} else {
			$('.dependency_panel_action_buttons').css('position', '');
			$('.dependency_panel_action_buttons').css('bottom', '5px');
		}
	};

	// called when the user clicks the show/hide layout adjustments twistie
	public.toggleGraphTwistie = function (twistieSpan) {
		var container = $('#' + twistieSpan.attr('for'));

		// update the legend twistie preference if applicable
		var isCalledFromLegendPanel = twistieSpan.parents('#legendDivId').length > 0;
		if (isCalledFromLegendPanel && twistieSpan.parents('.tabInner').length > 0) {
			var prefValue = public.serializeLegendTwistiePrefs();
			setUserPreference('legendTwistieState', prefValue);
		}

		if (twistieSpan.hasClass('closed')) {
			twistieSpan.removeClass('closed').addClass('open');
			container.slideDown(300, function () {
				if (isCalledFromLegendPanel) {
					public.correctLegendSize();
				} else {
					public.correctControlPanelSize();
				}
			});
		} else {
			twistieSpan.removeClass('open').addClass('closed');
			container.slideUp(300, function () {
				if (isCalledFromLegendPanel) {
					public.correctLegendSize();
				} else {
					public.correctControlPanelSize();
				}
			});
		}
	}


	public.toggleDependencyPanel = function(dependencyPanel, event) {
		var panelStatusOpen = $('.'+dependencyPanel).hasClass('open');
		if(panelStatusOpen){
			$(event).find('i.fa-fw').removeClass('fa-caret-down').addClass('fa-caret-right');
			$('.'+dependencyPanel).removeClass('open').addClass('closed');
			$('.'+dependencyPanel).slideUp(300, function(){
				public.correctDependenciesPanelSize();
			});
		} else {
			$(event).find('i.fa-fw').removeClass('fa-caret-right').addClass('fa-caret-down');
			$('.'+dependencyPanel).removeClass('closed').addClass('open');
			$('.'+dependencyPanel).slideDown(300, function(){
				public.correctDependenciesPanelSize();
			});
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


	// gets the currently open panel (using the PANELS enum)
	public.getOpenPanel = function () {
		if ($('#controlPanelId').css('display') == 'block')
			return PANELS.CONTROL;
		if ($('#dependenciesPanelId').css('display') == 'block')
			return PANELS.DEPENDENCY;
		if ($('#legendDivId').css('display') == 'block')
			return PANELS.LEGEND;
		return PANELS.NONE;
	}

	// hides the specified panel (takes a value from the PANELS enum as a parameter)
	public.hidePanel = function (panel) {
		if (panel == PANELS.CONTROL) {
			$('#controlPanelId').removeClass('openPanel');
			$('#controlPanelTabId').removeClass('activeTab');
		} else if (panel == PANELS.DEPENDENCY) {
			$('#dependenciesPanelId').removeClass('openPanel');
			$('#dependenciesPanelTabId').removeClass('activeTab');
		} else if (panel == PANELS.LEGEND) {
			$('#legendDivId').removeClass('openPanel');
			$('#legendTabId').removeClass('activeTab');
		}
	}

	// opens the specified panel (takes a value from the PANELS enum as a parameter)
	public.openPanel = function (panel) {
		if (panel == PANELS.CONTROL) {
			$('#controlPanelId').addClass('openPanel');
			$('#controlPanelTabId').addClass('activeTab');
		} else if (panel == PANELS.DEPENDENCY) {
			$('#dependenciesPanelId').addClass('openPanel');
			$('#dependenciesPanelTabId').addClass('activeTab');
		} else if (panel == PANELS.LEGEND) {
			$('#legendDivId').addClass('openPanel');
			$('#legendTabId').addClass('activeTab');
		}
	}

	// handles switching between the various panels in PANELS
	public.togglePanel = function (panel) {
		// control panel
		if (panel == PANELS.CONTROL) {
			if ($('#controlPanelTabId.activeTab').size() > 0)
				public.hidePanel(PANELS.CONTROL);
			else
				public.openPanel(PANELS.CONTROL);
			public.hidePanel(PANELS.LEGEND);
			public.hidePanel(PANELS.DEPENDENCY);
		// dependency panel
		} else if (panel == PANELS.DEPENDENCY) {
			if ($('#dependenciesPanelTabId.activeTab').size() > 0)
				public.hidePanel(PANELS.DEPENDENCY);
			else {
				public.openPanel(PANELS.DEPENDENCY);
			}
			public.hidePanel(PANELS.LEGEND);
			public.hidePanel(PANELS.CONTROL);
		// legend panel
		} else if (panel == PANELS.LEGEND) {
			if ($('#legendTabId.activeTab').size() > 0)
				public.hidePanel(PANELS.LEGEND);
			else
				public.openPanel(PANELS.LEGEND);
			public.hidePanel(PANELS.CONTROL);
			public.hidePanel(PANELS.DEPENDENCY);
		// hide all panels
		} else if (panel == 'hide' || panel == PANELS.NONE) {
			public.hidePanel(PANELS.CONTROL);
			public.hidePanel(PANELS.LEGEND);
			public.hidePanel(PANELS.DEPENDENCY);
		}
		public.correctPanelSizes();
	}

	// populates the team select
	public.populateTeamSelect = function (data) {
		// get the select element and clear whatever options were in it before
		teamSelect = $("#teamSelectId");
		teamSelect.children('option').remove();

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

	/*
	// handles the cursor change for a key event
	public.handleKeyCursorChange = function (key, state) {
		var className = ''
		if (key == KEY_CODES.SHIFT)
			className = 'shift_key'
		else if (key == KEY_CODES.CTRL)
			className = 'ctrl_key'

		if (state == KEY_STATE_DOWN)
			public.updateCursorStyle(className, true)
		else if (state == KEY_STATE_UP)
			public.updateCursorStyle(className, false)
	}
	*/

	// updates the cursor style for the graph based on input
	public.updateCursorStyle = function (cursorClass, enabled) {
		var target = canvas[0][0]
		if (enabled)
			target.classList.add(cursorClass)
		else
			target.classList.remove(cursorClass)

		public.forceReflow(canvas)
	}

	// called to activate a tool for the graph (for example: the lasso select tool)
	public.activateTool = function (tool, button) {
		button.addClass('toolActive')
		if (tool == public.LASSO_TOOL) {
			public.toolStates.lasso = true
			public.updateCursorStyle('regionSelectCursor', true)
		} else if (tool == public.SELECTION_ADD_TOOL) {
			public.toolStates.selectionAdd = true
		}
	}

	// called to deactivate a tool for the graph (for example: the lasso select tool)
	public.deactivateTool = function (tool, button) {
		button.removeClass('toolActive')
		if (tool == public.LASSO_TOOL) {
			public.toolStates.lasso = false
			public.updateCursorStyle('regionSelectCursor', false)
		} else if (tool == public.SELECTION_ADD_TOOL) {
			public.toolStates.selectionAdd = false
		}
	}

	// toggles the active state of a tool
	public.toggleToolState = function (tool, button) {
		var toolActive = button.hasClass('toolActive')
		if (toolActive)
			public.deactivateTool(tool, button)
		else
			public.activateTool(tool, button)
	}

	// ############################################################## graph data and control functions ##############################################################

	// gets the complete list of nodes applied to the force layout
	public.getNodes = function () {
		return GraphUtil.force.nodes()
	}

	// gets the complete list of links applied to the force layout
	public.getLinks = function () {
		return GraphUtil.force.links()
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
	public.updateNodeClasses = function (changedNodes) {
		var bundle = public.getFilteredBundle();
		public.nodeBindings.attr("class", function(d) {
			return 'node'
				+ ((d.selected == SELECTION_STATES.SELECTED_SECONDARY) ? ' selected selectedChild' : '')
				+ ((d.selected == SELECTION_STATES.SELECTED_PRIMARY) ? ' selected selectedParent' : '')
				+ ((d.root) ? ' root' : '')
				+ ((public.isConflictsEnabled() && ! d.hasMoveEvent) ? ' noEvent' : '')
				+ ((public.isBundleFilterEnabled() && ! public.isInFilteredBundle(d, bundle)) ? ' filtered' : '')
				+ ((d.sourceAsset) ? ' sourceAsset' : '')
				+ ((public.isBlackBackground()) ? ' blackBackground' : '')
				+ public.getFilteredClass(d)
		});
	}
	// sets the class list for every link in the graph
	public.updateLinkClasses = function (changedLinks) {
		public.linkBindings.attr("class", function(d) {
			return 'link'
				+ ((d.selected == SELECTION_STATES.SELECTED_PRIMARY) ? ' selected' : '')
				+ ((d.unresolved) ? ' unresolved' : '')
				+ ((d.notApplicable) ? ' notApplicable' : '')
				+ ((d.future) ? ' future' : '')
				+ ((d.validated) ? ' validated' : '')
				+ ((d.questioned) ? ' questioned' : '')
				+ ((d.cyclical) ? ' cyclical' : '')
				+ ((d.cut) ? ' cut' : '')
				+ ((d.root) ? ' root' : '')
				+ ((d.hide === 'y') ? ' hide_link' : '')
				+ ((public.isConflictsEnabled() && d.bundleConflict) ? ' bundleConflict' : '')
				+ ((public.isHighlightCyclesEnabled() && d.partOfCycle) ? ' cyclical' : '')
				+ ((public.isBlackBackground()) ? ' blackBackground' : '')
				+ public.getFilteredClass(d)
		});
	}
	// sets the class list for every label in the graph
	public.updateLabelClasses = function (changedNodes) {
		var bundle = public.getFilteredBundle();
		public.labelBindings.attr("class", function(d) {

			if (d.highlighted == 'y')
				$(this).children().attr('dy', '0.36em')
			else
				$(this).children().attr('dy', '0.35em')
			return 'label'
				+ ((d.selected > SELECTION_STATES.NOT_SELECTED) ? ' selected' : '')
				+ ((public.isBlackBackground()) ? ' blackBackground' : '')
				+ ((public.isBundleFilterEnabled() && ! public.isInFilteredBundle(d, bundle)) ? ' filtered' : '')
				+ ((! d.showLabel) ? ' hidden' : '')
				+ public.getFilteredClass(d)
		});
	}

	// updates the class list for ever graph element
	public.updateAllClasses = function (callback, changedNodes, changedLinks) {
		public.updateNodeClasses();
		public.updateLinkClasses();
		public.updateLabelClasses();
		if(callback){
			return callback();
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
	};

	public.createLineShadows = function (color) {
		public.force.links().each(function (o, i) {
			// get the cut shadow object (create it if it doesn't exist)
			var lineShadow = $(o.linkElement[0][0]);

			lineShadow.css('opacity', null);
			if (o.highlighted == 'y') {
				lineShadow.css('opacity', 1);
				lineShadow.css('stroke-width', 3);
			} else if (o.highlighted == 'n'){
				lineShadow.css('opacity', 0.3);
				lineShadow.css('stroke-width', 1);
			} else {
				lineShadow.css('opacity', 1);
				lineShadow.css('stroke-width', 1);
			}
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
		var oldTransformString = public.getTransformString(transformElement)
		var newTransformString = public.constructTransformString(translate[0], translate[1], scale, 'px')
		var transformInterpolation = d3.interpolateString(oldTransformString, newTransformString)

		// perform the transform, only animating the transition if an SVG element is used
		if (transformElement[0][0].tagName == 'DIV')
			transformElement.style('transform', newTransformString)
		else
			transformElement
				.transition()
				.duration(100)
				.ease(function (t) {
					return Math.min(t, 1)
				})
				.styleTween('transform', function (d) {
					return transformInterpolation
				})
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

	// sets the current position for the new selection temp path (connecting the current cursor position to the origin)
	public.setTempSelectionPath = function (next) {
		let nextRaster = next.screenPos.copy()
		public.tempPathString = ' L ' + Math.round(nextRaster.x) + ' ' + Math.round(nextRaster.y)
	}

	// initiallizes all the variables/elements for a new selection path starting at the given point, performing a multiselection if the multiselect parameter is true
	public.initializeSelectionPath = function (next, multiselect) {
		let nextRaster = next.screenPos.copy()
		let nextPos = next.graphPos.copy()
		lastSelectionPoint = next.copy()
		public.tempPathString = ''
		public.selectionPath = [lastSelectionPoint]
		public.selectionPathString = 'M ' + Math.round(nextRaster.x) + ' ' + Math.round(nextRaster.y)
		// update the selection path elements
		public.selectionElements.regionPath.attr('d', public.selectionPathString)
		public.selectionElements.regionProjection.attr('d', public.selectionPathString + 'Z')
		// initialize node variables
		for (let n of nodes) {
			n.insideRegion = false
			n.leftRegionEdges = 0
		}
		// depending on if multiselect is true, either deselect all nodes or set the current selection list as the "preselection list"
		if (!multiselect)
			modifyNodeSelection([], SELECT_MODES.REPLACE)
		else
			public.preselectionList = selectedNodes
	}

	// updates the selection path based on the new given cursor position. it will be added to the selection path if it is far enough from the last point to meet the distance threshold or the forceAdd parameter is given
	public.updateSelectionPath = function (next, forceAdd) {
		let nextRaster = next.screenPos.copy()
		let nextPos = next.graphPos.copy()
		var prev = lastSelectionPoint

		// only add the point to the region path if it is far enough from the previous one
		if (forceAdd || prev.screenPos.distanceTo(nextRaster) > public.SELECTION_MIN_EDGE_LENGTH) {
			public.selectionPathString += ' L ' + Math.round(nextRaster.x) + ' ' + Math.round(nextRaster.y)
			lastSelectionPoint = next.copy()
			public.selectionPath.push(lastSelectionPoint)

			// TODO : rmacfarlane 7/2018 : add some quadtree and heuristic based pruning here to decreases the search space for large graphs
			// check if any new nodes should be selected
			var newSelection = []
			var startPoint = public.selectionPath[0]
			var nodes = public.force.nodes()
			for (let node of nodes) {
				let isLeftOfNewEdge = isLeftOfEdge(node, prev.graphPos, lastSelectionPoint.graphPos)		// is this node left of the new edge?
				let isLeftOfProjection = isLeftOfEdge(node, lastSelectionPoint.graphPos, startPoint.graphPos)	// is this node left of the projected edge of the new point to the start point?

				let tempLeftEdges = node.leftRegionEdges
				if (isLeftOfNewEdge) {
					++node.leftRegionEdges
					++tempLeftEdges
				}
				if (isLeftOfProjection) {
					++tempLeftEdges
				}

				node.insideRegion = (tempLeftEdges % 2 == 1)
				if (node.insideRegion)
					newSelection.push(node)
			}

			// if realtime highlighting is enabled, update the selection now
			if (public.realtimeSelectionHighlighting) {
				newSelection = _.union(newSelection, public.preselectionList)
				modifyNodeSelection(newSelection, SELECT_MODES.REPLACE_NO_TOGGLE)
			}
		}

		// update the selection path elements
		public.selectionElements.regionPath.attr('d', public.selectionPathString + public.tempPathString)
		public.selectionElements.regionProjection.attr('d', public.selectionPathString + public.tempPathString + 'Z')
	}

	// filters the node selection list based on the current region select path
	public.filterRegionSelection = function () {
		var selectionPoints = []
		for (var i = 0; i < public.selectionPath.length; ++i) {
			var point = public.selectionPath[i].graphPos
			selectionPoints.push(point)
		}

		var newSelectionList = []
		var nodes = public.force.nodes()
		for (var n = 0; n < nodes.length; ++n) {
			var node = nodes[n]
			var intersections = 0
			for (var e = 0; e < selectionPoints.length; ++e) {
				var v1 = selectionPoints[e]
				var v2 = selectionPoints[(e+1)%(selectionPoints.length)]

				if (isLeftOfEdge(node, v1, v2))
					intersections++
			}
			if (intersections % 2 == 1)
				newSelectionList.push(node)
		}
		newSelectionList = _.union(newSelectionList, public.preselectionList)
		public.preselectionList = []
		modifyNodeSelection(newSelectionList, SELECT_MODES.REPLACE_NO_TOGGLE)
	}

	// returns true if a ray starting at the given node and going right would intersect with the edge defined by the two given vertices
	function isLeftOfEdge (node, v1, v2) {
		// salt the xy values so we don't have to worry about the edge cases of matching xy coordinates causing 0/inifinite slope
		v1 = v1.salted()
		v2 = v2.salted()

		// to simplify things always make the leftmost vertex v1
		if (v1.x > v2.x) {
			var temp = v2
			v2 = v1
			v1 = temp
		}

		// negate the y values to make the math easier to follow
		v1 = v1.inverted()
		v2 = v2.inverted()
		node = new Point(node.x, -1*node.y)

		// if the node is above or below both vertices they cannot intersect so if either of these cases are true we should stop here
		var nodeIsBelow = Math.min(v1.y, v2.y) > node.y
		var nodeIsAbove = Math.max(v1.y, v2.y) < node.y
		if (nodeIsBelow || nodeIsAbove)
			return false

		// calculate the slope/intercept of the line and use f(x)=mx+b to get the projected y value of the function when given the node's x
		var slope = (v2.y - v1.y) / (v2.x - v1.x)
		var offset = -1 * (slope * v1.x - v1.y)
		var projectedY = slope*node.x + offset

		// make final calculations and checks to see if the node is left of the edge
		var nodeIsLess = projectedY > node.y		// true if y valvue of the node is less than the projected y on the edge
		var positiveSlope = (slope > 0)			// true if the slope of the edge is positive
		var isLeft = (nodeIsLess != positiveSlope)	// true if the node is left of the edge
		return isLeft
	}

	// ############################################################## key binding functions ##############################################################

	// add key listeners for graph features such as zooming, panning, and freezing
	public.addKeyListeners = function () {
		$(window).off('keydown', public.handleKeyEvent)
		$(window).on('keydown', public.handleKeyEvent)
	}

	// triggers the appropriate graph behavior for a given keyboard event
	public.handleKeyEvent = function (event) {
		// ignore keystrokes while the user is typing in an text field
		if ( ! IGNORE_KEY_EVENT_TAGS.contains(event.target.tagName) ) {
			// handle modifier keys
			var modifier = 1
			if (event.shiftKey)
				modifier = 2
			if (event.ctrlKey)
				modifier = 0.5

			// perform action based on key code
			switch (event.keyCode) {
				case KEY_CODES.LEFT:  public.translateLeft(modifier); break;
				case KEY_CODES.RIGHT: public.translateRight(modifier); break;
				case KEY_CODES.UP:    public.translateUp(modifier); break;
				case KEY_CODES.DOWN:  public.translateDown(modifier); break;
				case KEY_CODES.PLUS:  performZoom('in', modifier); break;
				case KEY_CODES.MINUS: performZoom('out', modifier); break;
				case KEY_CODES.SPACE: public.toggleFreeze(); break;
			}
		}
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

	// highlight assets matching the id from the Group + Tag
	public.performTagSearch = function (assetList) {
		var nodes = public.force.nodes();
		var highlightList = [];
		for (var i = 0; i < nodes.length; ++i) {
			var node = nodes[i];
			var asset = assetList.filter( function(asset) {
				return asset === node.id;
			});
			if (asset && asset.length >= 1){
				highlightList.push(node.id);
			}
		}
		public.applyHighlights(highlightList);
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
				method: 'POST',
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
	};

	public.restoreDependencyPanel = function(type) {
		if(GraphUtil.dependencyPanelConfig['dependency' + type].status === 'true'){
			$('#dependency' + type + 'Control_show_all').prop('checked', true);
			$('#dependency' + type + 'Control_show_all').attr('state', 1);
			if (!GraphUtil.dependencyPanelConfig['dependency' + type].dirty) {
				$('.dependency' + type + 'ControlsShow').prop('checked', true);
			} else {
				$('.dependency' + type + 'ControlsShow').each(function () {
					$(this).prop('checked', GraphUtil.dependencyPanelConfig['dependency' + type].show.indexOf($(this).val()) !== -1);
				});
			}
		} else if(GraphUtil.dependencyPanelConfig['dependency' + type].status === 'false') {
			$('#dependency' + type + 'Control_show_all').prop('checked', false);
			$('#dependency' + type + 'Control_show_all').attr('state', 3);
			$('.dependency' + type + 'ControlsShow').each(function () {
				$(this).prop('checked', GraphUtil.dependencyPanelConfig['dependency' + type].show.indexOf($(this).val()) !== -1);
			});
		} else if(GraphUtil.dependencyPanelConfig['dependency' + type].status === 'indeterminate'){
			$('#dependency' + type + 'Control_show_all').prop('indeterminate', true);
			$('#dependency' + type + 'Control_show_all').attr('state', 2);
			$('.dependency' + type + 'ControlsShow').each(function(){
				var selected = GraphUtil.dependencyPanelConfig['dependency' + type].groupingControl.indexOf($(this).val()) !== -1;
				$(this).prop('checked', selected);
			});
		}

		if(GraphUtil.dependencyPanelConfig['dependency' + type].highlight.length > 0) {
			$('.dependency' + type + 'ControlsHighlight').each(function () {
				$(this).prop('checked', GraphUtil.dependencyPanelConfig['dependency' + type].highlight.indexOf($(this).val()) !== -1);
			});
		}

		$('.dependency' + type + 'ControlsShow').each(function(){
			var selected = GraphUtil.dependencyPanelConfig['dependency' + type].groupingControl.indexOf($(this).val()) !== -1;
			if(selected) {
				$(this).parent().addClass('groupingControl');
			}
		});
	};

	public.onSelectItemShowDependencyPanel = function(event){
		if(!$(event).is(":checked")) {
			$($(event).parent().siblings()[1]).find('input:checkbox').prop('checked', false);
		}

		var currentShowClass = $($(event)[0]).attr('class');
		var parentId = $($(event)[0]).attr('parentid');
		var noneChecked = true;
		$('.' + currentShowClass).each(function(){
			if($(this).is(":checked")) {
				noneChecked = false;
			}
		});

		if(noneChecked) {
			$('#' + parentId).prop('checked', false);
			$('#' + parentId).attr('state', 3);
		}
	};

	public.onSelectItemHighlightDependencyPanel = function(event){
		if($(event).is(":checked")) {
			$($(event).parent().siblings()[1]).find('input:checkbox').prop('checked', true);
		}
	};

	public.onSelectAllDependencyPanel = function(checkboxSelectorClass, config, event) {
		var state = parseInt($(event).attr('state'));
		if(state === 1) {
			state = 3;
			$('.' + checkboxSelectorClass + 'Show').prop('checked', false);
			$('.' + checkboxSelectorClass + 'Highlight').prop('checked', false);
			GraphUtil.dependencyPanelConfig[config].status = 'false';
		} else if(state === 2) {
			state = 1;
			$('.' + checkboxSelectorClass + 'Show').prop('checked', true);
			$(event).prop("checked", true);
			GraphUtil.dependencyPanelConfig[config].status = 'true';
		} else if(state === 3) {
			state = 2;
			$(event).prop("indeterminate", true);
			$('.' + checkboxSelectorClass + 'Show').each(function(){
				var showCheckbox = $(this);
				var selected = GraphUtil.dependencyPanelConfig[config].groupingControl.indexOf(showCheckbox.val()) !== -1;
				$(this).prop('checked', selected);
			});
			$('.' + checkboxSelectorClass + 'Highlight').each(function(){
				if($(this).is(":checked")){
					var sibling = $(this).parent().siblings()[1];
					if(!$(sibling).find('input:checkbox').is(":checked")) {
						$(this).prop('checked', false);
					}
				}
			});
			GraphUtil.dependencyPanelConfig[config].status = 'indeterminate';
		}
		$(event).attr('state', state);
	};

	public.applyShowHideDependencies = function () {
		var links = public.force.links();
		var showTypeItems = $('.dependencyTypeControlsShow:checked').map(function() { return this.value; }).get();
		var highlightTypeItems = $('.dependencyTypeControlsHighlight:checked').map(function() { return this.value; }).get();
		var showStatusItems = $('.dependencyStatusControlsShow:checked').map(function() { return this.value; }).get();
		var highlightStatusItems = $('.dependencyStatusControlsHighlight:checked').map(function() { return this.value; }).get();

		var hideList = [];
		var highlightList = [];
		for (var i = 0; i < links.length; ++i) {
			var link = links[i];
			var dependencyStatus = link.dependencyStatus;
			var dependencyType = link.dependencyType;
			link.hide = ((showTypeItems.indexOf(dependencyType) === -1) || (showStatusItems.indexOf(dependencyStatus) === -1))? 'y' : 'n';
			if(highlightTypeItems.length > 0 || highlightStatusItems.length > 0) {
				link.highlighted = (highlightTypeItems.indexOf(dependencyType) !== -1)? 'y' : (highlightStatusItems.indexOf(dependencyStatus) !== -1)? 'y' : 'n';
			} else if(highlightTypeItems.length == 0 && highlightStatusItems.length == 0) {
				link.highlighted = null;
			}
		}

		public.dependencyPanelConfig.dependencyType.show = showTypeItems;
		public.dependencyPanelConfig.dependencyType.highlight = highlightTypeItems;
		public.dependencyPanelConfig.dependencyType.dirty = true;

		public.dependencyPanelConfig.dependencyStatus.show = showStatusItems;
		public.dependencyPanelConfig.dependencyStatus.highlight = highlightStatusItems;
		public.dependencyPanelConfig.dependencyStatus.dirty = true;

		public.updateAllClasses(function(){
			$('.link').show();
			$('.hide_link').hide();
			public.createCutShadows(fill);
			public.createLineShadows(fill)
		});
	};

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

	public.getDistance = function (x1, y1, x2, y2) {
		var dx = x1 - x2;
		var dy = y1 - y2;
		var distance = Math.sqrt(dx * dx + dy * dy);
		return distance;
	}

	// constructs a string for a svg transformation attribute
	public.constructTransformString = function (x, y, scale, unit) {
		unit = unit ? unit : ''
		return 'translate(' + x + unit + ',' + y + unit  + ')scale(' + scale + ')'
	}

	// gets the correctly formatted transform string of an element in a d3 selection
	public.getTransformString = function (d3Element) {
		return d3Element[0][0].style.transform.replace(/ /g,'')
	}

	// adds a negligible tiny value to a given number for the sole purpose of preventing matches when it would be inconvenient
	public.saltValue = function (value) {
		var salt = Math.random() * SALT_MULTIPLIER
		return value + salt
	}

	// why doesn't javascript have XOR???
	public.xor = function (a, b) {
		return (a || b) && (a != b)
	}

	// prevents any further propagation or default behaviors from triggering off of the given event
	public.captureEvent = function (event) {
		event = event ? event : d3.event.sourceEvent
		if (event) {
			event.preventDefault()
			event.stopImmediatePropagation()
			event.stopPropagation()
		}
	}

	// ############################################################## meta functions ##############################################################



	// ############################################################## return object ##############################################################
	// return the public object to make the public functions accessable
	return public;

})(jQuery); //passed 'jQuery' global variable into local parameter '$'
