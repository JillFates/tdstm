import grails.test.mixin.TestFor
import net.transitionmanager.license.LicenseActivityTrack
import spock.lang.Specification

/**
 * Created by octavio on 2/21/17.
 */

@TestFor(LicenseActivityTrack)
class LicenseActivityTrackTests extends Specification{
	/*
	def '1. Test Activity Track change on Licensed Client' () {
		setup:
			LicensedClient licensedClient = new LicensedClient()
			licensedClient.save(flush: true)

		when:
			licensedClient.max = 13
			licensedClient.activationDate = new Date()
			licensedClient.expirationDate = new Date()
			licensedClient.save(flush: true)

		then:
			LicenseActivityTrack licenseActivityTrack = LicenseActivityTrack.findByLicensedClient(licensedClient)
			assert licenseActivityTrack != null

	}
	*/
}
