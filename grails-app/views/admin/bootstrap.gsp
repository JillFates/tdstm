<!DOCTYPE html>
<html>
<head>
<title>TDS Dropdown menu hover demo</title>
<link rel="stylesheet" href="${resource(dir:'css',file:'bootstrap.css')}" type="text/css"/>
<link rel="stylesheet" href="${resource(dir:'css',file:'bootstrap1.css')}" type="text/css"/>
<script src="jquery.js"></script>
<script src="bootstrap.js"></script>
</head>
<body style="background: none repeat scroll 0 0 #FFFFFF;">
<div class="tdsDiv">
<a href="http://www.transitionaldata.com/" target="new"><img class="tdsImage" src="${resource(dir:'images',file:'tds3b.png')}"/></a>
<div class="tdsName">&nbsp;TransitionManager&trade; -&nbsp;Accelrys</div>
<ul class="nav nav-pills tdsUl">
<li class="dropdown">
  <a class="dropdown-toggle" data-toggle="dropdown" href="#">John <b class="caret"></b></a>
  <ul id="menu1" class="dropdown-menu">
    <li><b style="margin-left: 20px;color:#5F9FCF;">John</b></li>
            <li><a href="#"> Account settings...</a></li>
            <li><a href="#">Edit preferences</a></li>
            <li><a href="#">Model Score: 5340</a></li>
            <li><a href="#">Use Mobile Site</a></li>
	<li class="divider"></li>
	<li><a href="#">SignOut</a></li>
</ul>
</li>
<li class="dropdown">
  <a class="dropdown-toggle" data-toggle="dropdown" href="#">Using GMT Time <b class="caret"></b></a>
  <ul id="menu1" class="dropdown-menu">
        <li><a href="#">GMT</a></li>
		<li><a href="#">PST</a></li>
		<li><a href="#">PDT</a></li>
		<li><a href="#">MST</a></li>
		<li><a href="#">MDT</a></li>
		<li><a href="#">CST</a></li>
		<li><a href="#">CDT</a></li>
		<li><a href="#">EST</a></li>
		<li><a href="#">EDT</a></li>
   </ul>
</li>
</ul>
</div>
<div class="navbar navbar-fixed-top">
	<div class="navbar-inner">
		<div class="container-fluid">
<ul class="nav nav-pills">
<li class="dropdown">
  <a class="dropdown-toggle" data-toggle="dropdown" href="#">Admin <b class="caret"></b></a>
  <ul id="menu1" class="dropdown-menu">
    <li>
        <a href="#">Administration</a>
        <ul class="dropdown-menu sub-menu">
            <li><a href="#">Admin Portal</a></li>
            <li><a href="#">Role Permissions</a></li>
            <li><a href="#">Asset Options</a></li>
            <li><a href="#">Release Notes</a></li>
             <li class="divider"></li>
            <li><a href="#">Help</a></li>
        </ul>
    </li>
    <li><a href="#">Manage Clients</a>
      <ul class="dropdown-menu sub-menu">
            <li><a href="#">List Companies</a></li>
            <li><a href="#">List Staff</a></li>
            <li><a href="#">List Users</a></li>
             <li class="divider"></li>
            <li><a href="#">Help</a></li>
        </ul>
      </li>
    <li><a href="#">Manage Workflows</a>
      <ul class="dropdown-menu sub-menu">
            <li><a href="#">List Workflows</a></li>
             <li class="divider"></li>
            <li><a href="#">Help</a></li>
        </ul>
      </li>
      <li><a href="#">Manage Model Library</a>
      <ul class="dropdown-menu sub-menu">
            <li><a href="#">List Manufacturers</a></li>
            <li><a href="#">List Models</a></li>
            <li><a href="#">Sync Libraries</a></li>
             <li class="divider"></li>
            <li><a href="#">Help</a></li>
        </ul>
      </li>
  </ul>
</li>
<li class="dropdown">
  <a class="dropdown-toggle" data-toggle="dropdown" href="#">Client/Project <b class="caret"></b></a>
  <ul id="menu1" class="dropdown-menu">
    <li>
        <a href="#">Client/Project</a>
        <ul class="dropdown-menu sub-menu">
            <li><a href="#">List Project</a></li>
            <li><a href="#">Create Project</a></li>
            <li><a href="#">Asset Options</a></li>
            <li><a href="#">FieldImportance</a></li>
             <li class="divider"></li>
            <li><a href="#">Help</a></li>
        </ul>
      </li>
    <li>
        <a href="#">For project</a>
        <ul class="dropdown-menu sub-menu">
            <li><a href="#">Project Settings</a></li>
            <li><a href="#">Project Staff</a></li>
             <li class="divider"></li>
            <li><a href="#">Help</a></li>
        </ul>
      </li>
  </ul>
</li>
    <li class="dropdown">
  <a class="dropdown-toggle" data-toggle="dropdown" href="#">Rooms Racks<b class="caret"></b></a>
  <ul id="menu1" class="dropdown-menu">
    <li>
        <a href="#">Rooms</a>
        <ul class="dropdown-menu sub-menu">
            <li><a href="#">List Rooms</a></li>
             <li class="divider"></li>
            <li><a href="#">Help</a></li>
        </ul>
    </li><li>
        <a href="#">Racks</a>
        <ul class="dropdown-menu sub-menu">
            <li><a href="#">Racks</a></li>
             <li class="divider"></li>
            <li><a href="#">Help</a></li>
        </ul>
    </li>
</ul>
</li>
 <li class="dropdown">
  <a class="dropdown-toggle" data-toggle="dropdown" href="#">Assets <b class="caret"></b></a>
  <ul id="menu1" class="dropdown-menu">
    <li>
        <a href="#">Manage Data</a>
        <ul class="dropdown-menu sub-menu">
            <li><a href="#">Import Assets</a></li>
            <li><a href="#">Manage Batches</a></li>
            <li><a href="#">Export Assets</a></li>
            <li><a href="#">Asset Comments</a></li>
             <li class="divider"></li>
            <li><a href="#">Help</a></li>
        </ul>
      </li>
    <li>
        <a href="#">Assets</a>
        <ul class="dropdown-menu sub-menu">
            <li><a href="#">Summary</a></li>
            <li><a href="#">List Assets</a></li>
            <li><a href="#">List Apps</a></li>
            <li><a href="#">List DBs</a></li>
            <li><a href="#">List Storage</a></li>
             <li class="divider"></li>
            <li><a href="#">Help</a></li>
        </ul>
    </li>
</ul>
</li>
<li class="dropdown">
  <a class="dropdown-toggle" data-toggle="dropdown" href="#">Events Bundles <b class="caret"></b></a>
  <ul id="menu1" class="dropdown-menu">
    <li>
        <a href="#">Events</a>
        <ul class="dropdown-menu sub-menu">
            <li><a href="#">List Events</a></li>
            <li><a href="#">Create Event</a></li>
            <li><a href="#">Pre-event Checklist</a></li>
            <li><a href="#">Export Runbook</a></li>
             <li class="divider"></li>
            <li><a href="#">Help</a></li>
        </ul>
    </li>
    <li>
        <a href="#">Bundles</a>
        <ul class="dropdown-menu sub-menu">
            <li><a href="#">List Bundles</a></li>
            <li><a href="#">Create Bundle</a></li>
            <li><a href="#">Planning Dashboard</a></li>
            <li><a href="#">Planning Console</a></li>
             <li class="divider"></li>
            <li><a href="#">Help</a></li>
        </ul>
    </li>
      <li>
        <a href="#">For UNSELECTED:</a>
        <ul class="dropdown-menu sub-menu">
            <li><a href="#">Bundle Settings</a></li>
            <li><a href="#">Bundled Assets</a></li>
            <li><a href="#">List Teams (old)</a></li>
            <li><a href="#">Planning Console</a></li>
            <li><a href="#">Assign Assets... (old)</a></li>
             <li class="divider"></li>
            <li><a href="#">Help</a></li>
        </ul>
    </li>
</ul>
</li>
<li class="dropdown">
  <a class="dropdown-toggle" data-toggle="dropdown" href="#">Tasks <b class="caret"></b></a>
  <ul id="menu1" class="dropdown-menu">
    <li>
        <a href="#">Tasks</a>
        <ul class="dropdown-menu sub-menu">
            <li><a href="#">My Tasks</a></li>
            <li><a href="#">Task Manager</a></li>
            <li><a href="#">Team Tasks (old)</a></li>
             <li class="divider"></li>
            <li><a href="#">Help</a></li>
        </ul>
    </li>
</ul>
</li>
<li class="dropdown">
  <a class="dropdown-toggle" data-toggle="dropdown" href="#">Console <b class="caret"></b></a>
  <ul id="menu1" class="dropdown-menu">
    <li>
        <a href="#">Supervisor Console</a>
        <ul class="dropdown-menu sub-menu">
            <li><a href="#">Supervise Techs (old)</a></li>
            <li><a href="#">Supervise Admins (old)</a></li>
             <li class="divider"></li>
            <li><a href="#">Help</a></li>
        </ul>
    </li>
    <li>
        <a href="#">News</a>
        <ul class="dropdown-menu sub-menu">
            <li><a href="#">List News</a></li>
            <li><a href="#">Create News</a></li>
             <li class="divider"></li>
            <li><a href="#">Help</a></li>
        </ul>
    </li>
      <li>
        <a href="#">Cart Tracker</a>
        <ul class="dropdown-menu sub-menu">
            <li><a href="#">Cart Status</a></li>
            <li><a href="#">Truck GPS Tracking</a></li>
             <li class="divider"></li>
            <li><a href="#">Help</a></li>
        </ul>
    </li>
</ul>
</li>
<li class="dropdown">
  <a class="dropdown-toggle" data-toggle="dropdown" href="#">Dashboards <b class="caret"></b></a>
  <ul id="menu1" class="dropdown-menu">
    <li>
        <a href="#">Live Dashboards</a>
        <ul class="dropdown-menu sub-menu">
            <li><a href="#"> Planning Dashboard</a></li>
            <li><a href="#">Event Dashboard</a></li>
            <li><a href="#">Asset Tracker</a></li>
             <li class="divider"></li>
            <li><a href="#">Help</a></li>
        </ul>
    </li>
</ul>
</li>
<li class="dropdown">
  <a class="dropdown-toggle" data-toggle="dropdown" href="#">Reports <b class="caret"></b></a>
  <ul id="menu1" class="dropdown-menu">
    <li>
        <a href="#">Discovery</a>
        <ul class="dropdown-menu sub-menu">
            <li><a href="#">Racks (old)</a></li>
            <li><a href="#">Cabling Conflict</a></li>
            <li><a href="#">Cabling Data</a></li>
            <li><a href="#">Power</a></li>
             <li class="divider"></li>
            <li><a href="#">Help</a></li>
        </ul>
    </li>
     <li>
        <a href="#">Event Prep</a>
        <ul class="dropdown-menu sub-menu">
            <li><a href="#">Pre-event Checklist</a></li>
            <li><a href="#">Login Badges</a></li>
            <li><a href="#">Asset Tags</a></li>
            <li><a href="#">Event Team Worksheets</a></li>
            <li><a href="#">Logistics Team Worksheets</a></li>
            <li><a href="#">Transport Worksheets</a></li>
             <li class="divider"></li>
            <li><a href="#">Help</a></li>
        </ul>
    </li>
    <li>
        <a href="#">Event Day</a>
        <ul class="dropdown-menu sub-menu">
            <li><a href="#">Issue Report</a></li>
            <li><a href="#">Event Results</a></li>
            <li><a href="#">Cabling QA </a></li>
             <li class="divider"></li>
            <li><a href="#">Help</a></li>
        </ul>
    </li>
</ul>
</li>
</ul>
          </div>
	</div>
</div>
</body>
</html>