// Angular
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
// Routing Logic
import { UIRouterModule } from '@uirouter/angular';
import { DEPENDENCY_INJECTION_STATES } from './import-assets-routing.states';
// Component
import { SharedModule } from '../../shared/shared.module';
import { InputsModule } from '@progress/kendo-angular-inputs';
// Services
import { ImportAssetsService } from './service/import-assets.service';
import {ManualImportComponent} from './components/manual-import/manual-import.component';

@NgModule({
	imports: [
		CommonModule,
		SharedModule,
		FormsModule,
		InputsModule,
		UIRouterModule.forChild({ states: DEPENDENCY_INJECTION_STATES }),
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