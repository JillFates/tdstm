import { Component } from '@angular/core';

@Component({
	selector: 'popup-validate-expression',
	templateUrl: 'popup-validate-expression.component.html'
})

export class PopupValidateExpressionComponent {
	public show = false;

	public onToggle(): void {
		this.show = !this.show;
	}
}
