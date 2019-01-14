// Angular
import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
// Shared
import {SharedModule} from '../../shared/shared.module';
// Route Module
import {NoticeRouteModule} from './notice-routing.states';
// Kendo
import {GridModule} from '@progress/kendo-angular-grid';
import {DropDownListModule} from '@progress/kendo-angular-dropdowns';
// Components
import {NoticeListComponent} from './components/list/notice-list.component';
import {NoticeViewEditComponent} from './components/view-edit/notice-view-edit.component';
// Resolves
import {ModuleResolveService} from '../../shared/resolves/module.resolve.service';
import {NoticeResolveService} from './resolve/notice-resolve.service';
// Services
import {NoticeService} from './service/notice.service';

@NgModule({
	imports: [
		// Angular
		CommonModule,
		SharedModule,
		FormsModule,
		// Kendo
		DropDownListModule,
		GridModule,
		// Route
		NoticeRouteModule
	],
	declarations: [
		NoticeListComponent,
		NoticeViewEditComponent
	],
	providers: [
		// Resolve
		ModuleResolveService,
		NoticeResolveService,
		// Service
		NoticeService
	],
	exports: [
		NoticeListComponent,
		NoticeViewEditComponent
	],
	entryComponents: [
		NoticeViewEditComponent
	]
})

export class NoticeManagerModule {
}