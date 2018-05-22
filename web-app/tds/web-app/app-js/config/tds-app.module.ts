/**
 * App or Root Module
 * it identify how the TDS App is being constructed
 */
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { NgModule, NgModuleFactoryLoader, SystemJsNgModuleLoader } from '@angular/core';
import { HttpModule, Http } from '@angular/http';
import { TDSAppComponent } from './tds-app.component';
// Feature modules
import { SharedModule } from '../shared/shared.module';
import { FieldSettingsModule } from '../modules/fieldSettings/field-settings.module';
import { AssetExplorerModule } from '../modules/assetExplorer/asset-explorer.module';
import { DataIngestionModule} from '../modules/dataIngestion/data-ingestion.module';
import { ImportAssetsModule } from '../modules/importAssets/import-assets.module';
import {DependencyBatchModule} from '../modules/dependencyBatch/dependency-batch.module';
// Router Logic
import { UIRouterModule, UIView } from '@uirouter/angular';
import { TDSRoutingStates, AuthConfig, PermissionConfig, MiscConfig } from './tds-routing.states';

// Decorator that tells to Angular is a module.
@NgModule({
	imports: [
		// Angular Modules
		BrowserModule,
		HttpModule,
		BrowserAnimationsModule,
		// Feature Modules
		SharedModule,
		FieldSettingsModule,
		AssetExplorerModule,
		DataIngestionModule,
		ImportAssetsModule,
		DependencyBatchModule,
		// Routing Modules using UI Router
		UIRouterModule.forRoot(<UIRouterModule>{
			states: TDSRoutingStates,
			otherwise: '/pages/notfound',
			config: (route) => {
				AuthConfig(route);
				PermissionConfig(route);
				MiscConfig(route);
			},
		}),
	],
	declarations: [
		TDSAppComponent,
	], // components, directives and pipes ONLY and only ONCE
	providers: [
		{ provide: NgModuleFactoryLoader, useClass: SystemJsNgModuleLoader }
	],
	bootstrap: [UIView]
})

export class TDSAppModule {
}