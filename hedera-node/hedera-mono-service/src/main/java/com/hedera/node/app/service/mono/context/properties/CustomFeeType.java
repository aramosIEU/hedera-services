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

package com.hedera.node.app.service.mono.context.properties;

import static com.hedera.node.app.service.mono.utils.MiscUtils.csvSet;

import java.util.Set;

/**
 * These are specifically for custom fees where the receiver pays the fee - this is **not** a
 * enumeration of all possible custom fee types. See
 * `contracts.precompile.unsupportedCustomFeeReceiverDebits`
 */
public enum CustomFeeType {
    FIXED_FEE,
    ROYALTY_FALLBACK_FEE;

    public static Set<CustomFeeType> csvTypeSet(final String propertyValue) {
        return csvSet(propertyValue, CustomFeeType::valueOf, CustomFeeType.class);
    }
}