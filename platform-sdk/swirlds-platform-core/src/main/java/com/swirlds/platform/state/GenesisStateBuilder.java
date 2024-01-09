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

package com.swirlds.platform.state;

import com.swirlds.common.config.BasicConfig;
import com.swirlds.common.context.PlatformContext;
import com.swirlds.platform.state.signed.ReservedSignedState;
import com.swirlds.platform.state.signed.SignedState;
import com.swirlds.platform.system.SoftwareVersion;
import com.swirlds.platform.system.SwirldState;
import com.swirlds.platform.system.address.AddressBook;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.time.Instant;

/**
 * Responsible for building the genesis state.
 */
public final class GenesisStateBuilder {

    private GenesisStateBuilder() {}

    /**
     * Construct a genesis platform state.
     *
     * @return a genesis platform state
     */
    private static PlatformState buildGenesisPlatformState(
            final AddressBook addressBook, final SoftwareVersion appVersion) {

        final PlatformState platformState = new PlatformState();
        platformState.setAddressBook(addressBook.copy());

        platformState.setCreationSoftwareVersion(appVersion);
        platformState.setRound(0);
        platformState.setRunningEventHash(null);
        platformState.setEpochHash(null);
        platformState.setConsensusTimestamp(Instant.ofEpochSecond(0L));

        return platformState;
    }

    /**
     * Build and initialize a genesis state.
     *
     * @param platformContext the platform context
     * @param addressBook     the current address book
     * @param appVersion      the software version of the app
     * @param swirldState     the application's genesis state
     * @return a reserved genesis signed state
     */
    public static ReservedSignedState buildGenesisState(
            @NonNull final PlatformContext platformContext,
            @NonNull final AddressBook addressBook,
            @NonNull final SoftwareVersion appVersion,
            @NonNull final SwirldState swirldState) {

        final BasicConfig basicConfig = platformContext.getConfiguration().getConfigData(BasicConfig.class);
        final State state = new State();
        state.setPlatformState(buildGenesisPlatformState(addressBook, appVersion));
        state.setSwirldState(swirldState);

        final long genesisFreezeTime = basicConfig.genesisFreezeTime();
        if (genesisFreezeTime > 0) {
            state.getPlatformState().setFreezeTime(Instant.ofEpochSecond(genesisFreezeTime));
        }

        final SignedState signedState = new SignedState(platformContext, state, "genesis state", false);
        return signedState.reserve("initial reservation on genesis state");
    }
}
