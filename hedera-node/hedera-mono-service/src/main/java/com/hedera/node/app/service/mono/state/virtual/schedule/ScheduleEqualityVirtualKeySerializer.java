/*
 * Copyright (C) 2021-2024 Hedera Hashgraph, LLC
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

import com.hedera.pbj.runtime.io.ReadableSequentialData;
import com.hedera.pbj.runtime.io.WritableSequentialData;
import com.hedera.pbj.runtime.io.buffer.BufferedData;
import com.swirlds.merkledb.serialize.KeyIndexType;
import com.swirlds.merkledb.serialize.KeySerializer;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Objects;

public class ScheduleEqualityVirtualKeySerializer implements KeySerializer<ScheduleEqualityVirtualKey> {

    static final long CLASS_ID = 0xc7b4f042e2fe2417L;

    static final int CURRENT_VERSION = 1;

    static final long DATA_VERSION = 1;

    // Serializer info

    @Override
    public long getClassId() {
        return CLASS_ID;
    }

    @Override
    public int getVersion() {
        return CURRENT_VERSION;
    }

    // Data version

    @Override
    public long getCurrentDataVersion() {
        return DATA_VERSION;
    }

    // Key serialization

    @Override
    public KeyIndexType getIndexType() {
        return KeyIndexType.GENERIC;
    }

    @Override
    public int getSerializedSize() {
        return ScheduleEqualityVirtualKey.sizeInBytes();
    }

    @Override
    public void serialize(@NonNull final ScheduleEqualityVirtualKey key, @NonNull final WritableSequentialData out) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(out);
        key.serialize(out);
    }

    @Override
    @Deprecated
    public void serialize(final ScheduleEqualityVirtualKey key, final ByteBuffer buffer) throws IOException {
        key.serialize(buffer);
    }

    // Key deserialization

    @Override
    public ScheduleEqualityVirtualKey deserialize(@NonNull ReadableSequentialData in) {
        Objects.requireNonNull(in);
        final var key = new ScheduleEqualityVirtualKey();
        key.deserialize(in);
        return key;
    }

    @Override
    @Deprecated
    public ScheduleEqualityVirtualKey deserialize(final ByteBuffer buffer, final long version) throws IOException {
        final var key = new ScheduleEqualityVirtualKey();
        key.deserialize(buffer);
        return key;
    }

    @Override
    public boolean equals(@NonNull BufferedData buffer, @NonNull ScheduleEqualityVirtualKey keyToCompare) {
        Objects.requireNonNull(buffer);
        Objects.requireNonNull(keyToCompare);
        return keyToCompare.equalsTo(buffer);
    }

    @Override
    @Deprecated
    public boolean equals(ByteBuffer buffer, int version, ScheduleEqualityVirtualKey key) {
        return key.equalsTo(buffer, version);
    }
}
