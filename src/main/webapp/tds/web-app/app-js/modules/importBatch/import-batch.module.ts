// Angular
import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
// Modules
import {SharedModule} from '../../shared/shared.module';
import {ClarityModule} from '@clr/angular';
import {TdsComponentLibraryModule} from 'tds-component-library';

// Kendo
import {GridModule} from '@progress/kendo-angular-grid';
import {DateInputsModule} from '@progress/kendo-angular-dateinputs';
import {ImportBatchService} from './service/import-batch.service';
import {DropDownsModule} from '@progress/kendo-angular-dropdowns';
import {UploadModule} from '@progress/kendo-angular-upload';
import {LayoutModule} from '@progress/kendo-angular-layout';
// Route Module
import {ImportBatchRouteModule} from './import-batch-routing.states';
// Resolves
import {ModuleResolveService} from '../../shared/resolves/module.resolve.service';
// Services
import {ImportAssetsService} from './service/import-assets.service';
import {DataScriptService} from '../dataScript/service/data-script.service';
// Components
import {ImportBatchRecordSummaryComponent} from './components/record/import-batch-record-summary.component';
import {ImportBatchRecordFieldsComponent} from './components/record/import-batch-record-fields.component';
import {ImportBatchListComponent} from './components/list/import-batch-list.component';
import {ImportBatchDetailDialogComponent} from './components/detail/import-batch-detail-dialog.component';
import {ImportBatchRecordDialogComponent} from './components/record/import-batch-record-dialog.component';
import {ImportAssetsComponent} from './components/import-assets/import-assets.component';

@NgModule({
	imports: [
		// Angular
		CommonModule,
		SharedModule,
		FormsModule,
		// Kendo
		GridModule,
		DropDownsModule,
		DateInputsModule,
		UploadModule,
		LayoutModule,
		// Claritys
		ClarityModule,
		TdsComponentLibraryModule,
		// Route
		ImportBatchRouteModule
	],
	declarations: [
		ImportBatchListComponent,
		ImportBatchDetailDialogComponent,
		ImportBatchRecordDialogComponent,
		ImportBatchRecordSummaryComponent,
		ImportBatchRecordFieldsComponent,
		ImportAssetsComponent
	],
	providers: [
		ModuleResolveService,
		ImportBatchService,
		ImportAssetsService,
		DataScriptService
	],
	exports: [
		ImportBatchListComponent,
		ImportAssetsComponent
	],
	entryComponents: [
		ImportBatchDetailDialogComponent,
		ImportBatchRecordDialogComponent
	],
})

export class ImportBatchModule {
}
