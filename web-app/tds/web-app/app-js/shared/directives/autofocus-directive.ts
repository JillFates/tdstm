import {Directive, OnInit, AfterViewInit, ElementRef, Renderer2} from '@angular/core';

// Set the focus to the attached host element
@Directive({ selector: '[tds-autofocus]' })
export class UIAutofocusDirective implements OnInit, AfterViewInit {
	constructor(private el: ElementRef, private renderer: Renderer2) {
	}

	ngOnInit() {
		// mark the host element as focusable
		this.renderer.setAttribute(this.el.nativeElement, 'tabindex',  '0');
	}

	ngAfterViewInit() {
		// we need to delay because the bootstrap effect displaying modals
		setTimeout(() => this.el.nativeElement.focus(), 500);
	}
}