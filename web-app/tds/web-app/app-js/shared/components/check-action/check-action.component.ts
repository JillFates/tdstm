/**
 * Created by Jorge Morayta on 01/22/2018.
 */

import {Component, DoCheck, EventEmitter, Input, Output, KeyValueDiffers } from '@angular/core';
import {CHECK_ACTION} from './model/check-action.model';

@Component({
	selector: 'tds-check-action',
	templateUrl: '../tds/web-app/app-js/shared/components/check-action/check-action.component.html',
	styles: [`
        i.fa {
            padding-right: 0px;
        }
        i.red {
            color: #d33724;
            font-weight: bold;
        }
        i.green {
            color: green;
            font-weight: bold;
        }
	`]
})

export class CheckActionComponent implements DoCheck {
	@Output('onClick') onCallback: EventEmitter<any> = new EventEmitter();
	@Input('model') model: any;
	@Input('name') name: string;
	@Input('disabled') disabled = false;
	private checkActionModel = CHECK_ACTION;

	private differ: any;
	private dataSignature = '';

	constructor(private differs: KeyValueDiffers) {
		this.differ = differs.find({}).create(null);
	}

	ngDoCheck(): void {
		let changes = this.differ.diff(this.model);
		if (changes) {
			changes.forEachChangedItem(r => {
				if (this.isDirty() && r.key === 'value') {
					this.dataSignature = JSON.stringify(r.currentValue);
					this.model.state = CHECK_ACTION.UNKNOWN;
				}
			});
		}
	}

	/**
	 * Verify the Object has not changed
	 * @returns {boolean}
	 */
	private isDirty(): boolean {
		return this.dataSignature !== JSON.stringify(this.model.value);
	}

	/**
	 * Determine is buttons should be disabled.
	 * @returns {boolean}
	 */
	private preventClick(): boolean {
		return this.model.state === CHECK_ACTION.VALID || this.disabled;
	}

	/**
	 * Return the event so it can be managed by the Parent
	 */
	protected onCheckThumbBindAction(): void {
		if (!this.preventClick()) {
			this.onCallback.emit();
		}
	}
}