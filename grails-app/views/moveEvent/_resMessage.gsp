<h2>Status:</h2>
${message.status}
<h2>Exceptions:</h2>
${message.exceptions}
<h2 class='assetImage' onclick='javascript:toogleGenDetails()'>
	Generation details...
	<asset:image src="images/triangle_right.png" />
	<asset:image src="images/triangle_down.png" />
</h2>
<br />
<span style="display: none;" id='generateDetailsSpan'>
	<h2>Log:</h2> ${message.Log}
</span>
