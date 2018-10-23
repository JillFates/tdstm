// import { Ng2StateDeclaration } from '@uirouter/angular';
// import { NoticeListComponent } from './components/list/notice-list.component';
// import { HeaderComponent } from '../../shared/modules/header/header.component';
// import { NoticeService } from './service/notice.service';
//
// export class NoticeStates {
// 	public static readonly LIST = {
// 		name: 'tds.noticelist',
// 		url: '/notice/list'
// 	};
// }
//
// /**
//  * This state displays the notice list.
//  * It also provides a nested ui-view (viewport) for child states to fill in.
//  * The notice are fetched using a resolve.
//  */
// export const noticeListState: Ng2StateDeclaration = <Ng2StateDeclaration>{
// 	name: NoticeStates.LIST.name,
// 	url: NoticeStates.LIST.url,
// 	data: {
// 		page: {
// 			title: 'NOTICE_MANAGER.NOTICE_ADMINISTRATION',
// 			instruction: '',
// 			menu: ['NOTICE_MANAGER.ADMIN', 'NOTICE_MANAGER.NOTICE', 'GLOBAL.LIST']
// 		},
// 		requiresAuth: true,
// 		requiresPermission: 'NoticeView'
// 	},
// 	views: {
// 		'headerView@tds': { component: HeaderComponent },
// 		'containerView@tds': { component: NoticeListComponent }
// 	},
// 	resolve: [
// 		{
// 			token: 'notices',
// 			policy: { async: 'RXWAIT' },
// 			deps: [NoticeService],
// 			resolveFn: (service: NoticeService) => service.getNoticesList()
// 		}
// 	]
// };
//
// export const NOTICE_STATES = [
// 	noticeListState
// ];