import {Component, Input, Output, OnInit, EventEmitter } from '@angular/core';
import { Aka, AkaParent, AkaChanges } from './model/aka.model';
import { ManufacturerService} from '../../../modules/assetExplorer/service/manufacturer.service';

@Component({
	selector: 'tds-aka',
	template: `
        <div class="aka-component">
            <tds-button-add id="btnAddAka" class="btn-primary"
                            [tabindex]="tabindex" [disabled]="hasError"
                            [title]="'AKA.ADD' | translate"
                            (click)="onAdd('')">
            </tds-button-add>
            <div class="aka-items">
                <div class="aka-item" *ngFor="let aka of akas;let index = index;trackBy:trackByIndex;">
                    <input type="text" [tabindex]="tabindex" [(ngModel)]="akas[index]" (change)="onAkaChange($event, index)" [disabled]="hasError && indexError !== index">
                    <span *ngIf="!hasError || (hasError && indexError === index)" class="glyphicon glyphicon-remove delete-aka" (click)="onDelete(index)" title="Delete aka"></span>
                </div>
                <div>
                    <label *ngIf="hasError" style="color: red">{{errorMessage}}</label>
                </div>
            </div>
        </div>
	`
})
export class AkaComponent implements OnInit {
	@Input('aka') akaCollection: Array<Aka>;
	@Input('tabindex') tabindex: string;
	@Input('akaParent') akaParent: AkaParent;
	@Output('modelChange') modelChange = new EventEmitter<AkaChanges>();
	@Output('validationErrors') validationErrors = new EventEmitter<boolean>();
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
		this.validationErrors.emit(false);
	}

	onAdd(aka: string): void {
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
				if (this.hasError && this.indexError === index) {
					this.cleanError();
				}
				this.clonedAkas[index].name = aka;
				this.sendAkaChanges();
			})
			.catch((err) => {
				this.validationErrors.emit(true);
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
		this.modelChange.emit(akaChanges);
	}

}