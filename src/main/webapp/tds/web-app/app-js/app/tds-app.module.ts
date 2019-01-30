/**
 * TDS App
 */
// Angular
import {BrowserModule} from '@angular/platform-browser';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {NgModule, NgModuleFactoryLoader, SystemJsNgModuleLoader, APP_INITIALIZER} from '@angular/core';
import {HttpModule} from '@angular/http';
import {TDSAppComponent} from './tds-app.component';
// Service
import {AuthGuardService} from '../modules/security/services/auth.guard.service';
import {AppSettingsService} from '../modules/security/services/app-settings.service';
// Root Basic modules
import {TDSAppRouteModule} from './tds-routing.states';
import {SharedModule} from '../shared/shared.module';
// Feature modules
import {TaskManagerModule} from '../modules/taskManager/task-manager.module';
import {UserModule} from '../modules/user/user.module';

@NgModule({
	imports: [
		// Angular Modules
		BrowserModule,
		HttpModule,
		BrowserAnimationsModule,
		TDSAppRouteModule,
		TaskManagerModule,
		UserModule,
		SharedModule.forRoot()
	],
	declarations: [
		TDSAppComponent,
	],
	providers: [
		AuthGuardService,
		AppSettingsService,
		{ provide: NgModuleFactoryLoader, useClass: SystemJsNgModuleLoader },
		{
			provide: APP_INITIALIZER,
			useFactory: (provider: AppSettingsService) => () => provider.initializeAppSettings(),
			deps: [AppSettingsService],
			multi: true
		}
	],
	bootstrap: [
		TDSAppComponent
	]
})

export class TDSAppModule {
}