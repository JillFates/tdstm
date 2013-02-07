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
	<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.datetimepicker.css')}" />
    <jqui:resources /> 
    <jqgrid:resources />
	<script type="text/javascript">
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
	    	$("#selectTimedBarId").val(${timeToUpdate})
	    	taskManagerTimePref = ${timeToUpdate}
	    	if(taskManagerTimePref != 0){
	    	  B2.Start(taskManagerTimePref);
	    	}else{
	   		  B2.Pause(0);
	   	    }
	    	var event = ${filterEvent}
	    	var justRemaining = ${justRemaining}
	    	var justMyTasks = ${justMyTasks}
	    	var filter = '${filter}'
	    	<jqgrid:grid id="taskListId"  url="'${createLink(action: 'listTaskJSON')}'"
	            colNames="'Action', 'Task', 'Description', 'Asset', 'AssetType', 'Updated', 'Due', 'Status',
		            'Assigned To', 'Role', 'Category', 'Suc.', 'Score', 'id', 'statusCss'"
	            colModel="{name:'act', index: 'act' , sortable: false, formatter: myCustomFormatter, search:false, width:50},
            				{name:'taskNumber', editable: true, formatter:taskFormatter},
                            {name:'comment', editable: true, width:500, formatter:taskFormatter},
                            {name:'assetEntity', editable: true, formatter:assetFormatter},
                            {name:'assetType', editable: true, formatter:taskFormatter},
                            {name:'updated', editable: true, formatter: updatedFormatter,sortable:false,search:false},
                            {name:'dueDate', editable: true, formatter: dueFormatter},
                            {name:'status', editable: true, formatter: statusFormatter},
                            {name:'assignedTo', editable: true, formatter:assignedFormatter},
                            {name:'role', editable: true, formatter:taskFormatter},
                            {name:'category', editable: true, formatter:taskFormatter},
                            {name:'suc', editable: true, formatter:taskFormatter,sortable:false,search:false},
                            {name:'score', editable: true, formatter:taskFormatter, search:false},
                            {name:'id', hidden: true},
                            {name:'statusCss', hidden: true}"
	            caption="'Task List'"
	            height="'auto'"
	            width="1200"
	           	rowNum="25"
	            rowList= "'25','50','100'"
	            scrollOffset="0"
	            viewrecords="true"
	            postData="{moveEvent:event, justRemaining:justRemaining, justMyTasks:justMyTasks, filter:filter}"
	            showPager="true"
	            datatype="'json'">
	            <jqgrid:filterToolbar id="taskListId" searchOnEnter="false" />
	            <jqgrid:navigation id="taskListId" add="false" edit="false" 
	                  del="false" search="false" refresh="true" />
	            <jqgrid:resize id="taskListId" resizeOffset="-2" />
	     		</jqgrid:grid>
		   	    
        });
	    	
        function myCustomFormatter (cellVal,options,rowObject) {
        	var editButton = '<a href="javascript:showAssetComment(\''+options.rowId+'\',\'edit\')">'+
       			"<img src='${resource(dir:'images/skin',file:'database_edit.png')}' border='0px'/>"+"</a>&nbsp;&nbsp;"
            return editButton
        }
        function taskFormatter(cellVal,options,rowObject) {
        	  return '<span class="cellWithoutBackground pointer" id="span_'+options.rowId+'" onclick="getActionBarGrid('+options.rowId+')" >' + (cellVal ? cellVal :"") + '</span>';
        }
        function assignedFormatter(cellVal,options,rowObject) {
      	  return '<span class="cellWithoutBackground pointer" id="assignedToName_'+options.rowId+'" onclick="getActionBarGrid('+options.rowId+')" >' + (cellVal ? cellVal :"") + '</span>';
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
        	return '<span class="cellWithoutBackground pointer" onclick= "getEntityDetails(\'listComment\', \''+rowObject[4]+'\', '+rowObject[16]+')\" >' + (cellVal ? cellVal :"") + '</span>';
        }
                
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
			<g:render template="commentCrud"/> 
			<form name="commentForm" id="commentForm" action="listjqGrid">
			<input type="hidden" name="justRemaining" id="justRemaining" value="${justRemaining}" />
			<input type="hidden" name="justMyTasks"   id="justMyTasks"   value="${justMyTasks}"/>
			<span >
				<b>Move Event </b>
			 	<g:select from="${moveEvents}" name="moveEvent" id="moveEventId" optionKey="id" optionValue="name" noSelection="${['0':' All']}" value="${filterEvent}" onchange="submitForm()" />
				&nbsp;&nbsp;
				<input type="checkbox" id="justRemainingCB" ${ (justRemaining == '1' ? 'checked="checked"': '') } onclick="toggleCheckbox(this, 'justRemaining');"  />
				<b> <label for="justRemainingCB" >Just Remaining Tasks</label></b>
				&nbsp;&nbsp;
				<input type="checkbox" id="justMyTasksCB" ${ (justMyTasks=="1" ? 'checked="checked"':'') } onclick="toggleCheckbox(this, 'justMyTasks');"/>
				<b><label for="justMyTasksCB" > Just My Tasks</label></b>&nbsp;&nbsp;
				<span style="float:right;">
					<span class="menuButton"><g:link class="create" controller="task" action="moveEventTaskGraph"
						params="[moveEventId:filterEvent,mode:'s']">View Task Graph</g:link>
					</span>
				
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
				<jqgrid:wrapper id="taskListId" />
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