/********************************************************************************
 * Copyright (c) 2020 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.famoco.se.plugin

import com.famoco.secommunication.ALPARProtocol
import com.famoco.secommunication.SmartcardReader
import org.eclipse.keyple.core.plugin.AbstractLocalReader
import org.eclipse.keyple.core.service.util.ContactCardCommonProtocols
import org.eclipse.keyple.core.util.ByteArrayUtil
import org.eclipse.keyple.famoco.se.plugin.AndroidFamocoPlugin.Companion.PLUGIN_NAME
import org.eclipse.keyple.famoco.se.plugin.AndroidFamocoReader.Companion.READER_NAME
import timber.log.Timber

internal object AndroidFamocoReaderImpl : AbstractLocalReader(PLUGIN_NAME, READER_NAME), AndroidFamocoReader {

    private val parameters: MutableMap<String, String> = HashMap()

    private val mSmarcardReader: SmartcardReader = SmartcardReader.getInstance()
    private var poweredOn = false
    private var atr: ByteArray? = null

    init {
        Timber.i("Initialize Famoco reader: $READER_NAME")
        SmartcardReader.setDebuggingEnabled(true)
        mSmarcardReader.openReader(115200)
        Timber.d("firmwareVersion = ${mSmarcardReader.firmwareVersion}")
        mSmarcardReader.isAutoNegotiate = true
    }

    override fun transmitApdu(apduIn: ByteArray?): ByteArray {
        Timber.d("Data Length to be sent to tag : ${apduIn?.size}")
        Timber.d("Data In : ${ByteArrayUtil.toHex(apduIn)}")
        val apduOut = mSmarcardReader.sendApdu(apduIn)
        Timber.d("Data Out : ${ByteArrayUtil.toHex(apduOut)}")
        return apduOut
    }

    override fun getATR(): ByteArray? {
        Timber.d("getATR()")
        Timber.d("ATR = ${ByteArrayUtil.toHex(atr)}")
        return atr
    }

    override fun openPhysicalChannel() {
        Timber.d("openPhysicalChannel()")
        atr = mSmarcardReader.powerOn()
        Timber.d("ATR = ${ByteArrayUtil.toHex(atr)}")
        mSmarcardReader.setClockCard(ALPARProtocol.PARAM_CLOCK_FREQUENCY_3_68MHz)
        poweredOn = true
    }

    override fun isPhysicalChannelOpen(): Boolean {
        Timber.d("isPhysicalChannelOpen()")
        return poweredOn
    }

    override fun checkCardPresence(): Boolean {
        Timber.d("checkSePresence()")
        return isCardPresent
    }

    override fun isCardPresent(): Boolean {
        // FIXED: Broken in famoco lib?
        return mSmarcardReader.isCardPresent
    }

    override fun closePhysicalChannel() {
        Timber.d("closePhysicalChannel()")
        mSmarcardReader.powerOff()
        poweredOn = false
    }

    override fun isContactless(): Boolean {
        return false
    }

    override fun isCurrentProtocol(readerProtocolName: String?): Boolean {
        return readerProtocolName == ContactCardCommonProtocols.ISO_7816_3.name
    }

    override fun deactivateReaderProtocol(readerProtocolName: String?) {
        // Do nothing
    }

    override fun activateReaderProtocol(readerProtocolName: String?) {
        // Do nothing
    }
}
