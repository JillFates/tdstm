<%@page import="com.tds.asset.AssetComment"%>
<%@page import="com.tdssrc.grails.TimeUtil"%>
<%@page import="com.tdssrc.grails.StringUtil"%>
<%@page import="com.tdsops.tm.enums.domain.AssetCommentStatus"%>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
	<meta name="layout" content="projectHeader" />
	<title>Task Manager</title>
	<g:javascript src="asset.tranman.js" />
	<g:javascript src="asset.comment.js" />
	<g:javascript src="entity.crud.js" />
	<script language="javascript" src="${g.resource(dir:"plugins/jmesa-0.8/js",file:"jmesa.js")}"></script>
	<link rel="stylesheet" type="text/css" href="${g.resource(dir:"plugins/jmesa-0.8/css",file:"jmesa.css")}" />
	<link type="text/css" rel="stylesheet" href="${g.resource(dir:'css',file:'ui.datepicker.css')}" />
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
        	$('#issueTimebar').width($(window).width()+'px')
            
        	$('#assetMenu').show();
        	$("#commentsListDialog").dialog({ autoOpen: false })
 	        $("#createCommentDialog").dialog({ autoOpen: false })
 	        $("#showCommentDialog").dialog({ autoOpen: false })
 	        $("#editCommentDialog").dialog({ autoOpen: false })
 	        $("#showEntityView").dialog({ autoOpen: false })
			$("#editEntityView").dialog({ autoOpen: false })
			$("#createEntityView").dialog({ autoOpen: false })
	    	currentMenuId = "#assetMenu";
	    	$("#teamMenuId a").css('background-color','#003366')
			<% // The .span_task_* are used to highlight the whole TD cell instead of just the text %>
	    	$(".span_task_tardy").parent().addClass("task_tardy")
	    	$(".span_task_late").parent().addClass("task_late")
	    	$(".span_task_ready").parent().addClass("task_ready")
	    	$(".span_task_hold").parent().addClass("task_hold")
	    	$(".span_task_started").parent().addClass("task_started")
	    	$(".span_task_pending").parent().addClass("task_pending")
	    	$(".span_task_planned").parent().addClass("task_planned")
	    	$(".span_task_completed").parent().addClass("task_completed")
	    	$(".span_task_na").parent().addClass("task_na")	
	    	$("#selectTimedBarId").val(${timeToUpdate})
	    	taskManagerTimePref = ${timeToUpdate}
	    	if(taskManagerTimePref != 0){
	    	  B2.Start(taskManagerTimePref);
	    	}else{
	   		  B2.Pause(0);
	   	    }
        });
        $(document).keyup(function(e) {
        	// esc to stop timer
       	    if (e.keyCode == 27) { if(B2 != '' && taskManagerTimePref != 0){ B2.Restart( taskManagerTimePref ); }}   
       	});
        	        
	</script>
</head>
<body>
	<input type="hidden" id="timeBarValueId" value="0"/>
	<div class="body">
		<div class="taskTimebar" id="issueTimebar" >
			<div id="issueTimebarId"></div>
		</div>
		<div class="body">
			<h1>Task Manager</h1>
			<g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
			</g:if>
			<div>
			<div>
			<input type="hidden" id="manageTaskId" value="manageTask"/>
			<form name="commentForm" id="commentForm" action="listTasks">
			<input type="hidden" name="justRemaining" id="justRemaining" value="${justRemaining}"/>
			<input type="hidden" name="justMyTasks"   id="justMyTasks"   value="${justMyTasks}"/>
			<span >
				<b>Move Event </b>
			 	<g:select from="${moveEvents}" name="moveEvent" optionKey="id" optionValue="name" noSelection="${['0':' All']}" value="${filterEvent}" onchange="submitForm()" />
				&nbsp;&nbsp;
				<input type="checkbox" id="justRemainingCB" ${ (justRemaining=="1" ? 'checked="checked"':'') } onclick="toggleCheckbox(this, 'justRemaining');"  />
				<b> Just Remaining Tasks</b>
				&nbsp;&nbsp;
				<input type="checkbox" id="justMyTasksCB" ${ (justMyTasks=="1" ? 'checked="checked"':'') } onclick="toggleCheckbox(this, 'justMyTasks');"/>
				<b> Just My Tasks</b>&nbsp;&nbsp;
				<span style="float:right;">
					<input type="button" value="Refresh" onclick="submitForm()" style="cursor: pointer;">&nbsp;
					<select id="selectTimedBarId"
					    onchange="${remoteFunction(controller:'clientConsole', action:'setTimePreference', params:'\'timer=\'+ this.value +\'&prefFor=myTask\' ', onComplete:'changeTimebarPref(e)') }">
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
				<jmesa:tableFacade id="tag" items="${assetCommentList}" maxRows="50" stateAttr="restore" var="commentInstance" autoFilterAndSort="true" maxRowsIncrements="25,50,100,250,500,1000" >
					<jmesa:htmlTable style=" border-collapse: separate" editable="true">
						<jmesa:htmlRow highlighter="true" style="cursor: pointer;" >
							<jmesa:htmlColumn property="id" sortable="false" filterable="false" cellEditor="org.jmesa.view.editor.BasicCellEditor" title="Actions" nowrap>
				        		<a href="javascript:showAssetComment(${commentInstance?.id}, 'edit')"><img src="${g.resource(dir:'images/skin',file:'database_edit.png')}" border="0px"/></a>
							</jmesa:htmlColumn>
							<jmesa:htmlColumn property="taskNumber" title="Task" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor" nowrap>
								<span id="taskNumber_${commentInstance?.id}"  onclick="getActionBar(this.id)">${commentInstance.taskNumber ? commentInstance.taskNumber :''}</span>
							</jmesa:htmlColumn>
							<jmesa:htmlColumn property="description" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor" title="Description" nowrap>
								<span id="description_${commentInstance?.id}" onclick="getActionBar(this.id)">${StringUtil.ellipsis(commentInstance.description, 45)}</span>
							</jmesa:htmlColumn>
							<jmesa:htmlColumn property="assetName" title="Asset" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">
        	                 	<span onclick="javascript:getEntityDetails('listComment', '${commentInstance.assetType}', '${commentInstance.assetEntityId}');">${commentInstance.assetName}</span>
							</jmesa:htmlColumn>
							<jmesa:htmlColumn width="50px" property="assetType" sortable="true" filterable="true" title="AssetType">
                	         	<span id="assetType_${commentInstance?.id}" onclick="getActionBar(this.id)">${commentInstance?.assetType == 'Files' ? 'Storage' : commentInstance?.assetType}</span>
							</jmesa:htmlColumn>
							<jmesa:htmlColumn property="lastUpdated" title="Updated" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor" nowrap>
								<span id="lastUpdated_${commentInstance?.id}" onclick="getActionBar(this.id)" class="span_${commentInstance.updatedClass}"><tds:elapsedAgo start="${commentInstance.lastUpdated}" end="${TimeUtil.nowGMT()}"/></span>
							</jmesa:htmlColumn>
							<jmesa:htmlColumn property="dueDate" title="Due" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">
							 	<span id="dueDate_${commentInstance?.id}" onclick="getActionBar(this.id)" class="${commentInstance.dueClass}">
								<tds:convertDate date="${commentInstance.dueDate}" format="${commentInstance.isRunbookTask() ? 'MM/dd kk:mm:ss' : 'MM/dd'}"/></span>
							</jmesa:htmlColumn>
							
							<jmesa:htmlColumn property="status" title="Status" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">
							 	<span id="status_${commentInstance?.id}" onclick="getActionBar(this.id);" class="span_${commentInstance.statusClass}">${commentInstance.status}</span>
							</jmesa:htmlColumn>
							<jmesa:htmlColumn property="assignedTo" title="Assigned To" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">
        	                 	<span id="assignedTo_${commentInstance?.id}" onclick="getActionBar(this.id);">${(commentInstance?.hardAssigned?'* ':'')}<span id="assignedToName_${commentInstance?.id}">${commentInstance.assignedTo?:''}</span></span>
							</jmesa:htmlColumn>
							<jmesa:htmlColumn property="role" title="Role" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">
        	                 	<span id="role_${commentInstance?.id}" onclick="getActionBar(this.id);">${commentInstance.role}</span>
							</jmesa:htmlColumn>
							 <%--
    	                     <jmesa:htmlColumn property="mustVerify" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">
    	                     	<span onclick="javascript:showAssetComment(${commentInstance?.id}, 'show')"><g:if test ="${commentInstance.mustVerify == 1}"></g:if><g:else><g:checkBox name="myVerifyBox" value="${true}" disabled="true"/></g:else></span>
    	                     </jmesa:htmlColumn>
        	                 --%>
							<jmesa:htmlColumn width="50px" property="category" sortable="true" filterable="true" title="Category">
                             	<span id="category_${commentInstance?.id}" onclick="getActionBar(this.id);">${commentInstance.category}</span>
							</jmesa:htmlColumn>
							<jmesa:htmlColumn property="succCount" title="Suc." sortable="true" filterable="true"  cellEditor="org.jmesa.view.editor.BasicCellEditor">
							 	<span id="succCount_${commentInstance?.id}" onclick="getActionBar(this.id);">${ commentInstance.succCount}</span>
							</jmesa:htmlColumn>
							<jmesa:htmlColumn property="score" title="Score" sortable="true" filterable="false">
							 	<span id="score__${commentInstance?.id}" onclick="getActionBar(this.id);">${commentInstance.score}</span>
							</jmesa:htmlColumn>
						</jmesa:htmlRow>
					</jmesa:htmlTable>
				</jmesa:tableFacade>
			</form>
            <div class="nav" style="border: 1px solid #CCCCCC; height: 11px">
		      <span class="menuButton"><a class="create" href="javascript:createIssue('','','')">Create Issue/Task</a></span>
	       	</div>
		</div>
		<div id="showEntityView" style="display: none;"></div>
		<div id="editEntityView" style="display: none;"></div>
		<div id="createEntityView" style="display: none;"></div>
		<div style="display: none;">
		<table id="assetDependencyRow">
			<tr>
				<td><g:select name="dataFlowFreq" from="${assetDependency.constraints.dataFlowFreq.inList}"></g:select></td>
				<td><g:select name="entity" from="['Server','Application','Database','Storage']" onchange='updateAssetsList(this.name, this.value)'></g:select></td>
				<td><g:select name="asset" from="${servers}" optionKey="id" optionValue="assetName" style="width:90px;"></g:select></td>
				<td><g:select name="dtype" from="${dependencyType.value}"  optionValue="value"></g:select></td>
				<td><g:select name="status" from="${dependencyStatus.value}" optionValue="value"></g:select></td>
			</tr>
			</table>
		</div>
		<div style="display: none;">
			<span id="Server"><g:select name="asset" from="${servers}" optionKey="id" optionValue="assetName" style="width:90px;"></g:select></span>
			<span id="Application"><g:select name="asset" from="${applications}" optionKey="id" optionValue="assetName" style="width:90px;"></g:select></span>
			<span id="Database"><g:select name="asset" from="${dbs}" optionKey="id" optionValue="assetName" style="width:90px;"></g:select></span>
			<span id="Storage"><g:select name="asset" from="${files}" optionKey="id" optionValue="assetName" style="width:90px;"></g:select></span>
		</div>
  </div>
  
 <g:render template="commentCrud"/> 
 </div>
 </div>
 <script type="text/javascript">
function toggleCheckbox(chkbox, field) {
	$('input[name='+field+']').val(chkbox.checked ? '1' : '0')
	submitForm()
}
 function submitForm(){
     $('#commentForm').submit()
 }
 function pageRefresh(){
   window.location.reload()
 }

 function Bar(o){
	var obj=document.getElementById(o.ID);
		this.oop=new zxcAnimate('width',obj,0);
		this.max=$('#issueTimebar').width();
		this.to=null;
	}
	Bar.prototype={
		Start:function(sec){
			clearTimeout(this.to);
			this.oop.animate(0,this.max,sec*1000);
			this.srt=new Date();
			this.sec=sec;
			this.Time();
		},
		Time:function(sec){
			var oop=this,sec=this.sec-Math.floor((new Date()-this.srt)/1000);
			//this.oop.obj.innerHTML=sec+' sec';
			$('#timeBarValueId').val(sec)
			if (sec>0){
				this.to=setTimeout(function(){ oop.Time(); },1000);
			}else{
				pageRefresh();
			}
		},
		Pause:function(sec){
			clearTimeout(this.to);
			if(sec==0){
				this.oop.animate(sec,'',sec*1000);
			}else{
				this.oop.animate($('#issueTimebarId').width(),$('#issueTimebarId').width(),sec*1000);
			}
		},
		Restart:function(sec){
			clearTimeout(this.to);
			var second = $('#timeBarValueId').val()
			this.oop.animate($('#issueTimebarId').width(),this.max,second*1000);
			this.srt=new Date();
			this.sec=second;
			this.Time();
		}
	}

	B2=new Bar({
		ID:'issueTimebarId'
	});
</script>
</body>
 
</html>