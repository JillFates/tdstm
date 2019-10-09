// Angular
import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
// Resolves
import {ModuleResolveService} from '../../shared/resolves/module.resolve.service';
// Services
import {AuthGuardService} from '../auth/service/auth.guard.service';
// Components
import {TagListComponent} from './components/tag-list/tag-list.component';
// Models
import {Permission} from '../../shared/model/permission.model';

const TOP_MENU_PARENT_SECTION = 'menu-parent-projects';

/**
 * Asset Tag Route States
 * @class
 * @classdesc To use externally to reference possible state of the Asset Tag Module
 */
export class AssetTagsRouteStates {
	public static readonly TAG_LIST = {
		url: 'list'
	};
}

export const AssetTagsRoute: Routes = [
	{path: '', pathMatch: 'full', redirectTo: AssetTagsRouteStates.TAG_LIST.url},
	{
		path: AssetTagsRouteStates.TAG_LIST.url,
		data: {
			page: {
				title: 'ASSET_TAGS.MANAGE_TAGS',
				instruction: '',
				menu: ['GLOBAL.PROJECT', 'ASSET_TAGS.MANAGE_TAGS'],
				topMenu: {parent: TOP_MENU_PARENT_SECTION, child: 'menu-parent-project-tags'}
			},
			requiresAuth: true,
			requiresPermissions: [Permission.TagView],
		},
		component: TagListComponent,
		canActivate: [AuthGuardService, ModuleResolveService]
	}
];

@NgModule({
	exports: [RouterModule],
	imports: [RouterModule.forChild(AssetTagsRoute)]
})

export class AssetTagsRouteModule {
}