package com.example.danmarkskort;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class AddressParser {

    String[] address; // street, house, postcode, city
    private final static String REGEX = "(?<fulladdress>(?<street>[0-9]*[A-Za-zæøåÆØÅ. ]+) (?<house>[0-9]+[A-Z]?)(,)*(?<floorandside>.)* *(?<postcode>[0-9]{4}) (?<city>[A-Za-zÆÅØåæø ]+))|(?<roadNumberCity>(?<street2>[0-9]*[A-Za-zæøåÆØÅ. ]+) (?<house2>[0-9]+[A-Z]?)(,)* (?<city2>[A-Za-zÆÅØåæø ]+))|(?<roadHouseNo>(?<street3>[0-9]*[A-Za-zæøåÆØÅ. ]+) (?<house3>[0-9]+[A-Z]?))|(?<roadOrCityOnly>(?<street4>[0-9]*[A-Za-zæøåÆØÅ. ]+))|(?<postcodeOnly>(?<postcode1>[0-9]{4}))";
    private final static Pattern PATTERN = Pattern.compile(REGEX);

    public void parseAddress(String input) {

        Matcher matcher = PATTERN.matcher(input);

        if (matcher.matches()) {
            address[0] = matcher.group("street");
            address[1] = matcher.group("house");
            address[2] = matcher.group("postcode");
            address[3] = matcher.group("city");
        }

    }

    public String[] getAddress() {
        return address;
    }


}