/**
 * Cross Browser solution when paste information into an input element (input, textarea)
 */
import {Directive, ElementRef, HostListener, Input, Output, EventEmitter} from '@angular/core';
import {NgControl} from '@angular/forms';

@Directive({
	selector: '[input-paste]'
})

export class InputPasteDirective {

	@Input('waitTime') waitTime = 500;
	@Output('onPaste') onPaste: EventEmitter<any> = new EventEmitter();

	constructor(private el: ElementRef, private control: NgControl) {
	}

	@HostListener('paste', ['$event']) onEvent($event) {
		$event.preventDefault();
		let data = $event.clipboardData.getData('text');
		setTimeout(() => {
			this.control.control.setValue(data);
			this.onPaste.emit(data);
		}, this.waitTime);
	}

}