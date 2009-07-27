<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="projectHeader" />
<title>Issue Report</title>
<g:javascript library="prototype" />
<script type="text/javascript">
    
    	function populateBundle(val) {    	
	     	var hiddenBundle = document.getElementById('moveBundle')
	     	hiddenBundle.value = val

     }
     function populateSort(val){
     	var hiddenBundle = document.getElementById('reportSort')
     	hiddenBundle.value = val
     }
     function resolvedCheckChange(val) {
     	var resolveCheck = document.getElementById('resolvedCheck')
     	var resolveInfo = document.getElementById('reportResolveInfo')
     	if(resolveCheck.checked == true) {
     		resolveInfo.value = "true";
     	}else{
     		resolveInfo.value = "false";
     	}
     }
    
     
    </script>
</head>
<body>

<div class="body">
<h1>Issue Report</h1>
<g:if test="${flash.message}">
	<div class="message">${flash.message}</div>
</g:if>
<div class="dialog">
<table>
	<tbody>
		<tr class="prop" id="bundleRow" >
			<td valign="top" class="name" style="paddingleft:10px;"><label>&nbsp;&nbsp;&nbsp;&nbsp;Bundles:</label></td>
			<td valign="top" class="value" align="left">
				<select id="moveBundleId" name="moveBundles" onchange="return populateBundle(this.value);">
					<option value="null" selected="selected">Please Select</option>
					<option value="">All Bundles</option>
					<g:each in="${moveBundleInstanceList}" var="moveBundleList">
						<option value="${moveBundleList?.id}">${moveBundleList?.name}</option>
					</g:each>
				</select>
			</td>
		</tr>
		<tr>
		<td valign="top" class="name" ><label>Sort report by: </label></td>
		<td valign="top" class="value" align="left">
				<select id="sortOrder" name="sortOrder" onchange="return populateSort(this.value);">
					<option value="id" selected="selected">Asset Id </option>
					<option value="assetName">Asset Name</option>
					<option value="sourceLocation">Source Location</option>
					<option value="targetLocation">Target Location</option>
				</select>
			</td>
		</tr>
		<tr>
		<td></td>
		<td style="width:auto;"><input id="resolvedCheck" type="checkbox" name="resolvedCheck" checked="checked" onclick="resolvedCheckChange(this.checked)"/>Include resolved issues in report</td>
		</tr>
		<tr>
			<td class="buttonR"><g:jasperReport controller="moveBundleAsset" action="issueReport" jasper="issueReport" format="PDF" name="Generate">
				<input type="hidden" name="moveBundle" id="moveBundle" value="null" />
				<input type="hidden" name="reportSort" id="reportSort" value="id" />
				<input type="hidden" name="reportResolveInfo" id="reportResolveInfo" value="true" />
				</g:jasperReport>
			</td>
		</tr>
	</tbody>
</table>
</div>
</div>
<script type="text/javascript">
$('#reportsMenu').show();
$('#assetMenu').hide();
</script>
</body>
</html>
