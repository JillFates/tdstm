<!doctype html>
<html xmlns:ng="http://angularjs.org">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="projectHeader" />
	<title>Cookbook</title>
	<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'bootstrap.css')}" />
	<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'tds-bootstrap.css')}" />
	<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ng-grid.css')}" />
	<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'codemirror/codemirror.css')}" />
	<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'codemirror/addon/dialog.css')}" />
	<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'codemirror/addon/show-hint.css')}" />
	<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'mergely/mergely.css')}" />	
	<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'cookbook.css')}" />
	<link type="text/css" rel="stylesheet" href="${resource(dir:'components/comment',file:'comment.css')}" />
	<g:javascript src="angular/angular.min.js" />
	<g:javascript src="codemirror/codemirror.js" />
	<g:javascript src="codemirror/ui-codemirror.js" />
	<g:javascript src="codemirror/addon/dialog.js" />
	<g:javascript src="codemirror/addon/search.js" />
	<g:javascript src="codemirror/addon/show-hint.js" />
	<g:javascript src="codemirror/addon/javascript-hint.js" />
	<g:javascript src="codemirror/javascript.js" />
	<g:javascript src="mergely/mergely.js" />	
	<g:javascript src="bootstrap.js" />
	<g:javascript src="angular/plugins/angular-ui.js"/>	
	<g:javascript src="angular/plugins/angular-resource.js" />
	<g:javascript src="angular/plugins/ui-bootstrap-tpls-0.10.0.min.js" />
	<script type="text/javascript" src="${resource(dir:'components/core',file:'core.js')}"></script>
	<script type="text/javascript" src="${resource(dir:'components/comment',file:'comment.js')}"></script>
	<g:javascript src="angular/plugins/angular-ui-router.min.js" />
	<g:javascript src="controllers/cookbook.js" />
	<g:javascript src="angular/plugins/ngGrid/ng-grid-2.0.7.min.js" />
	<g:javascript src="angular/plugins/ngGrid/ng-grid-layout.js" />
	<g:javascript src="asset.tranman.js" />
	<g:javascript src="asset.comment.js" />
	<g:javascript src="entity.crud.js" />
	<g:javascript src="model.manufacturer.js"/>
	<g:javascript src="moment-timezone-with-data-2010-2020.js" />
	<script type="text/javascript">
		var userPreferences = {
			"CURR_TZ" : "${userPreferenceService.get('CURR_TZ')}"
		}

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

<g:include view="/layouts/_error.gsp" />

	<div class="body" style="position: relative" id="cookbookRecipesEditor" ng-app="tdsCookbook" ng-controller="tds.cookbook.controller.MainController">

		<div class="alert alert-{{alert.type}}" ng-repeat="alert in alerts.list" ng-class="{animateShow: !alert.hidden}">
			<button type="button" class="close" aria-hidden="true" ng-click="alerts.closeAlert($index)">&times;</button>
			<span ng-bind="alert.msg"></span>
		</div>

		<div class="container" ng-controller="tds.comments.controller.MainController as comments" ui-view></div>

		<loading-indicator enabled="cookbook.loadingIndicatorEnabled"></loading-indicator>

	</div>

	<g:render template="../assetEntity/initAssetEntityData"/>
	<script type="text/javascript">
		$('#editSyntax').on('shown.bs.modal', function (e) {
			$('.CodeMirror').each(function(i, el){
				setTimeout(function(){
					el.CodeMirror.refresh();
					el.CodeMirror.focus();
				}, 10)
			});
		})
	</script>

</body>
</html>