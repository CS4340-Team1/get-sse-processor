/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.it.processor.getsse;

import org.apache.nifi.processor.Relationship;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;


public class GetSSETest {

    private TestRunner testRunner;

    @BeforeEach
    public void init() {
        testRunner = TestRunners.newTestRunner(GetSSE.class);
    }

    @Test
    public void testProcessor() {
        Path URLTest = Paths.get("src/main/java/com/it/processor/getsse/GetSSE.java");

        testRunner.setProperty(GetSSE.URL, "https://www.google.com");

        testRunner.enqueue(URLTest.toString());
        testRunner.run();

        Relationship expectedRel;
        expectedRel = GetSSE.RESPONSE;
        testRunner.assertTransferCount(expectedRel, 1);
        expectedRel = GetSSE.FAILURE;
        testRunner.assertTransferCount(expectedRel, 0);

    }

}

