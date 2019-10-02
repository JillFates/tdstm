import {Component, Input, Output, OnInit, EventEmitter} from '@angular/core';
import { Connector } from './model/connector.model';

@Component({
	selector: 'tds-connector',
	template: `
        <div class="tds-connector-component">
            <tds-button-add id="btnAddConnector" class="add-connector btn-primary"
                            [tabindex]="tabindex"
                            [title]="'CONNECTORS.ADD' | translate"
                            (click)="onAdd()">
            </tds-button-add>
            <div>
                <div class="header">
                    <div class="component-type">Type</div>
                    <div class="component-label">Label</div>
                    <div class="component-label-position">Label Position</div>
                    <div class="component-position-x">Conn Pos X</div>
                    <div class="component-position-y">Conn Pos Y</div>
                    <div class="delete-command">&nbsp;</div>
                </div>
            </div>
            <div class="connections">
                <div *ngFor="let connector of connectors;let ix = index;" class="data-row">
                    <div class="component-type">
                        <kendo-dropdownlist
								[tabindex]="tabindex"
                                class="select"
                                name="modelType"
                                (selectionChange)="typeChange($event, ix)"
                                [data]="types"
                                [(ngModel)]="connectors[ix].type">
                        </kendo-dropdownlist>
                    </div>
                    <div class="component-label">
                        <input type="text"
                               [tabindex]="tabindex"
							   (blur)="reportChanges()"
                               [(ngModel)]="connectors[ix].label"
                               name="labelValue">
                    </div>
                    <div class="component-label-position">
                        <kendo-dropdownlist
                                [tabindex]="tabindex"
                                class="select"
								(selectionChange)="positionChange($event, ix)"
                                name="modelPosition"
                                [data]="positions"
                                [(ngModel)]="connectors[ix].labelPosition">
                        </kendo-dropdownlist>
                    </div>
                    <div class="component-position-x">
                        <input type="number"
                               [tabindex]="tabindex"
                               (blur)="reportChanges()"
							   [(ngModel)]="connectors[ix].connectorPosX" name="connectorPosX">
                    </div>
                    <div class="component-position-y">
                        <input type="number"
                               [tabindex]="tabindex"
                               (blur)="reportChanges()"
							   [(ngModel)]="connectors[ix].connectorPosY" name="connectorPosY">
                    </div>
                    <div class="delete-command">
						<span class="glyphicon glyphicon-remove delete-connector" [tabindex]="tabindex" (click)="onDelete(ix)" title="Delete connector"></span>
					</div>
                </div>
            </div>
			<div *ngIf="hasErrors" class="duplicated-labels">There are duplicated labels</div>
        </div>
	`
})
export class ConnectorComponent implements OnInit {
	@Input('tabindex') tabindex: string;
	@Input('connectors') originalConnectors: Connector[];
	@Output('modelChange') modelChange = new EventEmitter<any>();
	order = 0;
	positions: string[];
	types: string[];
	modelTypeSelected: string;
	connectors: Connector[];
	hasErrors: boolean;
	readonly connectorLabel = 'Connector';

	constructor() {
		this.positions = ['Right', 'Left', 'Top', 'Bottom'];
		this.types = ['Ether', 'Serial', 'Power', 'Fiber', 'SCSI', 'USB', 'KVM', 'ILO', 'Management', 'SAS', 'Other'];
		this.modelTypeSelected = null;
		this.hasErrors = false;
	}

	ngOnInit() {
		this.connectors = [...this.originalConnectors];
	}

	/**
	 * Add a new empty connector element
	 */
	onAdd(): void {
		let count = 0;
		let nextLabel = '';

		do {
			count += 1;
			nextLabel = `${this.connectorLabel}${count}`;
		} while (this.connectors.find((connector: Connector) => connector.label.toLowerCase() === nextLabel.toLowerCase()));

		const connector: Connector = { id: null, type: 'Ether', label: nextLabel, labelPosition: 'Right', connectorPosX: 0, connectorPosY: 0};
		this.connectors.push(connector);
		this.reportChanges();
	}

	/**
	 * Used by ngFor in order to improve it
	 * @param {number} index
	 * @param obj
	 * @returns {any}
	 */

	trackByIndex(index: number, obj: any): any {
		return index;
	}

	/**
	 * Delete an connector and report the changes to the host component
	 * @param {number} index
	 */
	onDelete(index: number): void {
		this.connectors.splice(index, 1);
		this.reportChanges();
	}

	/**
	 * On position change update the connectors collection
	 * @param value
	 * @param {number} index
	 */
	positionChange(value: any, index: number): void {
		this.connectors[index].labelPosition = value;
		this.reportChanges();
	}

	/**
	 * On type change update the connectors collection
	 * @param value
	 * @param {number} index
	 */
	typeChange(value: any, index: number): void {
		this.connectors[index].type = value;
		this.reportChanges();
	}

	/**
	 * Determine if the connectors has duplicated labels
	 * @returns {boolean}
	 */
	hasDuplicatedLabels(): boolean {
		const items = [];

		this.connectors.forEach((connector: Connector) => {
			const label = connector.label.toLowerCase();
			if (items.indexOf(label) === -1) {
				items.push(label);
			}
		});

		return items.length !== this.connectors.length;
	}

	/**
	 * Report about connector changes to the host component
	 */
	reportChanges(): void {
		this.hasErrors = this.hasDuplicatedLabels();

		const added = this.connectors.filter((item) => item.id === null);

		const edited = this.connectors.filter((item) => {
			return this.originalConnectors.find((original) => original.id === item.id);
		});

		const deleted = this.originalConnectors.filter((item) => {
			return !this.connectors.find((original) => original.id === item.id);
		});

		const changes = {
			added: this.addConnectorOrder(added),
			edited: this.addConnectorOrder(edited),
			deleted: this.addConnectorOrder(deleted)
		};

		this.modelChange.emit(changes);
	}

	/**
	 * Add to each connector the order
	 * @param {any[]} items collection items
	 * @returns {any}
	 */
	addConnectorOrder(items: any[]): any {
		return  items.map((item: any) => {
			this.order = this.order + 1;
			return {
				id: item.id,
				type: item.type,
				label: item.label,
				labelPosition: item.labelPosition,
				xPosition: item.connectorPosX,
				yPosition: item.connectorPosY,
				connector: item.id || this.order
			};
		});
	}
}