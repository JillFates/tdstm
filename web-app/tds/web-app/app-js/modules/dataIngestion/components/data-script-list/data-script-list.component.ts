import { Component, Inject } from '@angular/core';
import { StateService } from '@uirouter/angular';
import { Observable } from 'rxjs/Observable';

import { DataIngestionService } from '../../service/data-ingestion.service';

import { PermissionService } from '../../../../shared/services/permission.service';
import { UIPromptService } from '../../../../shared/directives/ui-prompt.directive';
import { COLUMN_MIN_WIDTH, DataScriptColumnModel, DataScriptRowModel } from '../../model/data-script.model';
import { GridDataResult } from '@progress/kendo-angular-grid';
import { NotifierService } from '../../../../shared/services/notifier.service';

@Component({
	selector: 'data-script-list',
	templateUrl: '../tds/web-app/app-js/modules/dataIngestion/components/data-script-list/data-script-list.component.html'
})
export class DataScriptListComponent {

	private dataScriptsModels = Array<DataScriptRowModel>();
	dataScriptColumnModel = new DataScriptColumnModel();
	COLUMN_MIN_WIDTH = COLUMN_MIN_WIDTH;
	gridData: GridDataResult;

	constructor(
		private stateService: StateService,
		@Inject('dataScripts') dataScripts: Observable<DataScriptRowModel[]>,
		private permissionService: PermissionService,
		private assetExpService: DataIngestionService,
		private prompt: UIPromptService,
		private notifier: NotifierService) {
		dataScripts.subscribe(
			(result) => {
				this.gridData = {
					data: result,
					total: result.length
				};
			},
			(err) => console.log(err));
		console.log(this.dataScriptColumnModel);
	}
}