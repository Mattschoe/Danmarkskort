package com.example.danmarkskort.AddressSearch;

import com.example.danmarkskort.MapObjects.Node;

import java.util.ArrayList;
import java.util.List;
//TEST KLASSE
public class City {
    //region fields
    private final String cityName;
    private List<Street> streets;
    //endregion

    /**
     * A Street is a list of nodes related to that street-name
     * @param cityName the name of this street
     */
    public City(String cityName) {
        this.cityName = cityName;
        streets = new ArrayList<>();
    }

    //region getters and setters
    /**
     * Adds a Street to the City
     * @param street the street that will be associated with this City
     */
    public void addCity(Street street) { streets.add(street); }
    /**
     * @return The name of the city. For example "Helsingør"
     */
    public String getCityName() { return cityName; }
    /**
     * @return all the nodes associated with this Street
     */
    public List<Street> getStreets() { return streets; }
    //endregion

}

