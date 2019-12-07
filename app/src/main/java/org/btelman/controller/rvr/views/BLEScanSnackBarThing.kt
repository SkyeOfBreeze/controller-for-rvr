package org.btelman.controller.rvr.views

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.snackbar.BaseTransientBottomBar
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout

/**
 * Kind of a snack bar, but not really. Google might not be really proud of this
 */
class BLEScanSnackBarThing(
    parent: ViewGroup,
    val bleLayout: BLEScanLayout
) : BaseTransientBottomBar<BLEScanSnackBarThing>(parent, bleLayout, bleLayout) {

    override fun dismiss() {
        super.dismiss()
        bleLayout.stopScan()
    }

    companion object{
        fun make(view: View): BLEScanSnackBarThing {

            // First we find a suitable parent for our custom view
            val parent = view.findSuitableParent() ?: throw IllegalArgumentException(
                "No suitable parent found from the given view. Please provide a valid view."
            )

            // We inflate our custom view
            val customView = LayoutInflater.from(view.context).inflate(
                org.btelman.controller.rvr.R.layout.ble_scan_layout,
                parent,
                false
            ) as BLEScanLayout

            // We create and return our Snackbar
            return BLEScanSnackBarThing(
                parent,
                customView
            ).also {
                it.duration = LENGTH_INDEFINITE
                it.behavior = object : Behavior(){
                    override fun canSwipeDismissView(child: View): Boolean {
                        return false
                    }
                }
            }
        }

        internal fun View?.findSuitableParent(): ViewGroup? {
            var view = this
            var fallback: ViewGroup? = null
            do {
                if (view is CoordinatorLayout) {
                    // We've found a CoordinatorLayout, use it
                    return view
                } else if (view is FrameLayout) {
                    if (view.id == android.R.id.content) {
                        // If we've hit the decor bleLayout view, then we didn't find a CoL in the
                        // hierarchy, so use it.
                        return view
                    } else {
                        // It's not the bleLayout view but we'll use it as our fallback
                        fallback = view
                    }
                }

                if (view != null) {
                    // Else, we will loop and crawl up the view hierarchy and try to find a parent
                    val parent = view.parent
                    view = if (parent is View) parent else null
                }
            } while (view != null)

            // If we reach here then we didn't find a CoL or a suitable bleLayout view so we'll fallback
            return fallback
        }
    }
}