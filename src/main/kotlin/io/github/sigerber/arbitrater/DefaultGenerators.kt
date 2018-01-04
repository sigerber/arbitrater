package io.github.sigerber.arbitrater

import com.tyro.oss.randomdata.RandomBoolean
import com.tyro.oss.randomdata.RandomLocalDate
import com.tyro.oss.randomdata.RandomLocalDateTime
import com.tyro.oss.randomdata.RandomString
import java.math.BigDecimal
import java.math.BigInteger
import java.util.*
import kotlin.reflect.KType
import kotlin.reflect.full.withNullability
import kotlin.reflect.jvm.reflect

object DefaultGenerators {

    private val random = Random()

    private val _generators: MutableMap<KType, () -> Any> = mutableMapOf()

    val generators: Map<KType, () -> Any>
        get() = _generators.toMap()

    init {
        registerGenerator(RandomBoolean::randomBoolean)
        registerGenerator { random.nextInt(Byte.MAX_VALUE.toInt()).toByte() }
        registerGenerator { random.nextInt(Short.MAX_VALUE.toInt()).toShort() }
        registerGenerator(DefaultGenerators::randomByte)
        registerGenerator(random::nextInt)
        registerGenerator(random::nextLong)
        registerGenerator(random::nextFloat)
        registerGenerator(random::nextDouble)
        registerGenerator(RandomString::randomString)
        registerGenerator(DefaultGenerators::randomKotlinString)
        registerGenerator(RandomLocalDate::randomLocalDate)
        registerGenerator(RandomLocalDateTime::randomDateTime)
        registerGenerator(UUID::randomUUID)
        registerGenerator { BigInteger.valueOf(random.nextLong()) }
        registerGenerator { BigDecimal.valueOf(random.nextDouble()) }
    }

    fun registerGenerator(generator: () -> Any) {
        // Removing nullability so generators registered by passing in Java methods (with a platform type) will match up against a non-nullable Kotlin parameter declaration
        val returnType = generator.reflect()!!.returnType.withNullability(false)
        _generators[returnType] = generator
    }

    private fun randomByte(): Byte {
        val byteArray = ByteArray(1)
        random.nextBytes(byteArray)

        return byteArray[0]
    }

    private fun randomKotlinString(): String = RandomString.randomString() // Needed to convert the Java string to a Kotlin string
}