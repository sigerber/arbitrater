package io.github.sigerber.arbitrater

import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.reflect

// TODO: Option to generate optional values or not
// TODO: Option to generate default values or not
// TODO: Defaults for booleans, chars and strings
// TODO: Recurse object graph
// TODO: Arrays
// TODO: Collections

class InstanceCreator<out T : Any>(private val targetClass: KClass<T>) {
    private val generators: MutableMap<KType, () -> Any> = mutableMapOf()

    init {
        val random = Random()

        registerGenerator { random.nextInt(Byte.MAX_VALUE.toInt()).toByte() }
        registerGenerator { random.nextInt(Short.MAX_VALUE.toInt()).toShort() }
        registerGenerator(random::nextInt)
        registerGenerator(random::nextLong)
        registerGenerator(random::nextFloat)
        registerGenerator(random::nextDouble)
    }

    fun registerGenerator(generator: () -> Any) {
        val returnType = generator.reflect()!!.returnType
        generators[returnType] = generator
    }

    fun createInstance(): T {
        return try {
            targetClass.createInstance()
        } catch (e: RuntimeException) {
            val primaryConstructor = targetClass.primaryConstructor!!

            val constructorArguments = primaryConstructor
                    .parameters
                    .map { it to it.randomValue() }
                    .toMap()

            primaryConstructor.callBy(constructorArguments)
        }
    }

    private fun KParameter.randomValue(): Any? = when {
        this.type.isMarkedNullable -> null
        canGenerate(this.type) -> generate(this.type)
        else -> TODO("No support for ${this.type}")
    }

    private fun generate(type: KType): Any = generators[type]!!.invoke()

    private fun canGenerate(type: KType) = generators.containsKey(type)
}

