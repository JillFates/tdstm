import {Ng2StateDeclaration} from 'ui-router-ng2';
import {NoticeListComponent} from './components/list/notice-list.component';
import {HeaderComponent} from '../header/header.component';

/**
 * This state displays the notice list.
 * It also provides a nested ui-view (viewport) for child states to fill in.
 * The notice are fetched using a resolve.
 */
export const noticeListState: Ng2StateDeclaration = <Ng2StateDeclaration>{
    name: 'tds.noticelist',
    url: '/notice/list',
    data: {
        page: {title: 'Notice Administration', instruction: '', menu: ['Admin', 'Notice', 'List']},
        requiresAuth: true
    },
    views: {
        'headerView@tds': {component: HeaderComponent},
        'containerView@tds': {component: NoticeListComponent}
    }
};

export const NOTICE_STATES = [
    noticeListState
];