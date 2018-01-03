package net.transitionmanager.integration

/**
 *
 */
class ApiActionResponse {
    boolean readonly = false
    Object data
    Long elapsed
    String error
    Map<String, String> headers
    List<Map> files
    String filename
    String originalFilename
    Integer status
    Boolean successful

    void setReadonly(boolean value) {
        readonly = value
    }

    boolean isReadonly() {
        return readonly
    }

    void setProperty(String name, Object value) {
        if (name != 'readonly') {
            checkReadonly(name)
            this.@"$name" = value
        }
    }

    boolean hasHeader(String key) {
        if (Objects.nonNull(headers)) {
            return headers.containsKey(key)
        }
        return false
    }

    String getHeader(String key) {
        if (hasHeader(key)) {
            return headers.get(key)
        }
        return null
    }

    ApiActionResponse asImmutable() {
        ApiActionResponse immutable = new ApiActionResponse()
        immutable.setReadonly(true)
        immutable.data = this.data
        immutable.elapsed = this.elapsed
        immutable.error = this.error
        immutable.headers = this.headers
        immutable.files = this.files
        immutable.filename = this.filename
        immutable.originalFilename = this.originalFilename
        immutable.status = this.status
        immutable.successful = this.successful

        return immutable
    }

    private checkReadonly(String name) {
        if (isReadonly()) {
            throw new ReadOnlyPropertyException(name, ApiActionResponse.class.name)
        }
    }
}
