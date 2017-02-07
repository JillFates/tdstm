/**
 * Created by Jorge Morayta on 2/7/2017.
 */

import { Directive, ElementRef, Input } from '@angular/core';

@Directive(
    { selector: '[tds-highlight]' }
)

export class HighlightDirective {
    constructor(el: ElementRef) {
        el.nativeElement.style.backgroundColor = 'yellow';
    }
}