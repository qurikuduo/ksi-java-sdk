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

package com.guardtime.ksi.unisignature.verifier.rules;

import com.guardtime.ksi.exceptions.KSIException;
import com.guardtime.ksi.publication.PublicationData;
import com.guardtime.ksi.unisignature.verifier.VerificationContext;
import com.guardtime.ksi.unisignature.verifier.VerificationErrorCode;
import com.guardtime.ksi.unisignature.verifier.VerificationResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * This rule is used to verify that calendar authentication record publication time equals to calendar hash chain
 * publication time. If calendar authentication record is missing then status {@link VerificationResultCode#OK} is
 * returned.
 */
public class CalendarAuthenticationRecordAggregationTimeRule extends BaseRule {

    private static final Logger LOGGER = LoggerFactory.getLogger(CalendarAuthenticationRecordAggregationTimeRule.class);

    public VerificationResultCode verifySignature(VerificationContext context) throws KSIException {
        if (context.getCalendarAuthenticationRecord() == null) {
            return VerificationResultCode.OK;
        }

        Date calendarHashChainPublicationDate = context.getCalendarHashChain().getPublicationTime();
        PublicationData authenticationRecordPublicationData = context.getCalendarAuthenticationRecord().getPublicationData();
        if (calendarHashChainPublicationDate.equals(authenticationRecordPublicationData.getPublicationTime())) {
            return VerificationResultCode.OK;
        }
        LOGGER.info("Invalid calendar authentication record publication data. Expected '{}', found '{}'", calendarHashChainPublicationDate, authenticationRecordPublicationData.getPublicationTime());
        return VerificationResultCode.FAIL;
    }

    public VerificationErrorCode getErrorCode() {
        return VerificationErrorCode.INT_06;
    }
}
