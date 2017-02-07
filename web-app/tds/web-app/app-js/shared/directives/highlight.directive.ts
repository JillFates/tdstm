/**
 * Created by Jorge Morayta on 2/7/2017.
 */

import { Directive, ElementRef, HostListener, Input } from '@angular/core';

@Directive(
    { selector: '[tds-highlight]' }
)

export class HighlightDirective {

    @Input() highlightColor: string;

    constructor(private el: ElementRef) {
        el.nativeElement.style.backgroundColor = this.highlightColor;
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