import { Component } from '@angular/core';

@Component({
	selector: 'popup-polling',
	templateUrl: 'popup-polling.component.html',
	styles: [`
		div { width: 320px; padding: 10px; }
    `]
})

export class PopupPollingComponent {
	public show = false;

	public onToggle(): void {
		this.show = !this.show;
	}
}