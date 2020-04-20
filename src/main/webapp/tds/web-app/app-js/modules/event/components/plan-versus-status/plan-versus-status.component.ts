// Angular
import {Component, Input, Output, EventEmitter, OnChanges, SimpleChanges, ViewChild} from '@angular/core';
import {NgForm, Form} from '@angular/forms';
import {PermissionService} from '../../../../shared/services/permission.service';
import {Permission} from '../../../../shared/model/permission.model';

@Component({
	selector: 'tds-plan-versus-status',
	templateUrl: 'plan-versus-status.component.html'
})
export class PlanVersusStatusComponent implements OnChanges {
	@ViewChild('form', {static: false}) form: NgForm ;
	@Input() currentProgress = 0;
	@Input() hasBundleSteps: boolean;
	@Input() isDisabled: Boolean = false;
	@Output() changeProgress: EventEmitter<number> = new EventEmitter<number>();
	public progress = 0;
	public showEditControl = false;

	constructor(private permissionService: PermissionService) { }

	/**
	 * On host input changes get the reference to the curren progress
	 * and reset the state of the input progress
 	 * @param {SimpleChanges} changes  Input changes
	*/
	ngOnChanges(changes: SimpleChanges): void {
		if (changes && changes.currentProgress) {
			this.progress = changes.currentProgress.currentValue;
			this.currentProgress = this.progress;
			this.reset();
		}
		if (changes && changes.hasBundleSteps) {
			this.hasBundleSteps = changes.hasBundleSteps.currentValue;
		}
	}

	/**
	 * Save the status value
 	 * @param {string} value  Status value
	*/
	public onSave(value: number): void {
		this.currentProgress = value;
		this.changeProgress.emit(this.progress);
		this.reset();
	}

	/**
	 * On click chart, toggle the edit controls
	*/
	public onClickChart() {
		if (!this.isDisabled && this.isEditAvailable()) {
			this.showEditControl = !this.showEditControl;
		}
	}

	/**
	 * Reset the status of the control
	*/
	public reset() {
		if (this.form && this.form.controls) {
			this.form.controls['currentStatus'].markAsPristine();
			this.showEditControl = false;
		}
	}

	/**
	 * Based on the current progress get the corresponding image number
	 * for pairs numbers return the value otherwise return the value - 1
 	 * @param {number} progress  Current status percent progress
	*/
	public getImageDial(progress: number): number  {
		if (progress) {
			return (progress % 2 === 0) ? progress : progress - 1;
		}

		return progress;
	}

	protected isEditAvailable(): boolean {
		return this.permissionService.hasPermission(Permission.EventEdit);
	}
}
