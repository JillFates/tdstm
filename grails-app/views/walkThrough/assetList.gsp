<html>
<head>
<title>Walkthru&gt; Select Asset</title>
<g:javascript library="prototype" />
<g:javascript library="jquery" />
<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'qvga.css')}" />
<link rel="stylesheet"
	href="${createLinkTo(dir:'css',file:'walkThrough.css')}" />
</head>
<body>
<DIV class=qvga_border><A name=select_asset></A>
<DIV class=title>Walkthru&gt; Select Asset</DIV>
<DIV class=input_area>
<DIV style="FLOAT: left"><A class=button
	href="startMenu">Start Over</A></DIV>
<DIV style="FLOAT: right"><A class=button
	href="selectRack">Rack List</A></DIV>
<BR class=clear>
<TABLE>
	<TBODY>
		<TR>
			<TD class=label>Room/Rack:</TD>
			<TD class=field>QA Lab/R09</TD>
		</TR>
		<TR>
			<TD align=middle><LABEL>View:</LABEL> <A
				class="button unselected"
				href="http://ph1.tdsops.com/test/wt2.php#start">ToDo</A> <A
				class=button href="http://ph1.tdsops.com/test/wt2.php#start">All</A>
			</TD>
			<TD align=right><LABEL for=assetSearch>Scan Asset:</LABEL><INPUT
				style="WIDTH: 40px" id=assetSearch class="text search" size=8
				name=assetSearch></TD>
		</TR>
		<TR>
			<TD colSpan=2>
			<TABLE class=grid>
				<TBODY>
					<TR>
						<TH>U Pos</TH>
						<TH>Size</TH>
						<TH>Asset Tag</TH>
					</TR>
					<TR class=asset_ready onclick="location.href='assetMenu'">
						<TD class="asset_ready center">18</TD>
						<TD class=center>2</TD>
						<TD class=center>TW00334455</TD>
						
					</TR>
				</TBODY>
			</TABLE>
			</TD>
		</TR>
	</TBODY>
</TABLE>
</DIV>
</DIV>
</body>
</html>
