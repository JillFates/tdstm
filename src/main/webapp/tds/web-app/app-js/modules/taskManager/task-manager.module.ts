/**
 * Created by Jorge Morayta on 3/15/2017.
 */

// Angular
import {NgModule} from '@angular/core';
// Services
import {TaskService} from './service/task.service';
import {CommonModule} from '@angular/common';
import {SharedModule} from '../../shared/shared.module';
import {AssetExplorerModule} from '../assetExplorer/asset-explorer.module';
import {FormsModule} from '@angular/forms';
import {DropDownsModule} from '@progress/kendo-angular-dropdowns';
import {GridModule} from '@progress/kendo-angular-grid';
import {PopupModule} from '@progress/kendo-angular-popup';
import {SortableModule} from '@progress/kendo-angular-sortable';
import {IntlModule} from '@progress/kendo-angular-intl';
import {InputsModule} from '@progress/kendo-angular-inputs';
import {DateInputsModule} from '@progress/kendo-angular-dateinputs';
import {TaskManagerRouteModule} from './task-manager-routing.states';
import {TagsResolveService} from '../assetManager/resolve/tags-resolve.service';
import {ModuleResolveService} from '../../shared/resolves/module.resolve.service';
import {TaskListComponent} from './components/list/task-list.component';
import {ReportsService} from '../reports/service/reports.service';

@NgModule({
	imports: [
		// Angular
		CommonModule,
		SharedModule,
		AssetExplorerModule,
		FormsModule,
		// Kendo
		DropDownsModule,
		GridModule,
		PopupModule,
		SortableModule,
		IntlModule,
		InputsModule,
		DateInputsModule,
		// Route
		TaskManagerRouteModule
	],
	declarations: [
		TaskListComponent
	],
	providers: [
		// Resolve
		TagsResolveService,
		ModuleResolveService,
		// Services
		TaskService,
		ReportsService
	],
})

export class TaskManagerModule {
}
