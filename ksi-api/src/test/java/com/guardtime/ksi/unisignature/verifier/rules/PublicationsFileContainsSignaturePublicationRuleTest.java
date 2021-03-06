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

import com.guardtime.ksi.TestUtil;
import com.guardtime.ksi.exceptions.KSIException;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;
import com.guardtime.ksi.publication.PublicationData;
import com.guardtime.ksi.unisignature.inmemory.InMemorySignaturePublicationRecord;
import com.guardtime.ksi.unisignature.verifier.RuleResult;
import com.guardtime.ksi.unisignature.verifier.VerificationContext;
import com.guardtime.ksi.unisignature.verifier.VerificationErrorCode;
import com.guardtime.ksi.unisignature.verifier.VerificationResultCode;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Date;

public class PublicationsFileContainsSignaturePublicationRuleTest extends AbstractRuleTest {

    private PublicationsFileContainsSignaturePublicationRule rule = new PublicationsFileContainsSignaturePublicationRule();

    private VerificationContext context;

    @BeforeMethod
    public void setUp() throws Exception {
        this.context = Mockito.mock(VerificationContext.class);
        Mockito.when(context.getPublicationsFile()).thenReturn(TestUtil.loadPublicationsFile("publications.tlv"));
    }

    @Test
    public void testPublicationFileContainsSignaturePublication_Ok() throws Exception {
        RuleResult result = rule.verify(build(TestUtil.loadSignature("ok-sig-2014-06-2-extended.ksig"), TestUtil.loadPublicationsFile("publications.tlv")));
        Assert.assertEquals(result.getResultCode(), VerificationResultCode.OK);
    }

    @Test
    public void testPublicationFileDoesNotContainPublication() throws Exception {
        mockPublication(new Date(999999999999999999L));
        RuleResult result = rule.verify(context);
        Assert.assertEquals(result.getResultCode(), VerificationResultCode.NA);
    }

    @Test
    public void testPublicationFileContainsPublicationWithDifferentPublicationTime() throws Exception {
        mockPublication(new Date(1000L));
        RuleResult result = rule.verify(context);
        Assert.assertEquals(result.getResultCode(), VerificationResultCode.NA);
    }

    @Test
    public void testPublicationFileContainPublicationWithDifferentHash() throws Exception {
        mockPublication(new Date(1208217600000L));
        RuleResult result = rule.verify(context);
        Assert.assertEquals(result.getResultCode(), VerificationResultCode.FAIL);
        Assert.assertEquals(result.getErrorCode(), VerificationErrorCode.PUB_05);
    }

    private void mockPublication(Date publicationTime) throws KSIException {
        InMemorySignaturePublicationRecord mockedPublicationRecord = Mockito.mock(InMemorySignaturePublicationRecord.class);
        Mockito.when(mockedPublicationRecord.getPublicationData()).thenReturn(new PublicationData(publicationTime, new DataHash(HashAlgorithm.SHA2_256, new byte[32])));
        Mockito.when(context.getPublicationRecord()).thenReturn(mockedPublicationRecord);
        Mockito.when(mockedPublicationRecord.getPublicationTime()).thenReturn(publicationTime);
    }

}
