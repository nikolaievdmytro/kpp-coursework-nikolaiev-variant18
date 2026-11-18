package ua.edu.nupp.kpp.server;

/**
 * Розрахунок індексу ризику порушення: I = 0,40·F1 + 0,35·F2 + 0,25·F3.
 * Усі фактори нормовані до діапазону 0..100.
 */
public class RiskCalculator {

    /**
     * F1 — площа прояву: частка площі ділянки, охопленої проявом, у відсотках.
     * Якщо розрахована частка перевищує 100, значення обмежується 100.
     */
    public static double factor1(double affectedAreaHa, double plotAreaHa) {
        if (Double.isNaN(affectedAreaHa) || Double.isNaN(plotAreaHa)
                || affectedAreaHa < 0 || plotAreaHa <= 0) {
            throw new IllegalArgumentException("Некоректні дані для F1");
        }
        double value = affectedAreaHa / plotAreaHa * 100.0;
        return Math.max(0.0, Math.min(100.0, value));
    }

    /** Індекс ризику, округлений до двох знаків після коми. */
    public static double calculate(double f1, double f2, double f3) {
        check(f1);
        check(f2);
        check(f3);
        return Math.round((0.40 * f1 + 0.35 * f2 + 0.25 * f3) * 100.0) / 100.0;
    }

    /** Чи належить значення фактора діапазону 0..100 (NaN не належить). */
    public static boolean isFactor(double value) {
        return value >= 0 && value <= 100;
    }

    private static void check(double value) {
        if (!isFactor(value)) {
            throw new IllegalArgumentException("Фактор має бути в діапазоні 0..100");
        }
    }
}
