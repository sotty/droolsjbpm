package org.drools;

import java.io.Serializable;

public class FactC implements Serializable {
    String  f1;
    Integer f2;
    Float   f3;

    public FactC(final String a,
                 final Integer b,
                 final Float c) {
        this.f1 = a;
        this.f2 = b;
        this.f3 = c;
    }

    public FactC() {

    }

    public FactC( String f1 ) {
        this.f1 = f1;
    }
    
    public FactC( final Integer b ) {
        this.f2 = b;
    }    

    public String getF1() {
        return this.f1;
    }

    public void setF1(final String s) {
        this.f1 = s;
    }

    public Integer getF2() {
        return this.f2;
    }

    public void setF2(final Integer i) {
        this.f2 = i;
    }

    public Float getF3() {
        return this.f3;
    }

    public void setF3(final Float f) {
        this.f3 = f;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((f1 == null) ? 0 : f1.hashCode());
        result = prime * result + ((f2 == null) ? 0 : f2.hashCode());
        result = prime * result + ((f3 == null) ? 0 : f3.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj ) return true;
        if ( obj == null ) return false;
        if ( getClass() != obj.getClass() ) return false;
        final FactC other = (FactC) obj;
        if ( f1 == null ) {
            if ( other.f1 != null ) return false;
        } else if ( !f1.equals( other.f1 ) ) return false;
        if ( f2 == null ) {
            if ( other.f2 != null ) return false;
        } else if ( !f2.equals( other.f2 ) ) return false;
        if ( f3 == null ) {
            if ( other.f3 != null ) return false;
        } else if ( !f3.equals( other.f3 ) ) return false;
        return true;
    }

}
