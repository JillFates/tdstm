<html>
<head>
	<title>TransitionManager&trade; Error</title>
	<meta name="viewport" content="height=device-height,width=device-width" />
	<style type="text/css">
	  		.message {
	  			border: 1px solid black;
	  			padding: 5px;
	  			background-color:#E9E9E9;
	  		}
	  		.stack {
	  			border: 1px solid black;
	  			padding: 5px;
	  			overflow:auto;
	  			height: 300px;
	  		}
	  		.snippet {
	  			padding: 5px;
	  			background-color:white;
	  			border:1px solid black;
	  			margin:3px;
	  			font-family:courier;
	  		}
	  </style>
    <link rel="stylesheet" href="/tdstm/css/main.css" type="text/css"/>
    <link rel="stylesheet" href="/tdstm/css/tds.css" type="text/css"/>
</head>
<body>
	<div class="main_body">
		<input id="contextPath" type="hidden" value="/tdstm"/>
		<div class="tds_header">
			<div class="header_left">
				<a href="http://www.transitionaldata.com/" target="new"><img src="/tdstm/images/tds3.png" style="float: left;border: 0px;height: 30px;"/></a>      	    	 
			</div>
			<div class="title">&nbsp;TransitionManager&trade; Error</div>
		</div>
      <h2 style="color:#006DBA;">TransitionManager&trade; Error</h2>
<strong>Ok I got an error, now what? </strong><br/>
This error may be from a bug, a misconfiguration, bad data, or a system problem.  You can <a href="javascript:history.go(-1)">go back</a> then reload that page to try again.<br/><br />
If this happens again and you'd like help diagnosing the problem, just select all text (control-A, control-C) and email it to your TDS contact or @transitionaldata.com.<br/>

    <h2 style="color:#006DBA;">Precise Error details: </h2>
    <div class="message">
        <strong>Grails Runtime Exception</strong><br />
		<g:if test="${exception}">
	  		<strong>Exception Message:</strong> <span> ${exception.message?.encodeAsHTML()} </span> <br />
	  		<strong>Caused by:</strong> ${exception.cause?.message?.encodeAsHTML()} <br />
	  		<strong>Class:</strong> ${exception.className} <br />
	  		<strong>At Line:</strong> [${exception.lineNumber}] <br />
		</g:if>
		<strong>Error ${request.'javax.servlet.error.status_code'}:</strong> ${request.'javax.servlet.error.message'.encodeAsHTML()}<br/>
		<strong>Servlet:</strong> ${request.'javax.servlet.error.servlet_name'}<br/>
		<strong>URI:</strong> ${request.'javax.servlet.error.request_uri'}<br/>
		<g:if test="${exception}">
	  		<strong>Code Snippet:</strong><br />
	  		<div class="snippet">
	  			<g:each var="cs" in="${exception.codeSnippet}">
	  				${cs?.encodeAsHTML()}<br />
	  			</g:each>
	  		</div>
		</g:if>
  	</div>
	<g:if test="${exception}">
	    <strong>Stack Trace</strong>
	    <div class="stack">
	      <pre><g:each in="${exception.stackTraceLines}">${it.encodeAsHTML()}<br/></g:each></pre>
	    </div>
	</g:if>
	<script type="text/javascript" src="https://jira5.tdsops.com/s/en_US-7r7mpd-418945332/852/3/1.2.9/_/download/batch/com.atlassian.jira.collector.plugin.jira-issue-collector-plugin:issuecollector/com.atlassian.jira.collector.plugin.jira-issue-collector-plugin:issuecollector.js?collectorId=dc11b579"></script>
</body>
</html>
