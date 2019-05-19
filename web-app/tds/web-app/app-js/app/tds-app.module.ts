/**
 * TDS App
 */
import {BrowserModule} from '@angular/platform-browser';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {NgModule, NgModuleFactoryLoader, SystemJsNgModuleLoader} from '@angular/core';
import {HttpModule} from '@angular/http';
import {TDSAppComponent} from './tds-app.component';
// Service
import {AuthGuardService} from '../modules/security/services/auth.guard.service';
// Root Basic modules
import {TDSAppRouteModule} from './tds-routing.states';
import {SharedModule} from '../shared/shared.module';
// Feature modules
import {TaskManagerModule} from '../modules/taskManager/task-manager.module';
import {UserModule} from '../modules/user/user.module';
import {MandatoryNoticesValidatorService} from './services/mandatory-notices-validator.service';

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
		{ provide: NgModuleFactoryLoader, useClass: SystemJsNgModuleLoader },
		MandatoryNoticesValidatorService
	],
	bootstrap: [
		TDSAppComponent
	]
})

export class TDSAppModule {
}