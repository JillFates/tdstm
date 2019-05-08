// Angular
import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
// Service
import {UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {PreferenceService} from '../../../../shared/services/preference.service';
// Model
import {COLUMN_MIN_WIDTH} from '../../../dataScript/model/data-script.model';
import {AssetSummaryColumnModel} from '../../model/asset-summary-column.model';
import {AssetSummaryService} from '../../service/asset-summary.service';

@Component({
	selector: `tds-asset-summary-list`,
	templateUrl: 'asset-summary-list.component.html',
})
export class AssetSummaryListComponent implements OnInit {

	public assetSummaryColumnModel = new AssetSummaryColumnModel();
	public COLUMN_MIN_WIDTH = COLUMN_MIN_WIDTH;
	public gridData: any[] = [];
	public total = {
		application: 0,
		server: 0,
		device: 0,
		database: 0,
		storage: 0
	};

	constructor(
		private dialogService: UIDialogService,
		private permissionService: PermissionService,
		private prompt: UIPromptService,
		private preferenceService: PreferenceService,
		private route: ActivatedRoute,
		private router: Router,
		private assetSummaryService: AssetSummaryService) {
	}

	ngOnInit() {
		this.assetSummaryService.getSummaryTable().subscribe((data: any) => {
			this.gridData = data;

			// Get the list
			this.gridData.forEach((item: any) => {
				this.total.application += item.application.count;
				this.total.server += item.server.count;
				this.total.device += item.device.count;
				this.total.database += item.database.count;
				this.total.storage += item.storage.count;
			});
		});
	}

	/**
	 * Open from the Header the List
	 * @param event
	 * @param property
	 */
	public onClickHeader(event: any, property: string): void {
		if (property !== 'bundle') {
			this.router.navigate(['/asset', 'views', this.gridData[0][property].id, 'show']);
		}
	}

	public reloadData(): void {
		console.log('--');
	}

}