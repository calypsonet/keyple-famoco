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
package org.cna.keyple.famoco.example.activity

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.drawerLayout
import kotlinx.android.synthetic.main.activity_main.eventRecyclerView
import kotlinx.android.synthetic.main.activity_main.navigationView
import kotlinx.android.synthetic.main.activity_main.toolbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.cna.keyple.famoco.example.R
import org.cna.keyple.famoco.example.adapter.EventAdapter
import org.cna.keyple.famoco.example.model.ChoiceEventModel
import org.cna.keyple.famoco.example.model.EventModel
import org.eclipse.keyple.calypso.command.sam.SamRevision
import org.eclipse.keyple.calypso.transaction.CalypsoSam
import org.eclipse.keyple.calypso.transaction.PoSecuritySettings
import org.eclipse.keyple.calypso.transaction.PoTransaction
import org.eclipse.keyple.calypso.transaction.SamSelection
import org.eclipse.keyple.calypso.transaction.SamSelector
import org.eclipse.keyple.core.card.selection.CardResource
import org.eclipse.keyple.core.card.selection.CardSelectionsService
import org.eclipse.keyple.core.card.selection.MultiSelectionProcessing
import org.eclipse.keyple.core.service.Reader
import org.eclipse.keyple.core.service.event.ObservableReader
import org.eclipse.keyple.core.service.event.ReaderEvent
import org.eclipse.keyple.core.service.exception.KeypleReaderException
import org.eclipse.keyple.core.service.util.ContactCardCommonProtocols
import timber.log.Timber

abstract class AbstractExampleActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, ObservableReader.ReaderObserver {

    /**
     * Use to modify event update behaviour reguarding current use case execution
     */
    interface UseCase {
        fun onEventUpdate(event: ReaderEvent?)
    }

    /**
     * Variables for event window
     */
    private lateinit var adapter: RecyclerView.Adapter<*>
    private lateinit var layoutManager: RecyclerView.LayoutManager
    protected val events = arrayListOf<EventModel>()

    protected var useCase: UseCase? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initContentView()

        /**
         * Init recycler view
         */
        adapter = EventAdapter(events)
        layoutManager = LinearLayoutManager(this)
        eventRecyclerView.layoutManager = layoutManager
        eventRecyclerView.adapter = adapter

        /**
         * Init menu
         */
        navigationView.setNavigationItemSelectedListener(this)
        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_navigation_drawer, R.string.close_navigation_drawer)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        initReaders()

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    protected fun initActionBar(toolbar: Toolbar, title: String, subtitle: String) {
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.title = title
        actionBar?.subtitle = subtitle
    }

    protected fun showAlertDialog(t: Throwable) {
        val builder = AlertDialog.Builder(this@AbstractExampleActivity)
        builder.setTitle(R.string.alert_dialog_title)
        builder.setMessage(getString(R.string.alert_dialog_message, t.message))
        val dialog = builder.create()
        dialog.show()
    }

    protected fun clearEvents() {
        events.clear()
        adapter.notifyDataSetChanged()
    }

    protected fun addHeaderEvent(message: String) {
        events.add(EventModel(EventModel.TYPE_HEADER, message))
        updateList()
        Timber.d("Header: %s", message)
    }

    protected fun addActionEvent(message: String) {
        events.add(EventModel(EventModel.TYPE_ACTION, message))
        updateList()
        Timber.d("Action: %s", message)
    }

    protected fun addResultEvent(message: String) {
        events.add(EventModel(EventModel.TYPE_RESULT, message))
        updateList()
        Timber.d("Result: %s", message)
    }

    protected fun addChoiceEvent(title: String, choices: List<String>, callback: (choice: String) -> Unit) {
        events.add(ChoiceEventModel(title, choices, callback))
        updateList()
        Timber.d("Choice: %s: %s", title, choices.toString())
    }

    private fun updateList() {
        CoroutineScope(Dispatchers.Main).launch {
            adapter.notifyDataSetChanged()
            adapter.notifyItemInserted(events.lastIndex)
            eventRecyclerView.smoothScrollToPosition(events.size - 1)
        }
    }
    abstract fun initContentView()
    abstract fun initReaders()

    @Throws(KeypleReaderException::class, IllegalStateException::class)
    protected fun checkSamAndOpenChannel(samReader: Reader): CardResource<CalypsoSam> {
        /*
         * check the availability of the SAM doing a ATR based selection, open its physical and
         * logical channels and keep it open
         */
        val samSelection = CardSelectionsService(MultiSelectionProcessing.FIRST_MATCH)

        val samSelector = SamSelector.builder()
            .cardProtocol(ContactCardCommonProtocols.ISO_7816_3.name)
            .samRevision(SamRevision.C1)
            .build()

        samSelection.prepareSelection(SamSelection(samSelector))

        return try {
            if (samReader.isCardPresent) {
                val results = samSelection.processExplicitSelections(samReader)
                val calypsoSam = results.activeSmartCard as CalypsoSam
                CardResource<CalypsoSam>(samReader, calypsoSam)
            } else {
                addResultEvent("Error: Sam is not present in the reader")
                throw IllegalStateException("Sam is not present in the reader")
            }
        } catch (e: KeypleReaderException) {
            addResultEvent("Error: Reader exception ${e.message}")
            throw IllegalStateException("Reader exception: " + e.message)
        }
    }

    protected fun getSecuritySettings(samResource: CardResource<CalypsoSam>?): PoSecuritySettings? {

        // The default KIF values for personalization, loading and debiting
        val DEFAULT_KIF_PERSO = 0x21.toByte()
        val DEFAULT_KIF_LOAD = 0x27.toByte()
        val DEFAULT_KIF_DEBIT = 0x30.toByte()
        // The default key record number values for personalization, loading and debiting
        // The actual value should be adjusted.
        val DEFAULT_KEY_RECORD_NUMBER_PERSO = 0x01.toByte()
        val DEFAULT_KEY_RECORD_NUMBER_LOAD = 0x02.toByte()
        val DEFAULT_KEY_RECORD_NUMBER_DEBIT = 0x03.toByte()

        /* define the security parameters to provide when creating PoTransaction */
        return PoSecuritySettings.PoSecuritySettingsBuilder(samResource) //
            .sessionDefaultKif(PoTransaction.SessionSetting.AccessLevel.SESSION_LVL_PERSO, DEFAULT_KIF_PERSO) //
            .sessionDefaultKif(PoTransaction.SessionSetting.AccessLevel.SESSION_LVL_LOAD, DEFAULT_KIF_LOAD) //
            .sessionDefaultKif(PoTransaction.SessionSetting.AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KIF_DEBIT) //
            .sessionDefaultKeyRecordNumber(
                PoTransaction.SessionSetting.AccessLevel.SESSION_LVL_PERSO,
                DEFAULT_KEY_RECORD_NUMBER_PERSO
            ) //
            .sessionDefaultKeyRecordNumber(
                PoTransaction.SessionSetting.AccessLevel.SESSION_LVL_LOAD,
                DEFAULT_KEY_RECORD_NUMBER_LOAD
            ) //
            .sessionDefaultKeyRecordNumber(
                PoTransaction.SessionSetting.AccessLevel.SESSION_LVL_DEBIT,
                DEFAULT_KEY_RECORD_NUMBER_DEBIT
            )
            .build()
    }
}
