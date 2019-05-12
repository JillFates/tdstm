interface Support {
	type: string;
	class: string;
	name: string;
	frequency: string;
	bundle: string;
	status: string;
}

interface Dependency {
	type: string;
	class: string;
	name: string;
	frequency: string;
	bundle: string;
	status: string;
}

export interface EntityConflict {
	entity: {
		id: number;
		name: string;
	};
	bundle: {
		id: number;
		name: string;
	};
	supports: Array<Support>;
	dependencies: Array<Dependency>;
}