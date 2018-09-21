import {Component, ViewChild, ElementRef, OnInit} from '@angular/core';
import { SelectionRange } from '@progress/kendo-angular-dateinputs';

import {UIExtraDialog} from '../../services/ui-dialog.service';
import { UIPromptService} from '../../directives/ui-prompt.directive';
import { DecoratorOptions} from '../../model/ui-modal-decorator.model';
import {DateRangeSelectorModel} from './model/date-range-selector.model';
import {DateUtils, DurationParts} from '../../utils/date.utils';

@Component({
	selector: 'tds-date-range-selector',
	templateUrl: '../tds/web-app/app-js/shared/components/date-range-selector/date-range-selector.component.html'
})
export class DateRangeSelectorComponent extends UIExtraDialog  implements  OnInit {
	dataSignature: string;
	title: string;
	modalOptions: DecoratorOptions;
	durationParts: DurationParts = { days: null, minutes: null, hours: null };

	constructor(
		public model: DateRangeSelectorModel,
		private promptService: UIPromptService) {

		super('#date-range-selector-component');
		this.modalOptions = { isDraggable: true, isResizable: false, isCentered: false };
	}

	ngOnInit() {
		this.dataSignature = JSON.stringify(this.model);
		this.durationParts =  DateUtils.getDurationPartsAmongDates(this.model.start, this.model.end);
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

	onValueChange(event): void {
		setTimeout(() => {
			const {start, end} = this.model;
			this.durationParts = DateUtils.getDurationPartsAmongDates(start, end);
		}, 0);
	}
}