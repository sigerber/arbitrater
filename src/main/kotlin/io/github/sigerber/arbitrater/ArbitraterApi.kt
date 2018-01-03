package io.github.sigerber.arbitrater

import kotlin.reflect.KClass

fun <T : Any> KClass<T>.arbitraryInstance(): T = InstanceCreator(this)
        .createInstance()