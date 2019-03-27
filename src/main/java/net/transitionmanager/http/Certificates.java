package net.transitionmanager.http;

import java.security.cert.X509Certificate;
import java.security.cert.CertificateParsingException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;


/**
 * Class used to extract the list of CNs and Subject Alternative Name (SAN)
 * from given X.509 certificates
 *
 * @see
 * <code>https://alvinalexander.com/java/jwarehouse/axis2-1.3/modules/kernel/src/org/apache/axis2/transport/nhttp/HostnameVerifier.java.shtml</code>
 */
public class Certificates {

	public static String[] getCNs(X509Certificate cert) {
		LinkedList cnList = new LinkedList();
            /*
           Sebastian Hauer's original StrictSSLProtocolSocketFactory used
           getName() and had the following comment:

              Parses a X.500 distinguished name for the value of the
              "Common Name" field.  This is done a bit sloppy right
              now and should probably be done a bit more according to
              <code>RFC 2253.

            I've noticed that toString() seems to do a better job than
            getName() on these X500Principal objects, so I'm hoping that
            addresses Sebastian's concern.

            For example, getName() gives me this:
            1.2.840.113549.1.9.1=#16166a756c6975736461766965734063756362632e636f6d

            whereas toString() gives me this:
            EMAILADDRESS=juliusdavies@cucbc.com

            Looks like toString() even works with non-ascii domain names!
            I tested it with "花子.co.jp" and it worked fine.
           */
		String subjectPrincipal = cert.getSubjectX500Principal().toString();
		StringTokenizer st = new StringTokenizer(subjectPrincipal, ",");
		while (st.hasMoreTokens()) {
			String tok = st.nextToken();
			int x = tok.indexOf("CN=");
			if (x >= 0) {
				cnList.add(tok.substring(x + 3));
			}
		}
		if (!cnList.isEmpty()) {
			String[] cns = new String[cnList.size()];
			cnList.toArray(cns);
			return cns;
		} else {
			return null;
		}
	}

	/**
	 * Extracts the array of SubjectAlt DNS names from an X509Certificate.
	 * Returns null if there aren't any.
	 * <p/>
	 * Note:  Java doesn't appear able to extract international characters
	 * from the SubjectAlts.  It can only extract international characters
	 * from the CN field.
	 * <p/>
	 * (Or maybe the version of OpenSSL I'm using to test isn't storing the
	 * international characters correctly in the SubjectAlts?).
	 *
	 * @param cert X509Certificate
	 * @return Array of SubjectALT DNS names stored in the certificate.
	 */
	public static String[] getDNSSubjectAlts(X509Certificate cert) {
		LinkedList subjectAltList = new LinkedList();
		Collection c = null;
		try {
			c = cert.getSubjectAlternativeNames();
		}
		catch (CertificateParsingException cpe) {
			// Should probably log.debug() this?
			cpe.printStackTrace();
		}
		if (c != null) {
			Iterator it = c.iterator();
			while (it.hasNext()) {
				List list = (List) it.next();
				int type = ((Integer) list.get(0)).intValue();
				// If type is 2, then we've got a dNSName
				if (type == 2) {
					String s = (String) list.get(1);
					subjectAltList.add(s);
				}
			}
		}
		if (!subjectAltList.isEmpty()) {
			String[] subjectAlts = new String[subjectAltList.size()];
			subjectAltList.toArray(subjectAlts);
			return subjectAlts;
		} else {
			return null;
		}
	}
}
