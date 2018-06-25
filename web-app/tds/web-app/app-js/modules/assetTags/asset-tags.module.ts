import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {SharedModule} from '../../shared/shared.module';
import {FormsModule} from '@angular/forms';
import {BrowserModule} from '@angular/platform-browser';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {GridModule} from '@progress/kendo-angular-grid';
import {DropDownsModule} from '@progress/kendo-angular-dropdowns';
import {DateInputsModule} from '@progress/kendo-angular-dateinputs';
import {UIRouterModule} from '@uirouter/angular';
import {TagService} from './service/tag.service';
import {TagListComponent} from './components/tag-list/tag-list.component';
import {ASSET_TAGS_STATES} from './asset-tags-routing.states';

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
		UIRouterModule.forChild({ states: ASSET_TAGS_STATES })
	],
	declarations: [
		TagListComponent,
	],
	providers: [
		TagService
	],
	exports: [
		TagListComponent
	],
	entryComponents: []
})

export class AssetTagsModule {
}
