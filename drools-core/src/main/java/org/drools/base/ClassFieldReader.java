/**
 * Copyright 2005 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.base;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.drools.common.InternalWorkingMemory;
import org.drools.core.util.ClassUtils;
import org.drools.spi.InternalReadAccessor;

/**
 * This provides access to fields, and what their numerical index/object type is.
 * This is basically a wrapper class around dynamically generated subclasses of
 * BaseClassFieldExtractor,
 *  which allows serialization by regenerating the accessor classes
 * when needed.
 *
 * @author Michael Neale
 */
public class ClassFieldReader
    implements
    Externalizable,
    InternalReadAccessor {
    /**
     *
     */
    private static final long              serialVersionUID = 510l;
    private String                         className;
    private String                         fieldName;
    private transient InternalReadAccessor reader;

    public ClassFieldReader() {

    }

    public ClassFieldReader(final String className,
                            final String fieldName) {
        this.className = className;
        this.fieldName = fieldName;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject( className );
        out.writeObject( fieldName );
    }

    public void readExternal(final ObjectInput is) throws ClassNotFoundException,
                                                  IOException {
        className = (String) is.readObject();
        fieldName = (String) is.readObject();
    }

    public void setReadAccessor(InternalReadAccessor reader) {
        this.reader = reader;
    }

    public int getIndex() {
        return this.reader.getIndex();
    }

    public String getClassName() {
        return this.className;
    }

    public String getFieldName() {
        return this.fieldName;
    }

    public Object getValue(InternalWorkingMemory workingMemory,
                           final Object object) {
        return this.reader.getValue( workingMemory,
                                     object );
    }

    public ValueType getValueType() {
        return this.reader.getValueType();
    }

    public Class< ? > getExtractToClass() {
        return this.reader.getExtractToClass();
    }

    public String getExtractToClassName() {
        return ClassUtils.canonicalName( this.reader.getExtractToClass() );
    }

    public String toString() {
        return "[ClassFieldExtractor class=" + this.className + " field=" + this.fieldName + "]";
    }

    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((className == null) ? 0 : className.hashCode());
        result = prime * result + ((fieldName == null) ? 0 : fieldName.hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if ( this == obj ) return true;
        if ( obj == null ) return false;
        if ( !(obj instanceof ClassFieldReader) ) return false;
        ClassFieldReader other = (ClassFieldReader) obj;
        if ( className == null ) {
            if ( other.className != null ) return false;
        } else if ( !className.equals( other.className ) ) return false;
        if ( fieldName == null ) {
            if ( other.fieldName != null ) return false;
        } else if ( !fieldName.equals( other.fieldName ) ) return false;
        return true;
    }

    public boolean getBooleanValue(InternalWorkingMemory workingMemory,
                                   final Object object) {
        return this.reader.getBooleanValue( workingMemory,
                                            object );
    }

    public byte getByteValue(InternalWorkingMemory workingMemory,
                             final Object object) {
        return this.reader.getByteValue( workingMemory,
                                         object );
    }

    public char getCharValue(InternalWorkingMemory workingMemory,
                             final Object object) {
        return this.reader.getCharValue( workingMemory,
                                         object );
    }

    public double getDoubleValue(InternalWorkingMemory workingMemory,
                                 final Object object) {
        return this.reader.getDoubleValue( workingMemory,
                                           object );
    }

    public float getFloatValue(InternalWorkingMemory workingMemory,
                               final Object object) {
        return this.reader.getFloatValue( workingMemory,
                                          object );
    }

    public int getIntValue(InternalWorkingMemory workingMemory,
                           final Object object) {
        return this.reader.getIntValue( workingMemory,
                                        object );
    }

    public long getLongValue(InternalWorkingMemory workingMemory,
                             final Object object) {
        return this.reader.getLongValue( workingMemory,
                                         object );
    }

    public short getShortValue(InternalWorkingMemory workingMemory,
                               final Object object) {
        return this.reader.getShortValue( workingMemory,
                                          object );
    }

    public boolean isNullValue(InternalWorkingMemory workingMemory,
                               final Object object) {
        return this.reader.isNullValue( workingMemory,
                                        object );
    }

    public Method getNativeReadMethod() {
        return this.reader.getNativeReadMethod();
    }

    public int getHashCode(InternalWorkingMemory workingMemory,
                           final Object object) {
        return this.reader.getHashCode( workingMemory,
                                        object );
    }

    public boolean isGlobal() {
        return false;
    }

    public boolean isSelfReference() {
        return "this".equals( this.fieldName );
    }

    /**
     * @param object
     * @return
     * @see org.drools.spi.ReadAccessor#getBooleanValue(java.lang.Object)
     */
    public boolean getBooleanValue(Object object) {
        return reader.getBooleanValue( object );
    }

    /**
     * @param object
     * @return
     * @see org.drools.spi.ReadAccessor#getByteValue(java.lang.Object)
     */
    public byte getByteValue(Object object) {
        return reader.getByteValue( object );
    }

    /**
     * @param object
     * @return
     * @see org.drools.spi.ReadAccessor#getCharValue(java.lang.Object)
     */
    public char getCharValue(Object object) {
        return reader.getCharValue( object );
    }

    /**
     * @param object
     * @return
     * @see org.drools.spi.ReadAccessor#getDoubleValue(java.lang.Object)
     */
    public double getDoubleValue(Object object) {
        return reader.getDoubleValue( object );
    }

    /**
     * @param object
     * @return
     * @see org.drools.spi.ReadAccessor#getFloatValue(java.lang.Object)
     */
    public float getFloatValue(Object object) {
        return reader.getFloatValue( object );
    }

    /**
     * @param object
     * @return
     * @see org.drools.spi.ReadAccessor#getHashCode(java.lang.Object)
     */
    public int getHashCode(Object object) {
        return reader.getHashCode( object );
    }

    /**
     * @param object
     * @return
     * @see org.drools.spi.ReadAccessor#getIntValue(java.lang.Object)
     */
    public int getIntValue(Object object) {
        return reader.getIntValue( object );
    }

    /**
     * @param object
     * @return
     * @see org.drools.spi.ReadAccessor#getLongValue(java.lang.Object)
     */
    public long getLongValue(Object object) {
        return reader.getLongValue( object );
    }

    /**
     * @param object
     * @return
     * @see org.drools.spi.ReadAccessor#getShortValue(java.lang.Object)
     */
    public short getShortValue(Object object) {
        return reader.getShortValue( object );
    }

    /**
     * @param object
     * @return
     * @see org.drools.spi.ReadAccessor#getValue(java.lang.Object)
     */
    public Object getValue(Object object) {
        return reader.getValue( object );
    }

    /**
     * @param object
     * @return
     * @see org.drools.spi.ReadAccessor#isNullValue(java.lang.Object)
     */
    public boolean isNullValue(Object object) {
        return reader.isNullValue( object );
    }

    public BigDecimal getBigDecimalValue(InternalWorkingMemory workingMemory,
                                         Object object) {
        return reader.getBigDecimalValue( workingMemory,
                                          object );
    }

    public BigInteger getBigIntegerValue(InternalWorkingMemory workingMemory,
                                         Object object) {
        return reader.getBigIntegerValue( workingMemory,
                                          object );
    }

    public BigDecimal getBigDecimalValue(Object object) {
        return reader.getBigDecimalValue( object );
    }

    public BigInteger getBigIntegerValue(Object object) {
        return reader.getBigIntegerValue( object );
    }

}
