<html>
<head>
<title>Walkthru&gt; Select Rack</title>
<g:javascript library="prototype" />
<g:javascript library="jquery" />
<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'qvga.css')}" />
<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'walkThrough.css')}" />
<script type="text/javascript">
/*-----------------------------------------------------------------
	function to load the Bundles for selected Project via AJAX
	@author : Lokanath Reddy
	@params : Racks list object as JSON
*------------------------------------------------------------------*/
function updateRacks( e ) {
	var racksDetails = eval('(' + e.responseText + ')')
	var racksListBody = $("#racksListBody")
	var body = ""
	if(racksDetails.racksList){
		var length = racksDetails.racksList.length
		for(i = 0; i < length; i++){
			var rackList = racksDetails.racksList[i]
			if(i == 6 && length != 7 ){
		       body += "<TR class=jump><TD align=middle colspan='3'><A class=nav_button name='racklist"+i+"' href='#racklist"+i+"'>Page Down</A></TD></TR>"
        	} else if(i > 6 && (i - 6) % 13 == 0 && (length - 7 ) % 13 != 0){
		        body += "<TR class=jump><TD colSpan=3 align=middle><A class=nav_button href='#select_rack'>Top</A>&nbsp;&nbsp;&nbsp;"+ 
		            "<A class=nav_button href='#racklist"+(i-13)+"'>Page Up</A>&nbsp;&nbsp;&nbsp;"+ 
		            "<A class=nav_button name='racklist"+i+"' href='#racklist"+i+"'>Page Down</A></TD></TR>"
        	}
        	
        	body += "<tr class='asset_ready' onClick='alert(\'Need to be implement\')'>"+
        			"<td>"+(rackList[0] ? rackList[0] : '')+"</td>"+
        			"<td class='center'>"+(rackList[1] ? rackList[1] : '')+"</td>"+
        			"<td class='center'>"+rackList[2]+"</td></tr>"
        }
		body += "<TR class=jump><TD colSpan=3 align=middle>"+
	        	"<A class=nav_button href='#select_rack'>Top</A>&nbsp;&nbsp;&nbsp;"+ 
	            "<A class=nav_button href='#racklist6'>Page Up</A></TD></TR>"
	} else {
		body += "<TR class=jump><TD colSpan=3 align=middle style='color: red;font-weight: bold;'>No records found</TD></TR>"
	}
	racksListBody.html( body );
}
</script>
</head>
<body onload="$('#search').focus()">
<div class="qvga_border">
<a name="select_rack"></a> 
<div class="title">Walkthru&gt; Select Rack</div>
<div class="input_area">

<a class="button" href="startMenu">Start Over</a>

<table>
<tr>
   <td class="label">Location:</td>
   <td class="field">
      <select name="location" id="locationId" class="select"
      	onchange="${remoteFunction(action:'getRacksByLocation', params:'\'location=\' + this.value ', onComplete:'updateRacks(e)')}">
	      <g:each in="${locationsList}" var="locationsList">
	         <option value="${locationsList.location}">${locationsList.location}</option>
	      </g:each>
      </select>
   </td>
</tr>

<tr>
	<td align="center" style="margin-top:8px; margin-bottom:8px;">
		<label>View:</label> 
		<a class="button unselected" href="#start">ToDo</a>
		<a class="button" href="#start">All</a>
	</td>
	<td align="right">
		<label for="search">Search:</label> <input type="text" class="text search" size=8 name="search" id="search"> 
	</td>
</tr>


<tr><td colspan="2">
    <table class="grid" >
    	<thead>
        <tr>
        	<g:sortableColumn property="room" title="Room" params="${sortParams}"/>
        	<g:sortableColumn property="rack" title="Rack" params="${sortParams}"/>
        	<g:sortableColumn property="total" title="Remaining" params="${sortParams}"/>
        </tr>
        </thead>
        <tbody id="racksListBody">
        <g:each in="${racksList}" var="racksList" status="i">
        <g:if test="${i == 6 && racksList.size() != 7 }">
        <TR class=jump>
          <TD align=middle colspan="3"><A class=nav_button name="racklist${i}" href="#racklist${i}">Page Down</A></TD>
        </TR>
        </g:if>
        <g:if test="${i > 6 && (i - 6) % 13 == 0 && (racksList.size() - 7 ) % 13 != 0}">
        <TR class=jump>
          <TD colSpan=3 align=middle><A class=nav_button href="#select_rack">Top</A>&nbsp;&nbsp;&nbsp; 
            <A class=nav_button href="#racklist${i-13}">Page Up</A>&nbsp;&nbsp;&nbsp; 
            <A class=nav_button name="racklist${i}" href="#racklist${i}">Page Down</A> 
        	</TD>
        </TR>
        </g:if>
        <tr class="asset_ready" onClick="alert('Need to be implement')">
        	<td>${racksList[0]}</td><td class="center">${racksList[1]}</td><td class="center">${racksList[2]}</td>
        </tr>
        </g:each>
        <g:if test="${racksList}">
		<TR class=jump>
	    	<TD colSpan=3 align=middle>
	        	<A class=nav_button href="#select_rack">Top</A>&nbsp;&nbsp;&nbsp; 
	            <A class=nav_button href="#racklist6">Page Up</A>
	        </TD> 
		</TR>
		</g:if>
		<g:else>
		<TR class=jump>
	    	<TD colSpan=3 align=middle style="color: red;font-weight: bold;">No records found</TD> 
		</TR>
		</g:else>
		</tbody>
    </table>
</td></tr>
</table>
</div>
</div>
<script type="text/javascript">
$("#locationId").val('${auditLocation}')
</script>
</body>
</html>
		