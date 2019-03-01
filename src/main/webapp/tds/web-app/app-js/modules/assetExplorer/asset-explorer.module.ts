// Angular
import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
// Shared
import {SharedModule} from '../../shared/shared.module';
// Kendo
import {DropDownListModule} from '@progress/kendo-angular-dropdowns';
import {GridModule} from '@progress/kendo-angular-grid';
import {SortableModule} from '@progress/kendo-angular-sortable';
import {IntlModule} from '@progress/kendo-angular-intl';
import {DateInputsModule} from '@progress/kendo-angular-dateinputs';
// Services
import {AssetExplorerService} from '../assetManager/service/asset-explorer.service';
import {TaskCommentService} from './service/task-comment.service';
import {DependecyService} from './service/dependecy.service';
import {TagService} from '../assetTags/service/tag.service';
import {ManufacturerService} from './service/manufacturer.service';
import {ModelService} from './service/model.service';
import {CustomDomainService} from '../fieldSettings/service/custom-domain.service';
// Components
import {AssetShowComponent} from './components/asset/asset-show.component';
import {AssetEditComponent} from './components/asset/asset-edit.component';
import {TaskCommentComponent} from './components/task-comment/task-comment.component';
import {AssetDependencyComponent} from './components/asset-dependency/asset-dependency.component';
import {AssetDependencyShowComponent} from './components/asset-dependency/show/asset-dependency-show.component';
import {AssetDependencyEditComponent} from './components/asset-dependency/edit/asset-dependency-edit.component';
import {SingleCommentComponent} from './components/single-comment/single-comment.component';
import {SingleNoteComponent} from './components/single-note/single-note.component';
import {TaskDetailComponent} from '../taskManager/components/detail/task-detail.component';
import {TaskEditComponent} from '../taskManager/components/edit/task-edit.component';
import {ManufacturerShowComponent} from './components/device/manufacturer/components/manufacturer-show/manufacturer-show.component';
import {ManufacturerEditComponent} from './components/device/manufacturer/components/manufacturer-edit/manufacturer-edit.component';
import {ModelDeviceShowComponent} from './components/device/model-device/components/model-device-show/model-device-show.component';
import {AssetCloneComponent} from './components/asset-clone/asset-clone.component';
import {PopupAssetMessageComponent} from './components/popups/popup-asset-message.component';
import {TaskCommentDialogComponent} from './components/task-comment/dialog/task-comment-dialog.component';
import {TaskCreateComponent} from '../taskManager/components/create/task-create.component';
import {TaskActionsComponent} from '../taskManager/components/task-actions/task-actions.component';
import {AssetCreateComponent} from './components/asset/asset-create.component';
import {TaskNotesComponent} from '../../shared/components/task-notes/task-notes.component';

@NgModule({
	imports: [
		// Angular
		CommonModule,
		SharedModule,
		FormsModule,
		// Kendo
		DropDownListModule,
		GridModule,
		SortableModule,
		IntlModule,
		DateInputsModule,
	],
	declarations: [
		AssetShowComponent,
		AssetCreateComponent,
		AssetEditComponent,
		TaskCommentComponent,
		AssetDependencyComponent,
		AssetDependencyShowComponent,
		AssetDependencyEditComponent,
		SingleCommentComponent,
		SingleNoteComponent,
		ModelDeviceShowComponent,
		ManufacturerShowComponent,
		ManufacturerEditComponent,
		AssetCloneComponent,
		PopupAssetMessageComponent,
		TaskCommentDialogComponent,
		TaskCreateComponent,
		TaskEditComponent,
		TaskDetailComponent,
		TaskActionsComponent,
		TaskNotesComponent
	],
	providers: [
		// Services
		CustomDomainService,
		AssetExplorerService,
		TaskCommentService,
		DependecyService,
		TagService,
		ManufacturerService,
		ModelService
	],
	exports: [
		AssetShowComponent,
		AssetCreateComponent,
		AssetEditComponent,
		AssetDependencyComponent,
		AssetDependencyShowComponent,
		AssetDependencyEditComponent,
		TaskCommentComponent,
		TaskDetailComponent
	],
	entryComponents: [
		AssetShowComponent,
		AssetCreateComponent,
		AssetEditComponent,
		AssetDependencyComponent,
		SingleCommentComponent,
		SingleNoteComponent,
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
