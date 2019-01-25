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
// Route Module
import {AssetManagerRouteModule} from './asset-manager-route';
// Resolves
import {ModuleResolveService} from '../../shared/resolves/module.resolve.service';
// import {FieldsResolveService} from './resolve/fields-resolve.service';
import {ReportsResolveService} from './resolve/reports-resolve.service';
// import {ReportResolveService} from './resolve/report-resolve.service';
// import {TagsResolveService} from './resolve/tags-resolve.service';
// Services
import { AssetExplorerService } from './service/asset-explorer.service';
// import { TaskCommentService } from './service/task-comment.service';
// import { DependecyService } from './service/dependecy.service';
// import { TagService } from '../assetTags/service/tag.service';
// import { ManufacturerService } from './service/manufacturer.service';
// import { ModelService } from  './service/model.service';
import {CustomDomainService} from '../fieldSettings/service/custom-domain.service';
// Components
// import {AssetExplorerViewSelectorComponent} from './components/view-selector/asset-explorer-view-selector.component';
// import {AssetExplorerViewConfigComponent} from './components/view-config/asset-explorer-view-config.component';
// import {AssetExplorerViewSaveComponent} from './components/view-save/asset-explorer-view-save.component';
// import {AssetExplorerViewExportComponent} from './components/view-export/asset-explorer-view-export.component';
// import {AssetExplorerViewGridComponent} from './components/view-grid/asset-explorer-view-grid.component';
// import {AssetExplorerViewShowComponent} from './components/view-show/asset-explorer-view-show.component';
import {AssetViewManagerComponent} from './components/asset-view-manager/asset-view-manager.component';
// import {AssetCreateComponent} from './components/asset/asset-create.component';
// import {AssetShowComponent} from './components/asset/asset-show.component';
// import {AssetEditComponent} from './components/asset/asset-edit.component';
// import {TaskCommentComponent} from './components/task-comment/task-comment.component';
// import {AssetDependencyComponent} from './components/asset-dependency/asset-dependency.component';
// import {AssetDependencyShowComponent} from './components/asset-dependency/show/asset-dependency-show.component';
// import {AssetDependencyEditComponent} from './components/asset-dependency/edit/asset-dependency-edit.component';
// import {SingleCommentComponent} from './components/single-comment/single-comment.component';
// import {TaskDetailComponent} from '../taskManager/components/detail/task-detail.component';
// import {TaskEditComponent} from '../taskManager/components/edit/task-edit.component';
// import {ManufacturerShowComponent} from './components/device/manufacturer/components/manufacturer-show/manufacturer-show.component';
// import {ManufacturerEditComponent} from './components/device/manufacturer/components/manufacturer-edit/manufacturer-edit.component';
// import {ModelDeviceShowComponent} from './components/device/model-device/components/model-device-show/model-device-show.component';
// import {AssetCloneComponent} from './components/asset-clone/asset-clone.component';
// import {PopupAssetMessageComponent} from './components/popups/popup-asset-message.component';
// import {TaskCommentDialogComponent} from './components/task-comment/dialog/task-comment-dialog.component';
import {TaskCreateComponent} from '../taskManager/components/create/task-create.component';
import {TaskActionsComponent} from '../taskManager/components/task-actions/task-actions.component';

@NgModule({
	imports: [
		// Angular
		CommonModule,
		SharedModule,
		FormsModule,
		// Kendo
		DropDownListModule,
		GridModule,
		ExcelExportModule,
		SortableModule,
		IntlModule,
		DateInputsModule,
		// Route
		AssetManagerRouteModule
	],
	declarations: [
		// AssetExplorerViewSelectorComponent,
		AssetViewManagerComponent,
		// AssetExplorerViewConfigComponent,
		// AssetExplorerViewSaveComponent,
		// AssetExplorerViewExportComponent,
		// AssetExplorerViewGridComponent,
		// AssetExplorerViewShowComponent,
		// AssetShowComponent,
		// AssetEditComponent,
		// AssetCreateComponent,
		// TaskCommentComponent,
		// AssetDependencyComponent,
		// AssetDependencyShowComponent,
		// AssetDependencyEditComponent,
		// SingleCommentComponent,
		// ModelDeviceShowComponent,
		// ManufacturerShowComponent,
		// ManufacturerEditComponent,
		// AssetCloneComponent,
		// PopupAssetMessageComponent,
		// TaskCommentDialogComponent,
		TaskCreateComponent,
		// TaskEditComponent,
		// TaskDetailComponent,
		TaskActionsComponent
	],
	providers: [
		// Resolve
		ModuleResolveService,
		// FieldsResolveService,
		// ReportResolveService,
		ReportsResolveService,
		// TagsResolveService,
		// Services
		CustomDomainService,
		AssetExplorerService,
		// TaskCommentService,
		// DependecyService,
		// TagService,
		// ManufacturerService,
		// ModelService
	],
	exports: [
		// AssetShowComponent,
		// AssetEditComponent,
		// AssetCreateComponent,
		// AssetDependencyComponent,
		// AssetDependencyShowComponent,
		// AssetDependencyEditComponent,
		// TaskCommentComponent
	],
	entryComponents: [
		// AssetExplorerViewSaveComponent,
		// AssetExplorerViewExportComponent,
		// AssetShowComponent,
		// AssetEditComponent,
		// AssetCreateComponent,
		// AssetDependencyComponent,
		// SingleCommentComponent,
		// ManufacturerShowComponent,
		// ManufacturerEditComponent,
		// ModelDeviceShowComponent,
		// AssetCloneComponent,
		// TaskCommentDialogComponent,
		TaskCreateComponent,
		// TaskEditComponent,
		// TaskDetailComponent
	],
})

export class AssetManagerModule {
}
