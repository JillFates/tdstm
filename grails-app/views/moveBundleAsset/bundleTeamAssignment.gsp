
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="projectHeader" />
    <title>Bundle Team Assignment</title>
	
    <script type="text/javascript">
    
       <%--   Delete Rows from a Table --%>
    	 function deleteRow(table) {  
             try {  
             	var table = table;  
            	var rowCount = table.rows.length; 
	            for(var i=1; i<rowCount; i++) {  
    	             var row = table.rows[i];  
        	         table.deleteRow(i);  
                    	rowCount--;  
                     	i--;  
                 	  
	             }
             }catch(e) {  
                 alert(e);  
             }  
         }
         function  makeSelect(field,asset ) {
        	var spanText = jQuery('#span_'+asset);
        	spanText.attr("style", "display:none"); 
        	var TeamSelect
        	try{
        		TeamSelect = document.getElementsByName('assetTeamAssign_'+asset)
        		TeamSelect[0].style.display = 'block'
	   		}catch(ex){
	   			var obj=document.getElementById('row_'+asset)
        		 TeamSelect = obj.childNodes
        		 TeamSelect[7].childNodes[1].style.display = 'block'
	   		}
    	}
         function IsNumeric(sText)
		{
   			var ValidChars = "0123456789";
   			var IsNumber=true;
   			var Char;
			for (i = 0; i < sText.length && IsNumber == true; i++) 
      		{ 
      			Char = sText.charAt(i); 
      			if (ValidChars.indexOf(Char) == -1) 
         		{
         			IsNumber = false;
         		}
      		}
   			return IsNumber;
	   }
           
         
         <%--  Assign asset to entered Team in text Box corresonding to Asset --%>
         function assetToTeamAssign(team, asset)
         {
         		var teamNumber = team
           		var asset = asset
           		var rackPlan = jQuery('#rackPlan').val();
         		var role = jQuery('#role').val();
           		var bundleId = jQuery('#moveBundleId').val();
         		${remoteFunction(action:'assetTeamAssign', params:'\'teamId=\' +teamNumber+\'&asset=\'+asset+\'&rackPlan=\'+rackPlan+\'&bundleId=\'+bundleId+\'&role=\'+role', onComplete:"teamAssignComplete(e);")}
         }
         
         function assetCartAssign(cart, asset)
         {
         	
         		var cartNumber = cart.value
           		var asset = asset
           		var bundleId = jQuery('#moveBundleId').val();
         		${remoteFunction(action:'assetCartAssign', params:'\'cartNumber=\' +cartNumber+\'&asset=\'+asset+\'&bundleId=\'+bundleId', onComplete:"cartAssignComplete(e);")}
         	
         }
         function assetShelfAssign(shelf, asset)
         {
         		
         		var shelfNumber = shelf.value
           		var asset = asset
           		var rackPlan = jQuery('#rackPlan').val();
           		var bundleId = jQuery('#moveBundleId').val();
         		${remoteFunction(action:'assetShelfAssign', params:'\'shelfNumber=\' +shelfNumber+\'&asset=\'+asset+\'&bundleId=\'+bundleId')}
         	
         }
         	
         
        <%--   AutoFill assignment of assets to Selected Team --%>
         function autoFillTeam( teamCode) {
         	var teamCode = teamCode
         	var assets=document.getElementsByName('asset')
         	var assetList = new Array()
         	for(var assetRow = 0; assetRow < assets.length; assetRow++) {
         		assetList[assetRow] = assets[assetRow].value
         	}
         	var bundleId = jQuery('#moveBundleId').val();
         	var rackPlan = jQuery('#rackPlan').val();
         	var role = jQuery('#role').val();
         	if(teamCode != 'null' && teamCode!= '' && assetList.length > 0 && bundleId != null)
         	{
	         	${remoteFunction(action:'autoFillTeamAssign', params:'\'teamCode=\' +teamCode+\'&assets=\'+assetList+\'&rackPlan=\'+rackPlan+\'&bundleId=\'+bundleId+\'&role=\'+role',onLoading:"showProcessing();", onComplete:"autoTeamAssignComplete( e );")}
	        }
         }
         
         function filterAssetsOnTeam( teamCode ) {
         	jQuery('#filterRack').attr("selectedIndex","0");
         	var teamCode = teamCode
         	var rackPlan = jQuery('#rackPlan').val();
         	var role = jQuery('#role').val();
         	var bundleId = jQuery('#moveBundleId').val();
         	if(bundleId != null && bundleId != "") {
	         	${remoteFunction(action:'filterAssetByTeam', params:'\'teamCode=\' +teamCode+\'&rackPlan=\'+rackPlan+\'&bundleId=\'+bundleId+\'&role=\'+role',onLoad="showProcessing()", onComplete:"filterByTeam( e );")}
    		}     	
         }
         
         function filterAssetsOnRack( rack ) {
         	var arSelected = new Array();
         	jQuery('#filterTeam').attr("selectedIndex","0");
         	var filterRacks = document.getElementById('filterRack')
         	var rackPlan = jQuery('#rackPlan').val();
         	for(var rack=0;rack<filterRacks.length;rack++)
         	{ 
         		if (filterRacks[rack].selected) {
         			arSelected.push(filterRacks[rack].value);
         		}
         	} 
         	var bundleId = jQuery('#moveBundleId').val();
         	var role = jQuery('#role').val();
         	${remoteFunction(action:'filterAssetByRack', params:'\'rack=\'+arSelected+\'&rackPlan=\'+rackPlan+\'&bundleId=\'+bundleId+\'&role=\'+role', onComplete:"filterByTeam( e );")}
         }
         
         function moveRackUp() {
         	var optionList = jQuery('#filterRack').attr("options");
         	var selectedIndex = jQuery('#filterRack').attr("selectedIndex");
         	if ( selectedIndex > 0) {
         		jQuery('#filterRack').attr("selectedIndex", selectedIndex-1);
         		filterAssetsOnRack(jQuery('#filterRack').val())
         	}
         	 
         }
         function moveRackDown() {
         	var optionList = jQuery('#filterRack').attr("options");
         	var selectedIndex = jQuery('#filterRack').attr("selectedIndex");
         	if ( selectedIndex < (optionList.length - 1) ) {
         		jQuery('#filterRack').attr("selectedIndex",selectedIndex+1);
         		filterAssetsOnRack(jQuery('#filterRack').val())
         	}
         	 
         }
         
     function filterByTeam(e) {
     	hideProcessing();
   	 	var assetList = eval('(' + e.responseText + ')')
   	 	table = document.getElementById('assetTable')
   	 	var rowCount = table.rows.length; 
   	 	for(var i = 1; i < rowCount; i++) 
   	 	{
   	 		var row = table.rows[i];  
        	table.deleteRow(i);  
            rowCount--;  
            i--;  
        } 
        
        if (assetList) {
 			addAssetRow( assetList )    
		}   
   	 	
     }
     function addAssetRow(assetList){
     
     	var rackPlan = jQuery('#rackPlan').val()
     	var length = assetList.length 
     	table = document.getElementById('assetTable')
		for (var i=0; i < length; i++) {
			var assetEntity = assetList[i]
			var row = table.insertRow( i+1 ); 
			row.id='row_'+assetEntity.id;
			if (i % 2 == 0) {
                    row.style.backgroundColor ='#FFFFFF';
               } else {
                    row.style.backgroundColor ='#E0E0E0';
               }
            
	       	var cell1 = row.insertCell(0);
	        cell1.innerHTML = assetEntity.assetTag
	         var assetElement = document.createElement("input");  
            assetElement.type = "hidden";
            assetElement.name = "asset"
            assetElement.id = "asset"
            assetElement.value = assetEntity.id 
            cell1.appendChild(assetElement); 
	        var cell2 = row.insertCell(1);
	        cell2.innerHTML = assetEntity.assetName
	        var cell3 = row.insertCell(2);
	        cell3.innerHTML = assetEntity.model
	        if(rackPlan == "UnrackPlan") {
	        	var cell4 = row.insertCell(3);
	        	cell4.innerHTML = assetEntity.sourceLocation
	        	var cell5 = row.insertCell(4);
	        	cell5.innerHTML = assetEntity.sourceRack	
	        }else {
				var cell4 = row.insertCell(3);
	        	cell4.innerHTML = assetEntity.targetLocation
	        	var cell5 = row.insertCell(4);
	        	cell5.innerHTML = assetEntity.targetRack		        	
	        }
	        var cell6 = row.insertCell(5);
	        if(rackPlan == "RerackPlan") {
	        	cell6.innerHTML = assetEntity.targetPosition
	        }else {
				cell6.innerHTML = assetEntity.sourcePosition	        	
	        }
	       
	        var cell7 = row.insertCell(6);
	        cell7.innerHTML = assetEntity.uSize
	        var cell8 = row.insertCell(7);
	        cell8.id = assetEntity.id
	        cell8.onclick = function(){makeSelect(this, this.id );}
	        var teamAssignElement = document.createElement("span");  
            teamAssignElement.style.display = 'block'
            teamAssignElement.id = "span_"+assetEntity.id
            teamAssignElement.innerHTML = assetEntity.team
            teamAssignElement.style.width="50px"
            cell8.appendChild(teamAssignElement);
            var teamAssignSelectElement = document.createElement("select");  
            teamAssignSelectElement.style.display = 'block'
            teamAssignSelectElement.id = assetEntity.id
            teamAssignSelectElement.name = "assetTeamAssign_"+assetEntity.id
            var projectTeamList = assetEntity.projectTeam
            var defaultOption = document.createElement('option');
    		defaultOption.text = "UnAssigned"
    		defaultOption.value = "null"
    		try {
    			teamAssignSelectElement.add(defaultOption,null);
    		}catch(ex){
    			teamAssignSelectElement.add(defaultOption);
    		}
            for(var teamRow =0; teamRow < projectTeamList.length; teamRow++) {
            
            	var teamOption = document.createElement('option');
    			teamOption.text = projectTeamList[teamRow].teamCode
    			teamOption.value = projectTeamList[teamRow].id
    			
    			try {
      				teamAssignSelectElement.add(teamOption,null);
      				if( assetEntity.team == teamOption.value ) {
      					teamAssignSelectElement.selectedIndex = teamRow+1
      				} 
      				 // standards compliant; doesn't work in IE
    			}
   				catch(ex) {
      				teamAssignSelectElement.add(teamOption); // IE only
      				if( assetEntity.team == teamOption.value ) {
      					teamAssignSelectElement.selectedIndex = teamRow+1
      				}
    			}
            
            } 
            teamAssignSelectElement.style.width="75px"
            
            teamAssignSelectElement.onchange = function(){assetToTeamAssign(this.value, this.id );}
            teamAssignSelectElement.style.display = 'none'
            //var assetID =assetEntity.id 
            cell8.appendChild(teamAssignSelectElement); 
		 	if(rackPlan == "RerackPlan") {
		 		var cell9 = row.insertCell(8);
	        	var cartElement = document.createElement("input");  
            	cartElement.type = "text";
            	cartElement.name = "assetCartAssign_"+assetEntity.id
            	cartElement.id = assetEntity.id
           		cartElement.value = assetEntity.cart 
           		cartElement.onblur = function(){assetCartAssign(this, this.id );}
            	cartElement.style.width="50px"
            	cell9.appendChild(cartElement); 
           		var cell10 = row.insertCell(9);
	        	var shelfElement = document.createElement("input");  
            	shelfElement.type = "text";
            	shelfElement.name = "assetShelfAssign_"+assetEntity.id
            	shelfElement.value = assetEntity.shelf
            	shelfElement.id = assetEntity.id
            	shelfElement.onblur = function(){assetShelfAssign(this, this.id );} 
            	shelfElement.style.width="50px"
            	cell10.appendChild(shelfElement); 
	        		
		 	}
     	}
     }
     
     
     function autoTeamAssignComplete( e ) {
     	hideProcessing()
     	jQuery('#team').attr("selectedIndex","0");
     	var teamAssetList = eval('(' + e.responseText + ')')
     	var assetList = teamAssetList[0].assetList
		table = document.getElementById('assetTable')
      	deleteRow( table )
     	addAssetRow(assetList)
     	showTeamAssetCount(teamAssetList[0].teamAssetCounts)
     }
     
     
     function teamAssignComplete( e ) {
     	var teamAsset = eval('(' + e.responseText + ')')
     	if (teamAsset && teamAsset.length > 0) {
     		showTeamAssetCount(teamAsset)
     	} else {
     	jQuery('#rackPlan').focus();
     	alert("Team Not Found")
     	}
     }
     
     function cartAssignComplete( e ) {
     	var cartAssetList = eval('(' + e.responseText + ')')
     	if (cartAssetList && cartAssetList.length > 0) {
     		showCartAssetCount(cartAssetList)
     	} 
     	
     }
     
     function showCartAssetCount( cartAssetList ){
      	var table = document.getElementById('cartAssetCountTable');
      	deleteRow( table )
      	if (cartAssetList) {
 			var length = cartAssetList.length
			for (var i=0; i < length; i++) {
				var cartAsset = cartAssetList[i]
				var row = table.insertRow( i+1 ); 
	            var cartelement = row.insertCell(0);
	            cartelement.innerHTML = cartAsset.cart
				var assetElement = row.insertCell(1);  
	            assetElement.innerHTML = cartAsset.cartAssetCount;
	            var usizeElement = row.insertCell(2);  
	            usizeElement.innerHTML = cartAsset.usizeUsed;  
			 }     
		}
	 }
     function showTeamAssetCount( teamAsset ){
      	var table = document.getElementById('teamAssetCountTable');
      	deleteRow( table )
      	     	
      	if (teamAsset) {
 			var length = teamAsset.length
			for (var i=0; i < length; i++) {
				var team = teamAsset[i]
				var row = table.insertRow( i+1 ); 
	            var cell1 = row.insertCell(0);
	            cell1.innerHTML = team.teamCode
				var cell2 = row.insertCell(1);  
	             cell2.innerHTML = team.assetCount;  
			 }     
		}
	 }
	 function submitForm(){	
	 	jQuery('form#bundleTeamAssetForm').attr({action: "bundleTeamAssignment"}); 
	 	jQuery('form#bundleTeamAssetForm').submit();
	 }
 	var loaded = false;
    function showProcessing()
    {		
    	loaded =false;
          showLoadingImage()
    }

    function showLoadingImage()
    {
		var processTab = jQuery('#processDiv');
        processTab.attr("style", "display:block");
        var assetTab = jQuery('#assetDiv');
        assetTab.attr("style", "display:none");
	}
	function hideProcessing()
    {
    	var processTab = jQuery('#processDiv');
        processTab.attr("style", "display:none");
        var assetTab = jQuery('#assetDiv');
        assetTab.attr("style", "display:block");
    }
    function sortAssets( sortBy ){
    	var arSelected = new Array();
     	var team = jQuery('#filterTeam').val();
     	var filterRacks = document.getElementById('filterRack')
     	var rackPlan = jQuery('#rackPlan').val();
     	for(var rack=0;rack<filterRacks.length;rack++)
     	{ 
     		if (filterRacks[rack].selected) {
     			arSelected.push(filterRacks[rack].value);
     		}
     	} 
     	var order = 'asc'
        var thClass = $("#"+sortBy).attr("class")
        if(thClass == "sortable sorted asc"){
        	order = 'desc'
        }
     	$("#assetTable th").each( function() {
			$(this).attr("class","sortable");
		})	
		$("#"+sortBy).attr("class","sortable sorted "+order)
     	var bundleId = jQuery('#moveBundleId').val();
     	var role = jQuery('#role').val();
     	${remoteFunction(action:'sortAssets', params:'\'rack=\'+arSelected+\'&rackPlan=\'+rackPlan+\'&role=\'+role+\'&bundleId=\'+bundleId+\'&team=\'+team+\'&sortBy=\'+sortBy+\'&order=\'+order', onComplete:"filterByTeam( e );")}
    }
    </script>

  </head>
<body>
	<div class="body">
		<h1>Bundle Team Assignment</h1>
		<div>
	      	<table style="width:90%">
				<tr class="prop">
					<td style="width:100%; valign="top" class="value" colspan="2">
		        	<div class="border_bundle_team">
		        	<g:form method="post" name="bundleTeamAssetForm">
		        	<input type="hidden"  name="id" id="id" value="${moveBundleInstance?.id}" />
		          	<table style="border:0px;width:100%">
		            	<tbody>
		              		<tr style="width:100%">
								<td valign="top" style="25%"class="name">&nbsp;
		                			<label for="Name">
		                				<g:select optionKey="id" from="${MoveBundle.findAll('from MoveBundle where project = '+moveBundleInstance.project.id)}" id="moveBundleId" name="moveBundle" value="${moveBundleInstance.id}" onChange="submitForm()" />
		                			</label>
		              			</td>
		              			<td valign="top" style="25%"class="name">&nbsp;
		                			<label for="role">
		                				<g:select id="role" name="role" from="${ProjectTeam.constraints.role.inList}" valueMessagePrefix="ProjectTeam.role" value="${role}" onChange="submitForm()"></g:select>
		                			</label>
		              			</td>
		              			<td style="width:250px; float:left;">
		              				<label for="location">
		              					<g:select name="rackPlan" from="${['UnrackPlan', 'RerackPlan']}" id="rackPlan" valueMessagePrefix="bundle.rackPlan" value="${rack}" onChange="submitForm()" />
		              				</label>
		              			</td>
			              
								<td style="width:250px; height:auto; margin-left:100px;">
		              				<b style="margin-left:100px;">Team Assignments</b>
		              				<table id="teamAssetCountTable" style="width:150px; height:auto; margin-left:100px;">
		              					<thead>
		              						<tr>
		              							<th>Team</th>
		              							<th>Assets</th>
		              						</tr>
		              					</thead>
		              					<tbody>
		              						<g:each in="${teamAssetCount}" var="teamAsset">
			    	          					<tr><td>${teamAsset?.teamCode}</td><td>${teamAsset?.assetCount}</td></tr>
		              						</g:each>
		              					</tbody>		
		              				</table>
		              			</td>
		              			<g:if test="${rack == 'RerackPlan' && ['MOVE_TECH','CLEANER'].contains(role)}">
		              			<td style="width:150px; height:auto;">
		              				<b>Cart Assignments</b>
		              				<table id="cartAssetCountTable" style="width:200px; height:auto;">
		              					<thead>
		              						<tr>
		              							<th>Cart</th>
		              							<th>Assets</th>
		              							<th>U's Used</th>
		              						</tr>
		              					</thead>
		              					<tbody>
		              						<g:each in="${cartAssetCountList}" var="cartAsset">
					              				<tr><td>${cartAsset?.cart}</td><td>${cartAsset?.cartAssetCount}</td><td>${cartAsset?.usizeUsed}</td></tr>
					              			</g:each>
		    			          		</tbody>		
		              				</table>
		              			</td>
		              			</g:if>
		              		</tr>
		              		<tr>
		              			<td valign="middle">
			              			<div style="width:140px;">
			              				<span><b>Filter By Racks</b></span>
			              				<div style="float:left; width:30px;"><a href="#" onclick="moveRackUp()" id="add"><img src="${resource(dir:'images',file:'up-arrow.png')}" style="float: left; border: none;" /></a><br/>
							              	<a href="#" onclick="moveRackDown()" id="add"><img src="${resource(dir:'images',file:'down-arrow.png')}" style="float: left; border: none;" /></a><br/>
						              	</div>
						              	
				              			<div style="float:left;">
				              				<select id="filterRack" multiple="multiple" onMouseUp="filterAssetsOnRack(this.value)"  style="width: 100px; height: 70px;">
				              					<option value="all" selected="selected">All Racks</option>
				              					<g:each in="${assetEntitysRacks}" var="assetEntitysRacks">
				              						<g:if test="${rack == 'UnrackPlan' }">
				              							<option value="${assetEntitysRacks?.sourceRack}">${assetEntitysRacks?.sourceRack}</option>
				              						</g:if>
					              					<g:else>
					              						<option value="${assetEntitysRacks?.targetRack}">${assetEntitysRacks?.targetRack}</option>
					              					</g:else>
						           				</g:each>
				              				</select>
				              			</div>
			              			</div>
		    	       			</td>
		    	          
		    	          		<td style="padding-left:50px;">
			    	          		<div style="border:0px; width:auto; height:auto; float:left;">
			    	          			<b>Filter By Team</b>    	          	
			    	          				<div>
			    	          					<select id="filterTeam" onchange="filterAssetsOnTeam(this.value)">
				              						<option value="">All Teams</option>
			              							<g:each in="${projectTeamInstance}" var="projectTeam">
			       	      								<option value="${projectTeam?.id}">${projectTeam?.teamCode}</option>
				           							</g:each>
				           							<option value="unAssign">UnAssigned</option>
			             						</select>
			    	          				</div>
			    	          		</div>
		    	          		</td>
		    	          
		    	          		<td style="padding-left:50px; padding-top: 50px;">
		    	          			<table style="border:0px;">
				    	          		<tr>
				    	          			<td>
		    			          				<select id="team" onchange="autoFillTeam(this.value)">
		              								<option value="null">Select Team to Assign All Assets To</option>
		              								<g:each in="${projectTeamInstance}" var="projectTeam">
		       	      									<option value="${projectTeam?.id}">${projectTeam?.teamCode}</option>
			           								</g:each>
			           								<option value="UnAssign">UnAssign</option>
		             							</select>	
		    	          					</td>
		    	          				</tr>
		    	          			</table>
		    	          		</td>
			              </tr>
		          	</table>
		          	</g:form>
		          	<div  id="processDiv" style="overflow:scroll; width:100%; display:none;">
		          		<img src="../images/processing.gif"/>
		          	</div>
		           	<div  id="assetDiv" style="width:100%;">
		            	<table id="assetTable">
		              		<thead>
		                		<tr>
		                  			<th class="sortable" id="assetTag"><a href="javascript:sortAssets('assetTag')" >Asset Tag</a></th>
				                  	<th class="sortable" id="assetName"><a href="javascript:sortAssets('assetName')" >Asset</a></th>
									<th class="sortable" id="model"><a href="javascript:sortAssets('model')" >Model</a></th>
									<th class="sortable" id="room"><a href="javascript:sortAssets('room')" >Room</a></th>
				                  	<th class="sortable" id="rack"><a href="javascript:sortAssets('rack')" >Rack</a></th>
				                  	<th class="sortable" id="uposition"><a href="javascript:sortAssets('uposition')" >Pos</a></th>
				                  	<th class="sortable" id="usize"><a href="javascript:sortAssets('usize')" >Size</a></th>
				                  	<th class="sortable" id="teamHeader"><a href="javascript:sortAssets('teamHeader')" >Team</a></th>
				                  	<g:if test="${rack == 'RerackPlan' && ['MOVE_TECH','CLEANER'].contains(role)}">
				                  	<th class="sortable" id="cart"><a href="javascript:sortAssets('cart')" >Cart</a></th>
				                  	<th class="sortable" id="shelf"><a href="javascript:sortAssets('rack')" >Shelf</a></th>
				                  	</g:if>
				                </tr>
			              	</thead>
		              		<tbody>
		             			<%int row=0;%>
		                			<g:each in="${assetEntityInstanceList}" var="assetEntityInstance" status="i">
		                  				<tr style="background-color: ${(i % 2) == 0 ? '#FFFFFF' : '#E0E0E0'}" id="row_${assetEntityInstance?.id}">
						                    <td style="border:1px;"><input type="hidden" name="asset" id="asset" value="${assetEntityInstance?.id}" />${assetEntityInstance?.assetTag}</td>
		                    				<td>${assetEntityInstance?.assetName}</td>
		                    				<td>${assetEntityInstance?.modelName}</td>
	                    					<td>${assetEntityInstance?.room}</td>
					                    	<td>${assetEntityInstance?.rack}</td>
	                    					<td>${assetEntityInstance?.rackPosition}</td>
					                    	<td>${assetEntityInstance?.usize}</td>
					                    	<td id="${assetEntityInstance?.id}" onclick="makeSelect(this,${assetEntityInstance?.id})" >
												<g:select style="display:none;" from= "${projectTeamInstance}" name="assetTeamAssign_${assetEntityInstance?.id}" value="${assetEntityInstance?.teamId}" optionKey="id" optionValue="teamCode" noSelection="['null':'Unassigned']" onChange="assetToTeamAssign(this.value,'${assetEntityInstance?.id}');"/>
												<span id="span_${assetEntityInstance?.id}">${assetEntityInstance?.teamCode} </span>
											</td>
											<g:if test="${rack != 'UnrackPlan' && ['MOVE_TECH','CLEANER'].contains(role)}">
												<td><input size=5px; type=text name="assetCartAssign_${assetEntityInstance?.id}" id="${assetEntityInstance?.id}" value="${assetEntityInstance?.cart}" onblur="assetCartAssign(this,'${assetEntityInstance?.id}');"/></td>
												<td><input size=5px; type=text name="assetShelfAssign_${assetEntityInstance?.id}" id="${assetEntityInstance?.id}" value="${assetEntityInstance?.shelf}" onblur="assetShelfAssign(this,'${assetEntityInstance?.id}');"/></td>
											</g:if>
		                  				</tr>
		                  				<% row++;%>	
		                			</g:each>
		              		</tbody>
		            	</table>
		          	</div>
		        </div>
		      	</td>
		  	</tr>
			</table>
	    </div>
    </div>
    <script>
    currentMenuId = "#eventMenu";
	$("#eventMenuId a").css('background-color','#003366')
   </script>
</body>
</html>
