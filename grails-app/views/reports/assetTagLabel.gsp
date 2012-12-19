<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="projectHeader" />
<title>Asset Tags </title>

<script type="text/javascript">
//=============================================================================
// PRINT HERE
//=============================================================================
function startprintjob(e)
{
var job = window.TF.CreateJob();
var form = window.document.assetTagLabelForm;
var jobdata = job.NewJobDataRecordSet();
    job.RepositoryName = document.getElementById('urlPath').value;       			 
    job.ProjectName = form.PrjName.value;     
    job.FormName = form.FormName.value;                   
    job.PrinterName = form.PrinterName.value;
    // THIS IS THE PLACE TO ADD YOUR DATA
    jobdata.ClearRecords();  
    var reportData = eval("(" + e.responseText + ")")
    var assetsList = reportData[0]
    if(	assetsList.flashMessage != null) {
    	document.getElementById('teamNotFound').style.display = "block"
    	document.getElementById('warnMessage').innerHTML = assetsList.flashMessage
    	return flase;
    } else {
	    document.getElementById('teamNotFound').style.display = "none"   
	    for(var i = 0; i < assetsList.length; i++) {
	    	jobdata.AddNewRecord();                					
	    	jobdata.SetDataField('assetName', assetsList[i].assetName);       
	    	jobdata.SetDataField('assetTag', assetsList[i].assetTag); 
	    	jobdata.SetDataField('rack', assetsList[i].rack);
	    	
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
	var form = window.document.assetTagLabelForm;
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
	if (path.substr (0, 8) == "file:///")			                // do not use URL-style for Repository file name - remove file:///
	    path = path.substr (8);
    path= unescape(path);	                        				// unescape!
    form.RepPath.value 	= path + '/Demo Repository/Demos.tfr';  	// repository name
    form.PrjName.value 	= 'TFORMer_Runtime_Examples';				// project name
    form.FormName.value = 'BarcodeLabels';							// form name
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
var form = window.document.assetTagLabelForm;

  form.RepPath.value = form.FileFind.value;
}

//=============================================================================
// The selected dprinter has changed
//=============================================================================
function mySelect(x)
{
	document.getElementById("PrinterName").value = x.options[x.selectedIndex].value;
	
}

	/*----------------------------------------------
	 * To get the labels print for selected Bundles
	 * @author Lokanada Reddy
	 * @return call startPrintJob() method by passing the list of assets
	 * -----------------------------------------------*/ 
	 function generateLabels() {
	 	var projectId = ${projectInstance?.id}
	 	var moveBundle = document.getElementById( 'moveBundleId' ).value
		${remoteFunction(controller:'moveBundleAsset', action:'getAssetTagLabelData', params:'\'moveBundle=\' + moveBundle +\'&project=\'+projectId ', onComplete:'startprintjob(e)')}
	 }
     
    </script>
</head>
<body >
<div class="body">
<h1>Generate Asset Tag</h1>
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
<g:form	name="assetTagLabelForm" >
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

			<td valign="top" class="name"><label><b>Bundles:<span style="color: red;">*</span></b> </label>
			<input type="hidden" name="urlPath" id="urlPath" value="<g:resource dir="resource" file="assetTag_label.tff" absolute="true"/>" />
			<td valign="top" class="value">
				<select id="moveBundleId" name="moveBundles">
					<option value="null" selected="selected">Please Select</option>
					<option value="all">All Bundles</option>
					<g:each in="${moveBundleInstanceList}" var="moveBundleList">
						<option value="${moveBundleList?.id}">${moveBundleList?.name}</option>
					</g:each>
				</select>
			</td>
		</tr>

		<tr>
			<td>
					<input type= "hidden" id="RepPath" name="RepPath"/>
	      	  		<input type= "hidden" name="PrjName" id="PrjName"/>
	          		<input type= "hidden" name="FormName" id="FormName"/>
	          		<b>Printer:<span style="color: red;">*</span></b> 
			</td>
			<td>
			<select type= "hidden" id="Printers" name="Printers"  onChange="javascript:mySelect(this);"/>
          				<input type= "hidden" name="PrinterName" id="PrinterName"/>
			</td>
			</tr>

		<tr>

			<td class="buttonR"><input type="button" id="printButton"
				value="Print" onclick="generateLabels();"/></td>

		</tr>
	</tbody>
</table>
</g:form>
</div>
</div>
<script type="text/javascript">
currentMenuId = "#reportsMenu";
$("#reportsMenuId a").css('background-color','#003366')
InitData()
</script>
</body>

</html>
