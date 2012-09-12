<%@page import="com.tds.asset.AssetComment"%>
<%@page import="com.tdssrc.grails.GormUtil"%>
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
        });
        $(document).keyup(function(e) {
        	// esc to stop timer
       	    if (e.keyCode == 27) { if(B2 != ''){ B2.Restart(60); }}   
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
				<b> Just My Tasks</b>
			</span>
			<br/></br>
				<jmesa:tableFacade id="tag" items="${assetCommentList}" maxRows="50" stateAttr="restore" var="commentInstance" autoFilterAndSort="true" maxRowsIncrements="25,50,100" >
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
                	         	<span id="assetType_${commentInstance?.id}" onclick="getActionBar(this.id)">${commentInstance?.assetType}</span>
							</jmesa:htmlColumn>
							<jmesa:htmlColumn property="lastUpdated" title="Updated" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor" nowrap>
								<span id="lastUpdated_${commentInstance?.id}" onclick="getActionBar(this.id)" class="span_${commentInstance.updatedClass}"><tds:elapsedAgo start="${commentInstance.lastUpdated}" end="${GormUtil.convertInToGMT(new Date(), null)}"/></span>
							</jmesa:htmlColumn>
							<jmesa:htmlColumn property="dueDate" title="Due" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">
							 	<span id="dueDate_${commentInstance?.id}" onclick="getActionBar(this.id)" class="${commentInstance.dueClass}">
								<tds:convertDate date="${commentInstance.dueDate}" format="${commentInstance.isRunbookTask() ? 'MM/dd kk:mm:ss' : 'MM/dd'}"/></span>
							</jmesa:htmlColumn>
							
							<jmesa:htmlColumn property="status" title="Status" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">
							 	<span id="status_${commentInstance?.id}" onclick="getActionBar(this.id);" class="span_${commentInstance.statusClass}">${commentInstance.status}</span>
							</jmesa:htmlColumn>
							<jmesa:htmlColumn property="assignedTo" title="Assigned To" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">
        	                 	<span id="assignedTo_${commentInstance?.id}" onclick="getActionBar(this.id);">${(commentInstance?.hardAssigned?'* ':'')}<span id="assignedToName_${commentInstance?.id}">${commentInstance.assignedTo}</span></span>
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
 function zxcAnimate(mde,obj,srt){
	this.to=null;
	this.obj=typeof(obj)=='object'?obj:document.getElementById(obj);
	this.mde=mde.replace(/\W/g,'');
	this.data=[srt||0];
	return this;
 }
 
 zxcAnimate.prototype.animate=function(srt,fin,ms,scale,c){
	clearTimeout(this.to);
	this.time=ms||this.time||0;
	this.neg=srt<0||fin<0;
	this.data=[srt,srt,fin];
	this.mS=this.time*(!scale?1:Math.abs((fin-srt)/(scale[1]-scale[0])));
	this.c=typeof(c)=='string'?c.charAt(0).toLowerCase():this.c?this.c:'';
	this.inc=Math.PI/(2*this.mS);
	this.srttime=new Date().getTime();
	this.cng();
 }

 zxcAnimate.prototype.cng=function(){
	var oop=this,ms=new Date().getTime()-this.srttime;
	this.data[0]=(this.c=='s')?(this.data[2]-this.data[1])*Math.sin(this.inc*ms)+this.data[1]:(this.c=='c')?this.data[2]-(this.data[2]-this.data[1])*Math.cos(this.inc*ms):(this.data[2]-this.data[1])/this.mS*ms+this.data[1];
	this.apply();
	if (ms<this.mS) this.to=setTimeout(function(){oop.cng()},10);
	else {
		this.data[0]=this.data[2];
		this.apply();
	 if (this.Complete) this.Complete(this);
	}
 }

 zxcAnimate.prototype.apply=function(){
	if (isFinite(this.data[0])){
		if (this.data[0]<0&&!this.neg) this.data[0]=0;
		if (this.mde!='opacity') this.obj.style[this.mde]=Math.floor(this.data[0])+'px';
		else zxcOpacity(this.obj,this.data[0]);
	}
 }

 function zxcOpacity(obj,opc){
	if (opc<0||opc>100) return;
	obj.style.filter='alpha(opacity='+opc+')';
 	obj.style.opacity=obj.style.MozOpacity=obj.style.WebkitOpacity=obj.style.KhtmlOpacity=opc/100-.001;
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
			this.oop.animate(sec,'',sec*1000);
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

  B2.Start(60);
</script>
</body>
 
</html>