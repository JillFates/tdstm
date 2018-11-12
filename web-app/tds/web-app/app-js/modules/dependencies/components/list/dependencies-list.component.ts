import {Component, Inject, OnInit} from '@angular/core';

import {UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {ActivatedRoute} from '@angular/router';

@Component({
	selector: 'tds-dependencies-list',
	templateUrl: '../tds/web-app/app-js/modules/dependencies/components/list/dependencies-list.component.html'
})
export class DependenciesListComponent implements OnInit {
	constructor(
		private route: ActivatedRoute,
		private dialogService: UIDialogService,
		private permissionService: PermissionService,
		private prompt: UIPromptService,
		private preferenceService: PreferenceService) {
	}

	ngOnInit() {
		console.log('dependencies init');
	}
}
