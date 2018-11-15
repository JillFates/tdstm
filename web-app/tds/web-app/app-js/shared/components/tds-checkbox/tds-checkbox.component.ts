import {Component, OnInit, Input, Output, EventEmitter, ViewChild, ElementRef} from '@angular/core';
import { Subject } from 'rxjs/Subject';
import { CheckboxState, CheckboxStates} from './model/tds-checkbox.model';

const CHECKED_ATTRIBUTE = 'checked';
const INDETERMINATE_ATTRIBUTE = 'indeterminate';

@Component({
	selector: 'tds-checkbox',
	templateUrl: '../tds/web-app/app-js/shared/components/tds-checkbox/tds-checkbox.component.html'
})
export class TDSCheckboxComponent implements OnInit {
	@Input() hasThirdState: boolean;
	@Input() setStateSubject: Subject<CheckboxState>;
	@Output() changeState: EventEmitter<CheckboxState> = new EventEmitter();
	@ViewChild('tdsCheckbox') tdsCheckbox: ElementRef;
	private currentState: CheckboxStates;
	private transitionHandler: Function;

	ngOnInit() {
		this.setStateSubject
			.subscribe((state: CheckboxState) => {
				switch (state.current) {
					case CheckboxStates.checked:
						this.transitToChecked();
						break;
					case CheckboxStates.unchecked:
						this.transitToUnchecked();
						break;
				}
				if (state.affectItems) {
					this.changeState.emit(state)
				}
			});

		// start off unchecked
		this.currentState = CheckboxStates.unchecked;
		// depending of flag it decides the strategy to handle transitions
		this.transitionHandler = this.hasThirdState ? this.transitionThreeStates.bind(this) : this.transitionTwoStates.bind(this);
	}

	onChange(): void {
		try {
			// notify to the host the transition
			this.changeState.emit(this.transitionHandler(true));
		} catch (error) {
			console.error(error.message || error);
		}
	}

	// handle three states transition
	private transitionThreeStates(affectItems: boolean): CheckboxState {
		switch (this.currentState)  {
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

		return {current: this.currentState, affectItems};
	}

	// handle two states transition
	private transitionTwoStates(affectItems: boolean): CheckboxState {
		switch (this.currentState)  {
			case CheckboxStates.unchecked:
				this.transitToChecked();
				break;

			case CheckboxStates.checked:
				this.transitToUnchecked();
				break;

			default:
				throw new Error('Invalid tds checkbox state');
		}

		return {current: this.currentState, affectItems};
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