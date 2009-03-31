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
		var ob = document.assignAssetsForm.assetsLeft;
		var bundleLeft = document.assignAssetsForm.bundleLeft.value;
		var bundleRight = document.assignAssetsForm.bundleRight.value;
		var assets = new Array(); 
		for(i=0; i< ob.length; i++){
			if(ob.options[i].selected){
				assets.push(ob.options[i].value);
			}
		}
		if(assets != ""){
			${remoteFunction(action:'saveAssetsToBundle', params:'\'assets=\'+ assets +\'&bundleFrom=\'+bundleLeft +\'&bundleTo=\'+bundleRight', onComplete:'getAssets(e)')}
		}
	}
	function assignAssetsToLeftBundle(){
		var ob = document.assignAssetsForm.assetsRight;
		var bundleLeft = document.assignAssetsForm.bundleLeft.value;
		var bundleRight = document.assignAssetsForm.bundleRight.value;
		var assets = new Array(); 
		for(i=0; i< ob.length; i++){
			if(ob.options[i].selected){
				assets.push(ob.options[i].value);
			}
		}
		if(assets != ""){
			${remoteFunction(action:'saveAssetsToBundle', params:'\'assets=\'+ assets +\'&bundleFrom=\'+bundleRight +\'&bundleTo=\'+bundleLeft', onComplete:'getAssets(e)')}
		}
	}
	function showAssetsLeft(e){
	      // The response comes back as a bunch-o-JSON
		  var assets = eval("(" + e.responseText + ")")	
	      // evaluate JSON
	      var rselect = document.getElementById('assetsLeftId')
			//  Clear all previous options
		  var l = rselect.length
		  while (l > 1) {
			l--
			rselect.remove(l) 
		  }
	      // Rebuild the select
	      if (assets) {
		      var length = assets.length
		      for (var i=0; i < length; i++) {
			      var asset = assets[i]
			      var popt = document.createElement('option');
			      popt.innerHTML = asset.name
			      popt.value = asset.id
			      try {
				      rselect.appendChild(popt, null) // standards compliant; doesn't work in IE
			      } catch(ex) {
				      rselect.appendChild(popt) // IE only
			      }
		      }
	      }
	}
	function showAssetsRight(e){
	      // The response comes back as a bunch-o-JSON
		  var assets = eval("(" + e.responseText + ")")	
	      // evaluate JSON
	      var rselect = document.getElementById('assetsRightId')
			//  Clear all previous options
		  var l = rselect.length
		  while (l > 1) {
			l--
			rselect.remove(l) 
		  }
	      // Rebuild the select
	      if (assets) {
		      var length = assets.length
		      for (var i=0; i < length; i++) {
			      var asset = assets[i]
			      var popt = document.createElement('option');
			      popt.innerHTML = asset.name
			      popt.value = asset.id
			      try {
				      rselect.appendChild(popt, null) // standards compliant; doesn't work in IE
			      } catch(ex) {
				      rselect.appendChild(popt) // IE only
			      }
		      }
	      }
	}
	
	function getAssets(){
	}
	function initialize(){
	document.assignAssetsForm.bundleRight.value = "${moveBundleInstance?.id}"
	}
	</script> 
</head>
<body>
<div class="menu2">
<ul>
	<li><g:link class="home" controller="projectUtil">Project </g:link>
	</li>
	<li><g:link class="home" controller="person" action="projectStaff"	params="[projectId:moveBundleInstance?.project?.id]">Staff</g:link></li>
	<li><g:link class="home" controller="asset">Assets </g:link></li>
	<li><g:link class="home" controller="asset" action="assetImport">Import/Export</g:link></li>
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
      </ul>
</div>
<div class="body">
<h1>Assign Assets to Bundle</h1>
<div>
<g:form name="assignAssetsForm">
	<table>
			<tr class="prop">
				<td valign="top" class="value" colspan="2">
				<table style="border: none;">
					<tr>
						<td valign="top" class="name">
						<select name="bundleLeft" id="bundleLeftId" onchange="${remoteFunction(action:'getBundleAssets', params:'\'bundleId=\'+ this.value', onComplete:'showAssetsLeft(e)')}" >
							<option value="">Unassigned</option>
							<g:each in="${moveBundles}" var="moveBundle">
			                	<option value="${moveBundle.id}">${moveBundle}</option>
			                </g:each>
						</select>
						</td>
						<td valign="top" class="name"><label>&nbsp;</label></td>
						<td valign="top" class="name">
						<select name="bundleRight" id="bundleRightId" onchange="${remoteFunction(action:'getBundleAssets', params:'\'bundleId=\'+ this.value', onComplete:'showAssetsRight(e)')}" >
							<g:each in="${moveBundles}" var="moveBundle">
			                	<option value="${moveBundle.id}">${moveBundle}</option>
			                </g:each>
						</select>
						</td>
					</tr>
					<tr>
						<td valign="top" class="name">
						 <select name="assetsLeft" id="assetsLeftId" multiple="multiple" size="10" style="width: 400px" >
							<option disabled="disabled" class="option"><ui><li> Asset</li> : <li>Desc </li>:<li> Rack</li> :<li> Rack</li> </option>
							<g:each in="${AssetEntity.list()}" var="moveBundleAsset">
								<option value="${moveBundleAsset?.id}">${moveBundleAsset?.id} : ${moveBundleAsset?.serverName} : ${moveBundleAsset?.sourceRack} : ${moveBundleAsset?.sourceLocation}</option>
							</g:each>
						</select> 
						<!-- 
						<div class="scrollTable">  
					       <table>  
					         <thead>  
					           <tr>  
					             <th>Asset</th>  
					             <th>Desc</th>  
					             <th>Rack</th>  
					             <th>Rack</th>  
					           </tr>  
					         </thead>  
					         <tbody>  
					         <g:each in="${AssetEntity.list()}" var="moveBundleAsset">
					           <tr>  
					             <td>${moveBundleAsset?.id}</td>  
					             <td>${moveBundleAsset?.serverName}</td>  
					             <td>${moveBundleAsset?.sourceRack}</td>  
					             <td>${moveBundleAsset?.sourceLocation}</td>  
					           </tr>  
					           </g:each>
					         <g:each in="${AssetEntity.list()}" var="moveBundleAsset">
					           <tr>  
					             <td>${moveBundleAsset?.id}</td>  
					             <td>${moveBundleAsset?.serverName}</td>  
					             <td>${moveBundleAsset?.sourceRack}</td>  
					             <td>${moveBundleAsset?.sourceLocation}</td>  
					           </tr>  
					           </g:each>
					         <g:each in="${AssetEntity.list()}" var="moveBundleAsset">
					           <tr>  
					             <td>${moveBundleAsset?.id}</td>  
					             <td>${moveBundleAsset?.serverName}</td>  
					             <td>${moveBundleAsset?.sourceRack}</td>  
					             <td>${moveBundleAsset?.sourceLocation}</td>  
					           </tr>  
					           </g:each>
					         <g:each in="${AssetEntity.list()}" var="moveBundleAsset">
					           <tr>  
					             <td>${moveBundleAsset?.id}</td>  
					             <td>${moveBundleAsset?.serverName}</td>  
					             <td>${moveBundleAsset?.sourceRack}</td>  
					             <td>${moveBundleAsset?.sourceLocation}</td>  
					           </tr>  
					           </g:each>
					         <g:each in="${AssetEntity.list()}" var="moveBundleAsset">
					           <tr>  
					             <td>${moveBundleAsset?.id}</td>  
					             <td>${moveBundleAsset?.serverName}</td>  
					             <td>${moveBundleAsset?.sourceRack}</td>  
					             <td>${moveBundleAsset?.sourceLocation}</td>  
					           </tr>  
					           </g:each>
					         <g:each in="${AssetEntity.list()}" var="moveBundleAsset">
					           <tr>  
					             <td>${moveBundleAsset?.id}</td>  
					             <td>${moveBundleAsset?.serverName}</td>  
					             <td>${moveBundleAsset?.sourceRack}</td>  
					             <td>${moveBundleAsset?.sourceLocation}</td>  
					           </tr>  
					           </g:each>
					         </tbody>  
					       </table>  
					     </div>  -->
						</td>
						
						<td valign="middle" style="vertical-align: middle"><span
							style="white-space: nowrap; height: 100px;"> 
							<a href="#" id="add">
							<img  src="${createLinkTo(dir:'images',file:'right-arrow.png')}" style="float: left; border: none;">
							</a></span><br><br><br>
						<br>
						<span style="white-space: nowrap;"> <a href="#" id="remove">
						<img  src="${createLinkTo(dir:'images',file:'left-arrow.png')}" style="float: left; border: none;">
						</a></span></td>
						<td valign="top" class="name">
						<select name="assetsRight" id="assetsRightId" multiple="multiple" size="10"	style="width: 400px">
						<option disabled="disabled" style="background-color: #cccccc;color: #000000;font-weight: bold; ">Asset : Desc : Rack : Room </option>
							<g:each in="${currentBundleAssets}" var="currentBundleAsset">
								<option value="${currentBundleAsset?.asset?.id}">${currentBundleAsset?.asset?.id} : ${currentBundleAsset?.asset?.serverName} : ${currentBundleAsset?.asset?.sourceRack} : ${currentBundleAsset?.asset?.sourceLocation}</option>
							</g:each>
						</form>
						</td>
					</tr>
				</table>
				</td>
			</tr>
			<!-- <tr align="right"><td colspan="2" ><input type="button" value="Done"> </td> </tr> -->
	</div>
</g:form></div>
<script type="text/javascript">
initialize()
</script>
</body>
</html>
