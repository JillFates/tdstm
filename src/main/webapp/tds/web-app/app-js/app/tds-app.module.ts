/**
 * TDS App
 */
// Angular
import {BrowserModule} from '@angular/platform-browser';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {NgModule, NgModuleFactoryLoader, SystemJsNgModuleLoader, APP_INITIALIZER} from '@angular/core';
import {HttpClientModule} from '@angular/common/http';
import {TDSAppComponent} from './tds-app.component';
// Ngxs
import {NgxsModule} from '@ngxs/store';
import {TDSAppState} from './state/tds-app.state';
import {NgxsReduxDevtoolsPluginModule} from '@ngxs/devtools-plugin';
import {NgxsLoggerPluginModule} from '@ngxs/logger-plugin';
// Root Basic modules
import {TDSAppRouteModule} from './tds-routing.states';
import {SharedModule} from '../shared/shared.module';
// Feature modules
import {TaskManagerModule} from '../modules/taskManager/task-manager.module';
import {AuthModule} from '../modules/auth/auth.module';
import {UserContextState} from '../modules/auth/state/user-context.state';
import { ProjectService } from '../modules/project/service/project.service';
import {environment} from '../environment/environment';

@NgModule({
	imports: [
	NgxsModule.forRoot([TDSAppState, UserContextState], { developmentMode: !environment.production }),
		NgxsReduxDevtoolsPluginModule.forRoot(),
		NgxsLoggerPluginModule.forRoot({disabled: true}),
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
		ProjectService,
		{ provide: NgModuleFactoryLoader, useClass: SystemJsNgModuleLoader }
	],
	bootstrap: [
		TDSAppComponent
	]
})

export class TDSAppModule {
}
