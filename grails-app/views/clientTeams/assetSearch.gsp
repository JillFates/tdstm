<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="projectHeader" />
	<title>Asset</title>
<jq:plugin name="jquery"/>
	<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'main.css')}" />
	<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'tds.css')}" />
	<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'qvga.css')}" />
	<link rel="shortcut icon" href="${resource(dir:'images',file:'tds.ico')}" type="image/x-icon" />
	
<script type="text/javascript">
        window.addEventListener('load', function(){
                setTimeout(scrollTo, 0, 0, 1);
        }, false);
</script>
    <script type="text/javascript">
   
        function validation( actionType ){   
	        var enterNote = document.assetSearchForm.enterNote.value;
	        if(enterNote == ""){     
	        	alert('Please enter note');
	        	document.assetSearchForm.enterNote.focus();   
	        	return false;
	        }else{
	        	var alertText = ""
	      		if(actionType != 'hold'){
	      			alertText = "Add comment, are you sure?"
	      		} else {
	      			alertText = "Place on HOLD, are you sure?"
	      		}
	        	if(confirm( alertText )){
	        	return true;
	        	}
	        }   
       }  
       function placeOnHold( actionType ){
	       if(validation( actionType )){
	       		  var splittedComment
			      var enterNote = document.assetSearchForm.enterNote.value;
				  var completeComment = '${session.getAttribute( "COMMENT_COMPLETE" )}'
				  completeComment = completeComment.split('~');
				  var checkLength = completeComment.length
				  for ( var i=0; i<checkLength; i++ ) {
				  	if ( enterNote == completeComment[i] ) {
				  		document.assetSearchForm.similarComment.value = 'null';
				  	} 
				  }
	       		if(actionType != 'hold'){
	       			document.assetSearchForm.action = "addComment";
	       			document.assetSearchForm.submit();
	       		}else {
	       			document.assetSearchForm.action = "placeOnHold";
	       			document.assetSearchForm.submit();
	       		}
	       }else {
	       		return false;
	       }
       }
       function doTransition(){ 
	       if(doCheckValidation()){  
		      	document.assetSearchForm.action = "doTransition";      
       			document.assetSearchForm.submit();       
	       }else{
	       		return false;
	       }
       }
              
       function doCheckValidation(){
	   var j = 0;
       var boxes = document.getElementsByName('myCheckbox'); 
		for (i = 0; i < boxes.length; i++) {
          if (boxes[i].type == 'checkbox'){
               if(boxes[i].checked == false){
       			j=1;
       		 }
           }
     	}      
     	
       if(j == 0){     
       		return true;
       }else{
       		alert("Please check to confirm all instructions");                                   
      	 	return false;
       }     
      }      
      function commentSelect(cmtVal) {
      	  var splittedComment
		  var completeComment = '${session.getAttribute( "COMMENT_COMPLETE" )}'
		  completeComment = completeComment.split('~');
		  var checkLength = completeComment.length
		  for ( var i=0; i<checkLength; i++ ) {
		  	if ( completeComment[i].length > 25 ) {
		  	    if ( cmtVal != completeComment[i] ) {
		  			splittedComment = completeComment[i].substring(0,25);
		  		} else {
		  			splittedComment = completeComment[i];
		  		}
		  	} else {
		  		splittedComment = completeComment[i];
		  	}
		  	if ( cmtVal == splittedComment ) {
		  		document.assetSearchForm.enterNote.value = completeComment[i];
		  	}		  	
		 }
		 if ( cmtVal == 'Select a common reason:' ) {
		  	document.assetSearchForm.enterNote.value = '';
		 }
      	 document.assetSearchForm.selectCmt.value = cmtVal;
      }
      
    </script>
</head>
<body>
	<a name="top"></a>
	<div id="spinner" class="spinner" style="display: none;"><img src="${resource(dir:'images',file:'spinner.gif')}" alt="Spinner" /></div>
	<div class="mainbody">
	<div class="menu4">
		<ul>
			<li><g:link class="mobmenu" controller="clientTeams" params="[projectId:project?.id]">Teams</g:link></li>
			<li><g:link class="mobmenu" action='home' params='["bundleId":bundleId,"teamId":teamId,"location":location,"projectId":projectId]'>Home</g:link></li>
			<li><g:link class="mobmenu" action="myTasks" params='["bundleId":bundleId,"teamId":teamId,"location":location,"projectId":project?.id,"tab":"Todo"]'>Tasks</g:link></li>
			<li><a href="#" class="mobmenu mobselect">Asset</a></li>
		</ul>
	</div>
		<g:form name="assetSearchForm" action="assetSearch">
			<input name="bundleId" type="hidden" value="${bundleId}" />
			<input name="teamId" type="hidden" value="${teamId}" />
			<input name="location" type="hidden" value="${location}" />
			<input name="label" type="hidden" value="${label}"  />
			<input name="actionLabel" type="hidden" value="${actionLabel}"  />
			<input name="similarComment" id="similarComment" type="hidden" value="nosimilar"/>

	 	<div id="mydiv" onclick="this.style.display = 'none';">
 			<g:if test="${flash.message}">
				<div style="color: red;"><ul>${flash.message}</ul></div>
			</g:if> 
		</div>

		<div class="clear" style="margin:2px;"></div>

		<g:if test="${projMap}">			
		<table style="border:0px; width:420px;">
			<tr><td style="padding:0px;"><b>Asset</b>:&nbsp;<input name="search" type="text" size="12" value="${search}" id="search" autocorrect="off" autocapitalize="off"  />&nbsp;<a href="#detail">(Details...)</a></td>
			</tr>
	
 			<g:if test="${assetComment}">
			<tr><td><table style="border:0px; width=420">
				<tr>
					<td style="width:420px"><strong><u>Instructions</u></strong></td>
					<td><strong><u>Confirm</u></strong></td>
				</tr>
				<g:each status="i" in="${assetComment}" var="comments">
				<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
					<td>${comments.comment}</td>
					<g:if test="${comments.mustVerify == 1}">
						<td><g:checkBox name="myCheckbox" value="${false}" class="confirm_checkbox" /></td>
					</g:if>
					<g:else>
						<td>&nbsp;</td>
					</g:else>
				</tr>
				</g:each>
				</table>
				</td>
			</tr>
			</g:if>
			<g:if test ="${actionLabel}">	
			<tr>
				<td colspan=2 class="heading"><a class="heading" href="#detail">Task for ${projMap?.asset?.assetName}:</a></td>
			</tr><tr>
				<td colspan="2" style="text-align:center;"><input type="button" value="${label}" onclick="return doTransition();" class="action_button"/></td>
			</tr>
			</g:if>
		</table>
		</g:if>

	<div class="clear" style="margin:4px;"></div>
		<a name="comments"></a>
		<table style="width:420px;">
			<tr>
				<td class="heading" colspan=2><a class="heading" href="#comments">Other Actions</a></td>
			</tr>
			<tr>
			<td colspan=2>
				<g:select style="width: 188px;padding:0px;" from="${commentsList}" id="selectCmt" name="selectCmt" value="Select a common reason:" noSelection="['Select a common reason:':'Select a common reason:']" onchange="commentSelect(this.value);"></g:select>
			</td>
			</tr>		
			<tr><td colspan=2>
			<textarea rows="2" cols="100" style="width:188px;padding:0px;" title="Enter Note..." id="enterNote" name="enterNote" onkeydown="textCounter($('#enterNote'), 255)" onkeyup="textCounter($('#enterNote'), 255)"></textarea>
			</td></tr>		
			<tr><td class="button_style" colspan=1 style="text-align:center;">
				<input type="button" value="Comment" onclick="return placeOnHold('comment');" class="action_button_small"/>
			</td>
			<td class="button_style" colspan=1 style="text-align:center;">
				<input type="button" value="HOLD" onclick="return placeOnHold('hold');" class="action_button_hold" />
				</td>
			</tr>	
		</table>


		<div class="clear" style="margin:4px;"></div>
		<a name="detail" ></a>
	 	<div>
			<g:if test="${projMap}">

			<table style="width:420px;">
			<tr>
				<td class="heading"><a href="#detail">Details</a></td>
				<td><span style="float:right;"><a href="#top">Top</a></span></td>
			</tr>
			<tr><td colspan=2>

				<dl>
				<dt>Asset Tag:</dt><dd>&nbsp;${projMap?.asset?.assetTag}</dd>
				<dt>Asset Name:</dt><dd>&nbsp;${projMap?.asset?.assetName}</dd>
				<dt>Model:</dt><dd>&nbsp;${projMap?.asset?.model}</dd>
				<dt>Serial #:</dt><dd>&nbsp;${projMap?.asset?.serialNumber}</dd>
				<g:if test="${location == 'source'}">			   	
			   		<dt>Location:</dt><dd>&nbsp;${projMap?.asset?.sourceLocation}</dd>
			   		<dt>Room:</dt><dd>&nbsp;${projMap?.asset?.sourceRoom}</dd>
			   		<dt>Rack/Pos:</dt><dd>&nbsp;${projMap?.asset?.sourceRack}/${projMap?.asset?.sourceRackPosition}</dd>
			   		<dt>Plan Status:</dt><dd>&nbsp;${projMap?.asset?.planStatus}</dd>
					<dt>Rail Type:</dt><dd>&nbsp;${projMap?.asset?.railType}</dd>  			   	
				</g:if>
				<g:else>				
			   		<dt>Location:</dt><dd>&nbsp;${projMap?.asset?.targetLocation}</dd>
			   		<dt>Room:</dt><dd>&nbsp;${projMap?.asset?.targetRoom}</dd>
			   		<dt>Rack/Pos:</dt><dd>&nbsp;${projMap?.asset?.targetRack}/${projMap?.asset?.targetRackPosition}</dd>
			   		<dt>Truck:</dt><dd>&nbsp;${projMap?.asset?.truck}</dd>
			   		<dt>Cart/Shelf:</dt><dd>&nbsp;${projMap?.asset?.cart}/${projMap?.asset?.shelf}</dd>
			   		<dt>Plan Status:</dt><dd>&nbsp;${projMap?.asset?.planStatus}</dd>
					<dt>Rail Type:</dt><dd>&nbsp;${projMap?.asset?.railType}</dd>  			   	
				</g:else>
				<dt>Current State:</dt><dd>&nbsp;${stateLabel}</dd>
				</dl>
		</tr>
		</table>
		</g:if>
		</div>
	</g:form>
		<div style="margin:2px;" class="reset" ></div>

		<div class="cabling_div" style="width:420px;">
		<div id="cablingPanel" style="height: auto;">
		<g:if test="${projMap?.asset?.model?.rearImage && projMap?.asset?.model?.useImage == 1}">
			<img src="${createLink(controller:'model', action:'getRearImage', id:projMap?.asset?.model?.id)}" />
		</g:if>
		<g:each in="${assetCablingDetails}" status="i" var="assetCable">
			<div id="connector${i}" style="top:${assetCable.connectorPosY / 2}px ;left:${assetCable.connectorPosX}px ">
				<div>
					<img src="../i/cabling/${assetCable.status}.png"/>
				</div>
				<div class="connector_${assetCable.labelPosition}">
					<span>${assetCable.label}</span>
				</div>
			</div>
		</g:each>
		</div>
		<div class="list" style="margin-bottom: 10px;margin-right: 5px;">
		<table>
			<thead>
				<tr>
					<th>Connector</th>
					<th>Type</th>
					<th>Label</th>
					<th>Status</th>
					<th>Color</th>
					<th>Rack/Upos/Conn</th>
				</tr>
			</thead>
			<tbody id="connectorModelBody">
			<g:each in="${assetCablingDetails}" status="i" var="assetCable">
				<tr id="connectorTr${i}"  class="${(i % 2) == 0 ? 'odd' : 'even'}">
					<td>${assetCable.connector}</td>
					<td>${assetCable.type}</td>
					<td>${assetCable.label}</td>
					<td>${assetCable.displayStatus}</td>
					<td class="${assetCable.color}">&nbsp;</td>
					<td>${assetCable.rackUposition}</td>
				</tr>
			</g:each>
			</tbody>
		</table>
		</div>
		</div>
	</div>
	</div>
	<script type="text/javascript">
	function textCounter(obj, maxlimit) {
	      if (obj.val().length > maxlimit) {// if too long...trim it!
	    	  obj.val( obj.val().substring(0, maxlimit) );
		      return false;
	      } else {
	      	return true;
	      }
	}
	  /* cabling diagram*/
	var image = "${projMap?.asset?.model?.rearImage}"
	var usize = "${projMap?.asset?.model?.usize}"
	var useImage = "${projMap?.asset?.model?.useImage}" 
	if(!image || useImage != '1'){
		document.getElementById("cablingPanel").style.height = usize*30+"px";
	} else {
		$("#cablingPanel").css("background-color","#FFF")
	}
	</script>
	<script>
		currentMenuId = "#teamMenuId";
		$("#teamMenuId a").css('background-color','#003366')
	</script>
</body>
</html>
