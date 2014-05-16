<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta name="layout" content="projectHeader" />
<title>User Portal</title>
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'dashboard.css')}" />
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'tabcontent.css')}" />
<link rel="shortcut icon" href="${resource(dir:'images',file:'tds.ico')}" type="image/x-icon" />
	<g:javascript src="asset.comment.js" />
	<g:javascript src="asset.tranman.js" />
	<g:javascript src="entity.crud.js" />
	<g:javascript src="angular/angular.min.js" />
	<g:javascript src="angular/plugins/angular-ui.js"/>	
	<g:javascript src="cabling.js"/>
	<g:javascript src="model.manufacturer.js"/>
</head>
<body>
<a name="page_up"></a>
<div id="doc">
	<!-- Body Starts here-->
	<div id="bodycontent"  style="font-size:18px;">
	<h1 style="float:left;position:absolute;">My Dashboard</h1>
		<!--Header Starts here-->
		<div>
			<div style="float: left;margin-top:3%;position:absolute;">
			<span> 
				<g:select id="userProjectId" name="projectId" from="${projects}" optionKey="id" optionValue="name" value="${projectInstance.id}"
				onChange="userPortalByProject(this.value)"/>
			</span>
			</div><br><br>
		</div>
		<div id="userPortalDiv">
			<g:render template="../dashboard/portal"/>
		</div>
	</div>
	<g:render template="../assetEntity/modelDialog"/>
	<div id="showEntityView" style="display: none;"></div>
	<div id="editEntityView" style="display: none;"></div>
	<div id="editManufacturerView" style="display: none;"></div>
	<div id="createEntityView" style="display: none;"></div>
	<div id="cablingDialogId" style="display: none;"></div>
	<g:render template="../assetEntity/commentCrud"/>
	<g:render template="../assetEntity/newDependency" model="['forWhom':'Server', entities:servers]"></g:render>
</div>
<script type="text/javascript">
	<g:if test="${isOnIE && isCleaner}">
    /*
     * To load the installed printers into session by initializing TFORMer
     */
	$(function() {
		window.TF.RefreshOSPrinters();
		var def = "";
		var dropdown = new Array();
		for (i = 0; i < window.TF.GetOSPrintersCount(); i++){
			dropdown.push(window.TF.GetOSPrinter(i))
		}
		${remoteFunction(controller:'moveTech', action:'setPrintersIntoSession', params:'\'dropdown=\' + dropdown')}
		
	});
	</g:if>

    $(".actionBar").die().live('click',function(){
		var id = $(this).attr('data-itemId');
		var status = $(this).attr('data-status');
		var showStatusTr = $('#showStatusId_'+id);
		if(status=='Started'){
			$('#started_'+id).hide();
			$('#image_'+id).hide();
		}
		if(!$(this).data('state')){
			showStatusTr.toggle();
			$(this).data('state',true);
		} else{
			showStatusTr.toggle();
			$(this).data('state',false);
			$('#detailTdId_'+id).hide();
		}
	});
	
	function issueDetails(id,status) {
		// hideStatus(id,status)
		jQuery.ajax({
			url: contextPath+'/clientTeams/showIssue',
			data: {'issueId':id},
			type:'POST',
			success: function(data) {
				$('#showStatusId_'+id).css('display','none')
				//$('#issueTr_'+id).attr('onClick','cancelButton('+id+',"'+status+'")');
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
				$("#labelQuantity").focus();
			}
		});
	}
	function showAssetCommentMyTasks(id){
		$('#dependencyBox').css('display','table');
		jQuery.ajax({
			url: '../assetEntity/showComment',
			data: {'id':id},
			type:'POST',
			success: function(data) {
				var ac = data[0];
				$('#predecessorShowTd').html(ac.predecessorTable)
				$('#successorShowTd').html(ac.successorTable)
				$('#assignedToTdId').html(ac.assignedTo)
				$('#estStartShowId').html(ac.etStart)
				$('#estFinishShowId').html(ac.etFinish)
				$('#actStartShowId').html(ac.atStart)
				$('#actFinishShowId').html(ac.dtResolved)
				$('#dueDateId').html(ac.dueDate)
				ac = ac.assetComment;
				$('#statusShowId').attr("class","task_"+ac.status.toLowerCase())
				$('#showCommentTable #statusShowId').attr("class","task_"+ac.status.toLowerCase())
				$('#commentTdId_myTasks').html(ac.taskNumber+":"+ac.comment)
				$('#commentTdId1').html(ac.comment)
				$('#statusShowId').html(ac.status)
				$('#showCommentTable #statusShowId').html(ac.status)
				$('#roleTdId').html(ac.role)
				$('#hardAssignedShow').html(ac.hardAssigned)
				$('#durationShowId').html(ac.duration)
				$('#durationScale').html(ac.durationScale)
				$('#priorityShowId').html(ac.priority)
				$('#assetShowValueId').html(ac.assetEntity)
			}
		});
	}
	function closeBox(){
		$('#dependencyBox').css("display","none");
	}
	function cancelButton(id,status){
		//$('#myIssueList').css('display','block')
		$('#detailTdId_'+id).css('display','none')
		$('#taskLinkId').addClass('mobselect')
		$('#showStatusId_'+id).css('display','table-row')
		//$('#issueTr_'+id).attr('onClick','issueDetails('+id+',"'+status+'")');
	}

function changeAction(){
	 document.issueAssetForm.action = 'listTasks'
}

function retainAction(){
	 document.issueAssetForm.action = 'showIssue'
}
function pageRefresh(){
	document.issueAssetForm.action = 'listTasks'
	document.issueAssetForm.submit()
}
</script>
<script>
	currentMenuId = "#teamMenuId";
	$("#teamMenuId a").css('background-color','#003366')
	$(document).ready(function() {
		var myOption = "<option value='0'>All Active</option>"
		<g:if test="${projects.size()>1}">
			$("#userProjectId option:first").before(myOption);
		</g:if>
		
		$("#showEntityView").dialog({ autoOpen: false })
		$("#createEntityView").dialog({ autoOpen: false })
		$("#editEntityView").dialog({ autoOpen: false })
		$("#manufacturerShowDialog").dialog({ autoOpen: false })
		$("#modelShowDialog").dialog({ autoOpen: false })
		$("#showCommentDialog").dialog({ autoOpen: false })
		$("#editCommentDialog").dialog({ autoOpen: false })
		$("#editManufacturerView").dialog({ autoOpen: false})
		$("#createCommentDialog").dialog({ autoOpen: false })
		$("#cablingDialogId").dialog({ autoOpen:false })
	});

	function userPortalByProject(value){
		jQuery.ajax({
	        url:contextPath+'/dashboard/userPortalDetails',
	        data:{'project':value},
	        type:'POST',
			success: function(data) {
				console.log("success");
				$("#userPortalDiv").html(data);
			}
	    });
	}
 </script>
</body>
</html>