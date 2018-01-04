package io.github.sigerber.arbitrater

import com.tyro.oss.randomdata.RandomEnum
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.withNullability
import kotlin.reflect.jvm.reflect

// TODO: Arrays?
// TODO: Some way to add library level defaults

private val wildcardMapType = Map::class.createType(arguments = listOf(KTypeProjection.STAR, KTypeProjection.STAR))
private val wildcardCollectionType = Collection::class.createType(arguments = listOf(KTypeProjection.STAR))
private val wildcardListType = List::class.createType(arguments = listOf(KTypeProjection.STAR))
private val wildcardSetType = Set::class.createType(arguments = listOf(KTypeProjection.STAR))
private val wildcardEnumType = Enum::class.createType(arguments = listOf(KTypeProjection.STAR))

class InstanceCreator<out T : Any>(private val targetClass: KClass<T>, private val settings: GeneratorSettings = GeneratorSettings()) {

    private val generators: MutableMap<KType, () -> Any> = DefaultGenerators.generators.toMutableMap()

    fun registerGenerator(generator: () -> Any) {
        // Removing nullability so generators registered by passing in Java methods (with a platform type) will match up against a non-nullable Kotlin parameter declaration
        val returnType = generator.reflect()!!.returnType.withNullability(false)
        generators[returnType] = generator
    }

    fun generateNulls(value: Boolean = true): InstanceCreator<T> = InstanceCreator(targetClass, settings.copy(generateNulls = value))

    fun useDefaultValues(value: Boolean = true): InstanceCreator<T> = InstanceCreator(targetClass, settings.copy(useDefaultValues = value))

    fun createInstance(): T {
        try {
            val primaryConstructor = targetClass.primaryConstructor!!

            val constructorArguments = primaryConstructor
                    .parameters
                    .filterNot { it.isOptional && settings.useDefaultValues }
                    .map { it to it.type.randomValue() }
                    .toMap()

            return primaryConstructor.callBy(constructorArguments)
        } catch (e: Exception) {
            throw RuntimeException("Could not generate random value for class [${targetClass.qualifiedName}]", e)
        }
    }

    private fun KType.randomValue(): Any? {
        val nonNullableType = withNullability(false)

        return when {
            settings.generateNulls && isMarkedNullable -> null
            canGenerate(nonNullableType) -> generate(withNullability(false))
            isSubtypeOf(wildcardCollectionType) -> fillCollection(this)
            isSubtypeOf(wildcardMapType) -> fillMap(this)
            isSubtypeOf(wildcardEnumType) -> RandomEnum.randomEnumValue((classifier as KClass<Enum<*>>).java)
            classifier is KClass<*> -> InstanceCreator(classifier as KClass<*>).createInstance()
            else -> TODO("No support for ${this}")
        }
    }

    private fun fillMap(mapType: KType): Any {
        val keyType = mapType.arguments[0].type!!
        val valueType = mapType.arguments[1].type!!

        return (1..10)
                .map { keyType.randomValue() to valueType.randomValue() }
                .toMap()
    }

    private fun fillCollection(collectionType: KType): Any {
        val valueType = collectionType.arguments[0].type!!
        val randomValues = (1..10).map { valueType.randomValue() }

        return when {
            collectionType.isSubtypeOf(wildcardListType) -> randomValues.toList()
            collectionType.isSubtypeOf(wildcardSetType) -> randomValues.toSet()
            else -> TODO("No support for $valueType")
        }
    }

    private fun generate(type: KType): Any = generators[type]!!.invoke()

    private fun canGenerate(type: KType) = generators.containsKey(type)
}

data class GeneratorSettings(
        val useDefaultValues: Boolean = true,
        val generateNulls: Boolean = false
)

