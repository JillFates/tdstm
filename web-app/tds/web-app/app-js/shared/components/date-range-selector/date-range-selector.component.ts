import {Component, ViewChild, ElementRef, OnInit} from '@angular/core';

import {UIExtraDialog} from '../../services/ui-dialog.service';
import { UIPromptService} from '../../directives/ui-prompt.directive';
import { DecoratorOptions} from '../../model/ui-modal-decorator.model';
import {DateRangeSelectorModel} from './model/date-range-selector.model';
import {DateUtils, DurationParts} from '../../utils/date.utils';
import { SelectionRange } from '@progress/kendo-angular-dateinputs';

@Component({
	selector: 'tds-date-range-selector',
	templateUrl: '../tds/web-app/app-js/shared/components/date-range-selector/date-range-selector.component.html'
})
export class DateRangeSelectorComponent extends UIExtraDialog  implements  OnInit {
	dataSignature: string;
	title: string;
	modalOptions: DecoratorOptions;
	activeRangeEnd = 'start';

	constructor(
		public model: DateRangeSelectorModel,
		private promptService: UIPromptService) {

		super('#date-range-selector-component');
		this.modalOptions = { isDraggable: true, isResizable: false, isCentered: false };
	}

	ngOnInit() {
		this.dataSignature = JSON.stringify(this.model);
		// const {start, end, format, locked, duration} = this.model;
		// this.model = {start: start ? start : new Date(), end: end ? end : new Date(), format, locked: locked || false, duration};
		// this.durationParts =  duration; // DateUtils.getDurationPartsAmongDates(this.model.start, this.model.end);
	}
	/**
	 * Verify the Object has not changed
	 * @returns {boolean}
	 */
	protected isDirty(): boolean {
		return this.dataSignature !== JSON.stringify(this.model);
	}

	/**
	 * Verify if user filled all required fiedls
	 * @returns {boolean}
	 */
	protected canSave(): boolean {
		return this.isDirty();
	}

	save(): void {
		this.close(this.model);
	}

	/**
	 * Close the Dialog but first it verify is not Dirty
	 */
	protected cancelCloseDialog(): void {
		if (this.isDirty()) {

			this.promptService.open(
				'Confirmation Required',
				'You have changes that have not been saved. Do you want to continue and lose those changes?',
				'Confirm', 'Cancel')
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
		const {start, end, locked, format, duration} = this.model;
		let newStart = null;
		let newEnd = null;
		// preserve start hours
		const startHours = start.getHours();
		const startMinutes = start.getMinutes();

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
			newStart = DateUtils.increment(seed, [{value: startHours, unit: 'hours'}, {value: startMinutes, unit: 'minutes'}] );
			newEnd = DateUtils.increment(newStart, [{value: days, unit: 'days'}, {value: hours, unit: 'hours'}, {value: minutes, unit: 'minutes'}]);

			this.model = {start: newStart, end: newEnd, locked, format, duration};
		} else {
			this.model = {start: range.start, end: range.end, locked, format, duration: DateUtils.getDurationPartsAmongDates(range.start, range.end) };
		}
	}

	updateEstimatedFinish(unit: 'days' | 'hours' | 'minutes'): void {
		const {start, format, locked, duration} = this.model;

		const originalParts = DateUtils.getDurationPartsAmongDates(this.model.start, this.model.end);
		const diff = duration[unit] - originalParts[unit];

		const end =  DateUtils.increment(this.model.end, [{value: diff, unit}]);

		this.model = {start, end, format, locked, duration};
	}

	getTitle(): string {
		const startMessage = 'Select a Start Date';
		const endMessage = 'Select a Finish Date';

		if (this.model.locked) {
			return startMessage;
		}
		return this.activeRangeEnd === 'start' ? startMessage : endMessage;
	}

}