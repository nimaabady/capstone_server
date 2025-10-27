# For user login and register
    Please run UserLoginApplication within the userLogin at path server-parent/user-login/src/main/java/com/capstone/server/userLogin/UserLoginApplication.java

## debugging
    if theres a rabbitmq error please add this to your application properties inside of user-login: spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration
