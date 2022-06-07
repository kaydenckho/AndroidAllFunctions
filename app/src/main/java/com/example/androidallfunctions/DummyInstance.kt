package com.example.androidallfunctions

import javax.inject.Inject

class DummyInstance @Inject constructor() {
    fun a() {}
    fun b() {}
    var a: String? = null
    var b: String? = null
}