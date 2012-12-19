<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="projectHeader" />
<title>Cart Tracking</title>

<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.resizable.css')}" />
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.slider.css')}" />
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.tabs.css')}" />

<script type="text/javascript">
	$(document).ready(function() {
		$("#changeTruckDiv").dialog({ autoOpen: false })
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
			timer = setTimeout("pageReload()",timeoutPeriod);
		} else {
			clearTimeout(timer)
		}
	}
	function pageReload(){
		if('${myForm}'){
			document.forms['${myForm}'].submit() ;
		} else {
			window.location = document.URL;
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
		$('#changeTruckDiv').dialog('open');
		$("#reassignAssetDiv").dialog('close');
	}
	/*-----------------------------------------
	* function to get assets to Display on Asset Div
	*-----------------------------------------*/
	function getAssetsOnCart(cart, truck, rowId){
		var cartTable = $("#cartTableHighlightId > tr");
		cartTable.each(function(n, row){
			if(n == rowId) {		
		    	$(row).addClass('selectedRow'); 
		    } else {
		    	$(row).removeClass('selectedRow');
		    }		          		
     	});
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
		$("#reassignAssetDiv").dialog('close');
		$('#changeTruckDiv').dialog('close');
		var assetsOnCart = eval('(' + e.responseText + ')');
		var assetslength = assetsOnCart.length;
		var assetsTbody = $("#assetsOnCartTbodyId")
		var assetsDiv = $("#assetsOnCartDiv")
		var tbody =""
		if(assetslength != 0){
			for( i = 0; i < assetslength ; i++){
				var check = ""
				var cssClass = 'odd'
				if(i % 2 == 0){
					cssClass = 'even'
				}
				var assetOnCart = assetsOnCart[i]
				tbody +="<tr id='assetRow_"+assetOnCart.assetDetails.id+"' onclick='getReassignDetails("+assetOnCart.assetDetails.id+")' class='"+cssClass+"'>"+
						"<td>"+assetOnCart.assetDetails.assetTag+"</td><td>"+assetOnCart.assetDetails.assetName+"</td>"+
						"<td>"+assetOnCart.manufacturer +" "+ assetOnCart.model +"</td>"+
						"<td>"+assetOnCart.currentState+"</td><td>"+assetOnCart.team+"</td>"
						if(assetOnCart.checked){
							check +="<td><input type='checkbox' disabled='disabled' checked='checked'/></td></tr>"
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
		} else {
			$('#remainingAssetsId').css("backgroundColor","#aaefb8");
			$('#allAssetsId').css("backgroundColor","#FFFFFF");
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
		var assetTable = $("#assetsOnCartTbodyId > tr");
		assetTable.each(function(n, row){
		    $(row).removeClass('selectedRow');       		
     	});     
	    $("#assetRow_"+asset).addClass('selectedRow');
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
					 "<tr></td> <strong>Mfg/Model</strong> : "+assetDetails[0].manufacturer+" "+assetDetails[0].model+"</td></tr>"+
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
		$('#changeTruckDiv').dialog('close');
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
				${remoteFunction(action:'reassignAssetOnCart', params:'\'cart=\' + cart +\'&truck=\'+truck +\'&shelf=\'+shelf+\'&assetId=\'+assetId ', onComplete:'pageReload()')}
				return true;
			} else {
				alert("That cart is already On Truck");
				return false;
			}
		} else {
			alert("Asset is not Ready");
		}
	}
	/*-----------------------------------------
	* function to change all assets state to OnTruck
	*-----------------------------------------*/
	function moveToOnTruck(cart, truck, rowId){
		var confirmMove = confirm( "This action will update all assets on cart to On Truck.  Are you sure you want to continue?")
		if( confirmMove ){
			var projectId = $("#projectId").val();
			var moveBundle = $("#moveBundleId").val();
			${remoteFunction(action:'moveToOnTruck', params:'\'cart=\' + cart +\'&truck=\'+truck +\'&projectId=\'+projectId+\'&moveBundle=\'+moveBundle ', onComplete:'removeMoveToTruckLink(e,cart, truck,rowId)')}
			return true;
		} else {
			return false
		}
	}
	function removeMoveToTruckLink( e, cart, truck, rowId){
		var statusObj = e.responseText ;
		var statusList = statusObj.split("~");
		var message = "";
		if(statusList){
			var length = statusList.length;
			for(i = 0; i<length -1; i++){
				var status = statusList[i].split(':');
				if(status[0] != 'true'){
					message += status[1]  ;
					if(i != length-2){
					 	message += ", "
					} 
				}
			}
		} 
		if(message){
			alert(message +" transitions are failed")
		} else if('${cartAction}' == 'allId'){
			$("#completedTd_"+rowId).html('<input type="checkbox" checked="checked" disabled="disabled" />')			
		} else {
			$("#cartRow_"+rowId).hide()
		}
		getAssetsOnCart(cart, truck,rowId)		
	}
</script>
</head>
<body>
<script type="text/javascript">
	  		$('#assetMenu').hide();
	  		$('#bundleMenu').hide();
	  		$('#consoleMenu').show();
	  		$('#reportsMenu').hide();
</script>
<div style="width: 69%;margin-left: 1px; margin-top:1px; border: 1px solid #CCCCCC;" class="body">
<g:form name="cartTrackingForm" action="cartTracking" method="post">
	<div style="width: 100%;">
		<table style="border: 0px;">
			<tr>
				<td valign="top" class="name">
					<input type="hidden" id="projectId" name="projectId" value="${projectId }" />
					<input type="hidden" id="cartActionId" name="cartAction" value="${cartAction}" />
					<input type="hidden" name="myForm" value="cartTrackingForm"/>
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
					<input type="hidden" name="last_refresh_2342131123" value="${new Date()}"/>
					<input type="button" value="Refresh" onclick="pageReload();"/>
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
	<span>Carts</span>&nbsp;&nbsp;&nbsp;<input type="button" id="remainingId" value="Remaining" onclick="getCartDetatls(this.id)" style="background-color: #aaefb8"/><input type="button" id="allId" value="All" onclick="getCartDetatls(this.id)"/>
</div>
<div class="list">
<table cellpadding="0" cellspacing="0" >
	<thead>
		<tr><th>Truck</th>
		<th>Cart</th>
		<th>Total Assets</th>
		<th>Pending Assets</th>
		<th>U's Used</th>
		<th>Completed</th></tr>
	</thead>
	<tbody id="cartTableHighlightId">
		<g:if test="${cartTrackingDetails}">
		<g:each in="${cartTrackingDetails}" status="i" var="cartTrackingDetails" >
			<tr class="${(i % 2) == 0 ? 'even' : 'odd'}" id="cartRow_${i}">
			<td><a href="#" onclick="openChangeTruckDiv('${cartTrackingDetails?.cartDetails?.cart}')">${cartTrackingDetails?.cartDetails?.truck}</a></td>
			<td onclick="getAssetsOnCart('${cartTrackingDetails?.cartDetails?.cart}','${cartTrackingDetails?.cartDetails?.truck}',${i})">${cartTrackingDetails?.cartDetails?.cart}</td>
			<td onclick="getAssetsOnCart('${cartTrackingDetails?.cartDetails?.cart}','${cartTrackingDetails?.cartDetails?.truck}',${i})">${cartTrackingDetails?.cartDetails?.totalAssets}</td>
			<td onclick="getAssetsOnCart('${cartTrackingDetails?.cartDetails?.cart}','${cartTrackingDetails?.cartDetails?.truck}',${i})">${cartTrackingDetails?.pendingAssets}</td>
			<td onclick="getAssetsOnCart('${cartTrackingDetails?.cartDetails?.cart}','${cartTrackingDetails?.cartDetails?.truck}',${i})">${cartTrackingDetails?.cartDetails?.usize ? (Integer)cartTrackingDetails?.cartDetails?.usize : ''}</td>
			<td id="completedTd_${i}">
			<g:if test="${cartTrackingDetails?.completed}">
				<input type="checkbox" checked="checked" disabled="disabled"/>
			</g:if>
			<g:elseif test="${cartTrackingDetails?.pendingAssets == 0 }" >
				<a href="#" onclick="return moveToOnTruck('${cartTrackingDetails?.cartDetails?.cart}','${cartTrackingDetails?.cartDetails?.truck}','${i}')">Move to Truck</a>
			</g:elseif>
			</td>
			</tr>
		</g:each>
		</g:if>
		<g:else>
		<tr><td colspan="6" class="no_records">No records found</td></tr>
		</g:else>
	</tbody>
</table></div>
</g:form>
<br/>
<div id="changeTruckDiv" title="Change Trucks" style="display: none;">
	<g:form name="changeTruckForm">
		<table style="border: 0px;">
			<tr>
				<td id="changeTruckCartTdId">Cart : <input type="hidden" name="cart" id="changeTruckCartId" /></td>
			</tr>
			<tr>
				<td style="vertical-align: middle;">Truck : <select name="truck" id="changeTruckSelectId" >
				<g:each in="${trucks}" var="truck">
					<option value="${truck?.truck}">"${truck?.truck}"</option>
				</g:each>
				</select>
				 </td>
			</tr>
			<tr><td>
				<input type="button" value="Update" onclick="${remoteFunction(action:'changeTruck', params:'\'cart=\' + $(\'#changeTruckCartId\').val() +\'&truck=\'+$(\'#changeTruckSelectId\').val() +\'&projectId=\'+$(\'#projectId\').val() +\'&bundleId=\'+$(\'#moveBundleId\').val()', onComplete:'pageReload()')}"/>
				<input type="button" value="Cancel" onclick="$('#changeTruckDiv').dialog('close');"/> 
			</td></tr>
		</table>
	</g:form>
</div>
<div id="assetsOnCartDiv" style="display: none;">
<div class="cart_style">
	<span>Assets on Cart </span>&nbsp;&nbsp;&nbsp;<input type="button" id="remainingAssetsId" value="Remaining" onclick="getAssetDetatls(this.id)" style="background-color: #aaefb8"/><input type="button" id="allAssetsId" value="All" onclick="getAssetDetatls(this.id)"/>
	<input type="hidden" id="assetsOnCartId" name="assetsOnCart"/>
	<input type="hidden" id="assetsOnTruckId" name="assetsOnTruck"/>
	<input type="hidden" id="assetActionId" name="assetAction"/>
</div>
<div class="list">
<table cellpadding="0" cellspacing="0" >
	<thead>
		<tr><th>Asset Tag</th>
		<th>Name</th>
		<th>Mfg/Model</th>
		<th>Status</th>
		<th>Team</th>
		<th>On Cart</th></tr>
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
		
			<td style="vertical-align: middle;" nowrap="nowrap">Truck : 
			<input type="hidden" name="assetEntity" id="assetEntityId"/>
			<input type="hidden" name="maxState" id="maxStateId"/>
			<input type="hidden" name="onTruck" id="onTruckId"/>
			<select name="truck" id="reassignAssetSelectId" >
			<g:each in="${trucks}" var="truck">
				<option value="${truck?.truck}">${truck?.truck}</option>
			</g:each>
			</select>
			 Cart : <input type="text" name="reassignCart" id="reassignCartId" /> Shelf : <input type="text" name="reassignShelf" id="reassignShelfId"/></td>
		</tr>
		<tr><td>
			<input type="button" value="Update" onclick="return reassignAsset()"/>
			<input type="button" value="Cancel" onclick="$('#reassignAssetDiv').dialog('close');"/> 
		</td></tr>
		</tbody>
	</table>
</div></div>
</div>
<g:javascript>
initialize();
timedRefresh($("#selectTimedId").val())

/*------------------------------------------------------------------
* function to Unhighlight the Asset row when the edit DIV is closed
*-------------------------------------------------------------------*/
$("#reassignAssetDiv").bind('dialogclose', function(){   		
	var assetTable = $("#assetsOnCartTbodyId > tr");
	assetTable.each(function(n, row){
		$(row).removeClass('selectedRow');       		
    });   		
});	
</g:javascript>
<script>
	currentMenuId = "#consoleMenu";
	$("#consoleMenuId a").css('background-color','#003366')
</script>
</body>
</html>
