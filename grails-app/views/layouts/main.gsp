<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
<title><g:layoutTitle default="Grails" /></title>
<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'main.css')}" />
<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'tds.css')}" />
<link rel="shortcut icon"
	href="${createLinkTo(dir:'images',file:'tds.ico')}" type="image/x-icon" />
<g:layoutHead />
<g:javascript library="application" />
</head>

<body>
<div class="main_body">

<div class="header"><img
	src="${createLinkTo(dir:'images',file:'tds.jpg')}" style="float: left;">
<div class="header_right"><br />
<div style="font-weight: bold; color: #0000FF"><jsec:isLoggedIn>
	<strong>Welcome &nbsp;&nbsp;<jsec:principal />&nbsp;! </strong>
			&nbsp;<g:link controller="auth" action="signOut"
		style="color: #328714">sign out</g:link>
</jsec:isLoggedIn></div>
</div>
</div>

<div class="top_menu_layout">
<div class="menu1">
<ul>
	<li><g:link class="home" controller="projectUtil">Project Manager</g:link></li>
	<jsec:hasRole name="ADMIN">
	<li><g:link class="home" controller="auth" action="home">Administration </g:link> </li>
	</jsec:hasRole>
</ul>
</div>
</div>
<div class="title">&nbsp;Party Manager Application</div>
<!--
<div class="menu1">
<ul>
	<li><g:link class="home" controller="projectUtil">Main</g:link></li>
	<li><g:link class="home" controller="projectUtil"
		action="searchList">Search</g:link></li>
	<jsec:hasRole name="PROJECT_ADMIN">
		<li><g:link class="home" controller="project" action="create">Add</g:link>
		</li>
	</jsec:hasRole>
	<li><a href="#">Import/Export</a></li>
</ul>
</div>
-->
<div class="menu2">
<ul>
	<li><g:link class="home" controller="projectUtil">Project </g:link> </li>
	<li><g:link class="home" controller="asset">Assets </g:link></li>
	<li><g:link class="home" controller="asset" action="assetImport" >Import/Export</g:link> </li>
	<li><a href="#">Team </a></li>
	<li><a href="#">Contacts </a></li>
	<li><a href="#">Applications </a></li>
	<li><a href="#">Move Bundles </a></li>
</ul>
</div>
<div class="main_bottom"><g:layoutBody /></div>
</div>
</body>
</html>
