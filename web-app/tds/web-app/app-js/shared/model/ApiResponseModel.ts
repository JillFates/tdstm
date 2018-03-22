/**
 * Represents the standard response model structure of the API calls.
 */

export class ApiResponseModel {

	public static readonly API_SUCCESS = 'success';
	public static readonly API_ERROR = 'error';

	data: any;
	status: 'success' | 'error';
	errors?: Array<any>;
}