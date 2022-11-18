package com.kabarak.kabarakmhis.helperclass

import android.content.res.ColorStateList
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.RoundedCornerTreatment
import com.google.android.material.shape.ShapeAppearanceModel
import com.kabarak.kabarakmhis.fhir.data.Constants
import com.kabarak.kabarakmhis.fhir.data.Constants.CORNER_RADIUS
import com.kabarak.kabarakmhis.fhir.data.Constants.FILL_COLOR
import com.kabarak.kabarakmhis.fhir.data.Constants.STROKE_COLOR

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
fun allCornersRounded(): MaterialShapeDrawable {
    return MaterialShapeDrawable(
        ShapeAppearanceModel.builder()
            .setAllCornerSizes(CORNER_RADIUS)
            .setAllCorners(RoundedCornerTreatment())
            .build()
    )
        .applyStrokeColor()
}

fun topCornersRounded(): MaterialShapeDrawable {
    return MaterialShapeDrawable(
        ShapeAppearanceModel.builder()
            .setTopLeftCornerSize(CORNER_RADIUS)
            .setTopRightCornerSize(CORNER_RADIUS)
            .setTopLeftCorner(RoundedCornerTreatment())
            .setTopRightCorner(RoundedCornerTreatment())
            .build()
    )
        .applyStrokeColor()
}

fun bottomCornersRounded(): MaterialShapeDrawable {
    return MaterialShapeDrawable(
        ShapeAppearanceModel.builder()
            .setBottomLeftCornerSize(CORNER_RADIUS)
            .setBottomRightCornerSize(CORNER_RADIUS)
            .setBottomLeftCorner(RoundedCornerTreatment())
            .setBottomRightCorner(RoundedCornerTreatment())
            .build()
    )
        .applyStrokeColor()
}

fun noCornersRounded(): MaterialShapeDrawable {
    return MaterialShapeDrawable(ShapeAppearanceModel.builder().build()).applyStrokeColor()
}

fun MaterialShapeDrawable.applyStrokeColor(): MaterialShapeDrawable {
    strokeWidth = Constants.STROKE_WIDTH
    fillColor = ColorStateList.valueOf(FILL_COLOR)
    strokeColor = ColorStateList.valueOf(STROKE_COLOR)
    return this
}