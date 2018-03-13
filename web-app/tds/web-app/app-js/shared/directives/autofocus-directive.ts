import {Directive, AfterViewInit, ElementRef} from '@angular/core';

@Directive({ selector: '[tds-autofocus]' })
export class UIAutofocusDirective implements AfterViewInit {
	constructor(private el: ElementRef) {
	}
	ngAfterViewInit() {
		// we need to delay because the bootstrap effect displaying modals
		setTimeout(() => this.el.nativeElement.focus(), 500);
	}
}