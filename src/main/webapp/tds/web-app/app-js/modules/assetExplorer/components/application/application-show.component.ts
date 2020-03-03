import {Component, ComponentFactoryResolver} from '@angular/core';
import { UIActiveDialogService, UIDialogService } from '../../../../shared/services/ui-dialog.service';
import { DependecyService } from '../../service/dependecy.service';
import { AssetEditComponent } from '../asset/asset-edit.component';
import { DOMAIN, DIALOG_SIZE } from '../../../../shared/model/constants';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {AssetExplorerService} from '../../../assetManager/service/asset-explorer.service';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {AssetCloneComponent} from '../asset-clone/asset-clone.component';
import {CloneCLoseModel} from '../../model/clone-close.model';
import {AssetModalModel} from '../../model/asset-modal.model';
import {AssetCommonShow} from '../asset/asset-common-show';
import {WindowService} from '../../../../shared/services/window.service';
import {UserManageStaffComponent} from '../../../../shared/modules/header/components/manage-staff/user-manage-staff.component';
import {PersonModel} from '../../../../shared/components/add-person/model/person.model';
import {UserContextService} from '../../../auth/service/user-context.service';
import {ArchitectureGraphService} from '../../../assetManager/service/architecture-graph.service';
import {AssetTagUIWrapperService} from '../../../../shared/services/asset-tag-ui-wrapper.service';
import {DialogService, ModalSize} from 'tds-component-library';

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

		showAssetEditView(): Promise<any> {
			const componentParameters = [
				{ provide: 'ID', useValue: this.mainAsset },
				{ provide: 'ASSET', useValue: DOMAIN.APPLICATION }
			];

			return this.dialogService
				.replace(AssetEditComponent, componentParameters, DIALOG_SIZE.XXL);
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

		/**
		 * Allows to clone an application asset
		 */
		onCloneAsset(): void {

			const cloneModalModel: AssetModalModel = {
				assetType: DOMAIN.APPLICATION,
				assetId: this.mainAsset
			}
			this.dialogService.extra(AssetCloneComponent, [
				{provide: AssetModalModel, useValue: cloneModalModel}
			], false, false).then( (result: CloneCLoseModel)  => {

				if (result.clonedAsset && result.showEditView) {
					const componentParameters = [
						{ provide: 'ID', useValue: result.assetId },
						{ provide: 'ASSET', useValue: DOMAIN.APPLICATION }
					];

					this.dialogService
						.replace(AssetEditComponent, componentParameters, DIALOG_SIZE.XXL);
				} else if (!result.clonedAsset && result.showView) {
					this.showAssetDetailView(DOMAIN.APPLICATION, result.assetId);
				}
			})
				.catch( error => console.log('error', error));
		}

	}
	return ApplicationShowComponent;
}
