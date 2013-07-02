<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="projectHeader" />
<title>Project List</title>
<link type="text/css" rel="stylesheet"
	href="${resource(dir:'css/jqgrid',file:'ui.jqgrid.css')}" />
<script src="${resource(dir:'js',file:'jquery.form.js')}"></script>
<jqgrid:resources />
<jqui:resources />
<jqgrid:resources />

<script type="text/javascript">
		
$(document).ready(function() {
	var listCaption ="Projects: \
	<tds:hasPermission permission='CreateProject'>\
		<span class='capBtn'><input type='button' value='Create Project' onClick=\"window.location.href=\'"+contextPath+"/project/create\'\"/></span> \
		<span class='capBtn'><input type='button' class='save' onClick=\"window.location.href=\'"+contextPath+"/projectUtil/createDemo\'\" value='Create Demo Project' /></span>\
	</tds:hasPermission>\
	<span class='capBtn' style='${active=='active' ? 'display:none':'' }'><a href=\'"+contextPath+"/project/list?active=active\'> \
	<input type='button' value='Show Active Projects'/></a></span>\
	<span class='capBtn' style='${active=='completed' ? 'display:none':'' }'><a href=\'"+contextPath+"/project/list?active=completed\'> \
	<input type='button' value='Show Completed Projects'/></a></span>"
	
	var isActive = '${active}'
	<jqgrid:grid id="projectGridId" url="'${createLink(action: 'listJson')}'"
		colNames="'Project Code','Name', 'Start Date','Completion Date', 'Comment'"
		colModel="{name:'projectCode', index: 'projectCode', width:'150',formatter: myLinkFormatter},
					  {name:'name', editable: true, width:'150'},
					  {name:'startDate', editable: true,width:'150'},
					  {name:'completionDate', editable: true, width:'150'},
					  {name:'comment', editable: true,width:'100'}"
		sortname="'projectCode'"
		caption="listCaption"
		height="'100%'"
		width="'500px'"
		rowNum="'25'"
		rowList= "'25','100','500','1000'"
		viewrecords="true"
		postData="{isActive:isActive}"
		showPager="true"
		datatype="'json'">
		<jqgrid:filterToolbar id="projectGridId" searchOnEnter="false" />
		<jqgrid:navigation id="projectGridId" add="false" edit="false" del="false" search="false"/>
		<jqgrid:refreshButton id="projectGridId" />
	</jqgrid:grid>
	$.jgrid.formatter.integer.thousandsSeparator='';
	function myLinkFormatter (cellvalue, options, rowObjcet) {
		var value = cellvalue ? cellvalue : ''
		return '<a href="'+contextPath+'/project/addUserPreference/'+options.rowId+'">'+value+'</a>'
	}
});
		</script>
</head>
<body>
	<div class="body fluid">
		<h1>Project List</h1>
		<g:if test="${flash.message}">
			<div id="messageDivId" class="message">
				${flash.message}
			</div>
		</g:if>
		<div>
			<div id="messageId" class="message" style="display: none"></div>
		</div>
		<table id="gridTableId" style="width: 50% !important;">
			<tr>
				<td><jqgrid:wrapper id="projectGridId" /></td>
			</tr>
			<tr>
				<td><div class="buttons">
				<g:form>
					<tds:hasPermission permission='CreateProject '>
  						<span class="button"><g:actionSubmit class="save" action="Create" value="Create Project" /></span>
  					</tds:hasPermission>
  				</g:form>
  				</div></td>
			</tr>
		</table>
	</div>
</body>
</html>