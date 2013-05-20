<!doctype html>
<html xmlns:ng="http://angularjs.org">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="projectHeader" />
<title>Asset Fields</title>
<g:javascript src="angular.js" />
<g:javascript src="controllers/fieldImportance.js" />

</head>
<body>
<script type="text/javascript">
	var returnMap = ${returnMap}
</script>
<br><br>
<h1 class="assetFieldHeader1">Project Field Important</h1>
<div ng-app id="ng-app" ng-controller="assetFieldImportanceCtrl">
<h2 class="assetFieldHeader2">
	Assets <span id="showId" class="assetFieldSpan" ng-click="showAssets('show', 'AssetEntity')">|></span>
	<span id="hideId" class="assetFieldSpan" ng-click="showAssets('hide','AssetEntity')" style="display: none;">V</span>
</h2>
	<g:render template="assetImportance"></g:render>
</div>
</body>
</html>