<div class="body">
	<h1>Administrative Setting</h1>
	<g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
	</g:if>
	<div>
			<table>
				<thead>
					<tr>
						<th colspan="2"><h3>Asset Plan Status</h3>
						</th>
					</tr>
				</thead>
				<tbody id="planStatusTbodyId">
					<g:each in="${planStatusOptions}" status="i" var="planStatus">
						<tr id="planStatus_${planStatus.id}">
							<td>${planStatus.value}</td>
							<td><span class=" deleteStatus clear_filter"
								style="display: none;cursor: pointer;"
								onClick="deleteAssetStatus(${planStatus.id})"><b>X</b>
							</span>
							</td>
						</tr>
					</g:each>
			</table>
			<span id="newStatusOption" style="display: none;"> <input
				type="text" id="newplanStatus" name="planStatus" value="">
			</span>
			<input type="button" id="addButtonId" name="createAssetPlan"
				value="EDIT" onclick="addAssetPlanStatus()">
	</div>

	<script type="text/javascript">

	
		 
    function addAssetPlanStatus(){
    	$("#newStatusOption").show(500);
    	$("#addButtonId").val("SAVE");
    	$(".deleteStatus").show();
    	$("#addButtonId").attr("onClick","submitForm()");
    }
    function submitForm(){
        var planStatus = $("#newplanStatus").val()
        ${remoteFunction(action:'saveAssetoptions', params:'\'planStatus=\'+ planStatus ', onSuccess:'addAssetOption(e,planStatus)')};
    }
    function addAssetOption(e,planStatus){
    	var data = eval('(' + e.responseText + ')');
    	var id = data.id;
    	var planStatus = planStatus;
    	$("#planStatusTbodyId").append("<tr id='planStatus_"+id+"' style='cursor: pointer;'><td>"+planStatus+"</td><td><span class='deleteStatus clear_filter' + onClick='deleteAssetStatus("+id+")' ><b>X</b></span></td></tr>")
    	$("#newplanStatus").val("");
    }
    function deleteAssetStatus(id){
	     var id = id
	     ${remoteFunction(action:'deleteAssetStatus', params:'\'assetStatusId=\'+ id ', onComplete:'fillAssetOptions(id)')};
    }
    function fillAssetOptions(id){
    	$('#planStatus_'+id).remove();
    }
    

	</script>