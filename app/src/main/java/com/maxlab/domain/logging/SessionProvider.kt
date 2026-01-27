package com.maxlab.domain.logging

import com.maxlab.core.IdGenerator
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionProvider @Inject constructor(
    idGenerator: IdGenerator
) {
    val sessionId: String = idGenerator.newId()
}
