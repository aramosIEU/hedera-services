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

import static com.hedera.node.app.bbm.utils.ThingsToStrings.getMaybeStringifyByteString;
import static com.swirlds.common.threading.manager.AdHocThreadManager.getStaticThreadManager;

import com.hedera.hapi.node.base.NftID;
import com.hedera.hapi.node.state.token.Nft;
import com.hedera.node.app.bbm.DumpCheckpoint;
import com.hedera.node.app.bbm.utils.FieldBuilder;
import com.hedera.node.app.bbm.utils.ThingsToStrings;
import com.hedera.node.app.bbm.utils.Writer;
import com.hedera.node.app.service.mono.state.adapters.VirtualMapLike;
import com.hedera.node.app.service.mono.state.virtual.UniqueTokenKey;
import com.hedera.node.app.service.mono.state.virtual.UniqueTokenValue;
import com.hedera.node.app.state.merkle.disk.OnDiskKey;
import com.hedera.node.app.state.merkle.disk.OnDiskValue;
import com.swirlds.base.utility.Pair;
import com.swirlds.virtualmap.VirtualKey;
import com.swirlds.virtualmap.VirtualMap;
import com.swirlds.virtualmap.VirtualValue;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class UniqueTokenDumpUtils {
    public static void dumpModUniqueTokens(
            @NonNull final Path path,
            @NonNull final VirtualMap<OnDiskKey<NftID>, OnDiskValue<Nft>> uniques,
            @NonNull final DumpCheckpoint checkpoint) {
        try (@NonNull final var writer = new Writer(path)) {
            final var dumpableUniques =
                    gatherUniques(uniques, key -> UniqueTokenId.fromMod(key.getKey()), UniqueToken::fromMod);
            reportOnUniques(writer, dumpableUniques);
            System.out.printf(
                    "=== mod  uniques report is %d bytes at checkpoint %s%n", writer.getSize(), checkpoint.name());
        }
    }

    public static void dumpMonoUniqueTokens(
            @NonNull final Path path,
            @NonNull final VirtualMap<UniqueTokenKey, UniqueTokenValue> uniques,
            @NonNull final DumpCheckpoint checkpoint) {
        try (@NonNull final var writer = new Writer(path)) {
            final var dumpableUniques = gatherUniques(uniques, UniqueTokenId::fromMono, UniqueToken::fromMono);
            reportOnUniques(writer, dumpableUniques);
            System.out.printf(
                    "=== mono uniques report is %d bytes at checkpoint %s%n", writer.getSize(), checkpoint.name());
        }
    }

    @NonNull
    private static <K extends VirtualKey, V extends VirtualValue> Map<UniqueTokenId, UniqueToken> gatherUniques(
            @NonNull final VirtualMap<K, V> source,
            @NonNull final Function<K, UniqueTokenId> keyMapper,
            @NonNull final Function<V, UniqueToken> valueMapper) {
        final var r = new HashMap<UniqueTokenId, UniqueToken>();
        final var threadCount = 8; // Good enough for my laptop, why not?
        final var mappings = new ConcurrentLinkedQueue<Pair<UniqueTokenId, UniqueToken>>();
        try {
            VirtualMapLike.from(source)
                    .extractVirtualMapDataC(
                            getStaticThreadManager(),
                            p -> mappings.add(Pair.of(keyMapper.apply(p.left()), valueMapper.apply(p.right()))),
                            threadCount);
        } catch (final InterruptedException ex) {
            System.err.println("*** Traversal of uniques virtual map interrupted!");
            Thread.currentThread().interrupt();
        }
        // Consider in the future: Use another thread to pull things off the queue as they're put on by the
        // virtual map traversal
        while (!mappings.isEmpty()) {
            final var mapping = mappings.poll();
            r.put(mapping.key(), mapping.value());
        }
        return r;
    }

    private static void reportOnUniques(
            @NonNull final Writer writer, @NonNull final Map<UniqueTokenId, UniqueToken> uniques) {
        writer.writeln(formatHeader());
        uniques.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> formatUnique(writer, e.getKey(), e.getValue()));
        writer.writeln("");
    }

    @NonNull
    private static String formatHeader() {
        return "nftId,nftSerial,"
                + fieldFormatters.stream().map(Pair::left).collect(Collectors.joining(Writer.FIELD_SEPARATOR));
    }

    // spotless:off
    @NonNull
    private static final List<Pair<String, BiConsumer<FieldBuilder, UniqueToken>>> fieldFormatters = List.of(
            Pair.of("owner", getFieldFormatter(UniqueToken::owner, ThingsToStrings::toStringOfEntityId)),
            Pair.of("spender", getFieldFormatter(UniqueToken::spender, ThingsToStrings::toStringOfEntityId)),
            Pair.of("creationTime", getFieldFormatter(UniqueToken::creationTime, ThingsToStrings::toStringOfRichInstant)),
            Pair.of("metadata", getFieldFormatter(UniqueToken::metadata, getMaybeStringifyByteString(Writer.FIELD_SEPARATOR))),
            Pair.of("prev", getFieldFormatter(UniqueToken::previous, Object::toString)),
            Pair.of("next", getFieldFormatter(UniqueToken::next, Object::toString))
    );
    // spotless:on

    @NonNull
    static <T> BiConsumer<FieldBuilder, UniqueToken> getFieldFormatter(
            @NonNull final Function<UniqueToken, T> fun, @NonNull final Function<T, String> formatter) {
        return (fb, u) -> formatField(fb, u, fun, formatter);
    }

    static <T> void formatField(
            @NonNull final FieldBuilder fb,
            @NonNull final UniqueToken unique,
            @NonNull final Function<UniqueToken, T> fun,
            @NonNull final Function<T, String> formatter) {
        fb.append(formatter.apply(fun.apply(unique)));
    }

    private static void formatUnique(
            @NonNull final Writer writer, @NonNull final UniqueTokenId id, @NonNull final UniqueToken unique) {
        final var fb = new FieldBuilder(Writer.FIELD_SEPARATOR);
        fb.append(id.toString());
        fieldFormatters.stream().map(Pair::right).forEach(ff -> ff.accept(fb, unique));
        writer.writeln(fb);
    }
}
