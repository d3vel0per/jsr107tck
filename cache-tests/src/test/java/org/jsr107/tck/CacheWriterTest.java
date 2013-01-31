/**
 *  Copyright 2011 Terracotta, Inc.
 *  Copyright 2011 Oracle, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jsr107.tck;

import junit.framework.Assert;
import org.jsr107.tck.util.ExcludeListExcluder;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import javax.cache.Cache;
import javax.cache.CacheWriter;
import javax.cache.SimpleConfiguration;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * Unit test for {@link javax.cache.CacheWriter}s.
 *
 * @author Brian Oliver
 */
public class CacheWriterTest extends TestSupport {

    /**
     * Rule used to exclude tests
     */
    @Rule
    public ExcludeListExcluder rule = new ExcludeListExcluder(this.getClass());

    /**
     * The CacheWriter used for the tests.
     */
    private RecordingCacheWriter<Integer, String> cacheWriter;

    /**
     * The test Cache that will be configured to use the CacheWriter.
     */
    private Cache<Integer, String> cache;

    @Before
    public void setup() {
        cacheWriter = new RecordingCacheWriter<Integer, String>();

        SimpleConfiguration<Integer, String> config = new SimpleConfiguration<Integer, String>();
        config.setCacheWriter(cacheWriter);
        config.setWriteThrough(true);

        cache = getCacheManager().configureCache(getTestCacheName(), config);
    }

    @After
    public void cleanup() {
        for (Cache<?, ?> cache : getCacheManager().getCaches()) {
            getCacheManager().removeCache(cache.getName());
        }
    }

    @Test
    public void put_SingleEntry() {
        assertEquals(0, cacheWriter.getWriteCount());
        assertEquals(0, cacheWriter.getDeleteCount());

        cache.put(1, "Gudday World");

        assertEquals(1, cacheWriter.getWriteCount());
        assertEquals(0, cacheWriter.getDeleteCount());
        assertTrue(cacheWriter.containsKey(1));
        assertEquals("Gudday World", cacheWriter.get(1));
    }

    @Test
    public void put_SingleEntryMultipleTimes() {
        assertEquals(0, cacheWriter.getWriteCount());
        assertEquals(0, cacheWriter.getDeleteCount());

        cache.put(1, "Gudday World");
        cache.put(1, "Bonjour World");
        cache.put(1, "Hello World");

        assertEquals(3, cacheWriter.getWriteCount());
        assertEquals(0, cacheWriter.getDeleteCount());
        assertTrue(cacheWriter.containsKey(1));
        assertEquals("Hello World", cacheWriter.get(1));
    }

    @Test
    public void put_DifferentEntries() {
        assertEquals(0, cacheWriter.getWriteCount());
        assertEquals(0, cacheWriter.getDeleteCount());

        cache.put(1, "Gudday World");
        cache.put(2, "Bonjour World");
        cache.put(3, "Hello World");

        assertEquals(3, cacheWriter.getWriteCount());
        assertEquals(0, cacheWriter.getDeleteCount());

        assertTrue(cacheWriter.containsKey(1));
        assertEquals("Gudday World", cacheWriter.get(1));

        assertTrue(cacheWriter.containsKey(2));
        assertEquals("Bonjour World", cacheWriter.get(2));

        assertTrue(cacheWriter.containsKey(3));
        assertEquals("Hello World", cacheWriter.get(3));
    }

    @Test
    public void getAndPut_SingleEntryMultipleTimes() {
        assertEquals(0, cacheWriter.getWriteCount());
        assertEquals(0, cacheWriter.getDeleteCount());

        cache.getAndPut(1, "Gudday World");
        cache.getAndPut(1, "Bonjour World");
        cache.getAndPut(1, "Hello World");

        assertEquals(3, cacheWriter.getWriteCount());
        assertEquals(0, cacheWriter.getDeleteCount());
        assertTrue(cacheWriter.containsKey(1));
        assertEquals("Hello World", cacheWriter.get(1));
    }

    @Test
    public void getAndPut_DifferentEntries() {
        assertEquals(0, cacheWriter.getWriteCount());
        assertEquals(0, cacheWriter.getDeleteCount());

        cache.getAndPut(1, "Gudday World");
        cache.getAndPut(2, "Bonjour World");
        cache.getAndPut(3, "Hello World");

        assertEquals(3, cacheWriter.getWriteCount());
        assertEquals(0, cacheWriter.getDeleteCount());

        assertTrue(cacheWriter.containsKey(1));
        assertEquals("Gudday World", cacheWriter.get(1));

        assertTrue(cacheWriter.containsKey(2));
        assertEquals("Bonjour World", cacheWriter.get(2));

        assertTrue(cacheWriter.containsKey(3));
        assertEquals("Hello World", cacheWriter.get(3));
    }

    @Test
    public void putIfAbsent_SingleEntryMultipleTimes() {
        assertEquals(0, cacheWriter.getWriteCount());
        assertEquals(0, cacheWriter.getDeleteCount());

        cache.putIfAbsent(1, "Gudday World");
        cache.putIfAbsent(1, "Bonjour World");
        cache.putIfAbsent(1, "Hello World");

        assertEquals(1, cacheWriter.getWriteCount());
        assertEquals(0, cacheWriter.getDeleteCount());
        assertTrue(cacheWriter.containsKey(1));
        assertEquals("Gudday World", cacheWriter.get(1));
    }

    @Test
    public void replaceMatching_SingleEntryMultipleTimes() {
        assertEquals(0, cacheWriter.getWriteCount());
        assertEquals(0, cacheWriter.getDeleteCount());

        cache.putIfAbsent(1, "Gudday World");
        cache.replace(1, "Gudday World", "Bonjour World");
        cache.replace(1, "Gudday World", "Hello World");
        cache.replace(1, "Bonjour World", "Hello World");

        assertEquals(3, cacheWriter.getWriteCount());
        assertEquals(0, cacheWriter.getDeleteCount());
        assertTrue(cacheWriter.containsKey(1));
        assertEquals("Hello World", cacheWriter.get(1));
    }

    @Test
    public void replaceExisting_SingleEntryMultipleTimes() {
        assertEquals(0, cacheWriter.getWriteCount());
        assertEquals(0, cacheWriter.getDeleteCount());

        cache.replace(1, "Gudday World");
        cache.putIfAbsent(1, "Gudday World");
        cache.replace(1, "Bonjour World");
        cache.replace(1, "Hello World");

        assertEquals(3, cacheWriter.getWriteCount());
        assertEquals(0, cacheWriter.getDeleteCount());
        assertTrue(cacheWriter.containsKey(1));
        assertEquals("Hello World", cacheWriter.get(1));
    }

    @Test
    public void getAndReplace_SingleEntryMultipleTimes() {
        assertEquals(0, cacheWriter.getWriteCount());
        assertEquals(0, cacheWriter.getDeleteCount());

        cache.getAndReplace(1, "Gudday World");
        cache.putIfAbsent(1, "Gudday World");
        cache.getAndReplace(1, "Bonjour World");
        cache.getAndReplace(1, "Hello World");

        assertEquals(3, cacheWriter.getWriteCount());
        assertEquals(0, cacheWriter.getDeleteCount());
        assertTrue(cacheWriter.containsKey(1));
        assertEquals("Hello World", cacheWriter.get(1));
    }

    @Test
    public void invoke_CreateEntry() {
        assertEquals(0, cacheWriter.getWriteCount());
        assertEquals(0, cacheWriter.getDeleteCount());

        cache.invokeEntryProcessor(1, new Cache.EntryProcessor<Integer, String, Void>() {
            @Override
            public Void process(Cache.MutableEntry<Integer, String> entry) {
                entry.setValue("Gudday World");
                return null;
            }
        });

        assertEquals(1, cacheWriter.getWriteCount());
        assertEquals(0, cacheWriter.getDeleteCount());
        assertTrue(cacheWriter.containsKey(1));
        assertEquals("Gudday World", cacheWriter.get(1));
    }

    @Test
    public void invoke_UpdateEntry() {
        assertEquals(0, cacheWriter.getWriteCount());
        assertEquals(0, cacheWriter.getDeleteCount());

        cache.put(1, "Gudday World");
        cache.invokeEntryProcessor(1, new Cache.EntryProcessor<Integer, String, Void>() {
            @Override
            public Void process(Cache.MutableEntry<Integer, String> entry) {
                entry.setValue("Hello World");
                return null;
            }
        });

        assertEquals(2, cacheWriter.getWriteCount());
        assertEquals(0, cacheWriter.getDeleteCount());
        assertTrue(cacheWriter.containsKey(1));
        assertEquals("Hello World", cacheWriter.get(1));
    }

    @Test
    public void invoke_RemoveEntry() {
        assertEquals(0, cacheWriter.getWriteCount());
        assertEquals(0, cacheWriter.getDeleteCount());

        cache.put(1, "Gudday World");
        cache.invokeEntryProcessor(1, new Cache.EntryProcessor<Integer, String, Void>() {
            @Override
            public Void process(Cache.MutableEntry<Integer, String> entry) {
                entry.remove();
                return null;
            }
        });

        assertEquals(1, cacheWriter.getWriteCount());
        assertEquals(1, cacheWriter.getDeleteCount());
        assertFalse(cacheWriter.containsKey(1));
    }

    @Test
    public void remove_SingleEntry() {
        assertEquals(0, cacheWriter.getWriteCount());
        assertEquals(0, cacheWriter.getDeleteCount());

        cache.put(1, "Gudday World");
        cache.remove(1);

        assertEquals(1, cacheWriter.getWriteCount());
        assertEquals(1, cacheWriter.getDeleteCount());
        assertFalse(cacheWriter.containsKey(1));
    }

    @Test
    public void remove_SingleEntryMultipleTimes() {
        assertEquals(0, cacheWriter.getWriteCount());
        assertEquals(0, cacheWriter.getDeleteCount());

        cache.put(1, "Gudday World");
        cache.remove(1);
        cache.remove(1);
        cache.remove(1);

        assertEquals(1, cacheWriter.getWriteCount());
        assertEquals(3, cacheWriter.getDeleteCount());
        assertFalse(cacheWriter.containsKey(1));
    }

    @Test
    public void remove_SpecificEntry() {
        assertEquals(0, cacheWriter.getWriteCount());
        assertEquals(0, cacheWriter.getDeleteCount());

        cache.put(1, "Gudday World");
        cache.remove(1, "Hello World");
        cache.remove(1, "Gudday World");
        cache.remove(1, "Gudday World");
        cache.remove(1);

        assertEquals(1, cacheWriter.getWriteCount());
        assertEquals(2, cacheWriter.getDeleteCount());
        assertFalse(cacheWriter.containsKey(1));
    }

    @Test
    public void getAndRemove_SingleEntry() {
        assertEquals(0, cacheWriter.getWriteCount());
        assertEquals(0, cacheWriter.getDeleteCount());

        cache.getAndRemove(1);
        cache.put(1, "Gudday World");
        cache.getAndRemove(1);

        assertEquals(1, cacheWriter.getWriteCount());
        assertEquals(2, cacheWriter.getDeleteCount());
        assertFalse(cacheWriter.containsKey(1));
    }

    @Test
    public void iterator_remove() {
        assertEquals(0, cacheWriter.getWriteCount());
        assertEquals(0, cacheWriter.getDeleteCount());

        cache.getAndPut(1, "Gudday World");
        cache.getAndPut(2, "Bonjour World");
        cache.getAndPut(3, "Hello World");

        Iterator<Cache.Entry<Integer, String>> iterator = cache.iterator();

        iterator.next();
        iterator.remove();
        iterator.next();
        iterator.next();
        iterator.remove();

        assertEquals(3, cacheWriter.getWriteCount());
        assertEquals(2, cacheWriter.getDeleteCount());
    }

    /**
     * A CacheWriter implementation that records the entries written to it so
     * that they may be later asserted.
     *
     * @param <K> the type of the keys
     * @param <V> the type of the values
     */
    public static class RecordingCacheWriter<K, V> implements CacheWriter<K, V> {

        /**
         * A map of keys to values that have been written.
         */
        private ConcurrentHashMap<K, V> map;

        /**
         * The number of writes that have so far occurred.
         */
        private AtomicLong writeCount;

        /**
         * The number of deletes that have so far occurred.
         */
        private AtomicLong deleteCount;

        /**
         * Constructs a RecordingCacheWriter.
         */
        public RecordingCacheWriter() {
            this.map = new ConcurrentHashMap<K, V>();
            this.writeCount = new AtomicLong();
            this.deleteCount = new AtomicLong();
        }

        @Override
        public void write(Cache.Entry<? extends K, ? extends V> entry) {
            V previous = map.put(entry.getKey(), entry.getValue());
            writeCount.incrementAndGet();
        }

        @Override
        public void writeAll(Collection<Cache.Entry<? extends K, ? extends V>> entries) {
            for(Cache.Entry<? extends K, ? extends V> entry : entries) {
                write(entry);
            }
        }

        @Override
        public void delete(Object key) {
            V previous = map.remove(key);
            deleteCount.incrementAndGet();
        }

        @Override
        public void deleteAll(Collection<?> entries) {
            for(Object key : entries) {
                delete(key);
            }
        }

        /**
         * Gets the last written value of the specified key
         *
         * @param key the key
         * @return the value last written
         */
        public V get(K key) {
            return map.get(key);
        }

        /**
         * Determines if there is a last written value for the specified key
         *
         * @param key the key
         * @return true if there is a last written value
         */
        public boolean containsKey(K key) {
            return map.containsKey(key);
        }

        /**
         * Gets the number of writes that have occurred.
         *
         * @return the number of writes
         */
        public long getWriteCount() {
            return writeCount.get();
        }

        /**
         * Gets the number of deletes that have occurred.
         *
         * @return the number of writes
         */
        public long getDeleteCount() {
            return deleteCount.get();
        }

        /**
         * Clears the contents of stored values.
         */
        public void clear() {
            map.clear();
            this.writeCount = new AtomicLong();
            this.deleteCount = new AtomicLong();
        }
    }
}