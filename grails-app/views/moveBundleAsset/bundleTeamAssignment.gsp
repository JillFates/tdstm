

<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main" />
    <title>Bundle Team Assignment</title>
 	<g:javascript library="jquery" />
    <g:javascript library="ui.datetimepicker" />
    <g:javascript library="ui.datepicker" />
	<g:javascript library="prototype"/>
	
    <script>
         <!-- Delete Rows from a Table-->
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
           
         
         <!-- Assign asset to entered Team in text Box corresonding to Asset-->
         function assetToTeamAssign(team, asset)
         {
         if (!IsNumeric(team)) 
   			{ 
      			alert('Please enter only numbers in the Team field') 
      			return false; 
      		} else {
         		var teamNumber = team
           		var asset = asset
           		var rackPlan = document.getElementById('rackPlan').value
           		var bundleId = document.getElementById('id').value
         		${remoteFunction(action:'assetTeamAssign', params:'\'teamId=\' +teamNumber+\'&asset=\'+asset+\'&rackPlan=\'+rackPlan+\'&bundleId=\'+bundleId', onComplete:"teamAssignComplete(e);")}
         	}
         }
         
         function assetCartAssign(cart, asset)
         {
         	if (!IsNumeric(cart.value)) 
   			{ 
      			alert('Please enter only numbers in the Cart field') 
      			return false; 
      		} else {
         		var cartNumber = cart.value
           		var asset = asset
           		var bundleId = document.getElementById('id').value
         		${remoteFunction(action:'assetCartAssign', params:'\'cartNumber=\' +cartNumber+\'&asset=\'+asset+\'&bundleId=\'+bundleId')}
         	}	
         	
         }
         function assetShelfAssign(shelf, asset)
         {
         	if (!IsNumeric(shelf.value) || shelf.value >= 10) 
   			{ 
      			alert('Please enter only numbers in the Shelf field and lessthan 10') 
      			return false; 
      		}else { 
         		var shelfNumber = shelf.value
           		var asset = asset
           		var rackPlan = document.getElementById('rackPlan').value
           		var bundleId = document.getElementById('id').value
         		${remoteFunction(action:'assetShelfAssign', params:'\'shelfNumber=\' +shelfNumber+\'&asset=\'+asset+\'&bundleId=\'+bundleId')}
         	}	
         	
         }
         	
         
         <!-- AutoFill assignment of assets to Selected Team -->
         function autoFillTeam( teamCode) {
         
         	var teamCode = teamCode
         	var assets=document.getElementsByName('asset')
         	var assetList = new Array()
         	for(var assetRow = 0; assetRow < assets.length; assetRow++) {
         		assetList[assetRow] = assets[assetRow].value
         	}
         	var bundleId = document.getElementById('id').value
         	var rackPlan = document.getElementById('rackPlan').value
         	if(teamCode != 'null' && teamCode!= '' && assetList.length > 0 )
         	{
	         	${remoteFunction(action:'autoFillTeamAssign', params:'\'teamCode=\' +teamCode+\'&assets=\'+assetList+\'&rackPlan=\'+rackPlan+\'&bundleId=\'+bundleId', onComplete:"autoTeamAssignComplete( e );")}
	        }
         }
         
         function filterAssetsOnTeam( teamCode ) {
         	var teamCode = teamCode
         	var rackPlan = document.getElementById('rackPlan').value
         	var bundleId = document.getElementById('id').value
         	${remoteFunction(action:'filterAssetByTeam', params:'\'teamCode=\' +teamCode+\'&rackPlan=\'+rackPlan+\'&bundleId=\'+bundleId', onComplete:"filterByTeam( e );")}
         	
         }
         
         function filterAssetsOnRack( rack ) {
         	var rackPlan = document.getElementById('rackPlan').value
         	var selectedRack = rack
         	var bundleId = document.getElementById('id').value
         	${remoteFunction(action:'filterAssetByRack', params:'\'rack=\'+selectedRack+\'&rackPlan=\'+rackPlan+\'&bundleId=\'+bundleId', onComplete:"filterByTeam( e );")}
         }
         
         function moveRackUp() {
         	var optionList = document.getElementById('filterRack').options
         	var selectedIndex = document.getElementById('filterRack').selectedIndex
         	if ( selectedIndex > 0) {
         		document.getElementById('filterRack').selectedIndex = selectedIndex -1
         		filterAssetsOnRack(document.getElementById('filterRack').value)
         	}
         	 
         }
         function moveRackDown() {
         	var optionList = document.getElementById('filterRack').options
         	var selectedIndex = document.getElementById('filterRack').selectedIndex
         	if ( selectedIndex < (optionList.length - 1) ) {
         		document.getElementById('filterRack').selectedIndex = selectedIndex +1
         		filterAssetsOnRack(document.getElementById('filterRack').value)
         	}
         	 
         }
         
     function filterByTeam( e) {
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
     function addAssetRow(assetList)
     {
     
     	var rackPlan = document.getElementById('rackPlan').value
     	var length = assetList.length 
     	table = document.getElementById('assetTable')
		for (var i=0; i < length; i++) {
			var moveBundleAsset = assetList[i]
			var row = table.insertRow( i+1 ); 
			if (i % 2 == 0) {
                    row.style.backgroundColor ='#FFFFFF';
               } else {
                    row.style.backgroundColor ='#E0E0E0';
               }
            
	       	var cell1 = row.insertCell(0);
	        cell1.innerHTML = moveBundleAsset.id
	         var assetElement = document.createElement("input");  
            assetElement.type = "hidden";
            assetElement.name = "asset"
            assetElement.id = "asset"
            assetElement.value = moveBundleAsset.id 
            cell1.appendChild(assetElement); 
	        var cell2 = row.insertCell(1);
	        cell2.innerHTML = moveBundleAsset.serverName
	        var cell3 = row.insertCell(2);
	        cell3.innerHTML = moveBundleAsset.model
	        if(rackPlan == "UnrackPlan") {
	        	var cell4 = row.insertCell(3);
	        	cell4.innerHTML = moveBundleAsset.sourceLocation
	        	var cell5 = row.insertCell(4);
	        	cell5.innerHTML = moveBundleAsset.sourceRack	
	        }else {
				var cell4 = row.insertCell(3);
	        	cell4.innerHTML = moveBundleAsset.targetLocation
	        	var cell5 = row.insertCell(4);
	        	cell5.innerHTML = moveBundleAsset.targetRack		        	
	        }
	        var cell6 = row.insertCell(5);
	        if(rackPlan == "RerackPlan") {
	        	cell6.innerHTML = moveBundleAsset.targetPosition
	        }else {
				cell6.innerHTML = moveBundleAsset.sourcePosition	        	
	        }
	       
	        var cell7 = row.insertCell(6);
	        cell7.innerHTML = moveBundleAsset.uSize
	        var cell8 = row.insertCell(7);
	        var teamAssignElement = document.createElement("input");  
            teamAssignElement.type = "text";
            teamAssignElement.name = "assetTeamAssign_"+moveBundleAsset.id
            teamAssignElement.id = moveBundleAsset.id
            teamAssignElement.value = moveBundleAsset.team
            teamAssignElement.style.width="50px"
            //var assetID =moveBundleAsset.id 
            teamAssignElement.onblur = function(){assetToTeamAssign(this.value, this.id );}
            cell8.appendChild(teamAssignElement); 
		 	if(rackPlan == "RerackPlan") {
		 		var cell9 = row.insertCell(8);
	        	var cartElement = document.createElement("input");  
            	cartElement.type = "text";
            	cartElement.name = "assetCartAssign_"+moveBundleAsset.id
            	cartElement.id = moveBundleAsset.id
           		cartElement.value = moveBundleAsset.cart 
           		cartElement.onblur = function(){assetCartAssign(this, this.id );}
            	cartElement.style.width="50px"
            	cell9.appendChild(cartElement); 
           		var cell10 = row.insertCell(9);
	        	var shelfElement = document.createElement("input");  
            	shelfElement.type = "text";
            	shelfElement.name = "assetShelfAssign_"+moveBundleAsset.id
            	shelfElement.value = moveBundleAsset.shelf
            	shelfElement.id = moveBundleAsset.id
            	shelfElement.onblur = function(){assetShelfAssign(this, this.id );} 
            	shelfElement.style.width="50px"
            	cell10.appendChild(shelfElement); 
	        		
		 	}
     	}
     }
     
     
     function autoTeamAssignComplete( e ) {
     	var teamAssetList = eval('(' + e.responseText + ')')
     	assetList = teamAssetList[0].assetList
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
     	document.getElementById('rackPlan').focus()
     	alert("Team Not Found")
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

    </script>

  </head>
  <body>
  <div class="menu2">
<ul>
	<li><g:link class="home" controller="projectUtil">Project </g:link>
	</li>
	<li><g:link class="home" controller="person" action="projectStaff"	params="[projectId:moveBundleInstance?.project?.id]">Staff</g:link></li>
		<li><g:link class="home" controller="assetEntity" params="[projectId:moveBundleInstance?.project?.id]">Assets </g:link></li>
	<li><g:link class="home" controller="assetEntity" action="assetImport" params="[projectId:moveBundleInstance?.project?.id]">Import/Export</g:link> </li>
	<li><a href="#">Contacts </a></li>
	<li><a href="#">Applications </a></li>
	<li><g:link class="home" controller="moveBundle" params="[projectId:moveBundleInstance?.project?.id]">Move Bundles</g:link></li>
</ul>
</div>
<div class="menu2" style="background-color: #003366;">
<ul>
	<li class="title1">Move Bundle: ${moveBundleInstance?.name}</li>
        <li><g:link class="home" controller="projectTeam" action="list" params="[bundleId:moveBundleInstance?.id]" >Team </g:link> </li>
        <li><g:link controller="moveBundleAsset" action="assignAssetsToBundle" params="[bundleId:moveBundleInstance?.id]" >Bundle Asset Assignment</g:link> </li>
        <li><g:link class="home" controller="moveBundleAsset" action="bundleTeamAssignment" params="[bundleId:moveBundleInstance?.id, rack:'UnrackPlan']" >Bundle Team Assignment </g:link> </li>
      </ul>
</div>
  
    <div class="body">
<h1>Bundle Team Assignment</h1>
<div>
      	<table style="width:90%">
			<tr class="prop">
				<td style="width:100px; valign="top" class="value" colspan="2">
      <g:form method="post" name="bundleTeamAssetForm">
        <input type="hidden"  name="id" id="id" value="${moveBundleInstance?.id}" />
        <input type="hidden"  name="rackPlan" id="rackPlan" value="${rack}" />
        <div class="border_bundle_team">
          <table style="border:0px;">
            <tbody>

              &nbsp;<td valign="top" class="name">
                <label for="Name"><b> ${moveBundleInstance?.name}</b></label>
              </td>
              <td style="width:250px; float:left;">
              	<table style="width:250px; float:left;">
              		<tr>
              			<g:if test="${rack == 'UnrackPlan'}">
              				<td style="width:100px; float:left; background-color:#43ca56; padding-left:10px;">
              			</g:if>
              			<g:else>
              				<td style="width:112px; float:left;   padding-left:10px;">
              			</g:else>
              			<g:link controller="moveBundleAsset"  action="bundleTeamAssignment"  params="[bundleId:moveBundleInstance?.id, rackPlan:'unrackPlan']"  >Unrack Plan</g:link>
              			</td>
              			
              			<g:if test="${rack == 'RerackPlan'}">
              				<td style="width:100px; float:left; background-color:#43ca56; padding-left:10px;">
              			</g:if>
              			<g:else>
              				<td style="width:100px; float:left; border-left:1px solid #5585c7; padding-left:10px;">
              			</g:else>
              			<g:link controller="moveBundleAsset" action="bundleTeamAssignment" params="[bundleId:moveBundleInstance?.id, rackPlan:'RerackPlan']"  >Rerack Plan</g:link>
              			</td>
              			
              		</tr>
              	</table>
              </td>
              
              <td style="padding-right:250px;">
              	<table id="teamAssetCountTable">
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
              <tr>
              	<td  bordercolor="1px solid #000000;" align=left><b>Filter By Racks</b></td>
              </tr>
              <tr>
              	<td valign="middle">
              	<div>
              	<div style="float:left;"><a href="#" onclick="moveRackUp()" id="add"><img src="${createLinkTo(dir:'images',file:'up-arrow.png')}" style="float: left; border: none;" /></a><br>
              	
              	<a href="#" onclick="moveRackDown()" id="add"><img src="${createLinkTo(dir:'images',file:'down-arrow.png')}" style="float: left; border: none;" /></a><br>
              	
              	</div>
              	<div style="float:right;">
              		<select id="filterRack" multiple="multiple" onchange="filterAssetsOnRack(this.value)" style="width: 100px; height: 70px;">
              			<option value="">All Racks</option>
              			<g:each in="${moveBundleAssetsRacks}" var="moveBundleAssetsRacks">
              				<g:if test="${rack == 'UnrackPlan'}">
              					<option value="${moveBundleAssetsRacks?.asset?.sourceRack}">${moveBundleAssetsRacks?.asset?.sourceRack}</option>
              				</g:if>
              				<g:else>
              					<option value="${moveBundleAssetsRacks?.asset?.targetRack}">${moveBundleAssetsRacks?.asset?.targetRack}</option>
              				</g:else>
       	      				
	           			</g:each>
              		</select>
              		</div>
              		</div>
    	          </td>
    	          
    	          <td style="padding-left:50px;">
    	          	<table style="border:0px;">
    	          		<tr><td style="padding-left:50px;"><b>Filter By Team</b></td></tr>
    	          		<tr>
    	          			<td style="padding-left:50px;">
    	          				<select id="filterTeam" onchange="filterAssetsOnTeam(this.value)">
	              					<option value="">All Teams</option>
              						<g:each in="${projectTeamInstance}" var="projectTeam">
       	      							<option value="${projectTeam?.teamCode}">${projectTeam?.teamCode}</option>
	           						</g:each>
             					</select>
    	          			</td>
    	          		</tr>
    	          	</table>
              		
    	          </td>
    	          
    	          <td style="padding-left:50px; padding-top: 50px;">
    	          	<table style="border:0px;">
    	          		<tr></tr>
    	          		<tr>
    	          			<td style="padding-left:50px;">
    	          				<select id="team" onchange="autoFillTeam(this.value)">
              						<option value="null">Select Team to Assign All Assets To</option>
              							<g:each in="${projectTeamInstance}" var="projectTeam">
       	      								<option value="${projectTeam?.teamCode}">${projectTeam?.teamCode}</option>
	           							</g:each>
	           							<option value="UnAssign">UnAssign</option>
             					</select>	
    	          			</td>
    	          		</tr>
    	          	</table>
    	          </td>
    	          
              </tr>
            </tbody>
          </table>
           <div  style="overflow:scroll; width:950px;">
            <table id="assetTable">
              <thead>
                <tr>
                  <th>Asset</th>

                  <th>Server</th>

                  <th>Model</th>

                  <th>Room</th>

                  <th>Rack</th>

                  <th>Pos</th>

                  <th>Size</th>
                  
                  <th>Team</th>
                  
                  <g:if test="${rack == 'RerackPlan'}">
                  
                  <th>Cart</th>
                  
                  <th>Shelf</th>
                  
                  </g:if>

                </tr>
              </thead>
              <tbody>
             <%int row=0;%>
                <g:each in="${moveBundleAssetInstanceList}" var="moveBundleAssetInstance" status="i">
                  <tr style="background-color: ${(i % 2) == 0 ? '#FFFFFF' : '#E0E0E0'}">

                    <td style="border:1px;"><input type="hidden" name="asset" id="asset" value="${moveBundleAssetInstance?.asset?.id}" />${moveBundleAssetInstance?.asset?.id}</td>
                    
                    <td>${moveBundleAssetInstance?.asset?.serverName}</td>
                    
                    <td>${moveBundleAssetInstance?.asset?.model}</td>
                    <g:if test="${rack == 'UnrackPlan'}">
                    	<td>${moveBundleAssetInstance?.asset?.sourceLocation}</td>
                    
                    	<td>${moveBundleAssetInstance?.asset?.sourceRack}</td>
                    	<td>${moveBundleAssetInstance?.asset?.sourceRackPosition}</td>
                    </g:if>
                    <g:else>
                    	<td>${moveBundleAssetInstance?.asset?.targetLocation}</td>
                    
                    	<td>${moveBundleAssetInstance?.asset?.targetRack}</td>
                    	<td>${moveBundleAssetInstance?.asset?.sourceRackPosition}</td>
                    </g:else>
                    
                    
                    <td>${moveBundleAssetInstance?.asset?.usize}</td>
					<g:if test="${rack == 'UnrackPlan'}">
					<td><input size=5px; type=text name="assetTeamAssign_${moveBundleAssetInstance?.asset?.id}" id="${moveBundleAssetInstance?.asset?.id}" value="${moveBundleAssetInstance?.sourceTeam?.id}" onblur="assetToTeamAssign(this.value,'${moveBundleAssetInstance?.asset?.id}');" /></td>
                    </g:if>
                    <g:else >
                    <td><input size=5px; type=text name="assetTeamAssign_${moveBundleAssetInstance?.asset?.id}" id="${moveBundleAssetInstance?.asset?.id}" value="${moveBundleAssetInstance?.targetTeam?.id}" onblur="assetToTeamAssign(this.value,'${moveBundleAssetInstance?.asset?.id}');" /></td>
						<td><input size=5px; type=text name="assetCartAssign_${moveBundleAssetInstance?.asset?.id}" id="${moveBundleAssetInstance?.asset?.id}" value="${moveBundleAssetInstance?.cart}" onblur="assetCartAssign(this,'${moveBundleAssetInstance?.asset?.id}');"/></td>
					
						<td><input size=5px; type=text name="assetShelfAssign_${moveBundleAssetInstance?.asset?.id}" id="${moveBundleAssetInstance?.asset?.id}" value="${moveBundleAssetInstance?.shelf}" onblur="assetShelfAssign(this,'${moveBundleAssetInstance?.asset?.id}');"/></td>
					</g:else>
                  </tr>
                  <% row++;%>
                </g:each>
              </tbody>
            </table>
          </div>
        </div>
      </g:form>
      </td>
      </tr>
      </table>
    </div>
   </div>  
  </body>
</html>
