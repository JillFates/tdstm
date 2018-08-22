import {Component, Input, Output, OnInit, EventEmitter } from '@angular/core';
import { Aka, AkaParent, AkaChanges } from './model/aka.model';
import { ManufacturerService} from '../../../modules/assetExplorer/service/manufacturer.service';

@Component({
	selector: 'tds-aka',
	templateUrl: '../tds/web-app/app-js/shared/components/aka/aka.component.html'
})
export class AkaComponent implements OnInit {
	@Input('aka') akaCollection: Array<Aka>;
	@Input('akaParent') akaParent: AkaParent;
	@Output('modelChange') modelChange = new EventEmitter<AkaChanges>();
	clonedAkas: Array<Aka>;
	hasError: boolean;
	indexError: number;
	errorMessage: string;
	akas: string[];
	deletedAkas: Aka[];
	constructor(private manufacturerService: ManufacturerService) {
		this.akas = [];
	}

	ngOnInit() {
		this.cleanError();
		this.clonedAkas = [...this.akaCollection];

		this.deletedAkas = [];
		this.akas = (this.clonedAkas || [])
			.map((aka: Aka) => aka.name);

		this.sendAkaChanges();
	}

	private cleanError() {
		this.errorMessage = '';
		this.indexError = -1;
		this.hasError = false;
	}

	onAdd(aka: string): void {
		// check if there is empty aka

		const foundEmpty = this.akas.filter((aka: string) => (aka || '').trim() === '');
		if (foundEmpty && foundEmpty.length) {
			return;
		}

		this.akas.push(aka);
		this.clonedAkas.push({ id: null, name: aka });
	}

	trackByIndex(index: number, obj: any): any {
		return index;
	}

	onDelete(index) {
		this.deletedAkas.push(this.clonedAkas[index]);
		this.akas.splice(index, 1);
		this.clonedAkas.splice(index, 1);
		if (this.hasError && this.indexError === index) {
			this.cleanError();
		}
		this.sendAkaChanges();
	}

	onAkaChange(event: any, index: number) {
		const aka = event.target.value;

		this.validateAka(aka, index)
			.then(() => {
				console.log('There is no errors');
				if (this.hasError && this.indexError === index) {
					this.cleanError();
				}
				this.clonedAkas[index].name = aka;
				this.sendAkaChanges();
			})
			.catch((err) => {
				console.log('There is errors');
				this.indexError = index;
				this.hasError = true;
				this.errorMessage = err;
			})
	}

	validateAka(newAka: string, index: number): Promise<string> {
		return new Promise((resolve, reject) => {
			// check if services is already present
			const foundRepeated = this.akas.filter((aka: string) => aka === newAka);
			if (foundRepeated && foundRepeated.length && foundRepeated.length > 1) {
				return reject(`AKA ${newAka} already entered` );
			}

			if (!newAka || newAka.trim()  === '') {
				return reject(`AKA is empty`);
			}

			if (this.akaParent) {
				return this.manufacturerService.isValidAlias(newAka, this.akaParent.id, this.akaParent.name)
					.subscribe((result: string) => {
						console.log('The isValid result is:');
						console.log(result);
						console.log('---------------------');

						if (result === 'valid') {
							return resolve('');
						} else {
							return reject(`AKA ${newAka} Already entered`);
						}
				});
			} else {
				return resolve('');
			}
		});
	}

	sendAkaChanges() {
		const edited = this.clonedAkas
			.filter((aka: Aka) => aka.id)
			.map(aka => ({id: aka.id, name: aka.name}));

		const added = this.clonedAkas
			.filter((aka: Aka) => !aka.id)
			.map(aka => ({id: aka.id, name: aka.name}));

		const akaChanges: AkaChanges = {
			edited,
			added,
			deleted: this.deletedAkas
				.filter((aka: Aka) => aka.id !== null)
				.map(aka => ({id: aka.id, name: aka.name}))
		};

		console.log('AKA CHANGES:');
		console.log(akaChanges);

		this.modelChange.emit(akaChanges);
	}

}