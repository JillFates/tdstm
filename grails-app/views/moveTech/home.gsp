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
<script type="text/javascript">    
		var timeInterval;
		function submittheform() {
			document.bundleTeamAssetForm.submit();
		}
</script>
</head>
<body>
	<div id="spinner" class="spinner" style="display: none;"><img
		src="${createLinkTo(dir:'images',file:'spinner.gif')}" alt="Spinner" />
	</div>
	<div class="mainbody" style="width: 100%;" >
		<div class="colum_techlogin" style="float:left;">
				<div class="border_bundle_team">
          			<div style="float:left; width:200px; margin-left:13px;background-color:none;">
		        	<a href="#" style="height:21px; width:45px; float:left; margin:auto 0px;color: #5b5e5c; border:1px solid #5b5e5c; margin:0px;background:#aaefb8;padding:auto 0px;text-align:center;">Home</a>
					<g:link action="assetTask" params='["bundle":bundle,"team":team,"location":location,"project":project,"tab":"Todo"]' style="height:21px; width:60px; float:left; margin:auto 0px;color: #5b5e5c; border:1px solid #5b5e5c; margin:0px;padding:auto 0px;text-align:center;">My Task</g:link>
					<a href="#" style="height:21px; width:63px; float:left; margin:auto 0px;color: #5b5e5c; border:1px solid #5b5e5c; margin:0px;padding:auto 0px;text-align:center;">Asset</a>
				</div>		
			<div class="w_techlog">
				<g:form method="post" name="bundleTeamAssetForm" action="assetSearch">      					
					        <input name="bundle" type="hidden" value="${bundle}" />
							<input name="team" type="hidden" value="${team}" />
							<input name="location" type="hidden" value="${location}" />
							<input name="project" type="hidden" value="${project}" />
							<input name="tab" type="hidden" value="Todo" />	
							<input name="home" type="hidden" value="home" />									
				<div style="float:left; width:100%; margin:5px 0; ">              								
					<table style="border:0px;">
						<tr><td><g:link controller="moveTech" action="signOut" style="color: #5b5e5c; border:1px solid #5b5e5c; margin:2px 5px 5px 5px; height:15px; padding:1px 2px 1px 3px; width:50px; background:#aaefb8; float:left;">Log out</g:link></td>
							<td style="text-align:right;"><a href="#" style="color: #328714;"><input type="text" size="12" value="" name="search" onchange="submittheform()" onkeyup="timeInterval = setTimeout('submittheform()',1500)" onkeydown="if(timeInterval){clearTimeout(timeInterval)}"/></a>&nbsp;<img	src="${createLinkTo(dir:'images',file:'search.png')}"/></td>
						</tr>
					  </table>
				</div>
				<div style="float:left; width:100%;" id="mydiv" onclick="document.getElementById('mydiv').style.display = 'none'">						            
			   					<g:if test="${flash.message}">
								<div style="color: red;"><ul><li>${flash.message}</li></ul></div>
								</g:if> 
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
