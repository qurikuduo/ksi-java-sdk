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

package com.guardtime.ksi.trust;

import org.bouncycastle.util.Store;

import java.security.cert.X509Certificate;

/**
 * Represents the PKI based trust store containing certificates trusted by KSI
 */
public interface PKITrustStore {

    /**
     * @param certificate
     *         instance of PKI X.509 certificate. not null.
     * @param certStore
     *         additional certificates to be used to check if certificate chain is trusted or not.
     * @return returns true if certificate is trusted.
     */
    boolean isTrusted(X509Certificate certificate, Store certStore) throws CryptoException;

}
