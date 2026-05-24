package com.budgetbuddy.app

import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * BaseThemedActivity
 * ──────────────────────────────────────────────────────────────
 * Every Activity in BudgetBuddy extends this instead of
 * AppCompatActivity directly.  After setContentView() each
 * subclass calls applyCurrentTheme() to re-colour the standard
 * themed elements on that screen.
 */
abstract class BaseThemedActivity : AppCompatActivity() {

    // Subclasses must declare which view IDs carry the primary colour.
    // Return empty list for IDs that don't exist on a particular screen.

    /** FrameLayout / LinearLayout / View whose background should be primary-tinted */
    open fun themedBackgroundViewIds(): List<Int> = emptyList()

    /** ProgressBar IDs whose progress track should be primary-tinted */
    open fun themedProgressBarIds(): List<Int> = emptyList()

    /** TextView IDs whose text colour should be primary */
    open fun themedTextViewIds(): List<Int> = emptyList()

    /** ImageView IDs whose tint should be primary */
    open fun themedImageViewIds(): List<Int> = emptyList()

    /** BottomNavigationView ID (or 0 if absent) */
    open fun bottomNavId(): Int = 0

    /**
     * Call this at the end of onCreate(), after setContentView() and
     * all view bindings.
     */
    protected fun applyCurrentTheme() {
        val palette = ThemeManager.getPalette(this)

        themedBackgroundViewIds().forEach { id ->
            runCatching { ThemeManager.tintBackground(findViewById(id), palette.primary) }
        }

        themedProgressBarIds().forEach { id ->
            runCatching {
                val bar = findViewById<ProgressBar>(id)
                ThemeManager.tintProgressBar(bar, palette.primary)
            }
        }

        themedTextViewIds().forEach { id ->
            runCatching {
                val tv = findViewById<TextView>(id)
                ThemeManager.tintText(tv, palette.primary)
            }
        }

        themedImageViewIds().forEach { id ->
            runCatching {
                val iv = findViewById<ImageView>(id)
                ThemeManager.tintImage(iv, palette.primary)
            }
        }

        val navId = bottomNavId()
        if (navId != 0) {
            runCatching {
                val nav = findViewById<BottomNavigationView>(navId)
                ThemeManager.tintBottomNav(nav, palette.primary, 0xFF9CA3AF.toInt())
            }
        }
    }
}
