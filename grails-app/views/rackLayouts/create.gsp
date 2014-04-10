<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@page import="com.tds.asset.AssetCableMap"%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="projectHeader" />
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'rackLayout.css')}" />
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'jquery.autocomplete.css')}" />
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.datepicker.css')}" />

<g:javascript src="asset.tranman.js" />
<g:javascript src="room.rack.combined.js"/>
<g:javascript src="cabling.js"/>
<g:javascript src="entity.crud.js" />
<g:javascript src="model.manufacturer.js"/>
<g:javascript src="angular/angular.min.js" />
<g:javascript src="select2.js" />
<g:javascript src="angular/plugins/angular-ui.js"/>
<title>Rack View</title>
<script type="text/javascript">
	function updateRackDetails(e) {
     	var rackDetails = eval('(' + e.responseText + ')')   	
      	var sourceSelectObj = $('#sourceRackIdSelect');
      	var targetSelectObj = $('#targetRackIdSelect');
      	var sourceRacks = rackDetails[0].sourceRackList;
      	var targetRacks = rackDetails[0].targetRackList;
      	generateOptions(sourceSelectObj,sourceRacks,'none');
      	generateOptions(targetSelectObj,targetRacks,'all');
	        
      	var targetList = "${targetRackFilter}"
      	var targetArray =	targetList.split(",")
      	if(sourceList=='none'){
    		$("#sourceRackIdSelect option[value='none']").attr('selected', true);
        }else if(targetArray.length>1 || targetList!=""){
	        for(i=0; i<targetArray.length;i++){
	            var optvalue = targetArray[i].trim();
	            $("#targetRackIdSelect option[value="+optvalue+"]").attr('selected', 'selected');
	            $("#targetRackIdSelect option[value='']").attr('selected', false);
	 	    }
	    }else{
		  $("#targetRackIdSelect option[value='']").attr('selected', 'selected');
		}
		
      	var sourceList = "${sourceRackFilter}"
          
        	var sourceArray =	sourceList.split(",")
        	if(sourceList=='none'){
        		$("#sourceRackIdSelect option[value='none']").attr('selected', true);
            }else if(sourceArray.length>=1 && sourceList!=""){
	  	        for(i=0; i<sourceArray.length;i++){
	  	            var optsourcevalue = sourceArray[i].trim();
	  	            $("#sourceRackIdSelect option[value="+optsourcevalue+"]").attr('selected', 'selected');
	  	            $("#sourceRackIdSelect option[value='']").attr('selected', false);
	  	            $("#sourceRackIdSelect option[value='none']").attr('selected', false);
	  	 	    }
	  	    } else{
        		$("#sourceRackIdSelect option[value='']").attr('selected', 'selected');
        		$("#sourceRackIdSelect option[value='none']").attr('selected', false);
          }
      	/* Start with generated default */
      	$('input[value=Generate]').click();
     }
     function generateOptions(selectObj,racks,sel){
     	if (racks) {
			var length = racks.length
			if(sel == 'none')
				selectObj.html("<option value=''>All</option><option value='none' selected='selected'>None</option>");
			else
				selectObj.html("<option value='' selected='selected'>All</option><option value='none'>None</option>");
			
			racks.map(function(e) {
				var locvalue = e.location ? e.location : 'blank';
				var rmvalue = e.room ? e.room : 'blank';
				var ravalue = e.tag ? e.tag : 'blank';
				return({'value':e.id, 'innerHTML':locvalue +"/"+rmvalue+"/"+ ravalue});
			}).sort(function(a, b) {
				var compA = a.innerHTML;
				var compB = b.innerHTML;
				return (compA < compB) ? -1 : (compA > compB) ? 1 : 0;
			}).each(function(e) {
				var option = document.createElement("option");
				option.value = e.value;
				option.innerHTML = e.innerHTML;
				selectObj.append(option);
			});
      	}
     }
     var reqLoadRack
     function submitForm(form){
     	if($("#bundleId").val() == 'null') {
     		alert("Please select bundle")
     		return false;
     	} else if( !$("#frontView").is(":checked") && !$("#backViewId").is(":checked") ) {
     		alert("Please select print view")
     		return false;
     	} else if($('#commit').val() == 'Generate') {
			$("#cablingDialogId").dialog("close")
			$('#racksLayout').html('Loading...');
		if(reqLoadRack) reqLoadRack.abort();
		reqLoadRack = jQuery.ajax({
				url: $(form).attr('action'),
				data: $(form).serialize(),
				type:'POST',
				success: function(data) {
					getAssignedDetails('rack','');
					$('#racksLayout').html(data);					
				}
			});
	 		return false;
     	}
     }
	$(document).ready(function() {
	    $("#editDialog").dialog({ autoOpen: false })
	    $("#cablingDialogId").dialog({ autoOpen: false })
	    $("#createDialog").dialog({ autoOpen: false })
	    $("#listDialog").dialog({ autoOpen: false })
	    $("#manufacturerShowDialog").dialog({ autoOpen: false })
	    $("#modelShowDialog").dialog({ autoOpen: false })
	    $("#showAssetList").dialog({autoOpen: false})
	   	$("#createEntityView").dialog({autoOpen: false})
	   	$("#showEntityView").dialog({autoOpen: false})
	    $("#editEntityView").dialog({autoOpen: false})
	    $("#commentsListDialog").dialog({ autoOpen: false })
		$("#createCommentDialog").dialog({ autoOpen: false })
	    $("#showCommentDialog").dialog({ autoOpen: false })
	    $("#editCommentDialog").dialog({ autoOpen: false })
	    $("#editManufacturerView").dialog({ autoOpen: false})
	})
	// Script to get the combined rack list
	function getRackDetails( objId ){
        var bundles = new Array()
		$("#"+objId+" option:selected").each(function () {
			bundles.push($(this).val())
       	});
       	
		${remoteFunction(action:'getRackDetails', params:'\'bundles=\' +bundles', onComplete:'updateRackDetails(e)')}
	}
    </script>
</head>
<body>
<div class="body" style="width:98%;">
<g:if test="${flash.message}">
	<div class="message">${flash.message}</div>
</g:if>
<div class="dialog">
<g:form action="save" name="rackLayoutCreate" method="post" target="_blank" onsubmit="return submitForm(this)" style="border: 1px solid black; width: 100%">
<input type="hidden" id="redirectTo" value="rack"/>
<input type="hidden" id="fromRoomOrRack" value="rack"/>
<table style="width:auto; border: none">
	<tbody>
		<tr>
			<td>
				<h1 style="margin: 2px;">Rack View</h1>
				<label><b>Bundle</b></label><br />
				<select id="bundleId" name="moveBundle" multiple="multiple" size="3" onchange="getRackDetails(this.id)" style="width:150px">
					<option value="all" selected="selected">All</option>
					<g:each in="${moveBundleList}" var="moveBundle">
						<option value="${moveBundle?.id}">${moveBundle?.name}</option>
					</g:each>
				</select>
			</td>
			
			<td>
				<label><b>Source</b></label><br />
				<select id="sourceRackIdSelect" multiple="multiple" name="sourcerack" style="width:200px" size="4">
					<option value="null" selected="selected">All</option>
				</select>
			</td>

			<td>
				<div style="width:250px">
					<label><b>Target</b></label><br />
					<select id="targetRackIdSelect" multiple="multiple" name="targetrack" style="width:200px" size="4">
						<option value="null" selected="selected">All</option>
					</select>
				</div>
			</td>
			
			<td>
				<div style="width:150px">
					<label for="frontView" ><input type="checkbox" name="frontView" id="frontView" ${frontCheck ? 'checked="checked"' : '' }/>&nbsp;Front</label>&nbsp
				    <label for="backView" ><input type="checkbox" name="backView" id="backViewId" ${backCheck ? 'checked="checked"' : '' }/>&nbsp;Back</label><br />
				    <label for="bundleName" ><input type="checkbox" name="bundleName" id="bundleNameId" ${wBundleCheck ? 'checked="checked"': '' }/>&nbsp;w/ bundle names</label><br />
				    <label for="otherBundle" ><input type="checkbox" name="otherBundle" id="otherBundleId" ${woBundleCheck ? 'checked="checked"' :'' }/>&nbsp;w/ other bundles</label><br />
					
					<label for="showCabling" ><input type="checkbox" name="showCabling" id="showCabling" ${wDCheck ? 'checked="checked"' :'' }/>&nbsp;w/ diagrams</label><br />
				</div>
			</td>
			
			<td class="buttonR">
				<br /><br />
				<input type="hidden" id="commit" name="commit" value="" />
				<input type="submit" class="submit" value="Generate" id="generateId"/>
			</td>

			<td class="buttonR">
				<br/><br/>
				<input type="submit" class="submit" value="Print View" />
			</td>
		</tr>
	</tbody>
</table>
	</g:form>
</div>
<div style="display: none;" id="cablingDialogId"></div>
<div id="racksLayout" style="width:100%; overflow-x:auto; border: 1px solid black">

</div>
<div id="listDialog" title="Asset List" style="display: none;">
		<div class="dialog" >
			<table id="listDiv">
			</table>
		</div>
</div>

<g:render template="../assetEntity/commentCrud"/>
<g:render template="../assetEntity/modelDialog"/>
<div id ="createEntityView" style="display: none"></div>
<div id ="showEntityView" style="display: none" ></div>
<div id ="editEntityView" style="display: none" ></div>
<div id="editManufacturerView" style="display: none;"></div>
<input type="hidden" id="role" value="role"/>

<g:render template="../assetEntity/newDependency" model="['forWhom':'Server', entities:servers]"></g:render>
</div>
<script type="text/javascript">

	$(document).ready(function() {
		var bundleObj = $("#bundleId");
		var bundle = "${bundle}"
	    var bundleArray = bundle.split(",")
      	if(bundleArray != null && bundleArray != '' && bundleArray != 'all' && bundleArray.size()>0){
	        for(i=0; i < bundleArray.size();i++){
	            var optvalue = bundleArray[i].trim();
	            $("#bundleId option[value="+optvalue+"]").attr('selected', 'selected');
	            $("#bundleId option[value=all]").attr('selected', false);
	 	    }
	    } else {
		    var isCurrentBundle = '${isCurrentBundle}'
		    $("#bundleId option[value='all']").attr('selected', true);	    
			if(isCurrentBundle == "true"){
				bundleObj.val('${currentBundle}');
			}
		}
		var bundleId = bundleObj.val();
		${remoteFunction(action:'getRackDetails', params:'\'bundles=\' + bundleId', onComplete:'updateRackDetails(e)')};
		
		$('input.submit').click(function() {
			$('#commit').val($(this).val());
		});
	});
	
	function createAssetPage(type,source,rack,roomName,location,position){
		${remoteFunction(action:'create',controller:'assetEntity',params:['redirectTo':'rack'], onComplete:'createEntityView(e,type,source,rack,roomName,location,position)')}
	}
	function createBladeDialog(source,blade,position,manufacturer,assetType,assetEntityId, moveBundleId){
		var redirectTo = 'rack'
		new Ajax.Request('../assetEntity/create?redirectTo='+redirectTo+'&assetType='+assetType+'&manufacturer='+manufacturer,{asynchronous:true,evalScripts:true,
				onSuccess:function(e){
	      		    if(e.responseText.substr(0,1) == '{'){
			        	var resp = eval('(' + e.responseText + ')');
			       	 	alert(resp.errMsg)
			        }else{
		           	 	createEntityView(e,'Server');updateAssetBladeInfo(source,blade,position,manufacturer,moveBundleId);
		            }
		        },
				onFailure:function(jqXHR, textStatus, errorThrown){
					alert( "An unexpected error occurred. Please close and reload form to see if the problem persists" )
				}
	    })
	}
</script>
<script>
	currentMenuId = "#racksMenu";
	$("#rackMenuId a").css('background-color','#003366')
</script>
</body>
</html>
