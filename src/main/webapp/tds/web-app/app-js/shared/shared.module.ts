import {NgModule, ModuleWithProviders} from '@angular/core';
import {HTTP_INTERCEPTORS} from '@angular/common/http';
import {RouterModule} from '@angular/router';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {PopupModule} from '@progress/kendo-angular-popup';
import {DropDownsModule} from '@progress/kendo-angular-dropdowns';
import {InputsModule} from '@progress/kendo-angular-inputs';
import {GridModule} from '@progress/kendo-angular-grid';
import {DateInputsModule} from '@progress/kendo-angular-dateinputs';
import {UploadModule} from '@progress/kendo-angular-upload';
import {IntlModule} from '@progress/kendo-angular-intl';
// Shared Services
import {HeaderService} from './modules/header/services/header.service';
import {PreferenceService} from '../shared/services/preference.service';
import {NotifierService} from '../shared/services/notifier.service';
import {ComponentCreatorService} from '../shared/services/component-creator.service';
import {UIDialogService, UIActiveDialogService} from '../shared/services/ui-dialog.service';
import {UILoaderService} from '../shared/services/ui-loader.service';
import {PersonService} from './services/person.service';
import {PermissionService} from './services/permission.service';
import {WindowService} from './services/window.service';
import {BulkChangeService} from './services/bulk-change.service';
import {BulkCheckboxService} from './services/bulk-checkbox.service';
import {ButtonsFactoryService} from './services/buttons-factory.service';
import {ValidationRulesFactoryService} from './services/validation-rules-factory.service';
import {ValidationRulesDefinitionsService} from './services/validation-rules-definitions.service';
import {HttpRequestInterceptor, HTTPFactory} from './providers/http-request-interceptor.provider.';
import {KendoFileUploadInterceptor, HTTPKendoFactory} from './providers/kendo-file-upload.interceptor';
import {KendoFileHandlerService} from './services/kendo-file-handler.service';
// Shared Directives
import {UIAutofocusDirective} from './directives/autofocus-directive';
import {UIHandleEscapeDirective} from './directives/handle-escape-directive';
import {UILoaderDirective} from '../shared/directives/ui-loader.directive';
import {UIToastDirective} from '../shared/directives/ui-toast.directive';
import {UIDialogDirective} from '../shared/directives/ui-dialog.directive';
import {UIPromptDirective, UIPromptService} from '../shared/directives/ui-prompt.directive';
import {UIModalDecoratorDirective} from './directives/ui-modal-decorator.directive';
import {UISVGIconDirectiveDirective} from './directives/ui-svg-icon.directive';
import {UIFloatingHeaderKGridDirective} from './directives/ui-floating-header-k-grid.directive';
import {UIAutoCenterDirective} from './directives/autocenter-directive';
import {InputPasteDirective} from './directives/input-paste.directive';
import {CopyClipboardDirective} from './directives/copy-clipboard.directive';
// Shared Pipes
import {DateTimePipe} from './pipes/datetime.pipe';
import {UIBooleanPipe} from './pipes/ui-boolean.pipe';
import {TranslatePipe} from './pipes/translate.pipe';
import {FilterPipe} from './pipes/filter.pipe';
import {UtilsPipe} from './pipes/utils.pipe';
import {DatePipe} from './pipes/date.pipe';
import {NumericPipe} from './pipes/numeric.pipe';
import {EscapeUrlEncodingPipe} from './pipes/escape-url-encoding.pipe';
// Shared Components
import {PopupLegendsComponent} from './modules/popup/legends/popup-legends.component';
import {BreadcrumbNavigationComponent} from './modules/header/components/breadcrumb-navigation/breadcrumb-navigation.component';
import {HeaderComponent} from './modules/header/components/header/header.component';
import {TranmanMenuComponent} from './modules/header/components/tranman-menu/tranman-menu.component';
import {LicenseWarningComponent} from './modules/header/components/license-warning/license-warning.component';
import {UserManageStaffComponent} from './modules/header/components/manage-staff/user-manage-staff.component';
import {UserPreferencesComponent} from './modules/header/components/preferences/user-preferences.component';
import {UserEditPersonComponent} from './modules/header/components/edit-person/user-edit-person.component';
import {PasswordChangeComponent} from './modules/header/components/password-change/password-change.component';
import {UserDateTimezoneComponent} from './modules/header/components/date-timezone/user-date-timezone.component';
import {CodeMirrorComponent} from './modules/code-mirror/code-mirror.component';
import {DynamicComponent} from './components/dynamic.component';
import {CheckActionComponent} from './components/check-action/check-action.component';
import {URLViewerComponent} from './components/url-viewer/url-viewer.component';
import {TDSComboBoxComponent} from './components/combo-box/combobox.component';
import {TDSComboBoxGroupComponent} from './components/combo-box-group/combo-box-group.component';
import {SupportsDependsComponent} from './components/supports-depends/supports-depends.component';
import {DependentCommentComponent} from './components/dependent-comment/dependent-comment.component';
import {AddPersonComponent} from './components/add-person/add-person.component';
import {DateRangeSelectorComponent} from './components/date-range-selector/date-range-selector.component';
import {AssetTagSelectorComponent} from './components/asset-tag-selector/asset-tag-selector.component';
import {AkaComponent} from './components/aka/aka.component';
import {ConnectorComponent} from './components/connector/connector.component';
import {FieldReferencePopupComponent} from './components/field-reference-popup/field-reference-popup.component';
import {TDSDateControlComponent} from './components/custom-control/date-time/date-control.component';
import {TDSDateTimeControlComponent} from './components/custom-control/date-time/datetime-control.component';
import {TDSNumberControlComponent} from './components/custom-control/number/number-control.component';
import {TDSCheckboxComponent} from './components/tds-checkbox/tds-checkbox.component';
import {BulkChangeButtonComponent} from './components/bulk-change/components/bulk-change-button/bulk-change-button.component';
import {BulkChangeActionsComponent} from './components/bulk-change/components/bulk-change-actions/bulk-change-actions.component';
import {BulkChangeEditComponent} from './components/bulk-change/components/bulk-change-edit/bulk-change-edit.component';
import {TDSActionButton} from './components/button/action-button.component';
import {TDSCustomValidationErrorsComponent} from './components/custom-control/field-validation-errors/field-validation-errors.component';
import {RichTextEditorComponent} from './modules/rich-text-editor/rich-text-editor.component';
// Dictionary
import {DictionaryService} from './services/dictionary.service';
import {en_DICTIONARY} from './i18n/en.dictionary';
import {PreferencesResolveService} from './resolves/preferences-resolve.service';

@NgModule({
	imports: [
		CommonModule,
		FormsModule,
		PopupModule,
		DropDownsModule,
		GridModule,
		UploadModule,
		DateInputsModule,
		IntlModule,
		InputsModule,
		RouterModule
	],
	declarations: [
		UIAutofocusDirective,
		UIHandleEscapeDirective,
		UILoaderDirective,
		UIToastDirective,
		UIBooleanPipe,
		DateTimePipe,
		TranslatePipe,
		FilterPipe,
		UtilsPipe,
		DatePipe,
		NumericPipe,
		EscapeUrlEncodingPipe,
		UIDialogDirective,
		BreadcrumbNavigationComponent,
		HeaderComponent,
		UserPreferencesComponent,
		UserEditPersonComponent,
		UserDateTimezoneComponent,
		UserManageStaffComponent,
		TranmanMenuComponent,
		LicenseWarningComponent,
		PopupLegendsComponent,
		UIPromptDirective,
		UISVGIconDirectiveDirective,
		UIFloatingHeaderKGridDirective,
		DynamicComponent,
		CodeMirrorComponent,
		CheckActionComponent,
		URLViewerComponent,
		TDSComboBoxComponent,
		TDSComboBoxGroupComponent,
		SupportsDependsComponent,
		DependentCommentComponent,
		UIModalDecoratorDirective,
		AddPersonComponent,
		DateRangeSelectorComponent,
		AssetTagSelectorComponent,
		UIAutoCenterDirective,
		InputPasteDirective,
		CopyClipboardDirective,
		AkaComponent,
		ConnectorComponent,
		FieldReferencePopupComponent,
		PasswordChangeComponent,
		TDSDateTimeControlComponent,
		TDSNumberControlComponent,
		TDSDateControlComponent,
		TDSCheckboxComponent,
		BulkChangeButtonComponent,
		BulkChangeActionsComponent,
		BulkChangeEditComponent,
		TDSActionButton,
		TDSCustomValidationErrorsComponent,
		RichTextEditorComponent
	],
	exports: [
		UILoaderDirective,
		UIAutofocusDirective,
		UIHandleEscapeDirective,
		UIToastDirective,
		UIDialogDirective,
		UIBooleanPipe,
		DateTimePipe,
		UISVGIconDirectiveDirective,
		UIFloatingHeaderKGridDirective,
		InputPasteDirective,
		CopyClipboardDirective,
		TranslatePipe,
		FilterPipe,
		UtilsPipe,
		DatePipe,
		NumericPipe,
		EscapeUrlEncodingPipe,
		BreadcrumbNavigationComponent,
		HeaderComponent,
		UserPreferencesComponent,
		UserEditPersonComponent,
		UserDateTimezoneComponent,
		UserManageStaffComponent,
		TranmanMenuComponent,
		LicenseWarningComponent,
		PopupLegendsComponent,
		DynamicComponent,
		CodeMirrorComponent,
		CheckActionComponent,
		URLViewerComponent,
		TDSComboBoxComponent,
		TDSComboBoxGroupComponent,
		SupportsDependsComponent,
		DependentCommentComponent,
		UIModalDecoratorDirective,
		AddPersonComponent,
		DateRangeSelectorComponent,
		AssetTagSelectorComponent,
		UIAutoCenterDirective,
		AkaComponent,
		ConnectorComponent,
		PopupModule,
		FieldReferencePopupComponent,
		PasswordChangeComponent,
		TDSDateTimeControlComponent,
		TDSNumberControlComponent,
		TDSDateControlComponent,
		TDSCheckboxComponent,
		BulkChangeButtonComponent,
		BulkChangeEditComponent,
		BulkChangeActionsComponent,
		TDSActionButton,
		TDSCustomValidationErrorsComponent,
		RichTextEditorComponent
	],
	entryComponents: [
		DynamicComponent,
		DependentCommentComponent,
		AddPersonComponent,
		DateRangeSelectorComponent,
		TDSCheckboxComponent,
		BulkChangeButtonComponent,
		BulkChangeActionsComponent,
		BulkChangeEditComponent,
		UserPreferencesComponent,
		UserEditPersonComponent,
		UserDateTimezoneComponent,
		UserManageStaffComponent,
		PasswordChangeComponent
	]
})
export class SharedModule {
	static forRoot(): ModuleWithProviders {
		return {
			ngModule: SharedModule,
			providers: [
				// Preferences
				PreferencesResolveService,
				PreferenceService,
				// Permissions
				PermissionService,
				// Dialogs
				ComponentCreatorService,
				UILoaderService,
				UIDialogService,
				UIActiveDialogService,
				// Services
				HeaderService,
				PersonService,
				NotifierService,
				KendoFileHandlerService,
				{
					provide: HTTP_INTERCEPTORS,
					useClass: HttpRequestInterceptor,
					useFactory: HTTPFactory,
					deps: [NotifierService],
					multi: true
				},
				{
					provide: HTTP_INTERCEPTORS,
					useClass: KendoFileUploadInterceptor,
					useFactory: HTTPKendoFactory,
					deps: [KendoFileHandlerService],
					multi: true
				},
				UIPromptService,
				UISVGIconDirectiveDirective,
				UIFloatingHeaderKGridDirective,
				DictionaryService,
				WindowService,
				BulkChangeService,
				BulkCheckboxService,
				TranslatePipe,
				ButtonsFactoryService,
				{
					provide: 'localizedDictionary',
					useValue: en_DICTIONARY
				},
				ValidationRulesFactoryService,
				ValidationRulesDefinitionsService
			]
		};
	}
}
