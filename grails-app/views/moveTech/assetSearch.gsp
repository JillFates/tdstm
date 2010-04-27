<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>Asset</title>
<g:javascript library="jquery" />
	<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'main.css')}" />
	<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'tds.css')}" />
	<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'qvga.css')}" />
	<link rel="shortcut icon" href="${createLinkTo(dir:'images',file:'tds.ico')}" type="image/x-icon" />
<meta name="viewport" content="height=device-height,width=device-width" />
	
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
       var boxes = document.getElementsByTagName('input'); 
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
	<div id="spinner" class="spinner" style="display: none;"><img src="${createLinkTo(dir:'images',file:'spinner.gif')}" alt="Spinner" /></div>
	<div class="mainbody" style="border:0px solid #e7e7e7; width: 100%;">
	<div class="colum_techlogin" style="float:left;">
		<table border=0 cellpadding=0 cellspacing=0><tr>
			<td><g:link params='["bundle":bundle,"team":team,"location":location,"project":project,"user":"mt","fMess":"fMess"]' class="home" >Home</g:link></td>
			<td><g:link action="assetTask" params='["bundle":bundle,"team":team,"location":location,"project":project,"tab":"Todo","fMess":"fMess"]' class="my_task">My Tasks</g:link></td>
			<td><a href="#" class="asset_search_select">Asset</a></td>
		</tr></table>

		<g:form name="assetSearchForm" action="placeHold">
			<input name="bundle" type="hidden" value="${bundle}" />
			<input name="team" type="hidden" value="${team}" />
			<input name="location" type="hidden" value="${location}" />
			<input name="project" type="hidden" value="${project}" />
			<input name="search" type="hidden" value="${search}"  />
			<input name="assetComment" type="hidden" value="${assetComment}"  />
			<input name="label" type="hidden" value="${label}"  />
			<input name="actionLabel" type="hidden" value="${actionLabel}"  />
			<input name="user" type="hidden" value="mt"  />
			<input name="similarComment" id="similarComment" type="hidden" value="nosimilar"/>

	 	<div id="mydiv" onclick="this.style.display = 'none';">
 			<g:if test="${flash.message}">
				<div style="color: red;"><ul><li>${flash.message}</li></ul></div>
			</g:if> 
		</div>

		<div class="clear" style="margin:2px;"></div>

		<g:if test="${projMap}">			
		<table>
			<tr><td><dt>Asset Tag:</dt><dd><a href="#detail">&nbsp;${projMap?.asset?.assetTag}&nbsp;&nbsp;&nbsp;(Details)</a></dd></td></tr>
	
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
				<td class="heading"><a class="heading" href="#comments">Comments/Hold</a></td>
				<td><span style="float:right;"><a href="#top">Top</a></span></td>
			</tr>
			<tr>
			<td colspan=2>
				<g:select style="width: 188px;padding:0px;" from="${commentsList}" id="selectCmt" name="selectCmt" value="Select a common reason:" noSelection="['Select a common reason:':'Select a common reason:']" onchange="commentSelect(this.value);"></g:select>
			</td>
			</tr>		
			<tr><td colspan=2>
			<textarea rows="2" cols="100" style="width:188px;padding:0px;" title="Enter Note..." id="enterNote" name="enterNote"></textarea>
			</td></tr>		
			<tr><td class="button_style" colspan=2 style="text-align:center;">
				<input type="button" value="Add Comment" onclick="return doTransition('comment');" class="action_button"/>
			</td></tr>
			<tr><td class="button_style" colspan=2 style="text-align:center;">
				<input type="button" value="Place on HOLD" onclick="return doTransition('hold');" class="action_button_hold" />
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
			   		<dt>Location:</dt><dd>&nbsp;${projMap?.asset?.sourceLocation}</dd>
			   		<dt>Room:</dt><dd>&nbsp;${projMap?.asset?.sourceRoom}</dd>
			   		<dt>Rack/Pos:</dt><dd>&nbsp;${projMap?.asset?.sourceRack}/${projMap?.asset?.sourceRackPosition}</dd>
			   		<dt>New or Old:</dt><dd>&nbsp;${projMap?.asset?.newOrOld}</dd>
					<dt>Rail Type:</dt><dd>&nbsp;${projMap?.asset?.railType}</dd>  			   	
				</g:if>
				<g:else>				
			   		<dt>Location:</dt><dd>&nbsp;${projMap?.asset?.targetLocation}</dd>
			   		<dt>Room:</dt><dd>&nbsp;${projMap?.asset?.targetRoom}</dd>
			   		<dt>Rack/Pos:</dt><dd>&nbsp;${projMap?.asset?.targetRack}/${projMap?.asset?.targetRackPosition}</dd>
			   		<dt>Truck:</dt><dd>&nbsp;${projMap?.asset?.truck}</dd>
			   		<dt>Cart/Shelf:</dt><dd>&nbsp;${projMap?.asset?.cart}/${projMap?.asset?.shelf}</dd>
			   		<dt>New or Old:</dt><dd>&nbsp;${projMap?.asset?.newOrOld}</dd>
					<dt>Rail Type:</dt><dd>&nbsp;${projMap?.asset?.railType}</dd>  			   	
			   		<dt>Power Port:</dt><dd>&nbsp;${projMap?.asset?.pduPort}</dd>
			   		<dt>NIC Port:</dt><dd>&nbsp;${projMap?.asset?.nicPort}</dd>
			   		<dt>Remote Mgmt Port:</dt><dd>&nbsp;${projMap?.asset?.remoteMgmtPort}</dd>
			   		<dt>Fiber Cabinet:</dt><dd>&nbsp;${projMap?.asset?.fiberCabinet}</dd>
			   		<dt>HBA Port:</dt><dd>&nbsp;${projMap?.asset?.hbaPort}</dd>
			   		<dt>KVM Device:</dt><dd>&nbsp;${projMap?.asset?.kvmDevice}</dd>
			   		<dt>KVM Port:</dt><dd>&nbsp;${projMap?.asset?.kvmPort}</dd>			   	
				</g:else>
				<dt>Current State:</dt><dd>&nbsp;${stateLabel}</dd>
				</dl>
		</tr>
		</table>
		</g:if>
		</div>
	</g:form>
	</div>
	</div>
</body>
</html>
