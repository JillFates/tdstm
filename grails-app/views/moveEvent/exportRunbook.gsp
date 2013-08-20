<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="projectHeader" />
    <title>Export Runbook</title>
  </head>
  <script type="text/javascript">
  </script>
  <body>
   <g:form name="moveEventForm"  action="exportRunbookToExcel">
   <div class="body">
    <h1>Export Runbook</h1>
   
     <div class="dialog">
     <table>
     <tr><td>
        <g:select from="${moveEventList}"  name="eventId"  id="eventList"  optionKey="id"  optionValue="name"  noSelection="['':'please select']"  value="${ session.getAttribute('MOVE_EVENT')?.MOVE_EVENT }"/>
     </td></tr>
     <tr>
           <td class="buttonR">
				<input type="button" class="submit" value="Summary" id="generateId" onclick="generateRunbook();"/>
			</td>
     </tr>
     </table>
    </div>
    
     </div>
     <div id="exportResultId" style="float: left ; display: none; margin-top: 39px ; margin-left: 40px"></div>
  </g:form>
 <script type="text/javascript">
	currentMenuId = "#eventMenu";
	$("#eventMenuId a").css('background-color','#003366')
	function generateRunbook(){
		var moveEvent = $('#eventList').val();
		var boo = false;
		if(moveEvent){
			boo = true;
			jQuery.ajax({
				url: '../moveEvent/runbookStats',
			 	data: {'id':moveEvent},
				type:'POST',
				complete: function(e) {
					var resp = e.responseText;
					$('#exportResultId').html(resp)
					$('#exportResultId').css('display','block')
				}
			});
		}else{
			alert("Please Select Event")
	 	}
		return boo;
	}
 </script>
 </body>
 
 </html>
  