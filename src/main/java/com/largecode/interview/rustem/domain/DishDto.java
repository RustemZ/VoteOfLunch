package com.largecode.interview.rustem.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.NotEmpty;

import java.math.BigDecimal;

/**
 * Data Transfer object for LunchMenu
 * Created by r.zhunusov on 25.12.2015.
 */
public class DishDto {

    @ApiModelProperty(value = "Id of Dish from DB.", required = false)
    private Long idDish=DomainUtils.NO_ID;

    @NotEmpty
    @ApiModelProperty(value = "Description of DishDto.", required = true)
    String name="";

    @NotEmpty
    @ApiModelProperty(value = "Price of DishDto in string format.", required = true)
    String priceStr ="";

    @JsonIgnore
    BigDecimal validatedPrice;

    public Long getIdDish() {
        return idDish;
    }

    public void setIdDish(Long idDish) {
        this.idDish = idDish;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPriceStr() {
        return priceStr;
    }

    public void setPriceStr(String priceStr) {
        this.priceStr = priceStr;
    }

    public BigDecimal getValidatedPrice() {
        return validatedPrice;
    }

    public void setValidatedPrice(BigDecimal validatedPrice) {
        this.validatedPrice = validatedPrice;
    }
}
