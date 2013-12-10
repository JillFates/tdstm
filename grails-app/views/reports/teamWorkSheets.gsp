<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="projectHeader" />
<title>Move Team Worksheets </title>
<script type="text/javascript">
		$(document).ready(function() {
		    currentMenuId = "#reportsMenu";
		    $("#reportsMenuId a").css('background-color','#003366')
		});
    	
    	function populateTeams(val) {    	
    			
     	var hiddenBundle = document.getElementById('moveBundle')
		
     	hiddenBundle.value = val

     	var projectId = ${projectInstance?.id}     	

     	if( val == "null") {

     	 var selectObj = document.getElementById('projectTeamId')

      	 //Clear all previous options

	     var l = selectObj.length	    

	     while (l > 1) {

	     l--

	     selectObj.remove(l)

	     }

     	 return false

     	} else {

     	 ${remoteFunction(controller:'moveBundleAsset', action:'getTeamsForBundles', params:'\'bundleId=\' + val +\'&projectId=\'+projectId', onComplete:'assignTeams(e)')}

     	}     	
     }
     
     function assignTeams(e) {    

     	var projectteams = eval('(' + e.responseText + ')') 
     	  	
      	var selectObj = document.getElementById('projectTeamId')

      	//Clear all previous options

	     var l = selectObj.length	    

	     while (l > 1) {

	     l--

	     selectObj.remove(l)

	     }

      	if (projectteams) {

		      // assign project teams

		      var length = projectteams.length

		      for (var i=0; i < length; i++) {

			      var team = projectteams[i]

			      var opt = document.createElement('option')

			      opt.innerHTML = team.name

			      opt.value = team.id

			      try {

				      selectObj.appendChild(opt, null) // standards compliant; doesn't work in IE

			      } catch(ex) {

				      selectObj.appendChild(opt) // IE only

			      }

		      }		          	

      }

     	

     }
     
     function selectTeamFilter(team){
     	document.getElementById('teamFilter').value = team
     }

     

     function changeLocation(val) {		 
     	 document.getElementById('location').value = val
     }

	
     
    </script>
</head>
<body>

<div class="body">
<h1>Move Team Worksheets </h1>
<g:if test="${flash.message}">
	<div class="message">${flash.message}</div>
</g:if>
<div class="dialog">
<table>
	<tbody>
		<tr>
			<td colspan="2"><div class="required"> Fields marked ( * ) are mandatory </div> </td>
		</tr>
		<tr class="prop" id="bundleRow">

			<td valign="top" class="name"><label><b>Bundles:<span style="color: red;">*</span></b></label></td>

			<td valign="top" class="value"><select id="moveBundleId"
				name="moveBundles" onchange="return populateTeams(this.value);">

				<option value="null" selected="selected">Please Select</option>

				<option value="">All Bundles</option>
				<g:each in="${moveBundleInstanceList}" var="moveBundleList">
					<option value="${moveBundleList?.id}">${moveBundleList?.name}</option>
				</g:each>

			</select></td>

		</tr>

		<tr class="prop" id="teamRow">

			<td valign="top" class="name"><label>Teams:</label></td>

			<td valign="top" class="value"><select id="projectTeamId"
				name="projectTeam" onchange="selectTeamFilter(this.value)">

				<option value="null" selected="selected">All Teams</option>

			</select></td>

		</tr>

		<tr>

			<td valign="top" class="name"><label>Location:</label></td>

			<td><input type="radio" name="locations" value="both"
				onclick="changeLocation(this.value)" checked="true" /> Both <input
				type="radio" name="locations" value="source"
				onclick="changeLocation(this.value)" /> Source <input type="radio"
				name="locations" value="target" onclick="changeLocation(this.value)" />
			Target</td>

		</tr>

		<tr>

			<td class="buttonR"><g:jasperReport controller="reports"
				action="teamSheetReport" jasper="workSheetsReport" format="PDF"
				name="Generate">

				<input type="hidden" name="moveBundle" id="moveBundle" value="null" />

				<input type="hidden" name="teamFilter" id="teamFilter" value="" />

				<input type="hidden" name="location" id="location" value="both" />

			</g:jasperReport></td>

		</tr>
	</tbody>
</table>
</div>
</div>
</body>
</html>
