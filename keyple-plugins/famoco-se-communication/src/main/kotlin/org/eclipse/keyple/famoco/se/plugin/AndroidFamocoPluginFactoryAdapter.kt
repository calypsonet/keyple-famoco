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

import org.eclipse.keyple.core.common.CommonsApiProperties
import org.eclipse.keyple.core.plugin.PluginApiProperties
import org.eclipse.keyple.core.plugin.spi.PluginFactorySpi
import org.eclipse.keyple.core.plugin.spi.PluginSpi

class AndroidFamocoPluginFactoryAdapter : AndroidFamocoPluginFactory, PluginFactorySpi {

    override fun getPluginName(): String = AndroidFamocoPlugin.PLUGIN_NAME

    override fun getPlugin(): PluginSpi = AndroidFamocoPluginAdapter()

    override fun getCommonsApiVersion(): String = CommonsApiProperties.VERSION

    override fun getPluginApiVersion(): String = PluginApiProperties.VERSION
}
