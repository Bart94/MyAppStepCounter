package com.example.barto.stepcounterbart;

public class Person {
    String name, surname, sex, birth, weight, height, steps;

    public Person(String name, String surname, String sex, String birth, String weight, String height, String steps) {
        this.name = name;
        this.surname = surname;
        this.sex = sex;
        this.birth = birth;
        this.weight = weight;
        this.height = height;
        this.steps = steps;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public String getSex() {
        return sex;
    }

    public String getBirth() {
        return birth;
    }

    public String getWeight() {
        return weight;
    }

    public String getHeight() {
        return height;
    }

    public String getSteps() {
        return steps;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public void setBirth(String birth) {
        this.birth = birth;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public void setSteps(String steps) {
        this.steps = steps;
    }

    @Override
    public String toString() {
        return "Utente: " + name + " " + surname + ", Sesso: " + sex + ", Data di Nascita: " + birth +
                ", Peso: " + weight + " Kg, Altezza: " + height + " cm, Passi Oggi: " + steps;
    }
}
