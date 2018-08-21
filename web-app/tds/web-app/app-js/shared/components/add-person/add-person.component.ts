import {Component, ViewChild, ElementRef, OnInit} from '@angular/core';

import { UIExtraDialog} from '../../../shared/services/ui-dialog.service';
import { PersonModel } from './model/person.model';
import { AssetExplorerService } from '../../../modules/assetExplorer/service/asset-explorer.service';

@Component({
	selector: 'add-person',
	templateUrl: '../tds/web-app/app-js/shared/components/add-person/add-person.component.html'
})
export class AddPersonComponent extends UIExtraDialog  implements  OnInit {
	yesNoList = ['Y', 'N'];
	teams: any[] = [];
	errors: any;
	constructor(
		public personModel: PersonModel,
		private assetExplorerService: AssetExplorerService) {
		super('#add-person-component');
		this.errors = {};
	}

	ngOnInit() {
		const defaultPerson = { firstName: '',
			middleName: '',
			lastName: '',
			nickName: '',
			title: '',
			email: '',
			department: '',
			location: '',
			workPhone: '',
			mobilePhone: '',
			active: '',
			companies: [],
			teams: [] ,
			staffType: [],
			selectedTeams: []
		};
		this.personModel = Object.assign({},  defaultPerson, this.personModel)
	}

	/***
	 * Close the Active Dialog
	 */
	protected cancelCloseDialog(): void {
		this.dismiss();
	}

	addTeam(): void {
		const team = {team: null, data: [...this.personModel.teams]};
		this.teams.push(team);
	}

	removeTeam(index: number): void {
		this.teams.splice(index, 1);
	}

	onTeamSelected(team: any, index: number): void {
		console.log(team);
		this.teams[index].team = team;
	}

	save(): void {
		this.validateFields()
			.then(() => {
				console.log('Saving results');
				this.personModel.selectedTeams = this.teams;
				this.assetExplorerService.savePerson(this.personModel)
					.subscribe((result: any) => {
						console.log('The result is:');
						console.log(result);
						this.close(result);
					})
			})
			.catch(err => console.log('Error validating: ', err.message || err))
	}

	validateFields(): Promise<boolean> {
		const emailExp = /^([0-9a-zA-Z]+([_.-]?[0-9a-zA-Z]+)*@[0-9a-zA-Z]+[0-9,a-z,A-Z,.,-]+\.[a-zA-Z]{2,4})+$/
		const mobileExp = /^([0-9 +-])+$/

		console.log(this.personModel);
		const {email, workPhone, mobilePhone} = this.personModel;
		console.log('Email: ', email);

		return new Promise((resolve, reject) => {
			this.errors = {};

			if (email && !emailExp.test(email)) {
				this.errors.email = `${email} is not a valid e-mail address`;
			}

			if (workPhone && !(mobileExp.test(workPhone))) {
				this.errors.workPhone =  'The Work phone number contains illegal characters.';
			}

			if (mobilePhone && !(mobileExp.test(mobilePhone))) {
				this.errors.mobilePhone =  'The Mobile phone number contains illegal characters.';
			}

			return this.errors && Object.keys(this.errors).length ?  reject(new Error('Error validating fields')) : resolve(true); });
	}

	canSave(): boolean {
		const errors = this.errors || {};
		return Boolean(this.personModel.firstName.trim() && this.personModel.active && Object.keys(errors).length === 0);
	}

	resetFieldError(fieldName: string): void {
		if (this.errors &&  this.errors[fieldName]) {
			delete this.errors[fieldName];
		}
	}
}