// Angular
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {BrowserModule} from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
// Routing Logic
import { UIRouterModule } from '@uirouter/angular';
import { DEPENDENCY_BATCH_STATES } from './dependency-batch-routing.states';
// Component
import { SharedModule } from '../../shared/shared.module';
import {DependencyBatchListComponent} from './components/dependency-batch-list/dependency-batch-list.component';
// Kendo
import { GridModule } from '@progress/kendo-angular-grid';
import {DateInputsModule} from '@progress/kendo-angular-dateinputs';
// Services
import {DependencyBatchService} from './service/dependency-batch.service';

@NgModule({
	imports: [
		CommonModule,
		SharedModule,
		FormsModule,
		BrowserModule,
		BrowserAnimationsModule,
		GridModule,
		DateInputsModule,
		UIRouterModule.forChild({ states: DEPENDENCY_BATCH_STATES })
	],
	declarations: [
		DependencyBatchListComponent
	],
	providers: [
		DependencyBatchService
	],
	exports: [
		DependencyBatchListComponent
	],
	entryComponents: [],
})

export class DependencyBatchModule {
}
