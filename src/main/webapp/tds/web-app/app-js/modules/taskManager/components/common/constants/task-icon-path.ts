export const STATE_ICONS_PATH = {
	started: {
		iconAlt: '\uf1ce',
		// tslint:disable-next-line:max-line-length
		icon: 'F M224.3 273l-136 136c-9.4 9.4-24.6 9.4-33.9 0l-22.6-22.6c-9.4-9.4-9.4-24.6 0-33.9l96.4-96.4-96.4-96.4c-9.4-9.4-9.4-24.6 0-33.9L54.3 103c9.4-9.4 24.6-9.4 33.9 0l136 136c9.5 9.4 9.5 24.6.1 34zm192-34l-136-136c-9.4-9.4-24.6-9.4-33.9 0l-22.6 22.6c-9.4 9.4-9.4 24.6 0 33.9l96.4 96.4-96.4 96.4c-9.4 9.4-9.4 24.6 0 33.9l22.6 22.6c9.4 9.4 24.6 9.4 33.9 0l136-136c9.4-9.2 9.4-24.4 0-33.8z',
		color: '#01ab08',
		background: '#ffffff'
	},
	// play: '\uf144',
	hold: {
		iconAlt: '\uf04c',
		// tslint:disable-next-line:max-line-length
		icon: 'F M144 479H48c-26.5 0-48-21.5-48-48V79c0-26.5 21.5-48 48-48h96c26.5 0 48 21.5 48 48v352c0 26.5-21.5 48-48 48zm304-48V79c0-26.5-21.5-48-48-48h-96c-26.5 0-48 21.5-48 48v352c0 26.5 21.5 48 48 48h96c26.5 0 48-21.5 48-48z',
		color: '#585656',
		background: '#fff200'
	},
	// pause: '\uf28c',
	pending: {
		iconAlt: '\uf254',
		// tslint:disable-next-line:max-line-length
		icon: 'F M360 64c13.255 0 24-10.745 24-24V24c0-13.255-10.745-24-24-24H24C10.745 0 0 10.745 0 24v16c0 13.255 10.745 24 24 24 0 90.965 51.016 167.734 120.842 192C75.016 280.266 24 357.035 24 448c-13.255 0-24 10.745-24 24v16c0 13.255 10.745 24 24 24h336c13.255 0 24-10.745 24-24v-16c0-13.255-10.745-24-24-24 0-90.965-51.016-167.734-120.842-192C308.984 231.734 360 154.965 360 64z',
		color: '#898989',
		background: '#fff'
	},
	completed: {
		iconAlt: '\uf00c',
		// tslint:disable-next-line:max-line-length
		icon: 'F M173.898 439.404l-166.4-166.4c-9.997-9.997-9.997-26.206 0-36.204l36.203-36.204c9.997-9.998 26.207-9.998 36.204 0L192 312.69 432.095 72.596c9.997-9.997 26.207-9.997 36.204 0l36.203 36.204c9.997 9.997 9.997 26.206 0 36.204l-294.4 294.401c-9.998 9.997-26.207 9.997-36.204-.001z',
		color: '#fff',
		background: '#0b63a8'
	},
	forward: {
		iconAlt: '\uf04e',
		// tslint:disable-next-line:max-line-length
		icon: 'F M500.5 231.4l-192-160C287.9 54.3 256 68.6 256 96v320c0 27.4 31.9 41.8 52.5 24.6l192-160c15.3-12.8 15.3-36.4 0-49.2zm-256 0l-192-160C31.9 54.3 0 68.6 0 96v320c0 27.4 31.9 41.8 52.5 24.6l192-160c15.3-12.8 15.3-36.4 0-49.2z',
		color: '#fff',
		background: '#99d9ea'
	},
	ready: {
		iconAlt: '\uf164',
		// tslint:disable-next-line:max-line-length
		icon: 'F M104 224H24c-13.255 0-24 10.745-24 24v240c0 13.255 10.745 24 24 24h80c13.255 0 24-10.745 24-24V248c0-13.255-10.745-24-24-24zM64 472c-13.255 0-24-10.745-24-24s10.745-24 24-24 24 10.745 24 24-10.745 24-24 24zM384 81.452c0 42.416-25.97 66.208-33.277 94.548h101.723c33.397 0 59.397 27.746 59.553 58.098.084 17.938-7.546 37.249-19.439 49.197l-.11.11c9.836 23.337 8.237 56.037-9.308 79.469 8.681 25.895-.069 57.704-16.382 74.757 4.298 17.598 2.244 32.575-6.148 44.632C440.202 511.587 389.616 512 346.839 512l-2.845-.001c-48.287-.017-87.806-17.598-119.56-31.725-15.957-7.099-36.821-15.887-52.651-16.178-6.54-.12-11.783-5.457-11.783-11.998v-213.77c0-3.2 1.282-6.271 3.558-8.521 39.614-39.144 56.648-80.587 89.117-113.111 14.804-14.832 20.188-37.236 25.393-58.902C282.515 39.293 291.817 0 312 0c24 0 72 8 72 81.452z',
		color: '#fff',
		background: '#01ab08'
	}
};

export const ASSET_ICONS_PATH = {
	unknown: {
		name: 'unknown',
		iconAlt: '\uf0c8',
		// tslint:disable-next-line:max-line-length
		icon: 'F M400 32H48C21.5 32 0 53.5 0 80v352c0 26.5 21.5 48 48 48h352c26.5 0 48-21.5 48-48V80c0-26.5-21.5-48-48-48z',
		color: '#898989',
		background: 'transparent',
		iconName: 'faBox'
	},
	physicalserver: {
		name: 'physical server',
		iconAlt: '\uf233',
		// tslint:disable-next-line:max-line-length
		icon: 'F M12.41 148.02l232.94 105.67c6.8 3.09 14.49 3.09 21.29 0l232.94-105.67c16.55-7.51 16.55-32.52 0-40.03L266.65 2.31a25.607 25.607 0 0 0-21.29 0L12.41 107.98c-16.55 7.51-16.55 32.53 0 40.04zm487.18 88.28l-58.09-26.33-161.64 73.27c-7.56 3.43-15.59 5.17-23.86 5.17s-16.29-1.74-23.86-5.17L70.51 209.97l-58.1 26.33c-16.55 7.5-16.55 32.5 0 40l232.94 105.59c6.8 3.08 14.49 3.08 21.29 0L499.59 276.3c16.55-7.5 16.55-32.5 0-40zm0 127.8l-57.87-26.23-161.86 73.37c-7.56 3.43-15.59 5.17-23.86 5.17s-16.29-1.74-23.86-5.17L70.29 337.87 12.41 364.1c-16.55 7.5-16.55 32.5 0 40l232.94 105.59c6.8 3.08 14.49 3.08 21.29 0L499.59 404.1c16.55-7.5 16.55-32.5 0-40z',
		color: '#9e9e9e',
		background: 'transparent',
		iconName: 'faServer'
	},
	server: {
		name: 'server',
		iconAlt: '\uf233',
		// tslint:disable-next-line:max-line-length
		icon: 'F M480 160H32c-17.673 0-32-14.327-32-32V64c0-17.673 14.327-32 32-32h448c17.673 0 32 14.327 32 32v64c0 17.673-14.327 32-32 32zm-48-88c-13.255 0-24 10.745-24 24s10.745 24 24 24 24-10.745 24-24-10.745-24-24-24zm-64 0c-13.255 0-24 10.745-24 24s10.745 24 24 24 24-10.745 24-24-10.745-24-24-24zm112 248H32c-17.673 0-32-14.327-32-32v-64c0-17.673 14.327-32 32-32h448c17.673 0 32 14.327 32 32v64c0 17.673-14.327 32-32 32zm-48-88c-13.255 0-24 10.745-24 24s10.745 24 24 24 24-10.745 24-24-10.745-24-24-24zm-64 0c-13.255 0-24 10.745-24 24s10.745 24 24 24 24-10.745 24-24-10.745-24-24-24zm112 248H32c-17.673 0-32-14.327-32-32v-64c0-17.673 14.327-32 32-32h448c17.673 0 32 14.327 32 32v64c0 17.673-14.327 32-32 32zm-48-88c-13.255 0-24 10.745-24 24s10.745 24 24 24 24-10.745 24-24-10.745-24-24-24zm-64 0c-13.255 0-24 10.745-24 24s10.745 24 24 24 24-10.745 24-24-10.745-24-24-24z',
		color: '#9e9e9e',
		background: 'transparent',
		iconName: 'faServer'
	},
	database: {
		name: 'database',
		iconAlt: '\uf1c0',
		// tslint:disable-next-line:max-line-length
		icon: 'F M448 73.143v45.714C448 159.143 347.667 192 224 192S0 159.143 0 118.857V73.143C0 32.857 100.333 0 224 0s224 32.857 224 73.143zM448 176v102.857C448 319.143 347.667 352 224 352S0 319.143 0 278.857V176c48.125 33.143 136.208 48.572 224 48.572S399.874 209.143 448 176zm0 160v102.857C448 479.143 347.667 512 224 512S0 479.143 0 438.857V336c48.125 33.143 136.208 48.572 224 48.572S399.874 369.143 448 336z',
		color: '#9e9e9e',
		background: 'transparent',
		iconName: 'faDatabase'
	},
	shutdown: {
		name: 'shutdown',
		iconAlt: '\uf011',
		// tslint:disable-next-line:max-line-length
		icon: 'F M425.7 256c-16.9 0-32.8-9-41.4-23.4L320 126l-64.2 106.6c-8.7 14.5-24.6 23.5-41.5 23.5-4.5 0-9-.6-13.3-1.9L64 215v178c0 14.7 10 27.5 24.2 31l216.2 54.1c10.2 2.5 20.9 2.5 31 0L551.8 424c14.2-3.6 24.2-16.4 24.2-31V215l-137 39.1c-4.3 1.3-8.8 1.9-13.3 1.9zm212.6-112.2L586.8 41c-3.1-6.2-9.8-9.8-16.7-8.9L320 64l91.7 152.1c3.8 6.3 11.4 9.3 18.5 7.3l197.9-56.5c9.9-2.9 14.7-13.9 10.2-23.1zM53.2 41L1.7 143.8c-4.6 9.2.3 20.2 10.1 23l197.9 56.5c7.1 2 14.7-1 18.5-7.3L320 64 69.8 32.1c-6.9-.8-13.5 2.7-16.6 8.9z',
		color: '#9e9e9e',
		background: 'transparent',
		iconName: 'faPowerOff'
	},
	moveday: {
		name: 'moveday',
		iconAlt: '\uf4df',
		// tslint:disable-next-line:max-line-length
		icon: 'F M624 352h-16V243.9c0-12.7-5.1-24.9-14.1-33.9L494 110.1c-9-9-21.2-14.1-33.9-14.1H416V48c0-26.5-21.5-48-48-48H48C21.5 0 0 21.5 0 48v320c0 26.5 21.5 48 48 48h16c0 53 43 96 96 96s96-43 96-96h128c0 53 43 96 96 96s96-43 96-96h48c8.8 0 16-7.2 16-16v-32c0-8.8-7.2-16-16-16zM160 464c-26.5 0-48-21.5-48-48s21.5-48 48-48 48 21.5 48 48-21.5 48-48 48zm320 0c-26.5 0-48-21.5-48-48s21.5-48 48-48 48 21.5 48 48-21.5 48-48 48zm80-208H416V144h44.1l99.9 99.9V256z',
		color: '#9e9e9e',
		background: 'transparent',
		iconName: 'faQuestion'
	},
	general: {
		name: 'general',
		iconAlt: '\uf00c',
		// tslint:disable-next-line:max-line-length
		icon: 'F M488.6 250.2L392 214V105.5c0-15-9.3-28.4-23.4-33.7l-100-37.5c-8.1-3.1-17.1-3.1-25.3 0l-100 37.5c-14.1 5.3-23.4 18.7-23.4 33.7V214l-96.6 36.2C9.3 255.5 0 268.9 0 283.9V394c0 13.6 7.7 26.1 19.9 32.2l100 50c10.1 5.1 22.1 5.1 32.2 0l103.9-52 103.9 52c10.1 5.1 22.1 5.1 32.2 0l100-50c12.2-6.1 19.9-18.6 19.9-32.2V283.9c0-15-9.3-28.4-23.4-33.7zM358 214.8l-85 31.9v-68.2l85-37v73.3zM154 104.1l102-38.2 102 38.2v.6l-102 41.4-102-41.4v-.6zm84 291.1l-85 42.5v-79.1l85-38.8v75.4zm0-112l-102 41.4-102-41.4v-.6l102-38.2 102 38.2v.6zm240 112l-85 42.5v-79.1l85-38.8v75.4zm0-112l-102 41.4-102-41.4v-.6l102-38.2 102 38.2v.6z',
		color: '#9e9e9e',
		background: 'transparent',
		iconName: 'faLaptop'
	},
	application: {
		name: 'application',
		iconAlt: '\uf170',
		// tslint:disable-next-line:max-line-length
		icon: 'F M248 167.5l64.9 98.8H183.1l64.9-98.8zM496 256c0 136.9-111.1 248-248 248S0 392.9 0 256 111.1 8 248 8s248 111.1 248 248zm-99.8 82.7L248 115.5 99.8 338.7h30.4l33.6-51.7h168.6l33.6 51.7h30.2z',
		color: '#0b63a8',
		background: 'transparent',
		iconName: 'faAdn'
	},
	// virtual machine
	vm: {
		name: 'vm',
		iconAlt: '\uf233',
		// tslint:disable-next-line:max-line-length
		icon: 'F M528 0H48C21.5 0 0 21.5 0 48v320c0 26.5 21.5 48 48 48h192l-16 48h-72c-13.3 0-24 10.7-24 24s10.7 24 24 24h272c13.3 0 24-10.7 24-24s-10.7-24-24-24h-72l-16-48h192c26.5 0 48-21.5 48-48V48c0-26.5-21.5-48-48-48zm-16 352H64V64h448v288z',
		color: '#9e9e9e',
		background: 'transparent',
		iconName: 'faLaptop'
	},
	storage: {
		name: 'storage',
		iconAlt: '\uf07b',
		// tslint:disable-next-line:max-line-length
		icon: 'F M464 128H272l-64-64H48C21.49 64 0 85.49 0 112v288c0 26.51 21.49 48 48 48h416c26.51 0 48-21.49 48-48V176c0-26.51-21.49-48-48-48z',
		color: '#9e9e9e',
		background: 'transparent',
		iconName: 'faFolder'
	},
	network: {
		name: 'network',
		iconAlt: '\uf0e8',
		// tslint:disable-next-line:max-line-length
		icon: 'F M640 264v-16c0-8.84-7.16-16-16-16H344v-40h72c17.67 0 32-14.33 32-32V32c0-17.67-14.33-32-32-32H224c-17.67 0-32 14.33-32 32v128c0 17.67 14.33 32 32 32h72v40H16c-8.84 0-16 7.16-16 16v16c0 8.84 7.16 16 16 16h104v40H64c-17.67 0-32 14.33-32 32v128c0 17.67 14.33 32 32 32h160c17.67 0 32-14.33 32-32V352c0-17.67-14.33-32-32-32h-56v-40h304v40h-56c-17.67 0-32 14.33-32 32v128c0 17.67 14.33 32 32 32h160c17.67 0 32-14.33 32-32V352c0-17.67-14.33-32-32-32h-56v-40h104c8.84 0 16-7.16 16-16zM256 128V64h128v64H256zm-64 320H96v-64h96v64zm352 0h-96v-64h96v64z',
		color: '#9e9e9e',
		background: 'transparent',
		iconName: 'faSitemap'
	},
	logicalstorage: {
		name: 'storage',
		iconAlt: '\uf07b',
		// tslint:disable-next-line:max-line-length
		icon: 'F M464 128H272l-64-64H48C21.49 64 0 85.49 0 112v288c0 26.51 21.49 48 48 48h416c26.51 0 48-21.49 48-48V176c0-26.51-21.49-48-48-48z',
		color: '#9e9e9e',
		background: 'transparent',
		iconName: 'faFolder'
	},
	virtualserver: {
		name: 'virtual server',
		iconAlt: '\uf233',
		// tslint:disable-next-line:max-line-length
		icon: 'F M464 128H272l-64-64H48C21.49 64 0 85.49 0 112v288c0 26.51 21.49 48 48 48h416c26.51 0 48-21.49 48-48V176c0-26.51-21.49-48-48-48z',
		color: '#9e9e9e',
		background: 'transparent',
		iconName: 'faServer'
	},
	physicalstorage: {
		name: 'physical storage',
		iconAlt: '\uf0a0',
		// tslint:disable-next-line:max-line-length
		icon: 'F M464 128H272l-64-64H48C21.49 64 0 85.49 0 112v288c0 26.51 21.49 48 48 48h416c26.51 0 48-21.49 48-48V176c0-26.51-21.49-48-48-48z',
		color: '#9e9e9e',
		background: 'transparent',
		iconName: 'faHdd'
	},
};

export const CTX_MENU_ICONS_PATH = {
	start: {
		name: 'started',
		iconAlt: '\uf00c',
		// tslint:disable-next-line:max-line-length
		icon: 'faPlay',
		color: '#01ab08'
	},
	done: {
		name: 'done',
		iconAlt: '\uf00c',
		// tslint:disable-next-line:max-line-length
		icon: 'faCheck',
		color: '#0b63a8'
	},
	hold: {
		name: 'hold',
		iconAlt: '\uf00c',
		// tslint:disable-next-line:max-line-length
		icon: 'faPause',
		color: '#fff200'
	},
	invoke: {
		name: 'invoke',
		iconAlt: '\uf085',
		// tslint:disable-next-line:max-line-length
		icon: 'faCogs',
		color: '#9e9e9e'
	},
	edit: {
		name: 'edit',
		iconAlt: '\uf044',
		// tslint:disable-next-line:max-line-length
		icon: 'faEdit',
		color: '#9e9e9e'
	},
	view: {
		name: 'view',
		iconAlt: '\uf06e',
		// tslint:disable-next-line:max-line-length
		icon: 'faEye',
		color: '#9e9e9e'
	},
	assignToMe: {
		name: 'assignToMe',
		iconAlt: '\uf007',
		// tslint:disable-next-line:max-line-length
		icon: 'faUser',
		color: '#9e9e9e'
	},
	reset: {
		name: 'reset',
		iconAlt: '\uf0e2',
		// tslint:disable-next-line:max-line-length
		icon: 'faUndo',
		color: '#9e9e9e'
	},
	neighborhood: {
		name: 'neighborhood',
		iconAlt: '\uf015',
		// tslint:disable-next-line:max-line-length
		icon: 'faHome',
		color: '#9e9e9e'
	},
	assetDetail: {
		name: 'assetDetail',
		iconAlt: '\uf1ad',
		// tslint:disable-next-line:max-line-length
		icon: 'faBuilding',
		color: '#9e9e9e'
	}
};
