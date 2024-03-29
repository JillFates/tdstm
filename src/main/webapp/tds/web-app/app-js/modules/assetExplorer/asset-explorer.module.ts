// Angular
import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
// Shared
import {SharedModule} from '../../shared/shared.module';
// Kendo
import { ComboBoxModule, DropDownListModule } from '@progress/kendo-angular-dropdowns';
import {GridModule} from '@progress/kendo-angular-grid';
import {SortableModule} from '@progress/kendo-angular-sortable';
import {IntlModule} from '@progress/kendo-angular-intl';
import {DateInputsModule} from '@progress/kendo-angular-dateinputs';
import {TabStripModule} from '@progress/kendo-angular-layout';
// Services
import {AssetExplorerService} from '../assetManager/service/asset-explorer.service';
import {TaskCommentService} from './service/task-comment.service';
import {DependecyService} from './service/dependecy.service';
import {TagService} from '../assetTags/service/tag.service';
import {ManufacturerService} from './service/manufacturer.service';
import {ModelService} from './service/model.service';
import {CustomDomainService} from '../fieldSettings/service/custom-domain.service';
import { ArchitectureGraphService} from './components/architecture-graph/service/architecture-graph.service';
import { DependencyAnalyzerService } from './components/dependency-analyzer/service/dependency-analyzer.service';
// Components
import {AssetShowComponent} from './components/asset/asset-show.component';
import {AssetEditComponent} from './components/asset/asset-edit.component';
import {TaskCommentComponent} from './components/task-comment/task-comment.component';
import {AssetDependencyComponent} from './components/asset-dependency/asset-dependency.component';
import {AssetDependencyShowComponent} from './components/asset-dependency/show/asset-dependency-show.component';
import {AssetDependencyEditComponent} from './components/asset-dependency/edit/asset-dependency-edit.component';
import {SingleNoteComponent} from './components/single-note/single-note.component';
import {TaskDetailComponent} from '../taskManager/components/detail/task-detail.component';
import {ManufacturerShowComponent} from './components/device/manufacturer/components/manufacturer-show/manufacturer-show.component';
import {ManufacturerEditComponent} from './components/device/manufacturer/components/manufacturer-edit/manufacturer-edit.component';
import {ModelDeviceShowComponent} from './components/device/model-device/components/model-device-show/model-device-show.component';
import {AssetCloneComponent} from './components/asset-clone/asset-clone.component';
import {PopupAssetMessageComponent} from './components/popups/popup-asset-message.component';
import {TaskCommentDialogComponent} from './components/task-comment/dialog/task-comment-dialog.component';
import {TaskEditCreateComponent} from '../taskManager/components/edit-create/task-edit-create.component';
import {TaskActionsComponent} from '../taskManager/components/task-actions/task-actions.component';
import {AssetCreateComponent} from './components/asset/asset-create.component';
import {TaskNotesComponent} from '../../shared/components/task-notes/task-notes.component';
import {ModelDeviceEditComponent} from './components/device/model-device/components/model-device-edit/model-device-edit.component';
import { TaskActionSummaryComponent } from '../taskManager/components/task-actions/task-action-summary.component';
import {AssetExplorerRouteModule} from './asset-explorer.route';
import {ArchitectureGraphComponent} from './components/architecture-graph/architecture-graph.component';
import {DependencyAnalyzerComponent} from './components/dependency-analyzer/dependency-analyzer.component';
import {RegenerateComponent} from './components/dependency-analyzer/components/regenerate/regenerate.component';
import {DependencyGroupStatusComponent} from './components/dependency-analyzer/components/dependency-group-status-modal/dependency-group-status.component';
import {ProvidersResolveService} from '../provider/resolve/providers-resolve.service';
import {ProviderService} from '../provider/service/provider.service';
import {RegenerateProgressDialogComponent} from './components/dependency-analyzer/components/regenerate-progress-dialog/regenerate-progress-dialog.component';
import {DependentsComponent} from './components/dependents/dependents.component';

@NgModule({
	imports: [
		// Angular
		CommonModule,
		SharedModule,
		FormsModule,
		// Kendo
		DropDownListModule,
		ComboBoxModule,
		GridModule,
		SortableModule,
		IntlModule,
		DateInputsModule,
		TabStripModule,
		// Route
		AssetExplorerRouteModule
	],
	declarations: [
		AssetShowComponent,
		AssetCreateComponent,
		AssetEditComponent,
		TaskCommentComponent,
		AssetDependencyComponent,
		AssetDependencyShowComponent,
		AssetDependencyEditComponent,
		SingleNoteComponent,
		ModelDeviceShowComponent,
		ManufacturerShowComponent,
		ManufacturerEditComponent,
		ModelDeviceEditComponent,
		AssetCloneComponent,
		PopupAssetMessageComponent,
		TaskCommentDialogComponent,
		TaskEditCreateComponent,
		TaskDetailComponent,
		TaskActionsComponent,
		TaskNotesComponent,
		TaskActionSummaryComponent,
		ArchitectureGraphComponent,
		DependencyAnalyzerComponent,
		RegenerateComponent,
		DependencyGroupStatusComponent,
		RegenerateProgressDialogComponent,
		DependencyAnalyzerComponent,
		DependentsComponent
	],
	providers: [
		// Services
		CustomDomainService,
		AssetExplorerService,
		TaskCommentService,
		DependecyService,
		TagService,
		ManufacturerService,
		ModelService,
		ArchitectureGraphService,
		DependencyAnalyzerService,
		ProviderService
	],
	exports: [
		AssetShowComponent,
		AssetCreateComponent,
		AssetEditComponent,
		AssetDependencyComponent,
		AssetDependencyShowComponent,
		AssetDependencyEditComponent,
		TaskCommentDialogComponent,
		TaskCommentComponent,
		TaskActionsComponent,
		TaskActionSummaryComponent,
		RegenerateComponent,
		DependencyGroupStatusComponent,
		RegenerateProgressDialogComponent,
		TaskActionSummaryComponent,
		DependentsComponent
	],
	entryComponents: [
		AssetShowComponent,
		AssetCreateComponent,
		AssetEditComponent,
		AssetDependencyComponent,
		AssetDependencyShowComponent,
		AssetDependencyEditComponent,
		TaskCommentDialogComponent,
		TaskCommentComponent,
		TaskDetailComponent,
		TaskEditCreateComponent,
		SingleNoteComponent,
		ManufacturerShowComponent,
		ManufacturerEditComponent,
		ModelDeviceEditComponent,
		ModelDeviceShowComponent,
		AssetCloneComponent,
		TaskActionSummaryComponent,
		RegenerateComponent,
		DependencyGroupStatusComponent,
		RegenerateProgressDialogComponent
	]
})

export class AssetExplorerModule {
}
