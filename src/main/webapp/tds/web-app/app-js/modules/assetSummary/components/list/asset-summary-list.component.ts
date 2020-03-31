// Angular
import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
// Service
import {UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {PreferenceService} from '../../../../shared/services/preference.service';

// Components
import { HeaderActionButtonData } from 'tds-component-library';

// Model
import {COLUMN_MIN_WIDTH} from '../../../dataScript/model/data-script.model';
import {AssetSummaryColumnModel} from '../../model/asset-summary-column.model';
import {AssetSummaryService} from '../../service/asset-summary.service';
import {PREFERENCES_LIST} from '../../../../shared/services/preference.service';

@Component({
	selector: `tds-asset-summary-list`,
	templateUrl: 'asset-summary-list.component.html',
})
export class AssetSummaryListComponent implements OnInit {
	public headerActionButtons: HeaderActionButtonData[] = [];
	public assetSummaryColumnModel = new AssetSummaryColumnModel();
	public justPlanning = false;
	public COLUMN_MIN_WIDTH = COLUMN_MIN_WIDTH;
	public gridData: any[] = [];
	public total = {
		application: {
			total: 0,
			viewId: 7
		},
		server: {
			total: 0,
			viewId: 4
		},
		device: {
			total: 0,
			viewId: 3
		},
		database: {
			total: 0,
			viewId: 2
		},
		storage: {
			total: 0,
			viewId: 6
		}
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
		this.preferenceService.getPreference(PREFERENCES_LIST.ASSET_JUST_PLANNING).subscribe((preferences: any) => {
			this.justPlanning = (preferences[PREFERENCES_LIST.ASSET_JUST_PLANNING]) ? preferences[PREFERENCES_LIST.ASSET_JUST_PLANNING].toString() === 'true' : false;
			this.getAssetSummaryData();
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
		this.getAssetSummaryData();
	}

	/**
	 * Change the preference and update the grid
	 * @param isChecked
	 */
	public onChangeJustPlanning(isChecked = false): void {
		this.preferenceService.setPreference(PREFERENCES_LIST.ASSET_JUST_PLANNING, isChecked.toString()).subscribe(() => {
			this.reloadData();
		});
	}

	/**
	 * Get the Summary Data
	 */
	private getAssetSummaryData(): void {
		this.assetSummaryService.getSummaryTable().subscribe((data: any) => {
			this.gridData = data;

			// Get the list
			this.total.application.total = 0;
			this.total.server.total = 0;
			this.total.device.total = 0;
			this.total.database.total = 0;
			this.total.storage.total = 0;
			this.gridData.forEach((item: any) => {
				this.total.application.total += item.application.count;
				this.total.server.total += item.server.count;
				this.total.device.total += item.device.count;
				this.total.database.total += item.database.count;
				this.total.storage.total += item.storage.count;
			});
		});
	}

}
