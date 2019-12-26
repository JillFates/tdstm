// Angular
import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
// Shared
import {SharedModule} from '../../shared/shared.module';
// Route Module
import {AssetExplorerModule} from '../assetExplorer/asset-explorer.module';
import {InsightRouteModule} from './insight-routing.states';
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
import { ChartsModule } from '@progress/kendo-angular-charts';
// Components
import {InsightDashboardComponent} from './components/dashboard/insight-dashboard.component';

// Resolves
import {ModuleResolveService} from '../../shared/resolves/module.resolve.service';
// Services
import {InsightService} from './service/insight.service';

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
		InputsModule,
		DateInputsModule,
		SortableModule,
		IntlModule,
		InputsModule,
		DateInputsModule,
		ContextMenuModule,
		LayoutModule,
		ChartsModule,
		// Route
		InsightRouteModule
	],
	declarations: [
		InsightDashboardComponent,
	],
	providers: [
		// Resolve
		ModuleResolveService,
		// Service
		InsightService
	],
	entryComponents: [
	],
	exports: [
		InsightDashboardComponent,
	]
})

export class InsightModule {
}
