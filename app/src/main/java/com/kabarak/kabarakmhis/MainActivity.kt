package com.kabarak.kabarakmhis

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.fhir.sync.State
import com.kabarak.kabarakmhis.fhir.viewmodels.MainActivityViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    private val viewModel: MainActivityViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        observeSyncState()

        swipeRefreshLayout.setOnRefreshListener {
            Handler().postDelayed({
                swipeRefreshLayout.isRefreshing = false
            }, 4000)
            observeSyncState()
        }

    }

    override fun onStart() {
        super.onStart()

    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun observeSyncState() {
        lifecycleScope.launch {
            viewModel.pollState.collect {

                when(it){
                    is State.Started -> showToast("Sync: started")
                    is State.InProgress -> showToast("Sync: in progress with ${it.resourceType?.name}")
                    is State.Finished -> {
                        showToast("Sync: succeeded at ${it.result.timestamp}")
                        viewModel.updateLastSyncTimestamp()

                        Log.e("---- ", "here")

                    }
                    is State.Failed -> {
                        showToast("Sync: failed at ${it.result.timestamp}")
                        viewModel.updateLastSyncTimestamp()
                    }
                    else -> showToast("Sync: unknown state.")
                }

            }
        }
    }

    private fun showToast(message: String) {
//        Timber.i(message)
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}