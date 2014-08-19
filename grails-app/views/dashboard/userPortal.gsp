<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta name="layout" content="projectHeader" />
<title>User Dashboard For ${loggedInPerson}</title>
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'dashboard.css')}" />
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'tabcontent.css')}" />
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'userPortal.css')}" />
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.datepicker.css')}" />
<link rel="shortcut icon" href="${resource(dir:'images',file:'favicon.ico')}" type="image/x-icon" />
	<g:javascript src="bootstrap.js" />
	<g:javascript src="asset.comment.js" />
	<g:javascript src="asset.tranman.js" />
	<g:javascript src="entity.crud.js" />
	<g:javascript src="angular/angular.min.js" />
	<g:javascript src="angular/plugins/angular-ui.js"/>	
	<g:javascript src="angular/plugins/angular-resource.js" />
	<g:javascript src="cabling.js"/>
	<g:javascript src="model.manufacturer.js"/>
	<g:javascript src="angular/plugins/ui-bootstrap-tpls-0.10.0.min.js" />
	<g:javascript src="angular/plugins/ngGrid/ng-grid-2.0.7.min.js" />
	<script type="text/javascript" src="${resource(dir:'components/core',file:'core.js')}"></script>
	<script type="text/javascript" src="${resource(dir:'components/comment',file:'comment.js')}"></script>
	<script type="text/javascript" src="${resource(dir:'components/asset',file:'asset.js')}" /></script>
	<link type="text/css" rel="stylesheet" href="${resource(dir:'components/comment',file:'comment.css')}" />
</head>
<body>
<a name="page_up"></a>
<div id="doc" ng-app="tdsComments" ng-controller="tds.comments.controller.MainController as comments">
	<!-- Body Starts here-->
	<div id="bodycontent" >
	<h1 style="float:left;position:absolute;">User Dashboard for ${loggedInPerson}</h1>
		<div>
			<div style="float: left;margin-top:3%;position:absolute;">
			<span> 
				Project:&nbsp;<g:select id="userProjectId" name="projectId" from="${projects}" optionKey="id" optionValue="name" value="${projectInstance.id}"
				onChange="loadAll(this.value)"/>
			</span>
			</div><br><br>
		</div>
		<div id="userPortalDiv"  style="font-size:18px;">
			<g:render template="../dashboard/portal"/>
		</div>
	</div>
	<g:render template="../assetEntity/initAssetEntityData"/>
	<div id="personGeneralViewId" style="display: none;" title="User Details"></div>
</div>
<script type="text/javascript">

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
			url: contextPath+'/assetEntity/showComment',
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
			},
			error: function(jqXHR, textStatus, errorThrown) {
				alert("An unexpected error occurred when showing comments.")
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
var image = "<tr><td><div><img src='"+contextPath+"/images/processing.gif'></div></td></tr>"
function loadRelatedEntities(id){
	jQuery.ajax({
		url: contextPath+'/dashboard/getRelatedEntities',
		data: {'project':id ? id :$("#userProjectId").val()},
		type:'POST',
		success: function(data) {
			$("#relatedEntitiesId").html(data);
		},
		error: function(jqXHR, textStatus, errorThrown) {
			alert("An unexpected error occurred when updating entities.")
		}
	});
}

function loadEventTable(id){

	jQuery.ajax({
		url: contextPath+'/dashboard/getEvents',
		data: {'project':id ? id :$("#userProjectId").val()},
		type:'POST',
		beforeSend: function(xhr) {
			$("#eventTableId").html(image);
		},
		success: function(data) {
			$("#eventTableId").html(data);
		},
		error: function(jqXHR, textStatus, errorThrown) {
			$("#eventTableId").html("An unexpected error occurred when updating Event Table.");
		}
	});
}

function loadEventNewsTable(id){

	jQuery.ajax({
		url: contextPath+'/dashboard/getEventsNewses',
		data: {'project':id ? id :$("#userProjectId").val()},
		type:'POST',
		beforeSend: function(xhr) {
			$("#eventNewsTableId").html(image);
		},
		success: function(data) {
			$("#eventNewsTableId").html(data);
		},
		error: function(jqXHR, textStatus, errorThrown) {
			$("#eventNewsTableId").html("An unexpected error occurred when updating Event News Table.")
		}
	});
}

function loadTasksTable(id){

	jQuery.ajax({
		url: contextPath+'/dashboard/getTaskSummary',
		data: {'project':id ? id :$("#userProjectId").val()},
		type:'POST',
		beforeSend: function(xhr) {
			$("#myTaskList").html(image);
		},
		success: function(data) {
			$("#myTaskList").html(data);
			recompileDOM("myTaskList");
		},
		error: function(jqXHR, textStatus, errorThrown) {
			$("#myTaskList").html("<br>An unexpected error occurred when updating Tasks Table")
		}
	});
}

function loadAppTable(id){
	jQuery.ajax({
		url: contextPath+'/dashboard/getApplications',
		data: {'project':id ? id :$("#userProjectId").val()},
		type:'POST',
		beforeSend: function(xhr) {
			$("#appTableId").html(image);
		},
		success: function(data) {
			$("#appTableId").html(data);
		},
		error: function(jqXHR, textStatus, errorThrown) {
			$("#appTableId").html("An unexpected error occurred when updating Application Table.")
		}
	});
}

function loadActivepplTable(id){
	jQuery.ajax({
		url: contextPath+'/dashboard/getActivePeople',
		data: {'project':id ? id :$("#userProjectId").val()},
		type:'POST',
		beforeSend: function(xhr) {
			$("#actpplTableId").html(image);
		},
		success: function(data) {
			$("#actpplTableId").html(data);
		},
		error: function(jqXHR, textStatus, errorThrown) {
			$("#actpplTableId").html("An unexpected error occurred when updating Active ppl Table.")
		}
	});
}

function getUserDetails(personId, renderPage){
		jQuery.ajax({
			url : contextPath+'/person/loadGeneral',
			data : {
				'personId' : personId,'tab':renderPage
			},
			type : 'POST',
			success : function(data) {
				$("#personGeneralViewId").html(data)
				$("#personGeneralViewId").dialog('option', 'width', '420px')
				$("#personGeneralViewId").dialog('option', 'position', ['center','top']);
				$("#edtBId").parent().remove()
				$("#personGeneralViewId").dialog('open');
				
			}
		});
		
}

function switchTab(id,divId,header){
	$(".person").hide()
	currentTabShow = divId
	currentHeaderShow = header
	$(".mobmenu").removeClass("mobselect")
	$("#"+currentHeaderShow).addClass("mobselect")
	$("#"+currentTabShow).show()
}
</script>
<script>
	currentMenuId = "#teamMenuId";
	$("#teamMenuId a").css('background-color','#003366')
	$(document).ready(function() {
		$("#personGeneralViewId").dialog({ autoOpen: false })
		var myOption = "<option value='0'>All Active</option>"
		<g:if test="${projects.size()>1}">
			$("#userProjectId option:first").before(myOption);
		</g:if>

		var id = $("#userProjectId").val();
		loadAll(id);
		
	});

	function loadAll(id){
		if(id==0){
			if($("#eventTable th:first-child").html()!="Project"){
				$("#eventThId").prepend("<th>Project</th>")
				$("#eventNewsThId").prepend("<th>Project</th>")
				$("#taskThId").prepend("<th>Project</th>")
				$("#appThId").prepend("<th>Project</th>")
			}
		} else {
			if($("#eventTable th:first-child").html()=="Project"){
				$("#eventTable th:first-child").remove()
				$("#eventNewsTable th:first-child").remove()
				$("#issueTable th:first-child").remove()
				$("#appTable th:first-child").remove()
			}
		}
		loadRelatedEntities(id)
		loadEventTable(id);
		loadEventNewsTable(id);
		loadTasksTable(id);
		loadAppTable(id);
		loadActivepplTable(id);
	}

	
	function userPortalByProject(value){
		jQuery.ajax({
	        url:contextPath+'/dashboard/userPortalDetails',
	        data:{'project':value},
	        type:'POST',
			success: function(data) {
				console.log("success");
				$("#userPortalDiv").html(data);
			},
			error: function(jqXHR, textStatus, errorThrown) {
				alert("An unexpected error occurred when getting userPortal by project.")
			}
	    });
	}
 </script>
</body>
</html>