// Angular
import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
// Resolves
import {ModuleResolveService} from '../../shared/resolves/module.resolve.service';
import {PreferencesResolveService} from '../../shared/resolves/preferences-resolve.service';
// Services
import {AuthGuardService} from '../security/services/auth.guard.service';
// Components
import {AssetCommentListComponent} from './components/list/asset-comment-list.component';

/**
 * Asset Explorer Route States
 * @class
 * @classdesc To use externally to reference possible state of the Asset Explorer Module
 */
export class AssetCommentStates {
	public static readonly COMMENT_LIST = {
		url: 'list'
	};
}

export const AssetCommentRoute: Routes = [
	{path: '', pathMatch: 'full', redirectTo: AssetCommentStates.COMMENT_LIST.url},
	{
		path: AssetCommentStates.COMMENT_LIST.url,
		data: {
			page: {
				title: 'Asset Comments',
				instruction: '',
				menu: ['ASSETS.ASSETS', 'Asset Comments'],
			},
			requiresAuth: true
		},
		component: AssetCommentListComponent,
		canActivate: [AuthGuardService, ModuleResolveService, PreferencesResolveService],
		runGuardsAndResolvers: 'always'
	}
];

@NgModule({
	exports: [RouterModule],
	imports: [RouterModule.forChild(AssetCommentRoute)]
})

export class AssetCommentRouteModule {
}
