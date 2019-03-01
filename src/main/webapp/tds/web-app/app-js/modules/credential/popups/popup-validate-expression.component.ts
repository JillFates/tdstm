import { Component } from '@angular/core';

@Component({
	selector: 'popup-validate-expression',
	templateUrl: 'popup-validate-expression.component.html',
	styles: [`
		div { width: 320px; padding: 10px; }
    `]
})

export class PopupValidateExpressionComponent {
	public show = false;

	public onToggle(): void {
		this.show = !this.show;
	}
}