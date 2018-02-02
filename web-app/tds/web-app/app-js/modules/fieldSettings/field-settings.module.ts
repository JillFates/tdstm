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
import { MinMaxConfigurationPopupComponent } from './components/min-max/min-max-configuration-popup.component';
import { SelectListConfigurationPopupComponent } from './components/select-list/selectlist-configuration-popup.component';
import { FieldSettingsImportanceComponent } from './components/imp/field-settings-imp.component';
import { SharedModule } from '../../shared/shared.module';
// Import Kendo Modules
import { GridModule } from '@progress/kendo-angular-grid';
import { PopupModule } from '@progress/kendo-angular-popup';
import { SortableModule } from '@progress/kendo-angular-sortable';
import { DialogModule } from '@progress/kendo-angular-dialog';
import { InputsModule } from '@progress/kendo-angular-inputs';
// Services
import { FieldSettingsService } from './service/field-settings.service';
import { CustomDomainService } from './service/custom-domain.service';

@NgModule({
	imports: [
		CommonModule,
		SharedModule,
		FormsModule,
		GridModule,
		UIRouterModule.forChild({ states: FIELD_SETTINGS_STATES }),
		PopupModule,
		SortableModule,
		DialogModule,
		InputsModule
	],
	declarations: [
		FieldSettingsListComponent,
		FieldSettingsGridComponent,
		SelectListConfigurationPopupComponent,
		FieldSettingsImportanceComponent,
		MinMaxConfigurationPopupComponent
	],
	providers: [FieldSettingsService, CustomDomainService],
	exports: [
		SelectListConfigurationPopupComponent,
		FieldSettingsImportanceComponent,
		MinMaxConfigurationPopupComponent
	],
	entryComponents: [
		SelectListConfigurationPopupComponent,
		MinMaxConfigurationPopupComponent
	]
})

export class FieldSettingsModule {
}