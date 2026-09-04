package com.cartoonizer.rag.shared;

import java.time.LocalDate;

public class Person {

    private String firstName;
    private String lastName;
    private LocalDate birthDate;

    @Override
    public String toString() {
        return "Person {"
                + " firstName = \"" + firstName + "\""
                + ", lastName = \"" + lastName + "\""
                + ", birthDate = " + birthDate
                + " }";
    }
}
