package com.example.voyeurism.chromecast

import android.annotation.SuppressLint
import com.example.voyeurism.activitys.DetailsActivity
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManagerListener

class CastManagement {

    inner class SessionManagerListenerImpl : SessionManagerListener<CastSession> {
        override fun onSessionEnding(p0: CastSession) {
            TODO("Not yet implemented")
        }
        override fun onSessionResumeFailed(p0: CastSession, p1: Int) {
            TODO("Not yet implemented")
        }
        override fun onSessionResuming(p0: CastSession, p1: String) {
            TODO("Not yet implemented")
        }
        override fun onSessionStartFailed(p0: CastSession, p1: Int) {
            TODO("Not yet implemented")
        }
        @SuppressLint("SetTextI18n")
        override fun onSessionStarting(p0: CastSession) {
            DetailsActivity().textViewPlayer.text = "Chromecast is loading!"
        }
        override fun onSessionSuspended(p0: CastSession, p1: Int) {
            TODO("Not yet implemented")
        }
        @SuppressLint("SetTextI18n")
        override fun onSessionStarted(p0: CastSession, p1: String) {
            //invalidateOptionsMenu(DetailActivity())
            DetailsActivity().exoPlayer.pause()
            DetailsActivity().textViewPlayer.text = "Chromecast is Connected!"
        }
        override fun onSessionResumed(p0: CastSession, p1: Boolean) {
            //invalidateOptionsMenu(DetailActivity())
            //DetailActivity().exoPlayer.pause()
        }
        override fun onSessionEnded(p0: CastSession, p1: Int) {
            DetailsActivity().finish()
            DetailsActivity().exoPlayer.play()
        }
    }

}