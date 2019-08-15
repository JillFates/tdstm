// Angular
import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
// Resolves
import {ModuleResolveService} from '../../shared/resolves/module.resolve.service';
// Components
import {ExportComponent} from './components/export/export.component';
import {AuthGuardService} from '../auth/service/auth.guard.service';

/**
 * Auth Route States
 * @class
 * @classdesc To use externally to reference possible state of the Auth Module
 */
export class AssetExportRouteState {
	public static readonly ASSET_EXPORT = {
		url: 'export'
	};
}

export const AssetExportRoute: Routes = [
	{path: '', pathMatch: 'full', redirectTo: AssetExportRouteState.ASSET_EXPORT.url},
	{
		path: AssetExportRouteState.ASSET_EXPORT.url,
		data: {
			page: {
				title: 'ASSET_EXPORT.ASSET_EXPORT', instruction: '', menu: []
			}
		},
		component: ExportComponent,
		canActivate: [AuthGuardService, ModuleResolveService]
	},
];

@NgModule({
	exports: [RouterModule],
	imports: [RouterModule.forChild(AssetExportRoute)]
})

export class AssetExportRouteModule {
}
