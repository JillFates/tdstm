// Angular
import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
// Shared
import {SharedModule} from '../../shared/shared.module';
import {TranslatePipe} from '../../shared/pipes/translate.pipe';
import {AssetExplorerModule} from '../assetExplorer/asset-explorer.module';
// Route Module
import {ReportsRouteModule} from './reports-routing.states';
// Kendo
import {DropDownsModule} from '@progress/kendo-angular-dropdowns';
import {GridModule} from '@progress/kendo-angular-grid';
import {PopupModule} from '@progress/kendo-angular-popup';
import {InputsModule} from '@progress/kendo-angular-inputs';
import { SortableModule } from '@progress/kendo-angular-sortable';
import { IntlModule } from '@progress/kendo-angular-intl';
import {DateInputsModule} from '@progress/kendo-angular-dateinputs';
// Components
import {PreEventCheckListSelectorComponent} from './components/event-checklist/pre-event-checklist.component';
// Resolves
import {ModuleResolveService} from '../../shared/resolves/module.resolve.service';
// Services
import {ReportsService} from './service/reports.service';
import {TaskReportComponent} from './components/task-report/task-report.component';
import {ServerConflictsReportComponent} from './components/server-conflicts/server-conflicts-report.component';

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
		ReportsRouteModule
	],
	declarations: [
		PreEventCheckListSelectorComponent,
		TaskReportComponent,
		ServerConflictsReportComponent
	],
	providers: [
		ReportsService,
		// Resolve
		ModuleResolveService,
		TranslatePipe
	],
	exports: [PreEventCheckListSelectorComponent],
	entryComponents: []
})

export class ReportsModule {
}
