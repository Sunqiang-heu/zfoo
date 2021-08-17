/*
 * Copyright (C) 2020 The zfoo Authors
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.zfoo.orm.lpmap;

import com.zfoo.protocol.IPacket;
import io.netty.util.collection.LongObjectHashMap;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @author jaysunxiao
 * @version 3.0
 */
public class HeapMap<V extends IPacket> implements LpMap<V> {

    protected LongObjectHashMap<V> map;

    /**
     * 没有被使用的key
     */
    protected Queue<Long> freeKeyQueue = new LinkedList<>();

    protected long maxIndex = 0;

    public HeapMap(int initialCapacity) {
        map = new LongObjectHashMap<>(initialCapacity);
    }

    @Override
    public long insert(V value) {
        if (freeKeyQueue.isEmpty()) {
            map.put(++maxIndex, value);
            return maxIndex;
        } else {
            var freeKey = freeKeyQueue.poll();
            map.put(freeKey, value);
            return freeKey;
        }
    }

    @Override
    public V put(long key, V value) {
        checkKey(key);
        if (key <= maxIndex) {
            return map.put(key, value);
        } else {
            for (var i = maxIndex + 1; i < key; i++) {
                freeKeyQueue.add(i);
            }
            maxIndex = key;
            map.put(key, value);
            return null;
        }
    }

    @Override
    public V delete(long key) {
        checkKey(key);
        if (key > maxIndex) {
            return null;
        } else {
            var previousValue = map.remove(key);
            freeKeyQueue.add(key);
            return previousValue;
        }
    }

    @Override
    public V get(long key) {
        checkKey(key);
        return map.get(key);
    }
}
