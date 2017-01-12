<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="topNav" />
<title>Cabling Report</title>

<script type="text/javascript">
		$(document).ready(function() {
		    currentMenuId = "#reportsMenu";
			$('.menu-reports-cabling-data').addClass('active');
			$('.menu-parent-reports').addClass('active');
		});
    
    	function populateBundle( moveBundleVal ) {  	
	     	jQuery('#moveBundle').val( moveBundleVal );
        }

     	function setCableType( cableVal ) {
     		jQuery('#cableType').val( cableVal );
     	}
</script>

</head>
<body>
<tds:subHeader title="Structured Cabling Report" crumbs="['Reports', 'Cabling Data']"/><br />
<div class="body">
<g:if test="${flash.message}">
	<div class="message">${flash.message}</div>
</g:if>
<div class="dialog">
<table class="reports-discovery-table">
	<tbody>
		<tr>
			<td colspan="2"><div class="required"> Fields marked ( * ) are mandatory </div> </td>
		</tr>
		<tr class="prop" id="bundleRow">

			<td valign="top" class="name"><label><b>Bundles:<span style="color: red;">*</span></b></label></td>

			<td valign="top" class="value"><select id="moveBundleId"
				name="moveBundles" onchange="return populateBundle(this.value);">

				<option value="" selected="selected">All Bundles</option>
				<g:each in="${moveBundleInstanceList}" var="moveBundleList">
					<option value="${moveBundleList?.id}">${moveBundleList?.name}</option>
				</g:each>

			</select></td>

		</tr>
		<tr class="prop">
			<td valign="top" class="name"><label>Cable Type</label></td>
			<td valign="top" class="value">
				<select id="cableTypeId"
				name="cableType" onchange="setCableType(this.value);">

				<option value="" selected="selected">All Cables</option>

				<option value="ether">Ether</option>
				<option value="serial">Serial</option>
				<option value="power">Power</option>
				<option value="fiber">Fiber</option>
				<option value="SCSI">SCSI</option>
				<option value="USB">USB</option>
				<option value="KVM">KVM</option>
				<option value="others">Others</option>

			</select></td>
		</tr>
		
		<tr>

			<td class="buttonR"><g:jasperReport controller="reports"
				action="cablingDataReport" jasper="CablingDataReport" format="XLS"
				name="Generate">
				<input type="hidden" name="reportName" id="reportName" value="cablingData" />
				<input type="hidden" name="moveBundle" id="moveBundle" value="" />
				<input type="hidden" name="cableType" id="cableType" value="" />

			</g:jasperReport></td>

		</tr>
	</tbody>
</table>
</div>
</div>
</body>
</html>
