<!DOCTYPE html>
<html lang="en">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta charset="UTF-8">
	<meta name="layout" content="topNav" />
	<title>Restart Application Service</title>
	<style type="text/css">
		.padded {
			padding: 2em;
		}
		.center {      
			text-align: center;
		}
		.big {
			padding: 1em;
		}
		.red {
			color:red;
		}
		.modal{
			position:fixed;
			top: 0;
			left: 0;
			width: 100%;
			height: 100%;
			z-index: 999;
			background: rgba(255, 255, 255, 0.9);
		}
		.modal > div {
			padding-top: 5em;
		}
		#btnRestart{
			font-size: medium;
		}
		.active-users-list {
			padding-left:10em;
			font-size:16px;
		}
		.active-users-list > ul {
			padding-left: 2em;
		}

	</style>
</head>
<body>
	<g:if test="${restartable}">
		<div class="padded center red">
			
			<p class="padded">
				<h2>Restart Application Service</h2>
				<h3>Clicking the Restart button will cause a brief disruption of service while the application is stopped and then restarted. 
				The process typically takes 2-3 minutes to complete.
				<br>
				<br>				
				Please notify the Support/Operations team that an application restart will be performed before continuing.
				</h3>
			</p>
			<br>				
			<button id="btnRestart" type="button" class="big">Initiate Application Restart</button>
			</h2>
		</div>

		<div class="padded active-users-list"> 
			<b>Active users in past ${activityTimeLimit} minutes:</b>
			<ul>
			<g:if test="${users.size() > 0}">
				<g:each var="user" in="${users}">
					<li>${user}</li>
				</g:each>
			</g:if>
			<g:else>
				<li>No recent user activity</li>
			</g:else>
			</ul>
		<div>

		<script type="text/javascript">
			$(function(){
				$('#btnRestart').on("click", function(e){
					var proceed = confirm("Are you sure you want to restart the application? Click Ok to restart otherwise press Cancel.");
					if(proceed){
						$.post("restart")
						.done(function(data) {
						  	if(data.status == "fail"){
						  	  	alert("ERROR: " + data.data.message);

						  	}else {
						  	  	var message = "The application is being restarted. This may take 2 to 3 minutes to complete...";
						  	  	var timeOutSecs = 60;
						  		$("body").append("<div class='modal'><div class='center'><h2><strong>" + message + "</strong></h2></div></div>");
								$(".modal").show();

								setTimeout(function(){
									window.location.replace("${createLink(mapping:'adminPortal')}");
								}, timeOutSecs * 1000);
						  	}

						});
					}
				});
			});
		</script>
	</g:if>
	<g:else>
		<div class="padded center red">
			<h2>
			Restart Application Service
			<br>
			<br>
			<g:message code="tdstm.admin.serviceRestartCommand.error" />
			</h2>
		</div>
	</g:else>
</body>
</html>
