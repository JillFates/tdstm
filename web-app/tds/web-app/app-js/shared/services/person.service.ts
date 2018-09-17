import { Injectable } from '@angular/core';
import {Response, RequestOptions, Headers} from '@angular/http';
import { Observable } from 'rxjs';
import { HttpInterceptor } from '../providers/http-interceptor.provider';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import {PersonModel} from '../components/add-person/model/person.model';

@Injectable()
export class PersonService {

	private personUrl = '/tdstm/person/save';

	constructor(private http: HttpInterceptor) {
	}

	savePerson(person: PersonModel): Observable<any> {
		const headers = new Headers();
		headers.append('Content-Type', 'application/x-www-form-urlencoded');
		const requestOptions = new RequestOptions({headers: headers});

		let body = `createstaff=${person.asset}&company=${person.company && person.company.id || ''}`;
		body += `&firstName=${person.firstName}&middleName=${person.middleName}`;
		body += `&lastName=${person.lastName}&nickName=${person.nickName}`;
		body += `&title=${person.title}&staffType=${person.staffTypeId || 'Salary'}`;
		body += `&email=${person.email}&active=${person.active}`;
		body += `&department=${person.department}&location=${person.location}`;
		body += `&workPhone=${person.workPhone}&mobilePhone=${person.mobilePhone}`;

		if (person.selectedTeams && person.selectedTeams.length) {
			const teams = person.selectedTeams
				.map(item => item.team && item.team.id)
				.filter(item => Boolean(item))
				.map((team) => `&function=${team}`)
				.join('');

			body += teams;
		}
		body += `&funcToAdd=ACCT_MGR&fieldName=${person.fieldName}`;

		console.log(body);
		return this.http.post(`${this.personUrl}/save`, body, requestOptions)
			.map((res: Response) => {
				return res.json();
			})
			.catch((error: any) => error.json());
	}

}