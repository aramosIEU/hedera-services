/*
 * Copyright (C) 2023 Hedera Hashgraph, LLC
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

package com.hedera.node.app.service.token.records;

import com.hedera.node.app.spi.workflows.record.SingleTransactionRecordBuilder;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.List;

/**
 * A {@code RecordBuilder} specialization for tracking the side effects of a {@code TokenBurn}
 * transaction.
 */
public interface TokenBurnRecordBuilder extends SingleTransactionRecordBuilder {

    /**
     * Gets the new total supply of a token
     * @return new total supply of a token
     */
    long getNewTotalSupply();

    /**
     * Sets the new total supply of a token
     * @param newTotalSupply the new total supply of a token
     */
    @NonNull
    TokenBurnRecordBuilder newTotalSupply(final long newTotalSupply);

    /**
     * Sets the list of serial numbers burned
     * @param serialNumbers list of serial numbers burned
     */
    @NonNull
    TokenBurnRecordBuilder serialNumbers(@NonNull List<Long> serialNumbers);
}