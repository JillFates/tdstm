/**
 * TDS App
 */
// Angular
import {BrowserModule} from '@angular/platform-browser';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {NgModule, NgModuleFactoryLoader, SystemJsNgModuleLoader, APP_INITIALIZER} from '@angular/core';
import {HttpClientModule} from '@angular/common/http';
import {TDSAppComponent} from './tds-app.component';
// Service
import {UserContextService} from '../modules/auth/service/user-context.service';
// Root Basic modules
import {TDSAppRouteModule} from './tds-routing.states';
import {SharedModule} from '../shared/shared.module';
// Feature modules
import {TaskManagerModule} from '../modules/taskManager/task-manager.module';
import {AuthModule} from '../modules/auth/auth.module';

@NgModule({
	imports: [
		// Angular Modules
		BrowserModule,
		HttpClientModule,
		BrowserAnimationsModule,
		TDSAppRouteModule,
		AuthModule,
		TaskManagerModule,
		SharedModule.forRoot()
	],
	declarations: [
		TDSAppComponent,
	],
	providers: [
		{ provide: NgModuleFactoryLoader, useClass: SystemJsNgModuleLoader },
		{
			provide: APP_INITIALIZER,
			useFactory: (provider: UserContextService) => () => provider.initializeUserContext(),
			deps: [UserContextService],
			multi: true
		}
	],
	bootstrap: [
		TDSAppComponent
	]
})

export class TDSAppModule {
}