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

package com.guardtime.ksi.integration;

import com.guardtime.ksi.TestUtil;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;
import com.guardtime.ksi.publication.PublicationData;
import com.guardtime.ksi.service.client.KSIExtenderClient;
import com.guardtime.ksi.unisignature.*;
import com.guardtime.ksi.unisignature.verifier.VerificationErrorCode;
import com.guardtime.ksi.unisignature.verifier.VerificationResult;
import com.guardtime.ksi.unisignature.verifier.policies.UserProvidedPublicationBasedVerificationPolicy;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Date;

import static com.guardtime.ksi.TestUtil.loadSignature;

public class UserProvidedPublicationPolicyIntegrationTest extends AbstractCommonIntegrationTest {

    private final UserProvidedPublicationBasedVerificationPolicy policy = new UserProvidedPublicationBasedVerificationPolicy();
    private static final String PUBLICATION_STRING_2014_09_15 = "AAAAAA-CUCYWA-AAOBM6-PNYLRK-EPI3VG-2PJGCF-Y5QHV3-XURLI2-GRFBK4-VHBED2-Q37QIB-UE3ENA";

    @Test(groups = TEST_GROUP_INTEGRATION)
    public void testVerifySignatureWithNoPublicationRecordinExtendingAllowed_VerificationReturnsOk() throws Exception {
        KSISignature signature = TestUtil.loadSignature(SIGNATURE_2014_06_02);
        PublicationData publicationData = new PublicationData(PUBLICATION_STRING_2014_09_15);
        VerificationResult result = verify(ksi, simpleHttpClient, signature, policy, publicationData, true);
        Assert.assertTrue(result.isOk());
    }


    @Test(groups = TEST_GROUP_INTEGRATION)
    public void testVerifySignatureWithNoPublicationRecordinExtendingNotAllowed_VerificationReturnsGen2() throws Exception {
        KSISignature signature = TestUtil.loadSignature(SIGNATURE_2014_06_02);
        PublicationData publicationData = new PublicationData(PUBLICATION_STRING_2014_09_15);
        VerificationResult result = verify(ksi, simpleHttpClient, signature, policy, publicationData, false);
        Assert.assertFalse(result.isOk());
        Assert.assertEquals(result.getErrorCode(), VerificationErrorCode.GEN_2);
    }

    @Test(groups = TEST_GROUP_INTEGRATION)
    public void testVerifySignatureWithCorrectData_VerificationReturnsOk() throws Exception {
        KSISignature signature = TestUtil.loadSignature(EXTENDED_SIGNATURE_2014_06_02);
        PublicationData publicationData = signature.getPublicationRecord().getPublicationData();
        VerificationResult result = verify(ksi, simpleHttpClient, signature, policy, publicationData, true);
        Assert.assertTrue(result.isOk());
    }

    @Test(groups = TEST_GROUP_INTEGRATION)
    public void testVerifySignatureWithSamePublicationTimeButDifferentHash_VerificationReturnsInt09() throws Exception {
        KSISignature signature = TestUtil.loadSignature(EXTENDED_SIGNATURE_2014_06_02);
        PublicationData publicationData = new PublicationData(signature.getPublicationTime(), new DataHash(HashAlgorithm.SHA2_256, new byte[32]));
        VerificationResult result = verify(ksi, simpleHttpClient, signature, policy, publicationData, false);
        Assert.assertFalse(result.isOk());
        Assert.assertEquals(result.getErrorCode(), VerificationErrorCode.PUB_04);
    }

    @Test(groups = TEST_GROUP_INTEGRATION)
    public void testVerifySignatureUsingPublicationCreatedBeforeSignature_VerificationReturnsGen2() throws Exception {
        KSISignature signature = TestUtil.loadSignature(EXTENDED_SIGNATURE_2014_06_02);
        PublicationData publicationData = new PublicationData(new Date(10000L), new DataHash(HashAlgorithm.SHA2_256, new byte[32]));
        VerificationResult result = verify(ksi, simpleHttpClient, signature, policy, publicationData, true);
        Assert.assertFalse(result.isOk());
        Assert.assertEquals(result.getErrorCode(), VerificationErrorCode.GEN_2);
    }

    @Test(groups = TEST_GROUP_INTEGRATION)
    public void testVerifySignatureUsingPublicationCreatedAfterSignatureExtendingNotAllowed_VerificationReturnsGen2() throws Exception {
        KSISignature signature = TestUtil.loadSignature(EXTENDED_SIGNATURE_2014_06_02);
        PublicationData publicationData = new PublicationData(new Date(), new DataHash(HashAlgorithm.SHA2_256, new byte[32]));
        VerificationResult result = verify(ksi, simpleHttpClient, signature, policy, publicationData, false);
        Assert.assertFalse(result.isOk());
        Assert.assertEquals(result.getErrorCode(), VerificationErrorCode.GEN_2);
    }

    @Test(groups = TEST_GROUP_INTEGRATION)
    public void testVerifySignatureUsingPublicationCreatedAfterSignatureAllowExtending_VerificationReturnsPub1() throws Exception {
        KSISignature signature = TestUtil.loadSignature(EXTENDED_SIGNATURE_2014_06_02);
        PublicationData publicationData = new PublicationData(new Date(1431648000000L), new DataHash(HashAlgorithm.SHA2_256, new byte[32]));
        VerificationResult result = verify(ksi, simpleHttpClient, signature, policy, publicationData, true);
        Assert.assertFalse(result.isOk());
        Assert.assertEquals(result.getErrorCode(), VerificationErrorCode.PUB_01);
    }

    @Test(groups = TEST_GROUP_INTEGRATION)
    public void testVerifySignatureUsingWrongCalChainResponse_VerificationResultPub2() throws Exception {
        KSIExtenderClient mockedExtenderClient = Mockito.mock(KSIExtenderClient.class);

        String responseFile = "pdu/extension/extension-response-v1-with-only-cal-chain-for-ExtendedSignature-NoPubRec.tlv";
        mockExtenderResponseCalendarHashCain(responseFile, mockedExtenderClient);

        KSISignature signature = TestUtil.loadSignature("publication-based-verification/ExtendedSignature-NoPubRec.ksig");
        PublicationData publicationData = new PublicationData("AAAAAA-CVZ2AQ-AAIVXJ-PLJDAG-JMMYUC-OTP2GA-ELBIDQ-OKDY3C-C3VEH2-AR35I2-OJUACP-GOGD6K");
        VerificationResult result = verify(ksi, mockedExtenderClient, signature, policy, publicationData, true);

        Assert.assertFalse(result.isOk());
        Assert.assertEquals(result.getErrorCode(), VerificationErrorCode.PUB_02);
    }

    @Test(groups = TEST_GROUP_INTEGRATION)
    public void testVerifySignatureUsingPublicationAndResponseFromAnotherCore_VerificationResultPub3() throws Exception {
        KSIExtenderClient mockedExtenderClient = Mockito.mock(KSIExtenderClient.class);
        String responseFile = "publication-based-verification/another-core-response-with-only-cal-chain.tlv";
        mockExtenderResponseCalendarHashCain(responseFile, mockedExtenderClient);

        KSISignature signature = TestUtil.loadSignature("publication-based-verification/correct-core-signature_2015-09-13_21-34-00.ksig");
        PublicationData publicationData = new PublicationData("AAAAAA-CV6Z3M-EAM2OL-PO3DU7-SQMPO6-KONYI3-HLRICR-6LUQS5-PTFAGI-CYSUP3-KSENF5-BKWK4G");

        VerificationResult result = verify(ksi, mockedExtenderClient, signature, policy, publicationData, true);
        Assert.assertFalse(result.isOk());
        Assert.assertEquals(result.getErrorCode(), VerificationErrorCode.PUB_03);
    }

    @Test(groups = TEST_GROUP_INTEGRATION)
    public void testVerifySignatureUsingDifferentPublication_VerificationReturnsOk() throws Exception {
        KSISignature signature = TestUtil.loadSignature(EXTENDED_SIGNATURE_2014_06_02);
        PublicationData publicationData = new PublicationData("AAAAAA-CTRQ5E-QAISH7-QFMEEV-CYN3XW-FWO33A-PXL4PD-4KS3Y5-FY2SYO-LIRKZK-HS25IW-5FIBTP");
        VerificationResult result = verify(ksi, simpleHttpClient, signature, policy, publicationData, true);
        Assert.assertTrue(result.isOk());
    }

    @Test(groups = TEST_GROUP_INTEGRATION)
    public void testVerifySignatureWithWrongPublicationReferenceInPublicationRecord() throws Exception {
        KSISignature signature = loadSignature("publication-based-verification/ok-sig-2014-06-2-extended-wrong-publication-reference.ksig");
        PublicationData publicationData = new PublicationData(PUBLICATION_STRING_2014_09_15);
        VerificationResult result = verify(ksi, simpleHttpClient, signature, policy, publicationData, true);
        Assert.assertTrue(result.isOk());
    }

    @Test(groups = TEST_GROUP_INTEGRATION)
    public void testVerifySignatureWithNoPublicationReferenceInPublicationRecord() throws Exception {
        KSISignature signature = loadSignature("publication-based-verification/ok-sig-2014-06-2-extended-no-publication-reference.ksig");
        PublicationData publicationData = new PublicationData(PUBLICATION_STRING_2014_09_15);
        VerificationResult result = verify(ksi, simpleHttpClient, signature, policy, publicationData, true);
        Assert.assertTrue(result.isOk());
    }
}
