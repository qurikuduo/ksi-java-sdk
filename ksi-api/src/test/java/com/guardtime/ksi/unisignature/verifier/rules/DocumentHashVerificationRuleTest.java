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
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.DataHasher;
import com.guardtime.ksi.hashing.HashAlgorithm;
import com.guardtime.ksi.unisignature.verifier.RuleResult;
import com.guardtime.ksi.unisignature.verifier.VerificationErrorCode;
import com.guardtime.ksi.unisignature.verifier.VerificationResultCode;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DocumentHashVerificationRuleTest extends AbstractRuleTest {

    private Rule rule = new DocumentHashVerificationRule();

    @Test
    public void testSignatureVerificationWithoutDocumentHashReturnsOkStatus_Ok() throws Exception {
        RuleResult result = rule.verify(build(TestUtil.loadSignature("ok-sig-2014-06-2.ksig")));
        Assert.assertNotNull(result);
        Assert.assertEquals(result.getResultCode(), VerificationResultCode.OK);
        Assert.assertNull(result.getErrorCode());
    }

    @Test
    public void testSignatureVerificationWithValidDocumentHashReturnsOkStatus_Ok() throws Exception {
        DataHasher hasher = new DataHasher(HashAlgorithm.SHA2_256);
        hasher.addData(TestUtil.loadFile("infile"));
        Assert.assertEquals(rule.verify(build(TestUtil.loadSignature("ok-sig-2014-06-2.ksig"), hasher.getHash())).getResultCode(), VerificationResultCode.OK);
    }

    @Test
    public void testSignatureVerificationWithInvalidDocumentHashReturnsOkStatus_Ok() throws Exception {
        RuleResult result = rule.verify(build(TestUtil.loadSignature("ok-sig-2014-06-2.ksig"), new DataHash(HashAlgorithm.SHA2_256, new byte[32])));
        Assert.assertEquals(result.getResultCode(), VerificationResultCode.FAIL);
        Assert.assertEquals(result.getErrorCode(), VerificationErrorCode.GEN_1);
    }

    @Test
    public void testSignatureVerificationWithInvalidRfc3161OutputHashReturnsFailStatus_Ok() throws Exception {
        RuleResult result = rule.verify(build(TestUtil.loadSignature("signature/signature-with-rfc3161-record-ok.ksig"), new DataHash(HashAlgorithm.SHA2_256, new byte[32])));
        Assert.assertEquals(result.getResultCode(), VerificationResultCode.FAIL);
        Assert.assertEquals(result.getErrorCode(), VerificationErrorCode.GEN_1);
    }

}