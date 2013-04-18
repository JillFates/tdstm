<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="projectHeader" />
        <title>Model List</title>
<link type="text/css" rel="stylesheet" href="${resource(dir:'css/jqgrid',file:'ui.jqgrid.css')}" />
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'rackLayout.css')}" />
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.datepicker.css')}" />
    
<g:javascript src="model.manufacturer.js" />
<g:javascript src="drag_drop.js" />
<script src="${resource(dir:'js',file:'jquery.form.js')}"></script> 	
<jqgrid:resources />
<jqui:resources /> 
<jqgrid:resources />

<script type="text/javascript">
$(document).ready(function() {
	$("#createModelView").dialog({ autoOpen: false })
	$("#showModelView").dialog({ autoOpen: false })
	<jqgrid:grid id="modelId" url="'${createLink(action: 'listJson')}'"
	    colNames="'Model Name','Manufacturer', 'Description','Asset Type', 'Power','No Of Connectors','Assets ','Version','Source TDS','Model Status'"
	    colModel="{name:'modelName', index: 'modelName', width:'150',formatter: myLinkFormatter},
	      			  {name:'manufacturer', editable: true, width:'100'},
	      			  {name:'description', editable: true,width:'100'},
	                  {name:'assetType', editable: true, width:'100'},
	                  {name:'powerUse', editable: true,width:'50'}, 
	                  {name:'modelConnectors', editable: true,width:'80',search:false},
	                  {name:'assets', editable: false,width:'50',search:false},
	                  {name:'sourceTDSVersion', editable: false,width:'50',search:false},
	                  {name:'sourceTDS', editable: false,width:'60'},
	                  {name:'modelStatus',editable: false,width:'60' }"
	    sortname="'modelName'"
	    caption="'Models'"
	   	height="'100%'"
	    width="1100"
	    rowNum="'25'"
	    rowList= "'25','100','500','1000'"
	    viewrecords="true"
	    showPager="true"
	    datatype="'json'">
	    <jqgrid:filterToolbar id="modelId" searchOnEnter="false" />
	    <jqgrid:navigation id="modelId" add="false" edit="false" del="false" search="false" refresh="true" />
	    <jqgrid:resize id="modelId" resizeOffset="-2" />
	</jqgrid:grid>
	$.jgrid.formatter.integer.thousandsSeparator='';
	function myLinkFormatter (cellvalue, options, rowObjcet) {
		var value = cellvalue ? cellvalue : ''
		return '<a href="javascript:showOrEditModelManuDetails(\'model\','+options.rowId+',\'Model\',\'show\',\'Show\')">'+value+'</a>'
	}
});
</script>
</head>
<body>
<div class="body">
<h1>Model List</h1>
<g:if test="${flash.message}">
	<div id="messageDivId" class="message">${flash.message}</div>
</g:if>
<div >
	<div id="messageId" class="message" style="display:none"></div>
</div>
<jqgrid:wrapper id="modelId" />
<div id="createModelView" style="display: none;" ></div>
<div id="showModelView" style="display: none;"></div>
<div class="buttons">
  <span class="button"><input type="button" class="save" value="Create Model" onclick="createModelManuDetails('model','Model')" /></span>
</div>
</div>
</body>
</html>