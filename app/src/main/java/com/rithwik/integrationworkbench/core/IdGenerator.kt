package com.rithwik.integrationworkbench.core

import java.util.UUID

interface IdGenerator {
    fun newId(): String
}

class UuidGenerator : IdGenerator {
    override fun newId(): String = UUID.randomUUID().toString()
}
