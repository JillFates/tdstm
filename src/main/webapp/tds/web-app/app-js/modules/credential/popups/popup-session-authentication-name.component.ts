import { Component } from '@angular/core';

@Component({
	selector: 'popup-session-authentication-name',
	templateUrl: 'popup-session-authentication-name.component.html'
})

export class PopupSessionAuthenticationNameComponent {
	public show = false;

	public onToggle(): void {
		this.show = !this.show;
	}
}
