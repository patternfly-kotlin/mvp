package dev.fritz2.mvp

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.promise

@DelicateCoroutinesApi
fun <T> runTest(block: suspend () -> T): dynamic = GlobalScope.promise {
    delay(50)
    block()
    delay(50)
}
