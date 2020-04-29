// Angular
import {
	Component,
	OnInit,
	Input
} from '@angular/core';

import {Dialog} from 'tds-component-library';
// Other
import {Subject} from 'rxjs/Subject';
import {TranslatePipe} from '../../../../../../shared/pipes/translate.pipe';

@Component({
	selector: 'regenerate-modal',
	templateUrl: 'regenerate.component.html'
})
export class RegenerateComponent extends Dialog implements OnInit {
	@Input() data: any;

	public modalTitle: string;
	private dataSignature: string;
	protected isUnique = true;
	private providerName = new Subject<String>();

	public dependencyType = [];
	public dependencyStatus = [];
	public depGrpCrt: any;
	public saveDefault = false;

	constructor(
		private translatePipe: TranslatePipe,
	) {
		super();
	}

	ngOnInit(): void {
		if (this.data) {
			this.depGrpCrt = this.data.depGrpCrt;
			this.data.dependencyType.forEach(( item: string) => {
				this.dependencyType.push({
						label: item,
						checked: this.data.depGrpCrt.connectionTypes.includes(item)
					});
				});
			this.data.dependencyStatus.forEach( (item: string) => {
				this.dependencyStatus.push({
					label: item,
					checked: this.data.depGrpCrt.statusTypes.includes(item)
				});
			});
		}
	}

	/**
	 *	Close Dialog without sending data
	 * */
	public cancelCloseDialog(): void {
			this.onCancelClose();
	}

	/**
	 * User Dismiss Changes
	 */
	public onDismiss(): void {
		this.cancelCloseDialog();
	}

	/**
	 * Creates a variable to set all the selected item on both lists
	 * and sends it back to the component that call the dialog
	 * */
	regenerate() {
		const data = {
			dependencyType: this.dependencyType.filter( item => item.checked).map(item => item.label),
			dependencyStatus: this.dependencyStatus.filter( item => item.checked).map(item => item.label),
			saveDefault: this.saveDefault
		}
		// regenerate stuff
		super.onCancelClose(data);
	}
}
