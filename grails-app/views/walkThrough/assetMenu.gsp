<html>
<head>
<title>Walkthru&gt; Select Asset</title>
<g:javascript library="prototype" />
<g:javascript library="jquery" />
<jq:plugin name="jquery.contextMenu"/>
<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'qvga.css')}" />
<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'walkThrough.css')}" />
<script type="text/javascript">
$(document).ready( function() {
	// Show menu when #myDiv is clicked
	$("#selector").contextMenu('myMenu',{
		bindings: {
			'all': function(t) {
				$('#selectCmt').val('all');
				$('#sort').val('desc');
				${remoteFunction(action:'getComments', params:'\'id=\' + $(\'#assetId\').val() +\'&commentType=\'+$(\'#selectCmt\').val() +\'&sort=\'+$(\'#sort\').val() +\'&orderType=\'+$(\'#orderType\').val()', onComplete:'updateViewComment( e )')};
			},
			'comment': function(t) {
				 $('#selectCmt').val('comment');
				$('#sort').val('desc');
				${remoteFunction(action:'getComments', params:'\'id=\' + $(\'#assetId\').val() +\'&commentType=\'+$(\'#selectCmt\').val() +\'&sort=\'+$(\'#sort\').val() +\'&orderType=\'+$(\'#orderType\').val()', onComplete:'updateViewComment( e )')};
		    },
			'instruction': function(t) {
				$('#selectCmt').val('instruction');
				$('#sort').val('desc');
				${remoteFunction(action:'getComments', params:'\'id=\' + $(\'#assetId\').val() +\'&commentType=\'+$(\'#selectCmt\').val() +\'&sort=\'+$(\'#sort\').val() +\'&orderType=\'+$(\'#orderType\').val()', onComplete:'updateViewComment( e )')};
			},
			'issue': function(t) {
				$('#selectCmt').val('issue');
				$('#sort').val('desc');
				${remoteFunction(action:'getComments', params:'\'id=\' + $(\'#assetId\').val() +\'&commentType=\'+$(\'#selectCmt\').val() +\'&sort=\'+$(\'#sort\').val() +\'&orderType=\'+$(\'#orderType\').val()', onComplete:'updateViewComment( e )')};
			}
		}
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
	if ( data ) {
		${remoteFunction(action:'saveComment', params:'\'id=\' + $(\'#assetId\').val() +\'&comment=\'+escape($(\'#comments\').val()) +\'&commentType=\'+type', onComplete:'callAssetMenu()')};
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
var mustSave = false;
function setMustSave( changed, actual, type, attribute ){
	if( changed != actual ) {
		mustSave = true;
		$("#"+type+"CompleteId").css({'background-color' : 'green', 'color' : '#FFF'});
		$("#"+type+"SaveId").css({'background-color' : 'green', 'color' : '#FFF'});
		$("#generalCommentId").val($("#generalCommentId").val() + attribute+" form "+actual+" to "+ changed +", " )
	}
}
function moveOption( objectId, actual, type, actionType ){
	var optionList = $('#'+objectId+'Id').attr("options");
    var selectedIndex = $('#'+objectId+'Id').attr("selectedIndex");
    if( actionType != 'down'){
	    if ( selectedIndex > 0) {
	    	$('#'+objectId+'Id').attr("selectedIndex", selectedIndex-1);
	    }
    } else {
    	if ( selectedIndex < (optionList.length - 1) ) {
        	$('#'+objectId+'Id').attr("selectedIndex",selectedIndex+1);
        }
    }
    setMustSave( $('#'+objectId+'Id').val(), actual, type ,objectId )
}

function callAssetMenu() {
	$('#selectCmts').val('');
	$('#comments').val('');
	location.href = "#asset_menu";
}

function populateComments() {
	$('#selectCmt').val('all');
	$('#sort').val('desc');
	$('#orderType').val('commentType');
	${remoteFunction(action:'getComments', params:'\'id=\' + $(\'#assetId\').val() +\'&commentType=\'+$(\'#selectCmt\').val() +\'&sort=\'+$(\'#sort\').val() +\'&orderType=\'+$(\'#orderType\').val()', onComplete:'updateViewComment( e )')};
}

function updateViewComment( e ) {
	var assetComments = e.responseText;
	var commentTbody = $('#listCommentsTbodyId');
	$('#selectCmts').val('');
	$('#comments').val('');
	commentTbody.html( assetComments );
}

function sortCommentList(orderType) {
	var sortOrder = $('#sort').val();
	$('#orderType').val(orderType);
	if ( sortOrder == 'desc') {
		$('#sort').val('asc');
	} else {
		$('#sort').val('desc');
	}
	${remoteFunction(action:'getComments', params:'\'id=\' + $(\'#assetId\').val() +\'&commentType=\'+$(\'#selectCmt\').val() +\'&sort=\'+$(\'#sort\').val() +\'&orderType=\'+$(\'#orderType\').val()', onComplete:'updateViewComment( e )')};
}
function getModels(){
	var manufacturer = $("#manufacturerId").val()
	var device = $("#kvmDeviceId").val()
	${remoteFunction(action:'getModels', params:'\'manufacturer=\' + manufacturer +\'&device=\'+device ', onComplete:'updateModels( e )')};
}
function updateModels( e ){
	var models = eval('(' + e.responseText + ')');
	var length = models.length
	if(length > 0){
		$("#modelTdId").html("<select type=\"text\" name=\"model\" id=\"modelId\" />")
		var modelObj = $("#modelId")
		for(i = 0; i < length; i++){
			var model = models[i]
			var option = document.createElement("option")
			option.value = model.name
			option.innerHTML = model.name
			modelObj.append(option)
		} 
	} else {
		$("#modelTdId").html("<input type=\"text\" name=\"model\" id=\"modelId\" >")
	}
}
</script>
</head>
<body>
	<g:form action="saveAndCompleteAudit" method="post" name="auditForm" >
		<div class=qvga_border>
		<a name="asset_menu"></a>
		<div class=title>Walkthru&gt; Asset Menu</div>
		<div class=input_area>
		<input type="hidden" name="id" value="${assetEntity.id}" />
		<input type="hidden" name="submitType" id="submitTypeId">
		<input type="hidden" name="generalComment" id="generalCommentId" value="Asset changed: ">
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
					<a class="button" href="#select_asset" onClick="$('#submitTypeId').val('save');$('form#auditForm').submit();">Save</a>&nbsp;&nbsp;&nbsp;
			        <a class="button" href="#select_asset"  onClick="$('#submitTypeId').val('complete');$('form#auditForm').submit();">Completed</a>
				</div>
			</div>
		</div>
		<div class="gap"></div>
	<!-- Walkthru Asset:Front pg 1-->
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
				<td class="field">
					<input type="text" class="text" name="assetName" value="${assetEntity?.assetName}" size=20 maxsize=50 
							onchange="setMustSave(this.value,'${assetEntity?.assetName}','front1', this.name)">
				</td>
			</tr>
			
			<tr>
				<td class="label">Serial #:</td>
				<td class="field">
					<input type="text" class="text" name="serialNumber" value="${assetEntity?.serialNumber}" size=20 maxsize=50 
							onchange="setMustSave(this.value,'${assetEntity?.serialNumber}','front1', this.name)">
				</td>
			</tr>
			
			<tr>
				<td class="label">Device Type:</td>
				<td class="field">
				<refcode:select domain="kvmDevice" noSelection="['':'']" id="kvmDeviceId" name="kvmDevice" value="${assetEntity?.kvmDevice}" 
						onchange="setMustSave(this.value,'${assetEntity?.kvmDevice}','front1', this.name);getModels()"/> 
			</tr>
			
			<tr>
				<td class="label">Manufacturer:</td>
				<td class="field">
				<g:if test="${Manufacturer.list()}">
				<g:select name="manufacturer" from="${Manufacturer.list()}" value="${assetEntity?.manufacturer}" id="manufacturerId" 
					noSelection="['':'']"  onchange="setMustSave(this.value,'${assetEntity?.manufacturer}','front1', this.name);getModels();"/>
				</g:if>
				<g:else>
				<input type="text" name="manufacturer" id="manufacturerId" >
				</g:else>
				</td>
			</tr>
			
			<tr>
				<td class="label">Model:</td>
				<td class="field" id="modelTdId">
				<g:if test="${Model.list()}">
				<g:select name="model" from="${Model.list()}" id="modelId" noSelection="['':'']" 
					value="${assetEntity?.model}" onchange="setMustSave(this.value,'${assetEntity?.model}','front1', this.name)"/>
				</g:if>
				 <g:else>
				<input type="text" name="model" id="modelId" >
				</g:else>
				</td>
			</tr>
			
			<tr>
				<td class="label">Rails:</td>
				<td class="field">
				 <refcode:select domain="railType" noSelection="['':'']" id="railTypeId" name="railType" value="${assetEntity?.railType}" 
				onchange="setMustSave(this.value,'${assetEntity?.railType}','front1', this.name)"/>
				</td>
			</tr>
			</table>
			
			<div style="margin-top:10px;">
			   <div class="thefield" align="center">
			      <a class="button" href="#asset_front2">Next</a>&nbsp;&nbsp;&nbsp;
			      <a class="button" href="#select_asset" id="front1SaveId" onClick="$('#submitTypeId').val('save');$('form#auditForm').submit();">Save</a>&nbsp;&nbsp;&nbsp;
			      <a class="button" href="#select_asset" id="front1CompleteId" onClick="$('#submitTypeId').val('complete');$('form#auditForm').submit();">Completed</a>
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
				<g:select name="sourceRackPosition" from="${[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,'Undefined']}" 
							id="sourceRackPositionId" value="${assetEntity?.sourceRackPosition}" onchange="setMustSave(this.value,'${assetEntity?.sourceRackPosition}','front2', this.name)"/>
				<img src="${createLinkTo(dir:'images',file:'arrow_blue_up.png')}" height="18" onclick="moveOption('sourceRackPosition','${assetEntity?.sourceRackPosition}','front2','up')"/>
				<img src="${createLinkTo(dir:'images',file:'arrow_blue_down.png')}" height="18" onclick="moveOption('sourceRackPosition','${assetEntity?.sourceRackPosition}','front2','down')"/>
				</td>
			</tr>
			
			<tr>
				<td class="label">U-Size:</td>
				<td class="field">
				<g:select name="usize" from="${[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,'Undefined']}"   
							id="usizeId" value="${assetEntity?.usize}" onchange="setMustSave(this.value,'${assetEntity?.usize}','front2', this.name)"/>
				<img src="${createLinkTo(dir:'images',file:'arrow_blue_up.png')}" height="18" onclick="moveOption('usize','${assetEntity?.usize}','front2','up')"/>
				<img src="${createLinkTo(dir:'images',file:'arrow_blue_down.png')}" height="18" onclick="moveOption('usize','${assetEntity?.usize}','front2','down')"/>
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
					<input type="radio" name="hasAmber" id="hasAmberYes" value="Y" checked><label for="hasAmberYes">Yes</label>
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
			      <a class="button" href="#select_asset" id="front2SaveId"  onClick="$('#submitTypeId').val('save');$('form#auditForm').submit();">Save</a>&nbsp;&nbsp;&nbsp;
			      <a class="button" href="#select_asset"  id="front2CompleteId" onClick="$('#submitTypeId').val('complete');$('form#auditForm').submit();">Completed</a>
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
			<g:select name="powerPort" from="${[0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,24,32,40,48,56,64,72,80,88,96]}" value="${assetEntity?.powerPort}"
				onchange="setMustSave(this.value,'${assetEntity?.powerPort}','rear', this.name)"/>
			&nbsp;&nbsp;
				<refcode:select domain="powerType" noSelection="['':'Unknown']" id="powerType" name="powerType" value="${assetEntity?.powerType}" 
				onchange="setMustSave(this.value,'${assetEntity?.powerType}','rear', this.name)"/>
			</td>
		</tr>
		
		<tr>
			<td class="label">NIC Qty:</td>
			<td class="field">
			<g:select name="nicPort" from="${[0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,24,32,40,48,56,64,72,80,88,96]}" value="${assetEntity?.nicPort}"
						id="nicPortId" onchange="setMustSave(this.value,'${assetEntity?.nicPort}','rear', this.name)"/>
		     	<img src="${createLinkTo(dir:'images',file:'arrow_blue_up.png')}" height="18" onclick="moveOption('nicPort','${assetEntity?.nicPort}','rear','up')"/>
			 	<img src="${createLinkTo(dir:'images',file:'arrow_blue_down.png')}" height="18" onclick="moveOption('nicPort','${assetEntity?.nicPort}','rear','down')"/>
			</td>
		</tr>
		
		<tr>
			<td class="label">Fiber Qty/Type:</td>
			<td class="field">
				<g:select name="hbaPort" from="${[0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,24,32,40,48,56,64,72,80,88,96]}" value="${assetEntity?.hbaPort}"
						onchange="setMustSave(this.value,'${assetEntity?.hbaPort}','rear', this.name)"/>
		        &nbsp;&nbsp;
			<refcode:select domain="fiberCabinet" noSelection="['':'']" id="fiberCabinet" name="fiberCabinet" value="${assetEntity?.fiberCabinet}" 
				onchange="setMustSave(this.value,'${assetEntity?.fiberCabinet}','rear', this.name)"/>
			</td>
		</tr>
		</table>
		<table>
		<tr>
		        <td class="label"><label for=attachedKVM>Attached to KVM:</label></td>
		        <td class="field">
		        <g:if test="${assetEntity?.kvmDevice || assetEntity?.kvmPort }">
						<input type="checkbox" name=attachedKVM id="attachedKVM" checked="checked">
				</g:if>
				<g:else>
						<input type="checkbox" name=attachedKVM id="attachedKVM">
				</g:else>
		        </td>
		</tr>
		<tr>
		        <td class="label"><label for="hasILO">Has ILO Mgmt:</label></td>
		        <td class="field">
		        <g:if test="${assetEntity?.remoteMgmtPort}">
					<input type="checkbox" name=hasILO id="hasILO" checked="checked">
				</g:if>
				<g:else>
					<input type="checkbox" name=hasILO id="hasILO">
				</g:else>
		        </td>
		</tr>
		<tr>
		        <td class="label">Need cables moved?</td>
		        <td class="field">
		                <input type="radio" name="moveCables" id="moveCablesYes" value="Y"><label for="moveCablesYes">Yes</label>
		                &nbsp;&nbsp;
		                <input type="radio" name="moveCables" id="moveCablesNo" value="N" checked><label for="moveCablesNo">No</label>
		        </td>
		</tr>
		</table>      
		
		<div style="margin-top:20px;">
		   <div class="thefield" align="center">
		      <a class="button" href="#asset_front2">Back</a>&nbsp;&nbsp;
		      <a class="button" href="#select_asset" id="rearSaveId" onClick="$('#submitTypeId').val('save');$('form#auditForm').submit();">Save</a>&nbsp;&nbsp;&nbsp;
			  <a class="button" href="#select_asset" id="rearCompleteId" onClick="$('#submitTypeId').val('complete');$('form#auditForm').submit();">Completed</a>
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
				<td class="label">Asset Tag:</td>
				<td class="field">${assetEntity.assetTag}</td>
			</tr>
			</table>
			<select id="selectCmts" name="selectCmts" style="width: 200px;" onChange="return commentSelect(this.value);">
					<option value="">Select Common Comment</option>
				<g:each in="${walkthruComments}" status="i" var="messages">
    				<option value="${messages}">${messages}</option>
				</g:each>
			</select>
			<br/>
			
			<textarea name="comments" id="comments" rows="6" cols="8" value=""></textarea>
			</textarea>
			<br />
			<br />
			
			<label>Save As:</label>
			<br />
			<div style="float:center;">
			   	<a class="button"  href="#comments" onclick="var booConfirm = validateCommentSelect();if(booConfirm)${remoteFunction(action:'validateComments', params:'\'id=\' + $(\'#assetId\').val() +\'&comment=\'+escape($(\'#comments\').val()) +\'&commentType=\'+$(\'#commentType\').val()', onComplete:'callUpdateComment( e, \'comment\' )')}">COMMENT</a>&nbsp;&nbsp;
				<a class="button"  href="#comments" onclick="var booConfirm = validateCommentSelect();if(booConfirm)${remoteFunction(action:'validateComments', params:'\'id=\' + $(\'#assetId\').val() +\'&comment=\'+escape($(\'#comments\').val()) +\'&commentType=\'+$(\'#instructionType\').val()', onComplete:'callUpdateComment( e, \'instruction\' )')}">INSTRUCTION</a>&nbsp;&nbsp;
				<a class="button"  href="#comments" onclick="var booConfirm = validateCommentSelect();if(booConfirm)${remoteFunction(action:'validateComments', params:'\'id=\' + $(\'#assetId\').val() +\'&comment=\'+escape($(\'#comments\').val()) +\'&commentType=\'+$(\'#issueType\').val()', onComplete:'callUpdateComment( e, \'issue\' )')}">ISSUE</a>
			</div>
			<br class="clear"/>
			
			<br />
			
			<a class="button" href="#view_comments" onclick="populateComments();">View Issues &amp; Comments</a>
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
		<input type="hidden" name="selectCmt" id="selectCmt" value="all"/>
		<input type="hidden" name="sort" id="sort" value"desc"/>
		<input type="hidden" name="orderType" id="orderType" value"commentType"/>
		<table>
		
			<tr>
			    <th id="selector" onclick="sortCommentList('commentType');">Type</th>
				<th onclick="sortCommentList('comment');">Text</th>
				<th>Rsvld</th>
			</tr>
		<tbody id="listCommentsTbodyId">
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
		<div id="myMenu" class="contextMenu">
			<ul>
				<li id="all">All</li>
				<li id="comment">Comment</li>
				<li id="instruction">Instruction</li>
				<li id="issue">Issue</li>
			</ul>	
		</div>
	<script type="text/javascript">
	if('${commentCodes.needAssetTag}'){
		$("#needAssetTagYes").attr("checked",true)
	}
	if('${commentCodes.amberLights}'){
		$("#hasAmberYes").attr("checked",true)
	}
	if('${commentCodes.stackedOnTop}'){
		$("#stuffOnTopYes").attr("checked",true)
	}
	if('${commentCodes.poweredOff}'){
		$("#poweredOffYes").attr("checked",true)
	}
	if('${commentCodes.cablesMoved}'){
		$("#moveCablesYes").attr("checked",true)
	}
	$("#manufacturerId").val("${assetEntity?.manufacturer}")
	$("#modelId").val("${assetEntity?.model}") 
	</script>
</body>
</html>
