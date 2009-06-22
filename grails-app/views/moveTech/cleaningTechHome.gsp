<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>Home</title>
	<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'main.css')}" />
	<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'cleaning.css')}" />
</head>
<body >
	<div id="spinner" class="spinner" style="display: none;"><img src="${createLinkTo(dir:'images',file:'spinner.gif')}" alt="Spinner" /></div>
		<div class="mainbody" style="width: 100%;" >
				<div class="colum_techlogin" style="float:left;">
				
				<div class="border_bundle_team">
          		<div style="float:left; width:97.5%; margin-left:20px;margin-top:17px;">              									
		        	<a href="#" style="height:18px; width:45px; float:left;padding-top:3px; margin:auto 0px;color: #5b5e5c; border:1px solid #5b5e5c; margin:0px;background:#aaefb8;padding:auto 0px;text-align:center;vertical-align:center ">Home</a>
					<g:link action="cleaningAssetTask" params='["bundle":bundle,"team":team,"location":location,"project":project,"tab":"Todo"]' style="height:18px; padding-top:3px;width:60px; float:left; margin:auto 0px;color: #5b5e5c; border:1px solid #5b5e5c; margin:0px;padding:auto 0px;text-align:center;">My Task</g:link>
					<g:link action="cleaningAssetSearch" params='["bundle":bundle,"menu":"true","team":team,"location":location,"project":project,"user":"ct"]' style="height:18px; padding-top:3px; width:63px; float:left; margin:auto 0px;color: #5b5e5c; border:1px solid #5b5e5c; margin:0px;padding:auto 0px;text-align:center;">Asset</g:link>								           
			  	</div>		
				<div id="mydiv" onclick="document.getElementById('mydiv').style.display = 'none';">
					<g:if test="${flash.message}">
					<div style="color: red;">
					<ul>
					<li>${flash.message}</li>
					</ul>
					</div>
					</g:if>
				</div>
				<g:if test="${browserTest == true}" >
					<div style="color: red;">
					<ul>
					<li>Please note that in order to print barcode labels you will need to use the Internet Explorer browser</li>
					</ul>
					</div>
				</g:if>
				<div class="w_techlog" style="margin-top:15px;">
										
      					<g:form method="post" name="bundleTeamAssetForm">      										        
							<div style="float:left; width:100%; margin:5px 0; ">              								
              					<table style="border:0px;">
								<tr><td><g:link controller="moveTech" action="signOut" style="color: #5b5e5c; border:1px solid #5b5e5c; margin:5px;background:#aaefb8;">Log out</g:link></td>								            		
								</table>
							</div>								
              				<div style="float:left; width:100%; margin:5px 0; ">
              					<table style="border:0px;">
              					<tr><td colspan="2"><b>Currently Logged in as:</b></td></tr>              					     
              					<tr><td style="width:20px;"><b>Project:</b></td><td>${project}</td></tr>
              					<tr><td style="width:20px;"><b>Bundle:</b></td><td>${bundleName}</td></tr>
              					<tr><td style="width:20px;"><b>Team:</b></td><td>${projectTeam}</td></tr> 
              					<tr><td style="width:20px;"><b>Members:</b></td><td>${members}</td></tr>              					     
              					<tr><td style="width:20px;"><b>Location:</b></td><td>${loc}</td></tr>
              					</table>
              					</div>  					
              			</g:form>
  				</div>
  				</div>  
		</div>
</body>
</html>
