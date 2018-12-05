import {Component, Input, OnInit} from '@angular/core';
import {PasswordChangeModel} from './model/password-change.model';
import {UserService} from '../../../modules/user/service/user.service';

@Component({
	selector: 'tds-password-change',
	template: `
			<div class="tds-password-change-component">
                <table>
			        <tr class="prop">
			            <td valign="top" class="name">
			                <label for="hidePasswords">Hide Password:</label>
			            </td>
			            <td valign="top" class="value">
			                <input type="checkbox" id="hidePasswordsId" name="hidePasswords" [(ngModel)]="hidePasswordFields"/>
			            </td>
			        </tr>
			        <tr class="prop">
			            <td valign="top" class="name">
			                <label for="oldPassword">Old Password:</label>
			            </td>
			            <td valign="top" class="value">
			                <input type="{{hidePasswordFields ? 'password' : 'text'}}" id="oldPasswordId" name="oldPassword" [(ngModel)]="passwordChangeModel.oldPassword"/>
			            </td>
			        </tr>
				    <tr class="prop">
			            <td valign="top" class="name">
			                <label for="newPassword">New Password:</label>
			            </td>
			            <td valign="top" class="value">
			                <input type="{{hidePasswordFields ? 'password' : 'text'}}" id="newPasswordId" name="newPassword" [(ngModel)]="passwordChangeModel.newPassword" (input)="validatePassword()"/>
			            </td>
		            </tr>
		            <tr class="prop">
		                <td valign="top" class="name">
		                    <label for="requirements">Requirements:</label>
		                </td>
		                <td valign="top" class="value">
		                    <div class="passwordRequirement" [ngClass]="{'passRequirement' : !passwordChangeModel.containsUsername && passwordChangeModel.newPassword,
		                    'failRequirement' : passwordChangeModel.containsUsername && passwordChangeModel.newPassword}">
			                    <span>Password must not contain the username</span>
	                            <b *ngIf="passwordChangeModel.containsUsername && passwordChangeModel.newPassword">OK</b>
		                    </div>
			                <div class="passwordRequirement" [ngClass]="{'passRequirement' : passwordChangeModel.atLeastMinimumLength && passwordChangeModel.newPassword,
			                'failRequirement' : !passwordChangeModel.atLeastMinimumLength && passwordChangeModel.newPassword}">
			                    <span>Password must be at least 8 characters long</span>
	                            <b *ngIf="passwordChangeModel.atLeastMinimumLength && passwordChangeModel.newPassword">OK</b>
			                </div>
			                <div class="passwordRequirementGroup" [ngClass]="{'passRequirement' : passwordChangeModel.meetsCompositionRequirements && passwordChangeModel.newPassword,
			                 'failRequirement' : !passwordChangeModel.meetsCompositionRequirements && passwordChangeModel.newPassword}">
				                <span>Password must contain at least 3 of these requirements:</span>
	                            <b *ngIf="passwordChangeModel.meetsCompositionRequirements && passwordChangeModel.newPassword">OK</b>
			                </div>
			                <div>
				                <ul class="passwordRequirement">
					                <li [ngClass]="{'passRequirement' : passwordChangeModel.hasUppercaseChars  && passwordChangeModel.newPassword,
					                                'failRequirement' : !passwordChangeModel.hasUppercaseChars && !passwordChangeModel.meetsCompositionRequirements  && passwordChangeModel.newPassword}">
						                <span>Uppercase Characters</span>
						                <b *ngIf="passwordChangeModel.hasUppercaseChars && passwordChangeModel.newPassword">OK</b>
					                </li>
					                <li [ngClass]="{'passRequirement' : passwordChangeModel.hasLowercaseChars && passwordChangeModel.newPassword,
					                                'failRequirement' : !passwordChangeModel.hasLowercaseChars && !passwordChangeModel.meetsCompositionRequirements && passwordChangeModel.newPassword}">
						                <span>Lowercase Characters</span>
						                <b *ngIf="passwordChangeModel.hasLowercaseChars && passwordChangeModel.newPassword">OK</b>
					                </li>
					                <li [ngClass]="{'passRequirement' : passwordChangeModel.hasNumericChars  && passwordChangeModel.newPassword,
					                                'failRequirement' : !passwordChangeModel.hasNumericChars && !passwordChangeModel.meetsCompositionRequirements && passwordChangeModel.newPassword}">
						                <span>Numeric Characters</span>
						                <b *ngIf="passwordChangeModel.hasNumericChars && passwordChangeModel.newPassword">OK</b>
					                </li>
					                <li [ngClass]="{'passRequirement' : passwordChangeModel.hasNonAlphanumericChars && passwordChangeModel.newPassword,
					                                'failRequirement' : !passwordChangeModel.hasNonAlphanumericChars && !passwordChangeModel.meetsCompositionRequirements && passwordChangeModel.newPassword}">
						                <span>Nonalphanumeric Characters</span>
						                <b *ngIf="passwordChangeModel.hasNonAlphanumericChars && passwordChangeModel.newPassword">OK</b>
					                </li>
				                </ul>
			                </div>
		                </td>
		            </tr>
		            <tr class="prop">
		                <td valign="top" class="name">
		                    <label for="confirmPassword">Confirm New Password:</label>
		                </td>
		                <td valign="top" class="value">
		                    <input type="{{hidePasswordFields ? 'password' : 'text'}}" id="confirmPasswordId" name="confirmPassword" [(ngModel)]="passwordChangeModel.confirmPassword"/>
		                </td>
		            </tr>
				    <tr class="prop">
			            <td valign="top" class="name">
			            </td>
			            <td valign="top" class="value">
			                <em>Password should match</em>
			            </td>
		            </tr>
	            </table>
			</div>
	`
})
export class PasswordChangeComponent implements OnInit {
	@Input('passwordChangeModel') passwordChangeModel: PasswordChangeModel;
	public hidePasswordFields;
	private currentUsername;

	constructor(private userService: UserService) {
	}

	ngOnInit() {
		this.hidePasswordFields = true;
		this.userService.getUser().subscribe(
			(result: any) => {
				this.currentUsername = result.username;
			},
			(err) => console.log(err));
	}

	public validatePassword() {
		let password = this.passwordChangeModel.newPassword;
		if (password != undefined) {
			this.passwordChangeModel.containsUsername = password.includes(this.currentUsername);
			this.passwordChangeModel.atLeastMinimumLength = (password.length > 7);
			this.passwordChangeModel.hasLowercaseChars = /[a-z]/.test(password);
			this.passwordChangeModel.hasUppercaseChars = /[A-Z]/.test(password);
			this.passwordChangeModel.hasNumericChars = /[0-9]/.test(password);
			this.passwordChangeModel.hasNonAlphanumericChars = /[~!@#$%\^&\*_\-\+=`\|\\\(\)\{\}\[\]:;"'<>\,\.?\/]/.test(password);
			let strengthScore = (this.passwordChangeModel.hasLowercaseChars ? 1 : 0) +
								(this.passwordChangeModel.hasUppercaseChars ? 1 : 0) +
								(this.passwordChangeModel.hasNumericChars ? 1 : 0) +
								(this.passwordChangeModel.hasNonAlphanumericChars ? 1 : 0);
			this.passwordChangeModel.meetsCompositionRequirements = (strengthScore >= 3);
		}
	}
}