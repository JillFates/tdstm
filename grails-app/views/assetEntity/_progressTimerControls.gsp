<div class="row progress-bar-container">
	<div class="col-xs-4 item-wrapper refresh-button" onclick="if(typeof progressTimer !== 'undefined'){  progressTimer.updateTarget = true; progressTimer.refreshFunction();} else { timerBar.updateTarget = true; timerBar.refreshFunction(); }">
		<span class="glyphicon glyphicon-refresh" aria-hidden="true"></span>
	</div>
	<div class="col-xs-4 item-wrapper select-timer">
		<select id="selectTimedBarId">
			<option value="0" selected="selected">Manual</option>
			<g:each in="${timerValues}" var="it">
				<option value="${it}">${(it % 60 == 0) ? (it / 60 + ' Min') : (it + ' Sec')}</option>
			</g:each>
		</select>
	</div>
	<div class="col-xs-4 item-wrapper progress-bar-wrapper" style="display: none;">
		<div class="progress-bar-svg"></div>
	</div>
</div>
