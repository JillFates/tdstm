// Angular
import {Component, ComponentFactoryResolver} from '@angular/core';
// Model
import {DialogService, ModalSize} from 'tds-component-library';
// Service
import { UIActiveDialogService, UIDialogService } from '../../../../shared/services/ui-dialog.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {AssetExplorerService} from '../../../assetManager/service/asset-explorer.service';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {UserContextService} from '../../../auth/service/user-context.service';
import {ArchitectureGraphService} from '../../../assetManager/service/architecture-graph.service';
import {AssetTagUIWrapperService} from '../../../../shared/services/asset-tag-ui-wrapper.service';
import {WindowService} from '../../../../shared/services/window.service';
// Component
import { DependecyService } from '../../service/dependecy.service';
import {AssetCommonShow} from '../asset/asset-common-show';
import {UserManageStaffComponent} from '../../../../shared/modules/header/components/manage-staff/user-manage-staff.component';

export function ApplicationShowComponent(template, modelId: number, metadata: any) {
	@Component({
		selector: `tds-application-show`,
		template: template
	})
	class ApplicationShowComponent extends AssetCommonShow {

		constructor(
			private componentFactoryResolver: ComponentFactoryResolver,
			private newDialogService: DialogService,
			activeDialog: UIActiveDialogService,
			dialogService: UIDialogService,
			assetService: DependecyService,
			prompt: UIPromptService,
			assetExplorerService: AssetExplorerService,
			notifierService: NotifierService,
			userContextService: UserContextService,
			windowService: WindowService,
			architectureGraphService: ArchitectureGraphService,
			assetTagUIWrapperService: AssetTagUIWrapperService
			) {
				super(
					activeDialog,
					dialogService,
					assetService,
					prompt,
					assetExplorerService,
					notifierService,
					userContextService,
					windowService,
					architectureGraphService,
					assetTagUIWrapperService
				);
				this.mainAsset = modelId;
				this.assetTags = metadata.assetTags;
				this.loadThumbnailData(this.mainAsset);
		}

		/**
		 * Open the User Management Staff Component
		 * @param personModelId
		 */
		public launchManageStaff(personId: number): void {
			if (personId) {
				this.newDialogService.open({
					componentFactoryResolver: this.componentFactoryResolver,
					component: UserManageStaffComponent,
					data: {
						personId: personId
					},
					modalConfiguration: {
						title: 'Manage Staff',
						draggable: true,
						modalSize: ModalSize.CUSTOM,
						modalCustomClass: 'custom-user-manage-dialog'
					}
				}).subscribe();
			}
		}

	}
	return ApplicationShowComponent;
}
