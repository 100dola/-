package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "salary_configs")
data class SalaryConfig(
    @PrimaryKey val id: Int = 1,
    val userCategory: String = "government", // government, private, retired, small_business
    val basicSalary: Double = 45000.0,
    val bonuses: Double = 5000.0,
    val deductions: Double = 0.0,
    val cnasRate: Double = 9.0, // CNAS percentage (usually 9%)
    val irgRate: Double = 23.0, // Base IRG rate
    val currentTier: String = "Free" // Free, Premium, Business
)

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val description: String,
    val amount: Double,
    val category: String, // food, utilities, rent, transport, healthcare, goals, other
    val date: Long = System.currentTimeMillis(),
    val isAutoCategorized: Boolean = false
)

@Entity(tableName = "savings_goals")
data class SavingsGoal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val targetAmount: Double,
    val savedAmount: Double = 0.0,
    val category: String, // car, house, marriage, travel, other
    val deadlineMonths: Int = 12
)

@Entity(tableName = "loans")
data class Loan(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val totalAmount: Double,
    val interestRate: Double = 0.0, // Interest percentage (e.g., 5%)
    val tenureMonths: Int = 12,
    val remainingAmount: Double,
    val monthlyPayment: Double = 0.0,
    val startDate: Long = System.currentTimeMillis()
)

@Entity(tableName = "employees")
data class Employee(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val jobTitle: String,
    val basicSalary: Double,
    val bonusAmount: Double = 0.0,
    val experienceYears: Int = 0
)
