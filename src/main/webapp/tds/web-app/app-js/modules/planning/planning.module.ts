// Angular
import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
// Shared
import {SharedModule} from '../../shared/shared.module';
// Route Module
import {PlanningRouteModule} from './planning-routing.states';
// Kendo
import {DropDownsModule} from '@progress/kendo-angular-dropdowns';
import {GridModule} from '@progress/kendo-angular-grid';
import {PopupModule} from '@progress/kendo-angular-popup';
import {InputsModule} from '@progress/kendo-angular-inputs';
import {DateInputsModule} from '@progress/kendo-angular-dateinputs';
// Components
import {PlanningDashboardComponent} from './components/dashboard/planning-dashboard.component';
// Services
import {PlanningService} from './service/planning.service';

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
		PlanningRouteModule
	],
	declarations: [
		PlanningDashboardComponent
	],
	providers: [
		// Service
		PlanningService
	],
	entryComponents: [
		PlanningDashboardComponent
	]
})

export class PlanningModule {
}