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
import { AssetShowComponent } from './components/asset/asset-show.component';
import { AssetEditComponent } from './components/asset/asset-edit.component';
import { AssetCreateComponent } from './components/asset/asset-create.component';
import { SharedModule } from '../../shared/shared.module';
import { TaskCommentComponent } from './components/task-comment/task-comment.component';
import { AssetDependencyComponent } from './components/asset-dependency/asset-dependency.component';
import { SingleCommentComponent } from './components/single-comment/single-comment.component';
import { BulkChangeButtonComponent } from './components/bulk-change/components/bulk-change-button/bulk-change-button.component';
import { BulkChangeActionsComponent } from './components/bulk-change/components/bulk-change-actions/bulk-change-actions.component';
import { BulkChangeEditComponent } from './components/bulk-change/components/bulk-change-edit/bulk-change-edit.component';
import { TDSCheckboxComponent} from './components/tds-checkbox/tds-checkbox.component';
import { ManufacturerShowComponent } from './components/device/manufacturer/components/manufacturer-show/manufacturer-show.component';
import { ManufacturerEditComponent } from './components/device/manufacturer/components/manufacturer-edit/manufacturer-edit.component';
import { ModelDeviceShowComponent } from './components/device/model-device/components/model-device-show/model-device-show.component';
import {TaskDetailComponent} from '../taskManager/components/detail/task-detail.component';
import {TaskEditComponent} from '../taskManager/components/edit/task-edit.component';
import {TaskCreateComponent} from '../taskManager/components/create/task-create.component';

// Import Kendo Modules
import { DropDownListModule } from '@progress/kendo-angular-dropdowns';
import { GridModule } from '@progress/kendo-angular-grid';
import { ExcelExportModule } from '@progress/kendo-angular-excel-export';
import { SortableModule } from '@progress/kendo-angular-sortable';
import { IntlModule } from '@progress/kendo-angular-intl';
import { DateInputsModule } from '@progress/kendo-angular-dateinputs';
// Services
import { AssetExplorerService } from './service/asset-explorer.service';
import { TaskCommentService } from './service/task-comment.service';
import { DependecyService } from './service/dependecy.service';
import { TagService } from '../assetTags/service/tag.service';
import { BulkChangeService } from './service/bulk-change.service';
import { BulkCheckboxService } from './service/bulk-checkbox.service';
import { ManufacturerService } from './service/manufacturer.service';
import { ModelService } from  './service/model.service';
import {AssetCloneComponent} from './components/asset-clone/asset-clone.component';
import {PopupAssetMessageComponent} from './components/popups/popup-asset-message.component';
import {TaskCommentDialogComponent} from './components/task-comment/dialog/task-comment-dialog.component';
import {TaskManagerModule} from '../taskManager/task-manager.module';

@NgModule({
	imports: [
		CommonModule,
		SharedModule,
		FormsModule,
		DropDownListModule,
		GridModule,
		ExcelExportModule,
		SortableModule,
		IntlModule,
		DateInputsModule,
		UIRouterModule.forChild({ states: ASSET_EXPLORER_STATES })
	],
	declarations: [
		AssetExplorerViewSelectorComponent,
		AssetExplorerIndexComponent,
		AssetExplorerViewConfigComponent,
		AssetExplorerViewSaveComponent,
		AssetExplorerViewExportComponent,
		AssetExplorerViewGridComponent,
		AssetExplorerViewShowComponent,
		AssetShowComponent,
		AssetEditComponent,
		AssetCreateComponent,
		TaskCommentComponent,
		AssetDependencyComponent,
		SingleCommentComponent,
		BulkChangeButtonComponent,
		BulkChangeActionsComponent,
		BulkChangeEditComponent,
		TDSCheckboxComponent,
		ModelDeviceShowComponent,
		ManufacturerShowComponent,
		ManufacturerEditComponent,
		AssetCloneComponent,
		PopupAssetMessageComponent,
		TaskCommentDialogComponent,
		TaskCreateComponent,
		TaskEditComponent,
		TaskDetailComponent
	],
	providers: [
		AssetExplorerService,
		TaskCommentService,
		DependecyService,
		TagService,
		BulkChangeService,
		BulkCheckboxService,
		ManufacturerService,
		ModelService
	],
	exports: [AssetExplorerIndexComponent, TaskCommentComponent],
	entryComponents: [
		AssetExplorerViewSaveComponent,
		AssetExplorerViewExportComponent,
		AssetShowComponent,
		AssetEditComponent,
		AssetCreateComponent,
		AssetDependencyComponent,
		SingleCommentComponent,
		BulkChangeButtonComponent,
		BulkChangeActionsComponent,
		BulkChangeEditComponent,
		TDSCheckboxComponent,
		ManufacturerShowComponent,
		ManufacturerEditComponent,
		ModelDeviceShowComponent,
		AssetCloneComponent,
		TaskCommentDialogComponent,
		TaskCreateComponent,
		TaskEditComponent,
		TaskDetailComponent
	],
})

export class AssetExplorerModule {
}
