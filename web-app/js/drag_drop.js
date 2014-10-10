var Drag = {

	obj : null,

	init : function(o, oRoot, minX, maxX, minY, maxY, bSwapHorzRef, bSwapVertRef, fXMapper, fYMapper)
	{
		if(o){
			o.onmousedown	= Drag.start;
	
			o.hmode			= bSwapHorzRef ? false : true ;
			o.vmode			= bSwapVertRef ? false : true ;
	
			o.root = oRoot && oRoot != null ? oRoot : o ;
	
			if (o.hmode  && isNaN(parseInt(o.root.style.left  ))) o.root.style.left   = "0px";
			if (o.vmode  && isNaN(parseInt(o.root.style.top   ))) o.root.style.top    = "0px";
			if (!o.hmode && isNaN(parseInt(o.root.style.right ))) o.root.style.right  = "0px";
			if (!o.vmode && isNaN(parseInt(o.root.style.bottom))) o.root.style.bottom = "0px";
	
			o.minX	= typeof minX != 'undefined' ? minX : null;
			o.minY	= typeof minY != 'undefined' ? minY : null;
			o.maxX	= typeof maxX != 'undefined' ? maxX : null;
			o.maxY	= typeof maxY != 'undefined' ? maxY : null;
	
			o.xMapper = fXMapper ? fXMapper : null;
			o.yMapper = fYMapper ? fYMapper : null;
	
			o.root.onDragStart	= new Function();
			o.root.onDragEnd	= new Function();
			o.root.onDrag		= new Function();
		}
	},

	start : function(e)
	{
		//e.addClass("objectSelected");
 		var o = Drag.obj = this;
		e = Drag.fixE(e);
		var y = parseInt(o.vmode ? o.root.style.top  : o.root.style.bottom);
		var x = parseInt(o.hmode ? o.root.style.left : o.root.style.right );
		o.root.onDragStart(x, y);

		o.lastMouseX	= e.clientX;
		o.lastMouseY	= e.clientY;

		if (o.hmode) {
			if (o.minX != null)	o.minMouseX	= e.clientX - x + o.minX;
			if (o.maxX != null)	o.maxMouseX	= o.minMouseX + o.maxX - o.minX;
		} else {
			if (o.minX != null) o.maxMouseX = -o.minX + e.clientX + x;
			if (o.maxX != null) o.minMouseX = -o.maxX + e.clientX + x;
		}

		if (o.vmode) {
			if (o.minY != null)	o.minMouseY	= e.clientY - y + o.minY;
			if (o.maxY != null)	o.maxMouseY	= o.minMouseY + o.maxY - o.minY;
		} else {
			if (o.minY != null) o.maxMouseY = -o.minY + e.clientY + y;
			if (o.maxY != null) o.minMouseY = -o.maxY + e.clientY + y;
		}

		document.onmousemove	= Drag.drag;
		document.onmouseup		= Drag.end;

		return false;
	},

	drag : function(e)
	{
		e = Drag.fixE(e);
		var o = Drag.obj;

		var ey	= e.clientY;
		var ex	= e.clientX;
		var y = parseInt(o.vmode ? o.root.style.top  : o.root.style.bottom);
		var x = parseInt(o.hmode ? o.root.style.left : o.root.style.right );
		var nx, ny;

		if (o.minX != null) ex = o.hmode ? Math.max(ex, o.minMouseX) : Math.min(ex, o.maxMouseX);
		if (o.maxX != null) ex = o.hmode ? Math.min(ex, o.maxMouseX) : Math.max(ex, o.minMouseX);
		if (o.minY != null) ey = o.vmode ? Math.max(ey, o.minMouseY) : Math.min(ey, o.maxMouseY);
		if (o.maxY != null) ey = o.vmode ? Math.min(ey, o.maxMouseY) : Math.max(ey, o.minMouseY);

		nx = x + ((ex - o.lastMouseX) * (o.hmode ? 1 : -1));
		ny = y + ((ey - o.lastMouseY) * (o.vmode ? 1 : -1));

		if (o.xMapper)		nx = o.xMapper(y)
		else if (o.yMapper)	ny = o.yMapper(x)

		Drag.obj.root.style[o.hmode ? "left" : "right"] = nx + "px";
		Drag.obj.root.style[o.vmode ? "top" : "bottom"] = ny + "px";
		Drag.obj.lastMouseX	= ex;
		Drag.obj.lastMouseY	= ey;

		Drag.obj.root.onDrag(nx, ny);
		return false;
	},

	end : function()
	{
		document.onmousemove = null;
		document.onmouseup   = null;
		Drag.obj.root.onDragEnd(	parseInt(Drag.obj.root.style[Drag.obj.hmode ? "left" : "right"]), 
									parseInt(Drag.obj.root.style[Drag.obj.vmode ? "top" : "bottom"]));
		Drag.obj = null;
	},

	fixE : function(e)
	{
		if (typeof e == 'undefined') e = window.event;
		if (typeof e.layerX == 'undefined') e.layerX = e.offsetX;
		if (typeof e.layerY == 'undefined') e.layerY = e.offsetY;
		return e;
	}
};
function initializeConnectors( usize, type ){
	var valueY = usize*30 - 19
	$("#cablingPanel").css("height",type == 'auto' ? 'auto' : (usize*30) + 'px' )
	for (var ci=1; ci <= 50; ci++) {
		var connector = document.getElementById("connector" + ci);
		if(connector){
			Drag.init(connector, null, 0, 360, 0, valueY);
			connector.onDrag = modelConnectorDragHandler(ci);
		}
	}
};
function modelConnectorDragHandler(ci) {
	return function (x, y) {
		y = 2 * y
		$("#connectorPosXId" + ci).val(x)
		$("#connectorPosYId" + ci).val(y)
	}
};
function initializeRacksInRoom( racks ){
	var height = $("#room_layout_table tr").length 
	var width = $("#room_layout_table tr td").length / height
	
	//$("#room_layout_table").css({'height' : height*40, 'width' : width*40});
	//$("#room_layout").css({'height' : height*40+30, 'width' : width*40+20});
	$("#roomLayout").css('width',width*40+20);

	height = height*40
	width = width*40
	
	for(i=0;i<racks.length;i++){
		var rack = document.getElementById("rack_"+racks[i]);
		if(rack){
			Drag.init(rack, null, 0, width, 0, height);
		}
	}
	for(i=50000;i<50051;i++){
		var rack = document.getElementById("rack_"+i);
		if(rack){
			Drag.init(rack, null, 0, width, 0, height);
		}
	}
}
/*----------------------------------------*/
function assignExistingConnectors( modelConnectors ){
	alert(modelConnectors.size())
}