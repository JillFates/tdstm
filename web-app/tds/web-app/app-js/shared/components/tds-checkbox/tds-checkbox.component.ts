import {Component, OnInit, Input, Output, EventEmitter, ViewChild, ElementRef} from '@angular/core';
import {CheckboxStates} from './model/tds-checkbox.model';

@Component({
	selector: 'tds-checkbox',
	templateUrl: '../tds/web-app/app-js/shared/components/tds-checkbox/tds-checkbox.component.html'
})

export class TDSCheckboxComponent implements OnInit {
	@Input() hasThirdState: boolean;
	@Output() changeState: EventEmitter<CheckboxStates> = new EventEmitter();
	@ViewChild('tdsCheckbox') tdsCheckbox: ElementRef;
	currentState: CheckboxStates;

	ngOnInit() {
		this.currentState = CheckboxStates.unchecked;
	}

	onChange(currentState: CheckboxStates): void {
		try {
			const stateResult = this.transitionState(currentState);
			this.transitionCheckValue(currentState);
			this.setAttribute('indeterminate', (stateResult === CheckboxStates.indeterminate))
			this.changeState.emit(this.currentState);
		} catch (error) {
			console.error(error.message || error);
		}
	}

	private setAttribute(attribute: string, value: boolean): void {
		this.tdsCheckbox.nativeElement[attribute] = value;
	}

	private transitionState(currentState: CheckboxStates): CheckboxStates {
		switch (currentState)  {
			case CheckboxStates.unchecked:
				this.setCurrentState(CheckboxStates.checked);
				break;

			case CheckboxStates.checked:
				this.setCurrentState(this.hasThirdState ? CheckboxStates.indeterminate : CheckboxStates.unchecked);
				break;

			case CheckboxStates.indeterminate:
				this.setCurrentState(CheckboxStates.unchecked);
				break;

			default:
				throw new Error('Invalid tds checkbox state');
		}

		return this.currentState;
	}

	private transitionCheckValue(currentState: CheckboxStates): CheckboxStates {
		switch (currentState)  {
			case CheckboxStates.unchecked:
				this.setAttribute('checked', true);
				break;

			case CheckboxStates.checked:
			case CheckboxStates.indeterminate:
				this.setAttribute('checked', false);
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