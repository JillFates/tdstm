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
import groovy.transform.CompileStatic
import java.net.UnknownHostException


import net.transitionmanager.domain.ApiAction
import net.transitionmanager.integration.ActionRequest
import net.transitionmanager.integration.ActionRequest
import net.transitionmanager.integration.ActionHttpRequestElements
import net.transitionmanager.service.InvalidConfigurationException
import net.transitionmanager.service.InvalidParamException
import net.transitionmanager.service.InvalidRequestException
import com.tdssrc.grails.HttpUtil

@Transactional(readOnly = true)
@Slf4j
@CompileStatic
class ServiceNowService {
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
		log.debug 'fetchAssetList() Fetching ServiceNow assets: {}', actionRequest
		Map result
		Long actionId = (Long) actionRequest.options['actionId']
		ApiAction action = ApiAction.where { id == actionId }.get()
		Map map =  downloadAndSaveAssetsFile(action, actionRequest)
		if (map.error) {
			result = [status: 'error', cause: map.error]
		} else {
			result = [status: 'success', filename: map.filename]
		}
		return result
	}

	/**
	 * Construct service url from actionRequest
	 * @param actionRequest
	 * @return
	 */
	private String serviceUrl(ApiAction action, ActionRequest actionRequest) {
		ActionHttpRequestElements httpElements = new ActionHttpRequestElements( action.endpointUrl, actionRequest)
		log.debug 'serviceUrl() ActionHttpRequestElements() created with uri of {}',  httpElements.uri()
		return httpElements.uri()
	}

	/**
	 * Used to create a CredentialsProvider that uses the Credential associated with the ActionRequest
	 * @param actionRequest - the actionRequest object containing all the details involved with the request
	 * @return a CredentialsProvider populated with the credentials
	 */
	private CredentialsProvider getBasicAuth(ApiAction action, ActionRequest actionRequest) {
		if (! action.credential) {
			throw new InvalidConfigurationException ("Action '${action.name}' requires a credential")
		}
		CredentialsProvider provider = new BasicCredentialsProvider()
		String pswd = credentialService.decryptPassword(action.credential)
		UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(action.credential.username, pswd )

		provider.setCredentials(AuthScope.ANY, credentials)

		return provider
	}

	/**
	 * Used to handle the API response to download the content and save it using the FileSystemService
	 * @param actionRequest
	 * @return Map
	 *      filename <String> - the temporary filename
	 *      error <String> - the cause of the failure
	 */
	private Map downloadAndSaveAssetsFile(ApiAction action, ActionRequest actionRequest) throws ClientProtocolException, IOException {
		HttpResponse response = null
		String filename = null
		String error = null

		// return [filename:'servicenow-xytdk094epk4Z8rkOMgfEyokLK9dFyfR.csv', error:null]
		try {
			byte[] buffer = new byte[1024]
			CredentialsProvider cp = getBasicAuth(action, actionRequest)
			HttpClient httpClient = HttpClientBuilder.create()
				.setDefaultCredentialsProvider(cp)
				.build()
			HttpGet httpGet = new HttpGet(serviceUrl(action, actionRequest))
			response = httpClient.execute(httpGet)

			log.debug(response.getStatusLine().toString())
			HttpEntity entity = response.getEntity()

			int statusCode = response.getStatusLine().getStatusCode()
			if (statusCode != HttpStatus.SC_OK) {
				log.warn 'Request to ServiceNow failed code {}', statusCode
				throw new EmptyResultException('Service request failed - ServiceNow responded with failure code ' + response.getStatusLine().toString())
			}

			//
			// Try to determine if we actually got what we expected or perhaps that the ServiceNow instance is in hibernation
			//
			String contentType = entity.getContentType().toString()
			if (contentType.contains('text/html')) {
				// Looks like we got a web page instead of data - not good
				log.warn 'Request to ServiceNow received unexpected HTML page'
				for (def header in response.getAllHeaders()) {
					log.debug 'Header: {} Value:{}', header.getName(), header.getValue()
				}

				// Check to see if instance is hibernating
				String text = entity.getContent().text
				// log.debug 'text={}', text
				error = text.contains('Hibernating Instance') ? 'ServiceNow instance is in hibernation' : 'ServiceNow unavailable'
				throw new EmptyResultException(error)
			}

			log.debug contentType
			log.debug response.getFirstHeader("Content-Disposition")?.getValue()

			InputStream input = null
			OutputStream output = null

			try {
				String extension = getFilenameExtension(response, action, actionRequest)
				List fileInfo = fileSystemService.createTemporaryFile(FILENAME_PREFIX, extension)
				filename = (String) fileInfo[0]
				output = (OutputStream) fileInfo[1]
				input = entity.getContent()
				for (int length; (length = input.read(buffer)) > 0;) {
					output.write(buffer, 0, length)
				}
				log.debug 'downloadAndSaveAssetsFile() File successfully downloaded'
			} finally {
				httpClient.close()

				if (output != null) {
					try {
						output.close()
					} catch (IOException logOrIgnore) {
						log.warn 'downloadAndSaveAssetsFile() Failed to close output stream to temporary file {}', filename
					}
				}
				if (input != null) {
					try {
						input.close()
					} catch (IOException logOrIgnore) {
						log.warn 'downloadAndSaveAssetsFile() Failed to close input stream to ServiceNow webservice'
					}
				}
			}
			EntityUtils.consume(entity)
		} catch (UnknownHostException e) {
			error = "Missing Action HOSTNAME or DNS lookup failed (${action.endpointUrl})"
		} catch (InvalidParamException e) {
			error = e.getMessage()
		} catch (InvalidRequestException e) {
			error = e.getMessage()
		} catch (InvalidConfigurationException e) {
			error = e.getMessage()
		} catch (EmptyResultException e) {
			error = e.getMessage()
		} catch (Exception e) {
			log.warn('Error while attempting to call ServiceNow API', e)
			error = 'Invoking ServiceNow API failed'
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
	 * @param actionRequest
	 * @return
	 */
	private String getFilenameExtension(HttpResponse response, ApiAction action, ActionRequest actionRequest) {
		String extension

		// Attempt to strip out the filename extension from the download disposition (example: inline;filename=cmdb_ci_appl.csv)
		String disposition = response.getFirstHeader('Content-Disposition')?.getValue()
		if (disposition) {
			// https://github.com/spring-projects/spring-framework/blob/master/spring-web/src/main/java/org/springframework/http/ContentDisposition.java
			//ContentDisposition cd = ContentDisposition.parse(disposition)
			//log.debug "getFilenameExtension() type=${cd.type}, name=${cd.name}, filename=${cd.filename}, size=${cd.size}"
			//String fileName = disposition.replaceFirst(/(?i)^.*filename=\"?([^\"]+)\"?.*$/, "$1")
			//debug.log "*** getFilenameExtension() filename=$fileName"

			String[] parts = disposition.toLowerCase().split('filename=')
			if (parts.size() == 2) {
				// remove double quotations marks when present
				// e.g. Content-Disposition: inline;filename=cmdb_ci_appl.csv << no double quotations marks present
				// e.g. Content-Disposition: attachment; filename="cmdb_ci_appl.csv" << double quotations marks present
				parts = parts[1].toString().trim().replaceAll('"', '').split(/\./)
				if (parts.size() == 2) {
					extension = parts[1]
				}
			}
		}

		// Fall back to the actionRequest setting
		if (! extension) {
			extension = determineFileType(response, action, actionRequest)
		}

		return extension
	}

	/**
	 * Used to determine the file type (extension name) for the request
	 */
	String determineFileType(HttpResponse response, ApiAction action, ActionRequest actionRequest) {
		// TODO : JPM 3/2018 : Change determineFileType() look for the Content-Type response or for param CSV / EXCEL
		return 'csv'
	}

}
