/**
 * App or Root Module
 * it identify how the TDS App is being constructed
 */
import {BrowserModule} from '@angular/platform-browser';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {NgModule, NgModuleFactoryLoader, SystemJsNgModuleLoader} from '@angular/core';
import {HttpModule, Http} from '@angular/http';
import {TDSAppComponent} from './tds-app.component';
// Feature modules
// import { FieldSettingsModule } from '../modules/fieldSettings/field-settings.module';
// import { AssetExplorerModule } from '../modules/assetExplorer/asset-explorer.module';
// import { DataIngestionModule} from '../modules/dataIngestion/data-ingestion.module';
// import {AssetTagsModule} from '../modules/assetTags/asset-tags.module';
// import {ImportBatchModule} from '../modules/importBatch/import-batch.module';
// Router Logic
// import { UIRouterModule, UIView } from '@uirouter/angular';
// import { TdsAppRoute, AuthConfig, PermissionConfig, MiscConfig } from './tds-app.route';
// Hight level Services
import {PermissionService} from '../shared/services/permission.service';
import {AuthGuardService} from '../modules/security/services/auth.guard.service';
// Root Basic modules
import {TDSAppRouteModule} from './tds-app-route.module';
import {SharedModule} from '../shared/shared.module';
// Feature modules
import {TaskManagerModule} from '../modules/taskManager/task-manager.module';

// Decorator that tells to Angular is a module.
@NgModule({
	imports: [
		// Angular Modules
		BrowserModule,
		HttpModule,
		BrowserAnimationsModule,
		TDSAppRouteModule,
		SharedModule,
		TaskManagerModule,
		// Feature Modules
		// TaskManagerModule,
		// FieldSettingsModule,
		// AssetExplorerModule,
		// DataIngestionModule,
		// ImportBatchModule,
		// AssetTagsModule,
		// Routing Modules using UI Router
		/* UIRouterModule.forRoot(<UIRouterModule>{
			states: TdsAppRoute,
			otherwise: '/pages/notfound',
			config: (route) => {
				AuthConfig(route);
				PermissionConfig(route);
				MiscConfig(route);
			},
		}), */
	],
	declarations: [
		TDSAppComponent,
	],
	providers: [
		AuthGuardService,
		PermissionService,
		// { provide: NgModuleFactoryLoader, useClass: SystemJsNgModuleLoader }
	],
	bootstrap: [
		TDSAppComponent
	]
})

export class TDSAppModule {
}