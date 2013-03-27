<%@ page contentType="text/html;charset=ISO-8859-1" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
<meta name="layout" content="projectHeader" />
<title>WEB Event Results</title>
</head>
<body>
  <div class="body">
  <g:if test="${summary}">
    <h1> SUMMARY Event Results</h1>
	  <table>
	       <thead>
	         <tr>
	           <th>Bundle Id</th>
	           <th>Bundle Name</th>
	           <th>Transition Id</th>
	           <th>Transition Name</th>
	           <th>Started Time</th>
	           <th>Completed Time</th>
	         </tr>
	       </thead>
	       <tbody>
		       <g:each in="${moveEventResults}" status="i" var="moveEvent">
			       <tr>
				       <td>${moveEvent.move_bundle_id}</td>
				       <td>${moveEvent.bundle_name}</td>
				       <td>${moveEvent.state_to}</td>
				       <td>${moveEvent.name}</td>
				       <td>${moveEvent.started}</td>
				       <td>${moveEvent.completed}</td>
			       </tr>
			    </g:each>
		   <g:if test="${moveEventResults?.size() == 0}">
			  <tr><td colspan="3" class="no_records">NO RECORD FOUND</td></tr>
		   </g:if>
		       </tbody>
	  </table>
  </g:if>
  <g:else>
  <h1>DETAILED Event Results</h1>
	  <table>
	       <thead>
	         <tr>
	           <th>Bundle Id</th>
	           <th>Bundle Name</th>
	           <th>Asset Id</th>
	           <th>Asset Name</th>
	           <th>Voided</th>
	           <th>Transition From</th>
	           <th>Transition To</th>
	           <th>Transition Time</th>
	           <th>User</th>
	           <th>Team Name</th>
	         </tr>
	       </thead>
	       <tbody>
		       <g:each in="${moveEventResults}" status="i" var="moveEvent">
			       <tr>
				       <td>${moveEvent.move_bundle_id}</td>
				       <td>${moveEvent.bundle_name}</td>
				       <td>${moveEvent.asset_id}</td>
				       <td>${moveEvent.asset_name}</td>
				       <td>${moveEvent.voided}</td>
				       <td>${moveEvent.from_name}</td>
				       <td>${moveEvent.to_name}</td>
				       <td>${moveEvent.transition_time}</td>
				       <td>${moveEvent.username}</td>
				       <td>${moveEvent.team_name}</td>
			       </tr>
			    </g:each>
			    
		   <g:if test="${moveEventResults?.size() == 0}">
			  <tr><td colspan="3" class="no_records">NO RECORD FOUND</td></tr>
		   </g:if>
		       </tbody>
	  </table>
  </g:else>
  </div>
</body>
</html>