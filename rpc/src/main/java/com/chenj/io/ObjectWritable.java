package com.chenj.io;

import lombok.extern.log4j.Log4j;

import javax.management.RuntimeOperationsException;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Array;
import java.rmi.server.ExportException;
import java.util.HashMap;
import java.util.Map;

@Log4j
public class ObjectWritable implements Writable {

  private Class<?> declaredClass;
  private Object instance;

  private static final Map<String, Class<?>> PRIMITIVE_NAMES = new HashMap<>();

  static {
    PRIMITIVE_NAMES.put("boolean", Boolean.TYPE);
    PRIMITIVE_NAMES.put("byte", Byte.TYPE);
    PRIMITIVE_NAMES.put("char", Character.TYPE);
    PRIMITIVE_NAMES.put("short", Short.TYPE);
    PRIMITIVE_NAMES.put("int", Integer.TYPE);
    PRIMITIVE_NAMES.put("long", Long.TYPE);
    PRIMITIVE_NAMES.put("float", Float.TYPE);
    PRIMITIVE_NAMES.put("double", Double.TYPE);
    PRIMITIVE_NAMES.put("void", Void.TYPE);
  }

  public ObjectWritable(Object instance) {
    this.instance = instance;
  }

  public ObjectWritable(Class<?> declaredClass, Object instance) {
    this.declaredClass = declaredClass;
    this.instance = instance;
  }

  // 空对象
  private static class NullInstance implements Writable {
    private Class<?> declaredClass;

    public NullInstance() {}

    public NullInstance(Class<?> declaredClass) {
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
      if (declaredClass == null) {
        try {
          declaredClass = Class.forName(className);
        } catch (ClassNotFoundException e) {
          throw new RuntimeException(e.toString());
        }
      }
    }
  }

  public Object get() {
    return instance;
  }

  public Class<?> getDeclaredClass() {
    return declaredClass;
  }

  public void set(Object instance) {
    this.declaredClass = instance.getClass();
    this.instance = instance;
  }

  @Override
  public void write(DataOutput out) throws IOException {
      writeObject(out,instance,declaredClass);
  }

  @Override
  public void readFields(DataInput in) throws IOException {
      readObject(in,this);
  }

  public static void writeObject(DataOutput out, Object instance, Class<?> declaredClass)
      throws IOException {

    if (instance == null) { // null
      instance = new NullInstance(declaredClass);
      declaredClass = NullInstance.class;
    }

    if (instance instanceof Writable) { // writable
      out.writeUTF(instance.getClass().getName());
      ((Writable) instance).write(out);
      return;
    }

    out.writeUTF(declaredClass.getName());

    if (declaredClass.isArray()) { // array

      int length = Array.getLength(instance);
      out.writeInt(length);
      for (int i = 0; i < length; i++) {
        writeObject(out, Array.get(instance, i), declaredClass.getComponentType());
      }

    } else if (declaredClass == String.class) { // string

      out.writeUTF((String) instance);

    } else if (declaredClass.isPrimitive()) { // primitive

      if (declaredClass == Boolean.TYPE) {
        out.writeBoolean((Boolean) instance);
      } else if (declaredClass == Character.TYPE) {
        out.writeChar((Character) instance);
      } else if (declaredClass == Byte.TYPE) {
        out.writeByte((Byte) instance);
      } else if (declaredClass == Short.TYPE) {
        out.writeShort((Short) instance);
      } else if (declaredClass == Integer.TYPE) {
        out.writeInt((Integer) instance);
      } else if (declaredClass == Long.TYPE) {
        out.writeLong((Long) instance);
      } else if (declaredClass == Float.TYPE) {
        out.writeFloat((Float) instance);
      } else if (declaredClass == Double.TYPE) {
        out.writeDouble((Double) instance);
      } else if (declaredClass == Void.TYPE) {
      } else {
        throw new IllegalArgumentException("Not a primitive: " + declaredClass);
      }
    } else {
      throw new IOException("Can't write " + instance + " as " + declaredClass);
    }
  }

  public static Object readObject(DataInput in) throws IOException {
    return readObject(in, null);
  }

  public static Object readObject(DataInput in, ObjectWritable objectWritable) throws IOException {
    String className = in.readUTF();

    // if primitive
    Class<?> declaredClass = PRIMITIVE_NAMES.get(className);
    if (null == declaredClass) {
      try {
        declaredClass = Class.forName(className);
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(e.toString());
      }
    }

    Object instance;

    if (declaredClass == NullInstance.class) { // null
      NullInstance wrapper = new NullInstance();
      wrapper.readFields(in);
      declaredClass = wrapper.declaredClass;
      instance = null;
    } else if (declaredClass.isPrimitive()) {

      if (declaredClass == Boolean.TYPE) {
        instance = in.readBoolean();
      } else if (declaredClass == Character.TYPE) {
        instance = in.readChar();
      } else if (declaredClass == Byte.TYPE) {
        instance = in.readByte();
      } else if (declaredClass == Short.TYPE) {
        instance = in.readShort();
      } else if (declaredClass == Integer.TYPE) {
        instance = in.readInt();
      } else if (declaredClass == Long.TYPE) {
        instance = in.readLong();
      } else if (declaredClass == Float.TYPE) {
        instance = in.readFloat();
      } else if (declaredClass == Double.TYPE) {
        instance = in.readDouble();
      } else if (declaredClass == Void.TYPE) {
        instance = null;
      } else {
        throw new IllegalArgumentException("Not a primitive " + declaredClass);
      }

    } else if (declaredClass.isArray()) {
      int length = in.readInt();
      instance = Array.newInstance(declaredClass.getComponentType(), length);
      for (int i = 0; i < length; i++) {
        Array.set(instance, i, readObject(in));
      }
    } else if (declaredClass == String.class) {
      instance = in.readUTF();
    } else {
      Writable writable = WritableFactories.newInstance(declaredClass);
      writable.readFields(in);
      instance = writable;
    }

    if (objectWritable != null) {
      objectWritable.declaredClass = declaredClass;
      objectWritable.instance = instance;
    }
    return instance;
  }
}
