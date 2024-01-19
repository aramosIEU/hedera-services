/*
 * Copyright (C) 2022-2024 Hedera Hashgraph, LLC
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

package com.swirlds.platform.system.status;

import com.swirlds.common.UniqueId;

/**
 * The status of the Node
 */
public enum NodeStatus implements UniqueId {
    /**
     * The node is up (platform is ACTIVE and grpc servers are running)
     */
    UP(1),
    /**
     * The node is down (grpc servers are down)
     */
    DOWN(2);
    /**

    /**
     * Unique ID of the enum value
     */
    private final int id;

    /**
     * Constructs an enum instance
     *
     * @param id unique ID of the instance
     */
    NodeStatus(final int id) {
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }
}
