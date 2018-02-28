import {Component} from '@angular/core';
import {DependencyBatchService} from '../../service/dependency-batch.service';

@Component({
	selector: 'dependency-batch-record-detail-fields',
	templateUrl: '../tds/web-app/app-js/modules/dependencyBatch/components/dependency-batch-record-detail-fields/dependency-batch-record-detail-fields.component.html'
})
export class DependencyBatchRecordDetailFieldsComponent {

	private fieldsFilter: any = {
		options: [
			{text: 'All', value: 1},
			{text: 'With Errors', value: 2}
			],
		selected: {text: 'All', value: 1}
	};

	constructor(private dependencyBatchService: DependencyBatchService) {
		// Silence is golden
	}
}