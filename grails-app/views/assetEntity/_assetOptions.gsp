<div class="body">
	<g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
	</g:if>
	<div style="margin-left: 50px;">
		<table style="margin-top: 89px;">
			<thead>
				<tr>
					<th colspan="2"><h3>Asset Plan Status</h3></th>
				</tr>
			</thead>
			<tbody id="planStatusTbodyId">
				<g:each in="${planStatusOptions}" status="i" var="planStatus">
					<tr id="planStatus_${planStatus.id}">
						<td>${planStatus.value}</td>
						<td><span class=" deleteStatus clear_filter"
							style="display: none; cursor: pointer;"
							onClick="deleteAssetStatus(${planStatus.id},$('#planStatushiddenId').val())"><b>X</b>
						</span></td>
					</tr>
				</g:each>
		</table>
		<span id="newStatusOption" style="display: none;"> <input
			type="text" id="newplanStatus" name="planStatus" value=""> </span> <input
			type="hidden" id="planStatushiddenId" name="hiddenId"
			value="planStatus" /> <input type="button" id="addButtonId"
			name="createAssetPlan" value="EDIT"
			onclick="addAssetOptions($('#planStatushiddenId').val())"/>
	</div>
</div>
	<script type="text/javascript">
	currentMenuId = "#adminMenu";
	$("#adminMenuId a").css('background-color','#003366')
    function addAssetOptions(value){
        var option = value;
        if(option=='planStatus'){
	    	$("#newStatusOption").show(500);
	    	$("#addButtonId").val("SAVE");
	    	$(".deleteStatus").show();
	    	$("#addButtonId").attr("onClick","submitForm('"+option+"')");
        }else if(value=='Priority'){
        	$("#newPriorityOption").show(500);
        	$("#addPriorityButtonId").val("SAVE");
        	$(".deletePriority").show();
        	$("#addPriorityButtonId").attr("onClick","submitForm('"+option+"')");
	    }else if(value=='dependency'){
	    	$("#newDependency").show(500);
	    	$("#addDependencyButtonId").val("SAVE");
	    	$(".deleteDependency").show();
	    	$("#addDependencyButtonId").attr("onClick","submitForm('"+option+"')");
	    }
	    else if(value=='dependencyStatus'){
	    	$("#newDependencyStatus").show(500);
	    	$("#addDependencyStatusButtonId").val("SAVE");
	    	$(".deleteDependencyStatus").show();
	    	$("#addDependencyStatusButtonId").attr("onClick","submitForm('"+option+"')");
	    }
	    else if(value=='environment'){
	    	$("#newEnvironment").show(500);
	    	$("#addEnvironmentButtonId").val("SAVE");
	    	$(".deleteEnvironment").show();
	    	$("#addEnvironmentButtonId").attr("onClick","submitForm('"+option+"')");
	    }
    }

    function submitForm(option){
	        var planStatus = $("#newplanStatus").val();
	        var priorityOption = $("#priorityOption").val();
	        var dependencyType = $("#dependencyType").val();
	        var dependencyStatus = $("#dependencyStatus").val();
	        var environment = $("#environment").val();
	        
	        if(option=='planStatus' && planStatus){
		        ${remoteFunction(action:'saveAssetoptions', params:'\'planStatus=\'+ planStatus+\'&assetOptionType=\'+"planStatus" ', onSuccess:'addAssetOption(e,planStatus,option)')};
	        }else if(option=='Priority' && priorityOption){
	        	${remoteFunction(action:'saveAssetoptions', params:'\'priorityOption=\'+ priorityOption +\'&assetOptionType=\'+"Priority"', onSuccess:'addAssetOption(e,priorityOption,option)')};
	        } else if(option=='dependency' && dependencyType){
	        	${remoteFunction(action:'saveAssetoptions', params:'\'dependencyType=\'+ dependencyType +\'&assetOptionType=\'+"dependency" ', onSuccess:'addAssetOption(e,dependencyType,option)')};
	        }else if(option=='dependencyStatus' && dependencyStatus){
	        	${remoteFunction(action:'saveAssetoptions', params:'\'dependencyStatus=\'+ dependencyStatus +\'&assetOptionType=\'+"dependencyStatus"', onSuccess:'addAssetOption(e,dependencyStatus,option)')};
	        }else if(option=='environment' && environment){
	        	${remoteFunction(action:'saveAssetoptions', params:'\'environment=\'+ environment +\'&assetOptionType=\'+"environment"', onSuccess:'addAssetOption(e,environment,option)')};
	        }else{
    			alert(option+" can't be blank.")
    		}
    }
    
    function addAssetOption(e,value,option){
        var option = option;
    	 if(option=='planStatus'){
	    	var data = eval('(' + e.responseText + ')');
	    	var id = data.id;
	    	var planStatusValue = value;
	    	$("#planStatusTbodyId").append("<tr id='planStatus_"+id+"' style='cursor: pointer;'><td>"+planStatusValue+"</td><td><span class=\'deleteStatus clear_filter'\  onClick=\"deleteAssetStatus(\'"+id+"','"+option+"')\" ><b>X</b></span></td></tr>")
	    	$("#newplanStatus").val("");
    	 }else if(option=='Priority'){
    		var data = eval('(' + e.responseText + ')');
 	    	var id = data.id;
 	    	var priorityOptionValue = value;
 	    	$("#priorityStatusTbodyId").append("<tr id='priorityOption_"+id+"' style='cursor: pointer;'><td>"+priorityOptionValue+"</td><td><span class=\'deletePriority clear_filter'\ onClick=\"deleteAssetStatus(\'"+id+"','"+option+"')\" ><b>X</b></span></td></tr>")
 	    	$("#priorityOption").val("");
         }else if(option=='dependency'){
    		var data = eval('(' + e.responseText + ')');
 	    	var id = data.id;
 	    	var dependencyTypeValue = value;
 	    	$("#dependencyTypeTbodyId").append("<tr id='dependencyType_"+id+"' style='cursor: pointer;'><td>"+dependencyTypeValue+"</td><td><span class=\'deleteDependency clear_filter'\ onClick=\"deleteAssetStatus(\'"+id+"','"+option+"')\" ><b>X</b></span></td></tr>")
 	    	$("#dependencyType").val("");
         }else if(option=='dependencyStatus'){
    		var data = eval('(' + e.responseText + ')');
 	    	var id = data.id;
 	    	var dependencyStatusValue = value;
 	    	$("#dependencyStatusTbodyId").append("<tr id='dependencyStatus_"+id+"' style='cursor: pointer;'><td>"+dependencyStatusValue+"</td><td><span class=\'deleteDependencyStatus clear_filter'\ onClick=\"deleteAssetStatus(\'"+id+"','"+option+"')\" ><b>X</b></span></td></tr>")
 	    	$("#dependencyStatus").val("");
         }else if(option=='environment'){
    		var data = eval('(' + e.responseText + ')');
 	    	var id = data.id;
 	    	var environmentValue = value;
 	    	$("#envOptionTbodyId").append("<tr id='environment_"+id+"' style='cursor: pointer;'><td>"+environmentValue+"</td><td><span class=\'deleteEnvironment clear_filter'\ onClick=\"deleteAssetStatus(\'"+id+"','"+option+"')\" ><b>X</b></span></td></tr>")
 	    	$("#environment").val("");
         }
    }
    function deleteAssetStatus(id,option){
    	 if(option=='planStatus'){
		     var id = id;
		     ${remoteFunction(action:'deleteAssetOptions', params:'\'assetStatusId=\'+ id +\'&assetOptionType=\'+"planStatus" ', onSuccess:'fillAssetOptions(id,option)')};
    	 }else if(option=='Priority'){
    		 var id = id
		     ${remoteFunction(action:'deleteAssetOptions', params:'\'priorityId=\'+ id +\'&assetOptionType=\'+"Priority"', onSuccess:'fillAssetOptions(id,option)')};
    	 }else if(option=='dependency'){
    		 var id = id
		     ${remoteFunction(action:'deleteAssetOptions', params:'\'dependecyId=\'+ id +\'&assetOptionType=\'+"dependency"', onSuccess:'fillAssetOptions(id,option)')};
    	 }else if(option=='dependencyStatus'){
    		 var id = id
		     ${remoteFunction(action:'deleteAssetOptions', params:'\'dependecyId=\'+ id +\'&assetOptionType=\'+"dependencyStatus"', onSuccess:'fillAssetOptions(id,option)')};
    	 }else if(option=='environment'){
    		 var id = id
		     ${remoteFunction(action:'deleteAssetOptions', params:'\'environmentId=\'+ id +\'&assetOptionType=\'+"environment"', onSuccess:'fillAssetOptions(id,option)')};
    	 }
    }
    function fillAssetOptions(id,option){
    	 if(option=='planStatus'){
    	   $('#planStatus_'+id).remove();
    	 }else if(option=='Priority'){
    	   $('#priorityOption_'+id).remove();
    	 }else if(option=='dependency'){
    	   $('#dependencyType_'+id).remove();
    	 }else if(option=='dependencyStatus'){
    	   $('#dependencyStatus_'+id).remove();
    	 }else if(option=='environment'){
    	   $('#environment_'+id).remove();
    	 }
    }
	</script>