/*
 * Copyright 2013-2016 Guardtime, Inc.
 *
 * This file is part of the Guardtime client SDK.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES, CONDITIONS, OR OTHER LICENSES OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 * "Guardtime" and "KSI" are trademarks or registered trademarks of
 * Guardtime, Inc., and no license to trademarks is granted; Guardtime
 * reserves and retains all trademark rights.
 */
package com.guardtime.ksi.service.tcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * This class holds a map of active KSI TCP signing transactions by ID. I helps to keep track which responses go together with which requests
 * Each request is added to this holder, and each response passes through it to check that there is a corresponding request waiting.
 */
class ActiveTransactionsHolder {

    private static final Logger logger = LoggerFactory.getLogger(ActiveTransactionsHolder.class);

    private static final ConcurrentMap<Long, KSITCPSigningTransaction> ACTIVE_TRANSACTIONS = new ConcurrentHashMap<Long, KSITCPSigningTransaction>();

    synchronized static void put(KSITCPSigningTransaction transaction) {
        ACTIVE_TRANSACTIONS.put(transaction.getCorrelationId(), transaction);
    }

    synchronized static void remove(KSITCPSigningTransaction transaction) {
        ACTIVE_TRANSACTIONS.remove(transaction.getCorrelationId());
    }

    synchronized static void responseReceived(KSITCPSigningTransaction transaction) {
        long correlationId = transaction.getCorrelationId();
        if (ACTIVE_TRANSACTIONS.get(correlationId) != null) {
            ACTIVE_TRANSACTIONS.get(correlationId).responseReceived(transaction.getResponse());
        } else {
            logger.info("Received TCP signing response with id {}, but did not find corresponding request. It might have timed out.", correlationId);
        }

    }
}
