package com.budgetbuddy.app.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// ─────────────────────────────────────────────────────────────────────────────
// USER
// ─────────────────────────────────────────────────────────────────────────────
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val email: String,
    val passwordHash: String,
    val avatarInitials: String,
    val memberSince: String,
    val totalXp: Int = 0,
    val level: Int = 1,
    val dayStreak: Int = 0,
    val lastLoggedDate: String = "",
    val totalSaved: Double = 0.0,
    val currency: String = "ZAR",
    val notificationsEnabled: Boolean = true
)

// ─────────────────────────────────────────────────────────────────────────────
// EXPENSE CATEGORY
// Now scoped to a userId so each user has their own category list.
// ─────────────────────────────────────────────────────────────────────────────
@Entity(
    tableName = "expense_categories",
    foreignKeys = [ForeignKey(
        entity        = UserEntity::class,
        parentColumns = ["id"],
        childColumns  = ["userId"],
        onDelete      = ForeignKey.CASCADE   // deleting user deletes their categories
    )],
    indices = [Index("userId")]
)
data class ExpenseCategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,                         // ← owner
    val name: String,
    val emoji: String,
    val isActive: Boolean = true
)

// ─────────────────────────────────────────────────────────────────────────────
// EXPENSE
// ─────────────────────────────────────────────────────────────────────────────
@Entity(
    tableName = "expenses",
    foreignKeys = [ForeignKey(
        entity        = UserEntity::class,
        parentColumns = ["id"],
        childColumns  = ["userId"],
        onDelete      = ForeignKey.CASCADE
    )],
    indices = [Index("userId")]
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,                         // ← owner
    val amount: Double,
    val description: String,
    val categoryId: Int,
    val categoryName: String,
    val categoryEmoji: String,
    val date: String,
    val month: String,
    val receiptPath: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

// ─────────────────────────────────────────────────────────────────────────────
// BUDGET
// ─────────────────────────────────────────────────────────────────────────────
@Entity(
    tableName = "budgets",
    foreignKeys = [ForeignKey(
        entity        = UserEntity::class,
        parentColumns = ["id"],
        childColumns  = ["userId"],
        onDelete      = ForeignKey.CASCADE
    )],
    indices = [Index("userId")]
)
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,                         // ← owner
    val categoryId: Int,
    val categoryName: String,
    val categoryEmoji: String,
    val amount: Double,
    val month: String
)

// ─────────────────────────────────────────────────────────────────────────────
// SAVINGS GOAL
// ─────────────────────────────────────────────────────────────────────────────
@Entity(
    tableName = "savings_goals",
    foreignKeys = [ForeignKey(
        entity        = UserEntity::class,
        parentColumns = ["id"],
        childColumns  = ["userId"],
        onDelete      = ForeignKey.CASCADE
    )],
    indices = [Index("userId")]
)
data class SavingsGoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,                         // ← owner
    val name: String,
    val targetAmount: Double,
    val savedAmount: Double = 0.0,
    val targetDate: String,
    val frequency: String,
    val contributionAmount: Double = 0.0,
    val isCompleted: Boolean = false,
    val completedDate: String = "",
    val xpEarned: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

// ─────────────────────────────────────────────────────────────────────────────
// NOTIFICATION
// ─────────────────────────────────────────────────────────────────────────────
@Entity(
    tableName = "notifications",
    foreignKeys = [ForeignKey(
        entity        = UserEntity::class,
        parentColumns = ["id"],
        childColumns  = ["userId"],
        onDelete      = ForeignKey.CASCADE
    )],
    indices = [Index("userId")]
)
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,                         // ← owner
    val icon: String,
    val title: String,
    val body: String,
    val time: String,
    val tag: String,
    val groupLabel: String = "",
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
