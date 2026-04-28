package com.budgetbuddy.app

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.budgetbuddy.app.db.BudgetRepository
import com.budgetbuddy.app.db.ExpenseCategoryEntity
import com.budgetbuddy.app.db.SessionManager
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class LogBudgetActivity : AppCompatActivity() {

    private lateinit var repo: BudgetRepository
    private var selectedCategory: ExpenseCategoryEntity? = null
    private var categories = listOf<ExpenseCategoryEntity>()
    private val month = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_budget)
        repo = BudgetRepository(this)
        val userId = SessionManager.getUserId(this)

        findViewById<TextView>(R.id.tv_bar_title).text = "Log Budget Details"
        findViewById<ImageView>(R.id.iv_back).setOnClickListener { finish() }

        // Load categories
        repo.getActiveCategories(userId).observe(this) { cats -> categories = cats }

        val tvCat = findViewById<TextView>(R.id.tv_budget_category)
        val catClick = View.OnClickListener { v ->
            val popup = PopupMenu(this, v)
            categories.forEachIndexed { i, c -> popup.menu.add(0, i, i, "${c.emoji} ${c.name}") }
            popup.setOnMenuItemClickListener { item ->
                selectedCategory = categories[item.itemId]
                tvCat.text = "${selectedCategory!!.emoji} ${selectedCategory!!.name}"
                tvCat.setTextColor(getColor(R.color.text_primary))
                true
            }
            popup.show()
        }
        tvCat.setOnClickListener(catClick)
        findViewById<ImageView>(R.id.iv_cat_dropdown).setOnClickListener(catClick)

        findViewById<Button>(R.id.btn_cancel).setOnClickListener { finish() }

        findViewById<Button>(R.id.btn_save_budget).setOnClickListener {
            val amountStr = findViewById<EditText>(R.id.et_budget_amount).text.toString()
                .replace("R", "").replace(" ", "").trim()
            val amount = amountStr.toDoubleOrNull()
            if (amount == null || amount <= 0) {
                Toast.makeText(this, "Enter a valid amount", Toast.LENGTH_SHORT).show(); return@setOnClickListener
            }
            if (selectedCategory == null) {
                Toast.makeText(this, "Choose a category", Toast.LENGTH_SHORT).show(); return@setOnClickListener
            }
            lifecycleScope.launch {
                repo.saveBudget(userId, selectedCategory!!, amount, month)
                runOnUiThread {
                    Toast.makeText(this@LogBudgetActivity,
                        "Budget saved for ${selectedCategory!!.name}", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }

        findViewById<Button>(R.id.btn_add_category).setOnClickListener {
            startActivity(android.content.Intent(this, ExpenseCategoriesActivity::class.java))
        }
    }
}
