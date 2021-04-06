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

import com.famoco.secommunication.SmartcardReader
import io.mockk.MockKAnnotations
import io.mockk.mockk
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

internal class AndroidFamocoReaderAdapterTest {

    companion object {
        internal const val PLUGIN_NAME = "AndroidFamocoPlugin"
        internal const val PO_AID = "A000000291A000000191"
        internal const val PO_AID_RESPONSE = "6F25840BA000000291A00000019102A516BF0C13C70800000000C0E11FA653070A3C230C1410019000"
    }

    lateinit var nativeReader: SmartcardReader
    lateinit var reader: AndroidFamocoReaderAdapter

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        // default reader connected with secure element with poAid
        nativeReader = mockReader()

        // instantiate reader with nativeReader
        reader = AndroidFamocoReaderAdapter()
    }

    @After
    fun tearDown() {
        unmockkAll()
    }
    /*
     * TEST READERS
     */

    @Test
    fun getInstance() {
        Assert.assertNotNull(reader)
    }

    @Test
    fun getName() {
        Assert.assertEquals(getNativeReaderName(), reader.name)
    }

    @Test
    fun isContactless() {
        Assert.assertEquals(true, reader.isContactless)
    }

    @Test
    fun isSEPresent() {
        Assert.assertEquals(true, reader.checkCardPresence())
    }

    fun getNativeReaderName(): String {
        return reader.name
    }

    fun mockReader(): SmartcardReader {
        val nativeReader = mockk<SmartcardReader>()
        return nativeReader
    }
}
