export interface AkaParent {
	id: number;
	name: string;
}
export interface Aka {
	id: number;
	value: string;
}

export interface AkaChanges {
	deleted: Array<Aka>;
	edited: Array<Aka>;
	added: Array<Aka>;
}