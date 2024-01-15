/*
 * Copyright (C) 2022-2024 Hedera Hashgraph, LLC
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

package com.hedera.node.app.service.mono.state.virtual.schedule;

import com.google.common.base.MoreObjects;
import com.hedera.node.app.service.mono.state.submerkle.RichInstant;
import com.hedera.node.app.service.mono.state.virtual.temporal.SecondSinceEpocVirtualKey;
import com.hedera.node.app.service.mono.state.virtual.utils.ThrowingConsumer;
import com.hedera.node.app.service.mono.state.virtual.utils.ThrowingSupplier;
import com.hedera.pbj.runtime.io.ReadableSequentialData;
import com.hedera.pbj.runtime.io.WritableSequentialData;
import com.swirlds.common.io.streams.SerializableDataInputStream;
import com.swirlds.common.io.streams.SerializableDataOutputStream;
import com.swirlds.common.merkle.MerkleLeaf;
import com.swirlds.common.merkle.impl.PartialMerkleLeaf;
import com.swirlds.common.merkle.utility.Keyed;
import com.swirlds.virtualmap.VirtualValue;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Supplier;
import org.eclipse.collections.api.list.primitive.ImmutableLongList;
import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.api.list.primitive.MutableLongList;
import org.eclipse.collections.impl.list.mutable.primitive.LongArrayList;

/**
 * This is currently used in a MerkleMap due to issues with virtual map in the 0.27 release. It
 * should be moved back to VirtualMap in 0.28.
 */
public class ScheduleSecondVirtualValue extends PartialMerkleLeaf
        implements VirtualValue, Keyed<SecondSinceEpocVirtualKey>, MerkleLeaf {

    static final int CURRENT_VERSION = 1;

    static final long RUNTIME_CONSTRUCTABLE_ID = 0x1d2377926e3a85fcL;

    private long number;

    /**
     * The value must be a list because more than one schedule can be scheduled for the same
     * instant.
     */
    private final NavigableMap<RichInstant, ImmutableLongList> ids;

    public ScheduleSecondVirtualValue() {
        this(TreeMap::new, null);
    }

    public ScheduleSecondVirtualValue(Map<RichInstant, ? extends LongList> ids) {
        this(ids, null);
    }

    public ScheduleSecondVirtualValue(Map<RichInstant, ? extends LongList> ids, SecondSinceEpocVirtualKey key) {
        this();
        this.number = key == null ? -1 : key.getKeyAsLong();
        ids.forEach((k, v) -> this.ids.put(k, v.toImmutable()));
    }

    private ScheduleSecondVirtualValue(
            Supplier<NavigableMap<RichInstant, ImmutableLongList>> ids, SecondSinceEpocVirtualKey key) {
        this.ids = ids.get();
        this.number = key == null ? -1 : key.getKeyAsLong();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || ScheduleSecondVirtualValue.class != o.getClass()) {
            return false;
        }

        var that = (ScheduleSecondVirtualValue) o;
        return Objects.equals(this.ids, that.ids);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(ids);
    }

    @Override
    public String toString() {
        var helper = MoreObjects.toStringHelper(ScheduleSecondVirtualValue.class)
                .add("ids", ids)
                .add("number", number);
        return helper.toString();
    }

    int serializedSizeInBytes() {
        int size = 0;
        size += Integer.BYTES; // ids size
        for (var e : ids.entrySet()) {
            size += Integer.BYTES; // value size
            size += Long.BYTES * e.getValue().size(); // value
            size += Long.BYTES; // key seconds
            size += Integer.BYTES; // key nanos
        }
        size += Integer.BYTES; // number
        return size;
    }

    private <E extends Exception> void serializeTo(
            final ThrowingConsumer<Integer, E> writeIntFn, final ThrowingConsumer<Long, E> writeLongFn) throws E {
        writeIntFn.accept(ids.size());
        for (var e : ids.entrySet()) {
            writeIntFn.accept(e.getValue().size());
            for (int x = 0; x < e.getValue().size(); ++x) {
                writeLongFn.accept(e.getValue().get(x));
            }
            writeLongFn.accept(e.getKey().getSeconds());
            writeIntFn.accept(e.getKey().getNanos());
        }
        writeLongFn.accept(number);
    }

    @Override
    public void serialize(SerializableDataOutputStream out) throws IOException {
        serializeTo(out::writeInt, out::writeLong);
    }

    void serialize(final WritableSequentialData out) {
        serializeTo(out::writeInt, out::writeLong);
    }

    @Deprecated
    void serialize(ByteBuffer buffer) {
        serializeTo(buffer::putInt, buffer::putLong);
    }

    private <E extends Exception> void deserializeFrom(
            final ThrowingSupplier<Integer, E> readIntFn, final ThrowingSupplier<Long, E> readLongFn) throws E {
        int s = readIntFn.get();
        ids.clear();
        for (int x = 0; x < s; ++x) {
            int n = readIntFn.get();
            LongArrayList l = new LongArrayList(n);
            for (int y = 0; y < n; ++y) {
                l.add(readLongFn.get());
            }
            if (l.size() > 0) {
                ids.put(new RichInstant(readLongFn.get(), readIntFn.get()), l.toImmutable());
            }
        }
        number = readLongFn.get();
    }

    @Override
    public void deserialize(SerializableDataInputStream in, int version) throws IOException {
        deserializeFrom(in::readInt, in::readLong);
    }

    void deserialize(final ReadableSequentialData in) {
        deserializeFrom(in::readInt, in::readLong);
    }

    @Deprecated
    void deserialize(ByteBuffer buffer, int version) {
        deserializeFrom(buffer::getInt, buffer::getLong);
    }

    @Override
    public int getVersion() {
        return CURRENT_VERSION;
    }

    @Override
    public long getClassId() {
        return RUNTIME_CONSTRUCTABLE_ID;
    }

    @Override
    public ScheduleSecondVirtualValue copy() {
        var fc = new ScheduleSecondVirtualValue(ids, new SecondSinceEpocVirtualKey(number));

        this.setImmutable(true);

        return fc;
    }

    public void add(RichInstant instant, LongList idList) {
        throwIfImmutable("Cannot add to ids if it's immutable.");

        final ImmutableLongList l = ids.get(instant);

        final MutableLongList m;
        if (l == null) {
            m = new LongArrayList(idList.size());
        } else {
            m = new LongArrayList(l.size() + idList.size());
            l.forEach(m::add);
        }
        m.addAll(idList);
        ids.put(instant, m.toImmutable());
    }

    public void removeId(RichInstant instant, long id) {
        throwIfImmutable("Cannot remove from ids if it's immutable.");

        final ImmutableLongList curList = ids.get(instant);

        if (curList != null) {
            final var newList = new LongArrayList(curList.size());
            curList.forEach(l -> {
                if (l != id) {
                    newList.add(l);
                }
            });
            if (newList.size() > 0) {
                ids.put(instant, newList.toImmutable());
            } else {
                ids.remove(instant);
            }
        }
    }

    public NavigableMap<RichInstant, ImmutableLongList> getIds() {
        return Collections.unmodifiableNavigableMap(ids);
    }

    /** {@inheritDoc} */
    @Override
    public ScheduleSecondVirtualValue asReadOnly() {
        var c = new ScheduleSecondVirtualValue(this::getIds, new SecondSinceEpocVirtualKey(number));
        c.setImmutable(true);
        return c;
    }

    /**
     * Needed until getForModify works on VirtualMap
     *
     * @return a copy of this without marking this as immutable
     */
    public ScheduleSecondVirtualValue asWritable() {
        return new ScheduleSecondVirtualValue(this.ids, new SecondSinceEpocVirtualKey(number));
    }

    @Override
    public SecondSinceEpocVirtualKey getKey() {
        return new SecondSinceEpocVirtualKey(number);
    }

    @Override
    public void setKey(final SecondSinceEpocVirtualKey key) {
        this.number = key == null ? -1 : key.getKeyAsLong();
    }
}
