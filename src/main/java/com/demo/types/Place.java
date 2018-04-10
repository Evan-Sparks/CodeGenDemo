package com.demo.types;

import com.demo.types.markers.Cache;
import com.demo.types.markers.Identifiable;

@Cache
public interface Place extends Identifiable {
    String getAddress();
    void setAddress(String name);
}
