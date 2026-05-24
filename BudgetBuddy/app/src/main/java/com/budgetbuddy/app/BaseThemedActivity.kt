package com.budgetbuddy.app

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * BaseThemedActivity
 * ─────────────────────────────────────────────────────────────────
 * All activities extend this.  On every onResume() the current
 * theme is re-applied so navigating back from ProfileActivity
 * picks up a newly chosen theme immediately.
 */
abstract class BaseThemedActivity : AppCompatActivity() {

    /** Views whose background GradientDrawable should be filled with primary */
    open fun themedBackgroundViewIds(): List<Int> = emptyList()

    /** CardViews whose card background should be set to primary */
    open fun themedCardViewIds(): List<Int> = emptyList()

    /** Plain Views (dividers, solid blocks) whose background colour = primary */
    open fun themedSolidViewIds(): List<Int> = emptyList()

    /** ProgressBars whose progress tint = primary */
    open fun themedProgressBarIds(): List<Int> = emptyList()

    /** TextViews whose text colour = primary */
    open fun themedTextViewIds(): List<Int> = emptyList()

    /** ImageViews whose imageTint = primary */
    open fun themedImageViewIds(): List<Int> = emptyList()

    /** BottomNavigationView ID (0 = absent) */
    open fun bottomNavId(): Int = 0

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    override fun onResume() {
        super.onResume()
        // Called on every resume so that coming back from ProfileActivity
        // after a theme change re-colours this screen without recreating it.
        applyCurrentTheme()
    }

    // ── Theme application ─────────────────────────────────────────────────────

    protected fun applyCurrentTheme() {
        val palette = ThemeManager.getPalette(this)
        val primary = palette.primary

        // Rounded-background views: buttons, avatar circles, chips
        themedBackgroundViewIds().forEach { id ->
            runCatching { ThemeManager.tintBackground(findViewById(id), primary) }
        }

        // CardViews with solid primary background
        themedCardViewIds().forEach { id ->
            runCatching { findViewById<CardView>(id).setCardBackgroundColor(primary) }
        }

        // Plain View dividers / solid colour blocks
        themedSolidViewIds().forEach { id ->
            runCatching { findViewById<View>(id).setBackgroundColor(primary) }
        }

        // Progress bars
        themedProgressBarIds().forEach { id ->
            runCatching { ThemeManager.tintProgressBar(findViewById(id), primary) }
        }

        // Text views
        themedTextViewIds().forEach { id ->
            runCatching { ThemeManager.tintText(findViewById(id), primary) }
        }

        // Image views
        themedImageViewIds().forEach { id ->
            runCatching { ThemeManager.tintImage(findViewById(id), primary) }
        }

        // ── Bottom Navigation ─────────────────────────────────────────────────
        // We keep the icons' original drawable colours by setting a proper
        // ColorStateList on itemIconTintList and itemTextColor.
        // The UNSELECTED colour stays exactly as the original @color/nav_unselected.
        val navId = bottomNavId()
        if (navId != 0) {
            runCatching {
                val nav = findViewById<BottomNavigationView>(navId)
                val unselected = 0xFF9CA3AF.toInt()  // @color/nav_unselected
                val states = arrayOf(
                    intArrayOf(android.R.attr.state_checked),
                    intArrayOf(-android.R.attr.state_checked)
                )
                val csl = ColorStateList(states, intArrayOf(primary, unselected))
                nav.itemIconTintList = csl
                nav.itemTextColor    = csl
            }
        }
    }
}
