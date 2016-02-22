<%-- 
  -- This is the standard include of CSS and Javascript files necessary throughout the TM application to support angular
--%>

<g:javascript src="angular/angular.min.js" />
<g:javascript src="angular/plugins/angular-ui.js"/>
<g:javascript src="angular/plugins/angular-resource.js" />
<!-- Kendo Directives -->
<script type="text/javascript" src="${resource(dir:'dist/js/vendors/kendo',file:'kendo.all.min.js')}"></script>
<script type="text/javascript" src="${resource(dir:'components/core',file:'core.js')}"></script>
<script type="text/javascript" src="${resource(dir:'components/comment',file:'comment.js')}"></script>
<script type="text/javascript" src="${resource(dir:'components/asset',file:'asset.js')}" /></script>
<g:javascript src="shared/validators/tmLinkableUrlDirective.js"/>
<g:javascript src="cabling.js"/>
<g:javascript src="bootstrap.js" />
<g:javascript src="angular/plugins/ui-bootstrap-tpls-0.10.0.min.js" />
<g:javascript src="angular/plugins/ngGrid/ng-grid-2.0.7.min.js" />
<g:javascript src="angular/plugins/ngGrid/ng-grid-layout.js" />

<link type="text/css" rel="stylesheet" href="${resource(dir:'components/comment',file:'comment.css')}" />
<!-- Kendo UI Material Theme -->
<link type="text/css" rel="stylesheet" href="${resource(dir:'dist/css/kendo',file:'kendo.common-material.min.css')}">
<link type="text/css" rel="stylesheet" href="${resource(dir:'dist/css/kendo',file:'kendo.material.min.css')}">
