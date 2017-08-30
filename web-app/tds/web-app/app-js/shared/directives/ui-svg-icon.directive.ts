/**
 * UI Dialog Directive works as a placeholder for any dialog being initiaded
 */

import {Component, Input} from '@angular/core';

declare var jQuery: any;

@Component({
	selector: 'tds-ui-svg-icon',
	templateUrl: '../tds/web-app/app-js/shared/directives/ui-svg-icon.directive.html'
})
export class UISVGIconDirectiveDirective {
	@Input('name') svgName: string;
	@Input('height') svgHeight: string;
	@Input('width') svgWidth: string;
}