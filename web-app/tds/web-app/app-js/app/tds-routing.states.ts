import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {LocationStrategy, PathLocationStrategy} from '@angular/common';

export const TDSAppRoute: Routes = [
	{path: '', pathMatch: 'full', redirectTo: 'security'},
	{path: 'security', loadChildren: '../modules/security/security.module#SecurityModule'},
	{path: 'tag', loadChildren: '../modules/assetTags/asset-tags.module#AssetTagsModule'},
	{path: 'asset', loadChildren: '../modules/assetExplorer/asset-explorer.module#AssetExplorerModule'},
	{path: 'fieldsettings', loadChildren: '../modules/fieldSettings/field-settings.module#FieldSettingsModule'},
	{path: 'importbatch', loadChildren: '../modules/importBatch/import-batch.module#ImportBatchModule'},
	{path: 'provider', loadChildren: '../modules/provider/provider.module#ProviderModule'},
	{path: 'credential', loadChildren: '../modules/credential/credential.module#CredentialModule'},
	{path: 'action', loadChildren: '../modules/apiAction/api-action.module#APIActionModule'},
	{path: 'datascript', loadChildren: '../modules/dataScript/data-script.module#DataScriptModule'}
];

@NgModule({
	exports: [RouterModule],
	providers: [{provide: LocationStrategy, useClass: PathLocationStrategy}],
	imports: [RouterModule.forRoot(TDSAppRoute, {enableTracing: true, onSameUrlNavigation: 'reload'})]
})

export class TDSAppRouteModule {
}