package com.example.data.repository

import com.example.data.dao.AppDao
import com.example.data.model.*
import com.example.data.network.GeminiNetwork
import com.example.data.network.ReceiptData
import kotlinx.coroutines.flow.Flow

class AppRepository(private val appDao: AppDao) {

    // --- Core Flows ---
    val salaryConfig: Flow<SalaryConfig?> = appDao.getSalaryConfig()
    val allExpenses: Flow<List<Expense>> = appDao.getAllExpenses()
    val allSavingsGoals: Flow<List<SavingsGoal>> = appDao.getAllSavingsGoals()
    val allLoans: Flow<List<Loan>> = appDao.getAllLoans()
    val allEmployees: Flow<List<Employee>> = appDao.getAllEmployees()

    // --- Writes ---
    suspend fun saveSalaryConfig(config: SalaryConfig) {
        appDao.insertSalaryConfig(config)
    }

    suspend fun addExpense(expense: Expense) {
        appDao.insertExpense(expense)
    }

    suspend fun deleteExpense(expense: Expense) {
        appDao.deleteExpense(expense)
    }

    suspend fun clearExpenses() {
        appDao.clearExpenses()
    }

    suspend fun addSavingsGoal(goal: SavingsGoal) {
        appDao.insertSavingsGoal(goal)
    }

    suspend fun deleteSavingsGoal(goal: SavingsGoal) {
        appDao.deleteSavingsGoal(goal)
    }

    suspend fun addLoan(loan: Loan) {
        appDao.insertLoan(loan)
    }

    suspend fun deleteLoan(loan: Loan) {
        appDao.deleteLoan(loan)
    }

    suspend fun addEmployee(employee: Employee) {
        appDao.insertEmployee(employee)
    }

    suspend fun deleteEmployee(employee: Employee) {
        appDao.deleteEmployee(employee)
    }

    // --- AI Operations ---
    suspend fun generateInsights(
        salary: Double,
        category: String,
        expenses: List<Expense>,
        goals: List<SavingsGoal>,
        loans: List<Loan>
    ): String {
        return GeminiNetwork.generateFinancialInsights(salary, category, expenses, goals, loans)
    }

    suspend fun scanReceipt(base64Image: String?): ReceiptData {
        return GeminiNetwork.extractExpenseFromReceipt(base64Image)
    }
}
