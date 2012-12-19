<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="projectHeader" />
<title>Login Badges </title>


<script type="text/javascript"><!--
//=============================================================================
// PRINT HERE
//=============================================================================
function startprintjob(e)
{
var job = window.TF.CreateJob();
var form = window.document.loginLabelForm;
var jobdata = job.NewJobDataRecordSet();
    job.RepositoryName = document.getElementById('urlPath').value;       			 
    job.ProjectName = form.PrjName.value;     
    job.FormName = form.FormName.value;                   
    job.PrinterName = form.PrinterName.value;
    // THIS IS THE PLACE TO ADD YOUR DATA
    jobdata.ClearRecords();  
    var teamMembers = eval("(" + e.responseText + ")")
    if(	teamMembers.length == 1 && teamMembers[0].flashMessage != null) {
    	document.getElementById('teamNotFound').style.display = "block"
    	document.getElementById('warnMessage').innerHTML = teamMembers[0].flashMessage
    	return flase;
    }
    document.getElementById('teamNotFound').style.display = "none"   
    for(var label = 0; label < teamMembers.length; label++) {
    	jobdata.AddNewRecord();                					
    	jobdata.SetDataField('userName', teamMembers[label].name); 
    	jobdata.SetDataField('teamName', teamMembers[label].teamName);       
    	jobdata.SetDataField('moveBundle', teamMembers[label].bundleName); 
    	jobdata.SetDataField('barCode', teamMembers[label].barCode);    	   		
    	
    }
    // now we print one copy of the label with default settings
    try 
    {
    	job.PrintForm();
    }
    catch (e)
    {
	    alert ("TFORMer returned an error!" + 
	           "\nError description: " + e.description + 
	           "\nError name: " + e.name + 
	           "\nError number: " + e.number + 
	           "\nError message: " + e.message);
    }

}

//=============================================================================
// Add a new option to select element
//=============================================================================
function AddOption (selElement, text, value)
{
  opt = new Option(text, value, false, true);
  selElement.options[selElement.length] = opt;
}

//=============================================================================
// Set default data for TFORMer Runtime Properties
//=============================================================================
function InitData()
{
	if(${browserTest} == true) {
		var limit = document.forms[0].elements.length;
		for (i=0;i<limit;i++) {
			document.forms[0].elements[i].disabled = true;
		}
	}
		
	//To check the Instructions for enable the Clean Button
	var printButton = document.getElementById('printButton');
	if(!printButton.disabled){ 
		printButton.focus();
	}
	var form = window.document.loginLabelForm;
	var path = window.location.href;
	var i = -1;
	// the following code evaluates the path to the demo repository
	for (n=1; n<=3; n++)
	{
		i = path.lastIndexOf('/');
		if (i != -1)
		{
			path = path.substring(0,i)                              // one directory level up
		}
	}
	if (path.substr (0, 8) == "file:///")			                  // do not use URL-style for Repository file name - remove file:///
	    path = path.substr (8);
    path= unescape(path);	                        					// unescape!
    form.RepPath.value 	= path + '/Demo Repository/Demos.tfr';  // repository name
    form.PrjName.value 	= 'TFORMer_Runtime_Examples';						// project name
    form.FormName.value = 'BarcodeLabels';											// form name
    form.PrinterName.value = ''	
	// get list of installed printers
	var dropdown = document.getElementById("Printers");
	window.TF.RefreshOSPrinters();
	var def = 0;
	for (i = 0; i < window.TF.GetOSPrintersCount(); i++) 
	{
	  AddOption (dropdown, window.TF.GetOSPrinter(i), window.TF.GetOSPrinter(i));
	  if (window.TF.GetOSPrinter(i) == window.TF.GetOSDefaultPrinter())
	    def = i;
	}
	dropdown.options[def].selected = true;
	//mySelect(dropdown);
}

//=============================================================================
// Handle Browse Button
//=============================================================================
function FileFind_onchange()
{
var form = window.document.loginLabelForm;

  form.RepPath.value = form.FileFind.value;
}

//=============================================================================
// The selected dprinter has changed
//=============================================================================
function mySelect(x)
{
	document.getElementById("PrinterName").value = x.options[x.selectedIndex].value;
	
}

var sHint = "C:\\temp\\output";
--></script>

<script type="text/javascript">
    
    	function populateTeams(val) {    	
    			

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

     	 ${remoteFunction(controller:'moveBundleAsset',action:'getTeamsForBundles', params:'\'bundleId=\' + val +\'&projectId=\'+projectId', onComplete:'assignTeams(e)')}

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
     
     function changeLocation(val) {		 
     	 document.getElementById('location').value = val
     }
	/*----------------------------------------------
	 * To get the labels print for selected Bundles and team
	 * @author Srinivas
	 * @return call startPrintJob() method by passing the list of teamMembers
	 * -----------------------------------------------*/ 
	 function generateLabels() {
	 var projectId = ${projectInstance?.id}
	 var moveBundle = document.getElementById( 'moveBundleId' ).value
	 var team = document.getElementById( 'projectTeamId' ).value
	 var location = document.getElementById( 'location' ).value
	${remoteFunction(action:'getLabelBadges', params:'\'moveBundle=\' + moveBundle +\'&project=\'+projectId +\'&teamFilter=\'+team+\'&location=\'+location', onComplete:'startprintjob(e)')}
	 }
		$(document).ready(function() {
		    currentMenuId = "#reportsMenu";
		    $("#reportsMenuId a").css('background-color','#003366')
		});
     
    </script>
</head>
<body >
<div class="body">
<h1>Login Badges </h1>
<g:if test="${flash.message}">
	<div class="message">${flash.message}</div>
</g:if>
<g:if test="${browserTest == true}" >
			<div style="color: red;">
					<ul>
					<li>This report must be done using Internet Explorer</li>
					</ul>
			</div>
			</g:if>
<div class="dialog">
<object id="TF" classid="clsid:18D87050-AAC9-4e1a-AFF2-9D2304F88F7C" codebase="${resource(dir:'resource',file:'TFORMer60.cab')}"></object>
<g:form	name="loginLabelForm" >
<div id="teamNotFound" class="message" style="display:none">
					<ul>
					<span id="warnMessage" name="warnMessage"></span>
					</ul>
			</div>
<table>
	<tbody>
		<tr>
			<td colspan="2"><div class="required"> Fields marked ( * ) are mandatory </div> </td>
		</tr>
		<tr class="prop" id="bundleRow">

			<td valign="top" class="name"><label><b>Bundles:<span style="color: red;">*</span></b> </label><input type="hidden" name="urlPath" id="urlPath" value="<g:resource dir="resource" file="login_badge.tff" absolute="true"/>" />

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
				name="projectTeam" >

				<option value="null" selected="selected">All Teams</option>

			</select></td>

		</tr>

		<tr>

			<td valign="top" class="name"><label>Location:</label>
			<input type="hidden" name="location" id="location" value="both" />
			</td>

			<td><input type="radio" name="locations" value="both"
				onclick="changeLocation(this.value)" checked="true" /> Both <input
				type="radio" name="locations" value="source"
				onclick="changeLocation(this.value)" /> Source <input type="radio"
				name="locations" value="target" onclick="changeLocation(this.value)" />
			Target</td>
		</tr>
		<tr>
			<td>
					<input type= "hidden" id="RepPath" name="RepPath" />
	      	  		<input type= "hidden" name="PrjName" id="PrjName" />
	          		<input type= "hidden" name="FormName" id="FormName" />
	          		<b>Printer:<span style="color: red;">*</span></b> 
			</td>
			<td>
			<select type= "hidden" id="Printers" name="Printers"  onChange="javascript:mySelect(this);"/>
          				<input type= "hidden" name="PrinterName" id="PrinterName" />
			</td>
			</tr>

		<tr>

			<td class="buttonR"><input type="button" id="printButton"
				value="Print" onclick="generateLabels();" /></td>

		</tr>
	</tbody>
</table>
</g:form>
</div>
</div>
<script type="text/javascript">
InitData()
</script>
</body>

</html>
