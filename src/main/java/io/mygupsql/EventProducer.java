/* **
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Copyright 2020, Miguel Arregui a.k.a. marregui
 */

package io.mygupsql;

/**
 * Marking interface.
 * <p>
 * Implementors produce events defined by the functional signature declared by
 * {@link EventConsumer}. An event can be conceptually thought of as a triplet:
 * <ul>
 * <li>source, an instance of a class that implements this interface.</li>
 * <li>type, an enum declared by the event producer, defines the kind of event
 * being produced.</li>
 * <li>data, an instance of some particular class that carries the event
 * data.</li>
 * </ul>
 *
 * @param <EventType> enum declared by the event producer
 */
public interface EventProducer<EventType extends Enum<?>> {

    // intentionally left empty
}
