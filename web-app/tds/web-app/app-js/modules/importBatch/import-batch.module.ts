import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {BrowserModule} from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { UIRouterModule } from '@uirouter/angular';
import { IMPORT_BATCH_STATES } from './import-batch-routing.states';
import { SharedModule } from '../../shared/shared.module';
import { GridModule } from '@progress/kendo-angular-grid';
import {DateInputsModule} from '@progress/kendo-angular-dateinputs';
import {ImportBatchService} from './service/import-batch.service';
import {DropDownsModule} from '@progress/kendo-angular-dropdowns';
import {ImportBatchRecordSummaryComponent} from './components/record/import-batch-record-summary.component';
import {ImportBatchRecordFieldsComponent} from './components/record/import-batch-record-fields.component';
import {ImportBatchListComponent} from './components/list/import-batch-list.component';
import {ImportBatchDetailDialogComponent} from './components/detail/import-batch-detail-dialog.component';
import {ImportBatchRecordDialogComponent} from './components/record/import-batch-record-dialog.component';
import {ImportAssetsService} from './service/import-assets.service';
import {HTTP_INTERCEPTORS} from '@angular/common/http';
import {KendoFileUploadInterceptor} from '../../shared/providers/kendo-file-upload.interceptor';
import {ImportAssetsComponent} from './components/import-assets/import-assets.component';
import {UploadModule} from '@progress/kendo-angular-upload';
import {LayoutModule} from '@progress/kendo-angular-layout';

@NgModule({
	imports: [
		CommonModule,
		SharedModule,
		FormsModule,
		BrowserModule,
		BrowserAnimationsModule,
		GridModule,
		DropDownsModule,
		DateInputsModule,
		UploadModule,
		LayoutModule,
		UIRouterModule.forChild({ states: IMPORT_BATCH_STATES })
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
		ImportBatchService,
		ImportAssetsService,
		{
			provide: HTTP_INTERCEPTORS,
			useClass: KendoFileUploadInterceptor,
			multi: true
		}
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
