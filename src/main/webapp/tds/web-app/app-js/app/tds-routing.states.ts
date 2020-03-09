import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {LocationStrategy, PathLocationStrategy} from '@angular/common';

export const Paths = {
	notice: 'notice'
};

export const TDSAppRoute: Routes = [
	{path: '', pathMatch: 'full', redirectTo: 'auth'},
	{path: 'security', loadChildren: '../modules/security/security.module#SecurityModule'},
	{path: 'tag', loadChildren: '../modules/assetTags/asset-tags.module#AssetTagsModule'},
	{path: 'assetcomment', loadChildren: '../modules/assetComment/asset-comment.module#AssetCommentModule'},
	{path: 'assetsummary', loadChildren: '../modules/assetSummary/asset-summary.module#AssetSummaryModule'},
	{path: 'asset', loadChildren: '../modules/assetManager/asset-manager.module#AssetManagerModule'},
	{path: 'event', loadChildren: '../modules/event/event.module#EventModule'},
	{path: 'event-news', loadChildren: '../modules/eventNews/event-news.module#EventNewsModule'},
	{path: 'insight', loadChildren: '../modules/insight/insight.module#InsightModule'},
	{path: 'bundle', loadChildren: '../modules/bundle/bundle.module#BundleModule'},
	{path: 'fieldsettings', loadChildren: '../modules/fieldSettings/field-settings.module#FieldSettingsModule'},
	{path: 'importbatch', loadChildren: '../modules/importBatch/import-batch.module#ImportBatchModule'},
	{path: 'planning', loadChildren: '../modules/planning/planning.module#PlanningModule'},
	{path: 'project', loadChildren: '../modules/project/project.module#ProjectModule'},
	{path: 'provider', loadChildren: '../modules/provider/provider.module#ProviderModule'},
	{path: 'credential', loadChildren: '../modules/credential/credential.module#CredentialModule'},
	{path: 'action', loadChildren: '../modules/apiAction/api-action.module#APIActionModule'},
	{path: 'datascript', loadChildren: '../modules/dataScript/data-script.module#DataScriptModule'},
	{path: 'user', loadChildren: '../modules/user/user.module#UserModule'},
	{path: 'dependencies', loadChildren: '../modules/dependencies/dependencies.module#DependenciesModule'},
	{path: 'license/admin', loadChildren: '../modules/licenseAdmin/license-admin.module#LicenseAdminModule'},
	{path: 'license/manager', loadChildren: '../modules/licenseManager/license-manager.module#LicenseManagerModule'},
	{path: Paths.notice, loadChildren: '../modules/noticeManager/notice-manager.module#NoticeManagerModule'},
	{path: 'reports', loadChildren: '../modules/reports/reports.module#ReportsModule'},
	{path: 'taskManager', loadChildren: '../modules/taskManager/task-manager.module#TaskManagerModule'},
	{path: 'export', loadChildren: '../modules/export/export.module#ExportModule'},
	{path: 'manufacturer', loadChildren: '../modules/manufacturer/manufacturer.module#ManufacturerModule'},
	{path: '**', redirectTo: 'auth' },
];

@NgModule({
	exports: [RouterModule],
	providers: [{provide: LocationStrategy, useClass: PathLocationStrategy}],
	imports: [RouterModule.forRoot(TDSAppRoute, {enableTracing: false, onSameUrlNavigation: 'reload'})]
})

export class TDSAppRouteModule {
}
