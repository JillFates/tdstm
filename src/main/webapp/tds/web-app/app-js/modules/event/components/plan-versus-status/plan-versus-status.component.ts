// Angular
import {Component, Input, Output, EventEmitter, OnChanges, SimpleChanges, ViewChild} from '@angular/core';
import {NgForm, Form} from '@angular/forms';

@Component({
	selector: 'tds-plan-versus-status',
	templateUrl: 'plan-versus-status.component.html'
})
export class PlanVersusStatusComponent implements OnChanges {
	@ViewChild('form') form: NgForm ;
	@Input() currentProgress = 0;
	@Output() changeProgress: EventEmitter<number> = new EventEmitter<number>();
	public progress = 0;
	public showEditControl = false;

	/**
	 * On host input changes get the reference to the curren progress
	 * and reset the state of the input progress
 	 * @param {SimpleChanges} changes  Input changes
	*/
	ngOnChanges(changes: SimpleChanges): void {
		if (changes && changes.currentProgress) {
			this.progress = changes.currentProgress.currentValue;
			this.reset();
		}
	}

	/**
	 * Save the status value
 	 * @param {string} value  Status value
	*/
	public onSave(value: string): void {
		this.changeProgress.emit(this.progress);
		this.reset();
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
		this.form.controls['currentStatus'].markAsPristine();
	}
}
