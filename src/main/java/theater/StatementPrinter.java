package theater;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

/**
 * This class generates a statement for a given invoice of performances.
 * @author Jonathan Calvert
 */
public class StatementPrinter {
    private final Invoice invoice;
    private final Map<String, Play> plays;

    public StatementPrinter(Invoice invoice, Map<String, Play> plays) {
        this.invoice = invoice;
        this.plays = plays;
    }

    /**
     * Returns a formatted statement of the invoice associated with this printer.
     * @return the formatted statement
     * @throws RuntimeException if one of the play types is not known
     */
    public String statement() {

        final StringBuilder result = new StringBuilder("Statement for "
                + invoice.getCustomer() + System.lineSeparator());

        for (final Performance p : invoice.getPerformances()) {
            // print line for this order
            // 修复：修复 LineLength (行太长)，并使用 play.getName() 和 p.getAudience()
            result.append(String.format("  %s: %s (%s seats)%n",
                    getPlay(p).getName(), usd(getAmount(p)), p.getAudience()));
        }

        // 4. 最终追加 (Final Appends)
        // 修复：修复 LineLength (行太长)
        result.append(String.format("Amount owed is %s%n",
                usd(getTotalAmount())));
        result.append(String.format("You earned %s credits%n", getTotalVolumeCredits()));

        return result.toString();
    }

    private int getTotalAmount() {
        int totalAmount = 0;
        for (final Performance p : invoice.getPerformances()) {
            totalAmount += getAmount(p);
        }
        return totalAmount;
    }

    private int getTotalVolumeCredits() {
        int volumeCredits = 0;
        for (final Performance p : invoice.getPerformances()) {
            volumeCredits += getVolumeCredits(p);
        }
        return volumeCredits;
    }

    private static String usd(int totalAmount) {
        return NumberFormat.getCurrencyInstance(Locale.US).format(totalAmount / Constants.CENTS_PER_DOLLAR);
    }

    private int getVolumeCredits(Performance performance) {
        // 修复：使用 p.getAudience()
        int result = Math.max(performance.getAudience() - Constants.BASE_VOLUME_CREDIT_THRESHOLD, 0);
        // add extra credit for every five comedy attendees
        // 修复：使用 play.getType() 和 p.getAudience()
        if ("comedy".equals(getPlay(performance).getType())) {
            result += performance.getAudience() / Constants.COMEDY_EXTRA_VOLUME_FACTOR;
        }
        return result;
    }

    private Play getPlay(Performance performance) {
        return plays.get(performance.getPlayID());
    }

    private int getAmount(Performance performance) {
        int result = 0;

        // 2. 把 "play.getType()" 替换为 "getPlay(performance).getType()"
        switch (getPlay(performance).getType()) {
            case "tragedy":
                result = Constants.TRAGEDY_BASE_AMOUNT;
                // (这里的 performance.getAudience() 是正确的，不需要改)
                if (performance.getAudience() > Constants.TRAGEDY_AUDIENCE_THRESHOLD) {
                    result += Constants.TRAGEDY_OVER_BASE_CAPACITY_PER_PERSON
                            * (performance.getAudience() - Constants.TRAGEDY_AUDIENCE_THRESHOLD);
                }
                break;
            case "comedy":
                result = Constants.COMEDY_BASE_AMOUNT;
                // (这里的 performance.getAudience() 是正确的，不需要改)
                if (performance.getAudience() > Constants.COMEDY_AUDIENCE_THRESHOLD) {
                    result += Constants.COMEDY_OVER_BASE_CAPACITY_AMOUNT
                            + (Constants.COMEDY_OVER_BASE_CAPACITY_PER_PERSON
                            * (performance.getAudience() - Constants.COMEDY_AUDIENCE_THRESHOLD));
                }
                result += Constants.COMEDY_AMOUNT_PER_AUDIENCE * performance.getAudience();
                break;
            default:
                // 3. 把 "play.getType()" 替换为 "getPlay(performance).getType()"
                throw new RuntimeException(String.format("unknown type: %s", getPlay(performance).getType()));
        }
        return result;
    }
}

