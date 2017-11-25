package maxbe.goldenmaster.example;

import java.util.Random;

public class AwkwardClass {

    private final String entropy = "Xp5ABs8pm7Q1PpZ60B8b";

    public String doYourMagic(Integer index) {
        int base = new Random(index).nextInt(123456);
        int complicatedResult = 1;
        complicatedResult = base * complicatedResult + entropy.hashCode() * index;
        return String.valueOf(complicatedResult);
    }

}
