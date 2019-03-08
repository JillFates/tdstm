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
import {InputsModule} from '@progress/kendo-angular-inputs' ;
import {DateInputsModule} from '@progress/kendo-angular-dateinputs';
// Components
import {NoticeListComponent} from './components/list/notice-list.component';
import {NoticeViewEditComponent} from './components/view-edit/notice-view-edit.component';
import {ViewHtmlComponent} from './components/view-html/view-html.component';
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
		InputsModule,
		DateInputsModule,
		// Route
		NoticeRouteModule
	],
	declarations: [
		NoticeListComponent,
		NoticeViewEditComponent,
		ViewHtmlComponent
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
		NoticeViewEditComponent,
		ViewHtmlComponent
	],
	entryComponents: [
		NoticeViewEditComponent,
		ViewHtmlComponent
	]
})

export class NoticeManagerModule {
}