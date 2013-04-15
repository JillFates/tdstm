<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="projectHeader" />
        <title>Manufacturer List</title>
<link type="text/css" rel="stylesheet" href="${resource(dir:'css/jqgrid',file:'ui.jqgrid.css')}" />
<g:javascript src="model.manufacturer.js" />
<jqgrid:resources />
<jqui:resources /> 
<jqgrid:resources />

<script type="text/javascript">
$(document).ready(function() {
	$("#createManufacturerView").dialog({ autoOpen: false })
	$("#showManufacturerView").dialog({ autoOpen: false })
	<jqgrid:grid id="manufacturerId" url="'${createLink(action: 'listJson')}'"
	    colNames="'Name','AKA', 'Description','Models', 'AssetCount'"
	    colModel="{name:'name', index: 'name', width:'100',formatter: myLinkFormatter},
	      			  {name:'aka', editable: true, width:'200',search:false,sortable:false},
	      			  {name:'description', editable: true,width:'100'},
	                  {name:'models', editable: true, width:'40',search:false,sortable:false}, 
	                  {name:'assetCount', editable: true,width:'50',search:false,sortable:false}"
	    sortname="'name'"
	    caption="'Manufacturers'"
	   	height="'100%'"
	    width="1000"
	    rowNum="'25'"
	    rowList= "'25','100','500','1000'"
	    viewrecords="true"
	    showPager="true"
	    datatype="'json'">
	    <jqgrid:filterToolbar id="manufacturerId" searchOnEnter="false" />
	    <jqgrid:navigation id="manufacturerId" add="false" edit="false" del="false" search="false" refresh="true" />
	    <jqgrid:resize id="manufacturerId" resizeOffset="-2" />
	</jqgrid:grid>
	$.jgrid.formatter.integer.thousandsSeparator='';
	function myLinkFormatter (cellvalue, options, rowObjcet) {
		var value = cellvalue ? cellvalue : ''
			return '<a href="javascript:showOrEditModelManuDetails(\'manufacturer\','+options.rowId+',\'Manufacturer\',\'show\',\'Show\')">'+value+'</a>'
	}
});
</script>
</head>
<body>
<div class="body">
<h1>Manufacturer List</h1>
<g:if test="${flash.message}">
	<div id="messageDivId" class="message">${flash.message}</div>
</g:if>
<div >
	<div id="messageId" class="message" style="display:none"></div>
</div>
<jqgrid:wrapper id="manufacturerId" />
<div id="createManufacturerView" style="display: none;" ></div>
<div id="showManufacturerView" style="display: none;" ></div>
<div class="buttons">
	    <span class="button"><input type="button" class="save" value="Create Manufacturer"
		onclick="createModelManuDetails('manufacturer','Manufacturer')"/></span>
</div>
</div>
</body>
</html>