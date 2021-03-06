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

package io.mygupsql.backend;

import java.io.Closeable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Periodic database connections validity checker.
 * <p>
 * A connection is not valid when it was open, but became unresponsive perhaps
 * to a server side failure, or network latency.
 * <p>
 * Connections are provided by a supplier. Two threads are in charge for
 * periodically checking the validity of the supplied database connections.
 * <b>Only</b> connections that are <b>open</b> participate in the validity
 * check. When connections are detected to be invalid they are closed and
 * collected into a set which is given back as a callback to a consumer.
 * Supplier and consumer references are provided to the constructor of this
 * class.
 * 
 * @see Conn#isValid()
 */
public class ConnsChecker implements Closeable {

    private static final int PERIOD_SECS = 60;
    private static final int NUM_THREADS = 2;
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnsChecker.class);

    private final Supplier<List<Conn>> connsSupplier;
    private final Consumer<Set<Conn>> lostConnsConsumer;
    private final AtomicBoolean isChecking;
    private ScheduledExecutorService scheduler;

    /**
     * Constructor.
     * 
     * @param connsSupplier     provides a list of connections to be periodically
     *                          checked. Only open connections participate in the
     *                          validity check
     * @param lostConnsConsumer in the presence of invalid connections will get at
     *                          least one callback carrying a set containing lost
     *                          connections
     */
    public ConnsChecker(Supplier<List<Conn>> connsSupplier,
        Consumer<Set<Conn>> lostConnsConsumer) {
        this.connsSupplier = connsSupplier;
        this.lostConnsConsumer = lostConnsConsumer;
        this.isChecking = new AtomicBoolean();
    }

    /**
     * @return true if the checker was started
     */
    public synchronized boolean isRunning() {
        return scheduler != null && !scheduler.isTerminated();
    }

    /**
     * Starts the periodic connectivity check.
     */
    public synchronized void start() {
        if (scheduler != null) {
            throw new IllegalStateException("already started");
        }
        scheduler = Executors.newScheduledThreadPool(NUM_THREADS);
        scheduler.scheduleAtFixedRate(this::dbConnsValidityCheck, PERIOD_SECS, PERIOD_SECS, TimeUnit.SECONDS);
        LOGGER.info("Check every {} secs", PERIOD_SECS);
    }

    private final List<ScheduledFuture<?>> invalidConnsFutures() {
        return connsSupplier.get().stream().filter(Conn::isOpen).map(conn -> scheduler.schedule(() -> {
            return !conn.isValid() ? conn : null; // might block for up DBConnection.VALID_CHECK_TIMEOUT_SECS
        }, 0, TimeUnit.SECONDS)).collect(Collectors.toList());
    }

    private void dbConnsValidityCheck() {
        if (!isChecking.compareAndSet(false, true)) {
            return;
        }
        try {
            List<ScheduledFuture<?>> invalidConns = invalidConnsFutures();
            while (invalidConns.size() > 0) {
                Set<Conn> invalidSet = new HashSet<>();
                for (Iterator<ScheduledFuture<?>> it = invalidConns.iterator(); it.hasNext();) {
                    ScheduledFuture<?> invalidConnFuture = it.next();
                    if (invalidConnFuture.isDone()) {
                        try {
                            Conn conn = (Conn) invalidConnFuture.get();
                            if (conn != null) {
                                invalidSet.add(conn);
                            }
                        }
                        catch (Exception unexpected) {
                            LOGGER.error("Unexpected turn of events", unexpected);
                        }
                        finally {
                            it.remove();
                        }
                    }
                    else if (invalidConnFuture.isCancelled()) {
                        it.remove();
                    }
                }
                if (invalidSet.size() > 0) {
                    // notify the consumer as invalid connections are found
                    // rather than wait for all connections to be checked
                    lostConnsConsumer.accept(invalidSet);
                }
            }
        }
        finally {
            isChecking.set(false);
        }
    }

    /**
     * Terminates the periodic checks.
     */
    @Override
    public synchronized void close() {
        if (scheduler == null) {
            throw new IllegalStateException("not started");
        }
        scheduler.shutdownNow();
        try {
            // symbolic wait
            scheduler.awaitTermination(200L, TimeUnit.MILLISECONDS);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        finally {
            scheduler = null;
            isChecking.set(false);
            LOGGER.info("Connectivity check stopped");
        }
    }
}
