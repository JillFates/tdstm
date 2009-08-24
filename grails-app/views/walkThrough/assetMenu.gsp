<html>
<head>
<title>Walkthru&gt; Select Asset</title>
<g:javascript library="prototype" />
<g:javascript library="jquery" />
<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'qvga.css')}" />
<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'walkThrough.css')}" />
<script type="text/javascript">
$(document).ready( function() {
	// Show menu when #myDiv is clicked
	$("#selector").contextMenu({
		menu: 'myMenu'
	},
	function(action) {
		$('#selectCmt').val(action);
		$('form#commentsViewForm').attr({action: 'issuesandcommentsview'}).submit();
	});
});
function missingAsset( type, id, message ){
	if(confirm(message)){
		${remoteFunction(action:'missingAsset', params:'\'id=\' + id +\'&type=\'+type', onComplete:'updateMissingAsset(e,type,id)')}
	}
}
function updateMissingAsset( e, type, id ){
	if(e.responseText = "success"){
		var link = "<a href='#' class='button big' onclick=\"missingAsset"
		if(type == 'create'){
			link +="('resolve','"+id+"', 'Resolve missing asset issue. Are you sure?')\">Missing Asset Found </a>"
		} else {
			link +="('create','"+id+"', 'Mark asset as missing. Are you sure?')\" >Mark Asset Missing </a>"
		}
		$("#missingAsset").html(link)
	}
}
function commentSelect( cmtVal ) {
	if ( cmtVal == 'Select Common Comment' ) {
		$('#comments').val('');
	} else {
		$('#comments').val(cmtVal);
	}
}
    
function callUpdateComment( e, type ) {
	var data = eval('(' + e.responseText + ')');
	if ( data == "true" ) {
		switch ( type ) {
			case 'comment'  	: $('#saveCommentType').val(type);
							 	  $('form#commentForm').attr({action: "saveComment"}).submit();
								  break;
    								  
			case 'instruction' 	: $('#saveCommentType').val(type);
								  $('form#commentForm').attr({action: "saveComment"}).submit();
   								  break;
								  
			case 'issue' 		: $('#saveCommentType').val(type);
							      $('form#commentForm').attr({action: "saveComment"}).submit();
							      break;
		}
	return true;
	} else {
		alert( type +" already exists. " );
		return false;
    }
}
    
function validateCommentSelect() {
	var commentValue = $('#comments').val();
	if ( commentValue ) {
		return true;
	} else {
		alert(" Please Select Common Comment ");
		return false;
	}
}
</script>
</head>
<body>
	<div class=qvga_border>
	<a name="asset_menu"></a>
	<div class=title>Walkthru&gt; Asset Menu</div>
	<div class=input_area>
	<div style="FLOAT: left"><a class=button href="startMenu">Start Over</a></div>
	<div style="FLOAT: right"><a class=button href="selectAsset?moveBundle=${moveBundle}&location=${location}&room=${room}&rack=${rack}">Asset List</a></div>
	<table>
		<tbody>
			<tr>
				<td class=label>Asset Tag:</td>
				<td class=field>${assetEntity?.assetTag}</td>
			</tr>
			<tr>
				<td class=label>Asset Name:</td>
				<td class=field>${assetEntity?.assetName}</td>
			</tr>
		</trody>
	</table>
	<div style="MARGIN-TOP: 15px" align=center>
		<a class="button big" href="#asset_front1">Front Audit</a> <BR style="MARGIN-TOP: 6px">
		<a class="button big" href="#asset_rear1">Rear Audit</a> <BR style="MARGIN-TOP: 6px">
		<a class="button big" href="#comments">Issues/Comments</a> <BR style="MARGIN-TOP: 6px">
		<span id="missingAsset">
			<g:if test="${AssetComment.find('from AssetComment where assetEntity = '+ assetEntity?.id +' and commentType = ? and isResolved = ? and commentCode = ?' ,['issue',0,'ASSET_MISSING'])}">
				<a href="#" class="button big" onclick="missingAsset('resolve', '${assetEntity?.id}','Resolve missing asset issue. Are you sure?')">Missing Asset Found</a>
			</g:if>
			<g:else>
				<a href="#" class="button big" onclick="missingAsset('create', '${assetEntity?.id}','Mark asset as missing. Are you sure?')">Mark Asset Missing </a>
			</g:else>
		</span>
		<div style="MARGIN-TOP: 10px">
			<div class=thefield align=center>
				<a class=button	onclick="alert('Changes have been saved'); return true;" href="#select_asset">Save</a> &nbsp;&nbsp; 
				<a class=button	onclick="alert('Changes have been saved and marked completed'); return true;" href="#select_asset">Completed</a></div>
			</div>
		</div>
	</div>
	<div class="gap"></div>
	<!-- Walkthru Asset:Front pg 1-->
	<g:form action="saveAndCompleteAudit" method="post" >
		<div class="qvga_border">
			<a name="asset_front1"></a>
			<div class="title">Walkthru&gt; Front (1)</div>
			<div class="input_area">
			
			<div style="FLOAT: left"><a class=button href="startMenu">Start Over</a></div>
			<div style="float:right;"><a class="button" href="#asset_menu">Asset Menu</a></div>
			<br class="clear"/>
			<table>
			<tr>
				<td class="label">Asset Tag:</td>
				<td class="field">${assetEntity?.assetTag}</td>
			</tr>
			
			<tr>
				<td class="label">Asset Name:</td>
				<td class="field"><input type="text" class="text" name="assetName" value="${assetEntity?.assetName}" size=20 maxsize=50></td>
			</tr>
			
			<tr>
				<td class="label">Serial #:</td>
				<td class="field"><input type="text" class="text" name="serialNumber" value="${assetEntity?.serialNumber}" size=20 maxsize=50></td>
			</tr>
			
			<tr>
				<td class="label">Device Type:</td>
				<td class="field">
				<select id="deviceTypeId" name="kvmDevice">
			            <option value="${assetEntity?.kvmDevice}">${assetEntity?.kvmDevice}</option>
			    </select>
			</tr>
			
			<tr>
				<td class="label">Manufacturer:</td>
				<td class="field">
				<select id="manufacturerId" name="manufacturer">
			            <option value="${assetEntity?.manufacturer}">${assetEntity?.manufacturer}</option>
				</select>
				</td>
			</tr>
			
			<tr>
				<td class="label">Model:</td>
				<td class="field">
				<select id="modelId" name="model">
			            <option value="${assetEntity?.model}">${assetEntity?.model}</option>
				</select>
				</td>
			</tr>
			
			<tr>
				<td class="label">Rails:</td>
				<td class="field">
				<select id="railTypeId" name="railType">
			            <option value="${assetEntity?.railType}">${assetEntity?.railType}</option>
				</select>
				</td>
			</tr>
			</table>
			
			<div style="margin-top:10px;">
			   <div class="thefield" align="center">
			      <a class="button" href="#asset_front2">Next</a>&nbsp;&nbsp;&nbsp;
			      <a class="button" href="#select_asset" onClick="alert('Changes have been saved'); return true;">Save</a>&nbsp;&nbsp;&nbsp;
			      <a class="button" href="#select_asset"  onClick="alert('Changes have been saved and marked completed'); return true;">Completed</a>
			   </div>
			</div>
			
			</div>
		</div>
		<!-- end of Walkthru Asset:Front (1) -->
		<div class="gap"></div>
		<!-- Walkthru Asset:Front pg 2-->
		<div class="qvga_border">
			<a name="asset_front2"></a>
			<div class="title">Walkthru&gt; Front (2)</div>
			<div class="input_area">
			
			<div style="FLOAT: left"><a class=button href="startMenu">Start Over</a></div>
			<div style="float:right;"><a class="button" href="#asset_menu">Asset Menu</a></div>
			<br class="clear"/>
			
			<table>
			<tr>
				<td class="label">Asset Tag:</td>
				<td class="field">${assetEntity?.assetTag}</td>
			</tr>
			
			<tr>
				<td class="label">U-Position:</td>
				<td class="field" nowrap>
				<g:select name="sourceRackPosition" from="${[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,'Undefined']}" />
				<img src="${createLinkTo(dir:'images',file:'arrow_blue_up.png')}" height="18" />
				<img src="${createLinkTo(dir:'images',file:'arrow_blue_down.png')}" height="18" />
				</td>
			</tr>
			
			<tr>
				<td class="label">U-Size:</td>
				<td class="field">
				<g:select name="usize" from="${[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,'Undefined']}" />
				<img src="${createLinkTo(dir:'images',file:'arrow_blue_up.png')}" height="18" />
				<img src="${createLinkTo(dir:'images',file:'arrow_blue_down.png')}" height="18" />
				</td>
			</tr>
			
			<tr>
				<td class="label">Need Asset Tag?</td>
				<td class="field">
					<input type="radio" name="needAssetTag" id="needAssetTagYes" value="Y"><label for="needAssetTagYes">Yes</label>
					&nbsp;&nbsp;
					<input type="radio" name="needAssetTag" id="needAssetTagNo" value="N" checked><label for="needAssetTagNo">No</label>
				</td>
			</tr>
			<tr>
				<td class="label">Has Amber Lights?</td>
				<td class="field">
					<input type="radio" name="hasAmber" id="hasAmberYes" value="Y"><label for="hasAmberYes">Yes</label>
					&nbsp;&nbsp;
					<input type="radio" name="hasAmber" id="hasAmberNo" value="N" checked><label for="hasAmberNo">No</label>
				</td>
			</tr>
			<tr>
				<td class="label">Stuff Stacked On Top?</td>
				<td class="field">
					<input type="radio" name="stuffOnTop" id="stuffOnTopYes" value="Y"><label for="stuffOnTopYes">Yes</label>
					&nbsp;&nbsp;
					<input type="radio" name="stuffOnTop" id="stuffOnTopNo" value="N" checked><label for="stuffOnTopNo">No</label>
				</td>
			</tr>
			<tr>
				<td class="label">Is Powered OFF?</td>
				<td class="field">
					<input type="radio" name="poweredOff" id="poweredOffYes" value="Y"><label for="poweredOffYes">Yes</label>
					&nbsp;&nbsp;
					<input type="radio" name="poweredOff" id="poweredOffNo" value="N" checked><label for="poweredOffNo">No</label>
				</td>
			</tr>
			</table>
			
			<div style="margin-top:20px;">
			   <div class="thefield" align="center">
			      <a class="button" href="#asset_rear1">Next</a>&nbsp;&nbsp;
			      <a class="button" href="#asset_front1">Back</a>&nbsp;&nbsp;
			      <a class="button" href="#select_asset" onClick="alert('Changes have been saved'); return true;">Save</a>&nbsp;&nbsp;
			      <a class="button" href="#select_asset"  onClick="alert('Changes have been saved and marked completed'); return true;">Completed</a>
			   </div>
			</div>
			
			</div>
		</div>
		<!-- end of Walkthru Asset:Front (2) -->
		
		<div class="gap"></div>
		
		<!-- Walkthru Asset:Rear  -->
		<div class="qvga_border">
		<a name="asset_rear1"></a>
		<div class="title">Walkthru&gt; Rear</div>
		<div class="input_area">
		
		<div style="FLOAT: left"><a class=button href="startMenu">Start Over</a></div>
		<div style="float:right;"><a class="button" href="#asset_menu">Asset Menu</a></div>
		<br class="clear"/>
		
		<table>
		<tr>
			<td class="label">Asset Tag:</td>
			<td class="field">${assetEntity?.assetTag}</td>
		</tr>
		
		<tr>
			<td class="label">PDU Qty/Type:</td>
			<td class="field">
			<g:select name="powerPort" from="${[0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,24,32,40,48,56,64,72,80,88,96]}" />
			&nbsp;&nbsp;
		        <select>
		            <option>C13
		            <option selected>C14
		            <option>5-15R
		            <option>Unknown
		        </select>
			</td>
		</tr>
		
		<tr>
			<td class="label">NIC Qty:</td>
			<td class="field">
			<g:select name="nicPort" from="${[0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,24,32,40,48,56,64,72,80,88,96]}" />
		     	<img src="${createLinkTo(dir:'images',file:'arrow_blue_up.png')}" height="18" />
			 	<img src="${createLinkTo(dir:'images',file:'arrow_blue_down.png')}" height="18" />
			</td>
		</tr>
		
		<tr>
			<td class="label">Fiber Qty/Type:</td>
			<td class="field">
				<g:select name="hbaPort" from="${[0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,24,32,40,48,56,64,72,80,88,96]}" />
		        &nbsp;&nbsp;
			<select>
		            <option>LC
		            <option selected>SC
		            <option>ST
		            <option>MT-
		        </select>
			</td>
		</tr>
		
		</table>
		<table>
		<tr>
		        <td class="label"><label for=attachedKVM>Attached to KVM:</label></td>
		        <td class="field">
						<input type="checkbox" name=attachedKVM id="attachedKVM">
		        </td>
		</tr>
		<tr>
		        <td class="label"><label for="hasILO">Has ILO Mgmt:</label></td>
		        <td class="field">
						<input type="checkbox" name=hasILO id="hasILO">
		        </td>
		</tr>
		<tr>
		        <td class="label">Need cables moved?</td>
		        <td class="field">
		                <input style="border:0px;" type="radio" name="moveCables" id="moveCablesYes" value="Y"><label for="moveCablesYes">Yes</label>
		                &nbsp;&nbsp;
		                <input type="radio" name="moveCables" id="moveCablesNo" value="N" checked><label for="moveCablesNo">No</label>
		        </td>
		</tr>
		</table>      
		
		<div style="margin-top:20px;">
		   <div class="thefield" align="center">
		      <a class="button" href="#asset_front2">Back</a>&nbsp;&nbsp;
		      <a class="button" href="#select_asset" onClick="alert('Changes have been saved'); return true;">Save</a>&nbsp;&nbsp;
		      <a class="button" href="#select_asset"  onClick="alert('Changes have been saved and marked completed'); return true;">Completed</a>
		   </div>
		</div>
		
		</div>
		</div>
		<!-- end of Walkthru Asset:Back -->
		
		<div class="gap"></div>
		</g:form>
	
		<!-- Walkthru Asset:Comments -->
		<div class="qvga_border">
		<a name="comments"></a>
		<div class="title">Walkthru&gt; Issues &amp; Comments</div>
		<div class="input_area">
		
		<div style="FLOAT: left"><a class=button href="startMenu">Start Over</a></div>
		<div style="float:right;"><a class="button" href="#asset_menu">Asset Menu</a></div>
		<br class="clear"/>
		<g:form name="commentForm">	
			<table>
			<tr>
				<input type="hidden" name="assetId" id="assetId" value="${assetEntity.id}" />
				<input type="hidden" name="commentType" id="commentType" value="comment" />
				<input type="hidden" name="instructionType" id="instructionType" value="instruction" />
				<input type="hidden" name="issueType" id="issueType" value="issue" />
				<input type="hidden" name="saveCommentType" id="saveCommentType" value="issue" />
				<td class="label">Asset Tag:</td>
				<td class="field">${assetEntity.assetTag}</td>
			</tr>
			</table>
			<select name="selectCmt" style="width: 200px;" onChange="return commentSelect(this.value);">
				<option value="Select Common Comment">Select Common Comment</option>
				<option value="No Cables">No Cables</option>
				<option value="Screwed into rack">Screwed into rack</option>
				<option value="Take screws and rack clips">Take screws and rack clips</option>
			</select>
			<br />
			
			<textarea name="comments" id="comments" rows="6" cols="8" value=""></textarea>
			</textarea>
			<br />
			<br />
			
			<label>Save As:</label>
			<br />
			<div style="float:center;">
			   	<a class="button" href="#" onclick="var booConfirm = validateCommentSelect();if(booConfirm)${remoteFunction(action:'validateComments', params:'\'id=\' + $(\'#assetId\').val() +\'&comment=\'+$(\'#comments\').val() +\'&commentType=\'+$(\'#commentType\').val()', onComplete:'callUpdateComment( e, \'comment\' )')}">COMMENT</a>&nbsp;&nbsp;
				<a class="button" href="#" onclick="var booConfirm = validateCommentSelect();if(booConfirm)${remoteFunction(action:'validateComments', params:'\'id=\' + $(\'#assetId\').val() +\'&comment=\'+$(\'#comments\').val() +\'&commentType=\'+$(\'#instructionType\').val()', onComplete:'callUpdateComment( e, \'instruction\' )')}">INSTRUCTION</a>&nbsp;&nbsp;
				<a class="button" href="#" onclick="var booConfirm = validateCommentSelect();if(booConfirm)${remoteFunction(action:'validateComments', params:'\'id=\' + $(\'#assetId\').val() +\'&comment=\'+$(\'#comments\').val() +\'&commentType=\'+$(\'#issueType\').val()', onComplete:'callUpdateComment( e, \'issue\' )')}">ISSUE</a>
			</div>
			<br class="clear"/>
			
			<br />
			
			<a class="button" href="#view_comments">View Issues &amp; Comments</a>
		</g:form>
		</div>
		</div>
		<!-- end of Walkthru Asset -Comments-->
		
		<div class="gap"></div>
		<!-- Walkthru Asset:View Comments -->
		<div class="qvga_border">
		<a name="view_comments"></a>
		<div class="title">Walkthru&gt; View Issue&amp;Comments</div>
		<div class="input_area">
		
		<div style="FLOAT: left"><a class=button href="startMenu">Start Over</a></div>
		<div style="float:right;"><a class="button" href="#comments">Issues&amp;Comments</a></div>
		<br class="clear"/>
		<g:form action="issuesandcommentsview" name="commentsViewForm">
		<input type="hidden" name="selectCmt" id="selectCmt" value=""/>
		<table>
		<tbody>
			<tr>
				<g:sortableColumn id="selector" property="commentType" title="Type" params="['selectCmt':cmt,'assetId':assetId]"/>
				<g:sortableColumn  property="comment" title="Text" params="['selectCmt':cmt,'assetId':assetId]"/>
				<th>Rsvld</th>
			</tr>
			<g:each in="${AssetComment.findAll('from AssetComment where assetEntity = '+ assetEntity?.id +' order by commentType')}" status="i" var="assetCommentsInstance">
				<g:if test="${assetCommentsInstance.commentType == 'issue'}">
				<tr><td>Iss</td><td>${assetCommentsInstance.comment}</td><td>
					<g:if test="${assetCommentsInstance.isResolved == 1}">
						<input type="checkbox" checked disabled><br/>
					</g:if>
					<g:else>
						<input type="checkbox" disabled><br/>
					</g:else>
				</td></tr>
				</g:if>
				<g:elseif test="${assetCommentsInstance.commentType == 'comment'}">
					<tr><td>Cmnt</td><td>${assetCommentsInstance.comment}</td><td>&nbsp;</td></tr>
				</g:elseif>
				<g:else>
					<tr><td>Inst</td><td>${assetCommentsInstance.comment}</td><td>&nbsp;</td></tr>
				</g:else>
			</g:each>
		
		</tbody>
		</table>
		</g:form>
		<div class="gap"></div>
</body>
</html>
