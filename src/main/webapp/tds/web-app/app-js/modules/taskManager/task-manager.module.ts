/**
 * Created by Jorge Morayta on 3/15/2017.
 */

// Angular
import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {SharedModule} from '../../shared/shared.module';
import {AssetExplorerModule} from '../assetExplorer/asset-explorer.module';
// Services
import {TaskService} from './service/task.service';
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
import {NeighborhoodComponent} from './components/neighborhood/neighborhood.component';
import {DialogsModule} from '@progress/kendo-angular-dialog';
import {TaskViewToggleComponent} from './components/common/task-view-toggle.component';
import {TooltipModule} from '@progress/kendo-angular-tooltip';
import {ButtonModule, DropDownButtonModule} from '@progress/kendo-angular-buttons';
import {TaskHighlightFilter} from './components/common/task-highlight-filter.component';

@NgModule({
	imports: [
		// Angular
		CommonModule,
		SharedModule,
		FormsModule,
		AssetExplorerModule,
		// Kendo
		DropDownsModule,
		GridModule,
		PopupModule,
		SortableModule,
		IntlModule,
		InputsModule,
		DateInputsModule,
		DialogsModule,
		TooltipModule,
		DropDownButtonModule,
		ButtonModule,
		// Route
		TaskManagerRouteModule
	],
	declarations: [
		TaskListComponent,
		NeighborhoodComponent,
		TaskViewToggleComponent,
		TaskHighlightFilter
	],
	providers: [
		// Resolve
		ModuleResolveService,
		TagsResolveService,
		ReportsService,
		TaskService
	]
})

export class TaskManagerModule {
}
