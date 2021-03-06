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

package com.guardtime.ksi.unisignature.inmemory;

import com.guardtime.ksi.exceptions.KSIException;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.DataHasher;
import com.guardtime.ksi.hashing.HashAlgorithm;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVStructure;
import com.guardtime.ksi.unisignature.CalendarHashChainLink;

/**
 * Abstract class for left and right calendar hash chain links.  A calendar chain defines a computation performed
 * starting from the first hash step (a left or right link structure) and ending with the last one, in the order in
 * which the steps are given in the sequence.
 */
abstract class InMemoryCalendarHashChainLink extends TLVStructure implements CalendarHashChainLink {

    protected final DataHash dataHash;

    InMemoryCalendarHashChainLink(TLVElement rootElement) throws KSIException {
        super(rootElement);
        this.dataHash = rootElement.getDecodedDataHash();
    }

    public abstract DataHash calculateChainStep(DataHash previous) throws InvalidCalendarHashChainException;

    public abstract boolean isRightLink();

    protected DataHash calculateStep(byte[] imprintA, byte[] imprintB, HashAlgorithm algorithm) throws InvalidCalendarHashChainException {
        if (!algorithm.isImplemented()) {
            throw new InvalidCalendarHashChainException("Invalid calendar hash chain. Hash algorithm " +algorithm.getName() + " is not implemented");
        }
        DataHasher hasher = new DataHasher(algorithm);
        hasher.addData(imprintA);
        hasher.addData(imprintB);
        hasher.addData(new byte[]{(byte) 0xFF});
        return hasher.getHash();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        InMemoryCalendarHashChainLink that = (InMemoryCalendarHashChainLink) o;
        return dataHash.equals(that.dataHash);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + dataHash.hashCode();
        return result;
    }
}
