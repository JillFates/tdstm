<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="projectHeader" />
	<title>My Issues</title>
	<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'qvga.css')}" />
	<link rel="shortcut icon" href="${createLinkTo(dir:'images',file:'tds.ico')}" type="image/x-icon" />
</head>
<body>
	<div class="mainbody">
	<div class="menu4">
		<ul>
			<li><g:link class="mobmenu" controller="clientTeams" >Teams</g:link></li>
			<li><g:link class="mobmenu mobselect" action="listComment" params='["tab":"todo"]'>My Tasks</g:link></li>
			<li><a href="#" class="mobmenu">Details</a></li>
		</ul>
	</div>
	<div class="issueTimebar" id="issueTimebar"><div id="issueTimebarId" ></div>
	<div class="mobbodyweb">
      	<g:form method="post" name="issueAssetForm" action="showIssue">
			<input id="issueId" name="issueId" type="hidden" value="" />
			<input name="tab" type="hidden" value="${tab}" />								              	
			<div id="mydiv" onclick="this.style.display = 'none';setFocus();">						            
				<g:if test="${flash.message}">
					<br />
					<div class="message"><ul>${flash.message}</ul></div>
				</g:if> 
			</div>		
			<div id="taskId" style="float:left; margin:2px 0; ">              								
			<table style="border:0px;width:220px;">
			<tr>
				<td>&nbsp;</td>
				<td id="todoId" class="tab">
					<g:if test="${tab && tab == 'todo'}">
					  <g:link class="tab_select" action="listComment"  params='["tab":"todo","search":search]'>Todo&nbsp;(${todoSize})</g:link>
					</g:if>
					<g:else>
					  <g:link class="tab_deselect" action="listComment"  params='["tab":"todo","search":search]'>Todo&nbsp;(${todoSize})</g:link>
					</g:else>
				</td>
				<td id="allId" class="tab">
					<g:if test="${tab == 'all'}">
					  <g:link class="tab_select" action="listComment" params='["tab":"all","search":search]'>All&nbsp;(${allSize})</g:link>
					</g:if>
					<g:else>
					  <g:link class="tab_deselect" action="listComment" params='["tab":"all","search":search]'>All&nbsp;(${allSize})</g:link>
					</g:else>
				</td>
				<td class="tab_search"><input  type="text" size="08" value="${search}" id="search" name="search" autocorrect="off" autocapitalize="off" onfocus="changeAction()" onblur="retainAction()"/></td>
			</tr>
			</table>
			</div>
			<div id="assetIssueDiv" style="float:left;width:500px; ">
				<table id="issueTable" cellspacing="0px">
				<thead>
					<tr>
						<g:sortableColumn class="sort_column" style="width:60px;"  action="listComment" property="comment" title="Task" params="['tab':tab,'search':search]"></g:sortableColumn>
						<g:sortableColumn class="sort_column" style="width:100px;" action="listComment" property="dateCreated" title="Created" params="['tab':tab,'search':search]"></g:sortableColumn>
						<g:sortableColumn class="sort_column" style="width:100px;" action="listComment" property="dueDate" title="Due" params="['tab':tab,'search':search]"></g:sortableColumn>
						<g:sortableColumn class="sort_column" style="width:60px;" action="listComment" property="assetEntity" title="Related to" params="['tab':tab,'search':search]"></g:sortableColumn>
						<g:sortableColumn class="sort_column" style="width:60px;" action="listComment" property="status" title="Status" params="['tab':tab,'search':search]"></g:sortableColumn>
					</tr>
				</thead>
				<tbody>
				<g:each status="i" in="${listComment}" var="issue" >
				  <g:if test="${tab && tab == 'todo'}">
					  <tr id="issueTrId_${issue?.item?.id}" class="${issue.css}" style="cursor: pointer;" onclick="openStatus(${issue?.item?.id},'${issue?.item?.status}')">
				  </g:if>
				  <g:else>
				      <tr id="issueTrId_${issue?.item?.id}" class="${issue.css}" style="cursor: pointer;" onclick="actionSubmit(${issue?.item?.id})">
				  </g:else>
						<td class="asset_details_block_task">${issue?.item?.comment?.size() > 50 ? issue?.item?.comment?.substring(0,40)+'...' : issue?.item?.comment}</td>
						<td class="asset_details_block"><tds:convertDate date="${issue?.item?.dateCreated}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/></td>
						<td class="asset_details_block"><tds:convertDate date="${issue?.item?.dueDate}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/></td>
						<td class="asset_details_block">${issue?.item?.assetEntity?.assetName}</td>
						<td class="asset_details_block">${issue?.item?.status}</td>
					</tr>
					<g:if test="${tab && tab == 'todo'}">
					<tr id="showStatusId_${issue?.item?.id}" style="display: none; height: 30px;" class="statusClass" > 
					   <td nowrap="nowrap" colspan="5">
							
				             <span class="statusButton" id="started_${issue?.item?.id}" onclick="changeStatus('${issue?.item?.id}','Started',${userId})">&nbsp;&nbsp;Start&nbsp;&nbsp;</span>
				             <span id="image_${issue?.item?.id}"><img src="${createLinkTo(dir:'images',file:'player_play.jpg')}" /><img src="${createLinkTo(dir:'images',file:'player_pause.png')}" /></span>  
				              <span class="statusButton" onclick="changeStatus('${issue?.item?.id}','Completed',${userId})" style="margin-left: 30px">&nbsp;&nbsp;Complete&nbsp;&nbsp;</span>
				              <img src="${createLinkTo(dir:'images',file:'check.png')}" />
				              <span class="detailButton" onclick="actionSubmit(${issue?.item?.id})" style="margin: 30px">&nbsp;&nbsp;Details..&nbsp;&nbsp;</span>
					   </td>
					</tr>
					</g:if>
				</g:each>
				</tbody>
				</table>
			</div>
      		</g:form>
   		</div>
   		<br />
      <div>
      	<g:link class="mobfooter" action="listComment" params="[viewMode:'mobile']">Use Mobile Site</g:link>
      </div>
	</div>
	</div>
 <script type="text/javascript">
$( function() {
	$('#issueTimebar').width($('#issueTable').width())
});
function setFocus(){
	document.issueAssetForm.search.focus();
}
function changeStatus(id,status,user){
	jQuery.ajax({
		url: '../assetEntity/updateComment',
		data: {'id':id,'status':status,'redirectTo':'taskList','assignedTo':user},
		type:'POST',
		success: function(data) {
			pageRefresh();
		}
	});
      	
}
function openStatus(id,status){
	$('.statusClass').css('display','none')
	if(status=='Started'){
		$('#started_'+id).css('display','none')
		$('#image_'+id).css('display','none')
    }
	$('#showStatusId_'+id).show()
	$('#issueTrId_'+id).attr('onClick','hideStatus('+id+',"'+status+'")');
}
function hideStatus(id,status){
	$('#showStatusId_'+id).hide()
	$('#issueTrId_'+id).attr('onClick','openStatus('+id+',"'+status+'")');
}
 
function actionSubmit(id){
   $('#issueId').val(id)
   document.issueAssetForm.submit();
}

function changeAction(){
	 document.issueAssetForm.action = 'listComment'
}

function retainAction(){
	 document.issueAssetForm.action = 'showIssue'
}

function pageRefresh(){
	document.issueAssetForm.action = 'listComment'
	document.issueAssetForm.submit()
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
	setFocus();

	function Bar(o){
	var obj=document.getElementById(o.ID);
		this.oop=new zxcAnimate('width',obj,0);
		this.max=$('#issueTable').width();
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
		//	this.oop.obj.innerHTML=sec+' sec';
			if (sec>0){
				this.to=setTimeout(function(){ oop.Time(); },1000);
			}else{
				pageRefresh();
			}
		}
	}

	var B1=new Bar({
		ID:'issueTimebarId'
	});

	B1.Start(60);
</script>
 <script>
	currentMenuId = "#teamMenuId";
	$("#teamMenuId a").css('background-color','#003366')
 </script>
</body>
</html>