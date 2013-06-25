<%@page import="com.tds.asset.AssetEntity;"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
<meta name="layout" content="projectHeader" />
<title>Dependency Console</title>
<g:javascript src="asset.tranman.js" />
<g:javascript src="entity.crud.js" />
<g:javascript src="model.manufacturer.js"/>
<script type="text/javascript" src="${resource(dir:'d3',file:'d3.v2.js')} "></script>
<link rel="stylesheet" href="${resource(dir:'d3/force',file:'force.css')}" type="text/css"/>
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.datepicker.css')}" />

<script type="text/javascript">

$(document).ready(function() {
	
	${remoteFunction(controller:'assetEntity', action:'getLists', params:'\'entity=\' + "Apps" +\'&dependencyBundle=\'+ null', onComplete:'listUpdate(e)') }
	$("#checkBoxDiv").dialog({ autoOpen: false, resizable: false })
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
	$("#editManufacturerView").dialog({ autoOpen: false})
	currentMenuId = "#assetMenu";
	$("#assetMenuId a").css('background-color','#003366')	

});
</script>
</head>
<body>
	<input type="hidden" id="redirectTo" name="redirectTo" value="dependencyConsole" />
	<div class="body">
		<div id="dependencyTitle" style="float: left;">
			<h1>Dependency Console</h1>
		</div>
		 <tds:hasPermission permission='MoveBundleEditView'>
		 <div id="checkBoxDiv"  title="Dependency Grouping Control">
		<div id="checkBoxDivId">
		<g:form name="checkBoxForm"> 
			<div style="float: left; margin-left:18px;">
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
			<div class="buttonR">
				<input type="button" class="submit"
					style="margin-top: 40px; margin-left: -27px;" value="Generate" onclick="submitCheckBox()" />
			</div>
			
			</g:form>
         </div> 
		</div>
		</tds:hasPermission>
		<div style="clear: both;"></div>
		 <tds:hasPermission permission='MoveBundleEditView'>
			<div id = "dependencyBundleDetailsId" >
				<g:render template="dependencyBundleDetails" />
			</div>
		</tds:hasPermission>
		<div style="clear: both;"></div>
		
		<div id="moveBundleSelectId" title="Assignment" style="background-color: #808080; display: none; float: right" >
		<g:form name="changeBundle" action="saveAssetsToBundle" >
		        
		        <input type="hidden" name="assetVal" id="assetVal" />
		        <input type="hidden" name="assetType" id="assetsTypeId"  />
		        <table style="border: 0px;">
		        <tr>
		           <td style="color:#EFEFEF ; width: 260px"> <b> Assign selected assets to :</b></td>
		        </tr>
		        <tr>
		           <td>
		             <span style="color:#EFEFEF "><b>Bundle</b></span> &nbsp;&nbsp;<g:select name="moveBundleList" id="plannedMoveBundleList" from="${moveBundle}" optionKey="id"></g:select><br></br>
		           </td>
		        </tr>
		        <tr>
		           <td>
		             <span style="color:#EFEFEF "><b>Plan Status</b></span> &nbsp;&nbsp;<g:select name="planStatus" id="plannedStatus" from="${planStatusOptions}" optionKey="value" optionValue="value"></g:select><br></br>
		           </td>
		        </tr>
		        <tr>
		           <td>
		            <div>
						<label for="planningBundle" ><input type="radio" name="bundles" id="planningBundle" value="planningBundle" checked="checked" onChange="changeBundles(this.id)" />&nbsp;<span style="color:#EFEFEF "><b>Planning Bundles</b></span></label>
						<label for="allBundles" ><input type="radio" name="bundles" id="allBundles" value="allBundles" onChange="changeBundles(this.id)" />&nbsp;<span style="color:#EFEFEF "><b>All Bundles</b></span></label><br />
					</div>
			   </td>
		        </tr>
		        <tr>
		         <td style="text-align: left"><input type="button" id ="saveBundleId" name="saveBundle"  value= "Assign" onclick="submitMoveForm()"> </td>
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
		<div id="editManufacturerView" style="display: none;"></div>
		<div style="display: none;">
		  <g:select name="moveBundleList" id="moveBundleList_all" from="${allMoveBundles}" optionKey="id"></g:select><br></br>
		  <g:select name="moveBundleList" id="moveBundleList_planning" from="${moveBundle}" optionKey="id"></g:select><br></br>
		</div>
		<div style="display: none;">
		<table id="assetDependencyRow">
			<tr>
				<td><g:select name="dataFlowFreq" from="${assetDependency.constraints.dataFlowFreq.inList}"></g:select></td>
				<td><g:select name="entity" from="['Server','Application','Database','Storage','Network']" onchange='updateAssetsList(this.name, this.value)'></g:select></td>
				<td><span id="Server"><g:select name="asset" from="${servers}" optionKey="${-2}" optionValue="${1}" style="width:90px;"></g:select></span></td>
				<td><g:select name="dtype" from="${dependencyType.value}"></g:select></td>
				<td><g:select name="status" from="${dependencyStatus.value}"></g:select></td>
			</tr>
	   </table>
       </div>
       <div style="display: none;">
			<span id="Application"><g:select name="asset" from="${applications}" optionKey="${-2}" optionValue="${1}" style="width:90px;"></g:select></span>
			<span id="Database"><g:select name="asset" from="${dbs}" optionKey="${-2}" optionValue="${1}" style="width:90px;"></g:select></span>
			<span id="Storage"><g:select name="asset" from="${files}" optionKey="${-2}" optionValue="${1}" style="width:90px;"></g:select></span>
			<span id="Network"><g:select name="asset" from="${networks}" optionKey="${-2}" optionValue="${1}" style="width:90px;"></g:select></span>
	   </div>
	</div>

<script type="text/javascript">
	function getList(value,dependencyBundle, force, distance, labels){
		$('#moveBundleSelectId').dialog("close")
		var id = 'all'
		if(dependencyBundle != null) id = dependencyBundle
		
		$('.highlightSpan').removeClass('highlightSpan')
		$('.app_count').removeClass('app_count')
		$('.server_count').removeClass('server_count')
		$('.vm_count').removeClass('vm_count')
		$('.db_count').removeClass('db_count')
		$('.file_count').removeClass('file_count')
		
		$('#span_'+id).addClass('highlightSpan')
		$('#app_'+id).addClass('app_count')
		$('#server_'+id).addClass('server_count')
		$('#vm_'+id).addClass('vm_count')
		$('#db_'+id).addClass('db_count')
		$('#file_'+id).addClass('file_count')
		
		
		 
		switch(value){
		case "server" :
			$('#assetCheck').attr('checked','false')
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
    function changeBundles(id){
        if(id=="allBundles"){
        	$("#plannedMoveBundleList").html($("#moveBundleList_all").html())
        }else{
        	$("#plannedMoveBundleList").html($("#moveBundleList_planning").html())
        }
    }
</script>
</body>
</html>
