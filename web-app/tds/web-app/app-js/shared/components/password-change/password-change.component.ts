import {Component, Input, OnInit} from '@angular/core';
import {PasswordChangeModel} from './model/password-change.model';

@Component({
	selector: 'tds-password-change',
	template: `
			<div class="tds-password-change-component">
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
	                    <div class="passwordRequirement" [ngClass]="{'passRequirement' : !passwordChangeModel.containsUsername, 'failRequirement' : passwordChangeModel.containsUsername}">
		                    <span>Password must not contain the username</span>
                            <b *ngIf="passwordChangeModel.containsUsername">OK</b>
	                    </div>
		                <div class="passwordRequirement" [ngClass]="{'passRequirement' : passwordChangeModel.atLeastMinimumLength, 'failRequirement' : !passwordChangeModel.atLeastMinimumLength}">
		                    <span>Password must be at least 8 characters long</span>
                            <b *ngIf="passwordChangeModel.atLeastMinimumLength">OK</b>
		                </div>
		                <div class="passwordRequirementGroup" [ngClass]="{'passRequirement' : passwordChangeModel.meetsCompositionRequirements, 'failRequirement' : !passwordChangeModel.meetsCompositionRequirements}">
			                <span>Password must contain at least 3 of these requirements:</span>
                            <b *ngIf="passwordChangeModel.meetsCompositionRequirements">OK</b>
		                </div>
		                <div>
			                <ul class="passwordRequirement">
				                <li [ngClass]="{'passRequirement' : passwordChangeModel.hasUppercaseChars,
				                                'failRequirement' : !passwordChangeModel.hasUppercaseChars && !passwordChangeModel.meetsCompositionRequirements}">
					                <span>Uppercase Characters</span>
					                <b *ngIf="passwordChangeModel.hasUppercaseChars">OK</b>
				                </li>
				                <li [ngClass]="{'passRequirement' : passwordChangeModel.hasLowercaseChars,
				                                'failRequirement' : !passwordChangeModel.hasLowercaseChars && !passwordChangeModel.meetsCompositionRequirements}">
					                <span>Lowercase Characters</span>
					                <b *ngIf="passwordChangeModel.hasLowercaseChars">OK</b>
				                </li>
				                <li [ngClass]="{'passRequirement' : passwordChangeModel.hasNumericChars,
				                                'failRequirement' : !passwordChangeModel.hasNumericChars && !passwordChangeModel.meetsCompositionRequirements}">
					                <span>Numeric Characters</span>
					                <b *ngIf="passwordChangeModel.hasNumericChars">OK</b>
				                </li>
				                <li [ngClass]="{'passRequirement' : passwordChangeModel.hasNonAlphanumericChars,
				                                'failRequirement' : !passwordChangeModel.hasNonAlphanumericChars && !passwordChangeModel.meetsCompositionRequirements}">
					                <span>Nonalphanumeric Characters</span>
					                <b *ngIf="passwordChangeModel.hasNonAlphanumericChars">OK</b>
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
		            <td class="passwordRequirement" valign="top" class="value">
		                <span>Password should match</span>
		            </td>
	            </tr>
			</div>
	`
})
export class PasswordChangeComponent implements OnInit {
	@Input('passwordChangeModel') passwordChangeModel: PasswordChangeModel;
	public hidePasswordFields;

	ngOnInit() {
		this.hidePasswordFields = true;
	}

	public validatePassword() {
		let password = this.passwordChangeModel.newPassword;
		if(password != undefined) {
			this.passwordChangeModel.containsUsername = password.includes('tdsadmin'); // TODO: make this dynamic
			this.passwordChangeModel.atLeastMinimumLength = (password.length > 7);
			this.passwordChangeModel.hasLowercaseChars = /[a-z]/.test(password);
			this.passwordChangeModel.hasUppercaseChars = /[A-Z]/.test(password);
			this.passwordChangeModel.hasNumericChars = /[0-9]/.test(password);
			this.passwordChangeModel.hasNonAlphanumericChars = /[~!@#$%\^&\*_\-\+=`\|\\\(\)\{\}\[\]:;"'<>\,\.?\/]/.test(password);
			let strengthScore = (this.passwordChangeModel.hasLowercaseChars ? 1:0) +
								(this.passwordChangeModel.hasUppercaseChars ? 1:0) +
								(this.passwordChangeModel.hasNumericChars ? 1:0) +
								(this.passwordChangeModel.hasNonAlphanumericChars ? 1:0);
			this.passwordChangeModel.meetsCompositionRequirements = (strengthScore >= 3);
		}
	}
}