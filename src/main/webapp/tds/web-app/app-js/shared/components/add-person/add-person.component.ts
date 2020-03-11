import {Component, ViewChild, ElementRef, OnInit} from '@angular/core';

import { UIExtraDialog} from '../../../shared/services/ui-dialog.service';
import { PersonModel } from './model/person.model';
import { PersonService } from '../../services/person.service';
import { UIPromptService} from '../../directives/ui-prompt.directive';
import { DecoratorOptions} from '../../model/ui-modal-decorator.model';
import {TranslatePipe} from '../../pipes/translate.pipe';

@Component({
	selector: 'add-person',
	template: `
        <div tds-handle-escape (escPressed)="cancelCloseDialog()" class="modal fade in add-person-component" id="add-person-component" data-backdrop="static" tabindex="0" role="dialog">
            <div class="modal-dialog modal-md" role="document">
                <div class="tds-modal-content" tds-ui-modal-decorator=""
                     [options]="modalOptions">
                    <div class="modal-header">
						<button aria-label="Close" class="close" type="button" (click)="cancelCloseDialog($event)">
							<clr-icon aria-hidden="true" shape="close"></clr-icon>
						</button>
                        <h4 class="modal-title">Person Create</h4>
                    </div>
                    <div class="modal-body">
                        <div class="modal-body-container">
                            <form name="addPersonForm" role="form" data-toggle="validator" #addPersonForm='ngForm' class="form-horizontal left-alignment">
                                <div class="box-body">
                                    <div class="form-group">
                                        <div class="col-md-12 mandatory">Fields marked ( * ) are mandatory</div>
                                    </div>
                                    <div class="form-group">
                                        <div class="col-md-3 label-description">Company:</div>
                                        <div class="col-md-9">
                                            <kendo-dropdownlist name="company"
                                                                [data]="personModel.companies"
                                                                [textField]="'name'"
                                                                [valueField]="'id'"
                                                                [(ngModel)]="personModel.company"
                                                                class="form-control person-company">
                                            </kendo-dropdownlist>
                                        </div>
                                    </div>
                                    <div class="form-group">
                                        <div class="col-md-3 required-field label-description">First Name:</div>
                                        <div class="col-md-9"><input type="text"  [(ngModel)]="personModel.firstName" name="firstName"></div>
                                    </div>
                                    <div class="form-group">
                                        <div class="col-md-3 middle-name label-description">Middle Name:</div>
                                        <div class="col-md-9"><input type="text" [(ngModel)]="personModel.middleName" name="middleName"></div>
                                    </div>
                                    <div class="form-group">
                                        <div class="col-md-3 label-description">Last Name:</div>
                                        <div class="col-md-9"><input type="text" [(ngModel)]="personModel.lastName" name="lastName"></div>
                                    </div>
                                    <div class="form-group">
                                        <div class="col-md-3 label-description">Nick Name:</div>
                                        <div class="col-md-9"><input type="text" [(ngModel)]="personModel.nickName" name="nickName"></div>
                                    </div>
                                    <div class="form-group">
                                        <div class="col-md-3 label-description">Title:</div>
                                        <div class="col-md-9"><input type="text" [(ngModel)]="personModel.title" name="title"></div>
                                    </div>
                                    <div class="form-group">
                                        <div class="col-md-3 label-description">Staff type:</div>
                                        <div class="col-md-4">
                                            <kendo-dropdownlist
                                                    name="staffType"
                                                    class="person-salary"
                                                    [(ngModel)]="personModel.staffTypeId"
                                                    [data]="personModel.staffType">
                                            </kendo-dropdownlist>
                                        </div>
                                        <div class="col-md-1 required-field active-container label-description ">Active:</div>
                                        <div class="col-md-4">
                                            <kendo-dropdownlist
                                                    name="active"
                                                    class="person-active"
                                                    [(ngModel)]="personModel.active"
                                                    [data]="yesNoList">
                                            </kendo-dropdownlist>
                                        </div>
                                    </div>
                                    <div class="form-group">
                                        <div class="col-md-3 label-description">Email:</div>
                                        <div class="col-md-9">
                                            <input type="text" (keyup)="resetFieldError('email')" [(ngModel)]="personModel.email" name="email">
                                            <p *ngIf="errors && errors.email" class="error-msg">{{errors.email}}</p>
                                        </div>
                                    </div>
                                    <div class="form-group">
                                        <div class="col-md-3 label-description">Department:</div>
                                        <div class="col-md-9"><input type="text" [(ngModel)]="personModel.department" name="department"></div>
                                    </div>
                                    <div class="form-group">
                                        <div class="col-md-3 label-description">Location:</div>
                                        <div class="col-md-9"><input type="text" [(ngModel)]="personModel.location" name="location"></div>
                                    </div>
                                    <div class="form-group">
                                        <div class="col-md-3 label-description">Work Phone:</div>
                                        <div class="col-md-4">
                                            <input type="text" (keyup)="resetFieldError('workPhone')"  [(ngModel)]="personModel.workPhone" name="workPhone">
                                            <p *ngIf="errors && errors.workPhone" class="error-msg">{{errors.workPhone}}</p>
                                        </div>
                                        <div class="col-md-1 mobile label-description">Mobile:</div>
                                        <div class="col-md-4">
                                            <input type="text" (keyup)="resetFieldError('mobilePhone')" [(ngModel)]="personModel.mobilePhone" name="mobilePhone">
                                            <p *ngIf="errors && errors.mobilePhone" class="error-msg">{{errors.mobilePhone}}</p>
                                        </div>
                                    </div>
                                    <div class="form-group">
                                        <div class="col-md-3 label-description">Team:</div>
                                        <div class="col-md-9">
                                            <div><button (click)="addTeam()" class="component-action-add-team">Add Team</button></div>
                                            <div *ngFor="let team of teams;let index=index;" class="teams">
                                                <kendo-dropdownlist
                                                        name="active"
                                                        [value]="teams[index].team"
                                                        (valueChange)="onTeamSelected($event, index)"
                                                        [textField]="'description'"
                                                        [valueField]="'id'"
                                                        [data]="teams[index].data">
                                                </kendo-dropdownlist>
                                                <span class="glyphicon glyphicon-remove remove-team component-action-remove-team" (click)="removeTeam(index)"></span>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </form>
                        </div>
                    </div>
					<div class="modal-footer form-group-center">
						<tds-button theme="primary" (click)="save()" icon="floppy" title="Save" [disabled]="!canSave()">Save</tds-button>
						<tds-button (click)="cancelCloseDialog($event)" icon="ban" title="Cancel">Cancel</tds-button>
					</div>
                </div>
            </div>
        </div>
	`
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
		private translatePipe: TranslatePipe,
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
			country: '',
			keyWords: '',
			tdsLink: '',
			tdsNote: '',
			travelOK: 0,
			companies: [],
			teams: [] ,
			staffType: [],
			selectedTeams: [],
			personImageURL: ''
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
	public canSave(): boolean {
		const errors = this.errors || {};
		return Boolean(this.personModel.firstName.trim() && this.personModel.active && Object.keys(errors).length === 0);
	}

	/**
	 * Close the Dialog but first it verify is not Dirty
	 */
	public cancelCloseDialog(): void {
		if (this.isDirty()) {

			this.promptService.open(
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED'),
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.UNSAVED_CHANGES_MESSAGE'),
				this.translatePipe.transform('GLOBAL.CONFIRM'),
				this.translatePipe.transform('GLOBAL.CANCEL'),
			)
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
