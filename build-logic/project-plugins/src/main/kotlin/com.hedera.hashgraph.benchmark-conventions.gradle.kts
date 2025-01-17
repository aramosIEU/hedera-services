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

plugins { id("me.champeau.jmh") }

jmh {
    jmhVersion = "1.36"
    includeTests = false
}

tasks.jmh { outputs.upToDateWhen { false } }

tasks.jmhJar { manifest { attributes(mapOf("Multi-Release" to true)) } }

configurations {
    // Disable module Jar patching for the JMH runtime classpath.
    // The way the JMH plugin interacts with this in the 'jmhJar' task triggers this Gradle issue:
    // https://github.com/gradle/gradle/issues/27372
    // And since 'jmhJar' builds a fat jar, module information is not needed here anyway.
    jmhRuntimeClasspath {
        attributes { attribute(Attribute.of("javaModule", Boolean::class.javaObjectType), false) }
    }
}

tasks.assemble {
    // 'assemble' compiles all sources, including 'jmh'
    dependsOn(tasks.named("jmhClasses"))
}
