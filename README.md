[![Build status](https://github.com/Realiserad/servlet-logging-filter/actions/workflows/build-status.yml/badge.svg)](https://github.com/Realiserad/servlet-logging-filter/actions/workflows/build-status.yml)

# Servlet logging filter

Servlet filter for logging HTTP requests and responses as JSON.

Forked from Libor Ondrušek's code and refactored to be compatible with Jakarta EE.

## Java compatibility

This project is based on Java 17, but can be built on Java 11 if you skip the tests.
```
mvn clean package -Dmaven.test.skip=true -Djava.version=11
```

## Usage

The filter implements the ``jakarta.servlet.Filter`` interface from Jakarta Servlet API 6.

You can register by editing ``WEB-INF/web.xml``, and adding the following XML:
```xml
<filter>
	<filter-name>LoggingFilter</filter-name>
	<filter-class>org.stormhub.jakarta.LoggingFilter</filter-class>
	<init-param>
       <param-name>loggerName</param-name>
       <param-value>MyLogger</param-value>
   </init-param>
</filter>
<filter-mapping>
	<filter-name>LoggingFilter</filter-name>
	<url-pattern>/*</url-pattern>
</filter-mapping>
```

Or, by dynamically registering an instance of ``LoggingFilter`` in ``jakarta.servlet.ServletContext`` like this:
```java
public void onStartup(final ServletContext servletContext) throws ServletException {
    final Dynamic registration = servletContext.addFilter("LoggingFilter", new LoggingFilter());
    registration.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), false, "/*");
}
```
### Initialisation parameters
| Name           | Default    | Description                                                                        |
|----------------|------------| -----------------------------------------------------------------------------------|
| loggerName     | Class name | Logger name for output.                                                            |
| maxContentSize | 1024 bytes | The maximal logged body size in bytes.                                             |
| excludedPaths  | empty      | Comma separated list of URL prefixes to exclude from logging, e.g.: "/api,/admin". |
| requestPrefix  | REQUEST:   | TheFirst word on the request output line.                                          |
| responsePrefix | RESPONSE:  | First word on response output line.                                                |
| requestMarker  | RESPONSE   | Slf4J marker for the request.                                                      |
| responseMarker | RESPONSE   | Slf4J marker for the response.                                                     |
| disablePrefix  | false      | No prefixes are logged if set to ``true``.                                         |
| disableMarkers | false      | No Slf4J markers are logged if set to ``true``.                                    |

## Customization
There are few methods for you to rewrite if you want:

The filtering business logic is done in ``doFilter``:
```java
jakarta.servlet.filter.logging.LoggingFilter.doFilter
```

A description of the request is done in ``getRequestDescription``. The default is to create a JSON object.
```java
jakarta.servlet.filter.logging.LoggingFilter.getRequestDescription
```

A description of the request is done in ``getResponseDescription``. The default is to create a JSON object.
```java
jakarta.servlet.filter.logging.LoggingFilter.getResponseDescription
```

## Example output

Here is an example of what the output could look like:
```
REQUEST: {"sender": "127.0.0.1", "method": "GET", "path": "http://localhost:8080/test", "params": {"param1": "1000"}, "headers": {"Accept": "application/json", "Content-Type":"text/plain"}, "body": "Test request body"}
RESPONSE: {"status":200,"headers":{"Content-Type":"text/plain"},"body":"Test response body"}
```

Log lines are written on DEBUG level.
