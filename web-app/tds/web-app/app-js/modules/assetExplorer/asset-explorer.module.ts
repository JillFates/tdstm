// Angular
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
// Routing Logic
import { UIRouterModule } from '@uirouter/angular';
import { ASSET_EXPLORER_STATES } from './asset-explorer-routing.states';
// Components
import { AssetExplorerReportSelectorComponent } from './components/report-selector/asset-explorer-report-selector.component';
import { SharedModule } from '../../shared/shared.module';
// Import Kendo Modules
import { DropDownListModule } from '@progress/kendo-angular-dropdowns';
// Services
import { AssetExplorerService } from './service/asset-explorer.service';

@NgModule({
	imports: [
		CommonModule,
		SharedModule,
		FormsModule,
		DropDownListModule,
		UIRouterModule.forChild({ states: ASSET_EXPLORER_STATES })
	],
	declarations: [
		AssetExplorerReportSelectorComponent
	],
	providers: [AssetExplorerService],
	exports: [AssetExplorerReportSelectorComponent]
})

export class AssetExplorerModule {
}