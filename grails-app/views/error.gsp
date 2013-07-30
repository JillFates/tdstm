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
  </head>
  <body>
<h2 style="color:#006DBA;">TransitionManager&trade; Error</h2>
<strong>Ok I got an error, now what? </strong><br/>
This error may be from a bug, a misconfiguration, bad data, or a system problem.  You can <a href="javascript:history.go(-1)">go back</a> then reload that page to try again.<br/><br />
If this happens again and you'd like help diagnosing the problem, copy the text below and email it to your TDS contact or TranMan@transitionaldata.com.<br/>

    <h2 style="color:#006DBA;">Precise Error details: </h2>
    <div class="message">
        <strong>Grails Runtime Exception</strong><br />
		<strong>Error ${request.'javax.servlet.error.status_code'}:</strong> ${request.'javax.servlet.error.message'.encodeAsHTML()}<br/>
		<strong>Servlet:</strong> ${request.'javax.servlet.error.servlet_name'}<br/>
		<strong>URI:</strong> ${request.'javax.servlet.error.request_uri'}<br/>
		<g:if test="${exception}">
	  		<strong > Exception Message:</strong> <span> ${exception.message?.encodeAsHTML()} </span> <br />
	  		<strong>Caused by:</strong> ${exception.cause?.message?.encodeAsHTML()} <br />
	  		<strong>Class:</strong> ${exception.className} <br />
	  		<strong>At Line:</strong> [${exception.lineNumber}] <br />
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
  </body>
</html>
