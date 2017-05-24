/**
 * App or Root Module
 * it identify how the TDS App is being constructed
 */
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { NgModule, NgModuleFactoryLoader, SystemJsNgModuleLoader } from '@angular/core';
import { HttpModule, Http } from '@angular/http';

// import {TranslateModule, TranslateLoader, TranslateStaticLoader} from 'ng2-translate';

import { TDSAppComponent } from './tds-app.component';
// Feature modules
import { SharedModule } from '../shared/shared.module';
import { NoticesManagerModule } from '../modules/noticeManager/notice-manager.module';
import { TaskManagerModule } from '../modules/taskManager/task-manager.module';
import { FieldSettingsModule } from '../modules/fieldSettings/field-settings.module';
// Router Logic
import { UIRouterModule, UIView } from '@uirouter/angular';
import { TDSRoutingStates, AuthConfig } from './tds-routing.states';

// Decorator that tells to Angular is a module.
@NgModule({
	imports: [
		// Angular Modules
		BrowserModule,
		HttpModule,
		BrowserAnimationsModule,
		// Feature Modules
		SharedModule,
		NoticesManagerModule,
		TaskManagerModule,
		FieldSettingsModule,
		// Translator
		// TranslateModule.forRoot({
		//     provide: TranslateLoader,
		//     useFactory: (http: Http) => new TranslateStaticLoader(http, '../tds/web-app/i18n', '.json'),
		//     deps: [Http]
		// }),
		// Routing Modules using UI Router
		UIRouterModule.forRoot(<UIRouterModule>{
			states: TDSRoutingStates,
			config: AuthConfig,
		}),
	],
	declarations: [
		TDSAppComponent,
	], // components, directives and pipes ONLY and only ONCE\
	providers: [
		{ provide: NgModuleFactoryLoader, useClass: SystemJsNgModuleLoader }
	],
	bootstrap: [UIView]
})

export class TDSAppModule {
}