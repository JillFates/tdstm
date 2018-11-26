/*
	Control that wrap up a button and a font awesome icon
    This relay on the btn suit bootstrap classes
*/
import {Component, Input, HostBinding, ChangeDetectionStrategy} from '@angular/core';

@Component({
	selector: '[tds-button]',
	template: `
		<div class="tds-button">
			<i *ngIf="icon" [ngClass]='iconClasses'></i>
			<ng-content></ng-content>
		</div>
	`,
	changeDetection: ChangeDetectionStrategy.OnPush
})
export class TDSButton {
	@HostBinding('attr.type') type = 'button';
	@Input() icon = '';

	get iconClasses() {
		return {
			'fa': true,
			'fa-fw': true,
			[`fa-${this.icon}`]: true
		}
	}
}