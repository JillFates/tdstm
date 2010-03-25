<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="moveBundleHeader" />
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
			      var id = document.createTextNode(asset.assetTag);
			      td1.appendChild( checkbox )
			      td1.appendChild( id )
			      tr.appendChild( td1 )
			      var td2 = document.createElement('td');
			      var desc = document.createTextNode(asset.assetName);
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
			      var id = document.createTextNode(asset.assetTag);
			      td1.appendChild( checkbox );
			      td1.appendChild( id );
			      tr.appendChild( td1 );
			      var td2 = document.createElement('td');
			      var desc = document.createTextNode(asset.assetName);
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
	document.bundlesForm.bundleLeft.value = "${leftBundleInstance?.id}"
	}
	
	function sortTag(header, side) {
		var leftBundle = document.bundlesForm.bundleLeft.value;
		var rightBundle = document.bundlesForm.bundleRight.value;
		document.bundlesForm.action = "sortAssetList?column="+header+"&&side="+side+"&&leftBundle="+leftBundle+"&&rightBundle="+rightBundle;
		document.bundlesForm.submit();
	}
	function bundleChange(){
	document.bundlesForm.action = "assignAssetsToBundleChange";
	document.bundlesForm.submit();
	}
	</script> 
</head>
<body>

<div class="body">
<h1>Assign Assets to Bundle</h1>
<div>

	<table>
			<tr class="prop">
				<td valign="top" class="value" colspan="2">
				<table style="border: none;">
				<g:form name="bundlesForm">
					<input type="hidden" name="sortField" value="${sortField}"/>
					<input type="hidden" name="orderField" value="${orderField}"/>
					<input type="hidden" name="sideField" value="${sideField}"/>
					<tr>
						<td valign="top" >Move Bundle:
						<select name="bundleLeft" id="bundleLeftId" onchange="bundleChange();" >
							<option value="">Unassigned</option>
							<g:each in="${moveBundles}" var="moveBundle">
			                	<option value="${moveBundle.id}">${moveBundle}</option>
			                </g:each>
						</select>
						</td>
						<td valign="top" ><label>&nbsp;</label></td>
						<td valign="top" >Move Bundle:
						<select name="bundleRight" id="bundleRightId" onchange="bundleChange();" >
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
						<div class="scrollTable" style="width: 100%;float: left;">  
					       <table id="assetsLeftTableId" style="width: 100%;float: left; border: 0px">  
					         <thead>  
					           <tr>  
					           <g:sortableColumn action="sortAssetList"  property="assetTag" title="Asset Tag" params="['rightBundle':moveBundleInstance?.id, 'leftBundle':leftBundleInstance?.id,'side':'left','sortField':sortField,'sideField':sideField,'orderField':orderField]" />
					           <g:sortableColumn action="sortAssetList"  property="assetName" title="Server Name" params="['rightBundle':moveBundleInstance?.id, 'leftBundle':leftBundleInstance?.id, 'side':'left','sortField':sortField,'sideField':sideField,'orderField':orderField]"/>
					           <g:sortableColumn action="sortAssetList" property="lapplication" title="Application" params="['rightBundle':moveBundleInstance?.id, 'leftBundle':leftBundleInstance?.id, 'side':'left','sortField':sortField,'sideField':sideField,'orderField':orderField]"/>
					           <g:sortableColumn action="sortAssetList" property="sourceLocation" title="Src Loc/Rack" params="['rightBundle':moveBundleInstance?.id, 'leftBundle':leftBundleInstance?.id, 'side':'left','sortField':sortField,'sideField':sideField,'orderField':orderField]"/>
					           </tr>  
					         </thead>  
					         <tbody id="assetsLeftTbodyId">
					         <g:each in="${moveBundleAssets}" var="moveBundleAsset" status="i">
					           <tr id="trleft_${moveBundleAsset?.id}" onclick="selectCheckBox('leftassetId_${moveBundleAsset?.id}', this.id)">  
					             <td> <input type="checkbox" name="leftasset_${moveBundleAsset?.id}" id="leftassetId_${moveBundleAsset?.id}" value="${moveBundleAsset?.id}" onclick="selectCheckBox(this.id, 'trleft_${moveBundleAsset?.id}')" />${moveBundleAsset?.assetTag}</td>  
					             <td>${moveBundleAsset?.assetName}</td>  
					             <td>${moveBundleAsset?.application}</td>  
					             <td>${moveBundleAsset?.sourceLocation}/${moveBundleAsset?.sourceRack}</td>  
					           </tr>  
					           </g:each>
					         </tbody>  
					       </table>  
					     </div>
					     </g:form>
						</td>
						
						<td valign="middle" style="vertical-align: middle"><span
							style="white-space: nowrap; height: 100px;"> 
							<a href="#" id="add">
							<img  src="${createLinkTo(dir:'images',file:'right-arrow.png')}" style="float: left; border: none;"/>
							</a></span><br/><br/><br/>
						<br/>
						<span style="white-space: nowrap;"> <a href="#" id="remove" >
						<img  src="${createLinkTo(dir:'images',file:'left-arrow.png')}" style="float: left; border: none;"/>
						</a></span></td>
						<td valign="top" >
						<g:form name="assignRightAssetsForm">
						<input type="hidden" name="sample" value="abcd"/>
						<div class="scrollTable" style="width: 100%;float: left;">  
					       <table id="assetsRightTableId" style="width: 100%;float: left;border: 0px">  
					         <thead>  
					          <tr>  
					             <g:sortableColumn action="sortAssetList" property="asset_tag" title="Asset Tag" params="['rightBundle':moveBundleInstance?.id, 'leftBundle':leftBundleInstance?.id, 'side':'right','sortField':sortField,'sideField':sideField,'orderField':orderField]"/>
					             <g:sortableColumn action="sortAssetList" property="asset_name" title="Server Name" params="['rightBundle':moveBundleInstance?.id, 'leftBundle':leftBundleInstance?.id, 'side':'right','sortField':sortField,'sideField':sideField,'orderField':orderField]"/>
					             <g:sortableColumn action="sortAssetList" property="application" title="Application" params="['rightBundle':moveBundleInstance?.id, 'leftBundle':leftBundleInstance?.id, 'side':'right','sortField':sortField,'sideField':sideField,'orderField':orderField]"/>
					             <g:sortableColumn action="sortAssetList" property="source_location" title="Src Loc/Rack" params="['rightBundle':moveBundleInstance?.id, 'leftBundle':leftBundleInstance?.id, 'side':'right','sortField':sortField,'sideField':sideField,'orderField':orderField]"/>
					           </tr>
					         </thead>  
					         <tbody id="assetsRightTbodyId" >
					         	<g:each in="${currentBundleAssets}" var="currentBundleAsset" status="i">
					           <tr id="trright_${currentBundleAsset?.id}" onclick="selectCheckBox('rightassetId_${currentBundleAsset?.id}', this.id )">  
					             <td><input type="checkbox" name="rightasset_${currentBundleAsset?.id}" id="rightassetId_${currentBundleAsset?.id}" value="${currentBundleAsset?.id}" onclick="selectCheckBox( this.id, 'trright_${currentBundleAsset?.id}' )"/>${currentBundleAsset?.assetTag}</td>  
					             <td style="vertical-align:middle;">${currentBundleAsset?.assetName}</td>  
					             <td style="vertical-align:middle;">${currentBundleAsset?.application}</td>  
					             <td style="vertical-align:middle;">${currentBundleAsset?.sourceLocation}/${currentBundleAsset?.sourceRack}</td>
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
			</tr></table>
			<!-- <tr align="right"><td colspan="2" ><input type="button" value="Done"> </td> </tr> -->
<script type="text/javascript">
initialize()
</script>
</body>
</html>
