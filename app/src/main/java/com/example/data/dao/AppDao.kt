package com.example.data.dao

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // --- Salary Config ---
    @Query("SELECT * FROM salary_configs WHERE id = 1 LIMIT 1")
    fun getSalaryConfig(): Flow<SalaryConfig?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSalaryConfig(config: SalaryConfig)

    // --- Expenses ---
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    @Query("DELETE FROM expenses")
    suspend fun clearExpenses()

    // --- Savings Goals ---
    @Query("SELECT * FROM savings_goals ORDER BY id DESC")
    fun getAllSavingsGoals(): Flow<List<SavingsGoal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavingsGoal(goal: SavingsGoal)

    @Delete
    suspend fun deleteSavingsGoal(goal: SavingsGoal)

    // --- Loans ---
    @Query("SELECT * FROM loans ORDER BY id DESC")
    fun getAllLoans(): Flow<List<Loan>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoan(loan: Loan)

    @Delete
    suspend fun deleteLoan(loan: Loan)

    // --- Employees (SaaS Business Tier) ---
    @Query("SELECT * FROM employees ORDER BY id DESC")
    fun getAllEmployees(): Flow<List<Employee>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployee(employee: Employee)

    @Delete
    suspend fun deleteEmployee(employee: Employee)
}
