<%@page import="net.transitionmanager.security.Permission"%>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta name="layout" content="topNav" />
		<title>Project List - ${active=='active' ? 'Active' : 'Completed'} Projects</title>
		<asset:stylesheet href="css/jqgrid/ui.jqgrid.css" />
		<script src="${resource(dir:'js',file:'jquery.form.js')}"></script>
		<jqgrid:resources />
		<g:javascript src="jqgrid-support.js" />

		<script type="text/javascript">
			$(document).ready(function() {
				var listCaption ="Projects: \
				<tds:hasPermission permission="${Permission.ProjectCreate}">\
					<span class='capBtn'><input type='button' class='create' value='Create Project' onClick=\"window.location.href=\'"+contextPath+"/project/create\'\"/></span> \
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
					rowList="${ raw(com.tdsops.common.ui.Pagination.optionsAsText()) }"
					postData="{isActive:isActive}"
					gridComplete="function(){bindResize('projectGridId')}"
					showPager="true">
					<jqgrid:navigation id="projectGridId" add="false" edit="false" del="false" search="false"/>
					<jqgrid:refreshButton id="projectGridId" />
				</jqgrid:grid>
				TDS.jqGridFilterToolbar('projectGridId');

				$.jgrid.formatter.integer.thousandsSeparator='';
				function myLinkFormatter (cellvalue, options, rowObjcet) {
					var value = cellvalue ? cellvalue : ''
					return '<a href="'+contextPath+'/project/addUserPreference/'+options.rowId+'">'+value+'</a>'
				}
			});
		</script>
		<style>
			/*TODO: REMOVE ON COMPLETE MIGRATION */
			div.content-wrapper {
				background-color: #ecf0f5 !important;
			}
		</style>
	</head>
	<body>
	<tds:subHeader title="Project List - ${active=='active' ? 'Active' : 'Completed'} Projects" crumbs="['Project', active=='active' ? 'Active' : 'Completed' ]"/>
		<div class="body fluid">
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
			$('.menu-projects-active-projects').addClass('active');
			$('.menu-parent-projects').addClass('active');
		</script>
	</body>
</html>
