<%@page import="com.tds.asset.AssetComment"%>
<%@page import="com.tdssrc.grails.TimeUtil"%>
<%@page import="com.tdssrc.grails.StringUtil"%>
<%@page import="com.tdssrc.grails.HtmlUtil"%>
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
	<g:javascript src="cabling.js"/>
	<jqgrid:resources />
	<g:javascript src="jqgrid-support.js" />
	<link type="text/css" rel="stylesheet" href="${g.resource(dir:'css',file:'ui.datepicker.css')}" />
	<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.datetimepicker.css')}" />
	<link type="text/css" rel="stylesheet" href="${resource(dir:'css/jqgrid',file:'ui.jqgrid.css')}" />
	<script type="text/javascript">
		$(document).ready(function() {
			$('#issueTimebar').width("100%")
			
			$('#assetMenu').show();
			$("#commentsListDialog").dialog({ autoOpen: false })
			$("#createCommentDialog").dialog({ autoOpen: false })
			$("#showCommentDialog").dialog({ autoOpen: false })
			$("#editCommentDialog").dialog({ autoOpen: false })
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
			$("#viewtaskgraph_button_graph").click(function(event){
				 var moveEvent = $("#moveEventId").val()
				 if(moveEvent == '0'){
				 	alert("Please select an event first.")
				 	event.preventDefault()
				 }
			});
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
				<span class='capBtn'><input type='button' value='Create Task' onclick='createIssue()'/></span> \
				<span class='capBtn'><input type='button' id='bulkEditId' class='bulkEdit' value='Bulk Edit' data-method='bulkEditTasks' /></span>"
					
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
					{name:'${taskPref['3']}', formatter:assignedFormatter, width:200},
					{name:'${taskPref['4']}', formatter:taskFormatter, width:200},
					{name:'${taskPref['5']}', formatter:taskFormatter, width:200},
					{name:'suc', formatter:taskFormatter,sortable:false,search:false, width:50},
					{name:'score', formatter:taskFormatter, search:false, width:70},
					{name:'id', hidden: true},
					{name:'statusCss', hidden: true}"
				caption="listCaption"
				rowNum="sizePref"
				scrollOffset="0"
				gridComplete="function(){bindResize('taskListId')}"
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
			
			$('.bulkEdit').on('click',function(){
				var method = $(this).data('method');
				if(method == "bulkEditTasks"){
					$(this).attr('disabled', 'disabled');
					$(this).data('method','hideActionBars');
					bulkEditTasks();
				}else{
					$(this).data('method','bulkEditTasks');
					hideActionBars();
				}
			});
		})
		
		$.jgrid.formatter.integer.thousandsSeparator='';
		
		function myCustomFormatter (cellVal,options,rowObject) {
			var editButton = '<a href="javascript:showAssetComment(\''+options.rowId+'\',\'edit\')">'+
				"<img src='${resource(dir:'images/skin',file:'database_edit.png')}' border='0px'/>"+"</a>&nbsp;&nbsp;"
			return editButton
		}
		function isPublishedFormatter(cellVal,options,rowObject) {
			return '<span class="cellWithoutBackground pointer" id="span_'+options.rowId+'" >' + (rowObject[17] ? rowObject[17] : "false") + '</span>';
		}
		function taskFormatter(cellVal,options,rowObject) {
			return '<span class="cellWithoutBackground pointer" id="span_'+options.rowId+'" onclick="getActionBarGrid('+options.rowId+')" >' + (cellVal || cellVal == 0 ? cellVal :"") + '</span>';
		}
		function assignedFormatter(cellVal,options,rowObject) {
		  return '<span class="cellWithoutBackground pointer" id="assignedToName_'+options.rowId+'" onclick="getActionBarGrid('+options.rowId+')" >' + (cellVal || cellVal == 0 ? cellVal :"") + '</span>';
		}
		function statusFormatter(cellVal,options,rowObject){
			return '<span id="status_'+options.rowId+'" class="cellWithoutBackground '+rowObject[13] +' " onclick="getActionBarGrid('+options.rowId+')">' + cellVal + '</span>';
		 }
		
		function updatedFormatter(cellVal,options,rowObject){
			 return '<span id="span_'+options.rowId+'" class="cellWithoutBackground '+rowObject[14] +'" onclick="getActionBarGrid('+options.rowId+')">' + cellVal + '</span>';
		}
		function dueFormatter(cellVal,options,rowObject){
			return '<span id="span_'+options.rowId+'" class=" '+rowObject[15] +'" onclick="getActionBarGrid('+options.rowId+')">' + cellVal + '</span>';
		}
		function assetFormatter(cellVal,options,rowObject){
			return options.colModel.name == "assetName" && cellVal ? '<span class="cellWithoutBackground pointer" onclick= "getEntityDetails(\'listTask\', \''+rowObject[17]+'\', '+rowObject[16]+')\" >' + (cellVal) + '</span>' :
				(cellVal || cellVal == 0 ? cellVal : "<span class='cellWithoutBackground pointer'></span>")
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
		
		function bulkEditTasks(){
			var ids = new Array();
			$(".task_started, .task_ready").each(function(){
				var taskId = $(this).attr('id').split("_")[1]
				ids.push(taskId)
			})
			if (B2 != '') { B2.Pause() }
			getBulkActionBarGrid( ids )
		}
	</script>
</head>
<body>
	<input type="hidden" id="timeBarValueId" value="0"/>
	<div id="outerBodyId" class="body">
		<div class="taskTimebar" id="issueTimebar" >
			<div id="issueTimebarId"></div>
		</div>
		<div class="body fluid">
			<h1>Task Manager</h1>
			<g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
			</g:if>
			<div id="taskMessageDiv" class="message" style="display: none;"></div>
			<input type="hidden" id="manageTaskId" value="manageTask"/>
			<g:render template="commentCrud"/> 
			<form name="commentForm" id="commentForm" method="post" action="listTasks">
			<input type="hidden" name="justRemaining" id="justRemaining" value="${justRemaining}" />
			<input type="hidden" name="justMyTasks"   id="justMyTasks"   value="${justMyTasks}"/>
			<input type="hidden" name="viewUnpublished"   id="viewUnpublished"   value="${viewUnpublished}"/>
			<input type="hidden" id="myPage" value="taskManager" />
			<span>
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

					<span id="viewGraphSpanId">
					${HtmlUtil.actionButton('View Task Graph', 'ui-icon-zoomin', 'graph', '','../task/moveEventTaskGraph?moveEventId='+filterEvent+'&mode=s')}&nbsp;
				
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