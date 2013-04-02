package com.tds.asset

public enum AssetType {
	SERVER('Server'),
	VM('VM'),
	APPLICATION('Application'),
	DATABASE('Database'),
	FILES('Files'),
	NETWORK('Network'),
	BLADE('Blade')

	String name
	AssetType(String name) {
		this.name = name
	}
	
	String toString() { name }
}