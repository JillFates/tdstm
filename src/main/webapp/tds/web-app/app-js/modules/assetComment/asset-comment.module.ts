// Angular
import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
// Shared
import {SharedModule} from '../../shared/shared.module';
// Route Module
import {AssetCommentRouteModule} from './asset-comment-routing.states';
// Kendo
import {DropDownsModule} from '@progress/kendo-angular-dropdowns';
import {GridModule} from '@progress/kendo-angular-grid';
import {PopupModule} from '@progress/kendo-angular-popup';
import {InputsModule} from '@progress/kendo-angular-inputs';
import {DateInputsModule} from '@progress/kendo-angular-dateinputs';
// Components
import {AssetCommentListComponent} from './components/list/asset-comment-list.component';
// Resolves
import {ModuleResolveService} from '../../shared/resolves/module.resolve.service';

@NgModule({
	imports: [
		// Angular
		CommonModule,
		SharedModule,
		FormsModule,
		// Kendo
		DropDownsModule,
		GridModule,
		PopupModule,
		InputsModule,
		DateInputsModule,
		// Route
		AssetCommentRouteModule
	],
	declarations: [
		AssetCommentListComponent
	],
	providers: [
		// Resolve
		ModuleResolveService
	],
	exports: [
		AssetCommentListComponent,
	],
	entryComponents: [
	]
})

export class AssetCommentModule {
}