import {Component, ViewChild, ElementRef, OnInit} from '@angular/core';

import {UIExtraDialog} from '../../services/ui-dialog.service';
import { UIPromptService} from '../../directives/ui-prompt.directive';
import { DecoratorOptions} from '../../model/ui-modal-decorator.model';
import {DateRangeSelectorModel} from './model/date-range-selector.model';
import {DateUtils} from '../../utils/date.utils';
import { SelectionRange } from '@progress/kendo-angular-dateinputs';
import {TranslatePipe} from '../../pipes/translate.pipe';

declare var jQuery: any;
@Component({
	selector: 'tds-date-range-selector',
	template: `
        <div tds-handle-escape (escPressed)="cancelCloseDialog()" class="modal fade in date-range-selector-component" id="date-range-selector-component" data-backdrop="static" tabindex="0" role="dialog">
            <div class="modal-dialog modal-md" role="document">
                <div class="modal-content" tds-ui-modal-decorator=""
                     [options]="modalOptions">
                    <div class="modal-header">
                        <button (click)="cancelCloseDialog()" type="button" class="close component-action-close" aria-label="Close">
                            <span aria-hidden="true">×</span>
                        </button>
                        <h4 class="modal-title">{{getTitle()}}</h4>
                    </div>
                    <div class="modal-body">
                        <div class="modal-body-container">
                            <form name="dateRangeSelectorForm" role="form" data-toggle="validator" #dateRangeSelectorForm='ngForm' class="form-horizontal left-alignment">
                                <div>
                                    <div class="times-container">
                                        <div class="times-section">
                                            <kendo-dateinput
                                                    [disabled]="model.locked"
                                                    (valueChange)="onDateChanged('start', $event)"
                                                    [format]="getDateTimeFormat(model.start)"
                                                    [(value)]="model.start"></kendo-dateinput>
                                        </div>
                                        <div class="times-section">
                                            <kendo-dateinput
                                                    (valueChange)="onDateChanged('end', $event)"
                                                    [format]="getDateTimeFormat(model.end)"
                                                    [(value)]="model.end"></kendo-dateinput>
                                        </div>
                                    </div>
                                    <div>
                                        <kendo-multiviewcalendar
                                                [(activeRangeEnd)]="activeRangeEnd"
                                                (selectionRangeChange)="handleSelectionRange($event)"
                                                kendoDateRangeSelection [selectionRange]="model"></kendo-multiviewcalendar>
                                    </div>
                                </div>
                                <div>
                                    <div class="duration-controls">
                                        <div class="duration-value"><label>Estimated duration</label></div>
                                        <div class="duration-value">
                                            <input type="number"
                                                   min="0"
                                                   (change)="onChangeDuration('days')"
                                                   [(ngModel)]="model.duration.days" name="durationDays" id="durationDays" >
                                            <label class="label-part" for="durationDays">Days</label>
                                        </div>
                                        <div  class="duration-value">
                                            <input type="number"
                                                   min="0"
                                                   (change)="onChangeDuration('hours')"
                                                   [(ngModel)]="model.duration.hours" name="durationHours" id="durationHours" >
                                            <label class="label-part" for="durationHours">Hours</label>
                                        </div>
                                        <div  class="duration-value">
                                            <input type="number"
                                                   min="0"
                                                   (change)="onChangeDuration('minutes')"
                                                   [(ngModel)]="model.duration.minutes" name="durationMinutes" id="durationMinutes" >
                                            <label class="label-part" for="durationMinutes">Minutes</label>
                                        </div>
                                        <label data-toggle="popover"
                                               data-trigger="hover"
                                               class="duration-value label-part"
                                               data-content="Click to toggle the lock. When locked, changes to the Estimated Start/Finish will preserve the Duration">
                                            <i class="fa fa-fw  lock-state"
                                               [ngClass]="model.locked ? 'fa-lock' : 'fa-unlock'"
                                               (click)="model.locked = !model.locked">
                                            </i>
                                        </label>
                                    </div>
                                </div>
                            </form>
                        </div>
                    </div>
                    <div class="modal-footer form-group-center">
                        <button type="button"
                                [disabled]="!canSave()"
                                class="btn btn-primary pull-left component-action-save" (click)="save()"><span
                                class="fa fa-fw fa-floppy-o"></span> Save
                        </button>
                        <button type="button"
                                class="btn btn-default pull-right component-action-cancel" (click)="cancelCloseDialog()">
                            <span class="glyphicon glyphicon-ban-circle"></span>
                            <span>Cancel</span>
                        </button>
                    </div>
                </div>
            </div>
        </div>
	`
})
export class DateRangeSelectorComponent extends UIExtraDialog  implements  OnInit {
	dataSignature: string;
	title: string;
	modalOptions: DecoratorOptions;
	activeRangeEnd = 'start';

	constructor(
		public model: DateRangeSelectorModel,
		private promptService: UIPromptService,
		private translatePipe: TranslatePipe) {

		super('#date-range-selector-component');
		this.modalOptions = { isDraggable: true, isResizable: false, isCentered: false };
	}

	ngOnInit() {
		this.dataSignature = JSON.stringify(this.model);
		jQuery('[data-toggle="popover"]').popover();
	}
	/**
	 * Verify the Object has not changed
	 * @returns {boolean}
	 */
	protected isDirty(): boolean {
		return this.dataSignature !== JSON.stringify(this.model) && this.model.start && this.model.end;
	}

	/**
	 * Verify if user filled all required fiedls
	 * @returns {boolean}
	 */
	public canSave(): boolean {
		return this.isDirty();
	}

	/**
	 * Passing a valid date returns the user preference date format plus the default time format
	 * Otherwise returns empty string
	 * @param {any} date
	 * @returns {string}
	 */
	getDateTimeFormat(date: any): string {
		return date ? `${this.model.dateFormat} ${this.model.timeFormat}` :  this.model.dateFormat;
	}

	/**
	 * Save the changes
	 */
	save(): void {
		this.close(this.model);
	}

	/**
	 * Close the Dialog but first it verify is not Dirty
	 */
	public cancelCloseDialog(): void {
		if (this.isDirty()) {

			this.promptService.open(
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED')	,
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.UNSAVED_CHANGES_MESSAGE')	,
				this.translatePipe.transform('GLOBAL.CONFIRM'),
				this.translatePipe.transform('GLOBAL.CANCEL'))
				.then(confirm => {
					if (confirm) {
						this.dismiss();
					}
				})
				.catch((error) => console.log(error));
		} else {
			this.dismiss();
		}
	}

	/**
	 * On range selected, if lock in on just adjust the end date to the current duration, otherwise set the correct interval
	 * @param range -  current range selected
	 * @returns {void}
	 */
	public handleSelectionRange(range: SelectionRange): void {
		const {start, end, locked, dateFormat, timeFormat, duration} = this.model;
		let newStart = null;
		let newEnd = null;

		// preserve start hours
		const startHours = start ? start.getHours() : 0;
		const startMinutes = start ? start.getMinutes() : 0;

		if (locked) {
			let seed = null;

			if (range.end > end) {
				seed = range.end;
			} else {
				if (range.start < start) {
					seed = range.start;
				} else {
					seed = range[this.activeRangeEnd] ? range[this.activeRangeEnd] : range.start;
				}
			}

			const {days, hours, minutes} = this.model.duration;
			newStart = DateUtils.increment(seed, [
				{value: startHours, unit: 'hours'},
				{value: startMinutes, unit: 'minutes'}] );
			newEnd = DateUtils.increment(newStart, [
				{value: days, unit: 'days'},
				{value: hours, unit: 'hours'},
				{value: minutes, unit: 'minutes'}]);

			this.model = {start: newStart, end: newEnd, locked, dateFormat, timeFormat, duration};
		} else {
			this.model = {start: range.start, end: range.end, locked, dateFormat, timeFormat,
				duration: DateUtils.getDurationPartsAmongDates(range.start, range.end) };
		}
	}

	/**
	 * On date changed if lock is off update the duration, if lock is on shift start and end dates to keep duration intact
	 * @param {string} type: Could be start or end
	 * @param {any} value: Current date value selected
	 * @returns {void}
	 */
	onDateChanged(type: 'start' | 'end', value: any): void {
		if (!value) {
			return;
		}

		const start = type === 'start' ? value : this.model.start;
		const end = type === 'end' ? value : this.model.end;

		if (!this.model.locked) {
			if (this.model.start && this.model.end) {
				this.model.duration = DateUtils.getDurationPartsAmongDates(start, end);
			}
		} else {
			if (type === 'end' && value) {
				const duration = DateUtils.getDurationPartsAmongDates(this.model.end, value);
				this.model.start = DateUtils.increment(this.model.start,
					[
										{value: duration.days, unit: 'days'},
										{value: duration.hours, unit: 'hours'},
										{value: duration.minutes, unit: 'minutes'}]);

				this.model.end = DateUtils.increment(this.model.end,
					[
										{value: duration.days, unit: 'days'},
										{value: duration.hours, unit: 'hours'},
										{value: duration.minutes, unit: 'minutes'}]);
			}
		}
	}

	/**
	 * On change duration calculates the increment/decrement and shift the end date correspondingly
	 * @param {string} unit: unit to increment
	 * @returns {void}
	 */
	onChangeDuration(unit: 'days' | 'hours' | 'minutes'): void {
		if (!this.model.start || !this.model.end) {
			return;
		}

		const {start, dateFormat, timeFormat, locked, duration} = this.model;

		const originalParts = DateUtils.getDurationPartsAmongDates(this.model.start, this.model.end);
		const diff = duration[unit] - originalParts[unit];

		const end =  DateUtils.increment(this.model.end, [{value: diff, unit}]);
		this.model = {start, end, dateFormat, timeFormat, locked, duration};
	}

	/**
	 * Based upon current activeRangeEnd value display the window title
	 * @returns {void}
	 */
	getTitle(): string {
		const startMessage = this.translatePipe.transform('TASK_MANAGER.EDIT.SELECT_START_DATE');
		const endMessage = this.translatePipe.transform('TASK_MANAGER.EDIT.SELECT_END_DATE');

		if (this.model.locked) {
			return startMessage;
		}
		return this.activeRangeEnd === 'start' ? startMessage : endMessage;
	}

}