export class AgentModel {
	id?: string;
	name?: string;
}

export class CredentialModel {
	id?: string;
	name?: string;
}

export class AgentMethodModel {
	id?: number;
	name?: string;
	description?: string;
	method?: string;
	params?: {};
	results?: {};
}