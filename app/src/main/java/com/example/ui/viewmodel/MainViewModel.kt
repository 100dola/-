package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.*
import com.example.data.repository.AppRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AppRepository
    
    // UI state streams from Room
    val salaryConfig: StateFlow<SalaryConfig>
    val expenses: StateFlow<List<Expense>>
    val savingsGoals: StateFlow<List<SavingsGoal>>
    val loans: StateFlow<List<Loan>>
    val employees: StateFlow<List<Employee>>

    // Interactive UI navigation state
    private val _activeTab = MutableStateFlow(0) // 0: Dashboard, 1: Expenses, 2: Simulator, 3: Goals & Loans, 4: Business
    val activeTab: StateFlow<Int> = _activeTab.asStateFlow()

    // Monetization tier state managed directly or inside config
    private val _currentTierRef = MutableStateFlow("Free")
    val currentTierRef: StateFlow<String> = _currentTierRef.asStateFlow()

    // AI generation states
    private val _aiInsights = MutableStateFlow<String?>(null)
    val aiInsights: StateFlow<String?> = _aiInsights.asStateFlow()

    private val _aiLoading = MutableStateFlow(false)
    val aiLoading: StateFlow<Boolean> = _aiLoading.asStateFlow()

    private val _ocrScanning = MutableStateFlow(false)
    val ocrScanning: StateFlow<Boolean> = _ocrScanning.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = AppRepository(database.appDao())

        // Create hot state flows from Room flows with safe fallbacks
        salaryConfig = repository.salaryConfig
            .map { it ?: SalaryConfig() }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = SalaryConfig()
            )

        expenses = repository.allExpenses
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        savingsGoals = repository.allSavingsGoals
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        loans = repository.allLoans
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        employees = repository.allEmployees
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        // Sync local current tier binding when config updates
        viewModelScope.launch {
            salaryConfig.collect { config ->
                _currentTierRef.value = config.currentTier
            }
        }

        // Prepopulate data on launch if completely empty
        viewModelScope.launch {
            repository.salaryConfig.first().let {
                if (it == null) {
                    val defaultVal = SalaryConfig()
                    repository.saveSalaryConfig(defaultVal)
                    seedInitialData()
                }
            }
        }
    }

    private suspend fun seedInitialData() {
        // Seed some standard Algerian expenses so the initial dashboard looks professional!
        val seedExpenses = listOf(
            Expense(description = "شراء مواد غذائية من سوبيرات الأمان", amount = 8500.0, category = "طعام", date = System.currentTimeMillis() - 86400000 * 2),
            Expense(description = "فاتورة الكهرباء والغاز سونلغاز", amount = 4500.0, category = "فواتير", date = System.currentTimeMillis() - 86400000 * 5),
            Expense(description = "شحن بطاقة جيزي الدفع المسبق", amount = 1500.0, category = "فواتير", date = System.currentTimeMillis() - 86400000 * 7),
            Expense(description = "تعبئة بنزين نفطال للسيارة", amount = 1200.0, category = "نقل", date = System.currentTimeMillis() - 86400000)
        )
        for (e in seedExpenses) {
            repository.addExpense(e)
        }

        // Seed some initial savings goals
        val seedGoals = listOf(
            SavingsGoal(title = "شراء سيارة فيات 500", targetAmount = 2400000.0, savedAmount = 450000.0, category = "car", deadlineMonths = 48),
            SavingsGoal(title = "تكاليف زواج عائلي", targetAmount = 600000.0, savedAmount = 250000.0, category = "marriage", deadlineMonths = 18)
        )
        for (g in seedGoals) {
            repository.addSavingsGoal(g)
        }

        // Seed an initial loan
        val seedLoan = Loan(
            title = "سلفة بنك البركة لبناء سكن",
            totalAmount = 1500000.0,
            interestRate = 3.5,
            tenureMonths = 60,
            remainingAmount = 1100000.0,
            monthlyPayment = 27000.0
        )
        repository.addLoan(seedLoan)

        // Seed some employees for business simulation
        val seedEmployees = listOf(
            Employee(name = "محمد أمين بن يوسف", jobTitle = "مهندس برمجيات رئيسي", basicSalary = 120000.0, bonusAmount = 15000.0, experienceYears = 5),
            Employee(name = "فاطمة الزهراء دغيم", jobTitle = "مسؤولة محاسبة عامة", basicSalary = 80000.0, bonusAmount = 8000.0, experienceYears = 3),
            Employee(name = "مراد عيساني", jobTitle = "تقني سامي في الشبكات", basicSalary = 65000.0, bonusAmount = 5000.0, experienceYears = 2)
        )
        for (emp in seedEmployees) {
            repository.addEmployee(emp)
        }
    }

    // --- Actions ---

    fun setActiveTab(index: Int) {
        _activeTab.value = index
    }

    fun setSalaryConfig(
        userCategory: String,
        basicSalary: Double,
        bonuses: Double,
        deductions: Double,
        cnasRate: Double,
        irgRate: Double
    ) {
        viewModelScope.launch {
            val tier = salaryConfig.value.currentTier
            val updated = SalaryConfig(
                id = 1,
                userCategory = userCategory,
                basicSalary = basicSalary,
                bonuses = bonuses,
                deductions = deductions,
                cnasRate = cnasRate,
                irgRate = irgRate,
                currentTier = tier
            )
            repository.saveSalaryConfig(updated)
        }
    }

    fun switchTier(newTier: String) {
        viewModelScope.launch {
            val config = salaryConfig.value.copy(currentTier = newTier)
            _currentTierRef.value = newTier
            repository.saveSalaryConfig(config)
        }
    }

    fun addExpense(description: String, amount: Double, category: String) {
        viewModelScope.launch {
            val item = Expense(description = description, amount = amount, category = category)
            repository.addExpense(item)
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    fun clearAllExpenses() {
        viewModelScope.launch {
            repository.clearExpenses()
        }
    }

    fun addSavingsGoal(title: String, targetAmount: Double, savedAmount: Double, category: String, deadlineMonths: Int) {
        viewModelScope.launch {
            val goal = SavingsGoal(
                title = title,
                targetAmount = targetAmount,
                savedAmount = savedAmount,
                category = category,
                deadlineMonths = deadlineMonths
            )
            repository.addSavingsGoal(goal)
        }
    }

    fun updateGoalSaving(goal: SavingsGoal, additionalSaved: Double) {
        viewModelScope.launch {
            val updatedConfig = goal.copy(savedAmount = minOf(goal.targetAmount, goal.savedAmount + additionalSaved))
            repository.addSavingsGoal(updatedConfig)
        }
    }

    fun deleteSavingsGoal(goal: SavingsGoal) {
        viewModelScope.launch {
            repository.deleteSavingsGoal(goal)
        }
    }

    fun addLoan(title: String, totalAmount: Double, interestRate: Double, tenureMonths: Int, remainingAmount: Double, monthlyPayment: Double) {
        viewModelScope.launch {
            val ln = Loan(
                title = title,
                totalAmount = totalAmount,
                interestRate = interestRate,
                tenureMonths = tenureMonths,
                remainingAmount = remainingAmount,
                monthlyPayment = monthlyPayment
            )
            repository.addLoan(ln)
        }
    }

    fun deleteLoan(loan: Loan) {
        viewModelScope.launch {
            repository.deleteLoan(loan)
        }
    }

    fun addEmployee(name: String, jobTitle: String, basicSalary: Double, bonusAmount: Double, experienceYears: Int) {
        viewModelScope.launch {
            val emp = Employee(
                name = name,
                jobTitle = jobTitle,
                basicSalary = basicSalary,
                bonusAmount = bonusAmount,
                experienceYears = experienceYears
            )
            repository.addEmployee(emp)
        }
    }

    fun deleteEmployee(employee: Employee) {
        viewModelScope.launch {
            repository.deleteEmployee(employee)
        }
    }

    // --- AI Operation triggers ---

    fun requestFinancialInsights() {
        viewModelScope.launch {
            _aiLoading.value = true
            _aiInsights.value = null
            try {
                val sc = salaryConfig.value
                val expList = expenses.value
                val goalsList = savingsGoals.value
                val loansList = loans.value
                val insights = repository.generateInsights(
                    salary = sc.basicSalary,
                    category = sc.userCategory,
                    expenses = expList,
                    goals = goalsList,
                    loans = loansList
                )
                _aiInsights.value = insights
            } catch (e: Exception) {
                _aiInsights.value = "حدث خطأ غير متوقع أثناء إعداد التقرير: ${e.localizedMessage}"
            } finally {
                _aiLoading.value = false
            }
        }
    }

    fun scanReceiptAndAdd() {
        viewModelScope.launch {
            _ocrScanning.value = true
            try {
                // Pass null to use the simulated OCR parser defaults
                val result = repository.scanReceipt(null)
                addExpense(result.description, result.amount, result.category)
            } catch (e: Exception) {
                // Fail-safe
            } finally {
                _ocrScanning.value = false
            }
        }
    }
}
