// Angular
import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
// Resolves
import {ModuleResolveService} from '../../shared/resolves/module.resolve.service';
import {NoticeResolveService} from './resolve/notice-resolve.service';
// Services
import {AuthGuardService} from '../auth/service/auth.guard.service';
import {PreferencesResolveService} from '../../shared/resolves/preferences-resolve.service';
// Components
import {NoticeListComponent} from './components/list/notice-list.component';
import {PostNoticesComponent} from './components/post-notices/post-notices.component';

export class NoticeManagerStates {
	public static readonly NOTICE_LIST = {
		url: 'list'
	};
	public static readonly NOTICE_POST = {
		url: ''
	};
}

const TOP_MENU_PARENT_SECTION = 'menu-parent-admin';

export const NoticeManagerRoute: Routes = [
	{
		path: NoticeManagerStates.NOTICE_LIST.url,
		data: {
			page: {
				title: 'NOTICE.NOTICES',
				instruction: '',
				menu: ['GLOBAL.ADMIN', 'NOTICE.NOTICES'],
				topMenu: { parent: TOP_MENU_PARENT_SECTION, child: 'menu-admin-notice-manager', subMenu: true }
			},
			requiresAuth: true,
		},
		component: NoticeListComponent,
		resolve: {
			notices: NoticeResolveService
		},
		canActivate: [AuthGuardService, ModuleResolveService, PreferencesResolveService]
	},
	{
		path: NoticeManagerStates.NOTICE_POST.url,
		data: {
			page: {
				title: 'NOTICE.NOTICES',
				instruction: '',
				menu: ['GLOBAL.ADMIN', 'NOTICE.NOTICE', 'NOTICE.POST_NOTICES'],
			},
			requiresAuth: true,
		},
		component: PostNoticesComponent,
		resolve: {},
		canActivate: [AuthGuardService, ModuleResolveService, PreferencesResolveService]
	}
];

@NgModule({
	exports: [RouterModule],
	imports: [RouterModule.forChild(NoticeManagerRoute)]
})

export class NoticeRouteModule {
}
