/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.collection

actual class SparseArray<E> actual constructor(initialCapacity: Int) {
    internal actual var keys: IntArray
    internal actual var values: Array<Any?>
    init {
        if (initialCapacity == 0) {
            keys = EMPTY_INTS
            values = EMPTY_OBJECTS
        } else {
            val length = idealIntArraySize(initialCapacity)
            keys = IntArray(length)
            values = arrayOfNulls(length)
        }
    }

    internal actual var garbage: Boolean = false
    @Suppress("PropertyName") // Normal backing field name but internal for common code.
    internal actual var _size: Int = 0

    actual constructor(array: SparseArray<E>) : this(0) {
        _size = array._size
        keys = array.keys.copyOf()
        values = array.values.copyOf()
        garbage = array.garbage
        gc()
    }

    actual val size: Int get() = commonSize()

    actual fun isEmpty(): Boolean = commonIsEmpty()

    actual operator fun get(key: Int): E? = commonGet(key, null)
    actual fun get(key: Int, default: E): E = commonGet(key, default)

    actual fun put(key: Int, value: E): Unit = commonPut(key, value)
    actual fun putAll(other: SparseArray<out E>): Unit = commonPutAll(other)
    actual fun putIfAbsent(key: Int, value: E): E? = commonPutIfAbsent(key, value)
    actual fun append(key: Int, value: E): Unit = commonAppend(key, value)

    actual fun keyAt(index: Int): Int = commonKeyAt(index)

    actual fun valueAt(index: Int): E = commonValueAt(index)
    actual fun setValueAt(index: Int, value: E): Unit = commonSetValueAt(index, value)

    actual fun indexOfKey(key: Int): Int = commonIndexOfKey(key)
    actual fun indexOfValue(value: E): Int = commonIndexOfValue(value)

    actual fun containsKey(key: Int): Boolean = commonContainsKey(key)
    actual fun containsValue(value: E): Boolean = commonContainsValue(value)

    actual fun clear(): Unit = commonClear()

    actual fun remove(key: Int): Unit = commonRemove(key)
    actual fun remove(key: Int, value: Any?): Boolean = commonRemove(key, value)
    actual fun removeAt(index: Int): Unit = commonRemoveAt(index)

    actual fun replace(key: Int, value: E): E? = commonReplace(key, value)
    actual fun replace(key: Int, oldValue: E?, newValue: E): Boolean =
        commonReplace(key, oldValue, newValue)

    override fun toString(): String = commonToString()
}
