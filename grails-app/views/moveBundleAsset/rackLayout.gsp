<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="projectHeader" />
<title>Rack Layout</title>
<g:javascript library="prototype" />
<script type="text/javascript">
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
     
     function selectRecordsPerPage(val)

     {

     var count = document.getElementById('recordsPerPage');

     count.value = val;

     }
     
    </script>

</head>
<body>

<div class="body">
<h1>Rack Layout</h1>
<g:if test="${flash.message}">
	<div class="message">${flash.message}</div>
</g:if>
<div class="dialog">
<table>
	<tbody>

		<tr class="prop" id="bundleRow">

			<td valign="top" class="name"><label>Bundles:</label></td>

			<td valign="top" class="value"><select id="bundleId"
				name="moveBundle" onchange="return populateRacks(this.value)">

				<option value="null" selected="selected">Please Select</option>

				<option value="">All Bundles</option>

				<g:each in="${moveBundleInstanceList}" var="moveBundleList">
					<option value="${moveBundleList?.id}">${moveBundleList?.name}</option>
				</g:each>




			</select></td>

		</tr>

		<tr class="prop" id="teamRow">

			<td valign="top" class="name"><label>Racks:</label></td>

			<td valign="top" class="value"><select id="rackId"
				multiple="multiple" name="rack" style="width: 100px; height: 100px;">



				<option value="null" selected="selected">All Racks</option>



			</select></td>

		</tr>

		<tr>

			<td colspan="2">
			<div style="width: 100%; height: 10px; float: left;">Hold
			[Ctrl] when clicking to choose multiple racks</div>
			</td>

		</tr>

		<tr>

			<td valign="top" class="name"><label>Racks/Page:</label></td>

			<td><g:select id="rackPerPage" from="${1..6}" value="3"
				onChange="selectRecordsPerPage(this.value)" /></td>

		</tr>

		<tr>

			<td class="buttonR"><g:jasperReport controller="moveBundleAsset"
				action="rackLayoutReport" jasper="teamWorksheetReport" format="PDF"
				name="Generate">

				<input type="hidden" name="moveBundle" id="rackMoveBundle"
					value="null" />

				<input type="hidden" name="rack" id="rack" value="" />

				<input type="hidden" name="recordsPerPage" id="recordsPerPage"
					value="3" />

			</g:jasperReport></td>

		</tr>

	</tbody>
</table>
</div>

</div>
</body>
</html>
