package com.netflix.archaius;

import com.netflix.archaius.api.StringConverter;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Currency;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.xml.bind.DatatypeConverter;

public final class StringConverterRegistryBuilder {

    static final Map<Type, StringConverter<?>> DEFAULT_CONVERTERS = new IdentityHashMap<>(75);
    
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
    
    private final Map<Type, StringConverter<?>> known = new IdentityHashMap<>();
    private Function<Type, StringConverter<?>> notFoundFactory = (type) -> {
        throw new IllegalArgumentException("No converter registered for type " + type);
    }; 
    
    public StringConverterRegistryBuilder() {
        known.putAll(DEFAULT_CONVERTERS);
    }
    
    public <T> StringConverterRegistryBuilder withConverter(Type type, StringConverter<T> converter) {
        known.put(type, converter);
        return this;
    }
    
    public StringConverterRegistryBuilder withNotFoundConverter(Function<Type, StringConverter<?>> factory) {
        this.notFoundFactory = factory;
        return this;
    }
    
    public StringConverter.Registry build() {
        return create(new IdentityHashMap<>(known));
    }
    
    private StringConverter.Registry create(Map<Type, StringConverter<?>> known) {
        return new StringConverter.Registry() {
            private final Map<Type, StringConverter<?>> dynamic = new ConcurrentHashMap<>();
            
            @SuppressWarnings("unchecked")
            @Override
            public <T> StringConverter<T> get(Type type) {
                if (known.containsKey(type)) {
                    return (StringConverter<T>) known.get(type);
                }
          
                return (StringConverter<T>) dynamic.computeIfAbsent(type, t -> {
                    if (type instanceof Class) {
                        final Class<?> cls = (Class<?>) type;

                        if (cls.isArray()) {
                            final StringConverter<?> converter = get(cls.getComponentType());
                            return encoded -> {
                                String[] components = encoded.split(",");
                                Object ar = Array.newInstance(cls.getComponentType(), components.length);
                                for (int i = 0; i < components.length; i++) {
                                    Array.set(ar, i, converter.convert(components[i]));
                                }
                                return ar;
                            };
                        } else {
                            StringConverter<?> converter = forClass(cls);
                            if (converter != null) {
                                return converter;
                            }
                        }
                    }
                    
                    if (type instanceof ParameterizedType) {
                        ParameterizedType pType = (ParameterizedType) type;
                        if (pType.getRawType() == Map.class) {
                            return forMap(pType.getActualTypeArguments()[0], pType.getActualTypeArguments()[1]);
                        } else if (pType.getRawType() == List.class) {
                            return forCollection(pType.getActualTypeArguments()[0], ArrayList::new, Collections::emptyList);
                        } else if (pType.getRawType() == LinkedList.class) {
                            return forCollection(pType.getActualTypeArguments()[0], LinkedList::new, LinkedList::new);
                        } else if (pType.getRawType() == Set.class) {
                            return forCollection(pType.getActualTypeArguments()[0], LinkedHashSet::new, Collections::emptySet);
                        } else if (pType.getRawType() == SortedSet.class) {
                            return forCollection(pType.getActualTypeArguments()[0], TreeSet::new, Collections::emptySortedSet);
                        }
                    }
                    
                    return (StringConverter<T>) notFoundFactory.apply(type);
                });
            }

            private StringConverter<?> forMap(Type keyType, Type valueType) {
                StringConverter<?> keyConverter = get(keyType);
                StringConverter<?> valueConverter = get(valueType);
                return value -> Arrays.stream(value.toString().split(";"))
                                .map(String::trim)
                                .filter(str -> !str.isEmpty())
                                .map(str -> str.split("="))
                                .peek(pair -> {
                                    if (pair.length != 2) {
                                        throw new IllegalArgumentException("Invalid map string : " + value);
                                    }
                                 })
                                .collect(Collectors.toMap(
                                        pair -> keyConverter.convert(pair[0]),
                                        pair -> valueConverter.convert(pair[1]),
                                        (u, v) -> { throw new IllegalStateException("Duplicate keys not allowed"); },
                                        LinkedHashMap::new
                                 ));
            }
            
            private StringConverter<?> forCollection(Type elementType, Supplier<Collection> supplier, Supplier<Collection> defaultSupplier) {
                StringConverter<?> elementConverter = get(elementType);
                return value -> Arrays.stream(value.toString().split(","))
                        .map(String::trim)
                        .filter(str -> !str.isEmpty())
                        .map(elementConverter::convert)
                        .collect(Collectors.toCollection(supplier));
            }
        };
    }
    
    static StringConverter<?> forClass(Class<?> cls) {
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