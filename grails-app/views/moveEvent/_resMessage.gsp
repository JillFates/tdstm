<h2>Status:</h2>
${message.status}
<h2>Exceptions:</h2>
${message.exceptions}
<h2 class='assetImage' onclick='javascript:toogleGenDetails()'>
	Generation details... 
	<img id="rightTriangle" src="${resource(dir:'images',file:'triangle_right.png')}" /> 
	<img id="downTriangle" style="display: none;" src="${resource(dir:'images',file:'triangle_down.png')}" />
</h2>
<br />
<span style="display: none;" id='generateDetailsSpan'>
	<h2>Log:</h2> ${message.Log}
</span>