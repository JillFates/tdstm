import {Ng2StateDeclaration} from 'ui-router-ng2';
import { NoticeListComponent } from './list/notice-list.component'
/**
 * This state displays the notice list.
 * It also provides a nested ui-view (viewport) for child states to fill in.
 * The notice are fetched using a resolve.
 */
export const noticeListState: Ng2StateDeclaration = <Ng2StateDeclaration>{
    name: 'noticeList',
    url: '/notice/list',
    component: NoticeListComponent
};

export const NOTICE_STATES = [
    noticeListState
];