package com.maxlab.core

import java.util.UUID

interface IdGenerator {
    fun newId(): String
}

class UuidGenerator : IdGenerator {
    override fun newId(): String = UUID.randomUUID().toString()
}
