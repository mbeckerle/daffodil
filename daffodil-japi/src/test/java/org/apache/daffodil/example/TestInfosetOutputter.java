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

package org.apache.daffodil.example;

import java.nio.file.Path;
import java.util.ArrayList;

import org.apache.daffodil.runtime1.api.*;
import scala.Enumeration;


public class TestInfosetOutputter extends BlobMethodsImpl implements InfosetOutputter {

    public ArrayList<TestInfosetEvent> events;

    TestInfosetOutputter() {
        events = new ArrayList<>();
    }

    @Override
    public void reset() {
        events = new ArrayList<>();
    }

    @Override
    public void startDocument() {
        events.add(TestInfosetEvent.startDocument());
    }

    @Override
    public void endDocument() {
        events.add(TestInfosetEvent.endDocument());
    }

    @Override
    public void startSimple(InfosetSimpleElement diSimple) {
        events.add(
            TestInfosetEvent.startSimple(
                diSimple.name(),
                diSimple.namespace().toString(),
                diSimple.dataValueAsString(),
                diSimple.metadata().isNillable() ? diSimple.isNilled() : null));
    }

    @Override
    public void endSimple(InfosetSimpleElement diSimple) {
        events.add(
            TestInfosetEvent.endSimple(
                diSimple.name(),
                diSimple.namespace().toString()));
    }

    @Override
    public void startComplex(InfosetComplexElement diComplex) {
        events.add(
            TestInfosetEvent.startComplex(
                diComplex.name(),
                diComplex.namespace().toString(),
                diComplex.metadata().isNillable() ? diComplex.isNilled() : null));
    }

    @Override
    public void endComplex(InfosetComplexElement diComplex) {
        events.add(
            TestInfosetEvent.endComplex(
                diComplex.name(),
                diComplex.namespace().toString()));
    }

    @Override
    public void startArray(InfosetArray diArray) {
    }

    @Override
    public void endArray(InfosetArray diArray) {
    }

    @Override
    public Enumeration.Value getStatus() {
        return null;
    }
}
