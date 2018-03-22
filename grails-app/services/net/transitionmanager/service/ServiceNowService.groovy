package net.transitionmanager.service

import grails.transaction.Transactional
import groovy.util.logging.Slf4j
import net.transitionmanager.domain.Credential
import net.transitionmanager.integration.ActionRequest
import org.apache.http.HttpEntity
import org.apache.http.HttpResponse
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.ClientProtocolException
import org.apache.http.client.CredentialsProvider
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.commons.httpclient.HttpStatus
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.util.EntityUtils

@Transactional(readOnly = true)
@Slf4j
class ServiceNowService {
    private static final String DEFAULT_CHARACTER_ENCODING = 'UTF-8'
    private static final String FILENAME_PREFIX='servicenow-'

    CredentialService credentialService
    FileSystemService fileSystemService
    SecurityService securityService

    /**
     * Fetch assets from ServiceNow
     * @param actionRequest
     * @return
     */
    Map fetchAssetList(ActionRequest actionRequest) {
        log.debug 'Fetching ServiceNow assets: {}', actionRequest
        Map result
        Map map =  downloadAndSaveAssetsFile(actionRequest)
        if (map.error) {
            result = [status: 'error', cause: map.error]
        } else {
            result = [status: 'success', filename: map.filename]
        }
        return result
    }

    /**
     * Retrieve the URL for the service call.
     * TODO: As we're using the endpointUrl as is, we could probably remove this method.
     * @param apiActionMap
     * @return
     */
    private String serviceUrl(Map apiActionMap) {
        String url = apiActionMap['endpointUrl']
        log.debug 'serviceUrl={}', url
        return url
    }

    /**
     * Create a CredentialsProvider based on the given credentialId
     * @param crendentialId
     * @return
     */
    private CredentialsProvider getBasicAuth(Long credentialId) {
        Credential credential = credentialService.findById(credentialId)
        String password = credentialService.decryptPassword(credential)
        CredentialsProvider provider = new BasicCredentialsProvider()
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(credential.username, password)
        provider.setCredentials(AuthScope.ANY, credentials)

        return provider
    }

    /**
     * Download file and save it using the file system service
     * @param actionRequest
     * @return Map
     *      filename <String> - the temporary filename
     *      error <String> - the cause of the failure
     */
    private Map downloadAndSaveAssetsFile(ActionRequest actionRequest) throws ClientProtocolException, IOException {
        HttpResponse response = null
        String filename = null
        String error = null

        // return [filename:'servicenow-xytdk094epk4Z8rkOMgfEyokLK9dFyfR.csv', error:null]

        try {
            byte[] buffer = new byte[1024]
            Map apiActionMap = actionRequest.param.getProperty('apiAction')
            Long credentialId = apiActionMap.credential.id
            HttpClient httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider(getBasicAuth(credentialId)).build()
            HttpGet httpGet = new HttpGet(serviceUrl(apiActionMap))
            response = httpClient.execute(httpGet)

            log.debug(response.getStatusLine().toString())
            HttpEntity entity = response.getEntity()

            int statusCode = response.getStatusLine().getStatusCode()
            if (statusCode != HttpStatus.SC_OK) {
                log.warn 'Request to ServiceNow failed code {}', statusCode
                throw new EmptyResultException('Service request failed')
            }

            String contentType = entity.getContentType().toString()
            if (contentType.contains('text/html')) {
                // Looks like we got a web page instead of data - not good
                log.warn 'Request to ServiceNow received unexpected HTML page'
                for (def header in response.getAllHeaders()) {
                    log.debug 'Header: {} Value:{}', header.getName(), header.getValue()
                }

                // Check to see if instance is hibernating
                String text = entity.getContent().text
                log.debug 'text={}', text
                error = text.contains('Hibernating Instance') ? 'ServiceNow instance is in hibernation' : 'ServiceNow unavailable'
                throw new EmptyResultException(error)
            }

            log.debug contentType
            log.debug response.getFirstHeader("Content-Disposition")?.getValue()

            InputStream input = null
            OutputStream output = null

            try {
                String extension = getFilenameExtension(response, actionRequest)
                (filename, output) = fileSystemService.createTemporaryFile(FILENAME_PREFIX, extension)
                input = entity.getContent()
                for (int length; (length = input.read(buffer)) > 0;) {
                    output.write(buffer, 0, length)
                }
                log.info("File successfully downloaded!")
            } finally {
                httpClient.close()

                if (output != null) {
                    try {
                        output.close()
                    } catch (IOException logOrIgnore) {
                        log.warn 'Failed to close output stream {}', filename
                    }
                }
                if (input != null) {
                    try {
                        input.close()
                    } catch (IOException logOrIgnore) {
                        log.warn 'Failed to close input stream to ServiceNow webservice'
                    }
                }
            }
            EntityUtils.consume(entity)
        } catch (EmptyResultException e) {
            error = e.getMessage()
        } catch (Exception e) {
            log.warn('Error fetching external resource from ServiceNow.', e)
            error = 'Calling ServiceNow falled'
        } finally {
            response = null
        }

        if (error) {
            filename = null
        }

        return [filename:filename, error:error]
    }

    /**
     * Determines the proper file extension for the output file based on the response or service definition
     * @param response
     * @param payload
     * @return
     */
    private String getFilenameExtension(HttpResponse response, Map payload) {
        String extension

        // Attempt to strip out the filename extension from the download disposition (example: inline;filename=cmdb_ci_appl.csv)
        String disposition = response.getFirstHeader('Content-Disposition')?.getValue()
        if (disposition) {
            List parts = disposition.toLowerCase().split('filename=')
            if (parts.size() == 2) {
                parts = parts[1].split(/\./)
                if (parts.size() == 2) {
                    extension = parts[1]
                }
            }
        }

        // Fall back to the payload setting
        if (! extension) {
            extension = (payload.format ? payload.format.toLowerCase() : 'csv') // TODO: What is this format? where should it come from?
        }

        return extension
    }
}
