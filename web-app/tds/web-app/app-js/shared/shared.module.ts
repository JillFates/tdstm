import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PopupModule } from '@progress/kendo-angular-popup';
// import { TranslateModule } from 'ng2-translate';
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
import { OnlyNumbersValidator } from './validators/only-numbers-validator.directive';
// Shared Pipes
import { UIBooleanPipe } from './pipes/types/ui-boolean.pipe';
import { TranslatePipe } from './pipes/translate.pipe';
// Shared Components
import { PopupLegendsComponent } from './modules/popup/legends/popup-legends.component';
import { HeaderComponent } from './modules/header/header.component';
import { FormlyInputHorizontal } from './modules/formly/formly-input-horizontal.component';
import { RichTextEditorComponent } from './modules/rich-text-editor/rich-text-editor.component';
// Dictionaries
import { en_DICTIONARY } from './i18n/en.dictionary';

@NgModule({
	imports: [
		CommonModule,
		PopupModule,
		// TranslateModule
	],
	declarations: [
		UILoaderDirective,
		UIToastDirective,
		UIBooleanPipe,
		TranslatePipe,
		UIDialogDirective,
		HeaderComponent,
		PopupLegendsComponent,
		FormlyInputHorizontal,
		RichTextEditorComponent,
		UIPromptDirective,
		OnlyNumbersValidator
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
		{ provide: 'localizedDictionary', useValue: en_DICTIONARY }
	],
	exports: [UILoaderDirective,
		UIToastDirective,
		UIDialogDirective,
		UIBooleanPipe,
		TranslatePipe,
		HeaderComponent,
		PopupLegendsComponent,
		FormlyInputHorizontal,
		RichTextEditorComponent,
		OnlyNumbersValidator
		// TranslateModule
	]
})
export class SharedModule {
	constructor(private notifier: NotifierService) { }
}