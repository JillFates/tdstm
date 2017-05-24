// Angular
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
// Routing Logic
import { UIRouterModule } from '@uirouter/angular';
import { FIELD_SETTINGS_STATES } from './field-settings.states';
// Components
import { FieldSettingsListComponent } from './components/list/field-settings-list.component';
import { SharedModule } from '../../shared/shared.module';
// Services
import { FieldSettingsService } from './service/field-settings.service';

@NgModule({
	imports: [
		CommonModule,
		SharedModule,
		UIRouterModule.forChild({ states: FIELD_SETTINGS_STATES })
	],
	declarations: [FieldSettingsListComponent],
	providers: [FieldSettingsService],
	exports: [FieldSettingsListComponent]
})

export class FieldSettingsModule {
}