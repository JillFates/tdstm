/**
 * Created by Jorge Morayta on 2/7/2017.
 */

import {Directive, ElementRef, HostListener, Input} from '@angular/core';

@Directive(
	{selector: '[tds-highlight]'}
)

export class HighlightDirective {

	// Using an Input allow you to change the name of the binding externally, so, it becomes public to the 'scope'
	@Input('elementHighlight') highlightColor: string;

	constructor(private el: ElementRef) {
	}

	@HostListener('mouseenter') onMouseEnter() {
		this.highlight(this.highlightColor);
	}

	@HostListener('mouseleave') onMouseLeave() {
		this.highlight(null);
	}

	private highlight(color: string) {
		this.el.nativeElement.style.backgroundColor = color;
	}

}