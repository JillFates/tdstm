/**
 *  The component is being used dynamically, some vars will show as not being used or referenced but they could be part
 *  of the GSP
 *
 *  Use angular/views/TheAssetType as reference
 */
import { Component, Inject, OnInit } from '@angular/core';
import { UIActiveDialogService } from '../../../../shared/services/ui-dialog.service';

import { PreferenceService } from '../../../../shared/services/preference.service';

export function ApplicationEditComponent(template: string, editModel: any): any {
	@Component({
		selector: 'application-edit',
		template: template,
		providers: [
			{ provide: 'model', useValue: editModel }
		]
	})
	class ApplicationShowComponent {
		constructor( @Inject('model') private model: any, private activeDialog: UIActiveDialogService, private preference: PreferenceService) {}

		ngOnInit(): void {
			console.log('Loading application-edit.component');
		}

		shufflePerson(control1: string, control2: string) {
			console.log('Swapping controls');
		}
	}

	return ApplicationShowComponent;
}