# Overview

# Configuration

## Dynamic properties

# PropertySource

Raw containers of property sources.  A single PropertySource is normally associated with a single file or URL. 

## Immutable PropertySource
### SystemPropertySource
### EnvironmentPropertySource
### EmptyPropertySource
### ImmutablePropertySource

#### Bundles
#### Properties files
#### Cascading strategy
#### Yaml-x files

## Dynamic PropertySource
### MutablePropertySource
### PollingPropertySource

## Composite PropertySource

### CompositePropertySource

### LayeredPropertySource

Pre-defined layers

- TEST
- OVERRIDE
- SYSTEM
- ENVIRONMENT
- ENVIRONMENT_DEFAULTS
- REMOTE_OVERRIDE
- APPLICATION_OVERRIDE
- APPLICATION
- LIBRARIES

## Interpolation PropertySource
- InterpolatingPropertySource

## Utility classes
### Visitors
- FlattendNames
- PrintStream
- PropertyOverride
- SLF4JConfig

## Change notifications

# TypeResolver

TypeResolvers are used to convert property values into Java primitives and complex types.  Supported resolvers are

Basic resolver

Complex resolvers

- List
- Map
- Set
- Proxy
- Object

# Guice


