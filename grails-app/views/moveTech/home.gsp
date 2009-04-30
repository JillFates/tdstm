<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<title>Home</title>
<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'main.css')}" />
<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'qvga.css')}" />
<link rel="shortcut icon"
	href="${createLinkTo(dir:'images',file:'tds.ico')}" type="image/x-icon" />
<g:javascript library="application" />

</head>
<body>
	<div id="spinner" class="spinner" style="display: none;"><img
		src="${createLinkTo(dir:'images',file:'spinner.gif')}" alt="Spinner" />
	</div>
	<div class="mainbody" style="width: 100%;" >
				<div class="colum_techlogin" style="float:left;">
				<div class="border_bundle_team">
          										<div style="float:left; width:100%; margin-left:20px;">
              									
		              									<a href="#" style="height:26px; width:64px; float:left; margin:auto 0px;"><img src="${createLinkTo(dir:'images',file:'home_h.png')}" border="0"/></a>
							              				
              											<g:link action="assetTask" params='["bundle":bundle,"team":team,"location":location,"project":project,"tab":"Todo"]' style="height:26px; width:64px; float:left; margin:auto 0px;"><img src="${createLinkTo(dir:'images',file:'my_task.png')}" border="0" /></g:link>
              											
              											<img src="${createLinkTo(dir:'images',file:'asset.png')}" border="0"/>
								           
								              	</div>			
				<div class="w_techlog" style="overflow-y: scroll; overflow-x: none;">
				
      					<g:form method="post" name="bundleTeamAssetForm">
      										        
								              <div style="float:left; width:100%; margin:5px 0; ">              								
              								   <table style="border:0px;">
								            		<tr><td><g:link controller="moveTech" action="signOut" style="color: #328714; border:1px solid #328714; margin:5px; background:#ffffff;">Log out</g:link></td>
								            		<td style="text-align:right;"><a href="#" style="color: #328714;"><input type="text" size="15" value="" name="search" style="background:url(${createLinkTo(dir:'images',file:'search.png')}) no-repeat center right;"/></a></td></tr>
								               </table>
								            </div>  
								
              					<div style="float:left; width:100%; margin:5px 0; ">
              					  <table style="border:0px;">
              					     <tr><td colspan="2">Currently Logged in as:</td></tr>
              					     <tr><td>Project:</td><td>${project}</td></tr>
              					     <tr><td>Team:</td><td>${projectTeam}</td></tr>              					     
              					     <tr><td>Location:</td><td>${loc}</td></tr>
              					  </table>
              					</div>
              					
           
      </g:form>
  </div>
  </div>
  
  </div></body>

</html>
