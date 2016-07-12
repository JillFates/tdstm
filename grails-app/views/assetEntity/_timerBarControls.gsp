<div class="row progress-bar-container">
	<div class="col-xs-4 item-wrapper refresh-button">
		<input type="button" class="pointer" value="Refresh" onclick="timerBar.refreshFunction()"/>
	</div>
	<div class="col-xs-4 item-wrapper">
		<select id="selectTimedBarId">
			<option value="0" selected="selected">Manual</option>
			<g:each in="${timerValues}" var="it">
				<option value="${it}">${(it % 60 == 0) ? (it / 60 + ' Min') : (it + ' Sec')}</option>
			</g:each>
		</select>
	</div>
	<div class="col-xs-4 item-wrapper">
		<div class="progress-bar-svg"></div>
	</div>
</div>