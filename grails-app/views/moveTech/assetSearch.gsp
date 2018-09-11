<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>Asset</title>
<jq:plugin name="jquery"/>
	<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'tds-main.css')}" />
	<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'tds.css')}" />
	<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'qvga.css')}" />
	<link rel="shortcut icon" type="image/x-icon" href="${assetPath(src: 'images/favicon.ico')}"/>
<meta name="viewport" content="height=device-height,width=220" />
	
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
       function doTransition( actionType ){
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
	       			document.assetSearchForm.action = "placeHold";
	       			document.assetSearchForm.submit();
	       		}
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
       } else{
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
	<div class="mainbody" style="border:0px solid #e7e7e7; width:220px;">
	<div class="colum_techlogin">
		<table border=0 cellpadding=0 cellspacing=0><tr>
			<td><g:link params='["bundle":bundle,"team":team,"location":location,"project":project,"user":"mt","fMess":"fMess"]' class="home" >Home</g:link></td>
			<td><g:link action="assetTask" params='["bundle":bundle,"team":team,"location":location,"project":project,"tab":"Todo","fMess":"fMess"]' class="my_task">My Tasks</g:link></td>
			<td><a href="#" class="asset_search_select">Asset</a></td>
		</tr></table>

		<g:form name="assetSearchForm" action="assetSearch">
			<input name="bundle" type="hidden" value="${bundle}" />
			<input name="team" type="hidden" value="${team}" />
			<input name="location" type="hidden" value="${location}" />
			<input name="project" type="hidden" value="${project}" />
			<input name="label" type="hidden" value="${label}"  />
			<input name="actionLabel" type="hidden" value="${actionLabel}"  />
			<input name="user" type="hidden" value="mt"  />
			<input name="similarComment" id="similarComment" type="hidden" value="nosimilar"/>

	 	<div id="mydiv" onclick="this.style.display = 'none';">
 			<g:if test="${flash.message}">
				<div style="color: red;"><ul>${flash.message}</ul></div>
			</g:if> 
		</div>

		<div class="clear" style="margin:2px;"></div>

		<g:if test="${projMap}">			
		<table style="border:0px; width=220">
			<tr><td style="padding:0px;"><b>Asset</b>:&nbsp;<input name="search" type="text" size="12" value="${search}" id="search" autocorrect="off" autocapitalize="off"  />&nbsp;<a href="#detail">(Details...)</a></td>
			</tr>
	
 			<g:if test="${assetComment}">
			<tr><td><table style="border:0px; width=100%">
			<tr>
				<td style="width:219px"><strong><u>Instructions</u></strong></td>
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
			</g:if>
			<g:if test ="${actionLabel}">	
			<tr>
			<td colspan="2" style="text-align:center;"><input type="button" value="${label}" onclick="return unRack();" class="action_button"/></td>
			</tr>
		</table>
		</td>
		</tr>
		</g:if>
	</table>
	</g:if>

	<div class="clear" style="margin:4px;"></div>
		<a name="comments"></a>
		<table width="100%">
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
				<input type="button" value="Comment" onclick="return doTransition('comment');" class="action_button_small"/>
			</td>
			<td class="button_style" colspan=1 style="text-align:center;">
				<input type="button" value="HOLD" onclick="return doTransition('hold');" class="action_button_hold" />
				</td>
			</tr>	
		</table>


		<div class="clear" style="margin:4px;"></div>
		<a name="detail" ></a>
	 	<div>
			<g:if test="${projMap}">

			<div style="margin:2px;" class="reset" ></div>

			<table width="100%">
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
				<g:if test="${location == 's'}">			   	
			   		<dt>Location:</dt><dd>&nbsp;${projMap?.asset?.sourceLocationName}</dd>
			   		<dt>Room:</dt><dd>&nbsp;${projMap?.asset?.sourceRoomName}</dd>
			   		<dt>Rack/Pos:</dt><dd>&nbsp;${projMap?.asset?.sourceRackName}/${projMap?.asset?.sourceRackPosition}</dd>
			   		<dt>Plan Status:</dt><dd>&nbsp;${projMap?.asset?.planStatus}</dd>
					<dt>Rail Type:</dt><dd>&nbsp;${projMap?.asset?.railType}</dd>  			   	
				</g:if>
				<g:else>				
			   		<dt>Location:</dt><dd>&nbsp;${projMap?.asset?.targetLocationName}</dd>
			   		<dt>Room:</dt><dd>&nbsp;${projMap?.asset?.targetRoomName}</dd>
			   		<dt>Rack/Pos:</dt><dd>&nbsp;${projMap?.asset?.targetRackName}/${projMap?.asset?.targetRackPosition}</dd>
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
	</div>
	<div class="cabling_div">
		<div id="cablingPanel" style="height: auto;">
		<g:if test="${projMap?.asset?.model?.rearImage && projMap?.asset?.model?.useImage == 1}">
			<img src="${createLink(controller:'model', action:'retrieveRearImage', id:projMap?.asset?.model?.id)}" />
		</g:if>
		<g:each in="${assetCablingDetails}" status="i" var="assetCable">
			<div id="connector${i}" style="top:${assetCable.connectorPosY / 2}px ;left:${assetCable.connectorPosX}px ">
				<div>
					<img src="${resource(dir:'i/cabling',file:assetCable.status + '.png')}"/>
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
</body>
</html>
