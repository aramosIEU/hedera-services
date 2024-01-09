/*
 * Copyright (C) 2018-2024 Hedera Hashgraph, LLC
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

package com.swirlds.common.context;

import com.swirlds.base.time.Time;
import com.swirlds.common.crypto.Cryptography;
import com.swirlds.common.metrics.Metrics;
import com.swirlds.config.api.Configuration;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Objects;

/**
 * Default implementation of the platform context. Warning: this class is private API and will be changed in the future.
 * The {@link PlatformContext} interface should be used all the time to interact with basic services.
 */
public final class DefaultPlatformContext implements PlatformContext {

    private final Configuration configuration;
    private final Metrics metrics;
    private final Cryptography cryptography;
    private final Time time;

    /**
     * Constructor.
     *
     * @param configuration the configuration
     * @param metrics       the metrics
     * @param cryptography  the cryptography
     * @param time          the time
     */
    public DefaultPlatformContext(
            @NonNull final Configuration configuration,
            @NonNull final Metrics metrics,
            @NonNull final Cryptography cryptography,
            @NonNull final Time time) {

        this.configuration = Objects.requireNonNull(configuration);
        this.metrics = Objects.requireNonNull(metrics);
        this.cryptography = Objects.requireNonNull(cryptography);
        this.time = Objects.requireNonNull(time);
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public Cryptography getCryptography() {
        return cryptography;
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public Metrics getMetrics() {
        return metrics;
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public Time getTime() {
        return time;
    }
}
