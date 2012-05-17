package com.tds.asset

public enum AssetType {
	SERVER('Server'),
	VM('VM'),
	APPLICATION('Application'),
	DATABASE('Database'),
	FILES('Files')

	String name
	AssetType(String name) {
		this.name = name
	}
	
	String toString() { name }
}