/*
 * Copyright (C) 2023-2024 Hedera Hashgraph, LLC
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

package com.hedera.node.app.records.impl.producers.formats;

import static com.hedera.node.app.records.RecordTestData.SIGNER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hedera.node.app.fixtures.AppTestBase;
import com.hedera.node.app.records.impl.producers.formats.v6.BlockRecordWriterV6;
import java.nio.file.FileSystems;
import org.junit.jupiter.api.Test;

final class BlockRecordFactoryImplTest extends AppTestBase {
    @Test
    void createV6BasedOnConfig() throws Exception {
        final var app = appBuilder().build();
        final var factory =
                new BlockRecordWriterFactoryImpl(app.configProvider(), selfNodeInfo, SIGNER, FileSystems.getDefault());
        final var writer = factory.create();
        assertThat(writer).isInstanceOf(BlockRecordWriterV6.class);
    }

    @Test
    void createV7BasedOnConfigThrows() throws Exception {
        final var app = appBuilder()
                .withConfigValue("hedera.recordStream.recordFileVersion", 7)
                .build();

        final var factory =
                new BlockRecordWriterFactoryImpl(app.configProvider(), selfNodeInfo, SIGNER, FileSystems.getDefault());
        assertThatThrownBy(factory::create)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Record file version 7 is not yet supported");
    }

    @Test
    void createUnknownVersionBasedOnConfigThrows() throws Exception {
        final var app = appBuilder()
                .withConfigValue("hedera.recordStream.recordFileVersion", 99999)
                .build();

        final var factory =
                new BlockRecordWriterFactoryImpl(app.configProvider(), selfNodeInfo, SIGNER, FileSystems.getDefault());
        assertThatThrownBy(factory::create)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown record file version");
    }
}
