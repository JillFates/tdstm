package com.tdsops.tm.asset.graph

/**
 * Struct to maintain an asset dependency graph, that allow operation like group assets by references
 */
public class AssetGraph {

	def nodes = [:]

	/**
	 * Load graph from a sql result list with the following data in each record: assetId, assetDepFromId, assetDepToId, moveBundleId, status, type
	 */
	def loadFromResults(result) {
		nodes = [:]
		def currentId = -1
		def currentNode = null
		result.each { record ->
			if (currentId != record["assetId"]) {
				currentId = record["assetId"]
				currentNode = new AssetNode(id: record["assetId"], moveBundleId: record["moveBundleId"])
				nodes[currentNode.id] = currentNode
			}
			addDependencyToNode(currentNode, record["assetDepFromId"], record["assetDepToId"], record["type"], record["status"])
		}
	}

	/**
	 * Add a new dependency to an existing node
	 */
	private def addDependencyToNode(node, assetDepFromId, assetDepToId, type, status) {
		if (node.id == assetDepFromId) {
			node.deps << new AssetDep(status: status, type: type, depId: assetDepToId)
		} else {
			node.deps << new AssetDep(status: status, type: type, depId: assetDepFromId)
		}
	}

	/**
	 * Group assets by dependencies
	 */
	def groupByDependencies(statusList, typeList, moveBundleList) {
		def groups = []
		def group = []

		nodes.each { assetId, node ->
			if (!node.checked) {
				//If the node is not checked, then creates a new group and search for all assets/nodes related to this one
				group = []
				groups << group
				addToGroupAndAnalize(node, group, statusList, typeList, moveBundleList)
			}
		}

		return groups;
	}

	/**
	 * Analyze current node and if the first time that is viewed, the analyze all his dependencies and add it to the group
	 */
	private def addToGroupAndAnalize(node, group, statusList, typeList, moveBundleList) {
		if (!node.checked) {
			node.checked = true
			group << node
			node.deps.each { dep ->
				def depNode = nodes[dep.depId]
				if ( depNode &&
					 statusList.contains(dep.status) && 
					 typeList.contains(dep.type) && 
					 moveBundleList.contains(depNode.moveBundleId?.toString())
				) {
					addToGroupAndAnalize(depNode, group, statusList, typeList, moveBundleList);
				}
			}
		}
	}

	/**
	 * Destroy method used to help GC
	 */
	def destroy() {
		nodes.each { key, value ->
			value.destroy()
			nodes[key] = null
		}
		nodes = null
	}

}
