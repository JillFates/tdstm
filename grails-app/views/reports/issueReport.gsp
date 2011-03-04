<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="projectHeader" />
<title>Issue Report</title>
<script type="text/javascript">
    
    function populateSelect( id, value ) {    	
		$('#'+id).val( value )
    }
    function checkBoxChange(toId, fromId, value ) {
		if($('#'+toId).is(":checked")) {
     		$("#"+fromId).val("true");
     	}else{
     		$("#"+fromId).val("false");
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
		<tr>
			<td colspan="2"><div class="required"> Fields marked ( * ) are mandatory </div> </td>
		</tr>
	
		<tr class="prop" id="bundleRow" >
			<td valign="top" class="name" style="paddingleft:10px;"><label>&nbsp;&nbsp;&nbsp;&nbsp;<b>Bundles:<span style="color: red;">*</span> </b></label></td>
			<td valign="top" class="value" align="left">
				<select id="moveBundleId" name="moveBundles" onchange="return populateSelect('moveBundle', this.value);">
					<option value="null" selected="selected">Please Select</option>
					<option value="">All Bundles</option>
					<g:each in="${moveBundleInstanceList}" var="moveBundleList">
						<option value="${moveBundleList?.id}">${moveBundleList?.name}</option>
					</g:each>
				</select>
			</td>
		</tr>
		<tr>
		<td valign="top" class="name" nowrap="nowrap"><label>Sort report by: </label></td>
		<td valign="top" class="value" align="left">
				<select id="sortOrder" name="sortOrder" onchange="return populateSelect('reportSort', this.value);">
					<option value="id" selected="selected">Asset Id </option>
					<option value="assetName">Asset Name</option>
					<option value="sourceLocation">Source Location</option>
					<option value="targetLocation">Target Location</option>
				</select>
			</td>
		</tr>
		<tr>
		<td>
		</td>
		<td style="width:auto;"><input id="commentCheck" type="checkbox" name="commentCheck" checked="checked" onclick="checkBoxChange(this.id,'commentInfoId', this.checked)"/>Include comments in report</td>
		</tr>
		<tr>
		<td>
		</td>
		<td style="width:auto;"><input id="resolvedCheck" type="checkbox" name="resolvedCheck" checked="checked" onclick="checkBoxChange(this.id, 'reportResolveInfo', this.checked)"/>Include resolved issues in report</td>
		</tr>
		<tr>
			<td class="buttonR"><g:jasperReport controller="reports" action="issueReport" jasper="issueReport" format="PDF,XLS" name="Generate">
				<input type="hidden" name="moveBundle" id="moveBundle" value="null" />
				<input type="hidden" name="reportSort" id="reportSort" value="id" />
				<input type="hidden" name="reportResolveInfo" id="reportResolveInfo" value="true" />
				<input type="hidden" name="commentCode" id="commentCodeId"/>
				<input type="hidden" name="commentInfo" id="commentInfoId" value="true" />
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
