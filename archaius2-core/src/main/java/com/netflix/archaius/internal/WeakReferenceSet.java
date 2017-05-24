package com.netflix.archaius.internal;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

/**
 * Set like collections with build in garbage collection
 * @param <T>
 */
public class WeakReferenceSet<T> {
    private final static ReferenceQueue referenceQueue = new ReferenceQueue();

    static {
        new CleanupThread().start();
    }

    static class GarbageReference<T> extends WeakReference {
        final T value;
        final CopyOnWriteArraySet<T> data;

        GarbageReference(Object referent, T value, CopyOnWriteArraySet<T> data) {
            super(referent, referenceQueue);
            this.value = value;
            this.data = data;
        }
    }

    static class CleanupThread extends Thread {
        CleanupThread() {
            setPriority(Thread.MAX_PRIORITY);
            setName("GarbageCollectingConcurrentMap-cleanupthread");
            setDaemon(true);
        }

        public void run() {
            while (true) {
                try {
                    do {
                        GarbageReference ref = (GarbageReference) referenceQueue.remove();
                        ref.data.remove(ref);
                    } while (true);
                } catch (InterruptedException e) {
                    //ignore
                }
            }
        }
    }
    
    private final CopyOnWriteArraySet<GarbageReference<T>> data = new CopyOnWriteArraySet<>();

    public void clear() {
        data.clear();
    }

    public boolean isEmpty() {
        return data.isEmpty();
    }
    
    /**
     * @return Number of elements
     */
    public int size() {
        return data.size();
    }
    
    /**
     * Iterate through all elements and call the consumer for each
     * @param consumer
     */
    public void forEach(Consumer<T> consumer) {
        data.forEach((reference) -> {
            T element = (T)reference.value;
            if (element != null) {
                consumer.accept(element);
            }
        });
    }

    /**
     * Add an element that will be auto removed when the second parameter is garbage collected
     * @param value
     * @param garbageObject
     */
    public AutoCloseable add(T value, Object garbageObject) {
        if (value == null || garbageObject == null) throw new NullPointerException();
        if (value == garbageObject)
            throw new IllegalArgumentException("value can't be equal to garbageObject for gc to work");

        GarbageReference reference = new GarbageReference(garbageObject, value, data);
        
        data.add(reference);
        return () -> data.remove(reference);
    }
}
