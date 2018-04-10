package com.demo;

import com.demo.types.Person;
import com.demo.types.Place;

import java.util.Map;

public class CodeGenMain {
    public static void main(String[] args) {
        DataRepositoryGenerator dataRepositoryGenerator = new DataRepositoryGenerator();
        Map<Class, DataRepository> dataRepositories = dataRepositoryGenerator.GenerateRepositories();

        DataRepository<Person> personDataRepository = dataRepositories.get(Person.class);
        DataRepository<Place> placeDataRepository = dataRepositories.get(Place.class);

        System.out.println();

        // Create a place
        Place place = placeDataRepository.create("key2");
        place.setAddress("123FakeStreet");
        System.out.println("Created a Place with address " + place.getAddress());

        // Create a person
        Person person = personDataRepository.create("key1");
        person.setAge(123);
        person.setName("John Doe");
        System.out.println("Created a Person of age " + person.getAge() + ", named " + person.getName());

        // Setting edge from person to place
        person.setAddress(place);

        System.out.println();
        System.out.println("Person " + person.getName() + " lives at " + person.getAddress().getAddress());
    }
}
