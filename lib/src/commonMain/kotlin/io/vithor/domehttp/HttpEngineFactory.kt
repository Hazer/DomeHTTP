package io.vithor.domehttp

interface HttpEngineFactory {
    fun config(config: DomeClient.Config): HttpEngineFactory
    fun build(): HttpEngine
}