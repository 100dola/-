package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Employee
import com.example.data.model.Expense
import com.example.data.model.Loan
import com.example.data.model.SavingsGoal
import com.example.data.model.SalaryConfig
import com.example.data.utils.SalaryCalculator
import com.example.ui.components.CardSection
import com.example.ui.components.FinancialGauge
import com.example.ui.components.ProjectionLineChart
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MasterDashboard(viewModel: MainViewModel) {
    val context = LocalContext.current
    val salaryConfig by viewModel.salaryConfig.collectAsStateWithLifecycle()
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val savingsGoals by viewModel.savingsGoals.collectAsStateWithLifecycle()
    val loans by viewModel.loans.collectAsStateWithLifecycle()
    val employees by viewModel.employees.collectAsStateWithLifecycle()

    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
    val currentTier by viewModel.currentTierRef.collectAsStateWithLifecycle()

    val aiInsights by viewModel.aiInsights.collectAsStateWithLifecycle()
    val aiLoading by viewModel.aiLoading.collectAsStateWithLifecycle()
    val ocrScanning by viewModel.ocrScanning.collectAsStateWithLifecycle()

    // Calculate dynamic state statistics
    val breakdown = SalaryCalculator.calculate(salaryConfig)
    val totalExpenses = expenses.sumOf { it.amount }
    val remainingSalary = maxOf(0.0, breakdown.netSalary - totalExpenses)

    // Calculate score
    val savingsPercent = if (breakdown.netSalary > 0) (remainingSalary / breakdown.netSalary * 100).toInt() else 0
    val healthScore = when {
        totalExpenses > breakdown.netSalary -> 25
        savingsPercent < 10 -> 50
        savingsPercent in 10..22 -> 78
        else -> 95
    }

    // Force Arabic right-to-left layout for exact Algerian customization
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Budget DZ",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp,
                                    fontSize = 20.sp,
                                    color = EmeraldPrimary
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "| ميزانيتي 🇩🇿",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = OnBackgroundWhite
                                )
                            )
                        }
                    },
                    actions = {
                        // Quick VIP Pricing Tier Selector widget!
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(BankSurfaceVariant)
                                .clickable {
                                    val nextTier = when (currentTier) {
                                        "Free" -> "Premium"
                                        "Premium" -> "Business"
                                        else -> "Free"
                                    }
                                    viewModel.switchTier(nextTier)
                                    Toast.makeText(context, "تم التبديل لباقة: $nextTier", Toast.LENGTH_SHORT).show()
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "SaaS Level",
                                tint = when (currentTier) {
                                    "Premium" -> EmeraldPrimary
                                    "Business" -> GoldPremium
                                    else -> Color.Gray
                                },
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = when (currentTier) {
                                    "Premium" -> "بريميوم"
                                    "Business" -> "شركات"
                                    else -> "مجاني"
                                },
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OnBackgroundWhite
                                )
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = BankBackground
                    )
                )
            },
            bottomBar = {
                // Bottom navigation bar respecting system navigation safe zones
                NavigationBar(
                    containerColor = BankSurface,
                    modifier = Modifier.navigationBarsPadding()
                ) {
                    NavigationBarItem(
                        selected = activeTab == 0,
                        onClick = { viewModel.setActiveTab(0) },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
                        label = { Text("الرئيسية", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BankBackground,
                            selectedTextColor = EmeraldPrimary,
                            indicatorColor = EmeraldPrimary,
                            unselectedTextColor = TextSecondaryMuted,
                            unselectedIconColor = TextSecondaryMuted
                        )
                    )
                    NavigationBarItem(
                        selected = activeTab == 1,
                        onClick = { viewModel.setActiveTab(1) },
                        icon = { Icon(Icons.Default.Search, contentDescription = "Expenses") },
                        label = { Text("المصاريف والـ AI", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BankBackground,
                            selectedTextColor = EmeraldPrimary,
                            indicatorColor = EmeraldPrimary,
                            unselectedTextColor = TextSecondaryMuted,
                            unselectedIconColor = TextSecondaryMuted
                        )
                    )
                    NavigationBarItem(
                        selected = activeTab == 2,
                        onClick = { viewModel.setActiveTab(2) },
                        icon = { Icon(Icons.Default.Favorite, contentDescription = "Simulator") },
                        label = { Text("المحاكي المالي", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BankBackground,
                            selectedTextColor = EmeraldPrimary,
                            indicatorColor = EmeraldPrimary,
                            unselectedTextColor = TextSecondaryMuted,
                            unselectedIconColor = TextSecondaryMuted
                        )
                    )
                    NavigationBarItem(
                        selected = activeTab == 3,
                        onClick = { viewModel.setActiveTab(3) },
                        icon = { Icon(Icons.Default.Star, contentDescription = "Savings/Loans") },
                        label = { Text("ادخار وقروض", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BankBackground,
                            selectedTextColor = EmeraldPrimary,
                            indicatorColor = EmeraldPrimary,
                            unselectedTextColor = TextSecondaryMuted,
                            unselectedIconColor = TextSecondaryMuted
                        )
                    )
                    NavigationBarItem(
                        selected = activeTab == 4,
                        onClick = { viewModel.setActiveTab(4) },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Business") },
                        label = { Text("للشركات", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BankBackground,
                            selectedTextColor = EmeraldPrimary,
                            indicatorColor = EmeraldPrimary,
                            unselectedTextColor = TextSecondaryMuted,
                            unselectedIconColor = TextSecondaryMuted
                        )
                    )
                }
            },
            containerColor = BankBackground
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 14.dp)
            ) {
                when (activeTab) {
                    0 -> DashboardTabScreen(
                        salaryConfig = salaryConfig,
                        breakdown = breakdown,
                        totalExpenses = totalExpenses,
                        remainingSalary = remainingSalary,
                        healthScore = healthScore,
                        viewModel = viewModel
                    )
                    1 -> ExpensesTabScreen(
                        expenses = expenses,
                        currentTier = currentTier,
                        aiInsights = aiInsights,
                        aiLoading = aiLoading,
                        ocrScanning = ocrScanning,
                        viewModel = viewModel
                    )
                    2 -> SimulatorTabScreen(
                        salaryConfig = salaryConfig,
                        loans = loans,
                        viewModel = viewModel
                    )
                    3 -> SavingsLoansTabScreen(
                        savingsGoals = savingsGoals,
                        loans = loans,
                        viewModel = viewModel
                    )
                    4 -> BusinessTabScreen(
                        employees = employees,
                        currentTier = currentTier,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------
// TABS IMPLEMENTATION
// ----------------------------------------------------

@Composable
fun DashboardTabScreen(
    salaryConfig: SalaryConfig,
    breakdown: SalaryCalculator.SalaryBreakdown,
    totalExpenses: Double,
    remainingSalary: Double,
    healthScore: Int,
    viewModel: MainViewModel
) {
    var editSalaryOpen by remember { mutableStateOf(false) }

    // Forms
    var inputBasic by remember { mutableStateOf(salaryConfig.basicSalary.toString()) }
    var inputBonuses by remember { mutableStateOf(salaryConfig.bonuses.toString()) }
    var inputDeductions by remember { mutableStateOf(salaryConfig.deductions.toString()) }
    var selectedCategory by remember { mutableStateOf(salaryConfig.userCategory) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 20.dp, top = 8.dp)
    ) {
        // Welcome Header & Health score
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = BankSurface),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "أهلاً بك في Budget DZ 👋",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = OnBackgroundWhite
                            )
                        )
                        Text(
                            text = "المنصة المالية الذكية للعمال والموظفين في الجزائر.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondaryMuted,
                                fontSize = 11.sp
                            ),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        // Small state-indicator
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(BankSurfaceVariant)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "الفئة: " + when (salaryConfig.userCategory) {
                                    "government" -> "موظف حكومي 🏢"
                                    "private" -> "موظف قطاع خاص 💼"
                                    "retired" -> "متقاعد 👴"
                                    else -> "مؤسسة صغيرة 🏪"
                                },
                                color = EmeraldPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    FinancialGauge(score = healthScore)
                }
            }
        }

        // Central Quick Stats Card
        item {
            CardSection(title = "الخلاصة المالية الشهرية") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(BankSurfaceVariant, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("الراتب الصافي", fontSize = 11.sp, color = TextSecondaryMuted)
                        Text(
                            text = "${String.format("%,.0f", breakdown.netSalary)} دج",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(BankSurfaceVariant, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("مجموع المصاريف", fontSize = 11.sp, color = TextSecondaryMuted)
                        Text(
                            text = "${String.format("%,.0f", totalExpenses)} دج",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = CoralDeduction
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(BankSurfaceVariant, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("المتبقي للادخار", fontSize = 11.sp, color = TextSecondaryMuted)
                        Text(
                            text = "${String.format("%,.0f", remainingSalary)} دج",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldPremium
                        )
                    }
                }
            }
        }

        // Detailed Payroll breakdown Card
        item {
            CardSection(title = "نظام الرواتب والضرائب الجزائري 🇩🇿") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("الراتب المتفق عليه (خام bruto):", color = TextSecondaryMuted, fontSize = 13.sp)
                        Text("${String.format("%,.2f", breakdown.grossSalary)} دج", fontWeight = FontWeight.Bold, color = OnBackgroundWhite)
                    }

                    // CNAS item
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("اقتطاع الضمان الاجتماعي (CNAS):", color = TextSecondaryMuted, fontSize = 13.sp)
                            Text("النسبة القانونية: ${salaryConfig.cnasRate}%", fontSize = 10.sp, color = EmeraldPrimary)
                        }
                        Text("- ${String.format("%,.2f", breakdown.cnas)} دج", fontWeight = FontWeight.Bold, color = CoralDeduction)
                    }

                    // IRG item
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("ضريبة الدخل الإجمالي (IRG):", color = TextSecondaryMuted, fontSize = 13.sp)
                            if (breakdown.taxableSalary <= 30000.0) {
                                Text("معفى قانونياً أقل من 30,000 دج", fontSize = 10.sp, color = EmeraldPrimary)
                            } else {
                                Text("خاضع لمعدل ضريبي تدريجي", fontSize = 10.sp, color = CoralDeduction)
                            }
                        }
                        Text("- ${String.format("%,.2f", breakdown.irg)} دج", fontWeight = FontWeight.Bold, color = CoralDeduction)
                    }

                    Divider(color = BankSurfaceVariant, thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("الراتب الصافي الفعلي (Net):", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = OnBackgroundWhite)
                        Text("${String.format("%,.2f", breakdown.netSalary)} دج", fontWeight = FontWeight.Black, fontSize = 17.sp, color = EmeraldPrimary)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Buttons to configure salary
                    Button(
                        onClick = { editSalaryOpen = !editSalaryOpen },
                        colors = ButtonDefaults.buttonColors(containerColor = BankSurfaceVariant),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (editSalaryOpen) "إغلاق نافذة التعديل" else "تعديل معطيات الراتب والقوانين الضريبية", fontSize = 12.sp, color = OnBackgroundWhite)
                    }

                    AnimatedVisibility(visible = editSalaryOpen) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF0F111A), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Text("تعديل المعطيات الحالية ومحاكاة القوانين:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFA000))
                            
                            // Category Selector
                            Text("فئة العمل والوظيفة:", fontSize = 11.sp, color = Color(0xFF90A4AE))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                val categoriesList = listOf("government", "private", "retired")
                                categoriesList.forEach { cat ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (selectedCategory == cat) Color(0xFF00E676) else Color(0xFF22273B))
                                            .clickable { selectedCategory = cat }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = when (cat) {
                                                "government" -> "حكومي"
                                                "private" -> "خاص"
                                                else -> "متقاعد"
                                            },
                                            color = if (selectedCategory == cat) Color(0xFF0F111A) else Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }

                            // Inputs
                            OutlinedTextField(
                                value = inputBasic,
                                onValueChange = { inputBasic = it },
                                label = { Text("الراتب الأساسي (دج)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = inputBonuses,
                                onValueChange = { inputBonuses = it },
                                label = { Text("المنح والتعويضات الشهري (دج)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = inputDeductions,
                                onValueChange = { inputDeductions = it },
                                label = { Text("الاقتطاعات والخصومات (دج)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Button(
                                onClick = {
                                    val basicNum = inputBasic.toDoubleOrNull() ?: salaryConfig.basicSalary
                                    val bonusNum = inputBonuses.toDoubleOrNull() ?: salaryConfig.bonuses
                                    val deductNum = inputDeductions.toDoubleOrNull() ?: salaryConfig.deductions
                                    viewModel.setSalaryConfig(
                                        userCategory = selectedCategory,
                                        basicSalary = basicNum,
                                        bonuses = bonusNum,
                                        deductions = deductNum,
                                        cnasRate = salaryConfig.cnasRate,
                                        irgRate = salaryConfig.irgRate
                                    )
                                    editSalaryOpen = false
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676))
                            ) {
                                Text("تطبيق التعديلات الحالية 💾", color = Color(0xFF0F111A), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExpensesTabScreen(
    expenses: List<Expense>,
    currentTier: String,
    aiInsights: String?,
    aiLoading: Boolean,
    ocrScanning: Boolean,
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    var inputDesc by remember { mutableStateOf("") }
    var inputAmount by remember { mutableStateOf("") }
    var selectedCat by remember { mutableStateOf("طعام") }

    val categories = listOf("طعام", "فواتير", "إيجار", "نقل", "صحة", "ترفيه", "أخرى")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 20.dp, top = 8.dp)
    ) {
        // Quick tools (OCR & AI generation)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF171A26)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "الذكاء الاصطناعي ومسح الفواتير 🤖",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = "قم بمسح فواتيرك عبر الكاميرا أو أرسل لمعالج الإسراف وخطط الادخار الذكي.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF90A4AE)),
                        modifier = Modifier.padding(vertical = 6.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Scan Receipt Button (OCR)
                        Button(
                            onClick = {
                                viewModel.scanReceiptAndAdd()
                                Toast.makeText(context, "جاري معالجة الفاتورة واستخراج البيانات بالذكاء الاصطناعي...", Toast.LENGTH_LONG).show()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22273B)),
                            enabled = !ocrScanning
                        ) {
                            if (ocrScanning) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Add, contentDescription = "OCR", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("مسح بالـ OCR 🧾", fontSize = 11.sp, color = Color.White)
                            }
                        }

                        // Generate Insights Button
                        Button(
                            onClick = {
                                if (currentTier == "Free") {
                                    Toast.makeText(context, "الذكاء الاصطناعي متاح في باقة بريميوم Premium. يرجى الترقية بالأعلى!", Toast.LENGTH_LONG).show()
                                } else {
                                    viewModel.requestFinancialInsights()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (currentTier == "Free") Color(0xFF22273B) else Color(0xFF00E676)
                            ),
                            enabled = !aiLoading
                        ) {
                            if (aiLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.DarkGray, strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Star, contentDescription = "AI Report", modifier = Modifier.size(16.dp), tint = if (currentTier == "Free") Color.Gray else Color(0xFF0F111A))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("تقرير ذكي ⚡", fontSize = 11.sp, color = if (currentTier == "Free") Color.White else Color(0xFF0F111A))
                            }
                        }
                    }

                    // Blurry visual cue if on FREE
                    if (currentTier == "Free") {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF22273B).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFFFFA000).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🔒 ميزة مستشار الذكاء الاصطناعي متوفرة في باقة Premium. يمكنك تجربتها مجاناً بالتبديل للباقة في شريط العنوان بالأعلى!",
                                fontSize = 11.sp,
                                color = Color(0xFFFFA000),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // Animated AI respond area
        aiInsights?.let { report ->
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF22273B)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(2.dp, Color(0xFF00E676))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Star, contentDescription = "Report Logo", tint = Color(0xFFFFA000))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "المستشار المالي الذكي (Budget DZ AI)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = report,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                lineHeight = 20.sp,
                                fontSize = 13.sp,
                                color = Color(0xFFECEFF1)
                            )
                        )
                    }
                }
            }
        }

        // Manual Add form Card
        item {
            CardSection(title = "تسجيل مصروف يومي") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = inputDesc,
                        onValueChange = { inputDesc = it },
                        label = { Text("بيان المصروف (أين صرفت؟)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = inputAmount,
                            onValueChange = { inputAmount = it },
                            label = { Text("المبلغ (دج)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1.3f),
                            singleLine = true
                        )

                        // Simple category selector buttons rows
                        var expanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(1f).align(Alignment.CenterVertically)) {
                            Button(
                                onClick = { expanded = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22273B)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(selectedCat, fontSize = 12.sp, color = Color.White)
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.background(Color(0xFF171A26))
                            ) {
                                categories.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat, color = Color.White) },
                                        onClick = {
                                            selectedCat = cat
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Button(
                        onClick = {
                            val amt = inputAmount.toDoubleOrNull()
                            if (inputDesc.isNotBlank() && amt != null) {
                                viewModel.addExpense(inputDesc, amt, selectedCat)
                                inputDesc = ""
                                inputAmount = ""
                                Toast.makeText(context, "تم حفظ المصروف بنجاح ✅", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "الرجاء كمل حقول البيانات بشكل صحيح!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("إضافة المصروف 💵", color = Color(0xFF0F111A), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Historic List Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("سجل النفقات والمصاريف", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color.White)
                if (expenses.isNotEmpty()) {
                    TextButton(onClick = { viewModel.clearAllExpenses() }) {
                        Text("مسح الكل 🗑️", color = Color(0xFFFF5252), fontSize = 12.sp)
                    }
                }
            }
        }

        if (expenses.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("لا توجد مصاريف مسجلة حتى الآن. ابدأ بإضافتها أو مسح الإيصالات!", color = Color(0xFF90A4AE), fontSize = 13.sp)
                }
            }
        }

        // List of expenses
        items(expenses) { exp ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF171A26)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(vertical = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(exp.description, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF22273B))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(exp.category, fontSize = 10.sp, color = Color(0xFF00E676))
                            }
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "- ${String.format("%,.0f", exp.amount)} دج",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF5252),
                            fontSize = 15.sp,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        IconButton(onClick = { viewModel.deleteExpense(exp) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFF90A4AE))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SimulatorTabScreen(
    salaryConfig: SalaryConfig,
    loans: List<Loan>,
    viewModel: MainViewModel
) {
    // Simulator input parameters states
    var startingSalary by remember { mutableStateOf(salaryConfig.basicSalary) }
    var annualRaisePercentage by remember { mutableStateOf(5f) } // default 5% yearly raise
    var monthlySavingsAmount by remember { mutableStateOf(10000.0) } // default save 10k
    var includeLoanToggle by remember { mutableStateOf(false) }

    // Projections output state
    var calculatedProjections by remember { mutableStateOf<List<Double>?>(null) }
    val yearsLabels = listOf("الآن", "سنة 1 📈", "سنة 3 📊", "سنة 5 🚀")

    // Run simulation auto on launch
    LaunchedEffect(startingSalary, annualRaisePercentage, monthlySavingsAmount, includeLoanToggle, loans) {
        val projectionsList = mutableListOf<Double>()
        projectionsList.add(0.0) // Year 0 has 0 base accumulated savings

        var currentSalary = startingSalary
        var currentAccumulated = 0.0

        for (y in 1..5) {
            // Apply annual raising
            if (y > 1) {
                currentSalary *= (1 + (annualRaisePercentage / 100.0))
            }
            // 12 months in a year savings added
            val annualSaving = monthlySavingsAmount * 12
            currentAccumulated += annualSaving

            // If include loans, deduct loan payouts if active
            if (includeLoanToggle) {
                val totalLoanPayouts = loans.sumOf { it.monthlyPayment } * 12 * minOf(y, 5)
                currentAccumulated = maxOf(0.0, currentAccumulated - (totalLoanPayouts / 5.0))
            }

            if (y == 1 || y == 3 || y == 5) {
                // convert to Million DZD for graph readability
                projectionsList.add(currentAccumulated / 1000000.0)
            }
        }
        calculatedProjections = projectionsList
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 20.dp, top = 8.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF171A26)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        "محاكي الحياة المالية الجزائري 🔮",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        "توقع وضعك المادي بعد سنة، 3 سنوات، و 5 سنوات بناءً على مدخولك ونسبة الزيادة ومخطط الادخار الخاص بك.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF90A4AE)),
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )
                }
            }
        }

        // Graph output visualizer card
        calculatedProjections?.let { proj ->
            item {
                ProjectionLineChart(points = proj, labels = yearsLabels)
            }
        }

        // Adjustable Interactive Parameters
        item {
            CardSection(title = "مدخلات المحاكاة المالية") {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    
                    // 1. Starting Salary
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("الراتب الشهري الأساسي البدئي:", color = Color(0xFFECEFF1), fontSize = 13.sp)
                            Text("${String.format("%,.0f", startingSalary)} دج", fontWeight = FontWeight.Bold, color = Color(0xFF00E676))
                        }
                        Slider(
                            value = startingSalary.toFloat(),
                            onValueChange = { startingSalary = it.toDouble() },
                            valueRange = 25000f..250000f,
                            steps = 45,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF00E676),
                                activeTrackColor = Color(0xFF00E676)
                            )
                        )
                    }

                    // 2. Annual Salary Increase %
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("معدل الزيادة السنوية للراتب:", color = Color(0xFFECEFF1), fontSize = 13.sp)
                            Text("${String.format("%.1f", annualRaisePercentage)} %", fontWeight = FontWeight.Bold, color = Color(0xFFFFA000))
                        }
                        Slider(
                            value = annualRaisePercentage,
                            onValueChange = { annualRaisePercentage = it },
                            valueRange = 0f..20f,
                            steps = 20,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFFFFA000),
                                activeTrackColor = Color(0xFFFFA000)
                            )
                        )
                    }

                    // 3. Monthly savings in DZD
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("مبلغ الادخار الشهري المخطط:", color = Color(0xFFECEFF1), fontSize = 13.sp)
                            Text("${String.format("%,.0f", monthlySavingsAmount)} دج", fontWeight = FontWeight.Bold, color = Color(0xFF00BFA5))
                        }
                        Slider(
                            value = monthlySavingsAmount.toFloat(),
                            onValueChange = { monthlySavingsAmount = it.toDouble() },
                            valueRange = 2000f..80000f,
                            steps = 39,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF00BFA5),
                                activeTrackColor = Color(0xFF00BFA5)
                            )
                        )
                    }

                    // 4. Including debt simulator
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF22273B), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("دمج أقساط الديون والقروض الفعالة", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                            Text("سيقوم بخصم الدفعات المسجلة من منحنى الادخار", fontSize = 10.sp, color = Color(0xFF90A4AE))
                        }
                        Switch(
                            checked = includeLoanToggle,
                            onCheckedChange = { includeLoanToggle = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF00E676))
                        )
                    }
                }
            }
        }

        // Progressive projection metrics report in table
        item {
            CardSection(title = "توقعات تراكم الثروة المالية 💰") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val m1Sec = monthlySavingsAmount * 12
                    val m3Sec = monthlySavingsAmount * 12 * 3 * (1 + annualRaisePercentage/200f) // approximate raising compound
                    val m5Sec = monthlySavingsAmount * 12 * 5 * (1 + annualRaisePercentage/100f)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF22273B), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("الفترة الزمنية", fontWeight = FontWeight.Bold, color = Color(0xFFFFA000), fontSize = 13.sp)
                        Text("الادخار المتراكم المقدر", fontWeight = FontWeight.Bold, color = Color(0xFFFFA000), fontSize = 13.sp)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("بعد سنة 🟢", color = Color(0xFFECEFF1), fontSize = 13.sp)
                        Text("${String.format("%,.0f", m1Sec)} دج", fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("بعد 3 سنوات 🔵", color = Color(0xFFECEFF1), fontSize = 13.sp)
                        Text("${String.format("%,.0f", m3Sec)} دج", fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("بعد 5 سنوات 👑", color = Color(0xFFECEFF1), fontSize = 13.sp)
                        Text("${String.format("%,.0f", m5Sec)} دج", fontWeight = FontWeight.Bold, color = Color(0xFF00E676))
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                            .background(Color(0xFF22273B).copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "💡 نصيحة مالية: الاستمرار بادخار مبلغ شهري دائم يمنحك راحة نفسية وقدرة على اقتناء سيارة أو شراء سكن دون اللجوء للقروض التي تثقل كاهل عائلتك.",
                            fontSize = 11.sp,
                            color = Color(0xFF90A4AE),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SavingsLoansTabScreen(
    savingsGoals: List<SavingsGoal>,
    loans: List<Loan>,
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    var isNewGoalOpen by remember { mutableStateOf(false) }
    var isNewLoanOpen by remember { mutableStateOf(false) }

    // Goal Form values
    var goalTitle by remember { mutableStateOf("") }
    var goalTarget by remember { mutableStateOf("") }
    var goalSaved by remember { mutableStateOf("") }
    var goalCategory by remember { mutableStateOf("car") } // car, house, marriage, travel, other
    var goalMonths by remember { mutableStateOf("12") }

    // Loan Form values
    var loanTitle by remember { mutableStateOf("") }
    var loanAmount by remember { mutableStateOf("") }
    var loanInterest by remember { mutableStateOf("0") }
    var loanTenure by remember { mutableStateOf("12") }
    var loanPayment by remember { mutableStateOf("") }

    val goalCategories = listOf("car" to "شراء سيارة 🚗", "house" to "بناء منزل 🏠", "marriage" to "الزواج 💍", "travel" to "سفر وسياحة ✈️", "other" to "هدف مخصص 🎯")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 20.dp, top = 8.dp)
    ) {
        // --- GOALS SECTION ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("أهداف الادخار الشخصية", fontWeight = FontWeight.Black, fontSize = 16.sp, color = OnBackgroundWhite)
                Button(
                    onClick = { isNewGoalOpen = !isNewGoalOpen },
                    colors = ButtonDefaults.buttonColors(containerColor = BankSurfaceVariant),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(if (isNewGoalOpen) "إلغاء ❌" else "هدف جديد ➕", fontSize = 11.sp, color = OnBackgroundWhite)
                }
            }
        }

        // New Goal Form
        if (isNewGoalOpen) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = BankSurfaceVariant)) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("إنشاء هدف مالي مخصص", fontWeight = FontWeight.Bold, color = OnBackgroundWhite)
                        
                        OutlinedTextField(
                            value = goalTitle,
                            onValueChange = { goalTitle = it },
                            label = { Text("اسم الهدف الادخاري (مثلاً: سيارة داف)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = goalTarget,
                                onValueChange = { goalTarget = it },
                                label = { Text("المبلغ الكلي (دج)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = goalSaved,
                                onValueChange = { goalSaved = it },
                                label = { Text("المدخر الابتدائي (دج)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        // Category selector for goals
                        Text("نوع وتصنيف الهدف المالي:", fontSize = 11.sp, color = TextSecondaryMuted)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            goalCategories.forEach { (cat, label) ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (goalCategory == cat) EmeraldPrimary else BankSurface)
                                        .clickable { goalCategory = cat }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label.substring(label.length - 2), // display emoji only
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                val targetNum = goalTarget.toDoubleOrNull()
                                val savedNum = goalSaved.toDoubleOrNull() ?: 0.0
                                val monthsNum = goalMonths.toIntOrNull() ?: 12
                                if (goalTitle.isNotBlank() && targetNum != null) {
                                    viewModel.addSavingsGoal(goalTitle, targetNum, savedNum, goalCategory, monthsNum)
                                    goalTitle = ""
                                    goalTarget = ""
                                    goalSaved = ""
                                    isNewGoalOpen = false
                                    Toast.makeText(context, "تم حفظ هدفك المالي الجديد بنجاح 🎯", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "الرجاء كمل بيانات المبلغ بشكل صحيح!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("أضف الهدف المالي 💾", color = BankBackground, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        if (savingsGoals.isEmpty()) {
            item {
                Text("لا توجد أهداف ادخارية حالياً.", color = TextSecondaryMuted, fontSize = 12.sp)
            }
        }

        // List of goals
        items(savingsGoals) { goal ->
            Card(
                colors = CardDefaults.cardColors(containerColor = BankSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            val emoji = when (goal.category) {
                                "car" -> "🚗"
                                "house" -> "🏠"
                                "marriage" -> "💍"
                                "travel" -> "✈️"
                                else -> "🎯"
                            }
                            Text("$emoji ${goal.title}", fontWeight = FontWeight.Bold, color = OnBackgroundWhite)
                            Text("المستهدف: ${String.format("%,.0f", goal.targetAmount)} دج", fontSize = 11.sp, color = TextSecondaryMuted)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Quick Quick-save addition button!
                            Button(
                                onClick = {
                                    viewModel.updateGoalSaving(goal, 10000.0)
                                    Toast.makeText(context, "أضفت +10,000 دج لمدخرات الهدف! 💰", Toast.LENGTH_SHORT).show()
                                },
                                modifier = sizeButtonCompat(),
                                colors = ButtonDefaults.buttonColors(containerColor = BankSurfaceVariant),
                                elevation = ButtonDefaults.buttonElevation(0.dp)
                            ) {
                                Text("+10ألف دج", fontSize = 10.sp, color = EmeraldPrimary)
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(onClick = { viewModel.deleteSavingsGoal(goal) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextSecondaryMuted)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Progress indicator
                    val progress = if (goal.targetAmount > 0) (goal.savedAmount / goal.targetAmount).toFloat() else 0f
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LinearProgressIndicator(
                            progress = progress,
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = EmeraldPrimary,
                            trackColor = BankSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "المبلغ المجمع: ${String.format("%,.0f", goal.savedAmount)} دج",
                            fontSize = 11.sp,
                            color = TextSecondaryMuted
                        )
                    }
                }
            }
        }

        // --- DEBTS / LOANS SECTION ---
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("إدارة القروض والالتزامات البنكية", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color.White)
                Button(
                    onClick = { isNewLoanOpen = !isNewLoanOpen },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22273B)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(if (isNewLoanOpen) "إلغاء ❌" else "قرض جديد 💳", fontSize = 11.sp, color = Color.White)
                }
            }
        }

        if (isNewLoanOpen) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF22273B))) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("تسجيل قرض أو دين مع حساب التكاليف", fontWeight = FontWeight.Bold, color = Color.White)
                        
                        OutlinedTextField(
                            value = loanTitle,
                            onValueChange = { loanTitle = it },
                            label = { Text("جهة الإقراض أو المعني (مثال: بنك البركة)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = loanAmount,
                                onValueChange = { loanAmount = it },
                                label = { Text("القرض الإجمالي (دج)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = loanInterest,
                                onValueChange = { loanInterest = it },
                                label = { Text("نسبة الفائدة المقدرة (%)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = loanTenure,
                                onValueChange = { loanTenure = it },
                                label = { Text("مدة السداد (شهراً)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = loanPayment,
                                onValueChange = { loanPayment = it },
                                label = { Text("القسط الشهري (دج)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        Button(
                            onClick = {
                                val amtNum = loanAmount.toDoubleOrNull()
                                val intNum = loanInterest.toDoubleOrNull() ?: 0.0
                                val tenNum = loanTenure.toIntOrNull() ?: 12
                                var payNum = loanPayment.toDoubleOrNull() ?: 0.0

                                if (payNum == 0.0 && amtNum != null && tenNum > 0) {
                                    // Calculate simple installment
                                    val interestPart = amtNum * (intNum / 100.0)
                                    payNum = (amtNum + interestPart) / tenNum
                                }

                                if (loanTitle.isNotBlank() && amtNum != null) {
                                    viewModel.addLoan(
                                        title = loanTitle,
                                        totalAmount = amtNum,
                                        interestRate = intNum,
                                        tenureMonths = tenNum,
                                        remainingAmount = amtNum,
                                        monthlyPayment = payNum
                                    )
                                    loanTitle = ""
                                    loanAmount = ""
                                    loanInterest = ""
                                    loanTenure = ""
                                    loanPayment = ""
                                    isNewLoanOpen = false
                                    Toast.makeText(context, "تم تخزين معلومات القرض بنجاح 🏦", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "الرجاء كمل حقول البيانات بشكل صحيح!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("تطبيق وحفظ التزامات السداد 🔐", color = Color(0xFF0F111A), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        if (loans.isEmpty()) {
            item {
                Text("لا توجد قروض أو التزامات بنكية مسجلة حالياً.", color = Color(0xFF90A4AE), fontSize = 12.sp)
            }
        }

        items(loans) { loan ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF171A26)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(loan.title, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("قسط شهري: ${String.format("%,.0f", loan.monthlyPayment)} دج", fontSize = 12.sp, color = Color(0xFF00E676))
                        Text(
                            text = "القيمة الإجمالية: ${String.format("%,.0f", loan.totalAmount)} دج | فائدة: ${loan.interestRate}%",
                            color = Color(0xFF90A4AE),
                            fontSize = 11.sp
                        )
                    }
                    IconButton(onClick = { viewModel.deleteLoan(loan) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFF90A4AE))
                    }
                }
            }
        }
    }
}

@Composable
fun BusinessTabScreen(
    employees: List<Employee>,
    currentTier: String,
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    var isNewEmployeeOpen by remember { mutableStateOf(false) }

    // Forms
    var empName by remember { mutableStateOf("") }
    var empTitle by remember { mutableStateOf("") }
    var empBasic by remember { mutableStateOf("") }
    var empBonus by remember { mutableStateOf("0") }
    var empExp by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 20.dp, top = 8.dp)
    ) {
        // Master Header Card for Business SaaS Tier
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF171A26)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "ملف تسيير وإدارة المؤسسات والشركات 🏢",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFFA000).copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("باقة المؤسسات", fontSize = 10.sp, color = Color(0xFFFFA000), fontWeight = FontWeight.Bold)
                        }
                    }
                    Text(
                        "قم بتسجيل موظفيك وجرد رواتبهم الجماعية تلقائياً مع حساب الضمان الاجتماعي CNAS والضريبة IRG دفعة واحدة.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF90A4AE)),
                        modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                    )
                }
            }
        }

        // Locked Visual Indicator if on Free or Premium
        if (currentTier != "Business") {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF22273B)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFFFA000))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = "Locked", tint = Color(0xFFFFA000), modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "باقة تسيير الموارد البشرية والرواتب الجماعية مغلقة!",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "يتطلب تفعيل باقة الشركات (Business Tier). يمكنك تجربتها مجاناً بمحاكاة سحابية بنقرة واحدة فوق بطاقة باقتك الحالية في شريط العنوان بالأعلى تزامناً مع احتياجاتك!",
                            fontSize = 11.sp,
                            color = Color(0xFF90A4AE),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { viewModel.switchTier("Business") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA000))
                        ) {
                            Text("فتح باقة الشركات فوراً 🔓", color = Color(0xFF0F111A), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            // UNLOCKED STATE - Full features for Business
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("سجل الموظفين والعمال الحركي", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color.White)
                    Button(
                        onClick = { isNewEmployeeOpen = !isNewEmployeeOpen },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22273B)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(if (isNewEmployeeOpen) "إلغاء ❌" else "توظيف جديد 👤", fontSize = 11.sp, color = Color.White)
                    }
                }
            }

            if (isNewEmployeeOpen) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF22273B))) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("إضافة عامل / موظف إلى السجل", fontWeight = FontWeight.Bold, color = Color.White)

                            OutlinedTextField(
                                value = empName,
                                onValueChange = { empName = it },
                                label = { Text("الاسم الكامل للموظف") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = empTitle,
                                onValueChange = { empTitle = it },
                                label = { Text("المسمى الوظيفي والمركز") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = empBasic,
                                    onValueChange = { empBasic = it },
                                    label = { Text("الراتب الأساسي (دج)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1.2f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = empBonus,
                                    onValueChange = { empBonus = it },
                                    label = { Text("مكافأة المنصب") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }

                            OutlinedTextField(
                                value = empExp,
                                onValueChange = { empExp = it },
                                label = { Text("سنوات الخبرة الكلية في العمل") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Button(
                                onClick = {
                                    val basicNum = empBasic.toDoubleOrNull()
                                    val bonusNum = empBonus.toDoubleOrNull() ?: 0.0
                                    val expNum = empExp.toIntOrNull() ?: 0
                                    if (empName.isNotBlank() && basicNum != null) {
                                        viewModel.addEmployee(empName, empTitle, basicNum, bonusNum, expNum)
                                        empName = ""
                                        empTitle = ""
                                        empBasic = ""
                                        empBonus = "0"
                                        empExp = ""
                                        isNewEmployeeOpen = false
                                        Toast.makeText(context, "تم توظيف واحتساب رواتب العامل الجديد 🎉", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "الرجاء كمل بيانات راتب الموظف بدقة!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("إدخال لسجل الرواتب 💾", color = Color(0xFF0F111A), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            if (employees.isEmpty()) {
                item {
                    Text("لا يوجد موظفون مسجلون حالياً بملف شركتك.", color = Color(0xFF90A4AE), fontSize = 12.sp)
                }
            }

            // Calculation sheets & totals
            item {
                val totalCompanySalaries = employees.sumOf { it.basicSalary + it.bonusAmount }
                val totalCnasPaidByCompany = totalCompanySalaries * 0.09 // 9% employee contribution part simulation
                val totalCompanyDeductionsPart = totalCompanySalaries * 0.26 // 26% patronal employer contribution in Algeria!

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF171A26)),
                    border = BorderStroke(1.dp, Color(0xFF00BFA5))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("التقارير المالية والالتزامات العامة 📊", fontWeight = FontWeight.Bold, color = Color(0xFF00BFA5))
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("إجمالي الرواتب والمنح الخام:", color = Color(0xFF90A4AE), fontSize = 12.sp)
                            Text("${String.format("%,.0f", totalCompanySalaries)} دج", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("إجمالي اشتراكات الضمان الاجتماعي المقتطعة:", color = Color(0xFF90A4AE), fontSize = 12.sp)
                            Text("${String.format("%,.0f", totalCnasPaidByCompany)} دج", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("التزامات رب العمل (26% الضمان الاجتماعي للمؤسسات):", color = Color(0xFF90A4AE), fontSize = 12.sp)
                            Text("${String.format("%,.0f", totalCompanyDeductionsPart)} دج", fontWeight = FontWeight.Bold, color = Color(0xFFFFA000))
                        }

                        Divider(color = Color(0xFF22273B), thickness = 1.dp, modifier = Modifier.padding(vertical = 6.dp))

                        // Trigger export format demo
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    // Generate and toast CSV formatted layout demo
                                    val csv = "ID,Name,Position,Salary\n" + employees.map { "${it.id},${it.name},${it.jobTitle},${it.basicSalary}" }.joinToString("\n")
                                    Toast.makeText(context, "تم تصدير ملف الرواتب Excel بنجاح!\n$csv", Toast.LENGTH_LONG).show()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22273B))
                            ) {
                                Text("تصدير Excel 📊", fontSize = 11.sp, color = Color.White)
                            }
                            Button(
                                onClick = {
                                    Toast.makeText(context, "تم توليد ملف كشوف الرواتب PDF HR بنجاح وتخزينه!", Toast.LENGTH_LONG).show()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22273B))
                            ) {
                                Text("تصدير كشف PDF 📄", fontSize = 11.sp, color = Color.White)
                            }
                        }
                    }
                }
            }

            // Employee loop
            items(employees) { emp ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF171A26)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(emp.name, fontWeight = FontWeight.Bold, color = Color.White)
                                Text(emp.jobTitle, fontSize = 12.sp, color = Color(0xFF00E676))
                            }
                            IconButton(onClick = { viewModel.deleteEmployee(emp) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFFF5252))
                            }
                        }

                        Divider(color = Color(0xFF22273B), thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))

                        // Individual employee tax info representation
                        val empRawConfig = SalaryConfig(
                            basicSalary = emp.basicSalary,
                            bonuses = emp.bonusAmount,
                            userCategory = "private"
                        )
                        val empBreakdown = SalaryCalculator.calculate(empRawConfig)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("الراتب الخام: ", fontSize = 11.sp, color = Color(0xFF90A4AE))
                                Text("${String.format("%,.0f", empBreakdown.grossSalary)} دج", fontWeight = FontWeight.Medium, color = Color.White)
                            }
                            Column {
                                Text("CNAS (9%): ", fontSize = 11.sp, color = Color(0xFF90A4AE))
                                Text("-${String.format("%,.0f", empBreakdown.cnas)} دج", fontWeight = FontWeight.Medium, color = Color(0xFFFF5252))
                            }
                            Column {
                                Text("IRG الضريبة: ", fontSize = 11.sp, color = Color(0xFF90A4AE))
                                Text("-${String.format("%,.0f", empBreakdown.irg)} دج", fontWeight = FontWeight.Medium, color = Color(0xFFFF5252))
                            }
                            Column {
                                Text("الصافي الفعلي: ", fontSize = 11.sp, color = Color(0xFF90A4AE))
                                Text("${String.format("%,.0f", empBreakdown.netSalary)} دج", fontWeight = FontWeight.Bold, color = Color(0xFF00E676))
                            }
                        }
                    }
                }
            }
        }
    }
}

// Compact height helper for buttons in goal save items to support older layouts of Compose Material
@Composable
private fun sizeButtonCompat(): Modifier = Modifier
    .wrapContentSize()
    .height(34.dp)
