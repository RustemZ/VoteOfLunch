package com.largecode.interview.rustem;

import com.largecode.interview.rustem.controller.FakeSpringFoxController;
import com.largecode.interview.rustem.controller.LunchMenusController;
import com.largecode.interview.rustem.controller.RestaurantsController;
import com.largecode.interview.rustem.controller.UsersController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackageClasses = {
        SwaggerConfig.class, UsersController.class, RestaurantsController.class, LunchMenusController.class
        , FakeSpringFoxController.class
})

public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

