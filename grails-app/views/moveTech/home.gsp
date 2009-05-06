<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>MoveTech Home</title>
<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'main.css')}" />
<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'qvga.css')}" />

<style type="text/css">
dt {
	font-weight: bold;
	float: left;
}
</style>

</head>
<body>
	<div id="spinner" class="spinner" style="display: none;"><img
		src="${createLinkTo(dir:'images',file:'spinner.gif')}" alt="Spinner" />
	</div>
	<div class="mainbody" style="width: 100%;" >
		<div class="colum_techlogin" style="float:left;">
				<div class="border_bundle_team">
          			<div style="float:left; width:92%; margin-left:15px;">              									
		              	<a href="#" style="height:26px; width:64px; float:left; margin:auto 0px;"><img src="${createLinkTo(dir:'images',file:'home_h.png')}" border="0"/></a>							              				
              			<g:link action="assetTask" params='["bundle":bundle,"team":team,"location":location,"project":project,"tab":"Todo"]' style="height:26px; width:64px; float:left; margin:auto 0px;"><img src="${createLinkTo(dir:'images',file:'my_task.png')}" border="0" /></g:link>              											
              			<img src="${createLinkTo(dir:'images',file:'asset.png')}" style="height:26px; width:64px; float:left; margin:auto 0px;"/>								           
		       </div>			
			<div class="w_techlog">
				<g:form method="post" name="bundleTeamAssetForm">
				<div style="float:left; width:100%; margin:5px 0; ">              								
					<table style="border:0px;">
						<tr><td><g:link controller="moveTech" action="signOut" style="color: #5b5e5c; border:1px solid #5b5e5c; margin:2px 5px 5px 5px; height:15px; padding:1px 2px 1px 3px; width:50px; background:#aaefb8; float:left;">Log out</g:link></td>
							<td style="text-align:right;"><a href="#" style="color: #328714;"><input type="text" size="12" value="" name="search"/></a>&nbsp;<img	src="${createLinkTo(dir:'images',file:'search.png')}"/></td>
						</tr>
					  </table>
				</div>  
				<div style="float:left; width:200px; margin:4px;">
					<b>Currently Logged in as:</b>
					<dl compact>
						<dt>Project:&nbsp;</dt><dd>${project}</dd>
						<dt>Bundle:&nbsp;</dt><dd>${bundle}</dd> 
						<dt>Team:&nbsp;</dt><dd>${projectTeam}</dd>
						<dt>Members:&nbsp;</dt><dd>${members}</dd>              					     
						<dt>Location:&nbsp;</dt><dd>${loc}</dd>
					</dl>
				</div>
				</g:form>
			</div>
		</div>
	</div>
</body>
</html>
