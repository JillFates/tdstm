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
		<span id="newStatusOption" style="display: none;"> 
			<input type="text" id="newplanStatus" name="planStatus" maxlength="20" value=""> 
		</span> 
		<input type="hidden" id="planStatushiddenId" name="hiddenId" value="planStatus" />
		<input type="button" id="addButtonId" name="createAssetPlan" value="EDIT" onclick="addAssetOptions($('#planStatushiddenId').val())"/>
	</div>
</div>

	<script type="text/javascript">
	currentMenuId = "#adminMenu";

	$('.menu-admin-asset-options').addClass('active');
	$('.menu-parent-admin').addClass('active');
	
	// adds the text field for a new asset option to be added, changes the EDIT button to a SAVE button, and adds the option delete buttons
	function addAssetOptions(value){
		var option = value;
		if(option=='planStatus') {
			$("#newStatusOption").show(500);
			$("#addButtonId").val("SAVE");
			$(".deleteStatus").show();
			$("#addButtonId").attr("onClick","submitForm('"+option+"')");
		} else if(value=='Priority') {
			$("#newPriorityOption").show(500);
			$("#addPriorityButtonId").val("SAVE");
			$(".deletePriority").show();
			$("#addPriorityButtonId").attr("onClick","submitForm('"+option+"')");
		} else if(value=='dependency') {
			$("#newDependency").show(500);
			$("#addDependencyButtonId").val("SAVE");
			$(".deleteDependency").show();
			$("#addDependencyButtonId").attr("onClick","submitForm('"+option+"')");
		} else if(value=='dependencyStatus') {
			$("#newDependencyStatus").show(500);
			$("#addDependencyStatusButtonId").val("SAVE");
			$(".deleteDependencyStatus").show();
			$("#addDependencyStatusButtonId").attr("onClick","submitForm('"+option+"')");
		} else if(value=='environment') {
			$("#newEnvironment").show(500);
			$("#addEnvironmentButtonId").val("SAVE");
			$(".deleteEnvironment").show();
			$("#addEnvironmentButtonId").attr("onClick","submitForm('"+option+"')");
		} else if(value=='assetType') {
			$("#newAssetType").show(500);
			$("#addAssetTypeButtonId").val("SAVE");
			$(".deleteAssetType").show();
			$("#addAssetTypeButtonId").attr("onClick","submitForm('"+option+"')");
		}
	}
	
	// saves the new asset option
	function submitForm(option){
			var planStatus = $("#newplanStatus").val();
			var priorityOption = $("#priorityOption").val();
			var dependencyType = $("#dependencyType").val();
			var dependencyStatus = $("#dependencyStatus").val();
			var environment = $("#environment").val();
			var assetType = $("#assetType").val();

			if (option=='planStatus' && planStatus) {
				${remoteFunction(action:'saveAssetoptions', params:'\'planStatus=\'+ encodeURIComponent(planStatus)+\'&assetOptionType=\'+"planStatus" ', onSuccess:'addAssetOption(data,planStatus,option)')};
			} else if(option=='Priority' && priorityOption) {
				${remoteFunction(action:'saveAssetoptions', params:'\'priorityOption=\'+ encodeURIComponent(priorityOption) +\'&assetOptionType=\'+"Priority"', onSuccess:'addAssetOption(data,priorityOption,option)')};
			} else if(option=='dependency' && dependencyType) {
				${remoteFunction(action:'saveAssetoptions', params:'\'dependencyType=\'+ encodeURIComponent(dependencyType) +\'&assetOptionType=\'+"dependency" ', onSuccess:'addAssetOption(data,dependencyType,option)')};
			} else if(option=='dependencyStatus' && dependencyStatus) {
				${remoteFunction(action:'saveAssetoptions', params:'\'dependencyStatus=\'+ encodeURIComponent(dependencyStatus) +\'&assetOptionType=\'+"dependencyStatus"', onSuccess:'addAssetOption(data,dependencyStatus,option)')};
			} else if(option=='assetType' && assetType) {
				${remoteFunction(action:'saveAssetoptions', params:'\'assetType=\'+ encodeURIComponent(assetType) +\'&assetOptionType=\'+"assetType"', onSuccess:'addAssetOption(data,assetType,option)')};
			} else if(option=='environment' && environment) {
				if (environment.length <= 20) {
					${remoteFunction(action:'saveAssetoptions', params:'\'environment=\'+ encodeURIComponent(environment) +\'&assetOptionType=\'+"environment"', onSuccess:'addAssetOption(data,environment,option)')};
				} else {
                  	option = camelCaseToRegularForm(option);
					alert(option + " can't have more than 20 characters.")
				}
			} else {
			  	option = camelCaseToRegularForm(option);
				alert(option + " can't be blank.")
			}
	}

    /**
	 * Transform a Camel Case string to a Normal Form
	 * e.g. thisIsGood => This Is Good
     * @param str
     */
	function camelCaseToRegularForm(str) {
	  return str
      		// insert a space before all caps
    		.replace(/([A-Z])/g, ' $1').trim()
      		// uppercase the first character
      		.replace(/^./, function(str){ return str.toUpperCase(); });
	}
	
	// add an asset option to the table specified by @param option (This only affects the UI)
	function addAssetOption(data,value,option){
		var option = option;
		if (data.id == null) {
			// if a nonunique asset option was submitted, give the user an alert and don't add it to the UI
			alert("All Asset Options must be unique")
		} else if (option=='planStatus') {
			var id = data.id;
			var planStatusValue = value;
			$("#planStatusTbodyId").append("<tr id='planStatus_"+id+"' style='cursor: pointer;'><td>"+planStatusValue+"</td><td><span class=\'deleteStatus clear_filter'\  onClick=\"deleteAssetStatus(\'"+id+"','"+option+"')\" ><b>X</b></span></td></tr>")
			$("#newplanStatus").val("");
		} else if(option=='Priority') {
 			var id = data.id;
 			var priorityOptionValue = value;
 			$("#priorityStatusTbodyId").append("<tr id='priorityOption_"+id+"' style='cursor: pointer;'><td>"+priorityOptionValue+"</td><td><span class=\'deletePriority clear_filter'\ onClick=\"deleteAssetStatus(\'"+id+"','"+option+"')\" ><b>X</b></span></td></tr>")
 			$("#priorityOption").val("");
		} else if(option=='dependency') {
 			var id = data.id;
 			var dependencyTypeValue = value;
 			$("#dependencyTypeTbodyId").append("<tr id='dependencyType_"+id+"' style='cursor: pointer;'><td>"+dependencyTypeValue+"</td><td><span class=\'deleteDependency clear_filter'\ onClick=\"deleteAssetStatus(\'"+id+"','"+option+"')\" ><b>X</b></span></td></tr>")
 			$("#dependencyType").val("");
			sortTable($("#dependencyTypeTableId")[0]);
		} else if(option=='dependencyStatus') {
 			var id = data.id;
 			var dependencyStatusValue = value;
 			$("#dependencyStatusTbodyId").append("<tr id='dependencyStatus_"+id+"' style='cursor: pointer;'><td>"+dependencyStatusValue+"</td><td><span class=\'deleteDependencyStatus clear_filter'\ onClick=\"deleteAssetStatus(\'"+id+"','"+option+"')\" ><b>X</b></span></td></tr>")
 			$("#dependencyStatus").val("");
		} else if(option=='environment') {
 			var id = data.id;
 			var environmentValue = value;
 			$("#envOptionTbodyId").append("<tr id='environment_"+id+"' style='cursor: pointer;'><td>"+environmentValue+"</td><td><span class=\'deleteEnvironment clear_filter'\ onClick=\"deleteAssetStatus(\'"+id+"','"+option+"')\" ><b>X</b></span></td></tr>")
 			$("#environment").val("");
			sortTable($("#envOptionTableId")[0]);
		} else if(option=='assetType') {
            var id = data.id;
            var assetTypeValue = value;
            $("#assetTypeTbodyId").append("<tr id='assetType_"+id+"' style='cursor: pointer;'><td>"+assetTypeValue+"</td><td><span class=\'deleteAssetType clear_filter'\ onClick=\"deleteAssetStatus(\'"+id+"','"+option+"')\" ><b>X</b></span></td></tr>")
            $("#assetType").val("");
            sortTable($("#assetTypeTableId")[0]);
		}

	}

	//Simple function to sort the tables.
	function sortTable(table) {
		var tbody = $('tbody', table);

		tbody.find('tr').sort(function (a, b) {
			return $('td:first', a).text().localeCompare($('td:first', b).text());
		}).appendTo(tbody);
	}

	// invokes the controller method to delete the asset option then calls fillAssetOptions to update the UI if the call was successful
	function deleteAssetStatus(id,option){
		 if(option=='planStatus') {
			 var id = id;
			 ${remoteFunction(action:'deleteAssetOptions', params:'\'assetStatusId=\'+ id +\'&assetOptionType=\'+"planStatus" ', onSuccess:'fillAssetOptions(id,option)')};
		 } else if(option=='Priority') {
			 var id = id
			 ${remoteFunction(action:'deleteAssetOptions', params:'\'priorityId=\'+ id +\'&assetOptionType=\'+"Priority"', onSuccess:'fillAssetOptions(id,option)')};
		 } else if(option=='dependency') {
			 var id = id
			 ${remoteFunction(action:'deleteAssetOptions', params:'\'dependecyId=\'+ id +\'&assetOptionType=\'+"dependency"', onSuccess:'fillAssetOptions(id,option)')};
		 } else if(option=='dependencyStatus') {
			 var id = id
			 ${remoteFunction(action:'deleteAssetOptions', params:'\'dependecyId=\'+ id +\'&assetOptionType=\'+"dependencyStatus"', onSuccess:'fillAssetOptions(id,option)')};
		 } else if(option=='environment') {
			 var id = id
			 ${remoteFunction(action:'deleteAssetOptions', params:'\'environmentId=\'+ id +\'&assetOptionType=\'+"environment"', onSuccess:'fillAssetOptions(id,option)')};
		 } else if (option == 'assetType') {
			 var id = id
			 ${remoteFunction(action:'deleteAssetOptions', params:'\'assetTypeId=\'+ id +\'&assetOptionType=\'+"assetType"', onSuccess:'fillAssetOptions(id,option)')};
		 }
	}
	
	// removes an asset option from the table specified by @param option (This only affects the UI)
	function fillAssetOptions(id,option){
		if(option=='planStatus') {
			$('#planStatus_'+id).remove();
		} else if(option=='Priority') {
			$('#priorityOption_'+id).remove();
		} else if(option=='dependency') {
			$('#dependencyType_'+id).remove();
		} else if(option=='dependencyStatus') {
			$('#dependencyStatus_'+id).remove();
		} else if(option=='environment') {
			$('#environment_'+id).remove();
		} else if (option == 'assetType') {
			$('#assetType_' + id).remove();
		}
	}
	</script>
