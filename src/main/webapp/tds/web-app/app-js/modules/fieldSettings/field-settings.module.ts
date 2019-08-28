// Angular
import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
// Shared
import {SharedModule} from '../../shared/shared.module';
// Kendo
import {GridModule} from '@progress/kendo-angular-grid';
import {PopupModule} from '@progress/kendo-angular-popup';
import {SortableModule} from '@progress/kendo-angular-sortable';
import {DialogModule} from '@progress/kendo-angular-dialog';
import {InputsModule} from '@progress/kendo-angular-inputs';
// Route Module
import {FieldSettingsRouteModule} from './field-settings.states';
// Components
import {FieldSettingsListComponent} from './components/list/field-settings-list.component';
import {FieldSettingsGridComponent} from './components/grid/field-settings-grid.component';
import {MinMaxConfigurationPopupComponent} from './components/min-max/min-max-configuration-popup.component';
import {SelectListConfigurationPopupComponent} from './components/select-list/selectlist-configuration-popup.component';
import {FieldSettingsImportanceComponent} from './components/imp/field-settings-imp.component';
import {NumberConfigurationPopupComponent} from './components/number/number-configuration-popup.component';
import {FieldTypeSelectorComponent} from './components/field-type-selector/field-type-selector.component';
// Resolves
import {ModuleResolveService} from '../../shared/resolves/module.resolve.service';
import {FieldsResolveService} from './resolve/fields-resolve.service';
// Services
import {FieldSettingsService} from './service/field-settings.service';
import {CustomDomainService} from './service/custom-domain.service';
import {FieldConverterService} from './service/field-converter.service';

@NgModule({
	imports: [
		// Angular
		CommonModule,
		SharedModule,
		FormsModule,
		// Kendo
		GridModule,
		PopupModule,
		SortableModule,
		DialogModule,
		InputsModule,
		// Route
		FieldSettingsRouteModule
	],
	declarations: [
		FieldSettingsListComponent,
		FieldSettingsGridComponent,
		SelectListConfigurationPopupComponent,
		FieldSettingsImportanceComponent,
		MinMaxConfigurationPopupComponent,
		NumberConfigurationPopupComponent,
		FieldTypeSelectorComponent
	],
	providers: [
		// Resolve
		FieldsResolveService,
		// Services
		ModuleResolveService,
		FieldSettingsService,
		CustomDomainService,
		FieldConverterService
	],
	entryComponents: [
		SelectListConfigurationPopupComponent,
		MinMaxConfigurationPopupComponent,
		NumberConfigurationPopupComponent
	],
	exports: [],
})

export class FieldSettingsModule {
}