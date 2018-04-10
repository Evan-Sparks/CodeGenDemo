package com.demo.types;


import com.demo.types.markers.Cache;
import com.demo.types.markers.Edge;
import com.demo.types.markers.Identifiable;

@Cache
public interface Person extends Identifiable {
    String getName();
    void setName(String name);

    int getAge();
    void setAge(int age);

    @Edge
    Place getAddress();
    void setAddress(Place address);
}
