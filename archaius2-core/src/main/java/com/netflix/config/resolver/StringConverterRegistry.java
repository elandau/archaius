package com.netflix.config.resolver;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Period;
import java.time.ZonedDateTime;
import java.util.BitSet;
import java.util.Currency;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import javax.xml.bind.DatatypeConverter;

class StringConverterRegistry {

    public static final Map<Type, Function<String, ?>> DEFAULT_CONVERTERS = new IdentityHashMap<>(75);
    
    static {
        DEFAULT_CONVERTERS.put(String.class, v->v);
        DEFAULT_CONVERTERS.put(boolean.class, v->{
            if (v.equalsIgnoreCase("true") || v.equalsIgnoreCase("yes") || v.equalsIgnoreCase("on")) {
                return Boolean.TRUE;
            }
            else if (v.equalsIgnoreCase("false") || v.equalsIgnoreCase("no") || v.equalsIgnoreCase("off")) {
                return Boolean.FALSE;
            }
            throw new IllegalArgumentException("Error parsing value '" + v, new Exception("Expected one of [true, yes, on, false, no, off] but got " + v));
        });
        DEFAULT_CONVERTERS.put(Boolean.class, DEFAULT_CONVERTERS.get(boolean.class));
        DEFAULT_CONVERTERS.put(Integer.class, Integer::valueOf);
        DEFAULT_CONVERTERS.put(int.class, Integer::valueOf);
        DEFAULT_CONVERTERS.put(long.class, Long::valueOf);
        DEFAULT_CONVERTERS.put(Long.class, Long::valueOf);
        DEFAULT_CONVERTERS.put(short.class, Short::valueOf);
        DEFAULT_CONVERTERS.put(Short.class, Short::valueOf);
        DEFAULT_CONVERTERS.put(byte.class, Byte::valueOf);
        DEFAULT_CONVERTERS.put(Byte.class, Byte::valueOf);
        DEFAULT_CONVERTERS.put(double.class, Double::valueOf);
        DEFAULT_CONVERTERS.put(Double.class, Double::valueOf);
        DEFAULT_CONVERTERS.put(float.class, Float::valueOf);
        DEFAULT_CONVERTERS.put(Float.class, Float::valueOf);
        DEFAULT_CONVERTERS.put(BigInteger.class, BigInteger::new);
        DEFAULT_CONVERTERS.put(BigDecimal.class, BigDecimal::new);
        DEFAULT_CONVERTERS.put(AtomicInteger.class, s->new AtomicInteger(Integer.parseInt(s)));
        DEFAULT_CONVERTERS.put(AtomicLong.class, s->new AtomicLong(Long.parseLong(s)));
        DEFAULT_CONVERTERS.put(Duration.class, Duration::parse);
        DEFAULT_CONVERTERS.put(Period.class, Period::parse);
        DEFAULT_CONVERTERS.put(LocalDateTime.class, LocalDateTime::parse);
        DEFAULT_CONVERTERS.put(LocalDate.class, LocalDate::parse);
        DEFAULT_CONVERTERS.put(LocalTime.class, LocalTime::parse);
        DEFAULT_CONVERTERS.put(OffsetDateTime.class, OffsetDateTime::parse);
        DEFAULT_CONVERTERS.put(OffsetTime.class, OffsetTime::parse);
        DEFAULT_CONVERTERS.put(ZonedDateTime.class, ZonedDateTime::parse);
        DEFAULT_CONVERTERS.put(Instant.class, v->Instant.from(OffsetDateTime.parse(v)));
        DEFAULT_CONVERTERS.put(Date.class, v->new Date(Long.parseLong(v)));
        DEFAULT_CONVERTERS.put(Currency.class, Currency::getInstance);
        DEFAULT_CONVERTERS.put(BitSet.class, v->BitSet.valueOf(DatatypeConverter.parseHexBinary(v)));
    }
    
    public static class Builder {
        private StringConverterRegistry registry = new StringConverterRegistry();
        
        private Builder() {
            registry.known.putAll(DEFAULT_CONVERTERS);
        }
        
        public StringConverterRegistry build() {
            try {
                return new StringConverterRegistry();
            } finally {
                registry = null;
            }
        }
    }
    
    public static Builder newBuilder() {
        return new Builder();
    }
    
    private final Map<Type, Function<String, ?>> known = new IdentityHashMap<>();
    private final Map<Type, Function<String, ?>> dynamic = new ConcurrentHashMap<>();
    
    public Function<String, ?> getConverter(Type type) {
        if (known.containsKey(type)) {
            return known.get(type);
        }
  
        if (type instanceof Class) {
            final Class<?> cls = (Class<?>) type;

            return dynamic.computeIfAbsent(type, t -> {
                if (cls.isArray()) {
                    final Function<String, ?> converter = getConverter(cls.getComponentType());
                    return encoded -> {
                        String[] components = encoded.split(",");
                        Object ar = Array.newInstance(cls.getComponentType(), components.length);
                        for (int i = 0; i < components.length; i++) {
                            Array.set(ar, i, converter.apply(components[i]));
                        }
                        return ar;
                    };
                } else {
                    return forClass(cls);
                }
            });
        }
        
        return null;
    }
    
    static Function<String, ?> forClass(Class<?> cls) {
        // Next look a valueOf(String) static method
        try {
            Method method;
            try {
                method = cls.getMethod("valueOf", String.class);
                return encoded -> {
                    try {
                        return method.invoke(null, encoded);
                    } catch (Exception e) {
                        throw new IllegalArgumentException("Unable to convert '" + encoded + "' to " + cls.getName(), e);
                    }
                };
            } catch (NoSuchMethodException e1) {
                // Next look for a T(String) constructor
                Constructor<?> c;
                try {
                    c = (Constructor<?>) cls.getConstructor(String.class);
                    return encoded -> { 
                        try {
                            return c.newInstance(encoded);
                        } catch (Exception e) {
                            throw new IllegalArgumentException("Unable to convert '" + encoded + "' to " + cls.getName(), e);
                        }
                    };
                }
                catch (NoSuchMethodException e) {
                    return null;
                }
            }
        }
        catch (Exception e) {
            throw new RuntimeException("Unable to instantiate value of type " + cls.getCanonicalName(), e);
        }
    }
}
