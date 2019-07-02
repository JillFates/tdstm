// Angular
import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
// Shared
import {SharedModule} from '../../shared/shared.module';
// Route Module
import {AssetExplorerModule} from '../assetExplorer/asset-explorer.module';
import {EventRouteModule} from './event-routing.states';
// Kendo Module
import {DropDownsModule} from '@progress/kendo-angular-dropdowns';
import {GridModule} from '@progress/kendo-angular-grid';
import {SortableModule} from '@progress/kendo-angular-sortable';
import {IntlModule} from '@progress/kendo-angular-intl';
import {DateInputsModule} from '@progress/kendo-angular-dateinputs';
import {PopupModule} from '@progress/kendo-angular-popup';
import {InputsModule} from '@progress/kendo-angular-inputs'
import {ContextMenuModule} from '@progress/kendo-angular-menu';
import {LayoutModule} from '@progress/kendo-angular-layout';
// Components
import {EventDashboardComponent} from './components/dashboard/event-dashboard.component';
import {NewsComponent} from './components/news/news.component';
import {NewsCreateEditComponent} from './components/news-create-edit/news-create-edit.component';
import {PlanVersusStatusComponent} from './components/plan-versus-status/plan-versus-status.component';
import {EventResumeComponent} from './components/event-resume/event-resume.component';
import {TeamTaskPercentsComponent} from './components/team-task-percents/team-task-percents.component';

// Resolves
import {ModuleResolveService} from '../../shared/resolves/module.resolve.service';
// Services
import {EventsService} from './service/events.service';

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
		ContextMenuModule,
		LayoutModule,
		// Route
		EventRouteModule
	],
	declarations: [
		EventDashboardComponent,
		NewsCreateEditComponent,
		NewsComponent,
		PlanVersusStatusComponent,
		EventResumeComponent,
		TeamTaskPercentsComponent
	],
	providers: [
		// Resolve
		ModuleResolveService,
		// Service
		EventsService
	],
	exports: [
		EventDashboardComponent,
		NewsCreateEditComponent,
		NewsComponent,
		PlanVersusStatusComponent,
		EventResumeComponent,
		TeamTaskPercentsComponent
	],
	entryComponents: [
		NewsCreateEditComponent
	]
})

export class EventModule {

}