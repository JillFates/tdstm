<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="projectHeader" />
<title>Logistics Team Worksheets </title>

<script type="text/javascript">
		$(document).ready(function() {
		    currentMenuId = "#reportsMenu";
		    $("#reportsMenuId a").css('background-color','#003366')
		});
    
    	function populateBundle( moveBundleVal ) {  	
	     	jQuery('#moveBundle').val( moveBundleVal );
        }

     	function setSortOrder( sortVal ) {
     		jQuery('#sortType').val( sortVal );
     	}

</script>

</head>
<body>

<div class="body">
<h1>Logistics Team Worksheets </h1>
<g:if test="${flash.message}">
	<div class="message">${flash.message}</div>
</g:if>
<div class="dialog">
<table>
	<tbody>
		<tr>
			<td colspan="2"><div class="required"> Fields marked ( * ) are mandatory </div> </td>
		</tr>
		<tr class="prop" id="bundleRow">

			<td valign="top" class="name"><label><b>Bundles:<span style="color: red;">*</span></b></label></td>

			<td valign="top" class="value"><select id="moveBundleId"
				name="moveBundles" onchange="return populateBundle(this.value);">

				<option value="null" selected="selected">Please Select</option>

				<option value="">All Bundles</option>
				<g:each in="${moveBundleInstanceList}" var="moveBundleList">
					<option value="${moveBundleList?.id}">${moveBundleList?.name}</option>
				</g:each>

			</select></td>

		</tr>
		<tr class="prop">
			<td valign="top" class="name"><label>Sort By:</label></td>
			<td valign="top" class="value"><select id="sortOrderId"
				name="sortOrder" onchange="setSortOrder(this.value);">

				<option value="null" selected="selected">Please Select</option>

				<option value="TEAM_ASSET">Team/Asset Tag</option>
				<option value="ROOM_RACK_USIZE">Room/Rack/UPos</option>
				<option value="TRUCK_CART_SHELF">Truck/Cart/Shelf</option>
				<option value="ASSET_TAG">Asset Tag</option>				

			</select></td>
		</tr>
		
		<tr>

			<td class="buttonR"><g:jasperReport controller="reports"
				action="cartAssetReport" jasper="cartReport" format="PDF"
				name="Generate">
				<input type="hidden" name="reportName" id="reportName" value="cartAsset" />
				<input type="hidden" name="moveBundle" id="moveBundle" value="null" />
				<input type="hidden" name="sortType" id="sortType" value="null" />

			</g:jasperReport></td>

		</tr>
	</tbody>
</table>
</div>
</div>
</body>
</html>
