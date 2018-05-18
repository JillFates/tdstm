/**
 *  The component is being used dynamically, some vars will show as not being used or referenced but they could be part
 *  of the GSP
 *
 *  Use angular/views/TheAssetType as reference
 */
import { Component, ViewChild, Inject, OnInit } from '@angular/core';
import {UIActiveDialogService, UIDialogService} from '../../../../shared/services/ui-dialog.service';

import { PreferenceService } from '../../../../shared/services/preference.service';
import {DateUtils} from '../../../../shared/utils/date.utils';
import * as R from 'ramda';
import {Person} from '../../../../shared/components/add-person/model/person.model';
import {AddPersonComponent} from '../../../../shared/components/add-person/add-person.component';

interface SelectedItem {
	class: string;
	id: number;
}

export function ApplicationEditComponent(template: string, editModel: any): any {
	@Component({
		selector: 'application-edit',
		template: template,
		providers: [
			{ provide: 'model', useValue: editModel }
		]
	})
	class ApplicationShowComponent implements OnInit {
		defaultItem = {fullName: 'Please Select', personId: 0};
		yesNoList = ['Y', 'N'];
		private dateFormat: string;
		constructor(@Inject('model') private model: any, private activeDialog: UIActiveDialogService, private dialogService: UIDialogService, private preference: PreferenceService) {}
		ngOnInit(): void {
			console.log('Loading application-edit.component');
			this.dateFormat = this.preference.preferences['CURR_DT_FORMAT'];
			this.dateFormat = this.dateFormat.toLowerCase().replace(/m/g, 'M');
			this.initModel();
		}

		private initModel(): void {

			this.model.asset = R.clone(editModel.asset);
			this.model.asset.retireDate = DateUtils.compose(this.model.asset.retireDate);
			this.model.asset.maintExpDate = DateUtils.compose(this.model.asset.maintExpDate);

			this.model.asset.sme = this.model.asset.sme || { id: null };
			this.model.asset.sme2 = this.model.asset.sme2 || { id: null };
			this.model.asset.appOwner = this.model.asset.appOwner || { id: null };

			if (this.model.asset.scale === null) {
				this.model.asset.scale = {
					name: ''
				};
			}

			this.model.asset.startUpBySelectedValue = { id: null, text: 'Please Select'};
			if (this.model.asset.startUpBySelectedValue) {
				this.model.asset.startUpBySelectedValue.id = this.model.asset.startupBy;
				// this.model.asset.startUpBySelectedValue.text = 'Temp value';
			}

			/*
			this.model.asset.assetTypeSelectValue = {id: null};
			if (this.model.asset.assetType) {
				this.model.asset.assetTypeSelectValue.id = this.model.asset.assetType;
				this.model.asset.assetTypeSelectValue.text = this.model.asset.assetType;
			}
			this.model.asset.manufacturerSelectValue = {id: null};
			if (this.model.asset.manufacturer) {
				this.model.asset.manufacturerSelectValue.id = this.model.asset.manufacturer.id;
				this.model.asset.manufacturerSelectValue.text = this.model.asset.manufacturer.text;
			}
			*/
		}

		getCurrentValue(control: string) {
			console.log('get current value');
		}

		shufflePerson(source: string, target: string) {
			const sourceId = this.model.asset && this.model.asset[source] && this.model.asset[source].id || null;
			const targetId = this.model.asset && this.model.asset[target] && this.model.asset[target].id || null;

			if (sourceId && targetId) {
				const backSource = sourceId;

				this.model.asset[source].id = targetId;
				this.model.asset[target].id = backSource;
			}
		}

		addPerson(event, partyGroupList) {

			let person: Person = {
				name: 'the name',
				partyGroupList
			};

			this.dialogService.extra(AddPersonComponent,
				[UIDialogService,
					{
						provide: Person,
						useValue: person
					}
				], true, false)
				.then((result) => {
					console.log(result);
					// dataItem.comment = result.comment;
				}).catch((error) => console.log(error));
		}

	}

	return ApplicationShowComponent;
}