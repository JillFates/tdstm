<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<title>Home</title>
<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'main.css')}" />
<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'tds.css')}" />
<link rel="shortcut icon"
	href="${createLinkTo(dir:'images',file:'tds.ico')}" type="image/x-icon" />
<g:javascript library="application" />

</head>
<body>
 
<div id="serverInfoDialog" title ="Server Info" >
  
			
    </div>
	<div id="spinner" class="spinner" style="display: none;"><img
		src="${createLinkTo(dir:'images',file:'spinner.gif')}" alt="Spinner" />
	</div>
	<div class="mainbody" style="width: 100%;" >
				<div class="colum_techlogin">			
				<div class="w_techlog">
				<div style="float:center; width:100%;float:left;text-align:left; height:20px; border-bottom:1px solid #5F9FCF;">
								<span style="text-align:left;font-size:10"><b>Task</b></span>
								</div>
      					<g:form method="post" name="bundleTeamAssetForm" action="assetSearch" >
      					<input name="bundle" type="hidden" value="${bundle}"  />
      					<input name="team" type="hidden" value="${team}"  />
      					<input name="location" type="hidden" value="${location}"  />
      					<input name="project" type="hidden" value="${project}"  />
      					
					        <div class="border_bundle_team">
          						<div style="float:left; margin:5px 0; width:100%; border:1px solid #5F9FCF;">
              									<div style="width:30%; float:left; border-left:1px solid #5585c7; ">
		              										<g:link params='["bundle":bundle,"team":team,"location":location,"project":project]'>Home</g:link></div>
							              				<div style="width:30%; float:left; border-left:1px solid #5585c7; ">
              											<g:link action="assetTask" params='["bundle":bundle,"team":team,"location":location,"project":project]' >My Tasks</g:link></div>
              											<div style="width:30%; float:left; background-color:#43ca56; border-left:1px solid #5585c7; ">
              											<g:link action="#">Asset</g:link></div>
								              	</div>
								              	</div>
								            
								              
              								<div style="float:left; margin:5px; width:20%; border:1px solid #5F9FCF;">
								            	<g:link controller="moveTech"  action="signOut" style="color: #328714">Log out</g:link>
								            </div>
								           
								            <div style="float:left;">
      
      
       <input type="text" name="assetId" id="assetId" size="18"/>          
       <input type="submit" name="search" value="Search"  />          
    

       </div>
       </g:form>
  
								            
             <div>
             &nbsp;
             </div> 	
             			
           <div>
       <div>
       
<p>&nbsp;</p>
<p><strong>Device Description</strong></p>
<g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:if test="${projMap}">
     <p> ${projMap?.asset?.assetName}-${projMap?.asset?.model}-${projMap?.asset?.serialNumber}-${stateVal}</p>
      </g:if>
       </div>
          <div>
       <table>
<p>&nbsp;</p>
<tr><td><strong>Instructions</strong></td></tr>
<g:each status="i" in="${assetCommt}" var="comments">
     <tr><td> ${comments.comment}</td><td> <g:checkBox name="myCheckbox" value="${false}" /></td></tr>
     </g:each>
    <tr><td> <g:actionSubmit value="Place on HOLD" onclick="return confirm('Are you sure???')" /></td>
    <td> <g:actionSubmit value="Start Unracking" onclick="return confirm('Are you sure???')" /></td></tr>
      </table>
       </div>
       
       </div>
        </div>
    
  </div>
</div>

  

  
    
  </body>

</html>
