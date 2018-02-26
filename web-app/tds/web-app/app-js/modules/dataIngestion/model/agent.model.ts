export class AgentModel {
	id?: number;
	name?: string;
}

export class CredentialModel {
	id?: number;
	name?: string;
	provider?: {
		id?: number,
		name: string
	};
}

export class AgentMethodModel {
	id?: number;
	name?: string;
	description?: string;
	method?: string;
	params?: {};
	results?: {};
}