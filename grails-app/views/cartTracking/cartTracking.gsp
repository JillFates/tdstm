<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="projectHeader" />
<title>Cart Tracking</title>
<g:javascript library="prototype" />
<g:javascript library="jquery" />
<jq:plugin name="ui.core" />
<jq:plugin name="ui.draggable" />
<jq:plugin name="ui.resizable" />
<jq:plugin name="ui.dialog" />
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.core.css')}" />
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.dialog.css')}" />
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.resizable.css')}" />
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.slider.css')}" />
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.tabs.css')}" />
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.theme.css')}" />
<script>
	$(document).ready(function() {
		$("#chnageTruckDiv").dialog({ autoOpen: false })
	})
</script>
<script type="text/javascript">
	/*-----------------------------------------
	* Function to initialize default params
	*-----------------------------------------*/
	function initialize(){
		var bundleId = ${moveBundleInstance.id}; 
		$("#moveBundleId").val(bundleId);
		var time = '${timeToRefresh}';
			if(time != "" ){
				$("#selectTimedId").val( time ) ;
			} else if(time == "" ){
				$("selectTimedId").val( 120000 );	
			}
		var tab = '${cartAction}' ;
        if(tab == "allId"){
	        $('#remainingId').css("backgroundColor","#FFFFFF");
	        $('#allId').css("backgroundColor","#aaefb8");
        }
	}
	var timer
	/*-----------------------------------------
	* Function to set Time refresh
	*-----------------------------------------*/
   	function timedRefresh(timeoutPeriod) {
   		if(timeoutPeriod != 'never'){
			clearTimeout(timer);
			timer = setTimeout("location.reload(true);",timeoutPeriod);
		} else {
			clearTimeout(timer)
		}
	}
	/*-----------------------------------------
	* Function to initialize time refresh when user selects the time refresh 
	*-----------------------------------------*/
	function setRefreshTime(e) {
		var timeRefresh = eval("(" + e.responseText + ")")
		if(timeRefresh){
			timedRefresh(timeRefresh[0].refreshTime.CART_TRACKING_REFRESH)
		}
	}
	/*-----------------------------------------
	* function to submit form when user click on carts button
	*-----------------------------------------*/
	function getCartDetatls(id){
		$("#cartActionId").val(id);
		$("#cartTrackingForm").submit();
	}
	/*-----------------------------------------
	* function to pupup the Change Truck dialog
	*-----------------------------------------*/
	function openChangeTruckDiv( cart ) {
		$("#changeTruckCartTdId").html("Cart : "+cart);
		$("#changeTruckCartId").val(cart);
		$('#chnageTruckDiv').dialog('open');
	}
</script>
</head>
<body>
<div style="width: 69%;margin-left: 1px; margin-top:1px; border: 1px solid #CCCCCC;" class="body">
<g:form name="cartTrackingForm" action="cartTracking" method="post">
	<div style="width: 100%;">
		<table style="border: 0px;">
			<tr>
				<td valign="top" class="name">
					<input type="hidden" id="projectId" name="projectId" value="${projectId }" />
					<input type="hidden" id="cartActionId" name="cartAction" value="${cartAction}" />
					<label for="moveBundle">Move Bundle:</label>&nbsp;
					<select id="moveBundleId" name="moveBundle"	onchange="document.cartTrackingForm.submit()">
						<g:each status="i" in="${moveBundleInstanceList}" var="moveBundleInstance">
						<option value="${moveBundleInstance?.id}">${moveBundleInstance?.name}</option>
						</g:each>
					</select>
				</td>
				<td>
				<h1 align="left">Cart Tracking</h1>
				</td>
				<td style="text-align: right;">
					<input type="hidden" name="last_refresh" value="${new Date()}">
					<input type="button" value="Refresh" onclick="location.reload(true);">
					<select id="selectTimedId" onchange="${remoteFunction( action:'setTimePreference', params:'\'timer=\'+ this.value ' , onComplete:'setRefreshTime(e)') }">
						<option value="60000">1 min</option>
						<option value="120000">2 min</option>
						<option value="180000">3 min</option>
						<option value="240000">4 min</option>
						<option value="300000">5 min</option>
						<option value="never">Never</option>
					</select>
				</td>
			</tr>
		</table>
</div>
<div style="width: 20%;">
	<table style="border: 0;"><tr><td>
	<span style="font-size: 16px;font-weight: bold;">Carts</span>&nbsp;&nbsp;&nbsp;
	<input type="button" id="remainingId" value="Remaining" onclick="getCartDetatls(this.id)" style="background-color: #aaefb8"><input type="button" id="allId" value="All" onclick="getCartDetatls(this.id)">
	</td></tr></table>
</div>
<div class="list">
<table cellpadding="0" cellspacing="0" >
	<thead>
		<th>Truck</th>
		<th>Cart</th>
		<th>Total Assets</th>
		<th>Pending Assets</th>
		<th>U's Used</th>
		<th>Completed</th>
	</thead>
	<tbody>
		<g:each in="${cartTrackingDetails}" var="cartTrackingDetails" >
			<tr>
			<td><a href="#" onclick="openChangeTruckDiv('${cartTrackingDetails?.cartDetails?.cart}')">${cartTrackingDetails?.cartDetails?.truck}</a></td>
			<td>${cartTrackingDetails?.cartDetails?.cart}</td>
			<td>${cartTrackingDetails?.cartDetails?.totalAssets}</td>
			<td>${cartTrackingDetails?.pendingAssets}</td>
			<td>${cartTrackingDetails?.cartDetails?.usize}</td>
			<td>
			<g:if test="${cartTrackingDetails?.completed}">
				<input type="checkbox" checked="checked" disabled="disabled">
			</g:if>
			<g:elseif test="${cartTrackingDetails?.pendingAssets == 0 }" >
				<a href="#" >Move to Truck</a>
			</g:elseif>
			</td>
			</tr>
		</g:each>
	</tbody>
</table>
</g:form>
</div>
<div id="chnageTruckDiv" title="Change Trucks" style="display: none;">
	<table>
		<tr>
			<input type="hidden" name="cart" id="changeTruckCartId">
			<td id="changeTruckCartTdId">Cart : </td>
		</tr>
		<tr>
			<td style="vertical-align: middle;">Truck : <select name="truck" id="changeTruckSelectId" >
			<g:each in="${trucks}" var="truck">
				<option value="${truck?.truck}">${truck?.truck}</option>
			</g:each>
			 </td>
		</tr>
		<tr><td>
			<input type="button" value="Update" onclick="${remoteFunction(action:'changeTruck', params:'\'cart=\' + $(\'#changeTruckCartId\').val() +\'&truck=\'+$(\'#changeTruckSelectId\').val() +\'&projectId=\'+$(\'#projectId\').val() +\'&bundleId=\'+$(\'#moveBundleId\').val()', onComplete:'location.reload(true)')}">
			<input type="button" value="Cancel" onclick="$('#chnageTruckDiv').dialog('close');"> 
		</td></tr>
	</table>
</div>
<g:javascript>
initialize();
timedRefresh($("#selectTimedId").val())
</g:javascript>
</body>

</html>
