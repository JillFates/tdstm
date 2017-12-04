import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PopupModule } from '@progress/kendo-angular-popup';
import { HttpServiceProvider } from '../shared/providers/http-interceptor.provider';
// Shared Services
import { AuthService } from '../shared/services/auth.service';
import { PermissionService } from '../shared/services/permission.service';
import { PreferenceService } from '../shared/services/preference.service';
import { UserService } from '../shared/services/user.service';
import { NotifierService } from '../shared/services/notifier.service';
import { ComponentCreatorService } from '../shared/services/component-creator.service';
import { UIDialogService, UIActiveDialogService } from '../shared/services/ui-dialog.service';
import { UILoaderService } from '../shared/services/ui-loader.service';
// Shared Directives
import { UILoaderDirective } from '../shared/directives/ui-loader.directive';
import { UIToastDirective } from '../shared/directives/ui-toast.directive';
import { UIDialogDirective } from '../shared/directives/ui-dialog.directive';
import { UIPromptDirective, UIPromptService } from '../shared/directives/ui-prompt.directive';
import { UISVGIconDirectiveDirective } from './directives/ui-svg-icon.directive';
// Shared Pipes
import { UIBooleanPipe } from './pipes/ui-boolean.pipe';
import { TranslatePipe } from './pipes/translate.pipe';
import { FilterPipe } from './pipes/filter.pipe';
// Shared Components
import { PopupLegendsComponent } from './modules/popup/legends/popup-legends.component';
import { HeaderComponent } from './modules/header/header.component';
import { FormlyInputHorizontal } from './modules/formly/formly-input-horizontal.component';
import { CodeMirrorComponent } from './modules/code-mirror/code-mirror.component';
// Dictionaries
import { en_DICTIONARY } from './i18n/en.dictionary';
// Pages
import { ErrorPageComponent } from './modules/pages/error-page.component';
import { UnauthorizedPageComponent } from './modules/pages/unauthorized-page.component';
import { NotFoundPageComponent } from './modules/pages/not-found-page.component';
// Routing Logic
import { UIRouterModule } from '@uirouter/angular';
import { SHARED_STATES } from './shared-routing.states';
// Components
import { DynamicComponent } from './components/dynamic.component';

@NgModule({
	imports: [
		CommonModule,
		PopupModule,
		UIRouterModule.forChild({ states: SHARED_STATES })
	],
	declarations: [
		UILoaderDirective,
		UIToastDirective,
		UIBooleanPipe,
		TranslatePipe,
		FilterPipe,
		UIDialogDirective,
		HeaderComponent,
		PopupLegendsComponent,
		FormlyInputHorizontal,
		UIPromptDirective,
		UISVGIconDirectiveDirective,
		ErrorPageComponent,
		NotFoundPageComponent,
		UnauthorizedPageComponent,
		DynamicComponent,
		CodeMirrorComponent
	],
	providers: [
		AuthService,
		PermissionService,
		PreferenceService,
		UserService,
		NotifierService,
		UILoaderService,
		HttpServiceProvider,
		ComponentCreatorService,
		UIDialogService,
		UIActiveDialogService,
		UIPromptService,
		UISVGIconDirectiveDirective,
		{ provide: 'localizedDictionary', useValue: en_DICTIONARY }
	],
	exports: [UILoaderDirective,
		UIToastDirective,
		UIDialogDirective,
		UIBooleanPipe,
		UISVGIconDirectiveDirective,
		TranslatePipe,
		FilterPipe,
		HeaderComponent,
		PopupLegendsComponent,
		FormlyInputHorizontal,
		DynamicComponent,
		CodeMirrorComponent
	],
	entryComponents: [DynamicComponent]
})
export class SharedModule {
	constructor(private notifier: NotifierService) {
	}
}
