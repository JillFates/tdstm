// Angular
import {Component, ComponentFactoryResolver} from '@angular/core';
// Model
import {DialogService, ModalSize} from 'tds-component-library';
// Service
import {AssetExplorerService} from '../../../assetManager/service/asset-explorer.service';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {UserContextService} from '../../../auth/service/user-context.service';
import {ArchitectureGraphService} from '../../../assetManager/service/architecture-graph.service';
import {WindowService} from '../../../../shared/services/window.service';
// Component
import { DependecyService } from '../../service/dependecy.service';
import {AssetCommonShow} from '../asset/asset-common-show';
import {UserManageStaffComponent} from '../../../../shared/modules/header/components/manage-staff/user-manage-staff.component';

export function ApplicationShowComponent(template, modelId: number, metadata: any, parentDialog: any) {
	@Component({
		selector: `tds-application-show`,
		template: template
	})
	class ApplicationShowComponent extends AssetCommonShow {

		constructor(
			componentFactoryResolver: ComponentFactoryResolver,
			dialogService: DialogService,
			assetService: DependecyService,
			assetExplorerService: AssetExplorerService,
			notifierService: NotifierService,
			userContextService: UserContextService,
			windowService: WindowService,
			architectureGraphService: ArchitectureGraphService
			) {
				super(
					componentFactoryResolver,
					dialogService,
					assetService,
					assetExplorerService,
					notifierService,
					userContextService,
					windowService,
					architectureGraphService,
					parentDialog
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
				this.dialogService.open({
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
