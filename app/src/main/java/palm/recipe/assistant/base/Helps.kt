package palm.recipe.assistant.base

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.view.ActionMode
import android.view.*
import android.widget.Button
import android.widget.Toast
import palm.recipe.assistant.R
import kotlin.reflect.KClass

/*
 * A few global values and functions.
 */

/**
 * EXTRA label for passing an ID to an editor activity
 */
const val EXTRA_ID = "palm.recipe.assistant.ID"

/**
 * Delegates for lazily getting views
 */
fun <T: View> Activity.viewId(id: Int) = lazy { findViewById<T>(id) }
fun <T: View> Fragment.viewId(id: Int) = lazy {
    view?.findViewById<T>(id) ?: throw IllegalStateException("View not yet set within fragment")
}

/**
 * Launch an activity from within another activity.
 *
 * @param activityClass the KClass of the activity to launch.
 * @param init a lambda with an Intent receiver to be applied before the activity
 *  is started. Useful for adding extras or other stuff.
 */
fun Context.launchActivity(activityClass: KClass<out Activity>, init: Intent.() -> Unit = {}) {
    val intent = Intent(this, activityClass.java)
    intent.init()
    startActivity(intent)
}

/**
 * Create and show a toast on the screen with the given message.
 *
 * @param msg the message to show
 * @param length the length of time to show, with default Toast.LENGTH_SHORT
 * @param gravity the gravity, with default Gravity.CENTER
 */
fun Context.toast(msg: String, length: Int = Toast.LENGTH_SHORT, gravity: Int = Gravity.CENTER) {
    val toast = Toast.makeText(this, msg, length)
    toast.setGravity(gravity, 0, 0)
    toast.show()
}

fun createActionModeCallback(menuId: Int, itemMappings: Map<Int, () -> Unit>, onDestroy: () -> Unit): ActionMode.Callback {
    return object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            val inflater : MenuInflater = mode.menuInflater
            inflater.inflate(menuId, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            val action = itemMappings[item.itemId]
            action?.invoke()
            return action != null
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            onDestroy()
        }
    }
}

fun createBundle(vararg items: Pair<String, Any>): Bundle {
    return Bundle().apply {
        for ((key, value) in items) {
            when (value) {
                is Int -> putInt(key, value)
                is String -> putString(key, value)
                // TODO add as needed
            }
        }
    }
}

// Button Helpers

fun Activity.buttonOnClick(id: Int, listener: () -> Unit) {
    findViewById<Button>(id).setOnClickListener { listener() }
}

fun Button.onClick(listener: () -> Unit) = setOnClickListener { listener() }

// Dialog Helpers

fun Context.confirmDelete(msg: String, action: () -> Unit) {
    showDialog {
        setMessage(msg)
        setPositiveButton(R.string.delete) { _, _ -> action() }
        setNegativeButton(R.string.cancel) { _, _ -> }
    }
}

fun Context.showDialog(init: AlertDialog.Builder.() -> Unit) {
    val builder = AlertDialog.Builder(this)
    builder.init()
    builder.show()
}
