/*
 * Copyright (C) 2016-2024 Hedera Hashgraph, LLC
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

package com.swirlds.common.metrics;

import static com.swirlds.metrics.api.Metric.ValueType.VALUE;

import com.swirlds.base.utility.ToStringBuilder;
import com.swirlds.metrics.api.Metric;
import com.swirlds.metrics.api.MetricType;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.EnumSet;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * A {@code FunctionGauge} maintains a single value.
 * <p>
 * Unlike the other gauges, the value of a {@code FunctionGauge} is not explicitly set. Instead,
 * a {@link java.util.function.Supplier} has to be specified, which reads the current value of this gauge.
 * <p>
 * Only the current value is stored, no history or distribution is kept.
 *
 * @param <T> the type of the contained value
 */
public interface FunctionGauge<T> extends Metric {

    /**
     * {@inheritDoc}
     */
    @Override
    default MetricType getMetricType() {
        return MetricType.GAUGE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default EnumSet<ValueType> getValueTypes() {
        return EnumSet.of(VALUE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default T get(final ValueType valueType) {
        Objects.requireNonNull(valueType, "valueType");
        if (valueType == VALUE) {
            return get();
        }
        throw new IllegalArgumentException("Unsupported ValueType: " + valueType);
    }

    /**
     * Get the current value
     *
     * @return the current value
     */
    T get();

    /**
     * Configuration of a {@link FunctionGauge}
     *
     * @param <T> the type of the value that will be contained in the {@code FunctionGauge}
     */
    final class Config<T> extends PlatformMetricConfig<FunctionGauge<T>, Config<T>> {

        private final Class<T> type;
        private final Supplier<T> supplier;

        /**
         * Constructor of {@code FunctionGauge.Config}
         *
         * @param category
         * 		the kind of metric (metrics are grouped or filtered by this)
         * @param name
         * 		a short name for the metric
         * @param type
         * 		the type of the values this {@code FunctionGauge} returns
         * @param supplier
         * 		the {@code Supplier} of the value of this {@code Gauge}
         * @throws IllegalArgumentException
         * 		if one of the parameters is {@code null} or consists only of whitespaces
         */
        public Config(
                @NonNull final String category,
                @NonNull final String name,
                @NonNull final Class<T> type,
                @NonNull final Supplier<T> supplier) {
            super(category, name, "%s");
            this.type = Objects.requireNonNull(type, "type");
            this.supplier = Objects.requireNonNull(supplier, "supplier");
        }

        private Config(
                @NonNull final String category,
                @NonNull final String name,
                @NonNull final String description,
                @NonNull final String unit,
                @NonNull final String format,
                @NonNull final Class<T> type,
                @NonNull final Supplier<T> supplier) {
            super(category, name, description, unit, format);
            this.type = Objects.requireNonNull(type, "type");
            this.supplier = Objects.requireNonNull(supplier, "supplier");
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public FunctionGauge.Config<T> withDescription(@NonNull final String description) {
            return new FunctionGauge.Config<>(
                    getCategory(), getName(), description, getUnit(), getFormat(), getType(), getSupplier());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public FunctionGauge.Config<T> withUnit(@NonNull final String unit) {
            return new FunctionGauge.Config<>(
                    getCategory(), getName(), getDescription(), unit, getFormat(), getType(), getSupplier());
        }

        /**
         * Sets the {@link Metric#getFormat() Metric.format} in fluent style.
         *
         * @param format
         * 		the format-string
         * @return a new configuration-object with updated {@code format}
         * @throws IllegalArgumentException
         * 		if {@code format} is {@code null} or consists only of whitespaces
         */
        @NonNull
        public FunctionGauge.Config<T> withFormat(@NonNull final String format) {
            return new FunctionGauge.Config<>(
                    getCategory(), getName(), getDescription(), getUnit(), format, getType(), getSupplier());
        }

        /**
         * Getter of the type of the returned values
         *
         * @return the type of the returned values
         */
        @NonNull
        public Class<T> getType() {
            return type;
        }

        /**
         * Getter of the {@code supplier}
         *
         * @return the {@code supplier}
         */
        @NonNull
        public Supplier<T> getSupplier() {
            return supplier;
        }

        /**
         * {@inheritDoc}
         */
        @SuppressWarnings("unchecked")
        @Override
        public Class<FunctionGauge<T>> getResultClass() {
            return (Class<FunctionGauge<T>>) (Class<?>) FunctionGauge.class;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        @NonNull
        public FunctionGauge<T> create(@NonNull final PlatformMetricsFactory factory) {
            return factory.createFunctionGauge(this);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .appendSuper(super.toString())
                    .append("type", type.getName())
                    .toString();
        }
    }
}
