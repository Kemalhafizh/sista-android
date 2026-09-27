package com.sultanagung1.sista.core.telemetry

import java.util.concurrent.ConcurrentLinkedDeque

/**
 * Thread-safe ring buffer untuk menyimpan jejak telemetri (Breadcrumbs) terbaru.
 * Jika kapasitas terlampaui, elemen tertua otomatis di-evict (FIFO).
 */
class BreadcrumbBuffer(val capacity: Int = DEFAULT_CAPACITY) {

    private val deque = ConcurrentLinkedDeque<Breadcrumb>()

    fun add(breadcrumb: Breadcrumb) {
        deque.addLast(breadcrumb)
        while (deque.size > capacity) {
            deque.pollFirst()
        }
    }

    fun getSnapshot(): List<Breadcrumb> {
        return deque.toList()
    }

    fun size(): Int = deque.size

    fun clear() {
        deque.clear()
    }

    companion object {
        const val DEFAULT_CAPACITY = 50
    }
}
