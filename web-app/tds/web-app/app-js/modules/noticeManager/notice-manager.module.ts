// Angular
import { NgModule }      from '@angular/core';
import { CommonModule }       from '@angular/common';
// Routing Logic
import { UIRouterModule } from 'ui-router-ng2';
import { NOTICE_STATES } from './notice-routing.states';
// Components
import { NoticeListComponent } from './components/list/notice-list.component';
import { HeaderComponent } from '../header/header.component';

@NgModule({
    imports: [
        CommonModule,
        UIRouterModule.forChild({ states: NOTICE_STATES }), // Same as { states: [state1, state2 ] }
    ],
    declarations: [ NoticeListComponent, HeaderComponent ],
    exports:    [ NoticeListComponent]
})

export class NoticesManagerModule {
}