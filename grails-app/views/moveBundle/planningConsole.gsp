<%@page import="com.tds.asset.AssetEntity;com.tds.asset.AssetDependency;"%>
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
		<g:form name="checkBoxForm" action ="generateDependency"> 
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
				<g:actionSubmit class="submit"
					style="float: right; margin-top: 50px" value="Generate" action="generateDependency" />
			</div>
			</g:form>

		</div>
		<div style="clear: both;"></div>
		<g:render template="dependencyBundleDetails" />
		<div style="clear: both;"></div>
		<div id="items1" style="display: none"></div>
		<g:render template="../assetEntity/commentCrud" />
		<g:render template="../assetEntity/modelDialog" />
		<div id="createEntityView" style="display: none;"></div>
		<div id="showEntityView" style="display: none;"></div>
		<div id="editEntityView" style="display: none;"></div>
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