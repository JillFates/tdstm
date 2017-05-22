import { Ng2StateDeclaration } from '@uirouter/angular';
import { NoticeListComponent } from './components/list/notice-list.component';
import { HeaderComponent } from '../../shared/modules/header/header.component';

export class NoticeStates {
    public static readonly LIST = {
        name: 'tds.noticelist',
        url: '/notice/list'
    };
}

/**
 * This state displays the notice list.
 * It also provides a nested ui-view (viewport) for child states to fill in.
 * The notice are fetched using a resolve.
 */
export const noticeListState: Ng2StateDeclaration = <Ng2StateDeclaration>{
    name: NoticeStates.LIST.name,
    url: NoticeStates.LIST.url,
    data: {
        page: {
            title: 'NOTICE_MANAGER.NOTICE_ADMINISTRATION',
            instruction: '',
            menu: ['NOTICE_MANAGER.ADMIN', 'NOTICE_MANAGER.NOTICE', 'NOTICE_MANAGER.LIST']
        },
        requiresAuth: true
    },
    views: {
        'headerView@tds': { component: HeaderComponent },
        'containerView@tds': { component: NoticeListComponent }
    }
};

export const NOTICE_STATES = [
    noticeListState
];