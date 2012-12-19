<html>
<head>
<title>Main Menu</title>
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'walkThrough.css')}" />
</head>
<body>
<div class="qvga_border">
	<a name="mainmenu"></a> 
	<div class="title">Main Menu</div>
	<div class="input_area">

	<div style="float:right;">
	<g:link controller="auth" action="signOut" class="button">Logout</g:link> </div>
	<br class="clear"/>

	<div style="text-align:center;">
	<g:link action="startMenu" class="button big">Walk-Thru</g:link>
	<br />
	<br />
	<a class="button big" href="#mainmenu" onClick="alert('Coming soon to a theater near you!');">Console</a>
	</div>
</div>
</div>
</body>
</html>
		
