<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>Asset</title>
<g:javascript library="jquery" />
	<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'main.css')}" />
	<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'tds.css')}" />
	<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'qvga.css')}" />
	<link rel="shortcut icon" href="${createLinkTo(dir:'images',file:'tds.ico')}" type="image/x-icon" />
	
    <script type="text/javascript">
   
        function validation(){   
	        var enterNote = document.assetSearchForm.enterNote.value;
	        if(enterNote == ""){     
	        	alert('Please enter note');
	        	document.assetSearchForm.enterNote.focus();   
	        	return false;
	        }else{
	        	if(confirm('Are you sure?')){
	        	return true;
	        	}
	        }   
       }  
       function doTransition( actionType ){
	       if(validation()){
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
       		alert("Please select all instructions");                                   
      	 	return false;
       }     
      }      
      function commentSelect(cmtVal) {
	      	document.assetSearchForm.enterNote.value = cmtVal;
      		document.assetSearchForm.selectCmt.value = 'Select a common reason:';
      }
      
    </script>
</head>
<body>

<div id="spinner" class="spinner" style="display: none;"><img src="${createLinkTo(dir:'images',file:'spinner.gif')}" alt="Spinner" /></div>
	<div class="mainbody" style="width: 100%;">
		<div class="colum_techlogin" style="float:left;">
			<div class="tech_head">
					<a name="#assetTag"/>
		        	<g:link params='["bundle":bundle,"team":team,"location":location,"project":project,"user":"mt","fMess":"fMess"]' class="home" >Home</g:link>
					<g:link action="assetTask" params='["bundle":bundle,"team":team,"location":location,"project":project,"tab":"Todo","fMess":"fMess"]' class="my_task">My Task</g:link>
					<a href="#" class="asset_search_select">Asset</a>
				</div>
		<div class="w_techlog">
		<div style="float:left; width:219px; margin:5px 0; ">
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
			<table style="border:0px;">
	  <div id="mydiv" onclick="this.style.display = 'none';">
 			<g:if test="${flash.message}">
			<div style="color: red;"><ul><li>${flash.message}</li></ul></div>
			</g:if> 
			</div>
		<div>
			<g:if test="${projMap}">			
			<dt>Asset Tag:</dt><dd><a href="#Top">&nbsp;${projMap?.asset?.assetTag}</a></dd>
			</g:if>
 		</div>
 			<g:if test="${assetComment}">
			<tr>
			<td width="219px"><strong>Instructions</strong></td>
			<td><strong>Confirm</strong></td>
			</tr>
			<g:each status="i" in="${assetComment}" var="comments">
			<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
			<td>${comments.comment}</td>
			<g:if test="${comments.mustVerify == 1}">
			<td><g:checkBox name="myCheckbox" value="${false}" class="confirm_checkbox" /></td>
			</g:if>
			<g:else>
			<td></td>
			</g:else>
			</tr>
			</g:each>
			</g:if>
			<g:if test ="${actionLabel}">	
			<tr>
			<td colspan="2" style="text-align:right;"><input type="button" value="${label}" onclick="return unRack();" class="action_button"/></td>
			</tr>
			</g:if>
			<table>	
			<tr>
			<td >
			<g:select style="width: 170px;padding:0px;" from="['Select a common reason:','Device not powered down','Device is not in expected rack','Device will not power up']" id="selectCmt" name="selectCmt" value="Select a common reason:" onchange="commentSelect(this.value);"></g:select>
			</td>
			</tr>		
			<tr><td>
			<textarea rows="3" cols="10" style="width: 200px;padding:0px;" title="Enter Note..." id="enterNote" name="enterNote" ></textarea>
			</td></tr>		
			<tr>
			<td class="button_style"><input type="button" value="Add Comment" onclick="return doTransition('comment');" class="action_button"/></td>
			<tr>
			<tr>
			<td class="button_style"><input type="button" value="Place on HOLD" onclick="return doTransition('hold');" class="action_button"/></td>
			<tr>	
			</table>
			</table>
	 	<div>
			<g:if test="${projMap}">
			<dl>
			<dt onclick="window.scrollTo(0,0)" style="float: right;color: red;"><a name="#Top" href="#AssetTag">Top</a></dt><dd>&nbsp;</dd>
			<dt>Asset Name:</dt><dd>&nbsp;${projMap?.asset?.assetName}</dd>
			<dt>Model:</dt><dd>&nbsp;${projMap?.asset?.model}</dd>
			<dt>Rack/Pos:</dt><dd>&nbsp;<g:if test="${location == 's'}">${projMap?.asset?.sourceRack}/${projMap?.asset?.sourceRackPosition}</g:if><g:else test="${location == 't'}">${projMap?.asset?.targetRack}/${projMap?.asset?.targetRackPosition}</g:else></dd>
			<dt>New or Old:</dt><dd>&nbsp;${projMap?.asset?.newOrOld}</dd>
			<dt>Rail Type:</dt><dd>&nbsp;${projMap?.asset?.railType}</dd>	
			</g:if>
			<g:if test="${location == 's'}">			   	
			   	<dt>Location:</dt><dd>&nbsp;${projMap?.asset?.sourceLocation}</dd>
			   	<dt>Room:</dt><dd>&nbsp;${projMap?.asset?.sourceRoom}</dd>  			   	
			   	</dl>			   	
			</g:if>
			<g:else>				
			   	<dt>Location:</dt><dd>&nbsp;${projMap?.asset?.targetLocation}</dd>
			   	<dt>Room:</dt><dd>&nbsp;${projMap?.asset?.targetRoom}</dd> 			   	
			   	<dt>Power Port:</dt><dd>&nbsp;${projMap?.asset?.powerPort}</dd>
			   	<dt>NIC Port:</dt><dd>&nbsp;${projMap?.asset?.nicPort}</dd>
			   	<dt>Remote Mgmt Port:</dt><dd>&nbsp;${projMap?.asset?.remoteMgmtPort}</dd>
			   	<dt>Fiber Cabinet:</dt><dd>&nbsp;${projMap?.asset?.fiberCabinet}</dd>
			   	<dt>HBA Port:</dt><dd>&nbsp;${projMap?.asset?.hbaPort}</dd>
			   	<dt>KVM Device:</dt><dd>&nbsp;${projMap?.asset?.kvmDevice}</dd>
			   	<dt>KVM Port:</dt><dd>&nbsp;${projMap?.asset?.kvmPort}</dd>			   	
			   	</dl>
			</g:else>
 		</div>	
	</g:form>
	</div>
	</div>
	</div>
	</div>
</body>
</html>
