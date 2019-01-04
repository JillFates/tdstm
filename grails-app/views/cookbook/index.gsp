<!doctype html>
<html xmlns:ng="http://angularjs.org">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="topNav" />
	<title>Cookbook</title>
	<asset:stylesheet href="css/tds-bootstrap.css" />
	<asset:stylesheet href="css/ng-grid.css" />
	<asset:stylesheet href="css/codemirror/codemirror.css" />
	<asset:stylesheet href="css/codemirror/addon/dialog.css" />
	<asset:stylesheet href="css/codemirror/addon/show-hint.css" />
	<asset:stylesheet href="css/codemirror/addon/fullscreen.css" />
	<asset:stylesheet href="css/mergely/mergely.css" />
	<asset:stylesheet href="css/cookbook.css" />
	<asset:stylesheet href="components/comment/comment.css" />

	<g:render template="/layouts/responsiveAngularResources" />
	<g:javascript src="codemirror/codemirror.js" />
	<g:javascript src="codemirror/ui-codemirror.js" />
	<g:javascript src="codemirror/addon/dialog.js" />
	<g:javascript src="codemirror/addon/fullscreen.js" />
	<g:javascript src="codemirror/addon/show-hint.js" />
	<g:javascript src="codemirror/addon/javascript-hint.js" />
	<g:javascript src="codemirror/javascript.js" />
	<g:javascript src="mergely/mergely.js" />

	<g:javascript src="angular/plugins/angular-ui-router.min.js" />
	<g:javascript src="controllers/cookbook.js" />
	<g:javascript src="asset.tranman.js" />
	<g:javascript src="asset.comment.js" />
	<g:javascript src="entity.crud.js" />
	<g:javascript src="model.manufacturer.js"/>
	<g:javascript src="moment.min.js" />
	<g:javascript src="moment-timezone-with-data.min.js" />
	<script type="text/javascript">
		var userPreferences = {
			"CURR_TZ" : "${userPreferenceService.getTimeZone()}"
		};

		$(document).ready(function() {
			var opt = {
			        autoOpen: false,
			        modal: false,
			        width: 'auto',
			        height: 'auto',
			        position: ['center','top']
			};

			$('#createCommentDialog').dialog(opt);
			$('#showCommentDialog').dialog(opt);

			$('.hasTooltip').tooltip();
		});
	</script>
</head>
<body>
<tds:subHeader title="Cookbook" crumbs="['Tasks','Cookbook']"/>
<g:include view="/layouts/_error.gsp" />

	<div class="body cookbook_recipes_editor" style="position: relative" id="cookbookRecipesEditor" ng-app="tdsCookbook" ng-controller="tds.cookbook.controller.MainController">

		<div class="alert alert-{{alert.type}}" ng-repeat="alert in alerts.list" ng-class="{animateShow: !alert.hidden}">
			<button type="button" class="close" aria-hidden="true" ng-click="alerts.closeAlert($index)">&times;</button>
			<div class="alert-message-icon" ng-if="alert.type == 'success'">
				<h4><i class="icon fa fa-check"></i> Success:</h4>
			</div>
			<div class="alert-message-icon" ng-if="alert.type == 'danger'">
				<h4><i class="icon fa fa-ban"></i> Error:</h4>
			</div>
			<div class="alert-message-text"><span ng-bind="alert.msg"></span></div>
		</div>

		<div class="container" ng-controller="tds.comments.controller.MainController as comments" ui-view></div>

		<loading-indicator enabled="cookbook.loadingIndicatorEnabled"></loading-indicator>

	</div>

	<g:render template="/assetEntity/initAssetEntityData"/>
	<script type="text/javascript">
		$(document).ready(function() {
			$("#createEntityView").dialog({autoOpen: false});
			$("#cloneEntityView").dialog({autoOpen: false});
			$("#editEntityView").dialog({autoOpen: false});
			$("#showEntityView").dialog({autoOpen: false});
		});

		$('#editSyntax').on('shown.bs.modal', function (e) {
			$('.CodeMirror').each(function(i, el){
				setTimeout(function(){
					el.CodeMirror.refresh();
					el.CodeMirror.focus();
				}, 10)
			});
		});

		$(".menu-parent-tasks-cookbook").addClass('active');
		$(".menu-parent-tasks").addClass('active');
	</script>

	<div id="createEntityView" style="display: none;"></div>
	<div id="cloneEntityView" style="display: none;"></div>
	<div id="editEntityView" style="display: none;"></div>
	<div id="showEntityView" style="display: none;"></div>

</body>
</html>
