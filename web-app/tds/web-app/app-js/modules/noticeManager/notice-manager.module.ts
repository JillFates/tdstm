// Angular
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
// Routing Logic
import { UIRouterModule } from 'ui-router-ng2';
import { NOTICE_STATES } from './notice-routing.states';
// Components
import { NoticeListComponent } from './components/list/notice-list.component';
import { NoticeGridComponent } from './components/grid/notice-grid.component';
import { NoticeFormComponent } from './components/form/notice-form.component';
import { SharedModule } from '../../shared/shared.module';
// Import Kendo Modules
import { GridModule } from '@progress/kendo-angular-grid';
import { DropDownListModule } from '@progress/kendo-angular-dropdowns';

@NgModule({
    imports: [
        CommonModule,
        SharedModule,
        FormsModule,
        GridModule,
        DropDownListModule,
        UIRouterModule.forChild({ states: NOTICE_STATES }), // Same as { states: [state1, state2 ] }
    ],
    declarations: [NoticeListComponent, NoticeFormComponent, NoticeGridComponent],
    entryComponents: [NoticeFormComponent],
    exports: [NoticeGridComponent]
})

export class NoticesManagerModule {
}