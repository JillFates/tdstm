import {Component, Input, Output, EventEmitter,  OnInit } from '@angular/core';
import {DataGridOperationsHelper} from '../../utils/data-grid-operations.helper';
import {TaskNotesColumnsModel} from './model/task-notes-columns.model';

declare var jQuery: any;

@Component({
	selector: `tds-task-notes`,
	templateUrl: '../tds/web-app/app-js/shared/components/task-notes/task-notes.component.html',
	styles: []
})
export class TaskNotesComponent implements OnInit {
	@Input() dataGridTaskNotesHelper: DataGridOperationsHelper;
	@Output() create: EventEmitter<void> = new EventEmitter<void>();
	protected taskNotesColumnsModel = new TaskNotesColumnsModel();

	constructor() {
		// constructor
	}

	ngOnInit() {
		/* on init */
	}

	protected onCreateNote(): void {
		this.create.emit();
	}

}