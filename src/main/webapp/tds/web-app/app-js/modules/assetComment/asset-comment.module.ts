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
import {SingleCommentComponent} from './components/single-comment/single-comment.component';
// Resolves
import {ModuleResolveService} from '../../shared/resolves/module.resolve.service';
// Services
import {AssetCommentService} from './service/asset-comment.service';
import {AssetCommentResolveService} from './resolve/asset-comment-resolve.service';
import {AssetExplorerService} from '../assetExplorer/service/asset-explorer.service';
import {TaskService} from '../taskManager/service/task.service';

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
		AssetCommentListComponent,
		SingleCommentComponent
	],
	providers: [
		// Resolve
		ModuleResolveService,
		AssetCommentResolveService,
		// Service
		AssetCommentService,
		AssetExplorerService,
		TaskService
	],
	exports: [
		AssetCommentListComponent,
	],
	entryComponents: [
		SingleCommentComponent
	]
})

export class AssetCommentModule {
}