package io.vithor.domehttp

class DomeError(val rawData: RawData?, error: Throwable) : Throwable(cause = error)
