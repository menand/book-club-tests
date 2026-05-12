package tests;

import java.util.UUID;
import net.datafaker.Faker;

public class Fakers {

    public static final Faker FAKER = new Faker();

    public static String shortUid() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
