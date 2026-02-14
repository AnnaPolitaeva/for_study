package api.generators;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

public class RandomData {
    private RandomData() {}

    public static String getUsername() {
        return RandomStringUtils.randomAlphabetic(10);
    }

    public static String getPassword() {
        return RandomStringUtils.randomAlphabetic(3).toUpperCase() +
                RandomStringUtils.randomAlphabetic(5).toLowerCase() +
                RandomStringUtils.randomNumeric(3) + "$" ;
    }

    public static String getName() {
        return RandomStringUtils.randomAlphabetic(5) + " " + RandomStringUtils.randomAlphabetic(5);
    }

    public static String getIncorrectName() {
        return RandomStringUtils.randomAlphabetic(8);
    }

    public static float getNegativeAmount() {
        return RandomUtils.nextFloat(-3000F, -5F);
    }

    public static float getSmallAmount() {
        return RandomUtils.nextFloat(0.01F, 5000.00F);
    }

    public static float getBigAmount() {
        return RandomUtils.nextFloat(10000.01F, 15000F);
    }
}
