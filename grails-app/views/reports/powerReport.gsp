<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="projectHeader" />
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'rackLayout.css')}" />
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'jquery.autocomplete.css')}" />
<g:javascript src="asset.tranman.js" />
<title>Power Report</title>
<script type="text/javascript">
	$(document).ready(function() {
	    currentMenuId = "#reportsMenu";
	    $("#reportsMenuId a").css('background-color','#003366')
	});
	function updateRackDetails(e) {
     	var rackDetails = eval('(' + e.responseText + ')')   	
      	var sourceSelectObj = $('#sourceRackId');
      	var targetSelectObj = $('#targetRackId');
      	var sourceRacks = rackDetails[0].sourceRackList;
      	var targetRacks = rackDetails[0].targetRackList;
      	generateOptions(sourceSelectObj,sourceRacks,'none');
      	generateOptions(targetSelectObj,targetRacks,'all');
      	/* Start with generated default */
      	$('input[value=Generate]').click();
     }
     function generateOptions(selectObj,racks,sel){
     	if (racks) {
			var length = racks.length
			if(sel == 'none')
				selectObj.html("<option value=''>All</option><option value='none' selected='selected'>None</option>");
			else
				selectObj.html("<option value='' selected='selected'>All</option><option value='none'>None</option>");
			
			racks.map(function(e) {
				var locvalue = e.location ? e.location : 'blank';
				var rmvalue = e.room ? e.room : 'blank';
				var ravalue = e.tag ? e.tag : 'blank';
				return({'value':e.id, 'innerHTML':locvalue +"/"+rmvalue +"/"+ ravalue});
			}).sort(function(a, b) {
				var compA = a.innerHTML;
				var compB = b.innerHTML;
				return (compA < compB) ? -1 : (compA > compB) ? 1 : 0;
			}).each(function(e) {
				var option = document.createElement("option");
				option.value = e.value;
				option.innerHTML = e.innerHTML;
				selectObj.append(option);
			});
      	}
     }
     function submitForm(form){
		if($("form input:radio:checked").val() == "web"){
	    	$('#rackLayout').html('Loading...');
			jQuery.ajax({
				url: $(form).attr('action'),
				data: $(form).serialize(),
				type:'POST',
				success: function(data) {
					$('#rackLayout').html(data);
				}
			});
	 		return false;
		} else {
			return true
		}
     }
	$(document).ready(function() {
	    $("#editDialog").dialog({ autoOpen: false })
	    $("#cablingDialogId").dialog({ autoOpen: false })
	})
	function openAssetEditDialig( id ){
		$("#editFormId").val(id)
		${remoteFunction(controller:"assetEntity", action:"editShow", params:'\'id=\' + id ', onComplete:"showAssetDialog( e , 'edit')")}
	}
	function showEditAsset(e) {
		var assetEntityAttributes = eval('(' + e.responseText + ')')
		if (assetEntityAttributes != "") {
			$("#editDialog").dialog("close")
			$("#cablingDialogId").dialog("close")
			submitForm($('form [name=rackLayoutCreate]'));
		} else {
			alert("Asset is not updated, Please check the required fields")
		}
	}
	// Script to get the combined rack list
	function getRackDetails( objId ){
		var bundles = new Array()
		$("#"+objId+" option:selected").each(function () {
			bundles.push($(this).val())
       	});
       	
		${remoteFunction(controller:'rackLayouts', action:'getRackDetails', params:'\'bundles=\' +bundles', onComplete:'updateRackDetails(e)')}
	}
    </script>
</head>
<body>
<div class="body" style="width:98%;">
<g:if test="${flash.message}">
	<div class="message">${flash.message}</div>
</g:if>
<div class="dialog">
<g:form action="powerReportDetails" name="rackLayoutCreate" method="post" onsubmit="return submitForm(this)" style="border: 1px solid black; width: 100%">
<table style="width:auto; border: none">
	<tbody>
		<tr>
			<td>
				<h1 style="margin: 0px;">Rack Selection</h1>
				<label><b>Bundle</b></label><br />
				<select id="bundleId" name="moveBundle" multiple="multiple" size="3" onchange="getRackDetails(this.id)" style="width:150px">
					<option value="all" selected="selected">All</option>
					<g:each in="${moveBundleInstanceList}" var="moveBundleList">
						<option value="${moveBundleList?.id}">${moveBundleList?.name}</option>
					</g:each>
				</select>
			</td>
			
			<td>
				<label><b>Source</b></label><br />
				<select id="sourceRackId" multiple="multiple" name="sourcerack" style="width:200px" size="4">
					<option value="null" selected="selected">All</option>
				</select>
			</td>

			<td>
				<div style="width:250px">
					<label><b>Target</b></label><br />
					<select id="targetRackId" multiple="multiple" name="targetrack" style="width:200px" size="4">
						<option value="null" selected="selected">All</option>
					</select>
				</div>
			</td>
			
			<td>
				<div style="width:150px">
					<label><strong>Output:</strong></label>&nbsp;<br/>
					<label for="web" ><input type="radio" name="output" id="web" checked="checked" value="web"/>&nbsp;Web</label><br/>
					<label for="excel" ><input type="radio" name="output" id="excel" value="excel"/>&nbsp;Excel</label><br />
					<label for="pdf" ><input type="radio" name="output" id="pdf" value="pdf"/>&nbsp;PDF</label><br />
				</div>
			</td>
			
			<td class="buttonR">
				<table style="border: 0"><tr><td nowrap="nowrap">
				Display in: <g:select id="powerType" name='powerType' value="${session.getAttribute('CURR_POWER_TYPE')?.CURR_POWER_TYPE}" style="float:right; margin-right:510px;" from="${['Watts','Amps']}"> </g:select>
				</td></tr><tr></tr><td>
				<input type="submit" class="submit" value="Generate" /></td></tr> </table>
				</div>
			</td>
		</tr>
	</tbody>
</table>
	</g:form>
</div>

<div id="rackLayout" style="width:100%; overflow-x:auto; border: 1px solid black">

</div>

<script type="text/javascript">

	var click = 1
	$(document).ready(function() {
		var bundleObj = $("#bundleId");
		var isCurrentBundle = '${isCurrentBundle}'
		var bundleId = 'all';
		if(isCurrentBundle == "true"){
			bundleObj.val('${currentBundle}');
			bundleId = bundleObj.val();
		}
		${remoteFunction(controller:'rackLayouts',action:'getRackDetails', params:'\'bundles=\' + bundleId', onComplete:'updateRackDetails(e)')};
	});
	
</script>
</body>
</html>
