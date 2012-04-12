<%@page import="com.tds.asset.AssetEntity;"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
<meta name="layout" content="projectHeader" />
<title>Planning Console</title>
<g:javascript src="asset.tranman.js" />
<g:javascript src="entity.crud.js" />
<script type="text/javascript">
$(document).ready(function() {
	$("#createEntityView").dialog({ autoOpen: false })
	$("#showEntityView").dialog({ autoOpen: false })
	$("#editEntityView").dialog({ autoOpen: false })
    $("#commentsListDialog").dialog({ autoOpen: false })
    $("#createCommentDialog").dialog({ autoOpen: false })
    $("#showCommentDialog").dialog({ autoOpen: false })
    $("#editCommentDialog").dialog({ autoOpen: false })
    $("#manufacturerShowDialog").dialog({ autoOpen: false })
    $("#modelShowDialog").dialog({ autoOpen: false })
     
    
});


</script>

</head>
<body>
	<input type="hidden" id="redirectTo" name="redirectTo" value="planningConsole" />
	<div class="body">
		<div style="float: left;">
			<h1>Dependency Console</h1>
		</div>
		<div style="float: left; margin-left: 50px;">
		<g:form name="checkBoxForm"> 
			<div style="float: left;">
				<h3>Connection Type</h3>
				<g:each in="${dependencyType}" var="dependency">
					<input type="checkbox" id="${dependency.value}"
						name="connection" value="${dependency.value} " checked="checked"/>&nbsp;&nbsp;<span
						id="dependecy_${dependency.id}" > ${dependency.value} </span>
					<br />
				</g:each>
			</div>
			&nbsp;
			<div style="float: left;margin-left: 10px;">
				<h3>Connection Status</h3>
				<g:each in="${dependencyStatus}" var="dependencyStatus">
					<input type="checkbox" id="${dependencyStatus.value}"
						name="status" value="${dependencyStatus.value} " checked="checked"/>&nbsp;&nbsp;<span
						id="dependecy_${dependencyStatus.id}" > ${dependencyStatus.value} </span>
					<br />
				</g:each>
			</div>
			<div style="float: left;margin-left: 10px;" class="buttonR">
				<input type="button" class="submit"
					style="float: right; margin-top: 50px" value="Generate" onclick="submitCheckBox()" />
			</div>
			</g:form>

		</div>
		<div style="clear: both;"></div>
		
		<div id = "dependencyBundleDetailsId" >
			<g:render template="dependencyBundleDetails" />
		</div>
		<div style="clear: both;"></div>
		<div id="items1" style="display: none"></div>
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
	function getList(value,dependencyBundle){
		if(value=='Apps'){
			var app = 'Apps'
			${remoteFunction(controller:'assetEntity', action:'getLists', params:'\'entity=\' + app +\'&dependencyBundle=\'+ dependencyBundle', onComplete:'listUpdate(e)') }
		}else if(value=='server'){
			var server = 'server'
			${remoteFunction(controller:'assetEntity', action:'getLists', params:'\'entity=\' + server +\'&dependencyBundle=\'+ dependencyBundle', onComplete:'listUpdate(e)') }
		}
		else if(value=='database'){
			var server = 'database'
			${remoteFunction(controller:'assetEntity', action:'getLists', params:'\'entity=\' + server +\'&dependencyBundle=\'+ dependencyBundle', onComplete:'listUpdate(e)') }
		}else{
			var server = 'files'
			${remoteFunction(controller:'assetEntity', action:'getLists', params:'\'entity=\' + server +\'&dependencyBundle=\'+ dependencyBundle', onComplete:'listUpdate(e)') }
		}
	}
	function listUpdate(e){
		   var resp = e.responseText;
		   $('#items1').html(resp)
		   $('#items1').css('display','block');
    }
</script>
</body>
</html>