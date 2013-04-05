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
	$("#cablingPanel").css("height",type == 'auto' ? 'auto' : usize*30)
	var connector1 = document.getElementById("connector1");
	if(connector1){
		Drag.init(connector1, null, 0, 360, 0, valueY);
		connector1.onDrag = function(x, y) {
			y = 2 * y
			$("#connectorPosXId1").val(x)
			$("#connectorPosYId1").val(y)
		}
	}
	
	var connector2 = document.getElementById("connector2");
	if(connector2){
		Drag.init(connector2, null, 0, 360, 0, valueY);
		connector2.onDrag = function(x, y) {
			y = 2 * y
			$("#connectorPosXId2").val(x)
			$("#connectorPosYId2").val(y)
		}
	}
	var connector3 = document.getElementById("connector3");
	if(connector3){
		Drag.init(connector3, null, 0, 360, 0, valueY);
		connector3.onDrag = function(x, y) {
			y = 2 * y
			$("#connectorPosXId3").val(x)
			$("#connectorPosYId3").val(y)
		}
	}
	var connector4 = document.getElementById("connector4");
	if(connector4){
		Drag.init(connector4, null, 0, 360, 0, valueY);
		connector4.onDrag = function(x, y) {
			y = 2 * y
			$("#connectorPosXId4").val(x)
			$("#connectorPosYId4").val(y)
		}
	}
	var connector5 = document.getElementById("connector5");
	if(connector5){
		Drag.init(connector5, null, 0, 360, 0, valueY);
		connector5.onDrag = function(x, y) {
			y = 2 * y
			$("#connectorPosXId5").val(x)
			$("#connectorPosYId5").val(y)
		}
	}
	var connector6 = document.getElementById("connector6");
	if(connector6){
		Drag.init(connector6, null, 0, 360, 0, valueY);
		connector6.onDrag = function(x, y) {
			y = 2 * y
			$("#connectorPosXId6").val(x)
			$("#connectorPosYId6").val(y)
		}
	}
	var connector7 = document.getElementById("connector7");
	if(connector7){
		Drag.init(connector7, null, 0, 360, 0, valueY);
		connector7.onDrag = function(x, y) {
			y = 2 * y
			$("#connectorPosXId7").val(x)
			$("#connectorPosYId7").val(y)
		}
	}
	var connector8 = document.getElementById("connector8");
	if(connector8){
		Drag.init(connector8, null, 0, 360, 0, valueY);
		connector8.onDrag = function(x, y) {
			y = 2 * y
			$("#connectorPosXId8").val(x)
			$("#connectorPosYId8").val(y)
		}
	}
	var connector9 = document.getElementById("connector9");
	if(connector9){
		Drag.init(connector9, null, 0, 360, 0, valueY);
		connector9.onDrag = function(x, y) {
			y = 2 * y
			$("#connectorPosXId9").val(x)
			$("#connectorPosYId9").val(y)
		}
	}
	
	var connector10 = document.getElementById("connector10");
	if(connector10){
		Drag.init(connector10, null, 0, 360, 0, valueY);
		connector10.onDrag = function(x, y) {
			y = 2 * y
			$("#connectorPosXId10").val(x)
			$("#connectorPosYId10").val(y)
		}
	}
	var connector11 = document.getElementById("connector11");
	if(connector11){
		Drag.init(connector11, null, 0, 360, 0, valueY);
		connector11.onDrag = function(x, y) {
			y = 2 * y
			$("#connectorPosXId11").val(x)
			$("#connectorPosYId11").val(y)
		}
	}
	var connector12 = document.getElementById("connector12");
	if(connector12){
		Drag.init(connector12, null, 0, 360, 0, valueY);
		connector12.onDrag = function(x, y) {
			y = 2 * y
			$("#connectorPosXId12").val(x)
			$("#connectorPosYId12").val(y)
		}
	}
	var connector13 = document.getElementById("connector13");
	if(connector13){
		Drag.init(connector13, null, 0, 360, 0, valueY);
		connector13.onDrag = function(x, y) {
			y = 2 * y
			$("#connectorPosXId13").val(x)
			$("#connectorPosYId13").val(y)
		}
	}
	var connector14 = document.getElementById("connector14");
	if(connector14){
		Drag.init(connector14, null, 0, 360, 0, valueY);
		connector14.onDrag = function(x, y) {
			y = 2 * y
			$("#connectorPosXId14").val(x)
			$("#connectorPosYId14").val(y)
		}
	}
	var connector15 = document.getElementById("connector15");
	if(connector15){
		Drag.init(connector15, null, 0, 360, 0, valueY);
		connector15.onDrag = function(x, y) {
			y = 2 * y
			$("#connectorPosXId15").val(x)
			$("#connectorPosYId15").val(y)
		}
	}
	var connector16 = document.getElementById("connector16");
	if(connector16){
		Drag.init(connector16, null, 0, 360, 0, valueY);
		connector16.onDrag = function(x, y) {
			y = 2 * y
			$("#connectorPosXId16").val(x)
			$("#connectorPosYId16").val(y)
		}
	}
	var connector17 = document.getElementById("connector17");
	if(connector17){
		Drag.init(connector17, null, 0, 360, 0, valueY);
		connector17.onDrag = function(x, y) {
			y = 2 * y
			$("#connectorPosXId17").val(x)
			$("#connectorPosYId17").val(y)
		}
	}
	var connector18 = document.getElementById("connector18");
	if(connector18){
		Drag.init(connector18, null, 0, 360, 0, valueY);
		connector18.onDrag = function(x, y) {
			y = 2 * y
			$("#connectorPosXId18").val(x)
			$("#connectorPosYId18").val(y)
		}
	}
	var connector19 = document.getElementById("connector19");
	if(connector19){
		Drag.init(connector19, null, 0, 360, 0, valueY);
		connector19.onDrag = function(x, y) {
			y = 2 * y
			$("#connectorPosXId19").val(x)
			$("#connectorPosYId19").val(y)
		}
	}
	var connector20 = document.getElementById("connector20");
	if(connector20){
		Drag.init(connector20, null, 0, 360, 0, valueY);
		connector20.onDrag = function(x, y) {
			y = 2 * y
			$("#connectorPosXId20").val(x)
			$("#connectorPosYId20").val(y)
		}
	}
	var connector21 = document.getElementById("connector21");
	if(connector21){
		Drag.init(connector21, null, 0, 360, 0, valueY);
		connector21.onDrag = function(x, y) {
			y = 2 * y
			$("#connectorPosXId21").val(x)
			$("#connectorPosYId21").val(y)
		}
	}
	var connector22 = document.getElementById("connector22");
	if(connector22){
		Drag.init(connector22, null, 0, 360, 0, valueY);
		connector22.onDrag = function(x, y) {
			y = 2 * y
			$("#connectorPosXId22").val(x)
			$("#connectorPosYId22").val(y)
		}
	}
	var connector23 = document.getElementById("connector23");
	if(connector23){
		Drag.init(connector23, null, 0, 360, 0, valueY);
		connector23.onDrag = function(x, y) {
			y = 2 * y
			$("#connectorPosXId23").val(x)
			$("#connectorPosYId23").val(y)
		}
	}
	var connector24 = document.getElementById("connector24");
	if(connector24){
		Drag.init(connector24, null, 0, 360, 0, valueY);
		connector24.onDrag = function(x, y) {
			y = 2 * y
			$("#connectorPosXId24").val(x)
			$("#connectorPosYId24").val(y)
		}
	}
	var connector25 = document.getElementById("connector25");
	if(connector25){
		Drag.init(connector25, null, 0, 360, 0, valueY);
		connector25.onDrag = function(x, y) {
			y = 2 * y
			$("#connectorPosXId25").val(x)
			$("#connectorPosYId25").val(y)
		}
	}
	var connector26 = document.getElementById("connector26");
	if(connector26){
		Drag.init(connector26, null, 0, 360, 0, valueY);
		connector26.onDrag = function(x, y) {
			y = 2 * y
			$("#connectorPosXId26").val(x)
			$("#connectorPosYId26").val(y)
		}
	}
	var connector27 = document.getElementById("connector27");
	if(connector27){
		Drag.init(connector27, null, 0, 360, 0, valueY);
		connector27.onDrag = function(x, y) {
			y = 2 * y
			$("#connectorPosXId27").val(x)
			$("#connectorPosYId27").val(y)
		}
	}
	var connector28 = document.getElementById("connector28");
	if(connector28){
		Drag.init(connector28, null, 0, 360, 0, valueY);
		connector28.onDrag = function(x, y) {
			y = 2 * y
			$("#connectorPosXId28").val(x)
			$("#connectorPosYId28").val(y)
		}
	}
	var connector29 = document.getElementById("connector29");
	if(connector29){
		Drag.init(connector29, null, 0, 360, 0, valueY);
		connector29.onDrag = function(x, y) {
			y = 2 * y
			$("#connectorPosXId29").val(x)
			$("#connectorPosYId29").val(y)
		}
	}
	var connector30 = document.getElementById("connector30");
	if(connector30){
		Drag.init(connector30, null, 0, 360, 0, valueY);
		connector30.onDrag = function(x, y) {
			y = 2 * y
			$("#connectorPosXId30").val(x)
			$("#connectorPosYId30").val(y)
		}
	}
	var connector31 = document.getElementById("connector31");
	if(connector31){
		Drag.init(connector31, null, 0, 360, 0, valueY);
		connector31.onDrag = function(x, y) {
			y = 2 * y
			$("#connectorPosXId31").val(x)
			$("#connectorPosYId31").val(y)
		}
	}
	var connector32 = document.getElementById("connector32");
	if(connector32){
		Drag.init(connector32, null, 0, 360, 0, valueY);
		connector32.onDrag = function(x, y) {
			y = 2 * y
			$("#connectorPosXId32").val(x)
			$("#connectorPosYId32").val(y)
		}
	}
	var connector33 = document.getElementById("connector33");
	if(connector33){
		Drag.init(connector33, null, 0, 360, 0, valueY);
		connector33.onDrag = function(x, y) {
			y = 2 * y
			$("#connectorPosXId33").val(x)
			$("#connectorPosYId33").val(y)
		}
	}
	var connector34 = document.getElementById("connector34");
	if(connector34){
		Drag.init(connector34, null, 0, 360, 0, valueY);
		connector34.onDrag = function(x, y) {
			y = 2 * y
			$("#connectorPosXId34").val(x)
			$("#connectorPosYId34").val(y)
		}
	}
	var connector35 = document.getElementById("connector35");
	if(connector35){
		Drag.init(connector35, null, 0, 360, 0, valueY);
		connector35.onDrag = function(x, y) {
			y = 2 * y
			$("#connectorPosXId35").val(x)
			$("#connectorPosYId35").val(y)
		}
	}
	var connector36 = document.getElementById("connector36");
	if(connector36){
		Drag.init(connector36, null, 0, 360, 0, valueY);
		connector36.onDrag = function(x, y) {
			y = 2 * y
			$("#connectorPosXId36").val(x)
			$("#connectorPosYId36").val(y)
		}
	}
	var connector37 = document.getElementById("connector37");
	if(connector37){
		Drag.init(connector37, null, 0, 360, 0, valueY);
		connector37.onDrag = function(x, y) {
			y = 2 * y
			$("#connectorPosXId37").val(x)
			$("#connectorPosYId37").val(y)
		}
	}
	var connector38 = document.getElementById("connector38");
	if(connector38){
		Drag.init(connector38, null, 0, 360, 0, valueY);
		connector38.onDrag = function(x, y) {
			y = 2 * y
			$("#connectorPosXId38").val(x)
			$("#connectorPosYId38").val(y)
		}
	}
	var connector39 = document.getElementById("connector39");
	if(connector39){
		Drag.init(connector39, null, 0, 360, 0, valueY);
		connector39.onDrag = function(x, y) {
			y = 2 * y
			$("#connectorPosXId39").val(x)
			$("#connectorPosYId39").val(y)
		}
	}
	var connector40 = document.getElementById("connector40");
	if(connector40){
		Drag.init(connector40, null, 0, 360, 0, valueY);
		connector40.onDrag = function(x, y) {
			y = 2 * y
			$("#connectorPosXId40").val(x)
			$("#connectorPosYId40").val(y)
		}
	}
	var connector41 = document.getElementById("connector41");
	if(connector41){
		Drag.init(connector41, null, 0, 360, 0, valueY);
		connector41.onDrag = function(x, y) {
			y = 2 * y
			$("#connectorPosXId41").val(x)
			$("#connectorPosYId41").val(y)
		}
	}
	var connector42 = document.getElementById("connector42");
	if(connector42){
		Drag.init(connector42, null, 0, 360, 0, valueY);
		connector42.onDrag = function(x, y) {
			y = 2 * y
			$("#connectorPosXId42").val(x)
			$("#connectorPosYId42").val(y)
		}
	}
	var connector43 = document.getElementById("connector43");
	if(connector43){
		Drag.init(connector43, null, 0, 360, 0, valueY);
		connector43.onDrag = function(x, y) {
			y = 2 * y
			$("#connectorPosXId43").val(x)
			$("#connectorPosYId43").val(y)
		}
	}
	var connector44 = document.getElementById("connector44");
	if(connector44){
		Drag.init(connector44, null, 0, 360, 0, valueY);
		connector44.onDrag = function(x, y) {
			y = 2 * y
			$("#connectorPosXId44").val(x)
			$("#connectorPosYId44").val(y)
		}
	}
	var connector45 = document.getElementById("connector45");
	if(connector45){
		Drag.init(connector45, null, 0, 360, 0, valueY);
		connector45.onDrag = function(x, y) {
			y = 2 * y
			$("#connectorPosXId45").val(x)
			$("#connectorPosYId45").val(y)
		}
	}
	var connector46 = document.getElementById("connector46");
	if(connector46){
		Drag.init(connector46, null, 0, 360, 0, valueY);
		connector46.onDrag = function(x, y) {
			y = 2 * y
			$("#connectorPosXId46").val(x)
			$("#connectorPosYId46").val(y)
		}
	}
	var connector47 = document.getElementById("connector47");
	if(connector47){
		Drag.init(connector47, null, 0, 360, 0, valueY);
		connector47.onDrag = function(x, y) {
			y = 2 * y
			$("#connectorPosXId47").val(x)
			$("#connectorPosYId47").val(y)
		}
	}
	var connector48 = document.getElementById("connector48");
	if(connector48){
		Drag.init(connector48, null, 0, 360, 0, valueY);
		connector48.onDrag = function(x, y) {
			y = 2 * y
			$("#connectorPosXId48").val(x)
			$("#connectorPosYId48").val(y)
		}
	}
	var connector49 = document.getElementById("connector49");
	if(connector49){
		Drag.init(connector49, null, 0, 360, 0, valueY);
		connector49.onDrag = function(x, y) {
			y = 2 * y
			$("#connectorPosXId49").val(x)
			$("#connectorPosYId49").val(y)
		}
	}
	var connector50 = document.getElementById("connector50");
	if(connector50){
		Drag.init(connector50, null, 0, 360, 0, valueY);
		connector50.onDrag = function(x, y) {
			y = 2 * y
			$("#connectorPosXId50").val(x)
			$("#connectorPosYId50").val(y)
		}
	}
}
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