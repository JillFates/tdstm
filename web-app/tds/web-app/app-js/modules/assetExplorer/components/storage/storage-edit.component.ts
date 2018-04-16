/**
 *  The component is being used dynamically, some vars will show as not being used or referenced but they could be part
 *  of the GSP
 *
 *  Use angular/views/TheAssetType as reference
 */
import { Component, Inject, OnInit } from '@angular/core';
import { UIActiveDialogService } from '../../../../shared/services/ui-dialog.service';
import { PreferenceService } from '../../../../shared/services/preference.service';

export function StorageEditComponent(template: string, editModel: any): any {
	@Component({
		selector: 'storage-edit',
		template: template,
		providers: [
			{ provide: 'model', useValue: editModel }
		]
	})
	class StorageShowComponent implements  OnInit {
		constructor( @Inject('model') private model: any, private activeDialog: UIActiveDialogService, private preference: PreferenceService) {
		}

		ngOnInit(): void {
			console.log('Init storage-edit');
		}
	}

	return StorageShowComponent;
}
