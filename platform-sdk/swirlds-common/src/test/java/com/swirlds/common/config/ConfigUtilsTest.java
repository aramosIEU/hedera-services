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

package com.swirlds.common.config;

import com.swirlds.common.config.sub.TestConfig;
import com.swirlds.config.api.Configuration;
import com.swirlds.config.api.ConfigurationBuilder;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ConfigUtilsTest {

    @Test
    @DisplayName("Should scan and register all expected config records")
    void testDefaultBehavior() {
        // given
        final ConfigurationBuilder configurationBuilder = ConfigurationBuilder.create();

        ConfigUtils.scanAndRegisterAllConfigTypes(configurationBuilder);
        final Configuration configuration = configurationBuilder.build();

        // then
        Assertions.assertFalse(configuration.getConfigDataTypes().isEmpty());
        Assertions.assertTrue(configuration.getConfigDataTypes().contains(BasicCommonConfig.class));
        Assertions.assertTrue(configuration.getConfigDataTypes().contains(StateCommonConfig.class));
    }

    @Test
    void testNotExistingPackage() {
        // given
        final ConfigurationBuilder configurationBuilder = ConfigurationBuilder.create();

        // when
        ConfigUtils.scanAndRegisterAllConfigTypes(configurationBuilder, Set.of("not.available.package"));
        final Configuration configuration = configurationBuilder.build();

        // then
        Assertions.assertTrue(configuration.getConfigDataTypes().isEmpty());
    }

    @Test
    void testExistingPackage() {
        // given
        final ConfigurationBuilder configurationBuilder = ConfigurationBuilder.create();

        // when
        ConfigUtils.scanAndRegisterAllConfigTypes(configurationBuilder, Set.of("com.swirlds.common.config.sub"));
        final Configuration configuration = configurationBuilder.build();

        // then
        Assertions.assertFalse(configuration.getConfigDataTypes().isEmpty());
        Assertions.assertEquals(1, configuration.getConfigDataTypes().size());
        Assertions.assertTrue(configuration.getConfigDataTypes().contains(TestConfig.class));
    }
}
