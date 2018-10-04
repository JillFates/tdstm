import { NgModule, ModuleWithProviders } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PopupModule } from '@progress/kendo-angular-popup';
import { DropDownsModule } from '@progress/kendo-angular-dropdowns';
import { InputsModule } from '@progress/kendo-angular-inputs';
import { GridModule } from '@progress/kendo-angular-grid';
// TODO: REFACTOR TO USE NEW ANGULAR 6 INTERCEPTORS
import { HttpServiceProvider } from '../shared/providers/http-interceptor.provider';
// Shared Services
import { PreferenceService } from '../shared/services/preference.service';
import { NotifierService } from '../shared/services/notifier.service';
import { ComponentCreatorService } from '../shared/services/component-creator.service';
import { UIDialogService, UIActiveDialogService } from '../shared/services/ui-dialog.service';
import { UILoaderService } from '../shared/services/ui-loader.service';
import { PersonService } from './services/person.service';
import { PermissionService } from './services/permission.service';
// Shared Directives
import { UIAutofocusDirective } from './directives/autofocus-directive';
import { UIHandleEscapeDirective } from './directives/handle-escape-directive';
import { UILoaderDirective } from '../shared/directives/ui-loader.directive';
import { UIToastDirective } from '../shared/directives/ui-toast.directive';
import { UIDialogDirective } from '../shared/directives/ui-dialog.directive';
import { UIPromptDirective, UIPromptService } from '../shared/directives/ui-prompt.directive';
import { UIModalDecoratorDirective} from './directives/ui-modal-decorator.directive';
import { UISVGIconDirectiveDirective } from './directives/ui-svg-icon.directive';
import { UIFloatingHeaderKGridDirective} from './directives/ui-floating-header-k-grid.directive';
import {UIAutoCenterDirective} from './directives/autocenter-directive';
// Shared Pipes
import { UserDateTime } from './pipes/userDateTime.pipe';
import { UIBooleanPipe } from './pipes/ui-boolean.pipe';
import { TranslatePipe } from './pipes/translate.pipe';
import { FilterPipe } from './pipes/filter.pipe';
import { UtilsPipe } from './pipes/utils.pipe';
// Shared Components
import { PopupLegendsComponent } from './modules/popup/legends/popup-legends.component';
import { HeaderComponent } from './modules/header/header.component';
import { CodeMirrorComponent } from './modules/code-mirror/code-mirror.component';
import { DynamicComponent } from './components/dynamic.component';
import { CheckActionComponent } from './components/check-action/check-action.component';
import { URLViewerComponent } from './components/url-viewer/url-viewer.component';
import { TDSComboBoxComponent} from './components/combo-box/combobox.component';
import { TDSComboBoxGroupComponent} from './components/combo-box-group/combo-box-group.component';
import { SupportsDependsComponent } from './components/supports-depends/supports-depends.component';
import { DependentCommentComponent } from './components/dependent-comment/dependent-comment.component';
import { AddPersonComponent } from './components/add-person/add-person.component';
import { AssetTagSelectorComponent } from './components/asset-tag-selector/asset-tag-selector.component';
import { AkaComponent } from './components/aka/aka.component';
import { ConnectorComponent } from './components/connector/connector.component';
// Dictionary
import { DictionaryService } from './services/dictionary.service';
import { en_DICTIONARY } from './i18n/en.dictionary';

@NgModule({
	imports: [
		CommonModule,
		FormsModule,
		PopupModule,
		DropDownsModule,
		GridModule,
		InputsModule
	],
	declarations: [
		UIAutofocusDirective,
		UIHandleEscapeDirective,
		UILoaderDirective,
		UIToastDirective,
		UIBooleanPipe,
		UserDateTime,
		TranslatePipe,
		FilterPipe,
		UtilsPipe,
		UIDialogDirective,
		HeaderComponent,
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
		AssetTagSelectorComponent,
		UIAutoCenterDirective,
		AkaComponent,
		ConnectorComponent
	],
	exports: [
		UILoaderDirective,
		UIAutofocusDirective,
		UIHandleEscapeDirective,
		UIToastDirective,
		UIDialogDirective,
		UIBooleanPipe,
		UserDateTime,
		UISVGIconDirectiveDirective,
		UIFloatingHeaderKGridDirective,
		TranslatePipe,
		FilterPipe,
		UtilsPipe,
		HeaderComponent,
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
		AssetTagSelectorComponent,
		UIAutoCenterDirective,
		AkaComponent,
		ConnectorComponent,
		PopupModule
	],
	entryComponents: [
		DynamicComponent,
		DependentCommentComponent,
		AddPersonComponent
	]
})
export class SharedModule {
	static forRoot(): ModuleWithProviders {
		return {
			ngModule: SharedModule,
			providers: [
				PermissionService,
				PersonService,
				PreferenceService,
				NotifierService,
				UILoaderService,
				HttpServiceProvider,
				ComponentCreatorService,
				UIDialogService,
				UIActiveDialogService,
				UIPromptService,
				UISVGIconDirectiveDirective,
				UIFloatingHeaderKGridDirective,
				DictionaryService,
				{ provide: 'localizedDictionary', useValue: en_DICTIONARY }
			]
		};
	}
}
