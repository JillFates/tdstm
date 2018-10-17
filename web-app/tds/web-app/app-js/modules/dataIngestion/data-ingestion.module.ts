// Angular
import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {HTTP_INTERCEPTORS, HttpClientModule} from '@angular/common/http';
// Shared
import {SharedModule} from '../../shared/shared.module';
// Route Module
import {DataIngestionRouteModule} from './data-ingestion-routing.states';
// Kendo
import {DropDownsModule} from '@progress/kendo-angular-dropdowns';
import {GridModule} from '@progress/kendo-angular-grid';
import {DateInputsModule,} from '@progress/kendo-angular-dateinputs';
import {SortableModule} from '@progress/kendo-angular-sortable';
import {PopupModule} from '@progress/kendo-angular-popup';
import {InputsModule} from '@progress/kendo-angular-inputs';
import {LayoutModule} from '@progress/kendo-angular-layout';
import {NumericTextBoxModule} from '@progress/kendo-angular-inputs';
// Components
import {DataScriptListComponent} from './components/data-script-list/data-script-list.component';
import {DataScriptViewEditComponent} from './components/data-script-view-edit/data-script-view-edit.component';
import {DataScriptConsoleComponent} from './components/data-script-console/data-script-console.component';
import {DataScriptEtlBuilderComponent} from './components/data-script-etl-builder/data-script-etl-builder.component';
import {DataScriptSampleDataComponent} from './components/data-script-sample-data/data-script-sample-data.component';
import {APIActionListComponent} from './components/api-action-list/api-action-list.component';
import {APIActionViewEditComponent} from './components/api-action-view-edit/api-action-view-edit.component';
import {AssetExplorerModule} from '../assetExplorer/asset-explorer.module';
import {PopupPollingComponent} from './components/popups/popup-polling.component';
import {PopupProvidesDataComponent} from './components/popups/popup-provides-data.component';
// Resolves
import {ModuleResolveService} from '../../shared/resolves/module.resolve.service';
// Services
import {DataIngestionService} from './service/data-ingestion.service';
import {UploadModule} from '@progress/kendo-angular-upload';
import {KendoFileUploadInterceptor} from '../../shared/providers/kendo-file-upload.interceptor';
import {ImportAssetsService} from '../importBatch/service/import-assets.service';

@NgModule({
	imports: [
		// Angular
		CommonModule,
		SharedModule,
		FormsModule,
		// TODO: Only for Kendo Upload at this point
		HttpClientModule,
		// Kendo
		DropDownsModule,
		GridModule,
		DateInputsModule,
		SortableModule,
		PopupModule,
		NumericTextBoxModule,
		HttpClientModule,
		UploadModule,
		InputsModule,
		LayoutModule,
		AssetExplorerModule, // So we can use Shared components that belongs to this module
		// Route
		DataIngestionRouteModule
	],
	declarations: [
		DataScriptListComponent,
		DataScriptViewEditComponent,
		DataScriptConsoleComponent,
		DataScriptEtlBuilderComponent,
		DataScriptSampleDataComponent,
		APIActionListComponent,
		APIActionViewEditComponent,
		PopupPollingComponent,
		PopupProvidesDataComponent
	],
	providers: [
		ModuleResolveService,
		DataIngestionService,
		ImportAssetsService,
		{
			provide: HTTP_INTERCEPTORS,
			useClass: KendoFileUploadInterceptor,
			multi: true
		}
	],
	exports: [
		DataScriptListComponent,
		DataScriptViewEditComponent
	],
	entryComponents: [
		DataScriptViewEditComponent,
		DataScriptConsoleComponent,
		DataScriptEtlBuilderComponent,
		DataScriptSampleDataComponent,
		APIActionViewEditComponent
	]
})

export class DataIngestionModule {
}