package org.eclipse.keyple.famoco.se.plugin

/**
 *
 *  created on 30/03/2021
 *
 *  @author youssefamrani
 */

object AndroidFamocoPluginFactoryProvider {

     fun getFactory(): AndroidFamocoPluginFactory = AndroidFamocoPluginFactoryAdapter()
}