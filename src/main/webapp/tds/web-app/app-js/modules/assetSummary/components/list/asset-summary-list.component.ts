// Angular
import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
// Service
import {UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {PreferenceService} from '../../../../shared/services/preference.service';
// Model
import {COLUMN_MIN_WIDTH} from '../../../dataScript/model/data-script.model';
import {AssetSummaryColumnModel} from '../../model/asset-summary-column.model';
import {WindowService} from '../../../../shared/services/window.service';

@Component({
	selector: `tds-asset-summary-list`,
	templateUrl: 'asset-summary-list.component.html',
})
export class AssetSummaryListComponent implements OnInit {

	public assetSummaryColumnModel = new AssetSummaryColumnModel();
	public COLUMN_MIN_WIDTH = COLUMN_MIN_WIDTH;
	public gridData: any[] = [
		{
			bundle: {
				id: 3239,
				name: 'Buildout'
			},
			applicationCount: 226,
			serverCount: 26,
			deviceCount: 15,
			databaseCount: 0,
			storageCount: 0
		},
		{
			bundle: {
				id: 5689,
				name: 'Bundle - Moving Devices from Here to there'
			},
			applicationCount: 1,
			serverCount: 0,
			deviceCount: 0,
			databaseCount: 0,
			storageCount: 1
		},
		{
			bundle: {
				id: 3181,
				name: 'ERP App Environment'
			},
			applicationCount: 3,
			serverCount: 4,
			deviceCount: 4,
			databaseCount: 0,
			storageCount: 0
		},
		{
			bundle: {id: 3174, name: 'M1 - Physical'},
			applicationCount: 17,
			serverCount: 32,
			deviceCount: 27,
			databaseCount: 0,
			storageCount: 0
		},
		{
			bundle: {id: 5773, name: 'M1-Phy'},
			applicationCount: 110,
			serverCount: 0,
			deviceCount: 0,
			databaseCount: 11,
			storageCount: 0
		},
		{
			bundle: {id: 3180, name: 'M2-Hybrid'},
			applicationCount: 18,
			serverCount: 42,
			deviceCount: 22,
			databaseCount: 0,
			storageCount: 0
		},
		{
			bundle: {id: 2466, name: 'Master Bundle'},
			applicationCount: 7,
			serverCount: 81,
			deviceCount: 286,
			databaseCount: 0,
			storageCount: 10
		},
		{
			bundle: {id: 6257, name: 'NON Planning Bundle - by CN'},
			applicationCount: 4,
			serverCount: 0,
			deviceCount: 0,
			databaseCount: 0,
			storageCount: 0
		},
		{
			bundle: {id: 3179, name: 'Not Moving'},
			applicationCount: 2,
			serverCount: 1,
			deviceCount: 0,
			databaseCount: 0,
			storageCount: 0
		},
		{
			bundle: {id: 6305, name: 'Planning Bundle - by CN'},
			applicationCount: 1,
			serverCount: 0,
			deviceCount: 0,
			databaseCount: 0,
			storageCount: 0
		},
		{
			bundle: {id: 3178, name: 'Retired'},
			applicationCount: 0,
			serverCount: 0,
			deviceCount: 0,
			databaseCount: 0,
			storageCount: 0
		},
		{
			bundle: {id: 3177, name: 'Retiring'},
			applicationCount: 0,
			serverCount: 0,
			deviceCount: 0,
			databaseCount: 0,
			storageCount: 0
		},
		{
			bundle: {id: 5657, name: 'TBD'},
			applicationCount: 50,
			serverCount: 1021,
			deviceCount: 40,
			databaseCount: 0,
			storageCount: 0
		},
		{
			bundle: {id: 6412, name: 'Test Bundle'},
			applicationCount: 0,
			serverCount: 0,
			deviceCount: 0,
			databaseCount: 0,
			storageCount: 0
		},
		{
			bundle: {id: 6001, name: 'This is a very long bundle name that I am using to test alignment in the Dependency Analyzer because it seems like the right thing to do'},
			applicationCount: 0,
			serverCount: 0,
			deviceCount: 0,
			databaseCount: 0,
			storageCount: 0
		},
		{
			bundle: {id: 3661, name: 'Wave1- Bundle1'},
			applicationCount: 0,
			serverCount: 0,
			deviceCount: 0,
			databaseCount: 0,
			storageCount: 0
		}];

	public total = {
		applicationCount: 0,
		serverCount: 0,
		deviceCount: 0,
		databaseCount: 0,
		storageCount: 0
	};

	constructor(
		private dialogService: UIDialogService,
		private permissionService: PermissionService,
		private prompt: UIPromptService,
		private preferenceService: PreferenceService,
		private route: ActivatedRoute,
		private windowsService: WindowService) {
	}

	ngOnInit() {
		// Get the list
		this.gridData.forEach((item: any) => {
			this.total.applicationCount += item.applicationCount;
			this.total.serverCount += item.serverCount;
			this.total.deviceCount += item.deviceCount;
			this.total.databaseCount += item.databaseCount;
			this.total.storageCount += item.storageCount;
		});
	}

	/**
	 * Open different sections depending what is being selected
	 * @param item
	 */
	public onCellClick(item: any): void {
		if (item.columnIndex === 0) {
			this.windowsService.getWindow().location.href = '../moveBundle/show/' + item.dataItem.bundle.id;
		}
	}

	public onClickHeader(event: any): void {
		console.log('--');
	}

	public reloadData(): void {
		console.log('--');
	}

}