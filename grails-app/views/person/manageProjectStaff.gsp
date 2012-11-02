<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="projectHeader" />
        <g:javascript src="projectStaff.js" />
        <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'tds.css')}" />
	    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'qvga.css')}" />
        <title>Project Staff</title>   
        <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'calendarview.css')}" />
		<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.datepicker.css')}" />
		 
    </head>
    <body>
        <div class="body">
            <h1>Project Staff</h1>
            <div id="staffSelectId">
            <table style="border: 0px;width: 100%;" >
				<tr>
				   <td>
				       <span><b>Role</b></span><br/>
					   <label for="role">
	          				<g:select id="role" name="role" from="${roleTypes}" optionKey="id" optionValue="${{it.description.substring(it.description.lastIndexOf(':') +1).trim()}}"  value="${currRole}" onChange="loadFilteredStaff()"
	          						noSelection="${['0':'All']}"></g:select>
	            	   </label>
				   </td>
				    <%--<td>
				       <span><b>Location</b></span><br/>
					   <label for="location">
	          				<g:select id="location" name="location"  from="${['All', 'Local']}"  value="${currLoc }" onChange="loadFilteredStaff()"></g:select>
	            	   </label>
				   </td>
				   --%><td>
				      <span><b>Project</b></span><br/>
					   <label for="project">
	          				<g:select id="project" name="project"  from="${projects}"  noSelection="${['0':'All']}" value="${projectId}" optionKey="id" optionValue="name" onChange="loadFilteredStaff()"></g:select>
	            	   </label>
				   </td>
					<%--<td>
						<table style="border: 0px">
							<tr>
								<td><b>Phases</b></td>
								<td><label for="preMove"><input type="checkbox"
										name="PhaseCheck" id="preMove" checked="checked" onClick="unCheckAll();"/>&nbsp;PreMove</label>
								</td>
								<td><label for="physical-trg"><input
										type="checkbox" name="PhaseCheck" id="physical-trg"
										checked="checked" onClick="unCheckAll();"/>&nbsp;Physical-trg</label></td>
							</tr>
							<tr>
								<td><label for="allPhase"><input type="checkbox"
										name="allPhase" id="allPhase" checked="checked" onclick="if(this.checked){this.value = 1} else {this.value = 0 }; checkAllPhase();" value="1"/>&nbsp;All</label></td>
								<td><label for="ShutDown"><input type="checkbox"
										name="PhaseCheck" id="ShutDown" checked="checked" onClick="unCheckAll()"/>&nbsp;ShutDown</label>
								</td>
								<td><label for="startUp"><input type="checkbox"
										name="PhaseCheck" id="startUp" checked="checked" onClick="unCheckAll()"/>&nbsp;startUp</label>
								</td>
							</tr>
							<tr>
								<td></td>
								<td><label for="physical-src"><input
										type="checkbox" name="PhaseCheck" id="physical-src"
										checked="checked" onClick="unCheckAll()"/>&nbsp;physical-src</label></td>
								<td><label for="postMove"><input type="checkbox"
										name="PhaseCheck" id="postMove" checked="checked" onClick="unCheckAll()"/>&nbsp;postMove</label>
								</td>
							</tr>
						</table>
					</td>
					--%><td>
				      <span><b>Scale</b></span><br/>
					   <label for="scale">
	          				<select id="scale" name="scale" onChange="loadFilteredStaff()">
	          				 <option value="1"> 1 Month </option>
	          				 <option value="2"> 2 Month </option>
	          				 <option value="3"> 3 Month </option>
	          				 <option value="6"> 6 Month </option>
	          				</select>
	            	   </label>
				   </td>
				</tr>            
            </table>
            <br/>
            <div id="projectStaffTableId">
            	<g:render template="projectStaffTable"></g:render>
            </div>
            </div>
            <div id="personGeneralViewId" style="display: none;" title="Manage Staff "></div>
        </div>
        <script type="text/javascript">
	        $(document).ready(function() {
		        $("#scale").val(${currScale})
		        $("#personGeneralViewId").dialog({ autoOpen: false })
			})
			
			
     	</script>
     </body>
 </html>