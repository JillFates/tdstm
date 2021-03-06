import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import { Observable } from 'rxjs';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import {PersonModel} from '../components/add-person/model/person.model';

@Injectable()
export class PersonService {

	private personUrl = '/tdstm/person';

	constructor(private http: HttpClient) {
	}

	savePerson(person: PersonModel): Observable<any> {
		const params =  {
			createstaff: person.asset,
			company: person.company && person.company.id || '',
			staffType: person.staffTypeId || 'Salary',
			'function': person.selectedTeams
				.map(item => item.team && item.team.id)
				.filter(item => Boolean(item))
		};

		const payload = { ...person, ...params};
		return this.http.post(`${this.personUrl}/save`, JSON.stringify(payload))
			.map((response: any) => response)
			.catch((error: any) => error);
	}

}