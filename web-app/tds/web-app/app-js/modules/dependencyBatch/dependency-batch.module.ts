import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {BrowserModule} from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { UIRouterModule } from '@uirouter/angular';
import { DEPENDENCY_BATCH_STATES } from './dependency-batch-routing.states';
import { SharedModule } from '../../shared/shared.module';
import {DependencyBatchListComponent} from './components/dependency-batch-list/dependency-batch-list.component';
import { GridModule } from '@progress/kendo-angular-grid';
import {DateInputsModule} from '@progress/kendo-angular-dateinputs';
import {DependencyBatchService} from './service/dependency-batch.service';
import {DependencyBatchDetailDialogComponent} from './components/dependency-batch-detail-dialog/dependency-batch-detail-dialog.component';
import {DropDownsModule} from '@progress/kendo-angular-dropdowns';
import {DependencyBatchRecordDetailComponent} from './components/dependency-batch-record-detail/dependency-batch-record-detail.component';
import {DependencyBatchRecordDetailSummaryComponent} from './components/dependency-batch-record-detail-summary/dependency-batch-record-detail-summary.component';

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
		UIRouterModule.forChild({ states: DEPENDENCY_BATCH_STATES })
	],
	declarations: [
		DependencyBatchListComponent,
		DependencyBatchDetailDialogComponent,
		DependencyBatchRecordDetailComponent,
		DependencyBatchRecordDetailSummaryComponent
	],
	providers: [
		DependencyBatchService
	],
	exports: [
		DependencyBatchListComponent
	],
	entryComponents: [
		DependencyBatchDetailDialogComponent
	],
})

export class DependencyBatchModule {
}
