package com.rithwik.integrationworkbench.domain.logging

import com.rithwik.integrationworkbench.core.IdGenerator
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionProvider @Inject constructor(
    idGenerator: IdGenerator
) {
    val sessionId: String = idGenerator.newId()
}
