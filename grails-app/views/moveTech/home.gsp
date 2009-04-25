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
	<div id="spinner" class="spinner" style="display: none;"><img
		src="${createLinkTo(dir:'images',file:'spinner.gif')}" alt="Spinner" />
	</div>
	<div class="mainbody" style="width: 100%;" >
				<div class="colum_techlogin">			
				<div class="w_techlog">
				<div style="float:center; width:100%;float:left;text-align:left; height:20px; border-bottom:1px solid #5F9FCF;">
								<span style="text-align:left;font-size:10"><b>Home</b></span>
								</div>
      					<g:form method="post" name="bundleTeamAssetForm">
					        <div class="border_bundle_team">
          						<div style="float:left; margin:5px 0; width:100%; border:1px solid #5F9FCF;">
              									<div style="width:30%; float:left; background-color:#43ca56; border-left:1px solid #5585c7; ">
		              										<a href="#">Home</a></div>
							              				<div style="width:30%; float:left; border-left:1px solid #5585c7; ">
              											<a href="#">My Tasks</a></div>
              											<div style="width:30%; float:left; border-left:1px solid #5585c7; ">
              											<a href="#">Search</a></div>
								              	</div>
								              	</div>
								              	<div style="float:left; width:100%; margin:5px 0; ">	
              								<div style="float:left; margin:5px; width:20%; border:1px solid #5F9FCF;">
								            	<g:link controller="moveTech" action="signOut" style="color: #328714">Log out</g:link>
								            </div>
								            <div style="float:right; margin:5px; width:20%; border:1px solid #5F9FCF;">
								            	<a href="#" style="color: #328714">Search</a>
								            </div>
								            </div>
              						
              					<div style="float:left; width:100%;"><div style="text-align:left; padding-left:10px; color:#990000;"><b>Currently Logged in as:</b></div>
              					<div style="float:left; width:100%;"><div style="text-align:left; padding-left:35px;"><b>${projectTeam}</b></div></div>
              					<div style="float:left; width:100%;"><div style="text-align:left; padding-left:35px;"><b>${project}</b></div></div>
              					<div style="float:left; width:100%;"><div style="text-align:left; padding-left:35px;"><b>${location}</b></div></div>
              					
           <div style="float:left; width:100%; margin:5px 0; "><b>My Issues:</b></div>
            <div id="assetTable"style="float:left;width:100%; ">
            <div  style=" width:100%; ">
             <table id="assetTable" style="overflow:scroll;height:80px;">
              <thead>
                <tr>
                  <th>Issue</th>
                  <th>Assigned</th>
                  <th>Priority</th>
                  <th>state</th>
                </tr>
               </thead>
               <tbody>
                  <tr class ="even">
                  <td>Last Usb</td>
                  <td>Team2</td>
                  <td>High</td>
                  <td>Open</td>
                  </tr>
                  <tr class ="odd">
                  <td>Last Usb</td>
                  <td>Team2</td>
                  <td>High</td>
                  <td>Open</td>
                  </tr>
                 </tbody>
                </table>
        </div>
      </g:form>
  </div>
  </div>
  
  </div></body>

</html>
