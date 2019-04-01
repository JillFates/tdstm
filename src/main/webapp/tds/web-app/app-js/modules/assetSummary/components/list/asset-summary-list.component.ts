import {Component, OnInit} from '@angular/core';
import {UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {ActivatedRoute} from '@angular/router';

declare var jQuery: any;

@Component({
	selector: `tds-asset-summary-list`,
	templateUrl: 'asset-summary-list.component.html',
})
export class AssetSummaryListComponent implements OnInit {

	constructor(
		private dialogService: UIDialogService,
		private permissionService: PermissionService,
		private prompt: UIPromptService,
		private preferenceService: PreferenceService,
		private route: ActivatedRoute) {
	}

	ngOnInit() {
		console.log('--');
	}

}