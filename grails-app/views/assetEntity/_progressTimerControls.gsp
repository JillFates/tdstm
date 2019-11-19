<div class="row progress-bar-container">
	<div class="col-xs-4 item-wrapper refresh-button" onclick="if(typeof progressTimer !== 'undefined'){  progressTimer.updateTarget = true; progressTimer.refreshFunction();} else { timerBar.updateTarget = true; timerBar.refreshFunction(); }">
		<span style="font-size: 16px; color: #0077b8;" aria-hidden="true"><i class="fas fa-sync"></i></span>
	</div>
	<div class="col-xs-4 item-wrapper select-timer">
		<div class="clr-select-wrapper">
			<select id="selectTimedBarId" class="clr-select">
				<option value="0" selected="selected">Manual</option>
				<g:each in="${timerValues}" var="it">
					<option value="${it}">${(it % 60 == 0) ? (it / 60 + ' Min') : (it + ' Sec')}</option>
				</g:each>
			</select>
		</div>

	</div>
	<div class="col-xs-4 item-wrapper progress-bar-wrapper" style="display: none;">
		<div class="progress-bar-svg"></div>
	</div>
</div>
