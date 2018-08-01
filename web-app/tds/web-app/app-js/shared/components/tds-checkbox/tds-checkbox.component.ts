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
		this.currentState = CheckboxStates.unchecked;
		this.transitionHandler = this.hasThirdState ? this.transitionThreeStates.bind(this) : this.transitionTwoStates.bind(this);
	}

	onChange(currentState: CheckboxStates): void {
		try {
			this.changeState.emit(this.transitionHandler(currentState));
		} catch (error) {
			console.error(error.message || error);
		}
	}

	private setAttribute(attribute: string, value: boolean): void {
		this.tdsCheckbox.nativeElement[attribute] = value;
	}

	private transitionThreeStates(currentState: CheckboxStates): CheckboxStates {
		switch (currentState)  {
			case CheckboxStates.unchecked:
				this.setCurrentState(CheckboxStates.checked);
				this.setAttribute(INDETERMINATE_ATTRIBUTE, false)
				this.setAttribute(CHECKED_ATTRIBUTE, true);
				break;

			case CheckboxStates.checked:
				this.setCurrentState(CheckboxStates.indeterminate);
				this.setAttribute(INDETERMINATE_ATTRIBUTE, true)
				this.setAttribute(CHECKED_ATTRIBUTE, false);
				break;

			case CheckboxStates.indeterminate:
				this.setCurrentState(CheckboxStates.unchecked);
				this.setAttribute(INDETERMINATE_ATTRIBUTE, false)
				this.setAttribute(CHECKED_ATTRIBUTE, false);
				break;

			default:
				throw new Error('Invalid tds checkbox state');
		}

		return this.currentState;
	}

	private transitionTwoStates(currentState: CheckboxStates): CheckboxStates {
		switch (currentState)  {
			case CheckboxStates.unchecked:
				this.setCurrentState(CheckboxStates.checked);
				this.setAttribute(CHECKED_ATTRIBUTE, true);
				break;

			case CheckboxStates.checked:
				this.setCurrentState(CheckboxStates.unchecked);
				this.setAttribute(CHECKED_ATTRIBUTE, false);
				break;

			case CheckboxStates.indeterminate:
				this.setCurrentState(CheckboxStates.unchecked);
				this.setAttribute(CHECKED_ATTRIBUTE, false);
				break;

			default:
				throw new Error('Invalid tds checkbox state');
		}

		return this.currentState;
	}

	private setCurrentState(newState: CheckboxStates): void {
		this.currentState = newState;
	}

}