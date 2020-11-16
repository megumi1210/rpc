package com.chenj.io;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ObjectWritable implements Writable {

   private  Class<?>  declaredClass;
   private Object instance;

   private static final Map<String,Class<?>>  PRIMITIVE_NAMES = new HashMap<>();


   static {
        PRIMITIVE_NAMES.put("boolean",Boolean.TYPE);
        PRIMITIVE_NAMES.put("byte",Byte.TYPE);
        PRIMITIVE_NAMES.put("char",Character.TYPE);
        PRIMITIVE_NAMES.put("short",Short.TYPE);
        PRIMITIVE_NAMES.put("int",Integer.TYPE);
        PRIMITIVE_NAMES.put("long",Long.TYPE);
        PRIMITIVE_NAMES.put("float",Float.TYPE);
        PRIMITIVE_NAMES.put("double",Double.TYPE);
        PRIMITIVE_NAMES.put("void",Void.TYPE);
   }


   private static class NullInstance implements Writable{
       private Class<?>  declaredClass ;
       public NullInstance() {}

       public NullInstance(Class<?> declaredClass){
           this.declaredClass = declaredClass;
       }

       @Override
       public void write(DataOutput out) throws IOException {
             out.writeUTF(declaredClass.getName());
       }

       @Override
       public void readFields(DataInput in) throws IOException {
             String className = in.readUTF();
             declaredClass = PRIMITIVE_NAMES.get(className);
             if(declaredClass == null){
                  try{
                      declaredClass = Class.forName(className);
                  }catch (ClassNotFoundException e){
                      throw new RuntimeException(e.toString());
                  }
             }
       }
   }


    public ObjectWritable(Object instance) {
        this.instance = instance;
    }

    public ObjectWritable(Class<?> declaredClass, Object instance) {
        this.declaredClass = declaredClass;
        this.instance = instance;
    }

    public Object get(){return instance ;}


    public Class<?> getDeclaredClass(){return  declaredClass;}


    public void set(Object instance){
        this.declaredClass = instance.getClass();
        this.instance = instance;
    }


    @Override
    public void write(DataOutput out) throws IOException {

    }

    @Override
    public void readFields(DataInput in) throws IOException {

    }


    public static  void  writeObject(DataOutput out , Object instance ,Class<?> declaredClass) throws IOException{

       if(instance == null){//null
             instance = new NullInstance(declaredClass);
             declaredClass = NullInstance.class;
         }

         if(instance instanceof  Writable){//writable
              out.writeUTF(instance.getClass().getName());
              ((Writable) instance).write(out);
              return;
         }
    }
}
