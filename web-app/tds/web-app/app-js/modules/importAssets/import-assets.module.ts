// Angular
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {BrowserModule} from '@angular/platform-browser';
// Routing Logic
import { UIRouterModule } from '@uirouter/angular';
import { IMPORT_ASSETS_STATES } from './import-assets-routing.states';
// Component
import { SharedModule } from '../../shared/shared.module';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { LayoutModule } from '@progress/kendo-angular-layout';
import { ManualImportComponent } from './components/manual-import/manual-import.component';
// Services
import { ImportAssetsService } from './service/import-assets.service';
import {UploadModule} from '@progress/kendo-angular-upload';
import {HTTP_INTERCEPTORS} from '@angular/common/http';
import {FileUploadInterceptor} from '../dataIngestion/components/data-script-sample-data/file-upload.interceptor';

@NgModule({
	imports: [
		CommonModule,
		SharedModule,
		FormsModule,
		BrowserModule,
		BrowserAnimationsModule,
		LayoutModule,
		UploadModule,
		UIRouterModule.forChild({ states: IMPORT_ASSETS_STATES }),
	],
	declarations: [
		ManualImportComponent
	],
	providers: [
		ImportAssetsService,
		{
			provide: HTTP_INTERCEPTORS,
			useClass: FileUploadInterceptor,
			multi: true
		}
	],
	exports: [
		ManualImportComponent
	]
})

export class ImportAssetsModule {
}