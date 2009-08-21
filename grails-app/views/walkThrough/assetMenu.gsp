<html>
<head>
<title>Walkthru&gt; Select Asset</title>
<g:javascript library="prototype" />
<g:javascript library="jquery" />
<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'qvga.css')}" />
<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'walkThrough.css')}" />
</head>
<body>
<DIV class=qvga_border>
<DIV class=title>Walkthru&gt; Asset Menu</DIV>
<DIV class=input_area>
<DIV style="FLOAT: left">
	<A class=button href="startMenu">Start Over</A>
</DIV>
<DIV style="FLOAT: right">
	<A class=button	href="selectAsset?moveBundle=${moveBundle}&location=${location}&room=${room}&rack=${rack}">Asset List</A>
</DIV>
<TABLE>
	<TBODY>
		<TR>
			<TD class=label>Asset Tag:</TD>
			<TD class=field>${assetEntity?.assetTag}</TD>
		</TR>
		<TR>
			<TD class=label>Asset Name:</TD>
			<TD class=field>${assetEntity?.assetName}</TD>
		</TR>
	</TBODY>
</TABLE>
<DIV style="MARGIN-TOP: 15px" align=center><A class="button big"
	href="#asset_front1">Front Audit</A>
<BR style="MARGIN-TOP: 6px">
<A class="button big"
	href="#asset_rear1">Rear Audit</A> <BR
	style="MARGIN-TOP: 6px">
<A class="button big" href="#comments">Issues/Comments</A>
<BR style="MARGIN-TOP: 6px">
<A class="button big" onclick="alert('Asset has been updated');"
	href="#asdf">Mark Asset Missing</A></DIV>
<DIV style="MARGIN-TOP: 10px">
<DIV class=thefield align=center><A class=button
	onclick="alert('Changes have been saved'); return true;"
	href="#select_asset">Save</A>
&nbsp;&nbsp; <A class=button
	onclick="alert('Changes have been saved and marked completed'); return true;"
	href="#select_asset">Completed</A></DIV>
</DIV>
</DIV>
</DIV>
</body>
</html>
