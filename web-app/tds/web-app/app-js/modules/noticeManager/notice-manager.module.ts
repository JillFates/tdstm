// Angular
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
// Routing Logic
import { UIRouterModule } from 'ui-router-ng2';
import { NOTICE_STATES } from './notice-routing.states';
// Components
import { NoticeListComponent } from './components/list/notice-list.component';
import { HeaderComponent } from '../header/header.component';
import { SharedModule } from '../../shared/shared.module'

@NgModule({
    imports: [
        CommonModule,
        SharedModule,
        UIRouterModule.forChild({ states: NOTICE_STATES }), // Same as { states: [state1, state2 ] }
    ],
    declarations: [NoticeListComponent, HeaderComponent],
    exports: [NoticeListComponent]
})

export class NoticesManagerModule {
}