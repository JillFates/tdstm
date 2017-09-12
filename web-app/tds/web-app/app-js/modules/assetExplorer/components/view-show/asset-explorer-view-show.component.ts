import { Component, Inject } from '@angular/core';
import { Observable } from 'rxjs/Rx';

import { ViewModel } from '../../model/view.model';

@Component({
	selector: 'asset-explorer-view-show',
	templateUrl: '../tds/web-app/app-js/modules/assetExplorer/components/view-show/asset-explorer-view-show.component.html'
})
export class AssetExplorerViewShowComponent {
	model: ViewModel;

	constructor( @Inject('report') report: Observable<ViewModel>) {
		report.subscribe(
			(result) => {
				this.model = result;
			},
			(err) => console.log(err));
	}
}