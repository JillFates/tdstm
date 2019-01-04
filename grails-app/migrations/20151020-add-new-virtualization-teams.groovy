/**
   The following script adds virtualization teams (if not present) 
*/

databaseChangeLog = {	
	// add UnlockUserLogin permission
	changeSet(author: "arecordon", id: "20151020 TM-4170-1") {
		comment('Add Virtualization Teams')

		def teamCodes = ['VM_ADMIN_XEN', 'VM_ADMIN_UCS', 'VM_ADMIN_VMWARE', 'VM_ADMIN_HYPERV', 'VM_ADMIN_EC2']
		def teamDescriptions = ['Staff : VM Admin (Xen)', 'Staff : VM Admin (UCS)' , 'Staff : VM Admin (VMWare)', 'Staff : VM Admin (Hyper-V)', 'Staff : VM Admin (EC2)' ]

		for(i in 0..(teamCodes.size() -1)){
    		sql("INSERT IGNORE INTO role_type values('${teamCodes[i]}', '${teamDescriptions[i]}', null, 'TEAM', null)")
		}
	}

}
