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
		$("#reassignAssetDiv").dialog({ autoOpen: false })
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
		$("#reassignAssetDiv").dialog('close');
	}
	/*-----------------------------------------
	* function to get assets to Display on Asset Div
	*-----------------------------------------*/
	function getAssetsOnCart(cart, truck){
		$("#assetsOnCartId").val(cart);
		$("#assetsOnTruckId").val(truck);
		var projectId = $("#projectId").val();
		var moveBundle = $("#moveBundleId").val();
		var assetAction = $("#assetActionId").val()
		${remoteFunction(action:'getAssetsOnCart', params:'\'cart=\' + cart +\'&truck=\'+truck +\'&projectId=\'+projectId+\'&moveBundle=\'+moveBundle +\'&assetAction=\'+assetAction', onComplete:'showAssetDiv(e)')}
		timedRefresh('never')
	}
	/*-----------------------------------------
	* function to get assets to Display on Asset Div
	*-----------------------------------------*/
	function getAssetDetatls(id){
		$("#assetActionId").val(id);
		var cart = $("#assetsOnCartId").val();
		var truck = $("#assetsOnTruckId").val();
		var projectId = $("#projectId").val();
		var moveBundle = $("#moveBundleId").val();
		${remoteFunction(action:'getAssetsOnCart', params:'\'cart=\' + cart +\'&truck=\'+truck +\'&projectId=\'+projectId +\'&moveBundle=\'+moveBundle +\'&assetAction=\'+id', onComplete:'showAssetDiv(e)')}
		timedRefresh('never')
	}
	/*-----------------------------------------
	* function to show assets div
	*-----------------------------------------*/
	function showAssetDiv( e ) {
		var assetsOnCart = eval('(' + e.responseText + ')');
		var assetslength = assetsOnCart.length;
		var assetsTbody = $("#assetsOnCartTbodyId")
		var assetsDiv = $("#assetsOnCartDiv")
		var tbody =""
		if(assetslength != 0){
			for( i = 0; i < assetslength ; i++){
				var check = ""
				var assetOnCart = assetsOnCart[i]
				tbody +="<tr onclick='getReassignDetails("+assetOnCart.assetDetails.id+")'><td>"+assetOnCart.assetDetails.assetTag+"</td><td>"+assetOnCart.assetDetails.assetName+"</td>"+
						"<td>"+assetOnCart.assetDetails.manufacturer +" "+ assetOnCart.assetDetails.model +"</td>"+
						"<td>"+assetOnCart.currentState+"</td><td>"+assetOnCart.team+"</td>"
						if(assetOnCart.checked){
							check +="<td><input type='checkbox' disabled checked='checked'></td></tr>"
						} else {
							check += "<td>&nbsp;</td></tr>"
						}
				tbody += check
						
			}
			var assetAction = assetsOnCart[0].assetAction
			if(assetAction){
				$("#assetActionId").val(assetAction)
				if(assetAction == "allAssetsId"){
			        $('#remainingAssetsId').css("backgroundColor","#FFFFFF");
			        $('#allAssetsId').css("backgroundColor","#aaefb8");
		        } else {
		        	$('#remainingAssetsId').css("backgroundColor","#aaefb8");
			        $('#allAssetsId').css("backgroundColor","#FFFFFF");
		        }
			}
		}
		if(tbody == ""){
			tbody = "<tr><td colspan='6' class='no_records'>No records found</td></tr>"
		}
		assetsTbody.html(tbody)
		assetsDiv.css("display","block")
	}
	/*-----------------------------------------
	* function to get asset details for Reassign div
	*-----------------------------------------*/
	function getReassignDetails(asset){
		$("#assetEntityId").val(asset);
		${remoteFunction(action:'getAssetDetails', params:'\'assetId=\' + asset ', onComplete:'showReassignAssetDiv(e)')}
	}
	/*-----------------------------------------
	* function to show Reassign div
	*-----------------------------------------*/
	function showReassignAssetDiv( e ){
		var assetDetails = eval('(' + e.responseText + ')');
		var tbody = ""
		if(assetDetails[0]){
			tbody += "<tr></td> <strong>Asset Tag </strong> :  "+assetDetails[0].assetEntity.assetTag+"</td></tr>"+
					 "<tr></td> <strong>Name </strong>: "+assetDetails[0].assetEntity.assetName+"</td></tr>"+
					 "<tr></td> <strong>Mfg/Model</strong> : "+assetDetails[0].assetEntity.manufacturer+" "+assetDetails[0].assetEntity.model+"</td></tr>"+
					 "<tr></td> <strong>Team</strong> : "+assetDetails[0].team+"</td></tr>"
			$("#reassignCartId").val(assetDetails[0].assetEntity.cart);
			$("#reassignShelfId").val(assetDetails[0].assetEntity.shelf);
			$("#maxStateId").val(assetDetails[0].state);
			$("#onTruckId").val(assetDetails[0].onTruck);
		}
		$("#reassignAssetTbodyId").css({'font-size':'11px','padding':'5px 6px'});
		$("#reassignAssetTbodyId").html(tbody);
		$("#reassignAssetDiv").dialog('option', 'width', 550)
		$("#reassignAssetDiv").dialog('open');
		$('#chnageTruckDiv').dialog('close');
	}
	/*-----------------------------------------
	* function to submit the form when user click on update button
	*-----------------------------------------*/
	function reassignAsset(){
		var maxstate = $("#maxStateId").val()
		var onTruck = $("#onTruckId").val()
		if(maxstate){
			if(parseInt(maxstate) < parseInt(onTruck)){
				var cart = $("#reassignCartId").val();
				var shelf = $("#reassignShelfId").val();
				var truck = $("#reassignAssetSelectId").val()
				var assetId = $("#assetEntityId").val()
				${remoteFunction(action:'reassignAssetOnCart', params:'\'cart=\' + cart +\'&truck=\'+truck +\'&shelf=\'+shelf+\'&assetId=\'+assetId ', onComplete:'location.reload(true)')}
				return true;
			} else {
				alert("That cart is already On Truck");
				return false;
			}
		} else {
			alert("Asset is not Ready");
		}
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
					<input type="hidden" name="last_refresh_2342131123" value="${new Date()}">
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
<div class="cart_style">
	<span>Carts</span>&nbsp;&nbsp;&nbsp;<input type="button" id="remainingId" value="Remaining" onclick="getCartDetatls(this.id)" style="background-color: #aaefb8"><input type="button" id="allId" value="All" onclick="getCartDetatls(this.id)">
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
		<g:if test="${cartTrackingDetails}">
		<g:each in="${cartTrackingDetails}" var="cartTrackingDetails" >
			<tr>
			<td><a href="#" onclick="openChangeTruckDiv('${cartTrackingDetails?.cartDetails?.cart}')">${cartTrackingDetails?.cartDetails?.truck}</a></td>
			<td onclick="getAssetsOnCart('${cartTrackingDetails?.cartDetails?.cart}','${cartTrackingDetails?.cartDetails?.truck}')">${cartTrackingDetails?.cartDetails?.cart}</td>
			<td onclick="getAssetsOnCart('${cartTrackingDetails?.cartDetails?.cart}','${cartTrackingDetails?.cartDetails?.truck}')">${cartTrackingDetails?.cartDetails?.totalAssets}</td>
			<td onclick="getAssetsOnCart('${cartTrackingDetails?.cartDetails?.cart}','${cartTrackingDetails?.cartDetails?.truck}')">${cartTrackingDetails?.pendingAssets}</td>
			<td onclick="getAssetsOnCart('${cartTrackingDetails?.cartDetails?.cart}','${cartTrackingDetails?.cartDetails?.truck}')">${(Integer)cartTrackingDetails?.cartDetails?.usize}</td>
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
		</g:if>
		<g:else>
		<tr><td colspan="6" class="no_records">No records found</td></tr>
		</g:else>
	</tbody>
</table>
</g:form>
</div>
<div id="chnageTruckDiv" title="Change Trucks" style="display: none;">
	<table style="border: 0px;">
		<tr>
			<input type="hidden" name="cart" id="changeTruckCartId">
			<td id="changeTruckCartTdId">Cart : </td>
		</tr>
		<tr>
			<td style="vertical-align: middle;">Truck : <select name="truck" id="changeTruckSelectId" >
			<g:each in="${trucks}" var="truck">
				<option value="${truck?.truck}">${truck?.truck}</option>
			</g:each>
			</select>
			 </td>
		</tr>
		<tr><td>
			<input type="button" value="Update" onclick="${remoteFunction(action:'changeTruck', params:'\'cart=\' + $(\'#changeTruckCartId\').val() +\'&truck=\'+$(\'#changeTruckSelectId\').val() +\'&projectId=\'+$(\'#projectId\').val() +\'&bundleId=\'+$(\'#moveBundleId\').val()', onComplete:'location.reload(true)')}">
			<input type="button" value="Cancel" onclick="$('#chnageTruckDiv').dialog('close');"> 
		</td></tr>
	</table>
</div>
<div id="assetsOnCartDiv" style="display: none;">
<div class="cart_style">
	<span>Assets on Cart </span>&nbsp;&nbsp;&nbsp;<input type="button" id="remainingAssetsId" value="Remaining" onclick="getAssetDetatls(this.id)" style="background-color: #aaefb8"><input type="button" id="allAssetsId" value="All" onclick="getAssetDetatls(this.id)">
	<input type="hidden" id="assetsOnCartId" name="assetsOnCart">
	<input type="hidden" id="assetsOnTruckId" name="assetsOnTruck">
	<input type="hidden" id="assetActionId" name="assetAction">
</div>
<div class="list">
<table cellpadding="0" cellspacing="0" >
	<thead>
		<th>Asset Tag</th>
		<th>Name</th>
		<th>Mfg/Model</th>
		<th>Status</th>
		<th>Team(S/T)</th>
		<th>On Cart</th>
	</thead>
	<tbody id="assetsOnCartTbodyId"> 
	</tbody>
</table>
</div>
<div id="reassignAssetDiv" title="Reassign Asset" style="display: none;">
	<table style="border: 0px;">
		<tbody id="reassignAssetTbodyId">
		</tbody>
		<tbody>
		<tr>
		<input type="hidden" name="assetEntity" id="assetEntityId">
		<input type="hidden" name="maxState" id="maxStateId">
		<input type="hidden" name="onTruck" id="onTruckId">
			<td style="vertical-align: middle;" nowrap="nowrap">Truck : <select name="truck" id="reassignAssetSelectId" >
			<g:each in="${trucks}" var="truck">
				<option value="${truck?.truck}">${truck?.truck}</option>
			</g:each>
			</select>
			 Cart : <input type="text" name="reassignCart" id="reassignCartId"> Shelf : <input type="text" name="reassignShelf" id="reassignShelfId"></td>
		</tr>
		<tr><td>
			<input type="button" value="Update" onclick="return reassignAsset()">
			<input type="button" value="Cancel" onclick="$('#reassignAssetDiv').dialog('close');"> 
		</td></tr>
		</tbody>
	</table>
</div>
<g:javascript>
initialize();
timedRefresh($("#selectTimedId").val())
</g:javascript>
</body>

</html>
