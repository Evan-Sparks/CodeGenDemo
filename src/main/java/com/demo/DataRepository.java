package com.demo;

import com.demo.db.MapDb;
import com.demo.types.markers.Identifiable;
import javassist.CannotCompileException;
import javassist.NotFoundException;

import java.lang.reflect.InvocationTargetException;

/**
 * A repository object which allows the creation of new instances of a generated class, and reading/writing those
 * objects from the backing database.
 * @param <T>
 */
public class DataRepository<T extends Identifiable> {
    private final Class type;
    private final Class interfaceType;
    private final MapDb dbs;
    DataObjectClassGenerator generator = new DataObjectClassGenerator();

    public DataRepository(Class interfaceType, MapDb dbs) throws CannotCompileException, NotFoundException {
        this.type = generator.implement(interfaceType);
        this.interfaceType = interfaceType;
        this.dbs = dbs;
    }

    T create(String key) {
        T instance;
        try {
            instance = (T) type.getDeclaredConstructor(MapDb.class).newInstance(dbs);
            instance.setId(key);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Failed to instantiate " + type.getName() + " with key " + key, e);
        }
        set(instance);
        return instance;
    }

    T get(String key) {
        return (T) dbs.getDb(interfaceType).get(key);
    }

    void set(T val) {
        dbs.getDb(interfaceType).put(val.getId(), val);
    }
}
