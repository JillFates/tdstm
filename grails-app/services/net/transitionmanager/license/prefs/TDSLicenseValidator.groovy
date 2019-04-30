package net.transitionmanager.license.prefs

import net.nicholaswilliams.java.licensing.License
import net.nicholaswilliams.java.licensing.LicenseValidator
import net.nicholaswilliams.java.licensing.exception.ExpiredLicenseException
import net.nicholaswilliams.java.licensing.exception.InvalidLicenseException

import java.text.SimpleDateFormat

/**
 * Created by octavio on 12/22/16.
 */
class TDSLicenseValidator implements LicenseValidator {

	@Override
	void validateLicense(License license) throws InvalidLicenseException {
		long var2 = Calendar.getInstance().getTimeInMillis();
		if(license.getGoodAfterDate() > var2) {
			throw new InvalidLicenseException("The " + this.getLicenseDescription(license) + " does not take effect until " + this.getFormattedDate(license.getGoodAfterDate()) + ".");
		} else if(license.getGoodBeforeDate() < var2) {
			throw new ExpiredLicenseException("The " + this.getLicenseDescription(license) + " expired on " + this.getFormattedDate(license.getGoodAfterDate()) + ".");
		}
	}

	String getLicenseDescription(License license) {
		license.getSubject() + " license for " + license.getHolder()
	}

	String getFormattedDate(long epoc) {
		(new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z (Z)")).format(new Date(epoc))
	}
}
