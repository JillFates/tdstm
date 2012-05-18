<%@page import="com.tds.asset.AssetEntity;"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
<meta name="layout" content="projectHeader" />
<title>Planning Console</title>
<g:javascript src="asset.tranman.js" />
<g:javascript src="entity.crud.js" />
<script type="text/javascript" src="${createLinkTo(dir:'d3',file:'d3.v2.js')} "></script>
<link rel="stylesheet" href="${createLinkTo(dir:'d3/force',file:'force.css')}" type="text/css"/>
<script type="text/javascript">

$(document).ready(function() {
	
	${remoteFunction(controller:'assetEntity', action:'getLists', params:'\'entity=\' + "Apps" +\'&dependencyBundle=\'+ null', onComplete:'listUpdate(e)') }
	
	$("#createEntityView").dialog({ autoOpen: false })
	$("#showEntityView").dialog({ autoOpen: false })
	$("#editEntityView").dialog({ autoOpen: false })
	$("#commentsListDialog").dialog({ autoOpen: false })
	$("#createCommentDialog").dialog({ autoOpen: false })
	$("#showCommentDialog").dialog({ autoOpen: false })
	$("#editCommentDialog").dialog({ autoOpen: false })
	$("#manufacturerShowDialog").dialog({ autoOpen: false })
	$("#modelShowDialog").dialog({ autoOpen: false })
	$("#moveBundleSelectId").dialog({ autoOpen: false })  

});
</script>

</head>
<body>
	<input type="hidden" id="redirectTo" name="redirectTo" value="planningConsole" />
	<div class="body">
		<div id="dependencyTitle" style="float: left;">
			<h1>Dependency Console</h1>
			<img id="upArrow" style="display:none; margin-left: 179px; margin-top: -22px; float: left; " src="${createLinkTo(dir:'images',file:'triangle_up.png')}" onclick="collapsePage()"/>
	        <img id="downArrow" style="float:left; margin-left: 179px; margin-top: -22px;" src="${createLinkTo(dir:'images',file:'triangle_down.png')}" onclick="openPage()"/>
		</div>
		 <tds:hasPermission permission='MoveBundleEditView'>
		<div id="checkBoxDiv" style="float: left;margin-top:10px;display: none; margin-left: 50px;">
		<g:form name="checkBoxForm"> 
			<div style="float: left;">
			   <fieldset>
			      <legend>Connection Type:</legend>
					<g:each in="${dependencyType}" var="dependency">
						<input type="checkbox" id="${dependency.value}"
							name="connection" value="${dependency.value}" ${[ 'Batch' ].contains(dependency.value) ? "" : "checked"}/>&nbsp;&nbsp;
							<span id="dependecy_${dependency.id}"> ${dependency.value}</span>
						<br />
					</g:each>
				</fieldset>
			</div>
			&nbsp;
			<div style="float: left;margin-left: 10px;">
            <fieldset>
               <legend>Connection Status:</legend>
					<g:each in="${dependencyStatus}" var="dependencyStatus">
						<input type="checkbox" id="${dependencyStatus.value}"
							name="status" value="${dependencyStatus.value}" ${['Archived','Not Applicable'].contains(dependencyStatus.value) ? '' : 'checked'}/>&nbsp;&nbsp;
							<span id="dependecy_${dependencyStatus.id}"> ${dependencyStatus.value} </span>
						<br />
					</g:each>
            </fieldset>
			</div>
			<div style="float: left;margin-left: 10px;" class="buttonR">
				<input type="button" class="submit"
					style="float: right; margin-top: 50px" value="Generate" onclick="submitCheckBox()" />
			</div>
			
			</g:form>
             
		</div>
		</tds:hasPermission>
		<div style="clear: both;"></div>
		 <tds:hasPermission permission='MoveBundleEditView'>
			<div id = "dependencyBundleDetailsId" >
				<g:render template="dependencyBundleDetails" />
			</div>
		</tds:hasPermission>
		<div style="clear: both;"></div>
		
		<div id="moveBundleSelectId" title="Change Move Bundle" style="background-color: #808080; display: none; float: right" >
		<g:form name="changeBundle" action="saveAssetsToBundle" >
		        
		        <input type="hidden" name="assetVal" id="assetVal" />
		        <input type="hidden" name="assetType" id="assetsTypeId"  />
		        <table style="border: 0px;">
		        <tr>
		           <td style="color:#EFEFEF"> <b> Change MoveBundle for Selected Assets</b></td>
		        </tr>
		        <tr>
		           <td>
		             <g:select name="moveBundleList" from="${moveBundle}" optionKey="id"></g:select><br></br>
		           </td>
		        </tr>
		        <tr>
		         <td style="text-align: right"><input type="button" id ="saveBundleId" name="saveBundle"  value= "save" onclick="submitMoveForm()"> </td>
		        </tr>
		        </table>
          </g:form>
		</div>
		<tds:hasPermission permission='MoveBundleEditView'>
		  <div id="items1" style="display: none"></div>
		</tds:hasPermission>
		<g:render template="../assetEntity/commentCrud" />
		<g:render template="../assetEntity/modelDialog" />
		<div id="createEntityView" style="display: none;"></div>
		<div id="showEntityView" style="display: none;"></div>
		<div id="editEntityView" style="display: none;"></div>
		
		<div style="display: none;">
		<table id="assetDependencyRow">
			<tr>
				<td><g:select name="dataFlowFreq" from="${assetDependency.constraints.dataFlowFreq.inList}"></g:select></td>
				<td><g:select name="entity" from="['Server','Application','Database','Files']" onchange='updateAssetsList(this.name, this.value)'></g:select></td>
				<td><g:select name="asset" from="${servers}" optionKey="id" optionValue="assetName" style="width:90px;"></g:select></td>
				<td><g:select name="dtype" from="${dependencyType.value}"></g:select></td>
				<td><g:select name="status" from="${dependencyStatus.value}"></g:select></td>
			</tr>
	   </table>
       </div>
       <div style="display: none;">
			<span id="Server"><g:select name="asset" from="${servers}" optionKey="id" optionValue="assetName" style="width:90px;"></g:select></span>
			<span id="Application"><g:select name="asset" from="${applications}" optionKey="id" optionValue="assetName" style="width:90px;"></g:select></span>
			<span id="Database"><g:select name="asset" from="${dbs}" optionKey="id" optionValue="assetName" style="width:90px;"></g:select></span>
			<span id="Files"><g:select name="asset" from="${files}" optionKey="id" optionValue="assetName" style="width:90px;"></g:select></span>
	   </div>
	</div>

<script type="text/javascript">
	function getList(value,dependencyBundle, force, distance, labels){
		$('#moveBundleSelectId').dialog("close")
		$('.highlightSpan').css('background-color','#EEEEEE')
		$('.highlightSpan').css('font-weight','normal')
		$('#span_'+dependencyBundle).css('background-color','yellow')
		$('#span_'+dependencyBundle).css('font-weight','bold')
		if(dependencyBundle==null){
			$('#allDependencyBundle').css('background-color','yellow')
			$('#allDependencyBundle').css('font-weight','bold')
		}
		
		switch(value){
		case "server" :
			${remoteFunction(controller:'assetEntity', action:'getLists', params:'\'entity=\' + value +\'&dependencyBundle=\'+ dependencyBundle', onComplete:'listUpdate(e)') }
			break;
		case "Apps" :
			${remoteFunction(controller:'assetEntity', action:'getLists', params:'\'entity=\' + value +\'&dependencyBundle=\'+ dependencyBundle', onComplete:'listUpdate(e)') }
			break;
		case "database" :
			${remoteFunction(controller:'assetEntity', action:'getLists', params:'\'entity=\' + value +\'&dependencyBundle=\'+ dependencyBundle', onComplete:'listUpdate(e)') }
			break;
		case "files" :
			${remoteFunction(controller:'assetEntity', action:'getLists', params:'\'entity=\' + value +\'&dependencyBundle=\'+ dependencyBundle', onComplete:'listUpdate(e)') }
			break;
		case "graph" :
			var labelsList = "apps"
			${remoteFunction(controller:'assetEntity', action:'getLists', params:'\'entity=\' + value +\'&dependencyBundle=\'+ dependencyBundle+\'&force=\'+ force+\'&distance=\'+ distance+\'&labelsList=\'+ labelsList', onComplete:'listUpdate(e)') }
			break;
		}
	}
	function listUpdate(e){
		   var resp = e.responseText;
		   $('#items1').html(resp)
		   $('#items1').css('display','block');
    }
    function collapsePage(){
      $('#checkBoxDiv').hide(300);
      $('#upArrow').css('display','none')
      $('#downArrow').css('display','inline')
    }
    function openPage(){
        $('#checkBoxDiv').show(300)
        $('#upArrow').css('display','inline')
        $('#downArrow').css('display','none')
      }
    function fillView(e){
    	var data = e.responseText
    	$('#item1').html(data)
    }
    function refreshMap(force,distance,friction,height,width){
    	var labelsList = " "
    	$('#labelTree input:checked').each(function() {
    		labelsList += $(this).val()+',';
    	});
    	${remoteFunction(controller:'assetEntity',action:'reloadMap', params:'\'&force=\'+ force+\'&distance=\'+ distance+\'&friction=\'+ friction+\'&height=\'+ height+\'&width=\'+ width+\'&labelsList=\'+ labelsList', onComplete:'fillView(e)' )}
    }
</script>
</body>
</html>