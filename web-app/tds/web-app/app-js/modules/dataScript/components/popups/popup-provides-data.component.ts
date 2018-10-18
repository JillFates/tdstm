import { Component } from '@angular/core';

@Component({
	selector: 'popup-provides-data',
	templateUrl: '../../components/popups/popup-provides-data.component.html',
	styles: [`
		div { width: 320px; padding: 10px; }
    `]
})

export class PopupProvidesDataComponent {
	private show = false;

	public onToggle(): void {
		this.show = !this.show;
	}
}