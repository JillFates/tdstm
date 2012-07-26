<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>My Tasks</title>
	<jq:plugin name="jquery"/>
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'main.css')}" />
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'tds.css')}" />
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'qvga.css')}" />
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.theme.css')}" />
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.core.css')}" />
<link rel="shortcut icon" href="${createLinkTo(dir:'images',file:'tds.ico')}" type="image/x-icon" />
<meta name="viewport" content="height=device-height,width=220" />
	
<script type="text/javascript">    	
	window.addEventListener('load', function(){
		setTimeout(scrollTo, 0, 0, 1);
	}, false);

	function setFocus(){
		document.issueAssetForm.search.focus();
	}
	
	function updateOrientation(){
		var displayStr = "Orientation : ";
		switch(window.orientation) {
                    case 0,180:
			displayStr += "Portrait";
			var elems = document.getElementsByClassName("col2");
			for(var i = 0; i < elems.length; i++) elems[i].style.display = "none";
                    break;
                    case -90,90:
			displayStr += "Landscape";
			var elems = document.getElementsByClassName("col2");
			for(var i = 0; i < elems.length; i++) elems[i].style.display = "block";
                    break;
                }
		document.issueAssetForm.search.value = displayStr;
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

</script>      
</head>
<body onorientationchange="updateOrientation();">
	<div class="mainbody" style="width: 220px;">
	   <div id="mobtitle">TransitionManager&trade; - Mobile</div>
	<div class="menu4">
		<ul>
			<li><g:link class="mobmenu" controller="clientTeams" >Teams</g:link></li>
			<li><g:link elementId="taskLinkId"  class="mobmenu mobselect" action="listComment" params='["tab":"todo"]'>My Tasks</g:link></li>
			<li><a href="#" id="detailLinkId" class="mobmenu">Details</a></li>
		</ul>
	</div>
	<div class="issueTimebar" id="issueTimebar"><div id="timebar" ></div>
	<div id="detailId"  style="display: none;margin-top: 45px;position: absolute;width: 420px;" > </div>
	<div id="myIssueList" class="mobbodyweb" style="width: 220px;">
      	<g:form method="post" name="issueAssetForm" action="showIssue">
			<input id="issueId" name="issueId" type="hidden" value="" />
			<input name="tab" type="hidden" value="${tab}" />								              	
			<div id="mydiv" onclick="this.style.display = 'none';setFocus();">						            
				<g:if test="${flash.message}">
					<br />
					<div class="message"><ul>${flash.message}</ul></div>
				</g:if> 
			</div>		
			<div style="float:left; width:220px; margin:2px 0; ">              								
			<table style="border:0px;width:220px;">
			<tr>
				<td>&nbsp;</td>
				<td id="todoId" class="tab">
					<g:if test="${tab && tab == 'todo'}">
					  <g:link class="tab_select" action="listComment"  params='["tab":"todo","search":search]'>Todo&nbsp;(<span id="toDoNumberId">${todoSize}</span>)</g:link>
					</g:if>
					<g:else>
					  <g:link class="tab_deselect" action="listComment"  params='["tab":"todo","search":search]'>Todo&nbsp;(<span id="toDoAllId">${todoSize}</span>)</g:link>
					</g:else>
				</td>
				<td id="allId" class="tab">
					<g:if test="${tab == 'all'}">
					  <g:link class="tab_select" action="listComment" params='["tab":"all","search":search]'>All&nbsp;(${allSize})</g:link>
					</g:if>
					<g:else>
					  <g:link class="tab_deselect" action="listComment" params='["tab":"all","search":search]'>All&nbsp;(<span id="allNumberId">${allSize}</span>)</g:link>
					</g:else>
				</td>
				<td class="tab_search"><input  type="text" size="08" value="${search}" id="search" name="search" autocorrect="off" autocapitalize="off" onfocus="changeAction()" onblur="retainAction()"/></td>
			</tr>
			</table>
			</div>
			<div id="assetIssueDiv" style="float:left;width:220px;">
				<table id="issueTable" style="width:220px;" cellspacing="0px">
				<thead>
					<tr>
						<g:sortableColumn class="sort_column" style="width:60px;"  action="listComment" property="comment" title="Task" params="['tab':tab,'search':search]"></g:sortableColumn>
						<%--
						<g:sortableColumn class="sort_column" style="width:100px;" action="listComment" property="date_created" title="Created" params="['tab':tab,'search':search]"></g:sortableColumn>
						<g:sortableColumn class="sort_column" style="width:100px;" action="listComment" property="due_date" title="Due" params="['tab':tab,'search':search]"></g:sortableColumn>
						--%>
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
					<tr id="issueTr_${issue?.item?.id}" class="${issue.css}" style="cursor: pointer;" onclick="issueDetails(${issue?.item?.id})">
				  </g:else>
						<td id="comment_${issue?.item?.id}" class="asset_details_block">${issue?.item?.comment?.size() > 50 ? issue?.item?.comment?.substring(0,40)+'...' : issue?.item?.comment}</td>
						<td id="asset_${issue?.item?.id}"class="asset_details_block">${issue?.item?.assetEntity?.assetName}</td>
						<td id="statusTd_${issue?.item?.id}"id="statusTd_${issue?.item?.id}" class="asset_details_block">${issue?.item?.status}</td>
					</tr>
					<g:if test="${tab && tab == 'todo'}">
					<tr id="showStatusId_${issue?.item?.id}" style="display: none;" nowrap="nowrap"> 
						   <td nowrap="nowrap" colspan="3" class="statusButtonBar" >
							<a class="ui-button ui-widget ui-state-default ui-corner-all ui-button-text-icon-primary task_action" style="margin-left: 5px"
							 id="started_${issue?.item?.id}" onclick="changeStatus('${issue?.item?.id}','Started',${userId})">
								<span class="ui-button-icon-primary ui-icon ui-icon-play task_icon"></span>
								<span class="ui-button-text task_button">Start</span>
							</a>
							<a class="ui-button ui-widget ui-state-default ui-corner-all ui-button-text-icon-primary task_action"
							 onclick="changeStatus('${issue?.item?.id}','Completed',${userId})" style="margin-left: 5px">
								<span class="ui-button-icon-primary ui-icon ui-icon-check task_icon"></span>
								<span class="ui-button-text task_button">Complete</span>
							</a>
							<a class="ui-button ui-widget ui-state-default ui-corner-all ui-button-text-icon-primary task_action" style="margin-left: 5px" onclick="issueDetails(${issue?.item?.id})">
								<span class="ui-button-icon-primary ui-icon ui-icon-play task_icon"></span>
								<span class="ui-button-text task_button">Details..</span>
							</a>
						   </td>
						</tr>
					</g:if>
					<tr id="detailTdId_${issue?.item?.id}" style="display: none">
					<td colspan="3">
					   <div id="detailId_${issue?.item?.id}"  > </div>
					</td>
					</tr>
				</g:each>
				</tbody>
				</table>
			</div>
      		</g:form>
   		</div>
   		<br />
   		<g:link class="mobfooter" action="listComment" params="[viewMode:'web']">Use Full Site</g:link>
	</div>
	</div>
<script type="text/javascript">
	function actionSubmit(id){
	  $('#issueTimebar').width($('#issueTable').width())
	  $('#issueId').val(id)
	  document.issueAssetForm.submit();
    }

	function issueDetails(id){
		jQuery.ajax({
			url: 'showIssue',
			data: {'issueId':id},
			type:'POST',
			success: function(data) {
				B1.Pause()
				$('#showStatusId_'+id).css('display','none')
				$('#issueTr_'+id).attr('onClick','cancelButton('+id+')');
				$('#detailId_'+id).html(data)
				$('#detailTdId_'+id).css('display','table-row')
				$('#taskLinkId').removeClass('mobselect')
				$('#detailLinkId').addClass('mobselect')
			}
		});
	}
	
	function cancelButton(id){
		B1.Start(60);
		$('#detailTdId_'+id).css('display','none')
		$('#taskLinkId').addClass('mobselect')
		$('#detailLinkId').removeClass('mobselect')
		$('#showStatusId_'+id).css('display','table-row')
		$('#issueTr_'+id).attr('onClick','issueDetails('+id+')');
	}
	
	function changeStatus(id,status,user){
		jQuery.ajax({
			url: '../assetEntity/updateComment',
			data: {'id':id,'status':status,'assignedTo':user},
			type:'POST',
			success: function(data) {
				var myClass = $('#issueTrId_'+data.assetComment.id).attr("class");
				if(data.assetComment.status=='Started'){
					$('#statusTd_'+data.assetComment.id).html(data.assetComment.status)
					$('#started_'+data.assetComment.id).hide()
					$('#issueTrId_'+data.assetComment.id).removeClass(myClass).addClass('asset_process');
				}else{
					$('#showStatusId_'+data.assetComment.id).hide()
					$('#issueTrId_'+data.assetComment.id).remove()
					$('#toDoNumberId').html(parseInt($('#toDoNumberId').html())-1)
				}
			}
		});
	}

	function openStatus(id,status){
		if(status=='Started'){
			$('#started_'+id).css('display','none')
			$('#image_'+id).css('display','none')
	    }
		$('#showStatusId_'+id).show()
		$('#issueTrId_'+id).attr('onClick','hideStatus('+id+',"'+status+'")');
	}
	
	function hideStatus(id,status){
		$('#showStatusId_'+id).hide()
		$('#detailTdId_'+id).css('display','none')
		$('#issueTrId_'+id).attr('onClick','openStatus('+id+',"'+status+'")');
		B1.Start(60);
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
	
	setFocus();

	function Bar(o){
	var obj=document.getElementById(o.ID);
		this.oop=new zxcAnimate('width',obj,0);
		this.max=obj.parentNode.offsetWidth;
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
		},
		Pause:function(){
			clearTimeout(this.to);
		}
	}

	var B1=new Bar({
		ID:'timebar'
	});

	B1.Start(60);
</script>
</body>
</html>
