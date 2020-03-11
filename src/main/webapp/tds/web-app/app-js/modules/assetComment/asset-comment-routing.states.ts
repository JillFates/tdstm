// Angular
import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
// Resolves
import {ModuleResolveService} from '../../shared/resolves/module.resolve.service';
import {PreferencesResolveService} from '../../shared/resolves/preferences-resolve.service';
// Services
import {AuthGuardService} from '../auth/service/auth.guard.service';
// Components
import {AssetCommentListComponent} from './components/list/asset-comment-list.component';
import {AssetCommentResolveService} from './resolve/asset-comment-resolve.service';

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

/**
 * Top menu parent section class for all Dependencies module.
 * @type {string}
 */
const TOP_MENU_PARENT_SECTION = 'menu-parent-assets';

export const AssetCommentRoute: Routes = [
	{path: '', pathMatch: 'full', redirectTo: AssetCommentStates.COMMENT_LIST.url},
	{
		path: AssetCommentStates.COMMENT_LIST.url,
		data: {
			page: {
				title: 'ASSETS.COMMENTS',
				instruction: '',
				menu: ['ASSETS.ASSETS', 'ASSETS.COMMENTS'],
				topMenu: { parent: TOP_MENU_PARENT_SECTION, child: 'menu-parent-assets-comments-list', subMenu: true }
			},
			requiresAuth: true
		},
		component: AssetCommentListComponent,
		resolve: {
			assetComments: AssetCommentResolveService
		},
		canActivate: [AuthGuardService, ModuleResolveService],
		runGuardsAndResolvers: 'always'
	}
];

@NgModule({
	exports: [RouterModule],
	imports: [RouterModule.forChild(AssetCommentRoute)]
})

export class AssetCommentRouteModule {
}
