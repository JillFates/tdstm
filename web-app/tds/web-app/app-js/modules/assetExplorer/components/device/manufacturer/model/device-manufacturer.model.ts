import {Aka} from '../../../../../../shared/components/aka/model/aka.model';

export class DeviceManufacturer {
	public manufacturerId: number;
	public name: string;
	public description: string;
	public aka: Array<Aka>;
	public corporateLocation: string;
	public corporateName: string;
	public website: string;
}
/*
manufacturerId: 34,
			name: 'Avocent',
			aka: [ {id: 171, value: 'a1'}, {id:172, value: 'a2'}, {id:173, value: 'a3'}],
			description: 'text with desciption',
			corporateName: 'text with corporate name',
			corporateLocation: 'text with corporate location',
			website: 'text with website'
 */
