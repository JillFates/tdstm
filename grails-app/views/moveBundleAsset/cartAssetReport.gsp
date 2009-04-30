<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="projectHeader" />
<title>Cart Report</title>
<g:javascript library="prototype" />
<script type="text/javascript">
    
    	function populateBundle(val) {    	
    			
     	var hiddenBundle = document.getElementById('moveBundle')
		
     	hiddenBundle.value = val

     }

     

	
     
    </script>
</head>
<body>

<div class="body">
<h1>Cart Asset List</h1>
<g:if test="${flash.message}">
	<div class="message">${flash.message}</div>
</g:if>
<div class="dialog">
<table>
	<tbody>
		<tr class="prop" id="bundleRow">

			<td valign="top" class="name"><label>Bundles:</label></td>

			<td valign="top" class="value"><select id="moveBundleId"
				name="moveBundle" onchange="return populateBundle(this.value);">

				<option value="null" selected="selected">Please Select</option>

				<option value="">All Bundles</option>
				<g:each in="${moveBundleInstanceList}" var="moveBundleList">
					<option value="${moveBundleList?.id}">${moveBundleList?.name}</option>
				</g:each>

			</select></td>

		</tr>

		
		<tr>

			<td class="buttonR"><g:jasperReport controller="moveBundleAsset"
				action="cartAssetReport" jasper="cartReport" format="PDF"
				name="Generate">
				<input type="hidden" name="reportName" id="reportName" value="cartAsset" />
				<input type="hidden" name="moveBundle" id="moveBundle" value="null" />

			</g:jasperReport></td>

		</tr>


	</tbody>
</table>
</div>

</div>
</body>
</html>
