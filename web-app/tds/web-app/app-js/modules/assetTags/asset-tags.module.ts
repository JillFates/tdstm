// Angular
import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {SharedModule} from '../../shared/shared.module';
import {FormsModule} from '@angular/forms';
import {BrowserModule} from '@angular/platform-browser';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
// Kendo
import {GridModule} from '@progress/kendo-angular-grid';
import {DropDownsModule} from '@progress/kendo-angular-dropdowns';
import {DateInputsModule} from '@progress/kendo-angular-dateinputs';
// Route Module
import {AssetTagsRouteModule} from './asset-tags-routing.states';
// Services
import {TagService} from './service/tag.service';
// Components
import {TagListComponent} from './components/tag-list/tag-list.component';
import {TagMergeDialogComponent} from './components/tag-merge/tag-merge-dialog.component';

@NgModule({
	imports: [
		// Angular
		CommonModule,
		SharedModule,
		FormsModule,
		BrowserAnimationsModule,
		// Kendo
		GridModule,
		DropDownsModule,
		DateInputsModule,
		// Internal
		AssetTagsRouteModule

	],
	declarations: [
		TagListComponent,
		TagMergeDialogComponent
	],
	providers: [
		TagService
	],
	exports: [
		TagListComponent
	],
	entryComponents: [
		TagMergeDialogComponent
	]
})

export class AssetTagsModule {
}
