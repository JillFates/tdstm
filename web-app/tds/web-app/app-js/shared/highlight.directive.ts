/**
 * Created by Jorge Morayta on 2/5/2017.
 */

import { Directive, ElementRef } from '@angular/core';

@Directive({
    selector: '[highlight]'
})

export class HighlightDirective {
    constructor(el: ElementRef) {
        el.nativeElement.style.backgroundColor = 'gold';
        console.log('* AppRoot highlight called for ${el.nativeElement.tagName}');
    }
}