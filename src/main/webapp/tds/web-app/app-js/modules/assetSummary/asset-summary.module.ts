// Angular
import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
// Shared
import {SharedModule} from '../../shared/shared.module';
// Kendo
import {DropDownListModule} from '@progress/kendo-angular-dropdowns';
import {GridModule} from '@progress/kendo-angular-grid';
import {ExcelExportModule} from '@progress/kendo-angular-excel-export';
import {SortableModule} from '@progress/kendo-angular-sortable';
import {IntlModule} from '@progress/kendo-angular-intl';
import {DateInputsModule} from '@progress/kendo-angular-dateinputs';
// Assets Module
import {AssetExplorerModule} from '../assetExplorer/asset-explorer.module';
// Route Module
import {AssetSummaryRouteModule} from './asset-summary.route';
// Resolves
import {ModuleResolveService} from '../../shared/resolves/module.resolve.service';
// Services
import {AssetSummaryService} from './service/asset-summary.service';
// Components
import {AssetSummaryListComponent} from './components/list/asset-summary-list.component';

@NgModule({
	imports: [
		// Angular
		CommonModule,
		SharedModule,
		FormsModule,
		AssetExplorerModule,
		// Kendo
		DropDownListModule,
		GridModule,
		ExcelExportModule,
		SortableModule,
		IntlModule,
		DateInputsModule,
		// Route
		AssetSummaryRouteModule
	],
	declarations: [
		AssetSummaryListComponent
	],
	providers: [
		// Resolve
		ModuleResolveService,
		// Services
		AssetSummaryService
	]
})

export class AssetSummaryModule {
}
