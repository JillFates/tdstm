// Angular
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
// Routing Logic
import { UIRouterModule } from '@uirouter/angular';
import { ASSET_EXPLORER_STATES } from './asset-explorer-routing.states';
// Components
import { AssetExplorerViewSelectorComponent } from './components/view-selector/asset-explorer-view-selector.component';
import { AssetExplorerViewConfigComponent } from './components/view-config/asset-explorer-view-config.component';
import { AssetExplorerViewSaveComponent } from './components/view-save/asset-explorer-view-save.component';
import { AssetExplorerViewExportComponent } from './components/view-export/asset-explorer-view-export.component';
import { AssetExplorerViewGridComponent } from './components/view-grid/asset-explorer-view-grid.component';
import { AssetExplorerViewShowComponent } from './components/view-show/asset-explorer-view-show.component';
import { AssetExplorerIndexComponent } from './components/index/asset-explorer-index.component';
import { SharedModule } from '../../shared/shared.module';
import { Permission } from '../../shared/model/permission.model';
// Import Kendo Modules
import { DropDownListModule } from '@progress/kendo-angular-dropdowns';
import { GridModule } from '@progress/kendo-angular-grid';
import { ExcelExportModule } from '@progress/kendo-angular-excel-export';
import { SortableModule } from '@progress/kendo-angular-sortable';
// Services
import { AssetExplorerService } from './service/asset-explorer.service';

@NgModule({
	imports: [
		CommonModule,
		SharedModule,
		FormsModule,
		DropDownListModule,
		GridModule,
		ExcelExportModule,
		SortableModule,
		UIRouterModule.forChild({ states: ASSET_EXPLORER_STATES })
	],
	declarations: [
		AssetExplorerViewSelectorComponent,
		AssetExplorerIndexComponent,
		AssetExplorerViewConfigComponent,
		AssetExplorerViewSaveComponent,
		AssetExplorerViewExportComponent,
		AssetExplorerViewGridComponent,
		AssetExplorerViewShowComponent
	],
	providers: [AssetExplorerService],
	exports: [AssetExplorerIndexComponent],
	entryComponents: [
		AssetExplorerViewSaveComponent,
		AssetExplorerViewExportComponent
	]
})

export class AssetExplorerModule {
}