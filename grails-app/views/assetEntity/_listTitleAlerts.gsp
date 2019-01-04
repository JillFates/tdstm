		<div class="alert alert-{{alert.type}}" ng-repeat="alert in alerts.list" ng-class="{animateShow: !alert.hidden}">
			<button type="button" class="alert-close" aria-hidden="true" ng-click="alerts.closeAlert($index)">&times;</button>
			<span ng-bind="alert.msg"></span>
		</div>
		<g:if test="${flash.message}">
			<div id="messageDivId" class="message">${flash.message}</div>
		</g:if>
		<div>
			<div id="messageId" class="message" style="display:none"></div>
		</div>
		<g:include view="/layouts/_error.gsp" />
