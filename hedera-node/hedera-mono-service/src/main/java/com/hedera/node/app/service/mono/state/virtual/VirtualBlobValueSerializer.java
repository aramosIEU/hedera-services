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

package com.hedera.node.app.service.mono.state.virtual;

import com.hedera.pbj.runtime.io.ReadableSequentialData;
import com.hedera.pbj.runtime.io.WritableSequentialData;
import com.swirlds.merkledb.serialize.ValueSerializer;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.nio.ByteBuffer;
import java.util.Objects;

public class VirtualBlobValueSerializer implements ValueSerializer<VirtualBlobValue> {

    // Serializer class ID
    static final long CLASS_ID = 0xd08565ba3cf6c5c0L;

    // Serializer version
    static final int CURRENT_VERSION = 1;

    // Value data version
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

    // Value info

    @Override
    public long getCurrentDataVersion() {
        return DATA_VERSION;
    }

    @Override
    public int getSerializedSize() {
        return VARIABLE_DATA_SIZE;
    }

    @Override
    public int getSerializedSize(VirtualBlobValue data) {
        return data.getSizeInBytes();
    }

    @Override
    public int getTypicalSerializedSize() {
        return VirtualBlobValue.getTypicalSerializedSize();
    }

    // Value serialization

    @Override
    public void serialize(@NonNull final VirtualBlobValue value, @NonNull final WritableSequentialData out) {
        Objects.requireNonNull(value);
        Objects.requireNonNull(out);
        value.serialize(out);
    }

    @Override
    @Deprecated
    public void serialize(final VirtualBlobValue value, final ByteBuffer out) {
        value.serialize(out);
    }

    // Value deserialization

    @Override
    public VirtualBlobValue deserialize(@NonNull final ReadableSequentialData in) {
        Objects.requireNonNull(in);
        final VirtualBlobValue value = new VirtualBlobValue();
        value.deserialize(in);
        return value;
    }

    @Override
    @Deprecated
    public VirtualBlobValue deserialize(final ByteBuffer buffer, final long version) {
        final VirtualBlobValue value = new VirtualBlobValue();
        value.deserialize(buffer, (int) version);
        return value;
    }
}
