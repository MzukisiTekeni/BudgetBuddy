package com.budgetbuddy.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.budgetbuddy.app.db.BudgetRepository
import com.budgetbuddy.app.db.SessionManager
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private lateinit var repo: BudgetRepository
    private var userId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        repo   = BudgetRepository(this)
        userId = SessionManager.getUserId(this)

        findViewById<TextView>(R.id.tv_bar_title).text = "Profile"
        findViewById<ImageView>(R.id.iv_back).setOnClickListener { finish() }

        // ── Live unread-notification badge on the bell icon ───────────────────
        // This badge (tv_notif_badge) must sit on top of the bell ImageView in
        // your top bar layout (component_simple_top_bar or wherever the bell lives).
        repo.countUnread(userId).observe(this) { unread ->
            val badge = findViewById<TextView?>(R.id.tv_notif_badge)
            badge?.let {
                if ((unread ?: 0) > 0) {
                    it.visibility = View.VISIBLE
                    it.text = if (unread!! > 99) "99+" else unread.toString()
                } else {
                    it.visibility = View.GONE
                }
            }
        }

        // ── Observe user data ─────────────────────────────────────────────────
        repo.getLoggedInUser(userId).observe(this) { user ->
            user ?: return@observe

            findViewById<TextView>(R.id.tv_avatar_initials).text = user.avatarInitials
            findViewById<TextView>(R.id.tv_user_name).text       = user.username
            findViewById<TextView>(R.id.tv_level_badge).text     = "★ Lv ${user.level}"

            findViewById<TextView>(R.id.tv_day_streak).text = user.dayStreak.toString()
            findViewById<TextView>(R.id.tv_total_xp).text   = "%,d".format(user.totalXp)
            val savedStr = if (user.totalSaved >= 1000)
                "R${"%.1f".format(user.totalSaved / 1000)}K"
            else
                "R${"%,.0f".format(user.totalSaved)}"
            findViewById<TextView>(R.id.tv_total_saved).text = savedStr

            val maxXp    = user.level * 2000
            val progress = ((user.totalXp.toFloat() / maxXp) * 100).toInt().coerceIn(0, 100)
            findViewById<ProgressBar>(R.id.progress_xp).apply {
                max           = 100
                this.progress = progress
            }
        }

        // ── Total saved from goals ────────────────────────────────────────────
        repo.getTotalSaved(userId).observe(this) { /* supplements user.totalSaved */ }

        // ── XP earn rows ──────────────────────────────────────────────────────
        setEarnRow(R.id.row_log_expense,   "➕", "Log an expense",         "+10 XP")
        setEarnRow(R.id.row_stay_budget,   "✅", "Stay within budget",      "+50 XP")
        setEarnRow(R.id.row_complete_goal, "🎯", "Complete a savings goal", "+200 XP")

        // ── Tab switching ─────────────────────────────────────────────────────
        fun selectTab(tab: String) {
            listOf(R.id.tab_xp to "XP", R.id.tab_badges to "Badges", R.id.tab_themes to "Themes")
                .forEach { (id, label) ->
                    findViewById<TextView>(id).apply {
                        setBackgroundResource(if (label == tab) R.drawable.bg_chip_selected else R.drawable.bg_chip_unselected)
                        setTextColor(if (label == tab) getColor(R.color.text_on_primary) else getColor(R.color.text_secondary))
                    }
                }
            findViewById<View>(R.id.panel_xp).visibility = if (tab == "XP") View.VISIBLE else View.GONE
        }
        selectTab("XP")
        findViewById<TextView>(R.id.tab_xp).setOnClickListener     { selectTab("XP") }
        findViewById<TextView>(R.id.tab_badges).setOnClickListener  { selectTab("Badges") }
        findViewById<TextView>(R.id.tab_themes).setOnClickListener  { selectTab("Themes") }

        // ── Settings rows ─────────────────────────────────────────────────────
        setSettingsRow(R.id.row_notifications, "Notifications", showToggle = true, toggleOn = true)
        setSettingsRow(R.id.row_currency, "Currency", value = "ZAR • R")
        setSettingsRow(R.id.row_privacy,  "Privacy") {
            Toast.makeText(this, "Privacy settings coming soon", Toast.LENGTH_SHORT).show()
        }

        // ── Clear Data row ────────────────────────────────────────────────────
        setSettingsRow(R.id.row_clear_data, "Clear Data") { showClearDataDialog() }

        // ── Sign out ──────────────────────────────────────────────────────────
        setSettingsRow(R.id.row_signout, "Sign out") {
            SessionManager.clearSession(this)
            val intent = Intent(this, WelcomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    // ── Clear Data dialog ─────────────────────────────────────────────────────
    /**
     * Shows a multi-choice dialog so the user can pick exactly which data to wipe.
     * Clearing expenses also refreshes Budget Health and Statistics automatically
     * because both screens observe LiveData from the same expense table.
     *
     * Options:
     *   0 → Expenses & spending history  (also affects Statistics graph + Budget Health)
     *   1 → Category budgets
     *   2 → Savings goals
     *   3 → Notifications
     *   4 → Overall monthly budget (SharedPreferences)
     */
    private fun showClearDataDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_clear_data, null)

        val cbExpenses      = view.findViewById<CheckBox>(R.id.cb_expenses)
        val cbBudgets       = view.findViewById<CheckBox>(R.id.cb_budgets)
        val cbGoals         = view.findViewById<CheckBox>(R.id.cb_goals)
        val cbNotifications = view.findViewById<CheckBox>(R.id.cb_notifications)
        val cbOverall       = view.findViewById<CheckBox>(R.id.cb_overall_budget)

        val dialog = AlertDialog.Builder(this)
            .setTitle("🗑️ Clear Data")
            .setView(view)
            .setPositiveButton("Delete Selected", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val checked = booleanArrayOf(
                    cbExpenses.isChecked,
                    cbBudgets.isChecked,
                    cbGoals.isChecked,
                    cbNotifications.isChecked,
                    cbOverall.isChecked
                )

                if (checked.none { it }) {
                    Toast.makeText(this, "Please select at least one option", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val selectedLabels = listOf(
                    "Expenses & history",
                    "Category budgets",
                    "Savings goals",
                    "Notifications",
                    "Overall budget"
                ).filterIndexed { i, _ -> checked[i] }.joinToString(", ")

                dialog.dismiss()
                AlertDialog.Builder(this)
                    .setTitle("Are you sure?")
                    .setMessage("You are about to permanently delete:\n\n$selectedLabels\n\nThis cannot be undone.")
                    .setPositiveButton("Yes, delete") { _, _ -> performClear(checked) }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }

        dialog.show()
    }

    private fun performClear(checked: BooleanArray) {
        lifecycleScope.launch {
            val month = repo.currentMonth()

            // 0 — Expenses (LiveData observers on Statistics + Budget Health update automatically)
            if (checked[0]) {
                repo.expenseDao.deleteAllForUser(userId)
            }
            // 1 — Category budgets
            if (checked[1]) {
                repo.budgetDao.deleteAllForUser(userId)
                // Also reset the SharedPreferences balance
                repo.saveOverallBudget(this@ProfileActivity, userId, 0.0)
            }
            // 2 — Savings goals
            if (checked[2]) {
                repo.savingsGoalDao.deleteAllForUser(userId)
            }
            // 3 — Notifications
            if (checked[3]) {
                repo.clearAllNotifications(userId)
            }
            // 4 — Overall budget (SharedPreferences)
            if (checked[4]) {
                repo.saveOverallBudget(this@ProfileActivity, userId, 0.0)
            }

            runOnUiThread {
                Toast.makeText(this@ProfileActivity, "Data cleared ✓", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private fun setEarnRow(viewId: Int, icon: String, label: String, xp: String) {
        val row = findViewById<View>(viewId)
        row.findViewById<TextView>(R.id.tv_earn_icon).text  = icon
        row.findViewById<TextView>(R.id.tv_earn_label).text = label
        row.findViewById<TextView>(R.id.tv_earn_xp).text    = xp
    }

    private fun setSettingsRow(
        viewId: Int, label: String,
        value: String = "", showToggle: Boolean = false, toggleOn: Boolean = false,
        onClick: (() -> Unit)? = null
    ) {
        val row     = findViewById<View>(viewId)
        val tvLabel = row.findViewById<TextView>(R.id.tv_setting_label)
        val tvVal   = row.findViewById<TextView>(R.id.tv_setting_value)
        val ivChev  = row.findViewById<android.widget.ImageView>(R.id.iv_setting_action)
        val toggle  = row.findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(R.id.switch_toggle)

        tvLabel.text = label
        when {
            showToggle -> {
                toggle.visibility = View.VISIBLE
                ivChev.visibility = View.GONE
                toggle.isChecked  = toggleOn
            }
            value.isNotEmpty() -> {
                tvVal.text       = value
                tvVal.visibility = View.VISIBLE
            }
            else -> ivChev.visibility = View.VISIBLE
        }
        onClick?.let { row.setOnClickListener { it() } }
    }
}
