
package com.intellisoft.kabarakmhis.fhir.data

import android.content.Context
import androidx.work.WorkerParameters
import com.google.android.fhir.sync.DownloadWorkManager
import com.google.android.fhir.sync.FhirSyncWorker
import com.intellisoft.kabarakmhis.fhir.FhirApplication

class FhirPeriodicSyncWorker(appContext: Context, workerParams: WorkerParameters) :
    FhirSyncWorker(appContext, workerParams) {

    override fun getDownloadWorkManager(): DownloadWorkManager {
        return DownloadManagerImpl()
    }


    override fun getFhirEngine() = FhirApplication.fhirEngine(applicationContext)
}