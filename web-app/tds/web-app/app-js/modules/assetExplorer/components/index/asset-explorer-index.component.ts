import { Component } from '@angular/core';
import { StateService } from '@uirouter/angular';
import {AssetExplorerStates} from '../../asset-explorer-routing.states';

@Component({
	moduleId: module.id,
	selector: 'asset-explorer-index',
	templateUrl: '../tds/web-app/app-js/modules/assetExplorer/components/index/asset-explorer-index.component.html'
})
export class AssetExplorerIndexComponent {

	constructor(private stateService: StateService) {}

	protected onCreateNew(): void {
		this.stateService.go(AssetExplorerStates.REPORT_CREATE.name);
	}
}