<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="projectHeader" />
<title>Move Event News</title>

<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'jquery.autocomplete.css')}" />
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.accordion.css')}" />
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.resizable.css')}" />
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.slider.css')}" />
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.tabs.css')}" />
<script language="javascript" src="${createLinkTo(dir:"plugins/jmesa-0.8/js",file:"jmesa.js")}"></script>
<link rel="stylesheet" type="text/css" href="${createLinkTo(dir:"plugins/jmesa-0.8/css",file:"jmesa.css")}" />
<script type="text/javascript">
function onInvokeAction(id) {
    setExportToLimit(id, '');
    createHiddenInputFieldsForLimitAndSubmit(id);
}
</script>

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
     	
		$("#showEditCommentDialog").dialog('option', 'width', 'auto');
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
	$("#createNewsDialog").dialog('option', 'width', 'auto');
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
<script type="text/javascript">
	  		$('#assetMenu').hide();
	  		$('#bundleMenu').hide();
	  		$('#consoleMenu').show();
	  		$('#reportsMenu').hide();
</script>
<div class="body">
<g:form action="newsEditorList" name="newsEditorForm" method="get">
<div>
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
</div>
<div style="width: 100%; height: auto; border: 1px solid #5F9FCF; margin-top: 10px; padding: 10px 5px 10px 5px;">
<span style="position: absolute; text-align: center; width: auto; margin: -17px 0 0 10px; padding: 0px 8px; background: #ffffff;"><b>Display
Move News and Issues</b></span>
<jmesa:tableFacade id="tag" items="${newsAndCommentsList}" maxRows="25" stateAttr="restore" var="assetCommentInstance" autoFilterAndSort="true" maxRowsIncrements="25,50,100">
       <jmesa:htmlTable style=" border-collapse: separate">
           <jmesa:htmlRow highlighter="true">
               	<jmesa:htmlColumn property="createdAt" sortable="true" filterable="true" pattern="MM/dd/yyyy hh:mm a" cellEditor="org.jmesa.view.editor.DateCellEditor">
					<span onclick="${remoteFunction(action:'getCommetOrNewsData',params:'\'id=\'+'+assetCommentInstance.id+'+\'&commentType='+assetCommentInstance?.commentType+'\'', onComplete:'showEditCommentForm( e, '+i+')')}">
						<tds:convertDateTime date="${assetCommentInstance?.createdAt}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/>
					</span>
				</jmesa:htmlColumn>
				<jmesa:htmlColumn property="createdBy" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">
					<span onclick="${remoteFunction(action:'getCommetOrNewsData',params:'\'id=\'+'+assetCommentInstance.id+'+\'&commentType='+assetCommentInstance?.commentType+'\'', onComplete:'showEditCommentForm( e, '+i+')')}">${assetCommentInstance.createdBy}</span>
				</jmesa:htmlColumn>
				<jmesa:htmlColumn property="commentType" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">
					<span onclick="${remoteFunction(action:'getCommetOrNewsData',params:'\'id=\'+'+assetCommentInstance.id+'+\'&commentType='+assetCommentInstance?.commentType+'\'', onComplete:'showEditCommentForm( e, '+i+')')}">${assetCommentInstance.commentType}</span>
				</jmesa:htmlColumn>
				<jmesa:htmlColumn property="comment" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">
					<span onclick="${remoteFunction(action:'getCommetOrNewsData',params:'\'id=\'+'+assetCommentInstance.id+'+\'&commentType='+assetCommentInstance?.commentType+'\'', onComplete:'showEditCommentForm( e, '+i+')')}">${assetCommentInstance.comment}</span>
				</jmesa:htmlColumn>
	            <jmesa:htmlColumn property="resolution" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">
	            	<span onclick="${remoteFunction(action:'getCommetOrNewsData',params:'\'id=\'+'+assetCommentInstance.id+'+\'&commentType='+assetCommentInstance?.commentType+'\'', onComplete:'showEditCommentForm( e, '+i+')')}"><tds:truncate value="${assetCommentInstance.resolution}"/></span>
	            </jmesa:htmlColumn>
	            <jmesa:htmlColumn property="resolvedAt" sortable="true" filterable="true" pattern="MM/dd/yyyy hh:mm a" cellEditor="org.jmesa.view.editor.DateCellEditor">
	            	<span onclick="${remoteFunction(action:'getCommetOrNewsData',params:'\'id=\'+'+assetCommentInstance.id+'+\'&commentType='+assetCommentInstance?.commentType+'\'', onComplete:'showEditCommentForm( e, '+i+')')}">
	            		<tds:convertDateTime date="${assetCommentInstance?.resolvedAt}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/>
	            	</span>
	            </jmesa:htmlColumn>
	            <jmesa:htmlColumn property="resolvedBy" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">
	            	<span onclick="${remoteFunction(action:'getCommetOrNewsData',params:'\'id=\'+'+assetCommentInstance.id+'+\'&commentType='+assetCommentInstance?.commentType+'\'', onComplete:'showEditCommentForm( e, '+i+')')}"><tds:truncate value="${assetCommentInstance.resolvedBy}"/></span>
	            </jmesa:htmlColumn>
        </jmesa:htmlRow>
    </jmesa:htmlTable>
</jmesa:tableFacade>
</g:form>
<div class="paginateButtons" style="padding: 0px;">
<g:form name="paginateRows" action="newsEditorList">
	<table style="border: 0px;">
		<tr>
			<td style="width: 70px;padding: 0px;">
				 <div class="buttons"> <span class="button"><input type="button" value="Create News" class="save" onclick="openCreateNewsDialog()"/></span></div>
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
	<input name="id" value="" id="commentId" type="hidden"/>
	<input name="commentType" value="" id="commentTypeId" type="hidden"/>
	<input name="projectId" value="${projectId}" type="hidden"/>
	<input name="moveBundle" value="${params.moveBundle}" type="hidden"/>
	<input name="moveEvent" value="${params.moveEvent}" type="hidden"/>
	<input name="viewFilter" value="${params.viewFilter}" type="hidden"/>
		<div>
	<table id="showCommentTable" style="border: 0px">
		
		<tr>
		<td valign="top" class="name"><label for="dateCreated">Created
				At:</label></td>
				<td valign="top" class="value" id="dateCreatedId" ></td>
		</tr>
			<tr>
		<td valign="top" class="name"><label for="createdBy">Created
				By:</label></td>
				<td valign="top" class="value" id="createdById" ></td>
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
				<input type="radio" name="displayOption" value="U" checked="checked" id="displayOptionUid"/>&nbsp;
				<span style="vertical-align: text-top;">User Comment</span>&nbsp;&nbsp;&nbsp;
				<input type="radio" name="displayOption" value="G" id="displayOptionGid"/>&nbsp;
				<span style="vertical-align:text-top;">Generic Comment&nbsp;</span>
				</td>
		</tr>
		<tr class="prop" id="assetTrId">
		<td valign="top" class="name"><label for="assetTdId">Asset:</label></td>
				<td valign="top" class="value"><input type="text" disabled="disabled" id="assetTdId"/></td>
		</tr>
			<tr class="prop">
				<td valign="top" class="name"><label for="comment">Comment:</label>
				</td>
				<td valign="top" class="value" ><textarea cols="80" rows="5"
						id="commentTdId" name="comment" onkeyup="textCounter(this.id,255)" onkeydown="textCounter(this.id,255)"></textarea> </td>
			</tr>
			<tr class="prop">
				<td valign="top" class="name" nowrap="nowrap"><label for="isResolved" >Resolved / Archived:</label></td>
				<td valign="top" class="value" id="resolveTdId">
				<input type="checkbox" id="isResolvedId" value="0" onclick="updateHidden('isResolvedId','isResolvedHiddenId')"/>
				<input type="hidden" name="isResolved" value="0" id="isResolvedHiddenId"/>
				</td>
			</tr>
			<tr class="prop">
				<td valign="top" class="name"><label for="resolution">Resolution:</label>
				</td>
				<td valign="top" class="value" ><textarea cols="80" rows="5"
						id="resolutionId" name="resolution" onkeyup="textCounter(this.id,255)" onkeydown="textCounter(this.id,255)"></textarea> </td>
			</tr>
				<tr>
		<td valign="top" class="name"><label for="dateResolved">Resolved
				At:</label></td>
				<td valign="top" class="value" id="dateResolvedId" ></td>
		</tr>
			<tr>
		<td valign="top" class="name"><label for="resolvedBy">Resolved
				By:</label></td>
				<td valign="top" class="value" id="resolvedById" ></td>
		</tr>
		
	</table>
	</div>
	<div class="buttons"><span class="button"> 
	<input class="save" type="submit" value="Update" onclick="return validateNewsAndCommentForm()"/>
	</span> <span class="button"> 
	<input class="delete" type="button" value="Cancel" onclick="this.form.reset();$('#showEditCommentDialog').dialog('close');"/>
	</span></div>
	</div>
</g:form>
</div>
<div id="createNewsDialog" title="Create News Comment" style="display: none;">
	<g:form action="saveNews" method="post" name="createNewsForm">
	<input name="projectId" value="${projectId}" type="hidden"/>
	<input name="moveBundle" value="${params.moveBundle}" type="hidden"/>
	<input name="viewFilter" value="${params.viewFilter}" type="hidden"/>
	<input name="moveEvent.id" value="${moveEvent?.id}" type="hidden" id="moveEventId"/>
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
					id="messageId" name="message" onkeyup="textCounter(this.id,255)" onkeydown="textCounter(this.id,255)"></textarea></td>
			</tr>
			<tr class="prop">
				<td valign="top" class="name" nowrap="nowrap"><label for="isArchivedId" >Resolved / Archived:</label></td>
				<td valign="top" class="value" id="archivedTdId">
				<input type="checkbox" id="isArchivedId" value="0" onclick="updateHidden('isArchivedId','isArchivedHiddenId')"/>
				<input type="hidden" name="isArchived" value="0" id="isArchivedHiddenId"/>
				</td>
			</tr>
			<tr class="prop">
				<td valign="top" class="name"><label for="resolutionNewsId">Resolution:</label>
				</td>
				<td valign="top" class="value" ><textarea cols="80" rows="5"
						id="resolutionNewsId" name="resolution"  onkeyup="textCounter(this.id,255)" onkeydown="textCounter(this.id,255)"></textarea> </td>
			</tr>
			
		</table>
		</div>
		<div class="buttons"><span class="button"> 
		<input class="save" type="submit" value="Create" onclick="return validateCreateNewsForm()"/></span>
		<span class="button"> 
	<input class="delete" type="button" value="Cancel" onclick="this.form.reset();$('#createNewsDialog').dialog('close');"/>
	</span>
		</div>
	</g:form>
</div></div>
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
} else {
	$("#moveEvent").val("${session.getAttribute('MOVE_EVENT')?.MOVE_EVENT}")
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
/*
 * validate the text area size
*/
function textCounter(fieldId, maxlimit) {
	var value = $("#"+fieldId).val()
    if (value.length > maxlimit) { // if too long...trim it!
    	$("#"+fieldId).val(value.substring(0, maxlimit));
    	return false;
    } else {
    	return true;
    }
}
</script>
<script>
	currentMenuId = "#consoleMenu";
	$("#consoleMenuId a").css('background-color','#003366')
</script>
</body>
</html>
