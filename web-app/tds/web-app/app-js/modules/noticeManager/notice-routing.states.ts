// Angular
import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
// Resolves
import {ModuleResolveService} from '../../shared/resolves/module.resolve.service';
import {NoticeResolveService} from './resolve/notice-resolve.service';
// Services
import {AuthGuardService} from '../security/services/auth.guard.service';
import {PreferencesResolveService} from '../../shared/resolves/preferences-resolve.service';
// Components
import {NoticeListComponent} from './components/list/notice-list.component';

export class NoticeManagerStates {
	public static readonly NOTICE_LIST = {
		url: 'list'
	};
}

const TOP_MENU_PARENT_SECTION = 'menu-parent-admin';

export const NoticeManagerRoute: Routes = [
	{path: '', pathMatch: 'full', redirectTo: NoticeManagerStates.NOTICE_LIST.url},
	{
		path: NoticeManagerStates.NOTICE_LIST.url,
		data: {
			page: {
				title: 'NOTICE.NOTICE_ADMINISTRATION',
				instruction: '',
				menu: ['GLOBAL.ADMIN', 'NOTICE.NOTICE', 'GLOBAL.LIST'],
				topMenu: { parent: TOP_MENU_PARENT_SECTION, child: 'menu-admin-notice-manager', subMenu: true }
			},
			requiresAuth: true,
		},
		component: NoticeListComponent,
		resolve: {
			notices: NoticeResolveService
		},
		canActivate: [AuthGuardService, ModuleResolveService, PreferencesResolveService]
	}
];

@NgModule({
	exports: [RouterModule],
	imports: [RouterModule.forChild(NoticeManagerRoute)]
})

export class NoticeRouteModule {
}