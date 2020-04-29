// Angular
import {Component, ComponentFactoryResolver} from '@angular/core';
// Service
import {DependecyService} from '../../service/dependecy.service';
import {AssetExplorerService} from '../../../assetManager/service/asset-explorer.service';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {UserContextService} from '../../../auth/service/user-context.service';
import {ArchitectureGraphService} from '../../../assetManager/service/architecture-graph.service';
import {DialogService} from 'tds-component-library';
import {AssetTagUIWrapperService} from '../../../../shared/services/asset-tag-ui-wrapper.service';
// Component
import {AssetCommonShow} from '../asset/asset-common-show';
import {WindowService} from '../../../../shared/services/window.service';

export function StorageShowComponent(template, modelId: number, metadata: any, parentDialog: any) {
	@Component({
		selector: `tds-storage-show`,
		template: template
	})
	class StorageShowComponent extends AssetCommonShow {
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
				parentDialog,
				metadata
			);
			this.mainAsset = modelId;
			this.assetTags = metadata.assetTags;
			this.loadThumbnailData(this.mainAsset);
		}

	}

	return StorageShowComponent;
}
