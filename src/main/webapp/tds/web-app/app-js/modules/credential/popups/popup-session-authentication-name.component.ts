import { Component } from '@angular/core';

@Component({
	selector: 'popup-session-authentication-name',
	templateUrl: 'popup-session-authentication-name.component.html',
	styles: [`
		div { width: 390px; padding: 10px; }
    `]
})

export class PopupSessionAuthenticationNameComponent {
	public show = false;

	public onToggle(): void {
		this.show = !this.show;
	}
}