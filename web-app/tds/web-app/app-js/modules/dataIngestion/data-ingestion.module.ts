// Angular
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
// Routing Logic
import { UIRouterModule } from '@uirouter/angular';
import { DATA_INGESTION_STATES } from './data-ingestion-routing.states';
// Components
import { DataScriptListComponent } from './components/data-script-list/data-script-list.component';
import { DataScriptViewEditComponent } from './components/data-script-view-edit/data-script-view-edit.component';
import { ProviderListComponent } from './components/provider-list/provider-list.component';
import { ProviderViewEditComponent } from './components/provider-view-edit/provider-view-edit.component';
import { DataScriptConsoleComponent } from './components/data-script-console/data-script-console.component';
import { DataScriptEtlBuilderComponent } from './components/data-script-etl-builder/data-script-etl-builder.component';
import { DataScriptSampleDataComponent } from './components/data-script-sample-data/data-script-sample-data.component';
import { APIActionListComponent } from './components/api-action-list/api-action-list.component';
import { APIActionViewEditComponent } from './components/api-action-view-edit/api-action-view-edit.component';
import { SharedModule } from '../../shared/shared.module';
import { AssetExplorerModule } from '../assetExplorer/asset-explorer.module';
// Import Kendo Modules
import { DropDownListModule } from '@progress/kendo-angular-dropdowns';
import { GridModule } from '@progress/kendo-angular-grid';
import { DateInputsModule } from '@progress/kendo-angular-dateinputs';
import { SortableModule } from '@progress/kendo-angular-sortable';
// Services
import { DataIngestionService } from './service/data-ingestion.service';

@NgModule({
	imports: [
		CommonModule,
		SharedModule,
		FormsModule,
		DropDownListModule,
		GridModule,
		DateInputsModule,
		SortableModule,
		AssetExplorerModule, // So we can use Shared components that belongs to this module
		UIRouterModule.forChild({ states: DATA_INGESTION_STATES })
	],
	declarations: [
		DataScriptListComponent,
		DataScriptViewEditComponent,
		ProviderListComponent,
		ProviderViewEditComponent,
		DataScriptConsoleComponent,
		DataScriptEtlBuilderComponent,
		DataScriptSampleDataComponent,
		APIActionListComponent,
		APIActionViewEditComponent
	],
	providers: [DataIngestionService],
	exports: [
		DataScriptListComponent,
		DataScriptViewEditComponent,
		ProviderListComponent,
		ProviderViewEditComponent
	],
	entryComponents: [
		DataScriptViewEditComponent,
		ProviderViewEditComponent,
		DataScriptConsoleComponent,
		DataScriptEtlBuilderComponent,
		DataScriptSampleDataComponent,
		APIActionViewEditComponent
	]
})

export class DataIngestionModule {
}