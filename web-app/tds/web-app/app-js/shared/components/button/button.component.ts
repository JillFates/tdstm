/*
	Control that wrap up a button and a font awesome icon
    This relay on the btn suit bootstrap classes
*/
import {Component, Input, HostBinding} from '@angular/core';

@Component({
	selector: 'tds-button',
	template: `
		<div (click)="onClick($event)">
			<i *ngIf="icon" [ngClass]='iconClasses'></i>
			<ng-content></ng-content>
		</div>
	`
})
export class TDSButton {
	@HostBinding('class.disabled') isDisabled = false;
	@Input() icon = '';
	@Input() id = '';

	@Input('disabled')
	set disabled(value: boolean) {
		this.isDisabled = value;
	}

	get iconClasses() {
		return {
			'fa': true,
			'fa-fw': true,
			[`fa-${this.icon}`]: true
		}
	}

	onClick(event) {
		if (this.isDisabled) {
			event.stopPropagation();
		}
	}
}