package com.example.androidallfunctions.utils

import androidx.annotation.Nullable
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import java.lang.reflect.Field
import java.lang.reflect.Method


class LiveDataBus private constructor() {
    private val bus: MutableMap<String, BusMutableLiveData<Any>>

    private object SingletonHolder {
        val DEFAULT_BUS = LiveDataBus()
    }

    fun <T> with(key: String, type: Class<T>?): BusMutableLiveData<Any>? {
        if (!bus.containsKey(key)) {
            bus[key] = BusMutableLiveData()
        }
        return bus[key]
    }

    fun with(key: String): BusMutableLiveData<Any>? {
        return with(key, Any::class.java)
    }

    private class ObserverWrapper<T>(observer: Observer<T>) : Observer<T> {
        private val observer: Observer<T>?
        override fun onChanged(@Nullable t: T) {
            if (observer != null) {
                if (isCallOnObserve) {
                    return
                }
                observer.onChanged(t)
            }
        }

        private val isCallOnObserve: Boolean
            get() {
                val stackTrace = Thread.currentThread().stackTrace
                if (stackTrace.isNotEmpty()) {
                    for (element in stackTrace) {
                        if ("android.arch.lifecycle.LiveData" == element.className && "observeForever" == element.methodName) {
                            return true
                        }
                    }
                }
                return false
            }

        init {
            this.observer = observer
        }
    }

    class BusMutableLiveData<T> : MutableLiveData<T>() {
        private val observerMap = HashMap<Any, Any>()
        override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
            super.observe(owner, observer)
            try {
                hook(observer as Observer<*>)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun observeForever(observer: Observer<in T>) {
            if (!observerMap.containsKey(observer)) {
                observerMap[observer] = ObserverWrapper(observer)
            }
            super.observeForever(observerMap[observer] as Observer<in T>)
        }

        override fun removeObserver(observer: Observer<in T>) {
            var realObserver: Observer<in T>? = null
            realObserver = if (observerMap.containsKey(observer)) ({
                observerMap.remove(observer)
            }) as Observer<T>? else {
                observer
            }
            super.removeObserver(realObserver as Observer<in T>)
        }

        @Throws(Exception::class)
        private fun hook(observer: Observer<*>) {
            //get wrapper's version
            val classLiveData = LiveData::class.java
            val fieldObservers: Field = classLiveData.getDeclaredField("mObservers")
            fieldObservers.isAccessible = true
            val objectObservers: Any = fieldObservers.get(this) as Any
            val classObservers: Class<*> = objectObservers.javaClass
            val methodGet: Method = classObservers.getDeclaredMethod("get", Any::class.java)
            methodGet.isAccessible = true
            val objectWrapperEntry: Any = methodGet.invoke(objectObservers, observer) as Any
            var objectWrapper: Any? = null
            if (objectWrapperEntry is Map.Entry<*, *>) {
                objectWrapper = objectWrapperEntry.value
            }
            if (objectWrapper == null) {
                throw NullPointerException("Wrapper can not be bull!")
            }
            val classObserverWrapper: Class<*> = objectWrapper.javaClass.superclass as Class<*>
            val fieldLastVersion: Field = classObserverWrapper.getDeclaredField("mLastVersion")
            fieldLastVersion.isAccessible = true
            //get livedata's version
            val fieldVersion: Field = classLiveData.getDeclaredField("mVersion")
            fieldVersion.isAccessible = true
            val objectVersion: Any = fieldVersion.get(this) as Any
            //set wrapper's version
            fieldLastVersion.set(objectWrapper, objectVersion)

        }
    }

    companion object {
        fun get(): LiveDataBus {
            return SingletonHolder.DEFAULT_BUS
        }
    }

    init {
        bus = HashMap()
    }
}