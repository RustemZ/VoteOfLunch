package com.largecode.interview.rustem.service.validator;

import com.largecode.interview.rustem.domain.DishDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.math.BigDecimal;

/**
 * Created by r.zhunusov on 25.12.2015.
 */
@Component
public class DishDtoValidator implements Validator {
    private static final Logger LOGGER = LoggerFactory.getLogger(DishDtoValidator.class);

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.equals(DishDto.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        LOGGER.debug("Validating {}", target);
        DishDto userDto = (DishDto) target;
        validatePrice(errors, userDto);
    }

    private void validatePrice(Errors errors, DishDto dishDto) {
        try {
            BigDecimal price = new BigDecimal( dishDto.getPriceStr() );
            dishDto.setValidatedPrice( price );
        }
        catch (Exception ex) {
            String errorMessage = String.format(
                    "Can not convert string '%s' to BigDecimal. " +
                    "Use only number and '.' in Price value. Error message: '%s'.",
                    dishDto.getPriceStr(), ex.getMessage());
            errors.reject("price.not_big_decimal", errorMessage );
        }
    }


}
