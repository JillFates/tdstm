// Angular
import {Component, Input, Output, EventEmitter, OnChanges, SimpleChanges} from '@angular/core';

@Component({
	selector: 'tds-plan-versus-status',
	templateUrl: 'plan-versus-status.component.html'
})
export class PlanVersusStatusComponent implements OnChanges {
	@Input() currentProgress = 0;
	@Output() changeProgress: EventEmitter<number> = new EventEmitter<number>();
	public progress = 0;
	public showEditControl = false;

	/**
	 * On host input changes get the reference to the curren progress
 	 * @param {SimpleChanges} changes  Input changes
	*/
	ngOnChanges(changes: SimpleChanges): void {
		if (changes && changes.currentProgress) {
			this.progress = changes.currentProgress.currentValue;
		}
	}

	/**
	 * Save the status value
 	 * @param {string} value  Status value
	*/
	public onSave(value: string): void {
		this.changeProgress.emit(this.progress);
	}

	/**
	 * On click chart, toggle the edit controls
	*/
	public onClickChart() {
		this.showEditControl = !this.showEditControl;
	}

	/**
	 * Reset the statue of the control
	*/
	public reset() {
		this.showEditControl = false;
	}
}
