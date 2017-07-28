// Angular
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
// Routing Logic
import { UIRouterModule } from '@uirouter/angular';
import { ASSET_EXPLORER_STATES } from './asset-explorer-routing.states';
// Components
import { AssetExplorerReportSelectorComponent } from './components/report-selector/asset-explorer-report-selector.component';
import { AssetExplorerReportConfigComponent } from './components/report-config/asset-explorer-report-config.component';
import { AssetExplorerReportSaveComponent } from './components/report-save/asset-explorer-report-save.component';
import { AssetExplorerReportExportComponent } from './components/report-export/asset-explorer-report-export.component';
import { AssetExplorerReportGridComponent } from './components/report-grid/asset-explorer-report-grid.component';
import { AssetExplorerIndexComponent } from './components/index/asset-explorer-index.component';
import { SharedModule } from '../../shared/shared.module';
// Import Kendo Modules
import { DropDownListModule } from '@progress/kendo-angular-dropdowns';
import { GridModule } from '@progress/kendo-angular-grid';
// Services
import { AssetExplorerService } from './service/asset-explorer.service';

@NgModule({
	imports: [
		CommonModule,
		SharedModule,
		FormsModule,
		DropDownListModule,
		GridModule,
		UIRouterModule.forChild({ states: ASSET_EXPLORER_STATES })
	],
	declarations: [
		AssetExplorerReportSelectorComponent,
		AssetExplorerIndexComponent,
		AssetExplorerReportConfigComponent,
		AssetExplorerReportSaveComponent,
		AssetExplorerReportExportComponent,
		AssetExplorerReportGridComponent
	],
	providers: [AssetExplorerService],
	exports: [AssetExplorerIndexComponent],
	entryComponents: [
		AssetExplorerReportSaveComponent,
		AssetExplorerReportExportComponent
	]
})

export class AssetExplorerModule {
}