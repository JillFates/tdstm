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
			$("#showOrMergeId").dialog({ autoOpen: false })
			var listCaption ="Models: \
						<span class='capBtn'><input type='button' value='Create Model' onclick='createModelManuDetails(\"model\",\"Model\")'/></span> \
						<span class='capBtn'><input type='button' id='compareMergeId' value='Compare/Merge' onclick='compareOrMerge()' disabled='disabled'/></span>"
			<jqgrid:grid id="modelId" url="'${createLink(action: 'listJson')}'"
				colNames="'Model Name','Manufacturer', 'Description','Asset Type', 'Power','No Of Connectors','Assets ','Version','Source TDS','Model Status'"
				colModel="{name:'modelName', index: 'modelName', width:'150',formatter: myLinkFormatter},
							  {name:'manufacturer', editable: true, width:'100'},
							  {name:'description', editable: true,width:'100'},
							  {name:'assetType', editable: true, width:'100'},
							  {name:'powerUse', editable: true,width:'50'},
							  {name:'modelConnectors', editable: true,width:'80'},
							  {name:'assetsCount', editable: false,width:'50'},
							  {name:'sourceTDSVersion', editable: false,width:'50'},
							  {name:'sourceTDS', editable: false,width:'60'},
							  {name:'modelStatus',editable: false,width:'60'}"
				sortname="'modelName'"
				caption="listCaption"
				height="'100%'"
				rowNum="'25'"
				rowList= "'25','100','500','1000'"
				viewrecords="true"
				multiselect="true"
				showPager="true"
				loadComplete="initCheck"
				datatype="'json'">
				<jqgrid:filterToolbar id="modelId" searchOnEnter="false" />
				<jqgrid:navigation id="modelId" add="false" edit="false" del="false" search="false"/>
				<jqgrid:refreshButton id="modelId" />
			</jqgrid:grid>
			$.jgrid.formatter.integer.thousandsSeparator='';
			function myLinkFormatter (cellvalue, options, rowObjcet) {
				var value = cellvalue ? cellvalue : ''
				return '<a href="javascript:showOrEditModelManuDetails(\'model\','+options.rowId+',\'Model\',\'show\',\'Show\')">'+value+'</a>'
			}
			function initCheck() {
				 $('.cbox').change(function() {
					 var checkedLen = $('.cbox:checkbox:checked').length
					 if(checkedLen > 1 && checkedLen < 5) {
						$("#compareMergeId").removeAttr("disabled")
					 }else{
						$("#compareMergeId").attr("disabled","disabled")
					 }
				})
			}
			resizeGrid()
		});
		$(window).resize(resizeGrid);
		
		// Called when the window is resized to resize the grid wrapper 
		function resizeGrid(){
			$('#modelIdWrapper').width($('.fluid').width()-16) // 16 pixels comptensates for the border/padding/etc
			$('#modelIdGrid').fluidGrid({ base:'#modelIdWrapper', offset: 0 });
		}
		
		</script>
	</head>
	<body>
		<div class="body fluid">
			<h1>Model List</h1>
			<g:if test="${flash.message}">
				<div id="messageDivId" class="message" >${flash.message}</div>
			</g:if>
			<div >
				<div id="messageId" class="message" style="display:none">
				</div>
			</div>
			<span id="spinnerId" style="display: none">Merging ...<img alt="" src="${resource(dir:'images',file:'spinner.gif')}"/></span>
			<div id="createModelView" style="display: none;" ></div>
			<div id="showModelView" style="display: none;"></div>
			<div id="showOrMergeId" style="display: none;" title="Compare/Merge Models"></div>
			<table id="gridTableId">
				<tr>
					<td><jqgrid:wrapper id="modelId" /></td>
				</tr>
				<tr>
					<td><div class="buttons">
					  <span class="button"><input type="button" class="save" value="Create Model" onclick="createModelManuDetails('model','Model')" /></span>
					</div></td>
				</tr>
			</table>
		</div>
	</body>
</html>