import net.transitionmanager.domain.Manufacturer
import net.transitionmanager.domain.Model
import net.transitionmanager.domain.Person

databaseChangeLog = {

	changeSet(author: "jmartin", id: "20140927 TM-3397-1") {
		comment('Rename the Generic Manufacturer and Models to to Unidentified plus fill in the missing models')
		
		grailsChange {
			change {

				def manu = new Manufacturer([name:'Unspecified', description:'Used to identify devices when the manufacturer or model are unknown'])
				if (manu.validate() && manu.save(flush:true)) {

					String desc = 'with an unspecified manufacturer and model'
					// All of these will be created by John Martin
					Person john = Person.get(100)

					Map commonProps = [
						manufacturer: manu,
						createdBy: john,
						assetType: 'Server',
						description: "A server $desc",
						
					]

					def model

					// Create the various Server, Device and VM and Blade types of models

					// Server usize 1..7
					(1..7).each { usize -> 
						model = new Model(commonProps)
						model.usize = usize
						model.modelName = "Unspecified Server (${usize}U)"
						if (! model.validate() || ! model.save()) {
							println "Unable to create Server $usize"
						}
					}

					// Server u-size unknown
					commonProps.usize = null
					model = new Model(commonProps)
					model.modelName = "Unspecified Server (u-size unknown)"
					if (!model.validate() || !model.save()) {
						println "Unable to create Server (unknown u-size)"
					}


					// Device usize 1..7
					commonProps.assetType = 'Unspecified'
					(1..7).each { usize -> 
						model = new Model(commonProps)
						model.usize = usize
						model.modelName = "Unspecified Device (${usize}U)"
						if (!model.validate() || !model.save()) {
							println "Unable to create Device $usize"
						}
					}

					// Device u-size unknown
					model = new Model(commonProps)
					model.modelName = "Unspecified Device (u-size unknown)"
					if (!model.validate() || !model.save()) {
						println "Unable to create Device (unknown u-size)"
					}

					// Blades
					commonProps.assetType = 'Blade'
					commonProps.description = "A blade $desc"

					['Full','Half'].each { height ->
						model = new Model(commonProps)
						model.bladeHeight = height 
						model.modelName = "Unspecified Blade ($height)"
						if (! model.validate() || model.save()) {
							println "Unable to create blade $height"
						}
					}

					// VMs
					model = new Model(commonProps)
					model.modelName = "Unspecified VM"
					model.description = "A VM $desc"
					model.assetType = 'VM'
					if (!model.validate() || !model.save()) {
						println "Unable to create server unknown u-size"
					}

				} else {
					println "Unable to create manufacturer"
				}
			}
		}
	}
}
