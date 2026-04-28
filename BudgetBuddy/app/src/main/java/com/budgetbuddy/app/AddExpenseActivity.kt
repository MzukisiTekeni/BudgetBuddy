package com.budgetbuddy.app

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.budgetbuddy.app.db.BudgetRepository
import com.budgetbuddy.app.db.ExpenseCategoryEntity
import com.budgetbuddy.app.db.SessionManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AddExpenseActivity : AppCompatActivity() {

    private lateinit var repo: BudgetRepository
    private var userId = -1                          // set once in onCreate from SessionManager

    private var categories  = listOf<ExpenseCategoryEntity>()
    private var selectedCat : ExpenseCategoryEntity? = null
    private var receiptUri  : Uri? = null
    private var selectedDate = ""

    private val pickReceipt = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            receiptUri = it
            val ivPreview = findViewById<ImageView>(R.id.iv_receipt_preview)
            ivPreview.setImageURI(it)
            ivPreview.visibility = View.VISIBLE
            findViewById<LinearLayout>(R.id.ll_receipt_upload).visibility = View.GONE
            Toast.makeText(this, "Receipt attached ✓", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expense)

        repo   = BudgetRepository(this)
        userId = SessionManager.getUserId(this)

        findViewById<TextView>(R.id.tv_bar_title).text = "Add Expense"
        findViewById<ImageView>(R.id.iv_back).setOnClickListener { finish() }

        // ── Categories ────────────────────────────────────────────────────────
        repo.getActiveCategories(userId).observe(this) { cats ->
            categories = cats
            val noCategories = cats.isEmpty()
            findViewById<TextView>(R.id.tv_no_categories).visibility =
                if (noCategories) View.VISIBLE else View.GONE
            findViewById<Button>(R.id.btn_add_category).visibility =
                if (noCategories) View.VISIBLE else View.GONE
        }

        // ── Category dropdown ─────────────────────────────────────────────────
        val tvCat    = findViewById<TextView>(R.id.tv_selected_category)
        val catClick = View.OnClickListener { v ->
            if (categories.isEmpty()) {
                Toast.makeText(this, "Add categories first", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }
            val popup = PopupMenu(this, v)
            categories.forEachIndexed { i, cat ->
                popup.menu.add(0, i, i, "${cat.emoji} ${cat.name}")
            }
            popup.setOnMenuItemClickListener { item ->
                val chosen = categories[item.itemId]
                lifecycleScope.launch {
                    val budget = repo.budgetDao.getBudgetForCategory(
                        userId, repo.currentMonth(), chosen.id
                    )
                    runOnUiThread {
                        if (budget == null || budget.amount <= 0) {
                            AlertDialog.Builder(this@AddExpenseActivity)
                                .setTitle("⚠️ No Budget Set")
                                .setMessage(
                                    "You haven't set a budget for \"${chosen.emoji} ${chosen.name}\" yet.\n\n" +
                                    "Please set a category budget on the Monthly Budget page before logging expenses here."
                                )
                                .setPositiveButton("Set Budget") { _, _ ->
                                    startActivity(Intent(this@AddExpenseActivity, MonthlyBudgetActivity::class.java))
                                }
                                .setNegativeButton("Cancel", null)
                                .show()
                        } else {
                            selectedCat = chosen
                            tvCat.text = "${chosen.emoji} ${chosen.name}"
                            tvCat.setTextColor(getColor(R.color.text_primary))
                        }
                    }
                }
                true
            }
            popup.show()
        }
        tvCat.setOnClickListener(catClick)
        findViewById<ImageView>(R.id.iv_dropdown).setOnClickListener(catClick)

        // ── Date picker ───────────────────────────────────────────────────────
        val tvDate = findViewById<TextView>(R.id.tv_date)
        val dateCl = View.OnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(this, { _, y, m, d ->
                cal.set(y, m, d)
                selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
                tvDate.text  = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(cal.time)
                tvDate.setTextColor(getColor(R.color.text_primary))
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }
        tvDate.setOnClickListener(dateCl)
        findViewById<ImageView>(R.id.iv_calendar).setOnClickListener(dateCl)

        // ── Receipt ───────────────────────────────────────────────────────────
        findViewById<LinearLayout>(R.id.ll_receipt_upload).setOnClickListener {
            pickReceipt.launch("image/*")
        }
        findViewById<ImageView>(R.id.iv_receipt_preview).setOnClickListener {
            pickReceipt.launch("image/*")
        }

        // ── Buttons ───────────────────────────────────────────────────────────
        findViewById<Button>(R.id.btn_save_expense).setOnClickListener { saveExpense() }
        findViewById<Button>(R.id.btn_add_category).setOnClickListener {
            startActivity(Intent(this, ExpenseCategoriesActivity::class.java))
        }
    }

    private fun saveExpense() {
        val amountStr = findViewById<EditText>(R.id.et_amount).text.toString()
            .replace("R", "").replace(" ", "").replace(",", "").trim()
        val amount = amountStr.toDoubleOrNull()
        val desc   = findViewById<EditText>(R.id.et_description).text.toString().trim()

        if (amount == null || amount <= 0) {
            Toast.makeText(this, "Enter a valid amount", Toast.LENGTH_SHORT).show(); return
        }
        if (selectedCat == null) {
            Toast.makeText(this, "Select a category", Toast.LENGTH_SHORT).show(); return
        }
        if (selectedDate.isEmpty()) {
            Toast.makeText(this, "Select a date", Toast.LENGTH_SHORT).show(); return
        }

        lifecycleScope.launch {
            // Budget guard
            val budget = repo.budgetDao.getBudgetForCategory(
                userId, repo.currentMonth(), selectedCat!!.id
            )
            if (budget == null || budget.amount <= 0) {
                runOnUiThread {
                    AlertDialog.Builder(this@AddExpenseActivity)
                        .setTitle("⚠️ No Budget Set")
                        .setMessage(
                            "You need to set a budget for \"${selectedCat!!.emoji} ${selectedCat!!.name}\" " +
                            "before logging an expense."
                        )
                        .setPositiveButton("Set Budget") { _, _ ->
                            startActivity(Intent(this@AddExpenseActivity, MonthlyBudgetActivity::class.java))
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }
                return@launch
            }

            repo.saveExpense(
                userId      = userId,
                amount      = amount,
                description = desc,
                category    = selectedCat!!,
                date        = selectedDate,
                receiptPath = receiptUri?.toString() ?: ""
            )

            if (userId != -1) {
                repo.addXp(userId, 10)
                repo.updateStreak(userId)
                repo.addNotification(
                    userId     = userId,
                    icon       = "✅",
                    title      = "Expense logged!",
                    body       = "${selectedCat!!.emoji} $desc — R${"%,.2f".format(amount)}",
                    time       = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date()),
                    tag        = "NUDGE",
                    groupLabel = "Today"
                )
            }

            runOnUiThread {
                Toast.makeText(this@AddExpenseActivity, "Saved! +10 XP 🎉", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
