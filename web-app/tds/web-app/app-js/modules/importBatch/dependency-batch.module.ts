import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {BrowserModule} from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { UIRouterModule } from '@uirouter/angular';
import { IMPORT_BATCH_STATES } from './dependency-batch-routing.states';
import { SharedModule } from '../../shared/shared.module';
import { GridModule } from '@progress/kendo-angular-grid';
import {DateInputsModule} from '@progress/kendo-angular-dateinputs';
import {DependencyBatchService} from './service/dependency-batch.service';
import {DependencyBatchDetailDialogComponent} from './components/dependency-batch-detail-dialog/dependency-batch-detail-dialog.component';
import {DropDownsModule} from '@progress/kendo-angular-dropdowns';
import {DependencyBatchRecordDetailSummaryComponent} from './components/dependency-batch-record-detail-summary/dependency-batch-record-detail-summary.component';
import {DependencyBatchRecordDetailFieldsComponent} from './components/dependency-batch-record-detail-fields/dependency-batch-record-detail-fields.component';
import {DependencyBatchRecordDetailDialogComponent} from './components/dependency-batch-record-detail-dialog/dependency-batch-record-detail-dialog.component';
import {ImportBatchListComponent} from './components/list/import-batch-list.component';

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
		UIRouterModule.forChild({ states: IMPORT_BATCH_STATES })
	],
	declarations: [
		ImportBatchListComponent,
		DependencyBatchDetailDialogComponent,
		DependencyBatchRecordDetailDialogComponent,
		DependencyBatchRecordDetailSummaryComponent,
		DependencyBatchRecordDetailFieldsComponent
	],
	providers: [
		DependencyBatchService
	],
	exports: [
		ImportBatchListComponent
	],
	entryComponents: [
		DependencyBatchDetailDialogComponent,
		DependencyBatchRecordDetailDialogComponent,
	],
})

export class DependencyBatchModule {
}
