<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>Asset</title>
<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'main.css')}" />
<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'tds.css')}" />
<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'qvga.css')}" />
<link rel="shortcut icon"
	href="${createLinkTo(dir:'images',file:'tds.ico')}" type="image/x-icon" />
	<link type="text/css" rel="stylesheet"
	href="${createLinkTo(dir:'css',file:'jquery.autocomplete.css')}" />
<link type="text/css" rel="stylesheet"
	href="${createLinkTo(dir:'css',file:'ui.accordion.css')}" />
<link type="text/css" rel="stylesheet"
	href="${createLinkTo(dir:'css',file:'ui.core.css')}" />
<link type="text/css" rel="stylesheet"
	href="${createLinkTo(dir:'css',file:'ui.move_tech_dialog.css')}" />
<link type="text/css" rel="stylesheet"
	href="${createLinkTo(dir:'css',file:'ui.resizable.css')}" />
<link type="text/css" rel="stylesheet"
	href="${createLinkTo(dir:'css',file:'ui.slider.css')}" />
<link type="text/css" rel="stylesheet"
	href="${createLinkTo(dir:'css',file:'ui.theme.css')}" />

<g:javascript library="application" />
<g:javascript library="prototype" />
<g:javascript library="jquery" />
<jq:plugin name="jquery.bgiframe.min"/>
<jq:plugin name="jquery.autocomplete"/>
<jq:plugin name="ui.core" />
<jq:plugin name="ui.draggable" />
<jq:plugin name="ui.resizable" />
<jq:plugin name="ui.dialog" /> 
	
<g:javascript library="application" />

<script>
	      $(document).ready(function() {
	        $("#serverInfoDialog").dialog({ autoOpen: false })
	       
	      })
</script>

  <script type="text/javascript">
    
        function serverInfo(e){
             
        var loc = document.assetSearchForm.location.value;
      var location;
        var room;
        var rack;
        var pos;     
       
        var asset = eval('(' + e.responseText + ')');
          if(loc == 's'){
        location = asset[0].item.sourceLocation
        room = asset[0].item.sourceRoom
         rack = asset[0].item.sourceRack
         pos = asset[0].item.sourceRackPosition
          }else{
          location = asset[0].item.targetLocation
        room = asset[0].item.targetRoom
         rack = asset[0].item.targetRack
         pos = asset[0].item.targetRackPosition
          }     
        var htmlBody = '<table ><thead></thead><tbody>'+
'<tr><td style="font-size:9px"><b>Asset Tag:</b>  '+asset[0].item.assetTag+'</td></tr>'+
'<tr><td style="font-size:9px"><b>Asset Name:</b>  '+asset[0].item.assetName+'</td></tr>'+
'<tr><td style="font-size:9px"><b>Current State:</b>  '+asset[0].state+'</td></tr>'+
'<tr><td style="font-size:9px"><b>Serial Number:</b>  '+asset[0].item.serialNumber+'</td></tr>'+
'<tr><td style="font-size:9px"><b>Model:</b>  '+asset[0].item.model+'</td></tr>'+
'<tr><td style="font-size:9px"><b>Location:</b>  '+location+'</td></tr>'+
'<tr><td style="font-size:9px"><b>Room:</b>  '+room+'</td></tr>'+
'<tr><td style="font-size:9px"><b>Rack/Position:</b>  '+rack+'/'+pos+'</td></tr>'+
'<tr><td style="font-size:9px"><b>PDU:</b>  '+asset[0].item.powerPort+'</td></tr>'+
'<tr><td style="font-size:9px"><b>NIC:</b>  '+asset[0].item.nicPort+'</td></tr>'+
'<tr><td style="font-size:9px"><b>HBA:</b>  '+asset[0].item.hbaPort+'</td></tr>'+
'</tbody></table>' 
        var getDialogId = document.getElementById('serverInfoDialog')
        getDialogId.innerHTML = htmlBody
         $("#serverInfoDialog").dialog('option', 'width', 200)                     
		 $("#serverInfoDialog").dialog('option', 'position', ['left','top']);
        $('#serverInfoDialog').dialog('open');
        }
        
      function validation(){     
      
      var enterNote = document.assetSearchForm.enterNote.value;   
           
      if(enterNote == ""){
     
      alert('Please enter note');
         document.assetSearchForm.enterNote.focus();   
     return false;
      }else{
     if(confirm('Are you sure???')){
      return true;
      }
      }
   
      }  
      function doTransition(){
      if(validation()){
      document.assetSearchForm.submit();
      }else {
      return false;
      }
      }
       function unRack(){      
   if(doCheckValidation()){  
   document.assetSearchForm.action = "unRack";      
   document.assetSearchForm.submit();
   }else{
   return false;
   }
  }
      
   function doCheckValidation(){
	 var trArray = new Array()
	 var j = 0;
     trArray = document.getElementsByName('myCheckbox');        
     for(i=0; i < trArray.length; i++){         
     if(trArray[i].checked == false){
     j=1;
     }
    }
     if(j == 0){     
      return true;
      }else{
      alert('Please select all instructions');                                   
      return false;
      }     
     
      }
      
        </script>    
</head>
<body>
<div id="serverInfoDialog" title ="Server Info" onclick="$('#serverInfoDialog').dialog('close')">			
</div>

<div id="spinner" class="spinner" style="display: none;"><img
	src="${createLinkTo(dir:'images',file:'spinner.gif')}" alt="Spinner" />
</div>
<div class="mainbody" style="width: 100%;">
<div class="colum_techlogin" style="float:left;">
<div style="float: left; width: 200px; margin-left: 20px;"><g:link
	params='["bundle":bundle,"team":team,"location":location,"project":project,"user":"mt"]'
	style="height:26px; width:64px; float:left; margin:auto 0px;">
	<img src="${createLinkTo(dir:'images',file:'home.png')}" border="0" />
</g:link> <g:link action="assetTask"
	params='["bundle":bundle,"team":team,"location":location,"project":project,"tab":"Todo"]'
	style="height:26px; width:64px; float:left; margin:auto 0px;">
	<img  src="${createLinkTo(dir:'images',file:'my_task.png')}" border="0" /></g:link>
<img
	src="${createLinkTo(dir:'images',file:'asset_h.png')}" border="0" /></div>
<div class="w_techlog">




<div style="float:left; width:225px; margin:5px 0; ">
<g:form name="assetSearchForm" action="placeHold">
							<input name="bundle" type="hidden" value="${bundle}" />
							<input name="team" type="hidden" value="${team}" />
							<input name="location" type="hidden" value="${location}" />
							<input name="project" type="hidden" value="${project}" />
							<input name="search" type="hidden" value="${search}"  />
							<input name="assetCommt" type="hidden" value="${assetCommt}"  />
							<input name="label" type="hidden" value="${label}"  />
							<input name="actionLabel" type="hidden" value="${actionLabel}"  />
							<input name="user" type="hidden" value="mt"  />
<table style="border:0px;" >

<div id="mydiv" onclick="document.getElementById('mydiv').style.display = 'none';">
 <g:if test="${flash.message}">
	<div style="color: red;"><ul><li>${flash.message}</li></ul></div>
</g:if> 
</div>
<div style="background:url(${createLinkTo(dir:'images',file:'search.png')}) no-repeat top right;" onclick="${remoteFunction(action:'getServerInfo', params:'\'assetId=\'+'+projMap.asset.id,onComplete: 'serverInfo(e)')}">

<g:if test="${projMap}">
<p><b>Asset Tag:</b> ${projMap?.asset?.assetTag}</p>
<p><b>Asset Name:</b> ${projMap?.asset?.assetName}</p>
<p><b>Model:</b> ${projMap?.asset?.model}</p>
<p><b>Rack/Pos:</b> <g:if test="${location == 's'}">${projMap?.asset?.sourceRack}/${projMap?.asset?.sourceRackPosition}</g:if><g:else test="${location == 't'}">${projMap?.asset?.targetRack}/${projMap?.asset?.targetRackPosition}</g:else> </p>
	
</g:if>

</div>	
	<tr>
		<td width="200px"><strong>Instructions</strong></td>
		<td><strong>Confirm</strong></td>
	</tr>
	<g:each status="i" in="${assetCommt}" var="comments">
		<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
			<td >${comments.comment}</td>
			<td><g:checkBox name="myCheckbox" value="${false}" /></td>
		</tr>
	</g:each>
	<g:if test ="${actionLabel}">
	
	<tr>
		<td class="buttonR" colspan="2" style="text-align:right;"><input type="button" value="${label}"
			onclick="unRack()" /></td>
	</tr>
	</g:if>		

	<table>	
		
		<tr><td>
		<textarea rows="2" cols="10" style="width: 200px;padding:0px;" title="Enter Note..." name="enterNote" ></textarea>
		</td> </tr>
		
		<tr>
		<td class="buttonR" style="text-align: right;"><input type="button" value="Place on HOLD" onclick="doTransition()" /></td>
		<tr>
	
	</table>



</table>
</g:form>
</div>
</div>

</div>
</div>





</body>

</html>
