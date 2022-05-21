package com.intellisoft.kabarakmhis.helperclass

import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

fun AppCompatActivity.replaceFragmenty(fragment: Fragment,
                                       allowStateLoss: Boolean = false,
                                       @IdRes containerViewId: Int){

    val fragmentManager = supportFragmentManager

    val fragmentTransaction = fragmentManager.beginTransaction().replace(containerViewId, fragment)

    if (!supportFragmentManager.isStateSaved){
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()

    }else if (allowStateLoss){
        fragmentTransaction.commitAllowingStateLoss()
    }

}