package de.ph1b.audiobook.playback

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import de.ph1b.audiobook.injection.PerService
import de.ph1b.audiobook.injection.PrefKeys
import de.ph1b.audiobook.persistence.BookRepository
import de.ph1b.audiobook.persistence.pref.Pref
import de.ph1b.audiobook.playback.utils.ChangeNotifier
import javax.inject.Inject
import javax.inject.Named

/**
 * Holds the current connection status and notifies about
 * changes upon connection.
 */
@PerService class AndroidAutoConnection @Inject constructor(
    private val changeNotifier: ChangeNotifier,
    private val repo: BookRepository,
    @Named(PrefKeys.CURRENT_BOOK)
    private val currentBookIdPref: Pref<Long>
) {

  var connected = false
    private set

  private val receiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
      when (intent?.getStringExtra("media_connection_status")) {
        "media_connected" -> connected = true
        "media_disconnected" -> connected = false
      }

      if (connected) {
        // display the current book but don't play it
        repo.bookById(currentBookIdPref.value)?.let {
          changeNotifier.notify(ChangeNotifier.Type.METADATA, it, connected)
        }
      }
    }
  }

  fun register(context: Context) {
    context.registerReceiver(receiver, IntentFilter("com.google.android.gms.car.media.STATUS"))
  }

  fun unregister(context: Context) {
    context.unregisterReceiver(receiver)
  }
}
