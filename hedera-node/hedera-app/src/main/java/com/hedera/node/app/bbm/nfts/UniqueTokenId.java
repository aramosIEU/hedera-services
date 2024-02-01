/*
 * Copyright (C) 2024 Hedera Hashgraph, LLC
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

package com.hedera.node.app.bbm.nfts;

import com.google.common.collect.ComparisonChain;
import com.hedera.hapi.node.base.NftID;
import com.hedera.node.app.bbm.utils.Writer;
import com.hedera.node.app.service.mono.state.virtual.UniqueTokenKey;
import edu.umd.cs.findbugs.annotations.NonNull;

record UniqueTokenId(long id, long serial) implements Comparable<UniqueTokenId> {

    static UniqueTokenId fromMono(@NonNull final UniqueTokenKey ukey) {
        return new UniqueTokenId(ukey.getNum(), ukey.getTokenSerial());
    }

    static UniqueTokenId fromMod(@NonNull final NftID nftID) {
        return new UniqueTokenId(nftID.tokenIdOrThrow().tokenNum(), nftID.serialNumber());
    }

    @Override
    public String toString() {
        return "%d%s%d".formatted(id, Writer.FIELD_SEPARATOR, serial);
    }

    @Override
    public int compareTo(UniqueTokenId o) {
        return ComparisonChain.start()
                .compare(this.id, o.id)
                .compare(this.serial, o.serial)
                .result();
    }
}
