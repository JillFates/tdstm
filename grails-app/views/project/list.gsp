<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta name="layout" content="projectHeader" />
		<title>Project List - ${active=='active' ? 'Active' : 'Completed'} Projects</title>
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css/jqgrid',file:'ui.jqgrid.css')}" />
		<script src="${resource(dir:'js',file:'jquery.form.js')}"></script>
		<jqgrid:resources />
		<g:javascript src="jqgrid-support.js" />
		
		<script type="text/javascript">
			$(document).ready(function() {
				var listCaption ="Projects: \
				<tds:hasPermission permission='CreateProject'>\
					<span class='capBtn'><input type='button' class='create' value='Create Project' onClick=\"window.location.href=\'"+contextPath+"/project/create\'\"/></span> \
					<span class='capBtn'><input type='button' class='create' value='Create Demo Project' onClick=\"window.location.href=\'"+contextPath+"/projectUtil/createDemo\'\" /></span>\
				</tds:hasPermission>\
				<span class='capBtn' style='${active=='active' ? 'display:none':'' }'><a href=\'"+contextPath+"/project/list?active=active\'> \
				<input type='button' value='Show Active Projects'/></a></span>\
				<span class='capBtn' style='${active=='completed' ? 'display:none':'' }'><a href=\'"+contextPath+"/project/list?active=completed\'> \
				<input type='button' value='Show Completed Projects'/></a></span>"
				
				var isActive = '${active}'
				<jqgrid:grid id="projectGridId" url="'${createLink(action: 'listJson')}'"
					colNames="'Project Code','Name', 'Start Date','Completion Date', 'Comment'"
					colModel="{name:'projectCode', index: 'projectCode', width:'150',formatter: myLinkFormatter},
								  {name:'name', width:'150'},
								  {name:'startDate',width:'150'},
								  {name:'completionDate', width:'150'},
								  {name:'comment',width:'100'}"
					sortname="'projectCode'"
					caption="listCaption"
					height="'100%'"
					postData="{isActive:isActive}"
					gridComplete="function(){bindResize('projectGridId')}"
					showPager="true">
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
			<h1>Project List - ${active=='active' ? 'Active' : 'Completed'} Projects</h1>
			<g:if test="${flash.message}">
				<div id="messageDivId" class="message">
					${flash.message}
				</div>
			</g:if>
			<div>
				<div id="messageId" class="message" style="display: none"></div>
			</div>
			
			<div id="gridDivId" style="width: 50% !important;">
				<jqgrid:wrapper id="projectGridId" />
			</div>
		</div>
		<script>
			currentMenuId = "#projectMenu";
			$("#projectMenuId a").css('background-color','#003366')
		</script>
	</body>
</html>