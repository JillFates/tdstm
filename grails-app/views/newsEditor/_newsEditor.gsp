<html>
	<head>
		<title>Insert title here</title>
		<script type="text/javascript">
			$(document).ready(function() {
    			$("#createNewsDialog").dialog({ autoOpen: false })
			})
		</script>
	</head>
	<body>
  		<div class="body">
  		<div id="createNewsDialog" title="Create News Comment"  style="display: none;">
			<input name="projectId" value="${session.getAttribute("CURR_PROJ").CURR_PROJ}" type="hidden"/>
			<input name="moveBundle" value="${session.getAttribute("CURR_BUNDLE")?.CURR_BUNDLE}" type="hidden"/>
			<input name="moveEvent.id" value="${session.getAttribute("MOVE_EVENT")?.MOVE_EVENT}" type="hidden" id="moveEventId"/>
			<div class="dialog" style="border: 1px solid #5F9FCF">
				<table id="createCommentTable" style="border: 0px">
					<tr>
						<td colspan="2"><div class="required"> Fields marked ( * ) are mandatory </div> </td>
					</tr>
					<tr>
						<td valign="top" class="name"><label>Comment Type${currProjObj?.id}:</label></td>
						<td valign="top" class="value" > 
							<select disabled="disabled">
								<option>News</option>
							</select>
						</td>
					</tr>
					<tr class="prop">
						<td valign="top" class="name"><label for="messageId"><b>Comment:&nbsp;<span style="color: red">*</span></b></label></td>
						<td valign="top" class="value"><textarea cols="80" rows="5" id="messageId" name="message" onkeyup="textCounter(this.id,255)" onkeydown="textCounter(this.id,255)"></textarea></td>
					</tr>
					<tr class="prop">
						<td valign="top" class="name" nowrap="nowrap"><label for="isArchiveId" >Resolved / Archived:</label></td>
						<td valign="top" class="value" id="archivedTdId">
							<input type="checkbox" id="isArchivedId" value="0" onclick="updateHidden('isArchivedId','isArchivedHiddenId')"/>
							<input type="hidden" name="isArchived" value="0" id="isArchivedHiddenId"/>
						</td>
					</tr>
					<tr class="prop">
						<td valign="top" class="name"><label for="resolutionNewsId">Resolution:</label></td>
						<td valign="top" class="value" ><textarea cols="80" rows="5" id="resolutionNewsId" name="resolution"  onkeyup="textCounter(this.id,255)" onkeydown="textCounter(this.id,255)"></textarea> </td>
					</tr>
				</table>
			</div>
			<div class="buttons"><span class="button"> 
				<input class="save" type="submit" value="Save" onclick="return validateCreateNewsForm()"/></span>
					<span class="button"> 
					<input class="delete" type="button" value="Cancel" onclick="this.form.reset();$('#createNewsDialog').dialog('close');"/>
				</span>
			</div>
		</div>
  	</div>
	</body>
</html>
<script type="text/javascript">
function openCreateNewsDialog(){
	$("#createNewsDialog").dialog('option', 'width', 'auto');
	$("#createNewsDialog").dialog('option', 'position', ['center','top']);
	$('#createNewsDialog').dialog('open');
}
function updateHidden(checkBoxId,hiddenId){
	var resolve = $("#"+checkBoxId).is(':checked');
	if(resolve){
		$("#"+hiddenId).val(1);
	} else {
		$("#"+hiddenId).val(0);
	}
}
function validateCreateNewsForm(){
	var moveEvent = $("#moveEventId").val();
	var resolveBoo = $("#isArchivedId").is(':checked');
	var resolveVal = $("#resolutionNewsId").val();
	var comments = $("#messageId").val();
	var validate = false;
	if(moveEvent){
		if(resolveBoo && resolveVal == ""){
			alert('Please enter Resolution');
		} else if( !comments ){
			alert('Please enter Comment');
		} else {
			validate = true;
		}
	} else{
		alert("Please Assign MoveEvent to Current Bundle")
		return false;
	}
	if(validate){
		//timedUpdate( $("#updateTimeId").val() );
		${remoteFunction(controller:'newsEditor',action:'saveNews',params:'\'moveEvent.id=\' + moveEvent +\'&message=\'+ comments +\'&isArchived=\'+$(\'#isArchivedHiddenId\').val()+\'&resolution=\'+$(\'#resolutionNewsId\').val()',onComplete:'closeDiv()')}
	}
}
function closeDiv(){
	$("#createNewsDialog").dialog("close");
	$("#messageId").val("");
	$("#resolutionNewsId").val("");
	$('#isArchivedId').attr("checked",false)
	//$('#isResolvedId').attr("checked",false)
	//$("#showEditCommentDialog").dialog("close");
}
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