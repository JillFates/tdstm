import {Component, OnInit, Input, Output, EventEmitter, ViewChild, ElementRef} from '@angular/core';
import {CheckboxStates} from './model/tds-checkbox.model';

const CHECKED_ATTRIBUTE = 'checked';
const INDETERMINATE_ATTRIBUTE = 'indeterminate';

@Component({
	selector: 'tds-checkbox',
	templateUrl: '../tds/web-app/app-js/shared/components/tds-checkbox/tds-checkbox.component.html'
})

export class TDSCheckboxComponent implements OnInit {
	@Input() hasThirdState: boolean;
	@Output() changeState: EventEmitter<CheckboxStates> = new EventEmitter();
	@ViewChild('tdsCheckbox') tdsCheckbox: ElementRef;
	currentState: CheckboxStates;
	private transitionHandler: Function;

	ngOnInit() {
		// start off unchecked
		this.currentState = CheckboxStates.unchecked;
		// depending of flag it decides the strategy to handle transitions
		this.transitionHandler = this.hasThirdState ? this.transitionThreeStates : this.transitionTwoStates;
	}

	onChange(currentState: CheckboxStates): void {
		try {
			// notify to the host the transition
			this.changeState.emit(this.transitionHandler.bind(this)(currentState));
		} catch (error) {
			console.error(error.message || error);
		}
	}

	// handle three states transition
	private transitionThreeStates(currentState: CheckboxStates): CheckboxStates {
		switch (currentState)  {
			case CheckboxStates.unchecked:
				this.transitToChecked();
				break;

			case CheckboxStates.checked:
				this.transitToIndeterminate();
				break;

			case CheckboxStates.indeterminate:
				this.transitToUnchecked();
				break;

			default:
				throw new Error('Invalid tds checkbox state');
		}

		return this.currentState;
	}

	// handle two states transition
	private transitionTwoStates(currentState: CheckboxStates): CheckboxStates {
		switch (currentState)  {
			case CheckboxStates.unchecked:
				this.transitToChecked();
				break;

			case CheckboxStates.checked:
				this.transitToUnchecked();
				break;

			default:
				throw new Error('Invalid tds checkbox state');
		}

		return this.currentState;
	}

	// transitions among states
	private transitToChecked(): void {
		this.setCurrentState(CheckboxStates.checked);
		this.setAttribute(CHECKED_ATTRIBUTE, true);
		this.setAttribute(INDETERMINATE_ATTRIBUTE, false);
	}

	private transitToIndeterminate(): void {
		this.setCurrentState(CheckboxStates.indeterminate);
		this.setAttribute(CHECKED_ATTRIBUTE, false);
		this.setAttribute(INDETERMINATE_ATTRIBUTE, true);
	}

	private transitToUnchecked(): void {
		this.setCurrentState(CheckboxStates.unchecked);
		this.setAttribute(CHECKED_ATTRIBUTE, false);
		this.setAttribute(INDETERMINATE_ATTRIBUTE, false);
	}

	// helpers
	private setAttribute(attribute: string, value: boolean): void {
		this.tdsCheckbox.nativeElement[attribute] = value;
	}

	private setCurrentState(newState: CheckboxStates): void {
		this.currentState = newState;
	}

}