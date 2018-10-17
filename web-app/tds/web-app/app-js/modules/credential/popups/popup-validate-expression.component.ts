import { Component } from '@angular/core';

@Component({
	selector: 'popup-validate-expression',
	templateUrl: '../tds/web-app/app-js/modules/credential/popups/popup-validate-expression.component.html',
	styles: [`
		div { width: 320px; padding: 10px; }
    `]
})

export class PopupValidateExpressionComponent {
	private show = false;

	public onToggle(): void {
		this.show = !this.show;
	}
}