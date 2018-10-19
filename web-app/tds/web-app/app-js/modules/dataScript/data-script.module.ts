// Angular
import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {HTTP_INTERCEPTORS, HttpClientModule} from '@angular/common/http';
// Shared
import {SharedModule} from '../../shared/shared.module';
// Route Module
import {DataScriptRouteModule} from './data-script-routing.states';
// Kendo
import {DropDownsModule} from '@progress/kendo-angular-dropdowns';
import {GridModule} from '@progress/kendo-angular-grid';
import {DateInputsModule} from '@progress/kendo-angular-dateinputs';
import {SortableModule} from '@progress/kendo-angular-sortable';
import {PopupModule} from '@progress/kendo-angular-popup';
import {InputsModule} from '@progress/kendo-angular-inputs';
import {LayoutModule} from '@progress/kendo-angular-layout';
import {NumericTextBoxModule} from '@progress/kendo-angular-inputs';
// Components
import {DataScriptListComponent} from './components/list/data-script-list.component';
import {DataScriptViewEditComponent} from './components/view-edit/data-script-view-edit.component';
import {DataScriptConsoleComponent} from './components/console/data-script-console.component';
import {DataScriptEtlBuilderComponent} from './components/etl-builder/data-script-etl-builder.component';
import {DataScriptSampleDataComponent} from './components/sample-data/data-script-sample-data.component';
import {AssetExplorerModule} from '../assetExplorer/asset-explorer.module';
// Resolves
import {ModuleResolveService} from '../../shared/resolves/module.resolve.service';
import {DataScriptResolveService} from './resolve/data-script-resolve.service';
// Services
import {DataScriptService} from './service/data-script.service';
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
		DataScriptRouteModule
	],
	declarations: [
		DataScriptListComponent,
		DataScriptViewEditComponent,
		DataScriptConsoleComponent,
		DataScriptEtlBuilderComponent,
		DataScriptSampleDataComponent
	],
	providers: [
		// Resolve
		ModuleResolveService,
		DataScriptResolveService,
		// Service
		DataScriptService,
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
		DataScriptSampleDataComponent
	]
})

export class DataScriptModule {
}