/**
 * Cross Browser solution when paste information into an input element (input, textarea)
 */
import {Directive, HostListener, Input, Output, EventEmitter} from '@angular/core';

@Directive({
	selector: '[input-paste]'
})

export class InputPasteDirective {
	@Input('waitTime') waitTime = 500;
	@Output('onPaste') onPaste: EventEmitter<any> = new EventEmitter();

	@HostListener('paste', ['$event']) onEvent($event) {
		$event.preventDefault();
		let data = $event.clipboardData.getData('text');
		setTimeout(() => {
			this.onPaste.emit(data);
		}, this.waitTime);
	}

}