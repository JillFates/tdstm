<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<head>
<style type="text/css">
P, td  {MARGIN-TOP: 0px; MARGIN-BOTTOM: 0px; FONT-FAMILY: Arial; FONT-SIZE:10pt; }
h4, h2 {COLOR: #c21428; }
h3     {MARGIN-TOP: 0px; MARGIN-BOTTOM: 0px; COLOR: #ffffff; FONT-SIZE: 18pt;}

</style>
<script type="text/javascript" language="Javascript1.2">
var sHint = "C:\\temp\\output";
//=============================================================================
// PRINT HERE
//=============================================================================
function startprintjob()
{

var job = window.TF.CreateJob();

var form = window.document.assetSearchForm;
var jobdata = job.NewJobDataRecordSet();

    job.RepositoryName = "C:\\Documents and Settings\\All Users\\Application Data\\TEC-IT\\TFORMer\\6.0\\Examples\\Demo Repository\\Demos.tfr";       			 
    job.ProjectName 		= form.PrjName.value;     
    job.FormName 		    = form.FormName.value;                   
    job.PrinterName 		= form.PrinterName.value; 

    // THIS IS THE PLACE TO ADD YOUR DATA

    jobdata.ClearRecords();               								// delete all previous assigned data - start with empty record set

    jobdata.AddNewRecord();                							// add a new record. Each record prints the Detail Band of the form
    jobdata.SetDataField('ArticleName', '105C31D');  // set the value of the data-field ArticleName
    jobdata.SetDataField('ArticleNo',   '1235');       // set the value of the data-field ArticleNo
    jobdata.SetDataField('ArticlePrice','39');    	   		// set the value of the data-field ArticlePrice

    jobdata.AddNewRecord();                							// add second data record
    jobdata.SetDataField('ArticleName', '105D74C CSMEDI');
    jobdata.SetDataField('ArticleNo',   '778920');
    jobdata.SetDataField('ArticlePrice','39');

    jobdata.AddNewRecord();				   										// add second data record
    jobdata.SetDataField('ArticleName', 'AIX Console HMC3');
    jobdata.SetDataField('ArticleNo',   '775116');
    jobdata.SetDataField('ArticlePrice','75');
	
    jobdata.AddNewRecord();				   										// add fourth data record
    jobdata.SetDataField('ArticleName', '105D74C CSMEDI');
    jobdata.SetDataField('ArticleNo',   '549896');
    jobdata.SetDataField('ArticlePrice','200');

    jobdata.AddNewRecord();				   										// add fifth data record
    jobdata.SetDataField('ArticleName', 'CED14P');
    jobdata.SetDataField('ArticleNo',   '458862');
    jobdata.SetDataField('ArticlePrice','400');

    jobdata.AddNewRecord();				  										// add sixth data record
    jobdata.SetDataField('ArticleName', '20" LCD model');
    jobdata.SetDataField('ArticleNo',   '445866');
    jobdata.SetDataField('ArticlePrice','600');


		

    // now we print one copy of the label with default settings
    try 
    {
    	job.PrintForm();
	    alert ("Print Job finished!");
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
var form = window.document.assetSearchForm;
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
    form.PrinterName.value = ''																	// use default printer

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
		// add TFORMer standard output formats
	  AddOption (dropdown, "PDF-File", "PDF:" + sHint + ".PDF");
	  AddOption (dropdown, "PostScript-File", "PS:" + sHint + ".PS");
	  AddOption (dropdown, "HTML-File", "HTML:" + sHint + ".HTML");
	  AddOption (dropdown, "Zebra (ZPL-II)", "ZPL:" + sHint + ".ZPL");
	  AddOption (dropdown, "Image (BMP)", "IMGBMP:" + sHint + ".BMP");
	  AddOption (dropdown, "Image (GIF)", "IMGGIF:" + sHint + ".GIF");
	  AddOption (dropdown, "Image (JPG)", "IMGJPG:" + sHint + ".JPG");
	  AddOption (dropdown, "Image (PCX)", "IMGPCX:" + sHint + ".PCX");
	  AddOption (dropdown, "Image (PNG)", "IMGPNG:" + sHint + ".PNG");
	  AddOption (dropdown, "Image (TGA)", "IMGTGA:" + sHint + ".TGA");
	  AddOption (dropdown, "Image (TIF)", "IMGTIF:" + sHint + ".TIF");
	  AddOption (dropdown, "Image (TIF Multipage)", "IMGMULTITIF:" + sHint + ".TIF");
	  dropdown.options[def].selected = true;
	  mySelect(dropdown);
}

//=============================================================================
// Handle Browse Button
//=============================================================================
function FileFind_onchange()
{
var form = window.document.assetSearchForm;

  form.RepPath.value = form.FileFind.value;
}

//=============================================================================
// The selected dprinter has changed
//=============================================================================
function mySelect(x)
{
	document.getElementById("PrinterName").value = x.options[x.selectedIndex].value;
}

</script>



	<script type="text/javascript">	
	
	   
        function serverInfo(e){        
        var loc = document.assetSearchForm.location.value;
      	var location;
        var room;
        var rack;
        var pos;       
        var asset = eval('(' + e.responseText + ')');    
        if(loc == 's'){
        location = asset[0].item.sourceLocation
        room = asset[0].item.sourceRoom
        rack = asset[0].item.sourceRack
        pos = asset[0].item.sourceRackPosition
        }else{
        location = asset[0].item.targetLocation
        room = asset[0].item.targetRoom
        rack = asset[0].item.targetRack
        pos = asset[0].item.targetRackPosition
        }     
        var htmlBody = '<table ><thead></thead><tbody>'+
        '<tr><td class="asset_details_block"><b>Asset Tag:</b>  '+asset[0].item.assetTag+'</td></tr>'+
		'<tr><td class="asset_details_block"><b>Asset Name:</b>  '+asset[0].item.assetName+'</td></tr>'+
		'<tr><td class="asset_details_block"><b>Current State:</b>  '+asset[0].state+'</td></tr>'+		
		'<tr><td class="asset_details_block"><b>Serial Number:</b>  '+asset[0].item.serialNumber+'</td></tr>'+
		'<tr><td class="asset_details_block"><b>Model:</b>  '+asset[0].item.model+'</td></tr>'+
		'<tr><td class="asset_details_block"><b>Location:</b>  '+location+'</td></tr>'+
		'<tr><td class="asset_details_block"><b>Room:</b>  '+room+'</td></tr>'+
		'<tr><td class="asset_details_block"><b>Rack/Position:</b>  '+rack+'/'+pos+'</td></tr>'+
		'<tr><td class="asset_details_block"><b>PDU:</b>  '+asset[0].item.powerPort+'</td></tr>'+
		'<tr><td class="asset_details_block"><b>NIC:</b>  '+asset[0].item.nicPort+'</td></tr>'+
		'<tr><td class="asset_details_block"><b>HBA:</b>  '+asset[0].item.hbaPort+'</td></tr>'+
		'</tbody></table>' 
        var getDialogId = document.getElementById('serverInfoDialog')
        getDialogId.innerHTML = htmlBody
        $("#serverInfoDialog").dialog('option', 'width', 200)                     
		$("#serverInfoDialog").dialog('option', 'position', ['left','top']);
        $('#serverInfoDialog').dialog('open');
        }
        
        function validation(){      
        var enterNote = document.assetSearchForm.enterNote.value;      
        if(enterNote == ""){
      	alert('Please enter note');
        document.assetSearchForm.enterNote.focus();   
      	return false;
      	}else{
      	if(confirm('Are you sure???')){
      	return true;
      	}
      	}   
      	}  
      	function doTransition(){
      	if(validation()){
      	document.assetSearchForm.action="placeHold";
      	document.assetSearchForm.submit();
      	}else {
      	return false;
      	}
      	}
      
      	function clean(){
      	if(doCheckValidation()){  
   		document.assetSearchForm.action = "cleaning";      
   		document.assetSearchForm.submit();
   		}else{
   		return false;
   		}
      	}
      
        function doCheckValidation(){
	    var j = 0;
        var boxes = document.getElementsByTagName('input'); 
		for (i = 0; i < boxes.length; i++) {
          if (boxes[i].type == 'checkbox'){
               if(boxes[i].checked == false){
       			j=1;
       		 }
           }
     	}      
        if(j == 0){     
        return true;
        }else{
        alert("Please select all instructions");                                   
        return false;
        }     
        }
      	function setFocus(){ 
        document.assetSearchForm.textSearch.focus();
        }
           
    </script>
</head>
<body onload="InitData()">

<div id="serverInfoDialog" title="Server Info" onclick="$('#serverInfoDialog').dialog('close')">
</div>
		<object id="TF" classid="clsid:18D87050-AAC9-4e1a-AFF2-9D2304F88F7C" viewastext></object>
		<div id="spinner" class="spinner" style="display: none;"><img src="${createLinkTo(dir:'images',file:'spinner.gif')}" alt="Spinner" /></div>
			<div class="mainbody" style="width: 100%;">
			<div class="colum_techlogin" style="float: left;">
			<div style="float: left; width: 97.5%; margin-left: 20px;">
		        	<g:link params='["bundle":bundle,"team":team,"location":location,"project":project,"user":"ct"]' style="height:21px; width:45px; float:left; margin:auto 0px;color: #5b5e5c; border:1px solid #5b5e5c; margin:0px;padding:auto 0px;text-align:center;">Home</g:link>
					<g:link action="cleaningAssetTask" params='["bundle":bundle,"team":team,"location":location,"project":project,"tab":"Todo"]' style="height:21px; width:60px; float:left; margin:auto 0px;color: #5b5e5c; border:1px solid #5b5e5c; margin:0px;padding:auto 0px;text-align:center;">My Task</g:link>
					<a href="#" style="height:21px; width:63px; float:left; margin:auto 0px;color: #5b5e5c; border:1px solid #5b5e5c; margin:0px;background:#aaefb8;padding:auto 0px;text-align:center;">Asset</a>
			</div>
			<div class="w_techlog">
			<div style="float: left; width: 100%; margin: 5px 0;">
				<g:form	name="assetSearchForm" action="cleaningAssetSearch">
					<input name="bundle" type="hidden" value="${bundle}" />
					<input name="team" type="hidden" value="${team}" />
					<input name="location" type="hidden" value="${location}" />
					<input name="project" type="hidden" value="${project}" />
					<input name="search" type="hidden" value="${search}" />
					<input name="assetCommt" type="hidden" value="${assetCommt}" />
					<input name="label" type="hidden" value="${label}" />
					<input name="actionLabel" type="hidden" value="${actionLabel}"  />
					<input name="user" type="hidden" value="ct" />
					<input name="assetPage" type="hidden" value="assetPage" />
					<table style="border: 0px;">
			<div id="mydiv" onclick="document.getElementById('mydiv').style.display = 'none';">
					<g:if test="${flash.message}">
					<div style="color: red;">
					<ul>
					<li>${flash.message}</li>
					</ul>
					</div>
					</g:if></div>
			<div style="float:right;margin-right:10px;">
				<input type="text" name="textSearch" size="10"/>&nbsp;<img src="${createLinkTo(dir:'images',file:'search.png')}"/>
			</div>
			<div onclick="${remoteFunction(action:'getServerInfo', params:'\'assetId=\'+'+projMap.asset.id,onComplete: 'serverInfo(e)')}">
					
					<g:if test="${projMap}">
					<dl><dt>Asset Tag:</dt><dd>${projMap?.asset?.assetTag}</dd>
					<dt>Asset Name:</dt><dd> ${projMap?.asset?.assetName}</dd>
					<dt>Model:</dt><dd> ${projMap?.asset?.model}</dd>
					<dt>Rack/Pos:</dt><dd> <g:if test="${location == 's'}">${projMap?.asset?.sourceRack}/${projMap?.asset?.sourceRackPosition}</g:if><g:else test="${location == 't'}">${projMap?.asset?.targetRack}/${projMap?.asset?.targetRackPosition}</g:else> </dd>
					</dl>	
					</g:if>
					<g:else>
					<dl><dt>Asset Tag:</dt><dd>&nbsp;</dd>
					<dt>Asset Name:</dt><dd>&nbsp;</dd>
					<dt>Model:</dt><dd>&nbsp;</dd>
					<dt>Rack/Pos:</dt><dd>&nbsp;</dd>
					</dl>
					</g:else>
			</div>	
					<g:if test="${projMap}">	
					<tr>
					<td>Labels: <select name="labels">
					<option value="1">1</option>
					<option value="2" selected="selected">2</option>
					<option value="3">3</option>
					</select>
						<input type= "hidden" id="RepPath" name="RepPath">
      	  				<input type= "hidden" name="PrjName" id="PrjName">
          				<input type= "hidden" name="FormName" id="FormName">
	      				<select type= "hidden" id="Printers" name="Printers" onChange="javascript:mySelect(this);"/>
          				<input type= "hidden" name="PrinterName" id="PrinterName">
		  				<input id="button1" onclick="startprintjob()" type="button" value="Start Printing" name="button1" >
		  			</td>
					</tr>
					</g:if>
					<g:else>
					<tr>
					<td>Labels: <select name="labels">
					<option value="1">1</option>
					<option value="2" selected="selected">2</option>
					<option value="3">3</option>
					</select></td>
					<td class="buttonR"><input type="button" value="Print" disabled="disabled"/></td>
					</tr>
					</g:else>		
					<tr>
					<td><strong>Instructions</strong></td>
					<td><strong>Confirm</strong></td>
					</tr>
					<g:each status="i" in="${assetCommt}" var="comments">
					<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
					<td >${comments.comment}</td>
					<g:if test="${comments.mustVerify == 1}">
					<td><g:checkBox name="myCheckbox" value="${false}" /></td>
					</g:if>
					<g:else>
					<td></td>
					</g:else>
					</tr>
					</g:each>		
					<g:if test="${actionLabel}">
					<tr>
					<td class="buttonR" style="text-align: right;" colspan="2"><input type="button" value="${actionLabel}" onclick="return clean()" /></td>
					</tr>
					</g:if>
					<table>
					<tr>
					<td><textarea rows="2" cols="110" title="Enter Note..." name="enterNote"></textarea></td>
					</tr>
					<g:if test="${projMap}">
					<tr>
					<td class="buttonR" style="text-align: right;"><input type="button"
					value="Place on HOLD" onclick="return doTransition()" /></td>
					<tr>
					</g:if>
					</table>
					</table>
				</g:form>
		</div>
		</div>
		</div>
		</div>
<script>setFocus();</script>		
</body>
</html>
