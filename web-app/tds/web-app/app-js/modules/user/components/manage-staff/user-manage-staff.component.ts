import {Component} from '@angular/core';

import {UserService} from '../../service/user.service';
import {PersonModel} from '../../../../shared/components/add-person/model/person.model';

@Component({
	selector: 'user-list',
	templateUrl: '../tds/web-app/app-js/modules/user/components/manage-staff/user-manage-staff.component.html'
})
export class UserManageStaffComponent {
	public editing;
	public currentTab;
	private currentPersonId;
	private savedPersonModel;

	constructor(
		public personModel: PersonModel,
		private userService: UserService) {
		this.loadComponentModel();
		this.editing = false;
		this.currentTab = "person";
	}

	private loadComponentModel() {
		this.userService.fetchComponentModel().subscribe(
			(result: any) => {
				const defaultPerson = {
					firstName: '',
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
					staffTypeId: '',
					company: '',
					companies: [],
					teams: [],
					staffType: [],
					selectedTeams: []
				};

				this.personModel = Object.assign({}, defaultPerson, this.personModel);
				let currentPersonInfo = result.person;
				let personModel = this.personModel;
				// Fill the model based on the current person.
				Object.keys(currentPersonInfo).forEach(function (key) {
					if (key in personModel) {
						personModel[key] = currentPersonInfo[key];
					}
				});
				this.personModel = personModel;
				this.savedPersonModel = Object.assign({}, this.personModel, this.savedPersonModel);
				this.currentPersonId = result.person.id;
			},
			(err) => console.log(err));
	}
}