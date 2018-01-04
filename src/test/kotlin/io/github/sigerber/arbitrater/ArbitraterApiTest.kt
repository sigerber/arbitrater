package io.github.sigerber.arbitrater

import io.kotlintest.forAll
import io.kotlintest.forAtLeast
import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldNotBe
import org.junit.Test
import java.math.BigDecimal
import java.math.BigInteger
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.TimeUnit

class ArbitraterApiTest {

    @Test
    fun `can generate numbers`() {
        val numbers = Numbers::class.arbitraryInstance()
        println("numbers = $numbers")
    }

    @Test
    fun `can generate big numbers`() {
        val bigNumbers = BigNumbers::class.arbitraryInstance()
        println(bigNumbers)
    }

    @Test
    fun `can generate bools and bytes`() {
        val arbitraryInstance = BoolsAndBytes::class.arbitraryInstance()
        println("arbitraryInstance = $arbitraryInstance")
    }

    @Test
    fun `can generate strings`() {
        val strings = Strings::class.arbitraryInstance()
        println("strings = $strings")
    }

    @Test
    fun `can generate Java dates`() {
        val dates = Dates::class.arbitraryInstance()
        println(dates)
    }

    @Test
    fun `can generate UUIDs`() {
        val uuids = UUIDs::class.arbitraryInstance()
        println(uuids)
    }

    @Test
    fun `can generate kotlin enums`() {
        val kotlinNativeEnums = KotlinNativeEnums::class.arbitraryInstance()
        println(kotlinNativeEnums)
    }

    @Test
    fun `can generate Java enums`() {
        val javaEnums =  JavaEnums::class.arbitraryInstance()
        println(javaEnums)
    }

    @Test
    fun `can generate nested classes`() {
        val instance = NestedClasses::class.arbitraryInstance()
        println(instance)
    }

    @Test
    fun `can generate lists of values`() {
        val listOfValues = ListOfValues::class.arbitraryInstance()
        println("listOfValues = $listOfValues")
    }

    @Test
    fun `can generate lists of non-nested data classes`() {
        val instance = ListOfNonNestedDataClasses::class.arbitraryInstance()
        println(instance)
    }

    @Test
    fun `can generate lists of nested data classes`() {
        val instance = ListOfNestedDataClasses::class.arbitraryInstance()
        println(instance)
    }

    @Test
    fun `can generate sets`() {
        val instance = SetOfValues::class.arbitraryInstance()
        println(instance)
    }

    @Test
    fun `can generate maps`() {
        val instance = MapsOfDtos::class.arbitraryInstance()
        println(instance)
    }

    @Test
    fun `nullable types generate values by default`() {
        val arbitraryInstance = NullableValue::class.arbitraryInstance()
        arbitraryInstance.date shouldNotBe null
    }

    @Test
    fun `can generate nulls for null values if desired`() {
        val arbitraryInstance = NullableValue::class.arbitrater().generateNulls().createInstance()
        arbitraryInstance.date shouldBe null
    }

    @Test
    fun `uses default values by default`() {
        val arbitraryInstances = (1..100).map {
            DefaultValue::class.arbitraryInstance()
        }

        forAll(arbitraryInstances) { instance ->
            instance.int shouldBe 10
        }
    }

    @Test
    fun `can elect not to use default values`() {
        val arbitraryInstances = (1..100).map {
            DefaultValue::class.arbitrater().useDefaultValues(false).createInstance()
        }

        forAtLeast(1, arbitraryInstances) { instance ->
            instance.int shouldNotBe 10
        }
    }
}

data class MapsOfDtos(val map: Map<String, NestedClasses>)

data class ListOfNonNestedDataClasses(val dtoList: List<Numbers>)

data class ListOfNestedDataClasses(val dtoList: List<NestedClasses>)

data class ListOfValues(val intList: List<Int>)

data class SetOfValues(val intList: Set<Int>)

data class DefaultValue(val int: Int = 10)

data class NullableValue(val date: LocalDate?)

data class Dates(
        val localDate: LocalDate,
        val localDateTime: LocalDateTime
)

data class BigNumbers(
        val bigInteger: BigInteger,
        val bigDecimal: BigDecimal
)

data class Numbers(
        val byte: Byte,
        val short: Short,
        val int: Int,
        val long: Long,
        val float: Float,
        val double: Double
)

data class BoolsAndBytes(val bool: Boolean, val byte: Byte)

data class Strings(val string1: String, val string2: String)

data class NestedClasses(val numbers: Numbers, val boolsAndBytes: BoolsAndBytes, val strings: Strings)

data class UUIDs(val uuid: UUID)

data class KotlinNativeEnums(val someEnum: SomeEnum)

data class JavaEnums(val someJavaEnum: TimeUnit)

enum class SomeEnum {
    VALUE_1,
    VALUE_2
}