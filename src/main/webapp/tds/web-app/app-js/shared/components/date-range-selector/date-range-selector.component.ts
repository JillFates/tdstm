import {Component, OnInit, ChangeDetectionStrategy, Input} from '@angular/core';

import {UIExtraDialog} from '../../services/ui-dialog.service';
import { UIPromptService} from '../../directives/ui-prompt.directive';
import { DecoratorOptions} from '../../model/ui-modal-decorator.model';
import {DateRangeSelectorModel} from './model/date-range-selector.model';
import {DateUtils} from '../../utils/date.utils';
import { SelectionRange } from '@progress/kendo-angular-dateinputs';
import {TranslatePipe} from '../../pipes/translate.pipe';
import {Dialog, DialogButtonType, DialogConfirmAction, DialogService} from 'tds-component-library';
import * as R from 'ramda';

declare var jQuery: any;
@Component({
	selector: 'tds-date-range-selector',
	template: `
        <div class="date-range-selector-component">
			<form name="dateRangeSelectorForm" role="form" data-toggle="validator" #dateRangeSelectorForm='ngForm' class="form-horizontal left-alignment">
				<div>
					<div class="times-container">
						<div class="times-section">
																	<!---->
							<kendo-dateinput
																						(valueChange)="onDateChanged('start', $event)"
									[format]="this.model.dateFormat +' '+this.model.timeFormat"
									[(value)]="model.start"></kendo-dateinput>
						</div>
						<div class="times-section">
																	<!---->
							<kendo-dateinput
																			(valueChange)="onDateChanged('end', $event)"
																						[disabled]="model.locked"
									[format]="this.model.dateFormat +' '+this.model.timeFormat"
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
							   data-container="body"
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
	`,
	changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DateRangeSelectorComponent extends Dialog implements OnInit {
	@Input() data: any;

	public model: DateRangeSelectorModel;
	dataSignature: string;
	activeRangeEnd = 'start';

	constructor(
		private dialogService: DialogService,
		private translatePipe: TranslatePipe) {
		super();
	}

	ngOnInit() {
		this.model = R.clone(this.data.dateRangeSelectorModel);
		this.dataSignature = JSON.stringify(this.model);

		this.buttons.push({
			name: 'save',
			icon: 'floppy',
			text: 'Save',
			show: () => true,
			disabled: () => !this.canSave(),
			type: DialogButtonType.CONTEXT,
			action: this.save.bind(this)
		});

		this.buttons.push({
			name: 'cancel',
			icon: 'ban',
			text: 'Cancel',
			show: () => true,
			type: DialogButtonType.CONTEXT,
			action: this.cancelCloseDialog.bind(this)
		})

		jQuery('[data-toggle="popover"]')
			.popover({
				container: 'body'
			});

		setTimeout(() => {
			this.setTitle(this.getModalTitle());
		});
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
		this.onAcceptSuccess(this.model);
	}

	/**
	 * Close the Dialog but first it verify is not Dirty
	 */
	public cancelCloseDialog(): void {
		if (this.isDirty()) {
			this.dialogService.confirm(
				this.translatePipe.transform(
					'GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED'
				),
				this.translatePipe.transform(
					'GLOBAL.CONFIRMATION_PROMPT.UNSAVED_CHANGES_MESSAGE'
				)
			)
				.subscribe((data: any) => {
					if (data.confirm === DialogConfirmAction.CONFIRM) {
						super.onCancelClose();
					}
				});
		} else {
			super.onCancelClose();
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
	 * On date changed.
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
		// if duration lock is ON, then recalculate the end date based on the current duration values.
		if (this.model.locked) {
			const duration = DateUtils.getDurationPartsAmongDates(this.model.start, value);
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
		} else { // if duration lock is OFF, then recalculate the duration based on the new start/end values.
			if (this.model.start && this.model.end) {
				this.model.duration = DateUtils.getDurationPartsAmongDates(start, end);
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
	 * @returns {string}
	 */
	getModalTitle(): string {
		const startMessage = this.translatePipe.transform('TASK_MANAGER.EDIT.SELECT_START_DATE');
		const endMessage = this.translatePipe.transform('TASK_MANAGER.EDIT.SELECT_END_DATE');

		if (this.model.locked) {
			return startMessage;
		}
		return this.activeRangeEnd === 'start' ? startMessage : endMessage;
	}

	/**
	 * User Dismiss Changes
	 */
	public onDismiss(): void {
		this.cancelCloseDialog();
	}
}
