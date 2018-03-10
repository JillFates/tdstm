import {GridColumnModel} from '../../../shared/model/data-list-grid.model';
import {ProviderModel} from '../../dataIngestion/model/provider.model';
import {DataScriptModel} from '../../dataIngestion/model/data-script.model';
import {ProjectModel} from '../../../shared/model/project.model';
import {EnumModel} from '../../../shared/model/enum.model';

export enum BatchStatus {
	RUNNING = 'RUNNING',
	PENDING = 'PENDING',
	QUEUED = 'QUEUED',
	IGNORED = 'IGNORED'
};

export class ImportBatchModel {
	id: number;
	status: EnumModel;
	domainClassName: any;
	project: ProjectModel;
	provider: ProviderModel;
	datascript: DataScriptModel;
	createdBy: string;
	archived: boolean;
	timezone: string;
	dateFormat: string;
	progressInfoJob: any;
	originalFilename: string;
	nullIndicator: string;
	overwriteWithBlanks: number;
	autoProcess: number;
	warnOnChangesAfter: any;
	fieldNameList: Array<string>;
	dateCreated: Date;
	lastUpdated: Date;
	// TODO: check if this is going to be implemented on backend ..
	records: number;
	errors: number;
	pending: number;
	processed: number;
	ignored: number;
	currentProgress?: number;
}

/**
 * Configuration of the Import Batches Grid.
 * Defines the columns.
 */
export class DependencyBatchColumnsModel {
	columns: Array<GridColumnModel>;

	constructor() {
		this.columns = [
			// {
			// 	label: 'Action',
			// 	property: 'action',
			// 	type: 'action',
			// 	width: 70,
			// 	locked: false,
			// 	cellStyle: {'text-align': 'center'}
			// },
			{
				label: 'Id',
				property: 'id',
				type: 'number',
				width: 60,
				locked: false
			},
			{
				label: 'Status',
				property: 'status',
				type: 'text',
				width: 100,
				locked: false
			},
			{
				label: 'Imported At',
				property: 'dateCreated',
				type: 'date',
				format: '{0:yyyy/MM/dd HH:mm:ss}',
				width: 200,
				locked: false
			},
			{
				label: 'Imported By',
				property: 'createdBy',
				type: 'text',
				width: 100,
				locked: false
			},
			{
				label: 'Domain',
				property: 'domainClassName.name',
				type: 'text',
				width: 130,
				locked: false
			},
			{
				label: 'Provider',
				property: 'provider.name',
				type: 'text',
				width: 130,
				locked: false
			},
			{
				label: 'Datascript',
				property: 'datascript.name',
				type: 'text',
				width: 130,
				locked: false
			},
			{
				label: 'File Name',
				property: 'originalFilename',
				type: 'text',
				width: 150,
				locked: false
			},
			{
				label: 'Records',
				property: 'records',
				type: 'number',
				width: 80,
				locked: false
			},
			{
				label: 'Errors',
				property: 'errors',
				type: 'number',
				width: 80,
				locked: false
			},
			{
				label: 'Pending',
				property: 'pending',
				type: 'number',
				width: 80,
				locked: false
			},
			{
				label: 'Processed',
				property: 'processed',
				type: 'number',
				width: 100,
				locked: false
			},
			{
				label: 'Ignored',
				property: 'ignored',
				type: 'number',
				width: 80,
				locked: false
			},
		];
	}
}