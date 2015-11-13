//  Copyright (c) 2007-2008 Adrian Kuhn <akuhn(a)iam.unibe.ch>
//  
//  This file is part of 'Fame (for Java)'.
//  
//  'Fame (for Java)' is free software: you can redistribute it and/or modify
//  it under the terms of the GNU Lesser General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or (at your
//  option) any later version.
//  
//  'Fame (for Java)' is distributed in the hope that it will be useful, but
//  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
//  or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
//  License for more details.
//  
//  You should have received a copy of the GNU Lesser General Public License
//  along with 'Fame (for Java)'. If not, see <http://www.gnu.org/licenses/>.
//  

package ch.akuhn.fame.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import ch.akuhn.fame.FameProperty;
import ch.akuhn.util.Throw;

public class MethodAccess extends Access {

    private final Method getter;
    private final Method setter;

    public MethodAccess(Method m) {
        super(m.getGenericReturnType());
        this.getter = m;
        this.getter.setAccessible(true);
        this.setter = this.setterMethod();
    }

    public MethodAccess(Method getter, Method setter) {
        super(getter.getGenericReturnType());
        this.getter = getter;
        this.getter.setAccessible(true);
        this.setter = setter;
        this.setter.setAccessible(true);
    }
    
    @Override
    public FameProperty getAnnotation() {
        assert getter.getParameterTypes().length == 0 : getter;
        return getter.getAnnotation(FameProperty.class);
    }

    @Override
    public String getName() {
        return getter.getName();
    }

    @Override
    public Object read(Object element) {
        try {
            return getter.invoke(element);
        } catch (IllegalArgumentException ex) {
            throw Throw.exception(ex);
        } catch (IllegalAccessException ex) {
            throw Throw.exception(ex);
        } catch (InvocationTargetException ex) {
            throw Throw.exception(ex.getCause());
        }
    }

    private Method setterMethod() {
        if (getAnnotation().derived())
            return null;
        String setterName = this.setterName();
        try {
            Method $ = getter.getDeclaringClass().getDeclaredMethod(setterName, getter.getReturnType());
            $.setAccessible(true);
            return $;
        } catch (NoSuchMethodException ex) {
            throw new AssertionError(ex);
        } catch (SecurityException ex) {
            throw new AssertionError(ex);
        }
    }

    private String setterName() {
        String name = getter.getName();
        if (name.startsWith("is"))
            name = Character.toLowerCase(name.charAt(2)) + name.substring(3);
        if (name.startsWith("get"))
            name = Character.toLowerCase(name.charAt(3)) + name.substring(4);
        return "set" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    @Override
    public void write(Object element, Object value) {
        try {
            setter.invoke(element, value);
        } catch (IllegalArgumentException ex) {
            throw new AssertionError(ex);
        } catch (IllegalAccessException ex) {
            throw new AssertionError(ex);
        } catch (InvocationTargetException ex) {
            throw new AssertionError(ex);
        }
    }

}
