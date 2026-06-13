package com.example.data.utils

import com.example.data.model.SalaryConfig

object SalaryCalculator {

    data class SalaryBreakdown(
        val basicSalary: Double,
        val bonuses: Double,
        val deductions: Double,
        val grossSalary: Double,
        val cnas: Double,
        val taxableSalary: Double,
        val irg: Double,
        val netSalary: Double
    )

    /**
     * Calculates the full salary breakdown following Algerian payroll standards.
     */
    fun calculate(config: SalaryConfig): SalaryBreakdown {
        val basic = config.basicSalary
        val bonuses = config.bonuses
        val deductions = config.deductions
        
        // 1. Gross Salary (الراتب الخام)
        val gross = basic + bonuses - deductions

        // 2. CNAS (الضمان الاجتماعي - 9% of Gross)
        val cnasRateDec = config.cnasRate / 100.0
        val cnas = gross * cnasRateDec

        // 3. Taxable Salary (الراتب الخاضع للضريبة)
        val taxable = maxOf(0.0, gross - cnas)

        // 4. IRG (ضريبة الدخل الإجمالي)
        val irg = calculateIRG(taxable, config.userCategory)

        // 5. Net Salary (الراتب الصافي)
        val net = maxOf(0.0, gross - cnas - irg)

        return SalaryBreakdown(
            basicSalary = basic,
            bonuses = bonuses,
            deductions = deductions,
            grossSalary = gross,
            cnas = cnas,
            taxableSalary = taxable,
            irg = irg,
            netSalary = net
        )
    }

    /**
     * Direct mathematical progressive calculation of monthly IRG in Algeria (Finance Law rules).
     */
    fun calculateIRG(taxable: Double, category: String): Double {
        // High-level exemption for retirees: In Algeria, pensions under 50,000 DZD are exempted from IRG entirely!
        if (category == "retired" && taxable <= 50000.0) {
            return 0.0
        }

        // Standard worker threshold: Net taxable salaries <= 30,000 DZD are 100% exempted from IRG.
        if (taxable <= 30000.0) {
            return 0.0
        }

        // 1. Standard Progressive scale matching modern bareme logic:
        // - Bracket 1: up to 20,000 DZD -> 0%
        // - Bracket 2: 20,001 to 40,000 DZD -> 23%
        // - Bracket 3: 40,001 to 80,000 DZD -> 27%
        // - Bracket 4: 80,001 to 160,000 DZD -> 30%
        // - Bracket 5: 160,001 to 320,000 DZD -> 33%
        // - Bracket 6: > 320,000 DZD -> 35%
        var rawIrg = 0.0

        if (taxable > 20000.0) {
            val b1 = minOf(taxable, 40000.0) - 20000.0
            rawIrg += b1 * 0.23
        }
        if (taxable > 40000.0) {
            val b2 = minOf(taxable, 80000.0) - 40000.0
            rawIrg += b2 * 0.27
        }
        if (taxable > 80000.0) {
            val b3 = minOf(taxable, 160000.0) - 80000.0
            rawIrg += b3 * 0.30
        }
        if (taxable > 160000.0) {
            val b4 = minOf(taxable, 320000.0) - 160000.0
            rawIrg += b4 * 0.33
        }
        if (taxable > 320000.0) {
            val b5 = taxable - 320000.0
            rawIrg += b5 * 0.35
        }

        // 2. Abatement (Rebate) of 40%
        val abatement = rawIrg * 0.40
        // Abatement must be between 1,000 DZD and 1,500 DZD.
        val finalAbatement = when {
            abatement < 1000.0 -> 1000.0
            abatement > 1500.0 -> 1500.0
            else -> abatement
        }

        var irgWithAbatement = maxOf(0.0, rawIrg - finalAbatement)

        // 3. Smoothing / Transitional coefficient for salaries between 30,000 DZD and 35,000 DZD
        if (taxable in 30001.0..35000.0) {
            // Apply transitional proportional coefficient
            irgWithAbatement = irgWithAbatement * (taxable - 30000.0) / 5000.0
        }

        return Math.round(irgWithAbatement).toDouble()
    }
}
