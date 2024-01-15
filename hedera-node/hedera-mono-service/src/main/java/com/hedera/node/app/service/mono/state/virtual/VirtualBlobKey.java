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

import static com.hedera.node.app.service.mono.files.store.FcBlobsBytesStore.LEGACY_BLOB_CODE_INDEX;
import static java.lang.Long.parseLong;

import com.hedera.node.app.service.mono.state.merkle.internals.BitPackUtils;
import com.hedera.node.app.service.mono.utils.MiscUtils;
import com.hedera.pbj.runtime.io.ReadableSequentialData;
import com.hedera.pbj.runtime.io.WritableSequentialData;
import com.hedera.pbj.runtime.io.buffer.BufferedData;
import com.swirlds.common.io.streams.SerializableDataInputStream;
import com.swirlds.common.io.streams.SerializableDataOutputStream;
import com.swirlds.virtualmap.VirtualKey;
import java.io.IOException;
import java.nio.ByteBuffer;

public class VirtualBlobKey implements VirtualKey {
    public static final int CURRENT_VERSION = 1;

    static final int BYTES_IN_SERIALIZED_FORM = 5;
    static final long CLASS_ID = 0x11b982c14217d523L;

    private static final Type[] BLOB_TYPES = Type.values();

    public enum Type {
        FILE_DATA,
        FILE_METADATA,
        CONTRACT_BYTECODE,
        SYSTEM_DELETED_ENTITY_EXPIRY
    }

    private Type type;
    private int entityNumCode;

    public VirtualBlobKey() {
        /* Required by deserialization facility */
    }

    public VirtualBlobKey(final Type type, final int entityNumCode) {
        this.type = type;
        this.entityNumCode = entityNumCode;
    }

    public static VirtualBlobKey fromPath(final String path) {
        final var code = path.charAt(LEGACY_BLOB_CODE_INDEX);
        final var packedNum = BitPackUtils.codeFromNum(parseLong(path.substring(LEGACY_BLOB_CODE_INDEX + 1)));

        return switch (code) {
            case 'f' -> new VirtualBlobKey(Type.FILE_DATA, packedNum);
            case 'k' -> new VirtualBlobKey(Type.FILE_METADATA, packedNum);
            case 's' -> new VirtualBlobKey(Type.CONTRACT_BYTECODE, packedNum);
            case 'e' -> new VirtualBlobKey(Type.SYSTEM_DELETED_ENTITY_EXPIRY, packedNum);
            default -> throw new IllegalArgumentException("Invalid code in blob path '" + path + "'");
        };
    }

    @Override
    public void serialize(final SerializableDataOutputStream out) throws IOException {
        out.writeByte(type.ordinal());
        out.writeInt(entityNumCode);
    }

    void serialize(final WritableSequentialData out) {
        out.writeByte((byte) type.ordinal());
        out.writeInt(entityNumCode);
    }

    @Deprecated
    void serialize(final ByteBuffer buffer) {
        buffer.put((byte) type.ordinal());
        buffer.putInt(entityNumCode);
    }

    @Override
    public void deserialize(final SerializableDataInputStream in, final int version) throws IOException {
        type = BLOB_TYPES[0xff & in.readByte()];
        entityNumCode = in.readInt();
    }

    void deserialize(final ReadableSequentialData in) {
        type = BLOB_TYPES[0xff & in.readByte()];
        entityNumCode = in.readInt();
    }

    @Deprecated
    void deserialize(final ByteBuffer buffer) {
        type = BLOB_TYPES[0xff & buffer.get()];
        entityNumCode = buffer.getInt();
    }

    @Override
    public long getClassId() {
        return CLASS_ID;
    }

    @Override
    public int getVersion() {
        return CURRENT_VERSION;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || VirtualBlobKey.class != o.getClass()) {
            return false;
        }

        final var that = (VirtualBlobKey) o;

        return this.type == that.type && this.entityNumCode == that.entityNumCode;
    }

    @Override
    public int hashCode() {
        return (int) MiscUtils.perm64(entityNumCode | ((long) type.ordinal()) << 4);
    }

    public static int sizeInBytes() {
        return BYTES_IN_SERIALIZED_FORM;
    }

    public Type getType() {
        return type;
    }

    public int getEntityNumCode() {
        return entityNumCode;
    }

    @Override
    public int getMinimumSupportedVersion() {
        return CURRENT_VERSION;
    }

    /**
     * Verifies if the content from the buffer is equal to this virtual blob key.
     *
     * @param buffer The buffer with data to be compared with this key
     * @return If the content from the buffer has the same data as this instance
     */
    boolean equalsTo(final BufferedData buffer) {
        return (type.ordinal() == (0xff & buffer.readByte())) && (entityNumCode == buffer.readInt());
    }

    @Deprecated
    boolean equalsTo(final ByteBuffer buffer, final int version) {
        return (type.ordinal() == (0xff & buffer.get())) && (entityNumCode == buffer.getInt());
    }
}
