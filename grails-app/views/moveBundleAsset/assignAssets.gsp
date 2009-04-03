<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<title>Assign Assets</title>
<g:javascript library="prototype"/>
    <g:javascript library="jquery" />
	<script type="text/javascript">
			   $().ready(function() {  
			    $('#add').click(function() {
			    	assignAssetsToRightBundle(); 
			     return !$('#assetsLeftId option:selected').remove().appendTo('#assetsRightId');  
			    });  
			    $('#remove').click(function() {
			    	assignAssetsToLeftBundle();
			     return !$('#assetsRightId option:selected').remove().appendTo('#assetsLeftId');  
			    });  
			   });
	function assignAssetsToRightBundle(){
		var formSize = document.assignLeftAssetsForm.elements.length
		var bundleLeft = document.bundlesForm.bundleLeft.value;
		var bundleRight = document.bundlesForm.bundleRight.value;
		var tb = document.getElementById('assetsLeftTbodyId')
		var assets = new Array(); 
		for(i=0; i<formSize; i++)
		{
			if(document.assignLeftAssetsForm.elements[i].type == "checkbox")
			{
				var obj = document.assignLeftAssetsForm.elements[i]
				if(obj.checked == true ){
				 	assets.push(obj.value);
				} 
			}
		}
		for(i=0; i< assets.length; i++){
			var trleft = document.getElementById("trleft_"+assets[i])
			tb.removeChild( trleft )
		}
		if(assets != ""){
			${remoteFunction(action:'saveAssetsToBundle', params:'\'assets=\'+ assets +\'&bundleFrom=\'+bundleLeft +\'&bundleTo=\'+bundleRight', onComplete:'showAssetsRight(e)')}
		}
	}
	function assignAssetsToLeftBundle(){
		var formSize = document.assignRightAssetsForm.elements.length
		var bundleLeft = document.bundlesForm.bundleLeft.value;
		var bundleRight = document.bundlesForm.bundleRight.value;
		var tb = document.getElementById('assetsRightTbodyId')
		var assets = new Array(); 
		for(i=0; i<formSize; i++)
		{
			if(document.assignRightAssetsForm.elements[i].type == "checkbox")
			{
				var obj = document.assignRightAssetsForm.elements[i]
				if(obj.checked == true ){
				 	assets.push(obj.value);
				} 
			}
		}
		for(i=0; i< assets.length; i++){
			var trright = document.getElementById("trright_"+assets[i])
			tb.removeChild( trright )
		}
		if(assets != ""){
			${remoteFunction(action:'saveAssetsToBundle', params:'\'assets=\'+ assets +\'&bundleFrom=\'+bundleRight +\'&bundleTo=\'+bundleLeft', onComplete:'showAssetsLeft(e)')}
		}
	}
	function showAssetsLeft(e){
	      // The response comes back as a bunch-o-JSON
		  var assets = eval("(" + e.responseText + ")")	
	      // evaluate JSON
	      var rselect = document.getElementById('assetsLeftTableId')
	      var tb = document.getElementById('assetsLeftTbodyId')
	      if(tb != null){
	      rselect.removeChild(tb)
	      }
			var tbody = document.createElement('tbody');
			tbody.id = "assetsLeftTbodyId" 
			   
	      // Rebuild the select
	      if (assets) {
	     
		      var length = assets.length
		      	for (var i=0; i < length; i++) {
		      	
			      var asset = assets[i]
			      var tr = document.createElement('tr');
			      	tr.id = "trleft_"+asset.id;
			      	tr.name = asset.id;
			      var td1 = document.createElement('td');
			      var checkbox = document.createElement('input');
					checkbox.type = 'checkbox';
					checkbox.name = 'leftasset_'+asset.id;
					checkbox.id = 'leftassetId_'+asset.id;
					checkbox.value = asset.id;
					checkbox.onclick = function() { selectCheckBox( this.id, 'trleft_'+this.value); };
			      var id = document.createTextNode(asset.id);
			      td1.appendChild( checkbox )
			      td1.appendChild( id )
			      tr.appendChild( td1 )
			      var td2 = document.createElement('td');
			      var desc = document.createTextNode(asset.serverName);
			      td2.appendChild( desc )
			      tr.appendChild( td2 )
			      var td3 = document.createElement('td');
			      var application = document.createTextNode(asset.application);
			      td3.appendChild( application )
			      tr.appendChild( td3 )
			      var td4 = document.createElement('td');
			      var srcLocation = document.createTextNode(asset.srcLocation);
			      td4.appendChild( srcLocation )
			      tr.appendChild( td4 )
			      tr.onclick = function() { selectCheckBox('leftassetId_'+this.name, this.id); };
			      tbody.appendChild( tr )
		      	}
	      }
	      rselect.appendChild( tbody )
	}
	function showAssetsRight(e){
	      // The response comes back as a bunch-o-JSON
		  var assets = eval("(" + e.responseText + ")")	
	      
	      var rselect = document.getElementById('assetsRightTableId')
	      var tb = document.getElementById('assetsRightTbodyId')
	      if(tb != null){
	      rselect.removeChild(tb)
	      }
		  var tbody = document.createElement('tbody');
		  tbody.id = "assetsRightTbodyId" 
	      // Rebuild the tbody
	      if (assets) {
	      
		      var length = assets.length
		      for (var i=0; i < length; i++) {
			      var asset = assets[i];
			      var tr = document.createElement('tr');
			      	tr.id = "trright_"+asset.id;
			      	tr.name = asset.id;
			      var td1 = document.createElement('td');
			      var checkbox = document.createElement('input');
					checkbox.type = 'checkbox';
					checkbox.name = 'rightasset_'+asset.id;
					checkbox.id = 'rightassetId_'+asset.id;
					checkbox.value = asset.id;
					checkbox.onclick = function() { selectCheckBox(this.id, 'trright_'+this.value); };
			      var id = document.createTextNode(asset.id);
			      td1.appendChild( checkbox );
			      td1.appendChild( id );
			      tr.appendChild( td1 );
			      var td2 = document.createElement('td');
			      var desc = document.createTextNode(asset.serverName);
			      td2.appendChild( desc );
			      tr.appendChild( td2 );
			      var td3 = document.createElement('td');
			      var application = document.createTextNode(asset.application);
			      td3.appendChild( application );
			      tr.appendChild( td3 );
			      var td4 = document.createElement('td');
			      var srcLocation = document.createTextNode(asset.srcLocation);
			      td4.appendChild( srcLocation );
			      tr.appendChild( td4 );
			      tr.onclick = function() { selectCheckBox('rightassetId_'+this.name, this.id); };
			      tbody.appendChild( tr );
		      }
	      }
	       
	    rselect.appendChild( tbody );
	}
	
	function selectCheckBox( name, trId ){
		var checkboxName = document.getElementById(name)
		var trObj = document.getElementById(trId)
		var bcolor = trObj.style.backgroundColor
		if(checkboxName.checked){
			checkboxName.checked = false
			trObj.style.backgroundColor = '#FFFFFF'
		} else {
			checkboxName.checked = true
			trObj.style.backgroundColor = '#5F9FCF'
		}
	}
	function initialize(){
	document.bundlesForm.bundleRight.value = "${moveBundleInstance?.id}"
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
<h1>Assign Assets to Bundle</h1>
<div>

	<table>
			<tr class="prop">
				<td valign="top" class="value" colspan="2">
				<table style="border: none;">
				<g:form name="bundlesForm">
					<tr>
						<td valign="top" >
						<select name="bundleLeft" id="bundleLeftId" onchange="${remoteFunction(action:'getBundleAssets', params:'\'bundleId=\'+ this.value', onComplete:'showAssetsLeft(e)')}" >
							<option value="">Unassigned</option>
							<g:each in="${moveBundles}" var="moveBundle">
			                	<option value="${moveBundle.id}">${moveBundle}</option>
			                </g:each>
						</select>
						</td>
						<td valign="top" ><label>&nbsp;</label></td>
						<td valign="top" >
						<select name="bundleRight" id="bundleRightId" onchange="${remoteFunction(action:'getBundleAssets', params:'\'bundleId=\'+ this.value', onComplete:'showAssetsRight(e)')}" >
							<g:each in="${moveBundles}" var="moveBundle">
			                	<option value="${moveBundle.id}">${moveBundle}</option>
			                </g:each>
						</select>
						</td>
					</tr>
					</g:form>
					<tr>
						<td valign="top" >
						<g:form name="assignLeftAssetsForm">
						<div class="scrollTable">  
					       <table id="assetsLeftTableId">  
					         <thead>  
					           <tr>  
					             <th nowrap>Asset Tag</th>  
					             <th nowrap>Server Name</th>  
					             <th nowrap>Application</th>  
					             <th nowrap>Src Loc/Rack</th>  
					           </tr>  
					         </thead>  
					         <tbody id="assetsLeftTbodyId">
					         <g:each in="${moveBundleAssets}" var="moveBundleAsset" status="i">
					           <tr id="trleft_${moveBundleAsset?.id}" onclick="selectCheckBox('leftassetId_${moveBundleAsset?.id}', this.id)">  
					             <td> <input type="checkbox" name="leftasset_${moveBundleAsset?.id}" id="leftassetId_${moveBundleAsset?.id}" value="${moveBundleAsset?.id}" onclick="selectCheckBox(this.id, 'trleft_${moveBundleAsset?.id}')" />${moveBundleAsset?.assetTag}</td>  
					             <td>${moveBundleAsset?.serverName}</td>  
					             <td>${moveBundleAsset?.application}</td>  
					             <td>${moveBundleAsset?.sourceLocation}/${moveBundleAsset?.sourceRack}</td>  
					           </tr>  
					           </g:each>
					         </tbody>  
					       </table>  
					     </div>
						</td>
						</g:form>
						<td valign="middle" style="vertical-align: middle"><span
							style="white-space: nowrap; height: 100px;"> 
							<a href="#" id="add">
							<img  src="${createLinkTo(dir:'images',file:'right-arrow.png')}" style="float: left; border: none;">
							</a></span><br><br><br>
						<br>
						<span style="white-space: nowrap;"> <a href="#" id="remove" >
						<img  src="${createLinkTo(dir:'images',file:'left-arrow.png')}" style="float: left; border: none;">
						</a></span></td>
						<td valign="top" >
						<g:form name="assignRightAssetsForm">
						<div class="scrollTable">  
					       <table id="assetsRightTableId">  
					         <thead>  
					          <tr>  
					             <th nowrap>Asset Tag</th>  
					             <th nowrap>Server Name</th>  
					             <th nowrap>Application</th>  
					             <th nowrap>Src Loc/Rack</th>  
					           </tr>
					         </thead>  
					         <tbody id="assetsRightTbodyId" >
					         	<g:each in="${currentBundleAssets}" var="currentBundleAsset" status="i">
					           <tr id="trright_${currentBundleAsset?.asset?.id}" onclick="selectCheckBox('rightassetId_${currentBundleAsset?.asset?.id}', this.id )">  
					             <td><input type="checkbox" name="rightasset_${currentBundleAsset?.asset?.id}" id="rightassetId_${currentBundleAsset?.asset?.id}" value="${currentBundleAsset?.asset?.id}" onclick="selectCheckBox( this.id, 'trright_${currentBundleAsset?.asset?.id}' )"/>${currentBundleAsset?.asset?.assetTag}</td>  
					             <td>${currentBundleAsset?.asset?.serverName}</td>  
					             <td>${currentBundleAsset?.asset?.application}</td>  
					             <td>${currentBundleAsset?.asset?.sourceLocation}/${currentBundleAsset?.asset?.sourceRack}</td>
					           </tr>  
					           </g:each>
					         </tbody>
					        </table>
					     </div>
					     </g:form>
						</td>
					</tr>
				</table>
				</td>
			</tr>
			<!-- <tr align="right"><td colspan="2" ><input type="button" value="Done"> </td> </tr> -->
	</div>
</div>
<script type="text/javascript">
initialize()
</script>
</body>
</html>
