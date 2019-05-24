/**
 * TDS App
 */
import {BrowserModule} from '@angular/platform-browser';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {NgModule, NgModuleFactoryLoader, SystemJsNgModuleLoader, APP_INITIALIZER} from '@angular/core';
import {HttpModule} from '@angular/http';
import {TDSAppComponent} from './tds-app.component';
// Service
import {AuthGuardService} from '../modules/security/services/auth.guard.service';
import {UserPostNoticesContextService} from '../modules/user/service/user-post-notices-context.service';
import {NoticesValidatorService} from '../modules/user/service/notices-validator.service';
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
		UserPostNoticesContextService,
		{ provide: NgModuleFactoryLoader, useClass: SystemJsNgModuleLoader },
		NoticesValidatorService,
		{
			provide: APP_INITIALIZER,
			useFactory: (provider: UserPostNoticesContextService) => () => provider.initializeUserPostNoticesContext(),
			deps: [UserPostNoticesContextService],
			multi: true
		}
	],
	bootstrap: [
		TDSAppComponent
	]
})

export class TDSAppModule {
}