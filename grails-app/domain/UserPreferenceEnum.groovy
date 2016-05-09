enum UserPreferenceEnum {
	ImportApplication, ImportServer, ImportDatabase, ImportStorage, ImportDependency, ImportCabling, ImportComment, ImportRoom, ImportRack
	
	static getImportPreferenceKeys() {
		return [ImportApplication, ImportServer, ImportDatabase, ImportStorage, ImportDependency, ImportCabling, ImportComment]
	}

	static getExportPreferenceKeys() {
		return [ImportApplication,ImportServer,ImportDatabase,ImportStorage,ImportDependency,ImportRoom,ImportRack, ImportCabling,ImportComment]
	}

}
