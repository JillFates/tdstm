import {Component, ViewChild, ElementRef, OnInit} from '@angular/core';

import { UIExtraDialog} from '../../../shared/services/ui-dialog.service';
import { PersonModel } from './model/person.model';
import { PersonService } from '../../services/person.service';
import { UIPromptService} from '../../directives/ui-prompt.directive';
import { DecoratorOptions} from '../../model/ui-modal-decorator.model';

@Component({
	selector: 'add-person',
	templateUrl: '../tds/web-app/app-js/shared/components/add-person/add-person.component.html'
})
export class AddPersonComponent extends UIExtraDialog  implements  OnInit {
	yesNoList = ['Y', 'N'];
	teams: any[] = [];
	errors: any;
	dataSignature: string;
	public modalOptions: DecoratorOptions;
	constructor(
		public personModel: PersonModel,
		private personService: PersonService,
		private promptService: UIPromptService) {
		super('#add-person-component');
		this.errors = {};
		this.modalOptions = { isDraggable: true, isResizable: false, isCentered: false };
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
			active: 'Y',
			staffTypeId: [...this.personModel.staffType].pop() || '',
			company: [...this.personModel.companies].pop() || '',
			companies: [],
			teams: [] ,
			staffType: [],
			selectedTeams: []
		};
		this.personModel = Object.assign({},  defaultPerson, this.personModel)
		this.dataSignature = JSON.stringify(this.personModel);
	}
	/**
	 * Verify the Object has not changed
	 * @returns {boolean}
	 */
	protected isDirty(): boolean {
		return this.dataSignature !== JSON.stringify(this.personModel) || this.teams.length > 0;
	}

	/**
	 * Verify if user filled all required fiedls
	 * @returns {boolean}
	 */
	protected canSave(): boolean {
		const errors = this.errors || {};
		return Boolean(this.personModel.firstName.trim() && this.personModel.active && Object.keys(errors).length === 0);
	}

	/**
	 * Close the Dialog but first it verify is not Dirty
	 */
	protected cancelCloseDialog(): void {
		if (this.isDirty()) {

			this.promptService.open(
				'Confirmation Required',
				'You have changes that have not been saved. Do you want to continue and lose those changes?',
				'Confirm', 'Cancel')
				.then(confirm => {
					if (confirm) {
						this.dismiss();
					}
				})
				.catch((error) => console.log(error));
		} else {
			this.dismiss();
		}
	}

	/**
	 *  Add a team
	 */
	addTeam(): void {
		let defaultTeam = null;
		if (this.personModel.teams && this.personModel.teams.length) {
			defaultTeam = this.personModel.teams[0];
		}

		const team = {team: defaultTeam, data: [...this.personModel.teams]};
		this.teams.push(team);
	}

	/**
	 *  Remove teams located in index position
	 */
	removeTeam(index: number): void {
		this.teams.splice(index, 1);
	}

	/**
	 *  Save the reference to current team selected
	 */
	onTeamSelected(team: any, index: number): void {
		this.teams[index].team = team;
	}

	/**
	 *  Save changes  after passing validation process
	 */
	save(): void {
		this.validateFields()
			.then(() => {
				console.log('Saving results');
				this.personModel.selectedTeams = this.teams;
				this.personService.savePerson(this.personModel)
					.subscribe((result: any) => {
						if (result.errMsg) {
							alert(result.errMsg);
							return;
						}

						this.close(result);
					}, err => alert(err.message || err))
			})
			.catch(err => console.log('Error validating: ', err.message || err))
	}

	/**
	 *  Validates fields meet with proper values based on data types
	 */
	validateFields(): Promise<boolean> {
		const emailExp = /^([0-9a-zA-Z]+([_.-]?[0-9a-zA-Z]+)*@[0-9a-zA-Z]+[0-9,a-z,A-Z,.,-]+\.[a-zA-Z]{2,63})+$/
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

	/**
	 *  Clean errors collection
	 */
	resetFieldError(fieldName: string): void {
		if (this.errors &&  this.errors[fieldName]) {
			delete this.errors[fieldName];
		}
	}
}