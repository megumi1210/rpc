package com.chenj.io;

import java.io.DataInput;

import java.io.DataOutput;
import java.io.IOException;


/**
 *  基于二进制流简单高效的序列化协议
 *
 * @author  chenj
 */
public interface Writable {

    /**
     *  把一个对象的字段都写入到二进制流中
     */
    void write(DataOutput out) throws IOException;


    /**
     * 从二进制流中读取对象的字段
     */
    void readFields(DataInput in ) throws IOException;

}
