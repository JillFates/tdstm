<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
    <title><g:layoutTitle default="Grails" /></title>
    <link rel="stylesheet" href="${createLinkTo(dir:'css',file:'main.css')}" />
    <link rel="stylesheet" href="${createLinkTo(dir:'css',file:'tds.css')}" />
    <link rel="shortcut icon"
          href="${createLinkTo(dir:'images',file:'tds.ico')}" type="image/x-icon" />
         
    <g:layoutHead />
     <g:javascript library="prototype" />
    <g:javascript library="jquery"/>

    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.accordion.css')}"  />
    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.core.css')}" />
    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.dialog.css')}" />
    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.resizable.css')}"  />
    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.slider.css')}"  />
    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.tabs.css')}"  />
    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.theme.css')}" />
	<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'dropDown.css')}" />
    <jq:plugin name="ui.core"/>
    <jq:plugin name="ui.draggable"/>
    <jq:plugin name="ui.resizable"/>
    <jq:plugin name="ui.dialog"/>
    <% def currProj = session.getAttribute("CURR_PROJ");
    def projectId = currProj.CURR_PROJ ;
    def currProjObj;
    if( projectId != null){
      currProjObj = Project.findById(projectId)
    }
    %>
   <script type="text/javascript">
     var reportName="";
      $(document).ready(function(){
        $("#dialog").dialog({ autoOpen: false });
      });
      
      $(document).ready(function(){
        $("#reportDialog").dialog({ autoOpen: false });
      });
      $(document).ready(function(){
        $("#rackLayoutDialog").dialog({ autoOpen: false });
      });
   function showReportDialog(e) {

     	var moveBundles = eval('(' + e.responseText + ')')     	

      	var report = reportName

      	var selectObj

      	if (report == "Rack Layout") {

      	selectObj = document.getElementById('bundleId')

      	}else {

      		selectObj = document.getElementById('moveBundleId')

      	}

      	

      	//Clear all previous options

	     var l = selectObj.length	    

	     while (l > 2) {

	     l--

	     selectObj.remove(l)

	     }

      	if (moveBundles) {

		      // assign move bundles

		      var length = moveBundles.length

		      for (var i=0; i < length; i++) {

			      var bundle = moveBundles[i]

			      var opt = document.createElement('option');

			      opt.innerHTML = bundle.name

			      opt.value = bundle.id

			      try {

				      selectObj.appendChild(opt, null) // standards compliant; doesn't work in IE

			      } catch(ex) {

				      selectObj.appendChild(opt) // IE only

			      }

		      }

		          	

      }

       if (report == "Rack Layout") {

      		$("#rackLayoutDialog").dialog('option', 'width', 500)

      		$("#rackLayoutDialog").dialog( "open" )

      	}else {

      		$("#reportDialog").dialog('option', 'width', 500)

      		$("#reportDialog").dialog( "open" )

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

			      var opt = document.createElement('option');

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

     

     function assignRacks(e) {

     

     	var racks = eval('(' + e.responseText + ')')   	

      	

      	var selectObj = document.getElementById('rackId')

      	//Clear all previous options

	     var l = selectObj.length	    

	     while (l > 1) {

	     l--

	     selectObj.remove(l)

	     }

      	if (racks) {

		      // assign project teams

		      var length = racks.length

		      for (var i=0; i < length; i++) {

			      var team = racks[i]

			      var opt = document.createElement('option');

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

     	 ${remoteFunction(action:'getTeamsForBundles', params:'\'bundleId=\' + val +\'&projectId=\'+projectId', onComplete:'assignTeams(e)')}

     	}

     }   

     

     function populateRacks(val) {

     var hiddenBundle = document.getElementById('rackMoveBundle')

     	hiddenBundle.value = val

     	var projectId = ${projectInstance?.id}     	

     	if( val == "null") {

     	 var selectObj = document.getElementById('rackId')

      	 //Clear all previous options

	     var l = selectObj.length	    

	     while (l > 1) {

	     l--

	     selectObj.remove(l)

	     }

     	 return false

     	} else {

     	 ${remoteFunction(action:'getRacksForBundles', params:'\'bundleId=\' + val +\'&projectId=\'+projectId', onComplete:'assignRacks(e)')}

     	}

     } 

     

      

     function generateReportSelect(reportId){

     reportName = reportId

     ${remoteFunction(action:'getBundleListForReportDialog', params:'\'reportId=\'+reportId', onComplete:'showReportDialog(e)' )}""

     }

     

     function selectTeamFilter(team){

     var teamFilter = document.getElementById('teamFilter')

     teamFilter.value = team

     }

     

     function changeLocation(val)

     {

     	var location = document.getElementById('location')

     	location.value = val

     

     }

     function selectRecordsPerPage(val)

     {

     var count = document.getElementById('recordsPerPage');

     count.value = val;

     }
    </script>
  </head>

  <body>
     <div id="reportDialog" title="Team Worksheets" style="display:none;">

     <table id="reportTable">

     	<tbody>

     		<tr class="prop" id="bundleRow">

                <td valign="top" class="name">

                  <label>Bundles:</label>

                </td>

                <td valign="top" class="value"><select id="moveBundleId"

                                 name="moveBundle" onchange="return populateTeams(this.value)">

                    <option value="null" selected="selected">Please Select</option>

                    <option value="" >All Bundles</option>

                                      

                  </select>

               </td>   

              </tr>

              <tr class="prop" id="teamRow">

                <td valign="top" class="name">

                  <label>Teams:</label>

                </td>

                <td valign="top" class="value"><select id="projectTeamId"

                                 name="projectTeam" onchange="selectTeamFilter(this.value)">

                    <option value="null" selected="selected">All Teams</option>                   

                                      

                  </select>

               </td> 

              </tr>

              <tr>

              	<td valign="top" class="name">

                  <label>Location:</label>

                </td>

                <td>

                	<input type="radio" name="location" value="both" onclick="changeLocation(this.value)" checked="true"/> Both

                	<input type="radio" name="location" value="source" onclick="changeLocation(this.value)"/> Source

                	<input type="radio" name="location" value="target" onclick="changeLocation(this.value)"/> Target               	

                </td>                

              </tr>

              <tr>

                <td class="buttonR"><g:jasperReport controller="project" action="teamSheetReport" jasper="teamWorksheetReport" format="PDF" name="Generate"  >

                	<input type="hidden" name="moveBundle" id="moveBundle" value="" />

                	<input type="hidden" name="teamFilter" id="teamFilter" value="" />

                	<input type="hidden" name="location" id="location" value="both" />

                	</g:jasperReport>

                	</td>

              </tr>

     	</tbody>

     </table>

  	</div>

  	

  	  <div id="rackLayoutDialog" title="Rack Layout" style="display:none;">

     <table id="rackLayoutTable">

     	<tbody>

     		<tr class="prop" id="bundleRow">

                <td valign="top" class="name">

                  <label>Bundles:</label>

                </td>

                <td valign="top" class="value">

                

                <select id="bundleId"

                                 name="moveBundle" onchange="return populateRacks(this.value)">

                    <option value="null" selected="selected">Please Select</option>

                    <option value="" >All Bundles</option>

                                      

                  </select>

               </td>   

              </tr>

              <tr class="prop" id="teamRow">

                <td valign="top" class="name">

                  <label>Racks:</label>

                </td>

                <td valign="top" class="value">

                <select id="rackId" multiple="multiple" name="rack"  style="width: 100px; height: 100px;">

                

                    <option value="null" selected="selected">All Racks</option>                   

                                      

                  </select>

               </td> 

              </tr>

              <tr>

              <td colspan="2"><div style="width:100%;height:10px;float:left;"> Hold [Ctrl] when clicking to choose multiple racks  </div></td>

              </tr>

              <tr>

              	<td valign="top" class="name">

                  <label>Racks/Page:</label>

                </td>

                <td>

                	<g:select id="rackPerPage" from="${1..6}" value="3" onChange="selectRecordsPerPage(this.value)" />              	

                </td>                

              </tr>

              <tr>

                <td class="buttonR"><g:jasperReport controller="project" action="rackLayoutReport" jasper="teamWorksheetReport" format="PDF" name="Generate"  >

                	<input type="hidden" name="moveBundle" id="rackMoveBundle" value="" />

                	<input type="hidden" name="rack" id="rack" value="" />

                	<input type="hidden" name="recordsPerPage" id="recordsPerPage" value="3" />

                	</g:jasperReport>

                	</td>

              </tr>

     	</tbody>

     </table>

  	</div>

    

  
    
    <div class="main_body">

      <div class="header"><img
          src="${createLinkTo(dir:'images',file:'tds.jpg')}" style="float: left;">
        <div class="header_right"><br />
          <div style="font-weight: bold; color: #0000FF"><jsec:isLoggedIn>
              <strong>Welcome &nbsp;&nbsp;<jsec:principal />&nbsp;! </strong>
              &nbsp;<g:link controller="auth" action="signOut"
                            style="color: #328714">sign out</g:link>
          </jsec:isLoggedIn></div>
        </div>
      </div>

      <div class="top_menu_layout">
        <div class="menu1">
          <ul>
            <li><g:link class="home" controller="projectUtil">Project Manager</g:link></li>
            <jsec:hasRole name="ADMIN">
              <li><g:link class="home" controller="auth" action="home">Administration </g:link> </li>
            </jsec:hasRole>
          </ul>
        </div>
      </div>
      <div class="title">&nbsp;Transition Manager <g:if test="${currProjObj}"> - ${currProjObj.name} ( ${currProjObj.projectCode} ) </g:if></div>
      <!--
<div class="menu1">
<ul>
    <li><g:link class="home" controller="projectUtil">Main</g:link></li>
    <li><g:link class="home" controller="projectUtil"
        action="searchList">Search</g:link></li>
    <jsec:hasRole name="PROJECT_ADMIN">
        <li><g:link class="home" controller="project" action="create">Add</g:link>
        </li>
    </jsec:hasRole>
    <li><a href="#">Import/Export</a></li>
</ul>
</div>
      -->
      <div class="menu2">
      <ul>
        <li><g:link class="home" controller="projectUtil">Project </g:link> </li>
        <li><g:link class="home" controller="person" action="projectStaff" params="[projectId:currProjObj?.id]" >Staff</g:link></li>
        <li><g:link class="home" controller="assetEntity" params="[projectId:currProjObj?.id]">Assets </g:link></li>
		<li><g:link class="home" controller="assetEntity" action="assetImport" params="[projectId:currProjObj?.id]">Import/Export</g:link> </li>
        <li><a href="#">Contacts </a></li>
        <li><a href="#">Applications </a></li>
        <li><g:link class="home" controller="moveBundle" params="[projectId:currProjObj?.id]">Move Bundles</g:link></li>
        <li>  
          	<div id="menubar">
 			<div id="menu1" class="menu_new">Reports<ul>    
    	      <li><a href="#" onclick="generateReportSelect('Team Worksheets')">Team Worksheets</a></li>
    	      <li><a href="#" onclick="generateReportSelect('Rack Layout')">Rack Layout</a></li>
              </ul>
            </div>
            </div>
                	
        </li>
      </ul>
    </div>
      
      <div class="main_bottom"><g:layoutBody /></div>
    </div>
  </body>
</html>
