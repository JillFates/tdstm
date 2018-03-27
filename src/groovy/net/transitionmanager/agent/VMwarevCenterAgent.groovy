package net.transitionmanager.agent

import com.tdsops.common.grails.ApplicationContextHolder
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import net.transitionmanager.integration.ActionRequest
import net.transitionmanager.service.HttpProducerService

/**
 * Methods to interact with VMware vCenter to interact with the RESTful API provided there.
 */
@Slf4j(value='logger')
@Singleton(strict=false)
@CompileStatic
class VMwarevCenterAgent extends AbstractAgent {
	private static final String DOCUMENTATION_URL = 'https://code.vmware.com/apis/191/vsphere-automation';
	HttpProducerService httpProducerService

	private static final LinkedHashMap HOSTNAME_PARAM = [
					paramName: 'HOSTNAME',
					desc: 'Destination hostname',
					type: 'String',
					context: ContextType.USER_DEF,
					fieldName: null,
					value: 'Enter your FQDN to vCenter',
					required: 1,
					readonly: 0,
					encoded: 0
			]

	private static final LinkedHashMap VM_NAME_PARAM = [
					paramName: 'VM',
					desc: 'Virtual machine identifier. The parameter must be an identifier for the resource type: VirtualMachine.',
					type: 'String',
					context: ContextType.DEVICE,
					fieldName: 'assetName',
					value: null,
					required: 1,
					readonly: 0,
					encoded: 0
			]

	private static final List<LinkedHashMap> HOST_VM_NAME_PARAMS = [HOSTNAME_PARAM, VM_NAME_PARAM]

	VMwarevCenterAgent() {
		setInfo(AgentClass.VCENTER, 'VMWare vCenter')

		setDictionary([
				getClusterInfo: new DictionaryItem([
						agentMethod: 'getClusterInfo',
						name: 'List of clusters',
						description: 'Returns information about at most 1000 visible (subject to permission checks) clusters in vCenter.',
						endpointUrl: 'https://{{HOSTNAME}}/rest/vcenter/cluster',
						docUrl: DOCUMENTATION_URL,
						method: 'invokeHttpRequest',
						producesData: 0,
						params: [
								HOSTNAME_PARAM
						]
				]),
				getClusterDetailInfo: new DictionaryItem([
						agentMethod: 'getClusterDetailInfo',
						name: 'Get cluster',
						description: 'Retrieves information about the cluster corresponding to cluster.',
						endpointUrl: 'https://{{HOSTNAME}}/rest/vcenter/cluster',
						docUrl: DOCUMENTATION_URL,
						method: 'invokeHttpRequest',
						producesData: 0,
						params: [
								HOSTNAME_PARAM
						]
				]),
				getHostInfo: new DictionaryItem([
						agentMethod: 'getHostInfo',
						name: 'List of hosts',
						description: 'Returns information about at most 1000 visible (subject to permission checks) hosts in vCenter.',
						endpointUrl: 'https://{{HOSTNAME}}/rest/vcenter/host',
						docUrl: DOCUMENTATION_URL,
						method: 'invokeHttpRequest',
						producesData: 0,
						params: [
								HOSTNAME_PARAM
						]
				]),
				getNetworkInfo: new DictionaryItem([
						agentMethod: 'getNetworkInfo',
						name: 'List of networks',
						description: 'Returns information about at most 1000 visible (subject to permission checks) networks in vCenter.',
						endpointUrl: 'https://{{HOSTNAME}}/rest/vcenter/network',
						docUrl: DOCUMENTATION_URL,
						method: 'invokeHttpRequest',
						producesData: 0,
						params: [

						]
				]),
				getVMsInfo: new DictionaryItem([
						agentMethod: 'getVMsInfo',
						name: 'List of VMs',
						description: 'Returns information about at most 1000 visible (subject to permission checks) virtual machines in vCenter.',
						endpointUrl: 'https://{{HOSTNAME}}/rest/vcenter/vm',
						docUrl: DOCUMENTATION_URL,
						method: 'invokeHttpRequest',
						producesData: 0,
						params: [
								HOSTNAME_PARAM
						]
				]),
				getVMDetailInfo: new DictionaryItem([
						agentMethod: 'getVMDetailInfo',
						name: 'VM detailed info',
						description: 'Returns information about a virtual machine.',
						endpointUrl: 'https://{{HOSTNAME}}/rest/vcenter/vm/{{VM}}',
						docUrl: DOCUMENTATION_URL,
						method: 'invokeHttpRequest',
						producesData: 0,
						params: HOST_VM_NAME_PARAMS
				]),
				getVMPowerStatus: new DictionaryItem([
						agentMethod: 'getVMPowerStatus',
						name: 'VM power status',
						description: 'Returns the power state information of a virtual machine.',
						endpointUrl: 'https://{{HOSTNAME}}/rest/vcenter/vm/{{VM}}/power',
						docUrl: DOCUMENTATION_URL,
						method: 'invokeHttpRequest',
						producesData: 0,
						params: HOST_VM_NAME_PARAMS
				]),
				startVM: new DictionaryItem([
						agentMethod: 'startVM',
						name: 'Start VM',
						description: 'Powers on a powered-off or suspended virtual machine.',
						endpointUrl: 'https://{{HOSTNAME}}/rest/vcenter/vm/{{VM}}/power/start',
						docUrl: DOCUMENTATION_URL,
						method: 'invokeHttpRequest',
						producesData: 0,
						params: HOST_VM_NAME_PARAMS
				]),
				stopVM: new DictionaryItem([
						agentMethod: 'stopVM',
						name: 'Stop VM',
						description: 'Powers off a powered-on or suspended virtual machine.',
						endpointUrl: 'https://{{HOSTNAME}}/rest/vcenter/vm/{{VM}}/power/stop',
						docUrl: DOCUMENTATION_URL,
						method: 'invokeHttpRequest',
						producesData: 0,
						params: HOST_VM_NAME_PARAMS
				])
		])

		httpProducerService = (HttpProducerService) ApplicationContextHolder.getBean('httpProducerService')
	}

	/**
	 * Call the service supporting this agent execute method.
	 * @param actionRequest
	 * @return
	 */
	void invokeHttpRequest(ActionRequest actionRequest) {
		httpProducerService.executeCall(actionRequest)
	}

}
