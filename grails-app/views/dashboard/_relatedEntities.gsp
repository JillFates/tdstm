<g:render template="../assetEntity/commentCrud" model="['servers':servers, 'applications':applications, 'dbs':dbs, 'files':files]"/>
<g:render template="../assetEntity/newDependency" model="['forWhom':'Server', entities:servers, 'servers':servers,
	 'applications':applications, 'dbs':dbs, 'files':files, 'dependencyType':dependencyType, dependencyStatus:dependencyStatus,
	 'moveBundleList':moveBundleList]"></g:render>
<g:render template="../assetEntity/modelDialog"/>
<div id="showEntityView" style="display: none;"></div>
<div id="editEntityView" style="display: none;"></div>
<div id="editManufacturerView" style="display: none;"></div>
<div id="createEntityView" style="display: none;"></div>
<div id="cablingDialogId" style="display: none;"></div>
	
<script>
$(document).ready(function() {
		
		$("#showEntityView").dialog({ autoOpen: false })
		$("#createEntityView").dialog({ autoOpen: false })
		$("#editEntityView").dialog({ autoOpen: false })
		$("#manufacturerShowDialog").dialog({ autoOpen: false })
		$("#modelShowDialog").dialog({ autoOpen: false })
		$("#showCommentDialog").dialog({ autoOpen: false })
		$("#editCommentDialog").dialog({ autoOpen: false })
		$("#editManufacturerView").dialog({ autoOpen: false})
		$("#createCommentDialog").dialog({ autoOpen: false })
		$("#cablingDialogId").dialog({ autoOpen:false })
	});
</script>