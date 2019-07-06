// Angular
import {Component, Input} from '@angular/core';

@Component({
	selector: 'tds-bundle-steps',
	templateUrl: 'bundle-steps.component.html'
})
export class BundleStepsComponent {
	@Input() bundleSteps: any;
	public colSize = 2;
	public showFrom = 0;
	public elementsToShow = 5;

	constructor() {
		console.log('On constructor');
	}

	onBack(): void {
		if (this.showFrom > 0) {
			this.showFrom -= 1;
		}
	}

	onNext(): void {
		if (this.bundleSteps &&  (this.showFrom + this.elementsToShow + 1) <= this.bundleSteps.columnsLength)  {
			this.showFrom += 1;
		}
	}

	getColumns(row: any): any {
		if (!this.bundleSteps) {
			return [];
		}

		return row.slice(this.showFrom, this.showFrom + this.elementsToShow);
	}
}
