/**
 * UI Dialog Directive works as a placeholder for any dialog being initiaded
 */

import {Component, Input} from '@angular/core';

declare var jQuery: any;

@Component({
	selector: 'tds-ui-svg-icon',
	template: `
        <svg [ngStyle]="{'height':svgHeight + 'px', 'width': svgWidth + 'px'}" class='tds-svg-icons' viewBox='0 0 115 115' xmlns='http://www.w3.org/2000/svg' xmlns:xlink='http://www.w3.org/1999/xlink'>
            <image x='0' y='8' height='110px' width='110px' fill='#1f77b4' xmlns:xlink='http://www.w3.org/1999/xlink' [attr.xlink:href]="'/tdstm/icons/svg/' + svgName + '.svg'"></image>
        </svg>
	`
})
export class UISVGIconDirectiveDirective {
	@Input('name') svgName: string;
	@Input('height') svgHeight: string;
	@Input('width') svgWidth: string;
}