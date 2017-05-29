// Angular
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
// Routing Logic
import { UIRouterModule } from '@uirouter/angular';
import { FIELD_SETTINGS_STATES } from './field-settings.states';
// Components
import { FieldSettingsListComponent } from './components/list/field-settings-list.component';
import { FieldSettingsGridComponent } from './components/grid/field-settings-grid.component';
import { FieldSettingsImportanceComponent } from './components/imp/field-settings-imp.component';
import { SharedModule } from '../../shared/shared.module';
// Import Kendo Modules
import { GridModule } from '@progress/kendo-angular-grid';
// Services
import { FieldSettingsService } from './service/field-settings.service';

@NgModule({
	imports: [
		CommonModule,
		SharedModule,
		FormsModule,
		GridModule,
		UIRouterModule.forChild({ states: FIELD_SETTINGS_STATES })
	],
	declarations: [
		FieldSettingsListComponent,
		FieldSettingsGridComponent,
		FieldSettingsImportanceComponent
	],
	providers: [FieldSettingsService],
	exports: [FieldSettingsListComponent]
})

export class FieldSettingsModule {
}