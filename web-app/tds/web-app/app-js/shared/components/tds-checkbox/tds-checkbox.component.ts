import {Component, OnInit, Input, Output, EventEmitter, ViewChild, ElementRef, Renderer2} from '@angular/core';
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

	constructor(private renderer2: Renderer2) {
	}

	ngOnInit() {
		this.currentState = CheckboxStates.unchecked;
		console.log('on init');

		if (this.hasThirdState) {
			this.renderer2.setAttribute(this.tdsCheckbox.nativeElement, 'indeterminate', 'true');
			console.log('Setting indeterminated');
		}
	}

	onChange(currentState: CheckboxStates): void {
		this.transitionState(currentState);
		console.log('The state is');
		console.log(this.currentState);
	}

	private transitionState(currentState: CheckboxStates): void {
		switch (currentState)  {
			case CheckboxStates.unchecked:
				this.setCurrentState(CheckboxStates.checked);
				break;

			case CheckboxStates.checked:
				this.setCurrentState(CheckboxStates.indeterminated);
				break;

			case CheckboxStates.indeterminated:
				this.setCurrentState(CheckboxStates.unchecked);
				break;

			default:
				throw new Error('Invalid tds checkbox state');
		}
	}

	private setCurrentState(newState: CheckboxStates): void {
		this.currentState = newState;
	}

}