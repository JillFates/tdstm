import { Component, Input, EventEmitter, Output } from '@angular/core';

@Component({
	selector: 'tds-fixed-checkbox',
	template: `
        <div class="tds-fixed-checkbox">
			<div class="clr-control-container">
				<div class="clr-checkbox-wrapper" style="display: flex">
					<span class="checkboxContainer">
                                <div class="clr-form-control unset-margin-top">
                                    <div class="clr-control-container">
                                        <div class="clr-checkbox-wrapper">
											<input
												type="checkbox"
												[name]="controlName"
												[id]="controlName"
												[checked]="!!isFixed"
												[disabled]="!model || isTeamItemSelected()"
												(change)="onChangeFixed($event)"
												class="pointer" />
                                            <label [for]="controlName" class="clr-control-label clr-control-label-sm inline"><span class="left-label-pad">Fixed</span></label>
                                        </div>
                                    </div>
                                </div>
                    </span>
				</div>
            </div>
        </div>
	`
})
export class TDSFixedCheckboxComponent {
	@Output() isFixedChange = new EventEmitter<number>();
	@Input('model') model: any;
	@Input('isFixed') isFixed: number;
	@Input('name') controlName: string;

	/**
	 * Determine if current element belongs to teams category
	 */
	public isTeamItemSelected(): boolean {
		return this.model && this.model.toString().startsWith('@');
	}

	/**
	 * Notifiy to host whenever the checkbox change its value
	 * @param {any} event with checkbox current value
	 */
	public onChangeFixed(event: any): void {
		this.isFixed = event.target.checked ? 1 : 0;
		this.isFixedChange.next(this.isFixed);
	}
}
