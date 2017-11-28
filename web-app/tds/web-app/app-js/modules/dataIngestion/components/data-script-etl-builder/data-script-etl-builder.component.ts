import { Component, Output, EventEmitter, AfterViewInit } from '@angular/core';
import { UIExtraDialog, UIDialogService } from '../../../../shared/services/ui-dialog.service';
import { DataScriptSampleDataComponent } from '../data-script-sample-data/data-script-sample-data.component';
import { DataScriptConsoleComponent } from '../data-script-console/data-script-console.component';

@Component({
	selector: 'data-script-etl-builder',
	templateUrl: '../tds/web-app/app-js/modules/dataIngestion/components/data-script-etl-builder/data-script-etl-builder.component.html'
})
export class DataScriptEtlBuilderComponent extends UIExtraDialog implements AfterViewInit {

	collapsed = {
		code: true,
		sample: false,
		transformed: false
	};

	code = '';

	constructor(private dialogService: UIDialogService) {
		super('#etlBuilder');
	}

	protected cancelCloseDialog(): void {
		this.dismiss();
	}

	onCodeChange(event: { newValue: string, oldValue: string }) {
		console.log(event);
	}

	protected toggleSection(section: string) {
		this.collapsed[section] = !this.collapsed[section];
	}

	protected onLoadSampleData(): void {
		this.dialogService.extra(DataScriptSampleDataComponent, [])
			.then(() => console.log('ok'))
			.catch(() => console.log('still ok'));
	}

	protected onViewConsole(): void {
		this.dialogService.extra(DataScriptConsoleComponent, [])
			.then(() => console.log('ok'))
			.catch(() => console.log('still ok'));
	}

	ngAfterViewInit(): void {
		setTimeout(() => {
			this.collapsed.code = false;
		}, 300);
	}

}