package com.tdsops.common.security.spring

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import java.security.MessageDigest

/**
 * This bean is used to tag SQS messages using message digest (hostname + address)
 * so consumer of this message can filter by the message owner identifier.
 *
 * Example message tagged with this hostname identifier digest
 * {
 * "taskId":123,
 * "serverRefId":"server02",
 * "ttl":"1",
 * "status":"success",
 * "messageOwner":"3824c2007a2b0aa0d036d00a6de37dcd", <<==
 * "callbackMethod":"doSomething"
 * }
 */
@Slf4j
@CompileStatic
class CamelHostnameIdentifier {
    private static final String hostnameIdentifierDigest;

    static {
        String hostIdentifierMD5 = "[]"
        try {
            InetAddress addr = InetAddress.getLocalHost();
            String hostName = addr.getHostName();
            String hostAddr = addr.getHostAddress();
            String hostIdentifier = "[" + hostName + "::" + hostAddr + "]"
            hostIdentifierMD5 = MessageDigest.getInstance("MD5").digest(hostIdentifier.bytes).encodeHex().toString()
            log.debug("Camel Host Identifier: " + hostIdentifier + " Digest => " + hostIdentifierMD5)
        } catch (UnknownHostException e) {
            e.printStackTrace()
        }
        hostnameIdentifierDigest = hostIdentifierMD5
    }

    String getHostnameIdentifierDigest() {
        return hostnameIdentifierDigest
    }

}
