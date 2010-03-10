<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="projectHeader" />
<title>Move Event News</title>
<g:javascript library="prototype" />
<g:javascript library="jquery" />
<jq:plugin name="jquery.bgiframe.min" />
<jq:plugin name="jquery.autocomplete" />
<link type="text/css" rel="stylesheet"
	href="${createLinkTo(dir:'css',file:'jquery.autocomplete.css')}" />
<link type="text/css" rel="stylesheet"
	href="${createLinkTo(dir:'css',file:'ui.accordion.css')}" />
<link type="text/css" rel="stylesheet"
	href="${createLinkTo(dir:'css',file:'ui.core.css')}" />
<link type="text/css" rel="stylesheet"
	href="${createLinkTo(dir:'css',file:'ui.dialog.css')}" />
<link type="text/css" rel="stylesheet"
	href="${createLinkTo(dir:'css',file:'ui.resizable.css')}" />
<link type="text/css" rel="stylesheet"
	href="${createLinkTo(dir:'css',file:'ui.slider.css')}" />
<link type="text/css" rel="stylesheet"
	href="${createLinkTo(dir:'css',file:'ui.tabs.css')}" />
<link type="text/css" rel="stylesheet"
	href="${createLinkTo(dir:'css',file:'ui.theme.css')}" />
<jq:plugin name="ui.core" />
<jq:plugin name="ui.draggable" />
<jq:plugin name="ui.resizable" />
<jq:plugin name="ui.dialog" />
<script type="text/javascript">
$(document).ready(function() {
    $("#showEditCommentDialog").dialog({ autoOpen: false })
    $("#createNewsDialog").dialog({ autoOpen: false })
})
/*-------------------------------------------
 * @author : Lokanada Reddy
 * @param  : assetComment / moveEventNews object based on comment Type as JSON object
 * @return : Edit form
 *-------------------------------------------*/
function showEditCommentForm(e , rowId){
	var assetComments = eval('(' + e.responseText + ')');
	if (assetComments) {
		var tbody = $("#commetAndNewsBodyId > tr");
		tbody.each(function(n, row){
			if(n == rowId) {
		    	$(row).addClass('selectedRow'); 
		    } else {
		    	$(row).removeClass('selectedRow');
		    }		          		
     	});
     	
		$('#commentId').val(assetComments[0].commentObject.id)
		$('#assetTdId').val(assetComments[0].assetName)
		$('#dateCreatedId').html(assetComments[0].dtCreated);
		if(assetComments[0].personResolvedObj != null){
			$('#resolvedById').html(assetComments[0].personResolvedObj.firstName+" "+assetComments[0].personResolvedObj.lastName);
		}else{
			$('#resolvedById').html("");
			$('#resolvedByEditId').html("");
		}
		$('#createdById').html(assetComments[0].personCreateObj.firstName+" "+assetComments[0].personCreateObj.lastName);
		$('#resolutionId').val(assetComments[0].commentObject.resolution);
		
		if(assetComments[0].commentObject.commentType != 'issue'){

			$('#commentTypeId').val("news")
			$('#dateResolvedId').html(assetComments[0].dtResolved);
			$('#isResolvedId').val(assetComments[0].commentObject.isArchived)
			$('#commentTdId').val(assetComments[0].commentObject.message)
			if(assetComments[0].commentObject.isArchived != 0){
				$('#isResolvedId').attr('checked', true);
				$("#isResolvedHiddenId").val(1);
			} else {
				$('#isResolvedId').attr('checked', false);
				$("#isResolvedHiddenId").val(0);
			}
			$("#displayOptionTr").hide();
			$("#commentTypeOption").html("<option>News</option>");
			$("#assetTrId").hide();
			$("#showEditCommentDialog").dialog('option','title','Edit News Comment');

		} else {

			$('#commentTypeId').val("issue")
			$('#dateResolvedId').html(assetComments[0].dtResolved);
			$('#isResolvedId').val(assetComments[0].commentObject.isResolved)
			$('#commentTdId').val(assetComments[0].commentObject.comment)
			if(assetComments[0].commentObject.isResolved != 0){
				$('#isResolvedId').attr('checked', true);
				$("#isResolvedHiddenId").val(1);
			} else {
				$('#isResolvedId').attr('checked', false);
				$("#isResolvedHiddenId").val(0);
			}
			if(assetComments[0].commentObject.displayOption == "G"){
				$("#displayOptionGid").attr('checked', true);
			} else {
				$("#displayOptionUid").attr('checked', true);
			}
			$("#displayOptionTr").show();
			$("#commentTypeOption").html("<option>Issue</option>");
			$("#assetTrId").show();
			$("#showEditCommentDialog").dialog('option','title','Edit Issues Comment');
			
		}
     	
		$("#showEditCommentDialog").dialog('option', 'width', 700);
		$("#showEditCommentDialog").dialog('option', 'position', ['center','top']);
		$("#showEditCommentDialog").dialog("open");
		$("#createNewsDialog").dialog("close");
		}
}
/*-------------------------------------------
 * @author : Lokanada Reddy
 * @param  : isResolved
 * @return : boolean
 *-------------------------------------------*/
function validateNewsAndCommentForm(){
	var resolveBoo = $("#isResolvedId").is(':checked');
	var resolveVal = $("#resolutionId").val();
	if(resolveBoo && resolveVal == ""){
		alert('Please enter Resolution');
		return false;
	} else {
		return true;
	}
}
function updateHidden(checkBoxId,hiddenId){
	var resolve = $("#"+checkBoxId).is(':checked');
	if(resolve){
		$("#"+hiddenId).val(1);
	} else {
		$("#"+hiddenId).val(0);
	}
}
function openCreateNewsDialog(){
	$("#createNewsDialog").dialog('option', 'width', 700);
	$("#createNewsDialog").dialog('option', 'position', ['center','top']);
	$('#showEditCommentDialog').dialog('close');
	$('#createNewsDialog').dialog('open');
}
function validateCreateNewsForm(){
	var moveEvent = $("#moveEventId").val();
	var resolveBoo = $("#isArchivedId").is(':checked');
	var resolveVal = $("#resolutionNewsId").val();
	
	if(moveEvent){
		if(resolveBoo && resolveVal == ""){
			alert('Please enter Resolution');
			return false;
		} else {
			return true;
		}
	} else{
		alert("Please Assign MoveEvent to Current Bundle")
		return false;
	}
}
</script>
</head>
<body>
<div class="body">

<div>
<g:form action="newsEditorList" name="newsEditorForm">
<input type="hidden" name="projectId" value="${projectId}"/>
	<table style="border: none;" >
		<tr>
			<td nowrap="nowrap">
				<span style="padding-left: 10px;">
				<label for="moveEvent"><b>Event:</b></label>&nbsp;
					<select id="moveEvent" name="moveEvent" onchange="$('#newsEditorForm').submit();">
						<g:each status="i" in="${moveEventsList}" var="moveEventInstance">
							<option value="${moveEventInstance?.id}">${moveEventInstance?.name}</option>
						</g:each>
					</select>
				</span>
				<span style="padding-left: 10px;">
				<label for="moveBundle"><b>Bundle:</b></label>&nbsp;
					<select id="moveBundleId" name="moveBundle" onchange="$('#newsEditorForm').submit();">
						<option value="">All</option>
						<g:each status="i" in="${moveBundlesList}" var="moveBundleInstance">
							<option value="${moveBundleInstance?.id}">${moveBundleInstance?.name}</option>
						</g:each>
					</select>
				</span>
				<span  style="padding-left: 10px;">
					<label for="viewFilter"><b>View:</b></label>&nbsp;
					<select id="viewFilterId" name="viewFilter" onchange="$('#newsEditorForm').submit();">
						<option value="all">All</option>
						<option value="active">Active</option>
						<option value="archived">Archived</option>
					</select>
				</span>
			</td>
		</tr>
	</table>
	</g:form>
</div>
<div style="width: 100%; height: auto; border: 1px solid #5F9FCF; margin-top: 10px; padding: 10px 5px 10px 5px;">
<span style="position: absolute; text-align: center; width: auto; margin: -17px 0 0 10px; padding: 0px 8px; background: #ffffff;"><b>Display
Move News and Issues</b></span>
<table id="assetEntityTable">
	<thead>
		<tr>

			<g:sortableColumn property="createdAt" title="Created At" params="[projectId:projectId, moveBundle:params.moveBundle,moveEvent : params.moveEvent, viewFilter:params.viewFilter]" />

			<g:sortableColumn property="createdBy" title="Created By" params="[projectId:projectId, moveBundle:params.moveBundle,moveEvent : params.moveEvent, viewFilter:params.viewFilter]" />

			<g:sortableColumn property="commentType" title="Type" params="[projectId:projectId, moveBundle:params.moveBundle,moveEvent : params.moveEvent, viewFilter:params.viewFilter]" />

			<g:sortableColumn property="comment" title="Comment" params="[projectId:projectId, moveBundle:params.moveBundle,moveEvent : params.moveEvent, viewFilter:params.viewFilter]" />

			<g:sortableColumn property="resolution" title="Resolution" params="[projectId:projectId, moveBundle:params.moveBundle,moveEvent : params.moveEvent, viewFilter:params.viewFilter]" />

			<g:sortableColumn property="resolvedAt" title="Resolved At" params="[projectId:projectId, moveBundle:params.moveBundle,moveEvent : params.moveEvent, viewFilter:params.viewFilter]" />

			<g:sortableColumn property="resolvedBy" title="Resolved By" params="[projectId:projectId, moveBundle:params.moveBundle,moveEvent : params.moveEvent, viewFilter:params.viewFilter]" />

		</tr>
	</thead>
	<tbody id="commetAndNewsBodyId">
	
	<g:each in="${assetCommentsList}" status="i" var="assetCommentInstance">
		<tr class="${(i % 2) == 0 ? 'even' : 'odd'}" id="commentsRowId_${i}"
			onclick="${remoteFunction(action:'getCommetOrNewsData',params:'\'id=\'+'+assetCommentInstance.id+'+\'&commentType='+assetCommentInstance?.commentType+'\'', onComplete:'showEditCommentForm( e, '+i+')')}">
			<td><tds:convertDateTime date="${assetCommentInstance?.createdAt}"/></td>
			<td>
			${assetCommentInstance?.createdBy}
			</td>
			<g:if test="${assetCommentInstance?.commentType == 'issue'}">
				<td>Issue</td>
				<td>${AssetEntity.get(assetCommentInstance?.assetEntity)?.assetName} : 
				<g:if test="${${assetCommentInstance?.displayOption != 'G' }}">
				<tds:truncate value="${assetCommentInstance?.comment}"/>
				</g:if>
				<g:else>On Hold</g:else>
				</td>
			</g:if>
			<g:else>
				<td>News</td>
				<td><tds:truncate value="${assetCommentInstance?.comment}"/></td>
			</g:else>
			<td>
				<tds:truncate value="${assetCommentInstance?.resolution}"/>
			</td>
			<td><tds:convertDateTime date="${assetCommentInstance?.resolvedAt}"/></td>
			<td>
			${assetCommentInstance?.resolvedBy}
			</td>
		</tr>
	</g:each>
	</tbody>
</table>
<div class="paginateButtons" style="padding: 0px;">
<g:form name="paginateRows" action="newsEditorList">
	<table style="border: 0px;">
		<tr>
			<td style="width: 70px;padding: 0px;">
				 <div class="buttons"> <span class="button"><input type="button" value="Create News" class="save" onclick="openCreateNewsDialog()"></span></div>
			</td>
			<td style="width: 770px;vertical-align: middle;text-align: right;padding: 0px;">
				<g:if test="${totalCommentsSize > 25 }">
					<g:paginate total="${totalCommentsSize}" params="${params }"/>
				</g:if>
			</td>
		</tr>
	</table>
</g:form>
</div>
</div>
<div id="showEditCommentDialog" title="Edit Issue Comment"
	style="display: none;">
<g:form action="updateNewsOrComment" method="post" name="editCommentForm">
	<div class="dialog" style="border: 1px solid #5F9FCF">
	<input name="id" value="" id="commentId" type="hidden">
	<input name="commentType" value="" id="commentTypeId" type="hidden">
	<input name="projectId" value="${projectId}" type="hidden">
	<input name="moveBundle" value="${params.moveBundle}" type="hidden">
	<input name="moveEvent" value="${params.moveEvent}" type="hidden">
	<input name="viewFilter" value="${params.viewFilter}" type="hidden">
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
		<tr>
		<td valign="top" class="name"><label>Comment Type:</label></td>
				<td valign="top" class="value" > 
				<select disabled="disabled" id="commentTypeOption">
				<option>Issue</option>
				</select>
				</td>
		</tr>
		<tr id="displayOptionTr">
			
		<td valign="top" class="name" nowrap="nowrap">
			<label for="category">User / Generic Cmt:</label></td>
				<td valign="top" class="value" id="displayOption" >
				<input type="radio" name="displayOption" value="U" id="displayOptionUid">&nbsp;
				<span style="vertical-align: text-top;">User Comment</span>&nbsp;&nbsp;&nbsp;
				<input type="radio" name="displayOption" value="G" checked="checked" id="displayOptionGid">&nbsp;
				<span style="vertical-align:text-top;">Generic Comment&nbsp;</span>
				</td>
		</tr>
		<tr class="prop" id="assetTrId">
		<td valign="top" class="name"><label for="assetTdId">Asset:</label></td>
				<td valign="top" class="value"><input type="text" disabled="disabled" id="assetTdId"></td>
		</tr>
			<tr class="prop">
				<td valign="top" class="name"><label for="comment">Comment:</label>
				</td>
				<td valign="top" class="value" ><textarea cols="80" rows="5"
						id="commentTdId" name="comment"></textarea> </td>
			</tr>
			<tr class="prop">
				<td valign="top" class="name" nowrap="nowrap"><label for="isResolved" >Resolved / Archived:</label></td>
				<td valign="top" class="value" id="resolveTdId">
				<input type="checkbox" id="isResolvedId" value="0" onclick="updateHidden('isResolvedId','isResolvedHiddenId')"/>
				<input type="hidden" name="isResolved" value="0" id="isResolvedHiddenId">
				</td>
			</tr>
			<tr class="prop">
				<td valign="top" class="name"><label for="resolution">Resolution:</label>
				</td>
				<td valign="top" class="value" ><textarea cols="80" rows="5"
						id="resolutionId" name="resolution"></textarea> </td>
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
	<div class="buttons"><span class="button"> 
	<input class="save" type="submit" value="Update" onclick="return validateNewsAndCommentForm()"/>
	</span> <span class="button"> 
	<input class="delete" type="button" value="Cancel" onclick="this.form.reset();$('#showEditCommentDialog').dialog('close');">
	</span></div>
	</div>
</g:form>
</div>
<div id="createNewsDialog" title="Create News Comment" style="display: none;">
	<g:form action="saveNews" method="post" name="createNewsForm">
	<input name="projectId" value="${projectId}" type="hidden">
	<input name="moveBundle" value="${params.moveBundle}" type="hidden">
	<input name="viewFilter" value="${params.viewFilter}" type="hidden">
	<input name="moveEvent.id" value="${moveEvent?.id}" type="hidden" id="moveEventId">
		<div class="dialog" style="border: 1px solid #5F9FCF">
		<table id="createCommentTable" style="border: 0px">
			<tr>
				<td colspan="2"><div class="required"> Fields marked ( * ) are mandatory </div> </td>
			</tr>
			<tr>
		<td valign="top" class="name"><label>Comment Type:</label></td>
				<td valign="top" class="value" > 
				<select disabled="disabled">
				<option>News</option>
				</select>
				</td>
		</tr>
			<tr class="prop">
				<td valign="top" class="name"><label for="messageId"><b>Comment:&nbsp;<span style="color: red">*</span></b></label>
				</td>
				<td valign="top" class="value"><textarea cols="80" rows="5"
					id="messageId" name="message"></textarea></td>
			</tr>
			<tr class="prop">
				<td valign="top" class="name" nowrap="nowrap"><label for="isArchivedId" >Resolved / Archived:</label></td>
				<td valign="top" class="value" id="archivedTdId">
				<input type="checkbox" id="isArchivedId" value="0" onclick="updateHidden('isArchivedId','isArchivedHiddenId')"/>
				<input type="hidden" name="isArchived" value="0" id="isArchivedHiddenId">
				</td>
			</tr>
			<tr class="prop">
				<td valign="top" class="name"><label for="resolutionNewsId">Resolution:</label>
				</td>
				<td valign="top" class="value" ><textarea cols="80" rows="5"
						id="resolutionNewsId" name="resolution"></textarea> </td>
			</tr>
			
		</table>
		</div>
		<div class="buttons"><span class="button"> 
		<input class="save" type="submit" value="Create" onclick="return validateCreateNewsForm()"/></span>
		<span class="button"> 
	<input class="delete" type="button" value="Cancel" onclick="this.form.reset();$('#createNewsDialog').dialog('close');">
	</span>
		</div>
	</g:form>
</div>
<script type="text/javascript">
var moveBundle = "${params.moveBundle}"
var viewFilter = "${params.viewFilter}"
var moveEvent = "${params.moveEvent}"
if(moveBundle){
	$("#moveBundleId").val(moveBundle)
}
if(viewFilter){
	$("#viewFilterId").val(viewFilter)
}
if(moveEvent){
	$("#moveEvent").val(moveEvent)
}
/*------------------------------------------------------------------
* function to Unhighlight the Asset row when the edit DIV is closed
*-------------------------------------------------------------------*/
$("#showEditCommentDialog").bind('dialogclose', function(){   		
	var assetTable = $("#commetAndNewsBodyId > tr");
	assetTable.each(function(n, row){
		$(row).removeClass('selectedRow');       		
    });   		
});
</script>
</body>
</html>
