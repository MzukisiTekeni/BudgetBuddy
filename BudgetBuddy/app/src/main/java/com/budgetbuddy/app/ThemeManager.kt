package com.budgetbuddy.app

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * ThemeManager
 * ─────────────────────────────────────────────────────────────
 * Persists the user's selected theme and exposes helpers to
 * re-colour primary-tinted views across every Activity.
 *
 * Themes map to a primary colour that replaces the default
 * Forest green (#2D6A4F / R.color.primary) everywhere it appears.
 */
object ThemeManager {

    // ── SharedPreferences ─────────────────────────────────────────────────────
    private const val PREFS      = "budgetbuddy_theme"
    private const val KEY_THEME  = "selected_theme"
    const val THEME_FOREST   = "Forest"
    const val THEME_OCEAN    = "Ocean"
    const val THEME_MIDNIGHT = "Midnight"
    const val THEME_VIOLET   = "Violet"
    const val THEME_AMBER    = "Amber"

    // ── Theme colour palette ──────────────────────────────────────────────────
    data class ThemePalette(
        val primary: Int,         // main brand colour
        val primaryDark: Int,     // darker variant (status bar, headers)
        val primaryLight: Int,    // lighter variant (backgrounds, tints)
        val accentLight: Int      // very light tint (card backgrounds, chips)
    )

    private val palettes: Map<String, ThemePalette> = mapOf(
        THEME_FOREST to ThemePalette(
            primary      = Color.parseColor("#2D6A4F"),
            primaryDark  = Color.parseColor("#1B4332"),
            primaryLight = Color.parseColor("#52B788"),
            accentLight  = Color.parseColor("#D8F3DC")
        ),
        THEME_OCEAN to ThemePalette(
            primary      = Color.parseColor("#0077B6"),
            primaryDark  = Color.parseColor("#023E8A"),
            primaryLight = Color.parseColor("#48CAE4"),
            accentLight  = Color.parseColor("#CAF0F8")
        ),
        THEME_MIDNIGHT to ThemePalette(
            primary      = Color.parseColor("#3A0CA3"),
            primaryDark  = Color.parseColor("#10002B"),
            primaryLight = Color.parseColor("#7B2FBE"),
            accentLight  = Color.parseColor("#E0AAFF")
        ),
        THEME_VIOLET to ThemePalette(
            primary      = Color.parseColor("#7B2D8B"),
            primaryDark  = Color.parseColor("#4A0E57"),
            primaryLight = Color.parseColor("#C77DFF"),
            accentLight  = Color.parseColor("#F3D5F7")
        ),
        THEME_AMBER to ThemePalette(
            primary      = Color.parseColor("#B45309"),
            primaryDark  = Color.parseColor("#78350F"),
            primaryLight = Color.parseColor("#F59E0B"),
            accentLight  = Color.parseColor("#FEF3C7")
        )
    )

    // ── Persistence ───────────────────────────────────────────────────────────

    fun saveTheme(context: Context, themeName: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_THEME, themeName).apply()
    }

    fun getCurrentTheme(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_THEME, THEME_FOREST) ?: THEME_FOREST

    fun getPalette(context: Context): ThemePalette =
        palettes[getCurrentTheme(context)] ?: palettes[THEME_FOREST]!!

    fun getPaletteFor(themeName: String): ThemePalette =
        palettes[themeName] ?: palettes[THEME_FOREST]!!

    // ── Apply helpers ─────────────────────────────────────────────────────────

    /**
     * Tint a view's background drawable with the primary colour.
     * Works for shapes (GradientDrawable) — covers buttons, chips, avatar circle, etc.
     */
    fun tintBackground(view: View, color: Int) {
        val bg = view.background?.mutate()
        if (bg is GradientDrawable) {
            bg.setColor(color)
        } else {
            view.backgroundTintList = ColorStateList.valueOf(color)
        }
    }

    /**
     * Apply the current theme to a ProgressBar's progress tint.
     */
    fun tintProgressBar(bar: ProgressBar, color: Int) {
        bar.progressTintList = ColorStateList.valueOf(color)
    }

    /**
     * Apply the current theme to a BottomNavigationView's icon and text tints.
     */
    fun tintBottomNav(nav: BottomNavigationView, primary: Int, unselected: Int) {
        val states = arrayOf(
            intArrayOf(android.R.attr.state_checked),
            intArrayOf(-android.R.attr.state_checked)
        )
        val colors = intArrayOf(primary, unselected)
        val csl = ColorStateList(states, colors)
        nav.itemIconTintList = csl
        nav.itemTextColor   = csl
    }

    /**
     * Colour a TextView's text with the primary colour.
     */
    fun tintText(tv: TextView, color: Int) {
        tv.setTextColor(color)
    }

    /**
     * Tint an ImageView with the primary colour.
     */
    fun tintImage(iv: ImageView, color: Int) {
        iv.imageTintList = ColorStateList.valueOf(color)
    }
}
