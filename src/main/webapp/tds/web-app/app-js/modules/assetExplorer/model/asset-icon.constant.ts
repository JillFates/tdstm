import {IconModel} from 'tds-component-library/lib/diagram-layout/model/diagram-data.model';

export const ASSET_ICONS: IconModel = {
	application: {
		iconAlt: '\uf04b',
		// tslint:disable-next-line:max-line-length
		icon: '../assets/icons/svg/application.svg',
		color: '#fff',
		background: '#01ab08'
	},
	// play: '\uf144',
	database: {
		iconAlt: '\uf04c',
		// tslint:disable-next-line:max-line-length
		icon: '/./../../../../icons/svg/database.svg',
		color: '#fff',
		background: '#fff200'
	},
	// pause: '\uf28c',
	server: {
		iconAlt: '\uf017',
		// tslint:disable-next-line:max-line-length
		icon: '/./../../../../icons/svg/serverPhysical.svg',
		color: '#fff',
		background: '#fff200'
	},
	// clock: '\uf017',
	virtualServer: {
		iconAlt: '\uf068',
		// tslint:disable-next-line:max-line-length
		icon: '/./../../../../icons/svg/serverVirtual.svg',
		color: '#fff',
		background: '#9e9e9e'
	},
	logicalStorage: {
		iconAlt: '\uf00c',
		// tslint:disable-next-line:max-line-length
		icon: '/./../../../../icons/svg/storageLogical.svg',
		color: '#fff',
		background: '#0b63a8'
	},
	device: {
		iconAlt: '\uf04e',
		// tslint:disable-next-line:max-line-length
		icon: '/./../../../../icons/svg/device_menu.svg',
		color: '#fff',
		background: '#99d9ea'
	}
};
