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
import {AssetManagerRouteModule} from './asset-manager.route';
// Resolves
import {ModuleResolveService} from '../../shared/resolves/module.resolve.service';
import {FieldsResolveService} from './resolve/fields-resolve.service';
import {ReportsResolveService} from './resolve/reports-resolve.service';
import {ReportResolveService} from './resolve/report-resolve.service';
import {TagsResolveService} from './resolve/tags-resolve.service';
// Services
import {AssetExplorerService} from './service/asset-explorer.service';
import {TagService} from '../assetTags/service/tag.service';
import {CustomDomainService} from '../fieldSettings/service/custom-domain.service';
import {AssetGlobalFiltersService} from './service/asset-global-filters.service';
// Components
import {AssetViewSelectorComponent} from './components/asset-view-selector/asset-view-selector.component';
import {AssetViewConfigComponent} from './components/asset-view-config/asset-view-config.component';
import {AssetViewSaveComponent} from './components/asset-view-save/asset-view-save.component';
import {AssetViewExportComponent} from './components/asset-view-export/asset-view-export.component';
import {AssetViewGridComponent} from './components/asset-view-grid/asset-view-grid.component';
import {AssetViewShowComponent} from './components/asset-view-show/asset-view-show.component';
import {AssetViewManagerComponent} from './components/asset-view-manager/asset-view-manager.component';
import {ArchitectureGraphComponent} from './components/architecture-graph/architecture-graph.component';
// import {ExportComponent} from './components/asset-export/export.component';
// import {ExportAssetService} from './service/export-asset.service';
import { AssetViewShowSaveButtonComponent } from './components/asset-view-show/asset-view-show-save-button.component';
import { AssetViewShowCreateButtonComponent } from './components/asset-view-show/asset-view-show-create-button.component';
import { AssetViewConfigSaveButtonComponent } from './components/asset-view-config/asset-view-config-save-button.component';
import { TabStripModule } from '@progress/kendo-angular-layout';
import {AssetCommentModule} from '../assetComment/asset-comment.module';

@NgModule({
	imports: [
		// Angular
		CommonModule,
		SharedModule,
		FormsModule,
		AssetExplorerModule,
		AssetCommentModule,
		// Kendo
		DropDownListModule,
		GridModule,
		ExcelExportModule,
		SortableModule,
		IntlModule,
		DateInputsModule,
		TabStripModule,
		// Route
		AssetManagerRouteModule
	],
	declarations: [
		AssetViewSelectorComponent,
		AssetViewManagerComponent,
		AssetViewConfigComponent,
		AssetViewConfigSaveButtonComponent,
		AssetViewSaveComponent,
		AssetViewExportComponent,
		AssetViewGridComponent,
		AssetViewShowComponent,
		AssetViewShowSaveButtonComponent,
		AssetViewShowCreateButtonComponent,
		ArchitectureGraphComponent
	],
	providers: [
		// Resolve
		ModuleResolveService,
		FieldsResolveService,
		ReportResolveService,
		ReportsResolveService,
		TagsResolveService,
		// Services
		CustomDomainService,
		AssetExplorerService,
		TagService,
		// ExportAssetService,
		AssetGlobalFiltersService
	],
	exports: [],
	entryComponents: [
		AssetViewSaveComponent,
		AssetViewExportComponent
	],
})

export class AssetManagerModule {
}
