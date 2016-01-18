/* 	These shapes must be hard coded here due to limitations of the d3 library.
	There is no way for d3 to parse through svg data unless it is explicitly included in the DOM.	*/

// loads the shapes into the svg defs
function defineShapes (defs) {
	
	// define the arrowhead markers used for marking dependencies
	var markers = {'arrowhead':'#000000', 'arrowheadBlackBackground':'#ffffff', 'arrowheadNA':'#909090', 'arrowheadNABlackBackground':'#eeeeee', 'arrowheadUnresolved':'red', 'arrowheadSelected':'#00dd00', 'arrowheadCyclical':'blue', 'arrowheadRedundant':'orange', 'arrowheadCut':'orange', 'arrowheadBundleConflict':'red'};
	var keys = Object.keys(markers);
	for (var i = 0; i < keys.length; ++i) {
		defs.append("marker")
			.attr("id", keys[i])
			.attr("viewBox", "0 -5 10 10")
			.attr("refX", 20)
			.attr("refY", 0)
			.attr("markerUnits", "userSpaceOnUse")
			.attr("markerWidth", 10)
			.attr("markerHeight", 10)
			.attr("orient", "auto")
			.append("path")
			.attr("d", "M0,-4L10,0L0,4")
			.attr('fill', markers[keys[i]]);
	}
	
	defs.selectAll('#arrowheadSelected,#arrowheadCyclical,#arrowheadBundleConflict')
		.attr("viewBox", "0 -6 10 12")
		.attr("markerHeight", 12);
	defs.selectAll('#arrowheadSelected path,#arrowheadCyclical path,#arrowheadBundleConflict path').attr("d", "M0,-6L10,0L0,6");
	/*
	// define the custom database object
	var databaseShape = defs
		.append("g")
		.attr("id", "databaseShapeId")
		.attr("transform", "translate(-10, -10) scale(0.6)")
		.attr("stroke-width", "1");
	databaseShape.append('path')
		.attr("d", "M16,0C9.256,0,2,2.033,2,6.5v19C2,29.965,9.256,32,16,32c6.743,0,14-2.035,14-6.5v-19C30,2.033,22.742,0,16,0z")
		.attr("id", "databaseShapeBackgroundId");
	databaseShape.append('path')
		.attr("d", "M16,0C9.256,0,2,2.033,2,6.5v19C2,29.965,9.256,32,16,32c6.743,0,14-2.035,14-6.5v-19"
			+ "C30,2.033,22.742,0,16,0z M28,25.5c0,2.484-5.373,4.5-12,4.5c-6.628,0-12-2.016-12-4.5v-3.736C6.066,23.893,11.05,25,16,25"
			+ "c4.95,0,9.934-1.107,12-3.236V25.5z M28,19.5h-0.004c0,0.01,0.004,0.021,0.004,0.031C28,22,22.627,24,16,24c-6.627,0-12-2-12-4.469"
			+ "c0-0.01,0.004-0.021,0.004-0.031H4v-3.736C6.066,17.893,11.05,19,16,19c4.95,0,9.934-1.107,12-3.236V19.5z M28,13.5h-0.004"
			+ "c0,0.01,0.004,0.021,0.004,0.031C28,16,22.627,18,16,18c-6.627,0-12-2-12-4.469c0-0.01,0.004-0.021,0.004-0.031H4v-3.436"
			+ "C6.621,12.061,11.425,13,16,13c4.575,0,9.379-0.939,12-2.936V13.5z M16,11C9.372,11,4,8.984,4,6.5C4,4.014,9.372,2,16,2"
			+ "c6.627,0,12,2.014,12,4.5C28,8.984,22.627,11,16,11z")
		.attr("id", "databaseShapeForegroundId");
	databaseShape.append('circle')
		.attr("cy", 26)
		.attr("cx", 25)
		.attr("r", 1);
	databaseShape.append('circle')
		.attr("cy", 20)
		.attr("cx", 25)
		.attr("r", 1);
	databaseShape.append('circle')
		.attr("cy", 14)
		.attr("cx", 25)
		.attr("r", 1);

	// define the custom device shape
	var deviceShape = defs
		.append("g")
		.attr("id", "deviceShapeId")
		.attr("transform", "translate(-10, -10) scale(0.6)")
		.attr("stroke-width", "1");
	deviceShape.append('circle')
		.attr("cy", 26)
		.attr("cx", 25)
		.attr("r", 1);

	// define the shapes for the other objects that do not yet have custom svg shapes
	defs
		.append("path")
		.attr("id", "applicationShapeId")
		.attr("d", d3.svg.symbol().size(150).type('circle'));

	defs
		.append("path")
		.attr("id", "serverPhysicalShapeId")
		.attr("d", d3.svg.symbol().size(150).type('square'));
	defs
		.append("path")
		.attr("id", "serverVirtualShapeId")
		.attr("d", d3.svg.symbol().size(150).type('square'));

	defs
		.append("path")
		.attr("id", "storagePhysicalShapeId")
		.attr("d", d3.svg.symbol().size(150).type('diamond'));
	defs
		.append("path")
		.attr("id", "storageLogicalShapeId")
		.attr("d", d3.svg.symbol().size(150).type('diamond'));

	defs
		.append("path")
		.attr("id", "networkPhysicalShapeId")
		.attr("d", d3.svg.symbol().size(150).type('cross'));
	defs
		.append("path")
		.attr("id", "networkLogicalShapeId")
		.attr("d", d3.svg.symbol().size(150).type('cross'));
		
	defs
		.append("path")
		.attr("id", "otherShapeId")
		.attr("d", d3.svg.symbol().size(150).type('triangle-up'));
	*/
	return null;
}