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

import org.eclipse.keyple.core.plugin.spi.ObservablePluginSpi
import org.eclipse.keyple.core.plugin.spi.reader.ReaderSpi
import java.util.concurrent.ConcurrentHashMap

internal class AndroidFamocoPluginAdapter :
    ObservablePluginSpi,
    AndroidFamocoPlugin {

    companion object {
        private const val MONITORING_CYCLE_DURATION_MS = 1000
    }

    private lateinit var seReaders: ConcurrentHashMap<String, ReaderSpi>

    override fun searchAvailableReaders(): MutableSet<ReaderSpi> {

        seReaders = ConcurrentHashMap<String, ReaderSpi>()

        val sam = AndroidFamocoReaderAdapter()
        seReaders[sam.name] = sam

        return seReaders.map {
            it.value
        }.toMutableSet()
    }

    override fun searchReader(readerName: String?): ReaderSpi? {
        return if (seReaders.containsKey(readerName)) {
            seReaders[readerName]!!
        }
        else{
            null
        }
    }

    override fun searchAvailableReadersNames(): MutableSet<String> {
        return seReaders.map {
            it.key
        }.toMutableSet()
    }

    override fun getMonitoringCycleDuration(): Int {
        return MONITORING_CYCLE_DURATION_MS
    }

    override fun getName(): String = AndroidFamocoPlugin.PLUGIN_NAME

    override fun unregister() {
        //Do nothing
    }
}
