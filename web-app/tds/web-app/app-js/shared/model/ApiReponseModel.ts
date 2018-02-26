/**
 * Represents the standard response model structure of the API calls.
 */
export class ApiReponseModel {
	data: any;
	status: 'success'|'error';
	errors?: Array<any>;
}