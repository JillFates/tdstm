// Angular
import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
// Shared
import {SharedModule} from '../../shared/shared.module';
// Route Module
import {CredentialRouteModule} from './credential-routing.states';
// Kendo
import {DropDownsModule} from '@progress/kendo-angular-dropdowns';
import {GridModule} from '@progress/kendo-angular-grid';
import {PopupModule} from '@progress/kendo-angular-popup';
import {InputsModule} from '@progress/kendo-angular-inputs';
import {DateInputsModule} from '@progress/kendo-angular-dateinputs';
// Components
import {CredentialListComponent} from './components/list/credential-list.component';
import {CredentialViewEditComponent} from './components/view-edit/credential-view-edit.component';
import {PopupValidateExpressionComponent} from './popups/popup-validate-expression.component';
import {PopupSessionAuthenticationNameComponent} from './popups/popup-session-authentication-name.component';
// Resolves
import {ModuleResolveService} from '../../shared/resolves/module.resolve.service';
import {CredentialResolveService} from './resolve/credential-resolve.service';
// Services
import {CredentialService} from './service/credential.service';

@NgModule({
	imports: [
		// Angular
		CommonModule,
		SharedModule,
		FormsModule,
		// Kendo
		DropDownsModule,
		GridModule,
		PopupModule,
		InputsModule,
		DateInputsModule,
		// Route
		CredentialRouteModule
	],
	declarations: [
		CredentialListComponent,
		CredentialViewEditComponent,
		PopupValidateExpressionComponent,
		PopupSessionAuthenticationNameComponent
	],
	providers: [
		// Resolve
		ModuleResolveService,
		CredentialResolveService,
		// Service
		CredentialService
	],
	exports: [
		CredentialListComponent,
		CredentialViewEditComponent,
	],
	entryComponents: [
		CredentialViewEditComponent
	]
})

export class CredentialModule {
}