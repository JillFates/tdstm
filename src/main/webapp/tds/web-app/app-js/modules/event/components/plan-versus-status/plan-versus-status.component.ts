// Angular
import {Component, OnInit, Input, Output, EventEmitter, OnChanges, SimpleChanges} from '@angular/core';

@Component({
	selector: 'tds-plan-versus-status',
	templateUrl: 'plan-versus-status.component.html'
})
export class PlanVersusStatusComponent implements OnChanges {
	@Input() currentProgress = 0;
	@Output() changeProgress: EventEmitter<number> = new EventEmitter<number>();
	public progress = 0;
	public showEditControl = false;

	ngOnChanges(changes: SimpleChanges): void {
		if (changes && changes.currentProgress) {
			this.progress = changes.currentProgress.currentValue;
		}
	}

	/**
	 * Save the changes
	 */
	protected onSave(value: string): void {
		this.changeProgress.emit(this.progress);
	}

	public onClickChart() {
		this.showEditControl = !this.showEditControl;
	}
}
