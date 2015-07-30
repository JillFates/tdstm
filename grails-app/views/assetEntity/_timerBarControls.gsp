<input type="button" class="pointer" value="Refresh" onclick="timerBar.refreshFunction()"/>&nbsp;
<select id="selectTimedBarId">
	<option value="0" selected="selected">Manual</option>
	<g:each in="${timerValues}" var="it">
		<option value="${it}">${(it % 60 == 0) ? (it / 60 + ' Min') : (it + ' Sec')}</option>
	</g:each>
</select>