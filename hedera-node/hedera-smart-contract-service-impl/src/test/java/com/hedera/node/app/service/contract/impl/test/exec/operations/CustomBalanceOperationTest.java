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

package com.hedera.node.app.service.contract.impl.test.exec.operations;

import static com.hedera.node.app.service.contract.impl.exec.failure.CustomExceptionalHaltReason.INVALID_SOLIDITY_ADDRESS;
import static com.hedera.node.app.service.contract.impl.test.TestHelpers.NON_SYSTEM_LONG_ZERO_ADDRESS;
import static com.hedera.node.app.service.contract.impl.test.TestHelpers.SYSTEM_ADDRESS;
import static com.hedera.node.app.service.contract.impl.test.TestHelpers.assertSameResult;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.hedera.node.app.service.contract.impl.exec.AddressChecks;
import com.hedera.node.app.service.contract.impl.exec.FeatureFlags;
import com.hedera.node.app.service.contract.impl.exec.operations.CustomBalanceOperation;
import com.hedera.node.app.service.contract.impl.exec.utils.FrameUtils;
import com.hedera.node.app.service.contract.impl.state.ProxyWorldUpdater;
import org.apache.tuweni.units.bigints.UInt256;
import org.hyperledger.besu.evm.EVM;
import org.hyperledger.besu.evm.frame.ExceptionalHaltReason;
import org.hyperledger.besu.evm.frame.MessageFrame;
import org.hyperledger.besu.evm.gascalculator.GasCalculator;
import org.hyperledger.besu.evm.internal.UnderflowException;
import org.hyperledger.besu.evm.operation.Operation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CustomBalanceOperationTest {
    @Mock
    private GasCalculator gasCalculator;

    @Mock
    private AddressChecks addressChecks;

    @Mock
    private MessageFrame frame;

    @Mock
    private FeatureFlags featureFlags;

    @Mock
    private EVM evm;

    @Mock
    private ProxyWorldUpdater updater;

    private CustomBalanceOperation subject;

    @BeforeEach
    void setUp() {
        subject = new CustomBalanceOperation(gasCalculator, addressChecks);
    }

    @Test
    void catchesUnderflowWhenStackIsEmpty() {
        setupWarmGasCost();
        given(frame.getStackItem(0)).willThrow(UnderflowException.class);
        final var expected = new Operation.OperationResult(3L, ExceptionalHaltReason.INSUFFICIENT_STACK_ITEMS);
        final var actual = subject.execute(frame, evm);
        assertSameResult(expected, actual);
    }

    @Test
    void systemAccountBalanceHardCodedToZero() {
        setupWarmGasCost();
        given(frame.getStackItem(0)).willReturn(SYSTEM_ADDRESS);
        given(addressChecks.isSystemAccount(SYSTEM_ADDRESS)).willReturn(true);
        final var expected = new Operation.OperationResult(3L, null);
        final var actual = subject.execute(frame, evm);
        assertSameResult(expected, actual);
        verify(frame).popStackItem();
        verify(frame).pushStackItem(UInt256.ZERO);
    }

    @Test
    void rejectsMissingUserAddressIfAllowCallFeatureFlagOff() {
        try (MockedStatic<FrameUtils> frameUtils = Mockito.mockStatic(FrameUtils.class)) {
            setupWarmGasCost();
            given(frame.getStackItem(0)).willReturn(NON_SYSTEM_LONG_ZERO_ADDRESS);
            given(updater.contractMustBePresent()).willReturn(true);
            frameUtils.when(() -> FrameUtils.proxyUpdaterFor(frame)).thenReturn(updater);
            final var expected = new Operation.OperationResult(3L, INVALID_SOLIDITY_ADDRESS);
            final var actual = subject.execute(frame, evm);
            assertSameResult(expected, actual);
        }
    }

    @Test
    void delegatesToSuperIfAllowCallFeatureFlagOn() {
        try (MockedStatic<FrameUtils> frameUtils = Mockito.mockStatic(FrameUtils.class)) {
            setupWarmGasCost();
            given(frame.getStackItem(0)).willReturn(NON_SYSTEM_LONG_ZERO_ADDRESS);
            given(frame.popStackItem()).willReturn(NON_SYSTEM_LONG_ZERO_ADDRESS);
            given(frame.warmUpAddress(NON_SYSTEM_LONG_ZERO_ADDRESS)).willReturn(true);
            frameUtils.when(() -> FrameUtils.proxyUpdaterFor(frame)).thenReturn(updater);
            final var expected = new Operation.OperationResult(3L, ExceptionalHaltReason.INSUFFICIENT_GAS);
            final var actual = subject.execute(frame, evm);
            assertSameResult(expected, actual);
        }
    }

    @Test
    void delegatesToSuperForPresentUserAddress() {
        try (MockedStatic<FrameUtils> frameUtils = Mockito.mockStatic(FrameUtils.class)) {
            setupWarmGasCost();
            given(frame.getStackItem(0)).willReturn(NON_SYSTEM_LONG_ZERO_ADDRESS);
            given(frame.popStackItem()).willReturn(NON_SYSTEM_LONG_ZERO_ADDRESS);
            given(frame.warmUpAddress(NON_SYSTEM_LONG_ZERO_ADDRESS)).willReturn(true);
            given(addressChecks.isPresent(NON_SYSTEM_LONG_ZERO_ADDRESS, frame)).willReturn(true);
            given(updater.contractMustBePresent()).willReturn(true);
            frameUtils.when(() -> FrameUtils.proxyUpdaterFor(frame)).thenReturn(updater);
            final var expected = new Operation.OperationResult(3L, ExceptionalHaltReason.INSUFFICIENT_GAS);
            final var actual = subject.execute(frame, evm);
            assertSameResult(expected, actual);
        }
    }

    private void setupWarmGasCost() {
        given(gasCalculator.getBalanceOperationGasCost()).willReturn(1L);
        given(gasCalculator.getWarmStorageReadCost()).willReturn(2L);
    }
}
