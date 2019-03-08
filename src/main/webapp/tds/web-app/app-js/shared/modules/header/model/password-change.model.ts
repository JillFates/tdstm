export class PasswordChangeModel {
	public oldPassword: string;
	public confirmPassword: string;
	public newPassword: string;
	public containsUsername: boolean;
	public atLeastMinimumLength: boolean;
	public meetsCompositionRequirements: boolean;
	public hasUppercaseChars: boolean;
	public hasLowercaseChars: boolean;
	public hasNumericChars: boolean;
	public hasNonAlphanumericChars: boolean;
}