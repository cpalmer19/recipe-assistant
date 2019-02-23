package palm.recipe.assistant

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.view.ActionMode
import android.view.Gravity
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import kotlin.reflect.KClass

/*
 * A few global values and functions.
 */

/**
 * EXTRA label for passing an ID to an editor activity
 */
const val EXTRA_ID = "palm.whipit.palm.recipe.assistant.ID"

/**
 * Launch an activity from within another activity.
 *
 * @param activityClass the KClass of the activity to launch.
 * @param init a lambda with an Intent receiver to be applied before the activity
 *  is started. Useful for adding extras or other stuff.
 */
fun Activity.launchActivity(activityClass: KClass<out Activity>, init: Intent.() -> Unit = {}) {
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
fun Activity.toast(msg: String, length: Int = Toast.LENGTH_SHORT, gravity: Int = Gravity.CENTER) {
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

fun Button.onClick(listener: () -> Unit) = setOnClickListener { listener() }

fun Context.confirmDelete(msg: String, action: () -> Unit) {
    AlertDialog.Builder(this).apply {
        setMessage(msg)
        setPositiveButton(R.string.confirm_delete_delete) { _, _ -> action() }
        setNegativeButton(R.string.confirm_delete_cancel) { _, _ ->  }
        show()
    }
}

fun Fragment.confirmDelete(msg: String, action: () -> Unit) = context.confirmDelete(msg, action)
