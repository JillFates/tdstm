// Angular
import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
// Services
import {AuthGuardService} from '../auth/service/auth.guard.service';
// Components
import {ModelListComponent} from './components/list/model-list.component';

/**
 * Top menu parent section class.
 * @type {string}
 */
const TOP_MENU_PARENT_ADMIN = 'menu-parent-admin';

export class ModelStates {
	public static readonly MODEL_LIST = {
		url: 'list'
	};
}

export const ModelRoute: Routes = [
	{path: '', pathMatch: 'full', redirectTo: ModelStates.MODEL_LIST.url},
	{
		path: ModelStates.MODEL_LIST.url,
		data: {
			page: {
				title: 'MODEL.MODELS',
				instruction: '',
				menu: ['GLOBAL.ADMIN', 'MODEL.MODELS'],
				topMenu: { parent: TOP_MENU_PARENT_ADMIN, child: 'menu-parent-admin-model', subMenu: true}
			},
			requiresAuth: true,
		},
		component: ModelListComponent,
		canActivate: [AuthGuardService]
	}
];

@NgModule({
	exports: [RouterModule],
	imports: [RouterModule.forChild(ModelRoute)]
})

export class ModelRouteModule {
}
