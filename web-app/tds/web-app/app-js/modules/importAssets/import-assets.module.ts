// Angular
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {BrowserModule} from '@angular/platform-browser';
// Routing Logic
import { UIRouterModule } from '@uirouter/angular';
import { IMPORT_ASSETS_STATES } from './import-assets-routing.states';
// Component
import { SharedModule } from '../../shared/shared.module';
import { InputsModule } from '@progress/kendo-angular-inputs';
import { ManualImportComponent } from './components/manual-import/manual-import.component';
// Services
import { ImportAssetsService } from './service/import-assets.service';

@NgModule({
	imports: [
		CommonModule,
		SharedModule,
		FormsModule,
		InputsModule,
		BrowserModule,
		UIRouterModule.forChild({ states: IMPORT_ASSETS_STATES }),
	],
	declarations: [
		ManualImportComponent
	],
	providers: [
		ImportAssetsService
	],
	exports: [
		ManualImportComponent
	]
})

export class ImportAssetsModule {
}