<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<title>Login</title>
<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'main.css')}" />
<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'tds.css')}" />
<link rel="shortcut icon"
	href="${createLinkTo(dir:'images',file:'tds.ico')}" type="image/x-icon" />
<g:javascript library="application" />
</head>
<body>
	 <div class="body">
	 <h1>Home</h1>
		<div>
      		<table style="width:90%">
				<tr class="prop">
					<td style="width:100px; valign="top" class="value" colspan="2">
      					<g:form method="post" name="bundleTeamAssetForm">
					        <div class="border_bundle_team">
          						<table style="border:0px;">
            						<tbody>
              							<tr>
              								<td style="width:550px; float:center;">
              									<table style="width:360px; float:center;">
              										<tr>
							              				<td style="width:100px; float:center; background-color:#43ca56; padding-left:20px;">
		              										<a href="#">Home</a></td>
							              				<td style="width:100px; float:center; border-left:1px solid #5585c7; padding-left:20px;">
              											<a href="#">My Tasks</a></td>
              											<td style="width:100px; float:center; border-left:1px solid #5585c7; padding-left:20px;">
              											<a href="#">Search</a></td>
								              		</tr>
								              		<tr>
								              		</tr>
              									</table>
              								</td>
              							</tr>
              							<tr>
              								<td class="buttonR">
								            	<g:link controller="moveTech" action="signOut" style="color: #328714">Log out</g:link>
								            </td></tr>
              						</tbody>
              					</table>
              					<table>
              					<tr><td><div style="text-align:center; color:#990000;"><b>Currently Logged in as:</b></div></td></tr>
              					<tr><td><div style="text-align:center; "><b>${projectTeam}</b></div></td></tr>
              					<tr><td><div style="text-align:center; "><b>${project}</b></div></td></tr>
              					<tr><td><div style="text-align:center; "><b>${location}</b></div></td></tr>
              					</table>		
           <div style="text-align:left; "><b>My Issues:</b></div>
           <div  style="overflow:scroll; width:550px;">
            <table id="assetTable">
              <thead>
                <tr>
                  <th>Issue</th>

                  <th>Assigned</th>

                  <th>Priority</th>

                  <th>state</th>
               </tr>
              </thead>
              <tbody>
              	<tr>
              		<td>Last Usb</td>
              		<td>Team2</td>
              		<td>High</td>
              		<td>Open</td>	
              	</tr>
              	<tr>
              		<td>Last Usb</td>
              		<td>Team2</td>
              		<td>High</td>
              		<td>Open</td>	
              	</tr>		
              </tbody>
            </table>
          </div>
        </div>
      </g:form>
  </body>

</html>
