// Angular
import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
// Resolves
import {ModuleResolveService} from '../../shared/resolves/module.resolve.service';
// Components
import {ExportAssetComponent} from './components/export-asset/export-asset.component';
import {AuthGuardService} from '../auth/service/auth.guard.service';

/**
 * Auth Route States
 * @class
 * @classdesc To use externally to reference possible state of the Auth Module
 */
export class ExportRouteState {
	public static readonly ASSET_EXPORT = {
		url: 'assets'
	};
}

export const ExportRoute: Routes = [
	{path: '', pathMatch: 'full', redirectTo: ExportRouteState.ASSET_EXPORT.url},
	{
		path: ExportRouteState.ASSET_EXPORT.url,
		data: {
			page: {
				title: 'ASSET_EXPORT.ASSET_EXPORT', instruction: '', menu: ['ASSETS.ASSETS', 'ASSET_EXPORT.ASSET_EXPORT']
			}
		},
		component: ExportAssetComponent,
		canActivate: [AuthGuardService, ModuleResolveService]
	},
];

@NgModule({
	exports: [RouterModule],
	imports: [RouterModule.forChild(ExportRoute)]
})

export class ExportRouteModule {
}