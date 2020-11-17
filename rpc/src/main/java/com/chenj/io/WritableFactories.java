package com.chenj.io;

import java.util.HashMap;

public class WritableFactories {

    private static final HashMap<Class<?>,WritableFactory> CLASS_TO_FACTORY = new HashMap<>();

    private WritableFactories(){ }


    public static synchronized void setFactory(Class<?>  c , WritableFactory factory){
           CLASS_TO_FACTORY.put(c,factory);
    }


    public static synchronized  WritableFactory getFactory(Class<?> c){
           return  CLASS_TO_FACTORY.get(c);
    }

    public static Writable newInstance(Class<?> c){
        WritableFactory factory = CLASS_TO_FACTORY.get(c);
        if(factory != null){
            return  factory.newInstance();
        }else {
            try{
                return  (Writable) c.newInstance();
            }catch (InstantiationException | IllegalAccessException e){
                throw new RuntimeException(e);
            }

        }
    }
}
