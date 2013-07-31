<%@page import="com.tds.asset.AssetComment"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
<meta name="layout" content="projectHeader" />
        <title>Asset Comment</title>
         <g:javascript src="asset.tranman.js" />
          <g:javascript src="entity.crud.js" />
        <link type="text/css" rel="stylesheet" href="${resource(dir:'css/jqgrid',file:'ui.jqgrid.css')}" />
        <link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.datepicker.css')}" />
        <link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'tds.css')}" />
       <jqgrid:resources />
		
        <script type="text/javascript">
        function onInvokeAction(id) {
            setExportToLimit(id, '');
            createHiddenInputFieldsForLimitAndSubmit(id);
        }
        function onInvokeExportAction(id) {
            var parameterString = createParameterStringForLimit(id);
            location.href = '../list?' + parameterString;
        }
        $(document).ready(function() {
        	$('#assetMenu').show();
        	$("#commentsListDialog").dialog({ autoOpen: false })
 	        $("#createCommentDialog").dialog({ autoOpen: false })
 	        $("#showCommentDialog").dialog({ autoOpen: false })
 	        $("#editCommentDialog").dialog({ autoOpen: false })
 	        $("#showEntityView").dialog({ autoOpen: false })
			$("#editEntityView").dialog({ autoOpen: false })
			$("#createEntityView").dialog({ autoOpen: false })
	    	currentMenuId = "#assetMenu";
	    	$("#assetMenuId a").css('background-color','#003366')
	    	$(".span_ready").parent().addClass("task_ready")
	    	$(".span_hold").parent().addClass("task_hold")
	    	$(".span_started").parent().addClass("task_started")
	    	$(".span_pending").parent().addClass("task_pending")
	    	$(".span_planned").parent().addClass("task_planned")
	    	$(".span_completed").parent().addClass("task_completed")
	    	$(".span_na").parent().addClass("task_na")

	    	<jqgrid:grid id="listCommentGridId" url="'${createLink(action: 'listCommentJson')}'"
				colNames="'Action', 'Description','Updated', 'Type', 'Asset', 'AssetType','category'"
				colModel="{name:'act', index: 'act' , sortable: false, formatter: myCustomFormatter, search:false, width:40, fixed:true},
							  {name:'comment', editable: true, width:'350',formatter: myLinkFormatter},
							  {name:'lastUpdated', editable: true,width:'80',formatter: myLinkFormatter},
							  {name:'commentType', editable: true,width:'80',formatter: myLinkFormatter},
							  {name:'assetName', editable: true, width:'180',formatter: assetFormatter},
							  {name:'assetType', editable: true,width:'80',formatter: myLinkFormatter},
							  {name:'category', editable: true,width:'80',formatter: myLinkFormatter}"
				sortname="'lastUpdated'"
				sortorder="'desc'"
				caption="'Asset Comment:'"
				height="'100%'"
				width="'500px'"
				rowNum="'25'"
				rowList= "'25','100','500','1000'"
				viewrecords="true"
				showPager="true"
				datatype="'json'">
				<jqgrid:filterToolbar id="listCommentGridId" searchOnEnter="false" />
				<jqgrid:navigation id="listCommentGridId" add="false" edit="false" del="false" search="false"/>
				<jqgrid:refreshButton id="listCommentGridId" />
			</jqgrid:grid>
			$.jgrid.formatter.integer.thousandsSeparator='';
			function myLinkFormatter (cellvalue, options, rowObjcet) {
				var value = cellvalue ? cellvalue : ''
					return '<span class="Arrowcursor" onclick="javascript:showAssetComment(\''+options.rowId+'\',\'show\')">'+value+'</span>'
			}
			function myCustomFormatter (cellVal,options,rowObject) {
	        	var editButton = '<a href="javascript:showAssetComment(\''+options.rowId+'\',\'edit\')">'+
	       			"<img src='${resource(dir:'images/skin',file:'database_edit.png')}' border='0px'/>"+"</a>&nbsp;&nbsp;"
	            return editButton
	        }
			function assetFormatter(cellVal,options,rowObject){
	        	return cellVal ? '<span class="Arrowcursor" onclick= "getEntityDetails(\'listComment\', \''+rowObject[5]+'\', '+rowObject[7]+')\" >' + (cellVal) + '</span>' : "" 
	        } 
        });
        </script>
</head>
<body>
	<div class="body">
		<div class="body">
			<h1>Asset Comment</h1>
			<g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
			</g:if>
			<div>
			<div>
			<table id="gridTableId" style="width:58%!important;">
				<tr>
					<td><jqgrid:wrapper id="listCommentGridId" /></td>
				</tr>
			</table>
            </div>
            <div id="showEntityView" style="display: none;"></div>
			<div id="editEntityView" style="display: none;"></div>
			<div id="createEntityView" style="display: none;"></div>
			<div style="display: none;">
			<table id="assetDependencyRow">
				<tr>
					<td><g:select name="dataFlowFreq" from="${assetDependency.constraints.dataFlowFreq.inList}"></g:select></td>
					<td><g:select name="entity" from="['Server','Application','Database','Storage']" onchange='updateAssetsList(this.name, this.value)'></g:select></td>
					<td><span id="Server"><g:select name="asset" from="${servers}" optionKey="${-2}" optionValue="${1}" style="width:90px;"></g:select></span></td>
					<td><g:select name="dtype" from="${dependencyType.value}"  optionValue="value"></g:select></td>
					<td><g:select name="status" from="${dependencyStatus.value}" optionValue="value"></g:select></td>
				</tr>
			</table>
			</div>
			<div style="display: none;">
				<span id="Application"><g:select name="asset" from="${applications}" optionKey="${-2}" optionValue="${1}" style="width:90px;"></g:select></span>
				<span id="Database"><g:select name="asset" from="${dbs}" optionKey="${-2}" optionValue="${1}" style="width:90px;"></g:select></span>
				<span id="Storage"><g:select name="asset" from="${files}" optionKey="${-2}" optionValue="${1}" style="width:90px;"></g:select></span>
			</div>
  </div>
  
 <g:render template="commentCrud"/> 
 </div>
 </div>
 </body>
 
</html>
