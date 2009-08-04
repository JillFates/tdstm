<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="projectHeader" />
<title>Rack Elevations</title>
<g:javascript library="prototype" />
<script type="text/javascript">
	function updateRackDetails(e) {
     	var rackDetails = eval('(' + e.responseText + ')')   	
      	var sourceSelectObj = $('#sourceRackId');
      	var targetSelectObj = $('#targetRackId');
      	var sourceRacks = rackDetails[0].sourceRackList;
      	var targetRacks = rackDetails[0].targetRackList;
      	generateOptions(sourceSelectObj,sourceRacks);
      	generateOptions(targetSelectObj,targetRacks);
     }
     function generateOptions(selectObj,racks){
     	if (racks) {
			var length = racks.length
			selectObj.html("<option value='' selected='selected'>All</option>")
			for (var i=0; i < length; i++) {
				var rack = racks[i]
				var locvalue = rack.location ? rack.location : '';
				var rmvalue = rack.room ? rack.room : '';
				var ravalue = rack.rack ? rack.rack : '';
				var value = locvalue +"~"+rmvalue +"~"+ ravalue
				var text =  locvalue +"/"+rmvalue +"/"+ ravalue
				var option = document.createElement("option")
				option.value = value;
				option.innerHTML = text
				selectObj.append(option)
			}
				          	
      	}
     }
     function validateForm(){
     	if($("#bundleId").val() == 'null') {
     		alert("Please select bundle")
     		return false;
     	} else if( !$("#frontView").is(":checked") && !$("#backView").is(":checked") ) {
     		alert("Please select print view")
     		return false;
     	} else {
     		return true;
     	}
     }
    </script>
</head>
<body>
<div class="body">
<h1>Rack Elevations</h1>
<g:if test="${flash.message}">
	<div class="message">${flash.message}</div>
</g:if>
<div class="dialog">
<table>
	<g:form action="rackLayoutReport" method="post" onsubmit="this.target='_blank';return validateForm()">
	<tbody>
		<tr class="prop" id="bundleRow">
			<td valign="top" class="name"><label>Current Move Bundle :</label></td>
			<td valign="top" class="value" colspan="2">
				<select id="bundleId" name="moveBundle" onchange="${remoteFunction(action:'getRackDetails', params:'\'bundleId=\' + this.value', onComplete:'updateRackDetails(e)')}">
					<option value="null" selected="selected">Please Select</option>
					<g:each in="${moveBundleInstanceList}" var="moveBundleList">
						<option value="${moveBundleList?.id}">${moveBundleList?.name}</option>
					</g:each>
				</select>
			</td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name"><label>Location :</label></td>
			<td valign="top">
				<label for="source"><input type="radio" name="location" id="source" onclick="$('#targetRackId').hide();$('#sourceRackId').show();$('#locationNameId').val('source');" /> Source </label> 
			</td>
			<td valign="top">
				<label for="target"><input type="radio" name="location" id="target" checked="checked" onclick="$('#targetRackId').show();$('#sourceRackId').hide();$('#locationNameId').val('target');"/> Target </label> 
			</td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name"><label>Room/Rack :</label></td>
			<td valign="top" class="value" colspan="2">
				<select id="sourceRackId"	multiple="multiple" name="sourcerack" style="width: 100%;display: none;" size="10">
					<option value="null" selected="selected">All</option>
				</select>
				<select id="targetRackId"	multiple="multiple" name="targetrack" style="width: 100%;" size="10">
					<option value="null" selected="selected">All</option>
				</select>
			</td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td colspan="2" >
				<div style="width: 100%; height: 10px; float: left;"><i>Hold ctrl/shift to select multiple</i></div>
			</td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name"><label>Include other bundles :</label></td>
			<td valign="top" colspan="2"><input type="checkbox" name="otherBundle" checked="checked"></td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name"><label>Include bundle names :</label></td>
			<td valign="top" colspan="2"><input type="checkbox" name="bundleName" checked="checked"></td>
		</tr>
		<tr>
			<td valign="top" class="name"><label>Print Views :</label></td>
			<td valign="top"><label for="frontView" ><input type="checkbox" name="frontView" id="frontView" checked="checked" />&nbsp;Front</label></td>
			<td valign="top"><label for="backView" ><input type="checkbox" name="backView" id="backView" checked="checked">&nbsp;Back</label></td>
		</tr>
		<tr>
			<td colspan="3" class="buttonR" style="text-align: center;">
				<input type="hidden" name="locationName" id="locationNameId" value="target"/>
				<input type="submit" value="Generate" />
			</td>
		</tr>
	</tbody>
	</g:form>
</table>
</div>
</div>
<script type="text/javascript">
$('#reportsMenu').show();
$('#assetMenu').hide();
var bundleObj = $("#bundleId")
bundleObj.val('${currentBundle?.CURR_BUNDLE}')
var bundleId = bundleObj.val() 
${remoteFunction(action:'getRackDetails', params:'\'bundleId=\' + bundleId', onComplete:'updateRackDetails(e)')}
</script>
</body>
</html>
