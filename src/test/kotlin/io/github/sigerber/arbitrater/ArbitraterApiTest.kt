package io.github.sigerber.arbitrater

import org.junit.Test

class ArbitraterApiTest {

    @Test
    fun `scratchpad`() {
        val numbers = Numbers::class.arbitraryInstance()

        println("numbers = $numbers")
    }
}

data class Numbers(
        val byte: Byte,
        val short: Short,
        val int: Int,
        val long: Long,
        val float: Float,
        val double: Double
)