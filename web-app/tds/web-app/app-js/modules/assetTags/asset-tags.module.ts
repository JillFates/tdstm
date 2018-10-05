// Angular
import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
// Shared
import {SharedModule} from '../../shared/shared.module';
// Kendo
import {GridModule} from '@progress/kendo-angular-grid';
import {DropDownsModule} from '@progress/kendo-angular-dropdowns';
import {DateInputsModule} from '@progress/kendo-angular-dateinputs';
// Route Module
import {AssetTagsRouteModule} from './asset-tags-routing.states';
// Services
import {ModuleResolverService} from './service/module.resolver.service';
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
		// Kendo
		GridModule,
		DropDownsModule,
		DateInputsModule,
		// Route
		AssetTagsRouteModule
	],
	declarations: [
		TagListComponent,
		TagMergeDialogComponent
	],
	providers: [
		ModuleResolverService,
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
