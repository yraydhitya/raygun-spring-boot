# raygun-spring-boot

## Features

- Auto-configures a `RaygunTemplate` bean.
- Integrations:
    - Spring Web MVC controllers in a Servlet environment
    - Spring Web Services endpoints
- Can exclude exception types sent.
- Customizeable properties in the [properties documentation](PROPERTIES.md).
- Messages sending is asynchronous by default.
- Auto-configures a mock `RaygunTemplate` bean in tests.

## Usage

Add the dependency in the implementation configuration

```
dependencies {
    implementation 'com.midtrans:raygun-spring-boot-starter:0.5.0'
}
```

Set `raygun.api-key` property with the Raygun API key retrieved from the Raygun Application settings page.

## Integrations

### Web MVC Servlet Integration

Uncaught exceptions thrown from `@Controller` and `@RestController` methods are logged and sent to Raygun.

Responses can be built using Web MVC exception handling mechanism using exception annotated with `@ResponseStatus` or using `@ExceptionHandler` method in a `@Controller` or a `@ControllerAdvice`  as documented in the [reference documentation](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc-exceptionhandlers).

If the uncaught exceptions are sent in `@ExceptionHandler` methods, the uncaught exceptions will be sent to Raygun twice.

```java
@RestController
class UserRestController {

    //The IndexOutOfBoundsException will be sent to Raygun once
    @GetMapping("/uncaught")
    void uncaught() {
        throw new IndexOutOfBoundsException();
    }

    //The NullPointerException will be sent to Raygun twice
    @GetMapping("/controllerAdvice")
    void controllerAdvice() {
        throw new NullPointerException();
    }
}

@ControllerAdvice
class UserControllerAdvice {

    @Autowired
    RaygunTemplate raygunTemplate;

    @ExceptionHandler
    ResponseEntity<?> handle(NullPointerException ex) {
        raygunTemplate.send(ex);
        return ResponseEntity.internalServerError().build();
    }
}
```

#### Web MVC Servlet Integration in Tests

The Web MVC integration is configured when using [@WebMvcTest annotation](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#features.testing.spring-boot-applications.spring-mvc-tests).

Uncaught exceptions will still be caught and logged, but they are not sent to Raygun. Please refer to the [testing section](#testing)

```java
@WebMvcTest
class UserWebMvcTest {

    @Autowired
    MockMvc mockMvc;

    //Exceptions thrown by controller methods are caught and logged, but not sent to Raygun
    @Test
    void responseStatus() throws Exception {
        mockMvc.perform(get("/responseStatus"))
            .andExpect(status().isBadGateway());
    }
}
```

### Web Services Integration

Uncaught exceptions thrown from `@Endpoint` methods are logged and sent to Raygun.

```java
@Endpoint
class UserEndpoint {

    @PayloadRoot(localPart = "uncaught")
    void uncaught() {
        throw new IndexOutOfBoundsException();
    }
}
```

By default, responses for uncaught exceptions are SOAP Fault with the exception's message as the fault string as documented in the [reference documentation](https://docs.spring.io/spring-ws/docs/current/reference/html/#server-endpoint-exception-resolver).

## Exceptions Exclusion

To exclude exceptions being sent, register exception types through a `RaygunExceptionExcludeRegistrar` bean.

```java
@Component
class UserRaygunExcludeExceptionRegistrar implements RaygunExceptionExcludeRegistrar {

    @Override
    public void registerExceptions(RaygunExceptionExcludeRegistry registry) {
        registry.registerException(RuntimeException.class);
    }
}
```

## Messages Sending

`RaygunTemplate` will use a `TaskExecutor` to send the Raygun mesages.

In an idiomatic Spring Boot application, a `ThreadPoolTaskExecutor` bean is auto-configured with a sensible defaults and can be customized as documented in the [reference documentation](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#features.task-execution-and-scheduling).

Depending on how many `TaskExecutor` beans configured in the `ApplicationContext`, the behavior is different as below:

- no `TaskExecutor` bean configured, `RaygunTemplate` will use a `SyncTaskExecutor` to send the messages synchronously,
- one `TaskExecutor` bean configured, `RaygunTemplate` will use the configured `TaskExecutor` bean,
- multiple `TaskExecutor` beans configured
    - no bean named `raygunTaskExecutor` configured, `RaygunTemplate` will use a `SyncTaskExecutor` to send the messages synchronously,
    - one `TaskExecutor` bean named `raygunTaskExecutor` configured, `RaygunTemplate` will use the `raygunTaskExecutor` bean.

To know the behaviors and tradeoffs of using `ThreadPoolTaskExecutor` please refer to `RaygunTemplateMessagesSendingTest` and `RaygunTemplateMessagesRejectionTest`.

## Testing

In tests, `RaygunTemplate` bean is mocked and does not send exceptions to Raygun.

This will apply to `@SpringBootTest` and test slices.

```java
@SpringBootTest
class UserTest {

    @Autowired
    RaygunTemplate raygunTemplate;

    //The RaygunTemplate does not send the exception to Raygun
    @Test
    void contextLoads() {
        raygunTemplate.send(new IllegalArgumentException());
    }
}
```

## Contributing

Please read the [contributing guide](CONTRIBUTING.md).
