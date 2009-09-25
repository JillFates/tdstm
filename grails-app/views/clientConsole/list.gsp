<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="projectHeader" />
<title>PMO Dashboard</title>
<g:javascript library="prototype" />
<g:javascript library="jquery" />
<jq:plugin name="jquery.bgiframe.min" />
<jq:plugin name="jquery.autocomplete" />
<jq:plugin name="jquery.contextMenu"/>
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'jquery.autocomplete.css')}" />
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.accordion.css')}" />
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.core.css')}" />
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.dialog.css')}" />
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.resizable.css')}" />
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.slider.css')}" />
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.tabs.css')}" />
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.theme.css')}" />
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'dashboard.css')}" />
<g:javascript src="assetcrud.js" />
<g:javascript src="assetcommnet.js" />
<jq:plugin name="ui.core" />
<jq:plugin name="ui.draggable" />
<jq:plugin name="ui.resizable" />
<jq:plugin name="ui.dialog" />
<style type="text/css">
html, body {
overflow: hidden;
} 
</style>
<g:javascript>

function initialize(){
var bundleId = ${moveBundleInstance.id}; 
$("#moveBundleId").val(bundleId);
$("#appSmeId").val("${appSmeValue}");
$("#appOwnerId").val("${appOwnerValue}");
$("#applicationId").val("${appValue}");
var time = '${timeToRefresh}';
	if(time != "" ){
		$("#selectTimedId").val( time ) ;
	} else if(time == "" ){
		$("selectTimedId").val( 120000 );	
	}
}
</g:javascript>
<script>
var timeInterval
var fieldId
	$(document).ready(function() {
		$("#changeStatusDialog").dialog({ autoOpen: false })
	    $("#showDialog").dialog({ autoOpen: false })
	    $("#editDialog").dialog({ autoOpen: false })
		$("#commentsListDialog").dialog({ autoOpen: false })
	    $("#createCommentDialog").dialog({ autoOpen: false })
	    $("#showCommentDialog").dialog({ autoOpen: false })
	    $("#editCommentDialog").dialog({ autoOpen: false })
	    $("#showChangeStatusDialog").dialog({ autoOpen: false })	        
	
		var cells = "${htmlTdId}"
		var role = "${role}"
		var cellIds = "${htmlTdId}".split(",")
		var cellsCount = cellIds.length - 1
		for(i = 0; i < cellsCount; i++){
			var cellId = cellIds[i] 
			// Show menu when #myDiv is clicked
			if(role && cells){
				$("#"+cellId).contextMenu('transitionMenu', {
					onShowMenu: function(e, menu) {
						$(".cell-selected").attr('class',$("#cssClassId").val());
						var cssname = $("#"+$(e.target).attr("id")).attr('class')
						$("#cssClassId").val(cssname)
						$("#"+$(e.target).attr("id")).attr("class","cell-selected")
			      		${remoteFunction(action:'getMenuList', params:'\'id=\' + $(e.target).attr("id") ', onComplete:'updateMenu(e,menu)')};
			        	return menu;
			      	},
					bindings: {
		        		'done': function(t) {
				       		${remoteFunction(action:'createTransitionForNA', params:'\'actionId=\' + t.id +\'&type=done\'', onComplete:'updateTransitionRow(e)' )};
				        },
				        'ready': function(t) {
				          ${remoteFunction(action:'createTransitionForNA', params:'\'actionId=\' + t.id +\'&type=ready\'', onComplete:'updateTransitionRow(e)' )};
				        },
				        'NA': function(t) {
				          ${remoteFunction(action:'createTransitionForNA', params:'\'actionId=\' + t.id +\'&type=NA\'', onComplete:'updateTransitionRow(e)' )};
				        },
				        'pending': function(t) {
				          ${remoteFunction(action:'createTransitionForNA', params:'\'actionId=\' + t.id +\'&type=pending\'', onComplete:'updateTransitionRow(e)' )};
				        },
				        'void': function(t) {
				          	if(confirm("Void this and any successive transitions. Are you sure?")){
				          		${remoteFunction(action:'createTransitionForNA', params:'\'actionId=\' + t.id +\'&type=void\'', onComplete:'updateTransitionRow(e)' )};
							} else {
				          		return false
				         	}
				        },
				        'noOptions': function(t){
				        	$(".cell-selected").attr('class',$("#cssClassId").val());
				        }
			      	}
		    	});
			}
			$("#"+cellId).mouseover(function(){
				fieldId = this.id.toString()
				timeInterval = setTimeout("${remoteFunction(controller:'assetEntity', action:'showStatus', params:'\'id=\'+fieldId', onComplete:'window.status = e.responseText')}",2000);
			});
			$("#"+cellId).mouseout(function(){
				window.status = ""
			});
		}
	});
</script>
<script type="text/javascript">
	/*------------------------------------------------------------
	 * update the menu for transition 
	 *------------------------------------------------------------*/
	function updateMenu(e,menu){
		var actionType = e.responseText
		if( actionType == "noTransMenu"){
			$('#pending, #void, #ready, #noOptions', menu ).remove()
		} else if( actionType == "naMenu") {
			$('#NA, #void, #ready, #noOptions', menu ).remove()
		} else if( actionType == "doneMenu") {
			$('#done, #void, #ready, #noOptions', menu ).remove()
		} else if( actionType == "readyMenu") {
			$('#NA, #done, #void, #pending, #noOptions', menu ).remove()
		} else if( actionType == "voidMenu") {
			$('#NA, #done, #ready, #pending, #noOptions', menu ).remove()
		} else if( actionType == "doMenu") {
			$('#NA, #ready, #pending, #void, #noOptions', menu ).remove()
		} else {
			$('#NA, #done, #ready, #pending, #void', menu ).remove()
		}
		menu.show()
	}
	/*-------------------------------------------------------------
	 * update the row as per user transition transition 
	 *------------------------------------------------------------*/
	function updateTransitionRow( e ){
		var assetTransitions = eval('(' + e.responseText + ')')
		var length = assetTransitions.length
		if(length > 0){
			for( i=0; i<length; i++ ) {
				var transition = assetTransitions[i]
				$("#"+transition.id).attr("class",transition.cssClass )
			}
		}
	}
	function editAssetDialog() {
		timedRefresh('never')
		$("#showDialog").dialog("close")
		$("#editDialog").dialog('option', 'width', 600)
		$("#editDialog").dialog('option', 'position', ['center','top']);
		$("#editDialog").dialog("open")
		
	}
		    
	function showEditAsset(e) {
   		var assetEntityAttributes = eval('(' + e.responseText + ')')
			if (assetEntityAttributes != "") {
		    	var length = assetEntityAttributes.length
				for (var i=0; i < length; i ++) {
					var attribute = assetEntityAttributes[i]
				    var tdId = $("#"+attribute.attributeCode+'_'+attribute.id)
				    if(tdId != null ){
				    	tdId.html( attribute.value )
				    }
				}
				  $("#editDialog").dialog("close")
			} else {
		   		alert("Asset Entity is not updated")
			}
	}

	function showChangeStatusDialog(e){
		timedRefresh('never')
		var task = eval('(' + e.responseText + ')');
		var taskLen = task[0].item.length;
		var options = '';
		if(taskLen == 0){
			alert('Sorry but there were no common states for the assets selected');
			return false;
		}else{
	      	for (var i = 0; i < taskLen; i++) {
	        	options += '<option value="' + task[0].item[i].state + '">' + task[0].item[i].label + '</option>';
	      	}
	      	$("select#taskList").html(options);
	      	if(taskLen > 1 && task[0].item[0].state == "Hold"){
	      		$('#taskList').children().eq(1).attr('selected',true);
	      	}
	       	$('#asset').val(task[0].asset);
			$("#changeStatusDialog").dialog('option', 'width', 400)
			$("#changeStatusDialog").dialog('option', 'position', ['center','top']);
			$('#changeStatusDialog').dialog('open');
			$('#createCommentDialog').dialog('close');
			$('#commentsListDialog').dialog('close');
			$('#editCommentDialog').dialog('close');
			$('#showCommentDialog').dialog('close');
			$('#showDialog').dialog('close');
			$('#editDialog').dialog('close');
			$('#createDialog').dialog('close');
		}
	}
	
	function submitAction(){
		if(doCheck()){
			document.changeStatusForm.action = "changeStatus";
			document.changeStatusForm.submit();
			timedRefresh($("#selectTimedId").val())
		}else{
			return false;
		}
	}
	
	function doCheck(){
		var taskVal = $('#taskList').val();
		var noteVal = $('#enterNote').val();
		if((taskVal == "Hold")&&(noteVal == "")){
			alert('Please Enter Note');
			return false;
		}else{
			return true;
		}
	}
	
	function setRefreshTime(e) {
		var timeRefresh = eval("(" + e.responseText + ")")
		if(timeRefresh){
			timedRefresh(timeRefresh[0].refreshTime.CLIENT_CONSOLE_REFRESH)
		}
	}
	
	var timer
	function timedRefresh(timeoutPeriod) {
		if(timeoutPeriod != 'never'){
			clearTimeout(timer)
			timer = setTimeout("doAjaxCall()",timeoutPeriod);
			$("#selectTimedId").val( timeoutPeriod );
		} else {
			clearTimeout(timer)
		}
	}
	
	function pageReload(){
		if('${myForm}'){
		document.forms['${myForm}'].submit() ;
		} else {
			window.location = document.URL;
		}
	}
	
	function doAjaxCall(){
		var moveBundle = $("#moveBundleId").val();
		var application = $("#applicationId").val();
		var appOwner = $("#appOwnerId").val();
		var appSme = $("#appSmeId").val();
		var lastPoolTime = $("#lastPoolTimeId").val();
		${remoteFunction(action:'getTransitions', params:'\'moveBundle=\' + moveBundle +\'&application=\'+application +\'&appOwner=\'+appOwner+\'&appSme=\'+appSme +\'&lastPoolTime=\'+lastPoolTime', onComplete:'updateTransitions(e);' )}
		timedRefresh($("#selectTimedId").val())
	}
	
	function updateTransitions(e){
		try{
			var assetEntityCommentList = eval('(' + e.responseText + ')');
			var assetTransitions = assetEntityCommentList[0].assetEntityList;
			var assetComments = assetEntityCommentList[0].assetCommentsList;
			var assetslength = assetTransitions.length;
			var assetCommentsLength = assetComments.length;
			var sessionStatus = isNaN(parseInt(assetslength));
			if( !sessionStatus ){
				if(assetTransitions){
					for( i = 0; i <assetslength ; i++){
						var assetTransition = assetTransitions[i]
						var action = $("#action_"+assetTransition.id)
						if(action){
							if(!assetTransition.check){
								action.html('&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;');
							}
						}
						var application = $("#application_"+assetTransition.id)
						if(application){
							application.html( assetTransition.application );
						}
						var owner = $("#appOwner_"+assetTransition.id)
						if(owner){
							owner.html( assetTransition.appOwner );
						}
						var sme = $("#appSme_"+assetTransition.id)
						if(sme){
							sme.html( assetTransition.appSme );
						}
						var assetName = $("#assetName_"+assetTransition.id)
						if(assetName){
							assetName.html( assetTransition.assetName );
						}
						var tdIdslength = assetTransition.tdId.length
						for(j = 0; j< tdIdslength ; j++){
							var transition = assetTransition.tdId[j]
							var transTd = $("#"+transition.id)
							transTd.attr("class",transition.cssClass )
						}
					}
				}
				if(assetComments){
					for( i = 0; i <assetCommentsLength ; i++){
						var assetComment = assetComments[i]
						var commentIcon = $("#icon_"+assetComment.assetEntityId)
						if(commentIcon){
							var link = document.createElement('a');
							link.href = '#';
							link.id = assetComment.assetEntityId;
							if ( assetComment.type == "database_table_light.png" ) {
								link.onclick = function(){$('#newAssetCommentId').val(this.id);createNewAssetComment('');};
							} else {
								link.onclick = function(){$('#createAssetCommentId').val(this.id);new Ajax.Request('../assetEntity/listComments?id='+this.id,{asynchronous:true,evalScripts:true,onComplete:function(e){listCommentsDialog(e,'action');}})} //;return false
							}
							link.innerHTML = "<img src=\"../images/skin/"+assetComment.type+"\" border=\"0px\">";
							commentIcon.html(link);
						}
					}
				}
				$("#lastPoolTimeId").val(assetEntityCommentList[0].lastPoolTime)
			} else {
			location.reload(false);
			//timedRefresh('never')
			}
		} catch(ex){
		location.reload(false);
		}
	}

	function changeState(){
		timedRefresh('never')
		var assetArr = new Array();
		var totalAsset = ${assetEntityList.id};
		var j=0;
		for(i=0; i< totalAsset.size() ; i++){
			if($('#checkId_'+totalAsset[i]) != null){
				var booCheck = $('#checkId_'+totalAsset[i]).is(':checked');
				if(booCheck == true){
					assetArr[j] = totalAsset[i];
					j++;
				}
			}
		}	
		if(j == 0){
			alert('Please select the Asset');
		}else{
			${remoteFunction(action:'getList', params:'\'assetArray=\' + assetArr', onComplete:'showChangeStatusDialog(e);' )}
		}
	}
	
	var isFirst = true;
	function selectAll(){
		timedRefresh('never')
		var totalCheck = document.getElementsByName('checkChange');
		if(isFirst){
			for(i=0;i<totalCheck.length;i++){
				totalCheck[i].checked = true;
			}
			isFirst = false;
		}else{
			for(i=0;i<totalCheck.length;i++){
				totalCheck[i].checked = false;
			}
			isFirst = true;
		}
	}
	
	function showAssetDetails( assetId ){
		document.editForm.id.value=assetId
		//${remoteFunction(controller:'assetEntity', action:'editShow', params:'\'id=\'+ assetId', before:'document.showForm.id.value ='+ assetId+';', onComplete:'showAssetDialog(e , \'show\')')}
	}
	function vpWidth(type) {
		var data
		if(type == "width"){
			data  = window.innerWidth || document.documentElement.clientWidth || document.body.clientWidth;
		} else {
			data  = window.innerHeight || document.documentElement.clientHeight || document.body.clientHeight;
		}
		return data
	}

	function createNewAssetComment(asset){
		if(asset) {
			setAssetId( asset );
		} else {
			setAssetId( $('#newAssetCommentId').val() );
		}
		$('#statusId').val('new');
		$('#createCommentDialog').dialog('option', 'width', 700);
		$('#createCommentDialog').dialog('open');
		$('#commentsListDialog').dialog('close');
		$('#editCommentDialog').dialog('close');
		$('#showCommentDialog').dialog('close');
		$('#showDialog').dialog('close');
		$('#editDialog').dialog('close');
		$('#createDialog').dialog('close');
		$('#changeStatusDialog').dialog('close');
		document.createCommentForm.mustVerify.value=0;
		document.createCommentForm.reset();
	}
	//To catch the event and call the specific remotefunction 
	function catchevent(event) {
		var oSource = event.target
		var srcElement = event.srcElement ? event.srcElement : event.target;
		eventSrcID=(srcElement)?srcElement.id:'undefined';
		var idArray = eventSrcID.split('_')
		if( idArray.length = 2  ) {
			var assetId = idArray[1]
			if( idArray[0] == "comment"  ) {
				${remoteFunction(controller:'assetEntity', action:'listComments', params:'\'id=\'+assetId', before:'setAssetId(assetId);',onComplete:'listCommentsDialog( e ,\"action\");')}
			}else if( idArray[0] == "asset"  ) {
				${remoteFunction(controller:'assetEntity', action:'editShow', params:'\'id=\'+assetId', before:'showAssetDetails(assetId);',	onComplete:'showAssetDialog( e ,\"show\");')}	
			}else if( idArray[0] == "task" ) {
				${remoteFunction( action:"getTask", params:'\'assetEntity=\'+assetId', onComplete:'showChangeStatusDialog(e);')}
			}
		}
	}
	
		
</script>
</head>
<body>
<div title="Change Status" id="changeStatusDialog"
	style="background-color: #808080;display: none;">
	<input type="hidden" id="cssClassId">
<form name="changeStatusForm"><input type="hidden" name="asset"
	id="asset" /> <input type="hidden" name="projectId" id="projectId"
	value="${projectId}" />
	<input type="hidden" name="moveBundle" id="moveBundle"
	value="${moveBundleInstance.id}" />
<table style="border: 0px; width: 100%">
	<tr>
		<td width="40%"><strong>Change status for selected
		devices to:</strong></td>
		<td width="60%"></td>
	</tr>
	<tr>
		<td><select id="taskList" name="taskList" style="width: 250%">

		</select></td>
	</tr>
	<tr>
		<td>
		<textarea rows="2" cols="1"  title="Enter Note..." name="enterNote" id="enterNote" style="width: 200%"></textarea>
		</td>
	</tr>
	<tr>
		<td></td>
		<td style="text-align: right;"><input type="button" value="Save"
			onclick="var booConfirm = confirm('Are you sure?');if(booConfirm)submitAction()" /></td>
	</tr>
</table>
</form>
</div>
<div style="width:100%">
<div style="width: 100%;">
	<g:form	name="listForm" action="list" method="post">
	<input type="hidden" id="role" value="${role}"/>
	<input type="hidden" id="lastPoolTimeId" value="${lastPoolTime}">
	<input type="hidden" id="projectId" name="projectId" value="${projectId }" />
	<table style="border: 0px;">
		<tr>
			<td valign="top" class="name"><label for="moveBundle">Move
		Bundle:</label>&nbsp;<select id="moveBundleId"
			name="moveBundle" onchange="document.listForm.submit()" >	

			<g:each status="i" in="${moveBundleInstanceList}"
				var="moveBundleInstance">
				<option value="${moveBundleInstance?.id}">${moveBundleInstance?.name}</option>
			</g:each>

		</select></td>
			<td><h1 align="center">PMO Dashboard</h1></td>
			<td style="text-align: right;">
			<input type="hidden" name="last_refresh" value="${new Date()}">
			<input type="hidden" name="myForm" value="listForm">
			<input type="button"
				value="Refresh" onclick="pageReload();"> <select
				id="selectTimedId"
				onchange="${remoteFunction(action:'setTimePreference', params:'\'timer=\'+ this.value ' , onComplete:'setRefreshTime(e)') }">
				<option value="30000">30 sec</option>
				<option value="60000">1 min</option>
				<option value="120000">2 min</option>
				<option value="300000">5 min</option>
				<option value="never">no refresh</option>
			</select></td>
		</tr>
	</table>

</div>
<g:if test="${browserTest}">
<div class="tableContainerIE">
</g:if>
<g:else>
<div class="tableContainer">
</g:else>

<table cellpadding="0"   cellspacing="0"  style="border:0px;" >
	<thead>
		<tr>
		 <th style="padding-top:35px;"><span>Actions</span><jsec:hasAnyRole in="['ADMIN','MANAGER', 'PROJ_MGR']"> 
		 <input type="button" value="State..." onclick="changeState()" title="Change State" style="width: 80px;"/>
		 <a href="#" onclick="selectAll()" ><u style="color:blue;">All</u></a></jsec:hasAnyRole></th>
			<th style="padding-top:35px;" >
			<table style="border: 0px;">
				<thead>
				<tr style="background-color: #CCC">
					<g:sortableColumn style="border:0px;" property="application"  title="Application" params="['projectId':projectId,'application':appValue,'appOwner':appOwnerValue,'appSme':appSmeValue]"/>
				</tr>
				<tr style="background-color: #CCC">
					<th style="border:0px;">
						<select id="applicationId" name="application" onchange="document.listForm.submit();" style="width: 120px;">
							<option value="" selected="selected">All</option>
							<g:each in="${applicationList}" var="application">
								<option value="${application[0] ? application[0] : 'blank'}">${application[0] ? application[0] : 'blank'}&nbsp;(${application[1]})</option>
							</g:each>
						</select>
					</th>
				</tr>
				</thead>
			</table>
			</th>
			<th style="padding-top:35px;">
			<table style="border: 0px;">
				<thead>
				<tr style="background-color: #CCC">
					<g:sortableColumn style="border:0px;" property="app_owner" title="App Owner"  params="['projectId':projectId,'application':appValue,'appOwner':appOwnerValue,'appSme':appSmeValue]" />
				</tr>
				<tr style="background-color: #CCC">
					<th style="border:0px;">
						<select id="appOwnerId" name="appOwner"	onchange="document.listForm.submit();" style="width: 120px;">
							<option value="" selected="selected">All</option>
							<g:each in="${appOwnerList}" var="appOwner">
								<option value="${appOwner[0] ? appOwner[0] : 'blank'}">${appOwner[0] ? appOwner[0] : 'blank'}&nbsp;(${appOwner[1]})</option>
							</g:each>
						</select>
					</th>
				</tr>
				</thead>
			</table>
			</th>
			<th style="padding-top:35px;">
			<table style="border: 0px;">
				<thead>
				<tr style="background-color: #CCC">
					<g:sortableColumn style="border:0px;" property="app_sme" title="App SME" params="['projectId':projectId,'application':appValue,'appOwner':appOwnerValue,'appSme':appSmeValue]"/>
				</tr>
				<tr style="background-color: #CCC">
					<th style="border:0px;">
						<select id="appSmeId" name="appSme" onchange="document.listForm.submit();" style="width: 120px;">
							<option value="" selected="selected">All</option>
							<g:each in="${appSmeList}" var="appSme">
								<option value="${appSme[0] ? appSme[0] : 'blank'}">${appSme[0] ? appSme[0] : 'blank'}&nbsp;(${appSme[1]})</option>
							</g:each>
						</select>
					</th>
				</tr>
				</thead>
			</table>
			</th>
			<th style="padding-top:35px;">
			<table style="border: 0px;">
				<thead>
				<tr style="background-color: #CCC">
					<g:sortableColumn style="border:0px;" property="asset_name" title="Asset Name" params="['projectId':projectId,'application':appValue,'appOwner':appOwnerValue,'appSme':appSmeValue]"/>
				</tr>
				<tr style="background-color: #CCC">
					<td style="padding-left: 0px;"><select id="hiddenSelect" style="width: 120px;visibility: hidden;"/></td>
				</tr>
				</thead>
			</table>
			</th>
			<g:if test="${browserTest}">
			<g:each in="${processTransitionList}"  var="task">
				<th class="verticaltext" title="${task.header}">${task?.header}</th>
			</g:each>
			</g:if>
			<g:else>
			<th style="padding-left: 0px" colspan="${headerCount}"><embed src="${createLinkTo(dir:'templates',file:'headerSvg.svg')}" type="image/svg+xml" width="${headerCount*21.80}" height="102"/></th>
			</g:else>

		</tr>
	</thead>
	<tbody id="assetListTbody" onclick="catchevent(event)">
		<g:if test="${assetEntityList}">
		<g:each in="${assetEntityList}" var="assetEntity">
			<tr id="assetRow_${assetEntity.id}" >
			<td>
			<jsec:hasAnyRole in="['ADMIN','MANAGER','PROJ_MGR']">
			<span id="action_${assetEntity.id}">
				<g:if test="${assetEntity.checkVal == true}">
					<g:checkBox name="checkChange" id="checkId_${assetEntity.id}" onclick="timedRefresh('never')"></g:checkBox>
						<img id="task_${assetEntity.id}"src="${createLinkTo(dir:'images/skin',file:'database_edit.png')}"	border="0px">
				</g:if>
				<g:else>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</g:else>
			</span>
			</jsec:hasAnyRole>
			<img id="asset_${assetEntity.id}" src="${createLinkTo(dir:'images',file:'asset_view.png')}" border="0px">
			<span id="icon_${assetEntity.id}">
				<g:if test="${AssetComment.find('from AssetComment where assetEntity = '+assetEntity.id+' and commentType = ? and isResolved = ?',['issue',0])}">
					
						<img id="comment_${assetEntity.id}" src="${createLinkTo(dir:'images/skin',file:'database_table_red.png')}"	border="0px">
				</g:if>
				<g:else>
					<g:if test="${AssetComment.find('from AssetComment where assetEntity = '+assetEntity.id)}">
							<img  id="comment_${assetEntity.id}" src="${createLinkTo(dir:'images/skin',file:'database_table_bold.png')}"	border="0px">
					</g:if>
					<g:else>
					<jsec:hasAnyRole in="['ADMIN','MANAGER','PROJ_MGR']">
						<a href="javascript:createNewAssetComment(${assetEntity.id});" >
							<img  src="${createLinkTo(dir:'images/skin',file:'database_table_light.png')}"	border="0px">
						</a>
					</jsec:hasAnyRole>
					</g:else>
			</g:else>
			</span>
			</td>
			<td  id="${assetEntity.id}_application" >${assetEntity?.application}&nbsp;</td>
			<td id="${assetEntity.id}_appOwner" >${assetEntity?.appOwner}&nbsp;</td>
			<td id="${assetEntity.id}_appSme" >${assetEntity?.appSme}&nbsp;</td>
			<td id="${assetEntity.id}_assetName" >${assetEntity?.assetName}&nbsp;</td>
			<g:each in="${assetEntity.transitions}" var="transition" >${transition}</g:each>
			</tr>
		</g:each>
		</g:if>
		<g:else>
			<tr><td colspan="40" class="no_records">No records found</td></tr>
		</g:else>
	</tbody>
</table>
</g:form>
</div>
<div id="commentsListDialog" title="Show Asset Comments"
	style="display: none;"><br>
<div class="list">
<table id="listCommentsTable" >
	<thead>
		<tr>
			<g:if test="${role}">
			<th nowrap>Action</th>
			</g:if>
			
			<th nowrap>Comment</th>

			<th nowrap>Comment Type</th>
			
			<th nowrap>Resolved</th>

			<th nowrap>Must Verify</th>
			
			<th nowrap>Category</th>  
	          
	        <th nowrap>Comment Code</th> 

		</tr>
	</thead>
	<tbody id="listCommentsTbodyId">

	</tbody>
</table>
</div>
<jsec:hasAnyRole in="['ADMIN','MANAGER','PROJ_MGR']">
<div class="nav" style="border: 1px solid #CCCCCC; height: 11px">
<span class="menuButton"><a class="create" href="#"
	onclick="$('#statusId').val('');$('#createResolveDiv').css('display', 'none') ;$('#createCommentDialog').dialog('option', 'width', 700);$('#createCommentDialog').dialog('option', 'position', ['center','top']);$('#createCommentDialog').dialog('open');$('#showCommentDialog').dialog('close');$('#editCommentDialog').dialog('close');$('#showDialog').dialog('close');$('#editDialog').dialog('close');$('#createDialog').dialog('close');document.createCommentForm.mustVerify.value=0;document.createCommentForm.reset();">New
Comment</a></span></div>
</jsec:hasAnyRole>
</div>
<div id="createCommentDialog" title="Create Asset Comment"
	style="display: none;"><input type="hidden" name="assetEntity.id"
	id="createAssetCommentId" value=""> <input type="hidden"
	name="status" id="statusId" value=""> 
	<input type="hidden" id="newAssetCommentId" value=""/><g:form
	action="saveComment" method="post" name="createCommentForm">
	<input type="hidden" name="category" value="moveday"/>
	<div class="dialog" style="border: 1px solid #5F9FCF">
	<div>
	<table id="createCommentTable" style="border: 0px">
		
			<tr class="prop" >
				<td valign="top" class="name"><label for="commentType">Comment
				Type:</label></td>
				<td valign="top" style="width: 20%;" ><g:select id="commentType"
					name="commentType"
					from="${AssetComment.constraints.commentType.inList}" value=""
					noSelection="['':'please select']" onChange="commentChange('#createResolveDiv','createCommentForm')"></g:select>&nbsp;&nbsp;&nbsp;&nbsp;			
				
				<input type="checkbox"
					id="mustVerifyEdit" name="mustVerify" value="0"
					onclick="if(this.checked){this.value = 1} else {this.value = 0 }" />&nbsp;&nbsp;
					<label for="mustVerify">Must Verify</label>
				</td>
			</tr>
			<tr class="prop">
				<td valign="top" class="name"><label for="comment">Comment:</label>
				</td>
				<td valign="top" class="value"><textarea cols="80" rows="5"
					id="comment" name="comment"></textarea></td>
			</tr>
		
	</table>
	</div>
	<div id="createResolveDiv" style="display: none;">
		<table id="createResolveTable" style="border: 0px" >
            <tr class="prop">
            	<td valign="top" class="name">
                <label for="isResolved">Resolved:</label>
                </td>
                <td valign="top" class="value">
                <input type="checkbox" id="isResolved" name="isResolved" value="0" onclick="if(this.checked){this.value = 1} else {this.value = 0 }"/>
                </td>
            </tr>
          
            <tr class="prop">
				<td valign="top" class="name">
                <label for="resolution">Resolution:</label>
                </td>
				<td valign="top" class="value">
                <textarea cols="80" rows="5" id="resolution" name="resolution" ></textarea>
                </td>
            </tr> 
                
            </table>
            </div>
		
	</div>
	<div class="buttons"><span class="button"> <input
		class="save" type="button" value="Create"
		onclick="resolveValidate('createCommentForm','createAssetCommentId');" /></span></div>
</g:form></div>
<div id="showCommentDialog" title="Show Asset Comment"
	style="display: none;">
<div class="dialog" style="border: 1px solid #5F9FCF"><input name="id" value="" id="commentId"
	type="hidden">
	<div>
<table id="showCommentTable" style="border: 0px">
	
	<tr>
	<td valign="top" class="name"><label for="dateCreated">Created
			At:</label></td>
			<td valign="top" class="value" id="dateCreatedId" />
	</tr>
		<tr>
	<td valign="top" class="name"><label for="createdBy">Created
			By:</label></td>
			<td valign="top" class="value" id="createdById" />
	</tr>
		
		<tr class="prop">
			<td valign="top" class="name"><label for="commentType">Comment
			Type:</label></td>
			<td valign="top" class="value" id="commentTypeTdId" />
		</tr>
		
		<tr>
		
	<td valign="top" class="name"><label for="category">Category:
			</label></td>
			<td valign="top" class="value" id="categoryTdId" />
	</tr>
	
	<tr class="prop">
	<td valign="top" class="name"><label for="commentCode">comment
			Code:</label></td>
			<td valign="top" class="value" id="commentCodeTdId" />
	</tr>
	
		<tr class="prop">
			<td valign="top" class="name"><label for="mustVerify">Must
			Verify:</label></td>
			<td valign="top" class="value" id="verifyTdId"><input
				type="checkbox" id="mustVerifyShowId" name="mustVerify" value="0"
				disabled="disabled" /></td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name"><label for="comment">Comment:</label>
			</td>
			<td valign="top" class="value" ><textarea cols="80" rows="5"
					id="commentTdId" readonly="readonly"></textarea> </td>
		</tr>
		</table>
		</div>
		<div id="showResolveDiv" style="display: none;">
		<table id="showResolveTable" style="border: 0px">
		<tr class="prop">
			<td valign="top" class="name"><label for="isResolved">Is
			Resolved:</label></td>
			<td valign="top" class="value" id="resolveTdId"><input
				type="checkbox" id="isResolvedId" name="isResolved" value="0"
				disabled="disabled" /></td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name"><label for="resolution">Resolution:</label>
			</td>
			<td valign="top" class="value" ><textarea cols="80" rows="5"
					id="resolutionId" readonly="readonly"></textarea> </td>
		</tr>
			<tr>
	<td valign="top" class="name"><label for="dateResolved">Resolved
			At:</label></td>
			<td valign="top" class="value" id="dateResolvedId" />
	</tr>
		<tr>
	<td valign="top" class="name"><label for="resolvedBy">Resolved
			By:</label></td>
			<td valign="top" class="value" id="resolvedById" />
	</tr>
	
</table>
</div>
<jsec:hasAnyRole in="['ADMIN','MANAGER','PROJ_MGR']">
<div class="buttons"><span class="button"> <input
	class="edit" type="button" value="Edit"
	onclick="commentChangeEdit('editResolveDiv','editCommentForm');$('#editCommentDialog').dialog('option', 'width', 700);$('#editCommentDialog').dialog('option', 'position', ['center','top']);$('#createCommentDialog').dialog('close');$('#showCommentDialog').dialog('close');$('#editCommentDialog').dialog('open');$('#showDialog').dialog('close');$('#editDialog').dialog('close');$('#createDialog').dialog('close')" />
</span> <span class="button"> <input class="delete" type="button"
	value="Delete"
	onclick="${remoteFunction(controller:'assetEntity', action:'deleteComment', params:'\'id=\' + $(\'#commentId\').val() +\'&assetEntity=\'+$(\'#createAssetCommentId\').val() ', onComplete:'listCommentsDialog(e,\'action\')')}" />
</span></div>
</jsec:hasAnyRole>
</div></div>
<div id="editCommentDialog" title="Edit Asset Comment"
	style="display: none;"><g:form action="updateComment"
	method="post" name="editCommentForm">
	<div class="dialog" style="border: 1px solid #5F9FCF">
	<input type="hidden" name="id" id="updateCommentId" value="">
	<div>
	<table id="updateCommentTable" style="border: 0px">
		
		
			<tr>
	<td valign="top" class="name"><label for="dateCreated">Created
			At:</label></td>
			<td valign="top" class="value" id="dateCreatedEditId"  />
	</tr>
		<tr>
	<td valign="top" class="name"><label for="createdBy">Created
			By:</label></td>
			<td valign="top" class="value" id="createdByEditId" />
	</tr>
			<tr class="prop" >
				<td valign="top" class="name"><label for="commentType">Comment
				Type:</label></td>
				<td valign="top" style="width: 20%;" >
				<jsec:hasAnyRole in="['ADMIN','PROJ_MGR']">
				<g:select id="commentTypeEditId"
					name="commentType"
					from="${AssetComment.constraints.commentType.inList}" value=""
					 onChange="commentChange('#editResolveDiv','editCommentForm')"></g:select>&nbsp;&nbsp;&nbsp;&nbsp;			
				</jsec:hasAnyRole>
				<jsec:lacksAllRoles in="['ADMIN','PROJ_MGR']">
				
				<input type="text" id="commentTypeEditId" name="commentType" readonly style="border: 0;">&nbsp;&nbsp;&nbsp;&nbsp;
				</jsec:lacksAllRoles>						
				
				<input type="checkbox" id="mustVerifyEditId" name="mustVerify" value="0"
					onclick="if(this.checked){this.value = 1} else {this.value = 0 }" />&nbsp;&nbsp;
					<label for="mustVerify">Must
				Verify</label>
				</td>
			</tr>
			<tr class="prop">
				<td valign="top" class="name"><label for="category">Category:</label>
				</td>
				<td valign="top" class="value" id="categoryEditId" />
			</tr>
			<tr class="prop">
				<td valign="top" class="name"><label for="commentCode">Comment Code:</label>
				</td>
				<td valign="top" class="value" id="commentCodeEditId" />
			</tr>
			<tr class="prop">
				<td valign="top" class="name"><label for="comment">Comment:</label>
				</td>
				<td valign="top" class="value"><textarea cols="80" rows="5"
					id="commentEditId" name="comment"></textarea></td>
			</tr>
			</table>
			
			</div>
			<div id="editResolveDiv" style="display: none;">
		<table id="updateResolveTable" style="border: 0px">
            <tr class="prop">
            	<td valign="top" class="name">
                <label for="isResolved">Resolved:</label>
                </td>
                <td valign="top" class="value">
                <input type="checkbox" id="isResolvedEditId" name="isResolved" value="0" onclick="if(this.checked){this.value = 1} else {this.value = 0 }"/>
                </td>
            </tr>
          
            <tr class="prop">
				<td valign="top" class="name">
                <label for="resolution">Resolution:</label>
                </td>
				<td valign="top" class="value">
                <textarea cols="80" rows="5" id="resolutionEditId" name="resolution" ></textarea>
                </td>
            </tr> 
               <tr>
	<td valign="top" class="name"><label for="dateResolved">Resolved
			At:</label></td>
			<td valign="top" class="value" id="dateResolvedEditId" />
	</tr>
		<tr>
	<td valign="top" class="name"><label for="resolvedBy">Resolved
			By:</label></td>
			<td valign="top" class="value" id="resolvedByEditId"  />
	</tr>
            </table>
            </div>
		
		

	</div>

	<div class="buttons"><span class="button"> <input
		class="save" type="button" value="Update"
		onclick="resolveValidate('editCommentForm','updateCommentId');" />
	</span> <span class="button"> <input class="delete" type="button"
		value="Delete"
		onclick="${remoteFunction(controller:'assetEntity', action:'deleteComment', params:'\'id=\' + $(\'#updateCommentId\').val() +\'&assetEntity=\'+$(\'#createAssetCommentId\').val() ', onComplete:'listCommentsDialog(e,\'action\')')}" />
	</span></div>
</g:form></div>
<div id="showDialog" title="Show Asset Entity" style="display: none;">
<g:form controller="assetEntity" action="save" method="post" name="showForm">
	<div class="dialog" id="showDiv">
	<table id="showTable">
	</table>
	</div>
	<jsec:hasAnyRole in="['ADMIN','MANAGER','PROJ_MGR']">
	<div class="buttons">
	<input type="hidden" name="id" value="" />
	<input type="hidden" name="projectId" value="${projectId}" />
	<input type="hidden" name="moveBundle" value="${moveBundleInstance.id}" />
	<input type="hidden" name="clientList" value="clientList" />
	 <span class="button"><input
		type="button" class="edit" value="Edit"
		onClick="return editAssetDialog()" /></span> <span class="button"><g:actionSubmit 
		class="delete" onclick="return confirm('Delete Asset, are you sure?');"
		value="Delete" /></span>
		<span class="button"><g:actionSubmit action="remove"
		class="delete"  onclick="return confirm('Remove Asset from project, are you sure?');"
		value="Remove From Project" /></span>
		</div>
		</jsec:hasAnyRole>
</g:form></div>

<div id="editDialog" title="Edit Asset Entity" style="display: none;">
<g:form method="post" name="editForm" controller="assetEntity">
	<input type="hidden" name="id" value="" />
	<input type="hidden" name="projectId" value="${projectId}" />
	<input type="hidden" name="moveBundle" value="${moveBundleInstance.id}" />	
	<input type="hidden" name="clientList" value="clientList" />
	<div class="dialog" id="editDiv">
	
	</div>
	<jsec:hasAnyRole in="['ADMIN','MANAGER','PROJ_MGR']">
	<div class="buttons"><span class="button">
	<input type="button" class="save" value="Update Asset Entity" onClick="${remoteFunction(controller:'assetEntity', action:'getAssetAttributes', params:'\'assetId=\' + document.editForm.id.value ', onComplete:'callUpdateDialog(e)')}" />
	</span> <span class="button"><input type="button"  
		class="delete" onclick="return editDialogDeleteRemove('delete')"
		value="Delete" /></span>
		<span class="button"><input type="button"
		class="delete" onclick="return editDialogDeleteRemove('remove');"
		value="Remove From Project" /></span>
		</div>
		</jsec:hasAnyRole>
</g:form></div>
<div class="contextMenu" id="myMenu"/>
<div class="contextMenu" id="transitionMenu" style="visibility: hidden;">
	<ul>
        <li id="done">Done</li>
        <li id="NA">N/A</li>
        <li id="pending">Pending</li>
        <li id="void">Void</li>
        <li id="ready">Ready</li>
        <li id="noOptions">No Options</li>
    </ul>
</div>
<script type="text/javascript">
initialize();
timedRefresh($("#selectTimedId").val())
if('${browserTest}' == 'true'){
	$("div.tableContainerIE").css("height",vpWidth("height") - 142)
	$("div.tableContainerIE").css("width",vpWidth("width"));
} else {
	$("div.tableContainer").css("width",vpWidth("width"));
	$("#assetListTbody").css("height",vpWidth("height") - 257)
	window.onresize = pageReload;
}
</script>
</body>
</html>
