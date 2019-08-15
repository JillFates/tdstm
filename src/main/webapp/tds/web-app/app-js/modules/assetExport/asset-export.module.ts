// Angular
import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
// Shared
import {SharedModule} from '../../shared/shared.module';
// Kendo
import {DropDownsModule} from '@progress/kendo-angular-dropdowns';
import {DialogModule} from '@progress/kendo-angular-dialog';
// Route Module
import {AssetExportRouteModule} from './asset-export-route.module';
// Services
import {ExportAssetService} from './service/export-asset.service'
// Components
import {ExportComponent} from './components/export/export.component';

@NgModule({
	imports: [
		// Angular
		CommonModule,
		SharedModule,
		FormsModule,
		// Kendo
		DropDownsModule,
		DialogModule,
		// Route
		AssetExportRouteModule
	],
	providers: [ExportAssetService],
	declarations: [
		ExportComponent,
	]
})
export class AssetExportModule {
}
