package com.eliezercruz.ledxcalc.util

private fun wasmDateNow(): Double = js("Date.now()")

actual fun currentTimeMillis(): Long = wasmDateNow().toLong()
