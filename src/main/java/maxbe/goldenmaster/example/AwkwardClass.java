package maxbe.goldenmaster.example;

public class AwkwardClass {

    private final String entropy = "Xp5ABs8pm7Q1PpZ60B8b";

    public String doYourMagic(Integer index) {
        int prime = 31;
        int complicatedResult = 1;
        complicatedResult = prime * complicatedResult + entropy.hashCode() * index;
        return String.valueOf(complicatedResult);
    }

}
