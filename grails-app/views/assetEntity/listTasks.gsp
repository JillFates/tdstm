<%@page import="com.tds.asset.AssetComment"%>
<%@page import="com.tdssrc.grails.TimeUtil"%>
<%@page import="com.tdssrc.grails.StringUtil"%>
<%@page import="com.tdssrc.grails.HtmlUtil"%>
<%@page import="com.tdsops.tm.enums.domain.AssetCommentType"%>
<%@page import="com.tdsops.tm.enums.domain.AssetCommentStatus"%>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
	<meta name="layout" content="projectHeader" />
	<title>Task Manager</title>
	<g:javascript src="asset.tranman.js" />
	<g:javascript src="asset.comment.js" />
	<g:javascript src="entity.crud.js" />
	<g:javascript src="model.manufacturer.js"/>
	<g:javascript src="angular/angular.min.js" />
	<g:javascript src="angular/plugins/angular-ui.js"/>
	<g:javascript src="angular/plugins/angular-resource.js" />
    <script type="text/javascript" src="${resource(dir:'components/core',file:'core.js')}"></script>
    <script type="text/javascript" src="${resource(dir:'components/comment',file:'comment.js')}"></script>
    <script type="text/javascript" src="${resource(dir:'components/asset',file:'asset.js')}" /></script>
	<g:javascript src="cabling.js"/>
	<jqgrid:resources />
	<g:javascript src="jqgrid-support.js" />
	<g:javascript src="bootstrap.js" />
	<g:javascript src="angular/plugins/ui-bootstrap-tpls-0.10.0.min.js" />
	<g:javascript src="angular/plugins/ngGrid/ng-grid-2.0.7.min.js" />
	<g:javascript src="angular/plugins/ngGrid/ng-grid-layout.js" />
	<link type="text/css" rel="stylesheet" href="${g.resource(dir:'css',file:'ui.datepicker.css')}" />
	<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.datetimepicker.css')}" />
	<link type="text/css" rel="stylesheet" href="${resource(dir:'css/jqgrid',file:'ui.jqgrid.css')}" />
	<link type="text/css" rel="stylesheet" href="${resource(dir:'components/comment',file:'comment.css')}" />
	<script type="text/javascript">
		$(document).ready(function() {
			$('#issueTimebar').width("100%")
			
			$('#assetMenu').show();
			$("#showEntityView").dialog({ autoOpen: false })
			$("#editEntityView").dialog({ autoOpen: false })
			$("#createEntityView").dialog({ autoOpen: false })
			$("#editManufacturerView").dialog({ autoOpen: false})
			$("#manufacturerShowDialog").dialog({ autoOpen: false })
			$("#modelShowDialog").dialog({ autoOpen: false })
			$("#cablingDialogId").dialog({ autoOpen:false })
			currentMenuId = "#assetMenu";
			$("#teamMenuId a").css('background-color','#003366')
			$("#viewGraphSpanId").css('margin-left',$(window).width()*3.3/100+'%')
			$("#selectTimedBarId").val(${timeToUpdate})			
			taskManagerTimePref = ${timeToUpdate}
			$(window).resize(function() {
				B2.Restart(taskManagerTimePref)
			});
			
			if (taskManagerTimePref != 0) {
				B2.Start(taskManagerTimePref);
			} else {
				B2.Pause(0);
			}
			var event = ${filterEvent}
			var justRemaining = ${justRemaining}
			var justMyTasks = ${justMyTasks}
			var viewUnpublished = ${viewUnpublished}
			var filter = '${filter}'
			var comment = '${comment}'
			var taskNumber = '${taskNumber}'
			var assetEntity = '${assetName}'
			var assetType = '${assetType}'
			var dueDate = '${dueDate}'
			var status = '${status}'
			var assignedTo = '${assignedTo}'
			var role = '${role}'
			var category = '${category}'
			var sizePref = '${sizePref}'
			var windowWidth = $(window).width() - $(window).width()*5/100 ;
			var listCaption ="Tasks: \
				<tdsactionbutton id='createTask' label='Create Task' icon='/icons/table_add.png' link='' ng-click='comments.createCommentBy(\"${AssetCommentType.TASK}\",\"\",\"\")'></tdsactionbutton>&nbsp; \
				<tdsactionbutton id='bulkEdit' label='Bulk Edit' icon='' link='' ng-click='comments.bulkEditTasks()'></tdsactionbutton>"

			<jqgrid:grid id="taskListId"  url="'${createLink(action: 'listTaskJSON')}'"
				colNames="'Action', 'Task', 'Description', '${modelPref['1']}', '${modelPref['2']}', 'Updated', 'Due', 'Status',
					'${modelPref['3']}', '${modelPref['4']}', '${modelPref['5']}', 'Suc.', 'Score', 'id', 'statusCss'"
				colModel="{name:'act', index: 'act' , sortable: false, formatter: myCustomFormatter, search:false, width:50, fixed:true},
					{name:'taskNumber', formatter:taskFormatter, width:80},
					{name:'comment', width:680, formatter:taskFormatter},
					{name:'${taskPref['1']}', formatter:assetFormatter, width:200},
					{name:'${taskPref['2']}', formatter:taskFormatter, width:200},
					{name:'updated', formatter: updatedFormatter,sortable:false,search:false},
					{name:'dueDate', formatter: dueFormatter},
					{name:'status', formatter: statusFormatter},
					{name:'${taskPref['3']}', formatter:taskFormatter, width:200},
					{name:'${taskPref['4']}', formatter:taskFormatter, width:200},
					{name:'${taskPref['5']}', formatter:taskFormatter, width:200},
					{name:'suc', formatter:taskFormatter,sortable:false,search:false, width:50},
					{name:'score', formatter:taskFormatter, search:false, width:70},
					{name:'id', hidden: true},
					{name:'statusCss', hidden: true}"
				caption="listCaption"
				rowNum="sizePref"
				scrollOffset="0"
				gridComplete="function(){bindResize('taskListId');recompileDOM('taskListIdWrapper');}"
				postData="{moveEvent:event, justRemaining:justRemaining, justMyTasks:justMyTasks, filter:filter, comment:comment, taskNumber:taskNumber,
					assetEntity:assetEntity, assetType:assetType, dueDate:dueDate, status:status, assignedTo:assignedTo, role:role, category:category, viewUnpublished : viewUnpublished}"
				showPager="true">
				<jqgrid:filterToolbar id="taskListId" searchOnEnter="false" />
				<jqgrid:navigation id="taskListId" add="false" edit="false" 
					  del="false" search="false" refresh="false" />
				<jqgrid:refreshButton id="taskListId" />
			</jqgrid:grid>
			populateFilter();

			<g:each var="key" in="['1','2','3','4','5']">
				var taskPref= '${taskPref[key]}';
				$("#taskListIdGrid_"+taskPref).append('<img src="../images/select2Arrow.png" class="selectImage customizeSelect editSelectimage_'+${key}+'" onclick="showSelect(\''+taskPref+'\',\'taskList\',\''+${key}+'\')">');
			</g:each>
		})
		
		$.jgrid.formatter.integer.thousandsSeparator='';
		
		function checkSelectedEvent(event) {
			 var moveEvent = $("#moveEventId").val()
			 if(moveEvent == '0'){
			 	alert("Please select an event first.")
			 	event.preventDefault()
			 }
		};

		function myCustomFormatter (cellVal,options,rowObject) {
			var editButton = '<a ng-click="comments.editCommentById(\''+options.rowId+'\',\'task\')">'+
				"<img src='${resource(dir:'icons',file:'table_edit.png')}' border='0px'/>"+"</a>&nbsp;&nbsp;"
			return editButton
		}
		function isPublishedFormatter(cellVal,options,rowObject) {
			return '<span class="cellWithoutBackground pointer" id="span_'+options.rowId+'" >' + (rowObject[17] ? rowObject[17] : "false") + '</span>';
		}
		function taskFormatter(cellVal,options,rowObject) {
			return '<span class="cellWithoutBackground pointer" id="' + options.colModel.name + '_'+options.rowId+'" action-bar-cell config-table="config.table" comment-id="'+options.rowId+'" asset-id="'+rowObject[16]+'" status="'+rowObject[7]+'">' + (cellVal || cellVal == 0 ? cellVal :"") + '</span>';
		}
		function statusFormatter(cellVal,options,rowObject){
			return '<span id="status_'+options.rowId+'" class="cellWithoutBackground '+rowObject[13] +' " action-bar-cell config-table="config.table" comment-id="'+options.rowId+'" asset-id="'+rowObject[16]+'" status="'+rowObject[7]+'">' + cellVal + '</span>';
		 }
		
		function updatedFormatter(cellVal,options,rowObject){
			 return '<span id="span_'+options.rowId+'" class="cellWithoutBackground '+rowObject[14] +'" action-bar-cell config-table="config.table" comment-id="'+options.rowId+'" asset-id="'+rowObject[16]+'" status="'+rowObject[7]+'">' + cellVal + '</span>';
		}
		function dueFormatter(cellVal,options,rowObject){
			return '<span id="span_'+options.rowId+'" class=" '+rowObject[15] +'" master="true" action-bar-cell config-table="config.table" comment-id="'+options.rowId+'" asset-id="'+rowObject[16]+'" status="'+rowObject[7]+'">' + cellVal + '</span>';
		}
		function assetFormatter(cellVal,options,rowObject){
			return options.colModel.name == "assetName" && cellVal ? '<span class="cellWithoutBackground pointer" onclick= "getEntityDetails(\'listTask\', \''+rowObject[17]+'\', '+rowObject[16]+')\" >' + (cellVal) + '</span>' :
				(cellVal || cellVal == 0 ? taskFormatter(cellVal,options,rowObject) : "<span class='cellWithoutBackground pointer'></span>")
		}
		
		function populateFilter(){
			$("#gs_comment").val('${comment}')
			$("#gs_taskNumber").val('${taskNumber}')
			$("#gs_assetEntity").val('${assetName}')
			$("#gs_assetType").val('${assetType}')
			$("#gs_dueDate").val('${dueDate}')
			$("#gs_status").val('${status}')
			$("#gs_assignedTo").val('${assignedTo}')
			$("#gs_role").val('${role}')
			$("#gs_category").val('${category}')
		}
		$(document).keyup(function(e) {
			// esc to stop timer
			if (e.keyCode == 27) { if(B2 != '' && taskManagerTimePref != 0){ B2.Restart( taskManagerTimePref ); }}   
		});
	</script>
</head>
<body>
	<input type="hidden" id="timeBarValueId" value="0"/>
	<div id="outerBodyId" class="body" ng-app="tdsComments" ng-controller="tds.comments.controller.MainController as comments">
		<div class="taskTimebar" id="issueTimebar" >
			<div id="issueTimebarId"></div>
		</div>
		<div class="body fluid">
			<h1>Task Manager</h1>
			<g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
			</g:if>
			<div class="alert alert-{{alert.type}}" ng-repeat="alert in alerts.list" ng-class="{animateShow: !alert.hidden}">
				<button type="button" class="alert-close" aria-hidden="true" ng-click="alerts.closeAlert($index)">&times;</button>
				<span ng-bind="alert.msg"></span>
			</div>
			<div id="taskMessageDiv" class="message" style="display: none;"></div>
			<input type="hidden" id="manageTaskId" value="manageTask"/>
			<form name="commentForm" id="commentForm" method="post" action="listTasks">
			<input type="hidden" name="justRemaining" id="justRemaining" value="${justRemaining}" />
			<input type="hidden" name="justMyTasks"   id="justMyTasks"   value="${justMyTasks}"/>
			<input type="hidden" name="viewUnpublished"   id="viewUnpublished"   value="${viewUnpublished}"/>
			<input type="hidden" id="myPage" value="taskManager" />
			<span >
				<b>Event </b>
			 	<g:select from="${moveEvents}" name="moveEvent" id="moveEventId" optionKey="id" optionValue="name" noSelection="${['0':' All']}" value="${filterEvent}" onchange="submitForm()" />
				&nbsp;&nbsp;
				<input type="checkbox" id="justRemainingCB" ${ (justRemaining == '1' ? 'checked="checked"': '') } onclick="toggleCheckbox(this, 'justRemaining');"  />
				<b> <label for="justRemainingCB" >Just Remaining</label></b>
				<input type="checkbox" id="justMyTasksCB" ${ (justMyTasks=="1" ? 'checked="checked"':'') } onclick="toggleCheckbox(this, 'justMyTasks');"/>
				<b><label for="justMyTasksCB" > Just Mine</label></b>&nbsp;&nbsp;
				<tds:hasPermission permission="PublishTasks">
					<input type="checkbox" id="viewUnpublishedCB" ${ (viewUnpublished=="1" ? 'checked="checked"':'') } onclick="toggleCheckbox(this, 'viewUnpublished');"/>
					<b><label for="viewUnpublishedCB" > View unpublished</label></b>&nbsp;&nbsp;
				</tds:hasPermission>

				<span style="float: right">
					<span style="margin-right: 30px;">
						<tdsactionbutton id="graph" label="View Task Graph" icon="/icons/tds_task_graph.png" link="/task/taskGraph?moveEventId=${filterEvent}" click="checkSelectedEvent"></tdsactionbutton>&nbsp;

						<tdsactionbutton id="timeline" label="View Timeline" icon="/icons/timeline_marker.png" link="/task/taskTimeline"></tdsactionbutton>&nbsp;
					</span>
					<input type="button" value="Refresh" onclick="loadGrid()" style="cursor: pointer;">&nbsp;
					<select id="selectTimedBarId"
					    onchange="${remoteFunction(controller:'clientConsole', action:'setTimePreference', params:'\'timer=\'+ this.value +\'&prefFor=TASKMGR_REFRESH\' ', onComplete:'changeTimebarPref(e)') }">
						<option value="0">Manual</option>
						<option value="60" selected="selected">1 Min</option>
						<option value="120">2 Min</option>
						<option value="180">3 Min</option>
						<option value="240">4 Min</option>
						<option value="300">5 Min</option>
					</select>
				</span>				
			</span>
			<br/></br>
			<jqgrid:wrapper id="taskListId" />
		</div>
		<input type="hidden" id="customizeFieldCount" value="6" />
		<g:each var="key" in="['1','2','3','4','5']">
			<div id="columnCustomDiv_${taskPref[key]}" style="display:none;">
				<div class="columnDiv_${key} customScroll customizeDiv">
					<input type="hidden" id="previousValue_${key}" value="${taskPref[key]}" />
					<g:each var="attribute" in="${attributesList}">
						<label><input type="radio" name="coloumnSelector_${taskPref[key]}" id="coloumnSelector_${taskPref[key]}" value="${attribute}" 
							${taskPref[key]==attribute?'checked':'' } 
							onchange="setColumnAssetPref(this.value,'${key}','Task_Columns')"/> ${attribute}</label><br>
					</g:each>
				</div>
			</div>
		</g:each>
			
		<g:render template="../assetEntity/modelDialog" />
		<div id="showEntityView" style="display: none;"></div>
		<div id="editEntityView" style="display: none;"></div>
		<div id="createEntityView" style="display: none;"></div>
		<div id="editManufacturerView" style="display: none;"></div>
		<div id="cablingDialogId" style="display: none;"></div>
		<g:render template="../assetEntity/newDependency" model="['forWhom':'Server', entities:servers]"></g:render>
	</div>
 <g:render template="initAssetEntityData"/>
 <script type="text/javascript">
function toggleCheckbox (chkbox, field) {
	$('input[name='+field+']').val(chkbox.checked ? '1' : '0')
	submitForm()
}
function submitForm () {
	$('#commentForm').submit()
}

function loadGrid () {
	$(".ui-icon-refresh").click()
	var timePref = $("#selectTimedBarId").val()
	if(timePref != 0){
		B2.Start(timePref);
	} else{
		B2.Pause(0);
	}
}

function pageRefresh () {
	window.location.reload()
}

function Bar (o) {
	var obj = document.getElementById(o.ID);
	this.oop = new zxcAnimate('width',obj,0);
	this.max = $('#issueTimebar').width();
	this.to = null;
}
Bar.prototype = {
	Start:function (sec) {
		clearTimeout(this.to);
		this.oop.animate(0,$('#issueTimebar').width(),sec*1000);
		this.srt = new Date();
		this.sec = sec;
		this.Time();
	},
	Time:function (sec) {
		var oop = this
			,sec=this.sec-Math.floor((new Date()-this.srt)/1000);
		//this.oop.obj.innerHTML=sec+' sec';
		$('#timeBarValueId').val(sec)
		if (sec > 0) {
			this.to = setTimeout(function(){ oop.Time(); },1000);
		} else {
			loadGrid();
		}
	},
	Pause:function (sec) {
		clearTimeout(this.to);
		if (sec == 0) {
			this.oop.animate(sec,'',sec*1000);
		} else {
			this.oop.animate($('#issueTimebarId').width(),$('#issueTimebarId').width(),sec*1000);
		}
	},
	Restart:function (sec) {
		clearTimeout(this.to);
		var second = $('#timeBarValueId').val()
		this.oop.animate($('#issueTimebarId').width(),$('#issueTimebar').width(),second*1000);
		this.srt = new Date();
		this.sec = second;
		this.Time();
	}
}

B2 = new Bar({
	ID:'issueTimebarId'
});
</script>
</body>

</html>