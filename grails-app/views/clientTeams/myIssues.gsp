<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="projectHeader" />
	<title>My Tasks</title>
	<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'qvga.css')}" />
	<link rel="shortcut icon" href="${createLinkTo(dir:'images',file:'tds.ico')}" type="image/x-icon" />
</head>
<body>
		<div class="mainbody">
		     <div id="myTaskList">
		           <g:render template="tasks"/>
		     </div>
		</div>
	    <br />
		<div>
			<g:link class="mobfooter" action="listComment" params="[viewMode:'mobile']">Use Mobile Site</g:link>
		</div>
<%--
/*
 **************************
 * Dependency Dialog
 **************************
 */
--%>
	<div id="dependencyBox" style="display: none;" align="right">
		<span class="ui-icon ui-icon-closethick" unselectable="on" onclick="closeBox()"></span>
		<table id="showCommentTable" style="border: 0px;">
			<tr class="prop">
				<td valign="top" class="name"><label for="comment"><b>Description:</b></label></td>
				<td valign="top" class="value" colspan="3">
					<textarea cols="80" rows="2" id="commentTdId" readonly="readonly"></textarea>
				</td>
			</tr>
			<tr class="prop" id="predecessorShowTr">
				<td valign="top"><label for="precessorShowId">Predecessor:</label></td>
				<td valign="top" id="predecessorShowTd" colspan="2" width="34%"></td>
				<td valign="top"  style="float: left;"><label for="precessorShowId" >Successor:</label></td>
				<td valign="top" id="successorShowTd" colspan="2" width="76%" style="margin-left: 0px;float: left;"></td>
			</tr>
			<tr class="prop">
				<td valign="top" class="name"><label for="status">Status:</label></td>
				<td valign="top" class="value" id="statusShowId" colspan="1" style="width: 20%"></td>
			</tr>
			<tr class="issue" id="assignedToTrId">
				<td valign="top" class="name"><label for="assignedTo">Assigned To:</label></td>
				<td valign="top" class="value" id="" colspan="3">
					<span id="assignedToTdId"></span>&nbsp;-&nbsp;
					<span id="roleTdId"></span>&nbsp;
					<input type="checkbox" id="hardAssignedShow" name="hardAssignedShow" value="0" readonly="readonly"/>
					<label for="hardAssignedShow" >Fixed Assignment</label>&nbsp;&nbsp;
					<span id="dueDateTrId">
						<label for="dueDate">Due :</label>
						<span id="dueDateId"></span>
					</span>
				</td>
			</tr>
			<tr class="issue" id="workFlowShow">
				<td valign="top" class="name"><label for="durationShowId">Duration:</label></td>
				<td valign="top" class="value" colspan="3">
					<span id="durationShowId"></span>
					<span id="durationScale"></span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
					<span ><label for="priorityShowId">Priority:</label></span>
					<span id="priorityShowId"></span>
				</td>
			</tr>
			<tr id="assetShowId" class="prop">
				<td valign="top" class="name" id="assetTdId"><label for="asset">Asset:</label></td>
				<td valign="top" class="value" id="assetShowValueId" colspan="3"></td>
			</tr>
			<tr class = "issue" id="estStartShow">
				<td valign="top" class="name" nowrap="nowrap"><label for="estStartShowId">Estimated Start:</label></td>
				<td valign="top" class="value" id="estStartShowId" nowrap="nowrap"></td>
				<td valign="top" class="name" nowrap="nowrap"><label for="estFinishShowId">Estimated Finish:</label></td>
				<td valign="top" class="value" id="estFinishShowId" nowrap="nowrap"></td>
			</tr>
			<tr class = "issue" id="actStartShow">
				<td valign="top" class="name"><label for="actStartShowId">Actual Start:</label></td>
				<td valign="top" class="value" id="actStartShowId"></td>
				<td valign="top" class="name" nowrap="nowrap" width="10%"><label for="actFinishShowId">Actual Finish:</label></td>
				<td valign="top" class="value" id="actFinishShowId" nowrap="nowrap"></td>
			</tr>
		</table>
	</div>
<script type="text/javascript">
	$( function() {
		$('#issueTimebar').width($('#issueTable').width())
	});

	function setFocus(){
		document.issueAssetForm.search.focus();
	}
	function issueDetails(id,status){
		jQuery.ajax({
			url: 'showIssue',
			data: {'issueId':id},
			type:'POST',
			success: function(data) {
				B1.Pause()
				$('#showStatusId_'+id).css('display','none')
				$('#issueTr_'+id).attr('onClick','cancelButton('+id+',"'+status+'")');
				$('#detailId_'+id).html(data)
				$('#detailTdId_'+id).css('display','table-row')
				//$('#detailId_'+id).css('display','block')
				$('#taskLinkId').removeClass('mobselect')
				new Ajax.Request('../assetEntity/updateStatusSelect?id='+id,{asynchronous:false,evalScripts:true,
					onComplete:function(e){
						var resp = e.responseText;
							resp = resp.replace("statusEditId","statusEditId_"+id).replace("showResolve(this.value)","showResolve()")
							$('#statusEditTrId_'+id).html(resp)
							// $('#statusEditId_'+id).val(status)
			 			}
				})
			}
		});
	}
	function showAssetComment(id){
		$('#dependencyBox').css('display','table');
		jQuery.ajax({
			url: '../assetEntity/showComment',
			data: {'id':id},
			type:'POST',
			success: function(data) {
				B1.Pause()
				var ac = data[0];
				$('#predecessorShowTd').html(ac.predecessorTable)
				$('#successorShowTd').html(ac.successorTable)
				$('#assignedToTdId').html(ac.assignedTo)
				ac = ac.assetComment;
				$('#commentTdId').html(ac.comment)
				$('#statusShowId').html(ac.status)
				$('#roleTdId').html(ac.role)
				$('#hardAssignedShow').html(ac.hardAssigned)
				$('#dueDateId').html(ac.dueDate)
				$('#durationShowId').html(ac.duration)
				$('#durationScale').html(ac.durationScale)
				$('#priorityShowId').html(ac.priority)
				$('#assetShowValueId').html(ac.assetEntity)
				$('#estStartShowId').html(ac.estStart)
				$('#estFinishShowId').html(ac.estFinish)
				$('#actStartShowId').html(ac.actStart)
				$('#actFinishShowId').html(ac.dateResolved)
			}
		});
	}
	function closeBox(){
		$('#dependencyBox').css("display","none");
	}
	function cancelButton(id,status){
		B1.Start(60);
		//$('#myIssueList').css('display','block')
		$('#detailTdId_'+id).css('display','none')
		$('#taskLinkId').addClass('mobselect')
		$('#showStatusId_'+id).css('display','table-row')
		$('#issueTr_'+id).attr('onClick','issueDetails('+id+',"'+status+'")');
	}
	function changeStatus(id,status, currentStatus){
		var tab = $('#tabId').val()
		jQuery.ajax({
			url: '../assetEntity/updateComment',
			data: {'id':id,'status':status,'currentStatus':currentStatus,'view':'myTask','tab':tab},
			type:'POST',
			success: function(data) {
				if (typeof data.error !== 'undefined') {
					alert(data.error);
				} else {
					 $('#myTaskList').html(data)
					 $('#showStatusId_'+id).show()
					 $('#issueTrId_'+id).attr('onClick','hideStatus('+id+',"'+status+'")')
					 if(status=='Started'){
					 	$('#started_'+id).hide()
					 }
					 B1.Start(60);
				}
			},
			error: function(jqXHR, textStatus, errorThrown) {
				alert("An unexpected error occurred while attempting to update task/comment")
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
		},
		Pause:function(){
			clearTimeout(this.to);
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