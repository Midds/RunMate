package jmidds17.runningbuddy;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Created by James on 19/12/2016.
 *
 * 'round()' method is taken directly from(source below), and is used to round a double to a selected amount of decimal places.
 * This method is used in a number of different classes to round down numbers with an unhelpful amount of decimal points, usually to
 * make the GUI more presentable.
 *
 * Jonik (2010) Round a double to 2 decimal places
 * [stack overflow] 11 May. Available from
 * https://stackoverflow.com/questions/2808535/round-a-double-to-2-decimal-places [Accessed 18 December 2016].
 */
public class RoundNumber {
    static public double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
